param(
    [string]$EsUrl = "http://127.0.0.1:9200",
    [switch]$StartEsWithDocker,
    [switch]$StartEsLocal,
    [string]$EsBatPath = "D:\Develop\elasticsearch-9.3.2\bin\elasticsearch.bat",
    [string]$EsContainerName = "online-shopping-es",
    [string]$MavenCommand,
    [int]$EsWaitSeconds = 180,
    [switch]$SkipDbCredentialCheck,
    [switch]$SkipEsHealthCheck
)

$ErrorActionPreference = "Stop"
$ProjectRoot = Split-Path -Parent $PSScriptRoot

if (-not $MavenCommand) {
    $MavenCommand = Join-Path $ProjectRoot "mvnw.cmd"
}

function Test-EsHealth {
    param([string]$Url)
    try {
        $response = Invoke-RestMethod -Method Get -Uri $Url -TimeoutSec 3
        if ($null -ne $response -and $response.cluster_name) {
            return $true
        }
    }
    catch {
        return $false
    }
    return $false
}

function Wait-EsReady {
    param(
        [string]$Url,
        [int]$WaitSeconds
    )

    $maxRetry = [Math]::Max(1, [int]($WaitSeconds / 2))
    for ($i = 0; $i -lt $maxRetry; $i++) {
        Start-Sleep -Seconds 2
        if (Test-EsHealth -Url $Url) {
            return $true
        }
    }

    return $false
}

Write-Host "[1/4] PowerShell version: $($PSVersionTable.PSVersion)" -ForegroundColor Cyan

if ($StartEsWithDocker) {
    Write-Host "[2/4] Starting Elasticsearch container: $EsContainerName" -ForegroundColor Cyan
    docker rm -f $EsContainerName 2>$null | Out-Null
    docker run -d --name $EsContainerName -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:9.3.2 | Out-Null

    if (-not (Wait-EsReady -Url $EsUrl -WaitSeconds $EsWaitSeconds)) {
        throw "Elasticsearch did not become ready in time. Check: docker logs $EsContainerName"
    }
}

if ($StartEsLocal) {
    if (Test-EsHealth -Url $EsUrl) {
        Write-Host "[2/4] Elasticsearch already running at $EsUrl, skip local startup." -ForegroundColor Yellow
    }
    else {
    Write-Host "[2/4] Starting local Elasticsearch: $EsBatPath" -ForegroundColor Cyan
    if (-not (Test-Path $EsBatPath)) {
        throw "Local ES startup file not found: $EsBatPath"
    }

    $esHome = Split-Path -Parent (Split-Path -Parent $EsBatPath)
    Start-Process -FilePath $EsBatPath -WorkingDirectory $esHome | Out-Null

    if (-not (Wait-EsReady -Url $EsUrl -WaitSeconds $EsWaitSeconds)) {
        throw "Local Elasticsearch did not become ready in time. Check Elasticsearch console/logs."
    }
    }
}

if (-not $SkipEsHealthCheck) {
    Write-Host "[3/4] Checking Elasticsearch: $EsUrl" -ForegroundColor Cyan
    if (-not (Test-EsHealth -Url $EsUrl)) {
        throw "Cannot connect to Elasticsearch at $EsUrl. Start ES first (or use -StartEsLocal / -StartEsWithDocker)."
    }
}

$env:APP_SEARCH_ES_ENABLED = "true"
$env:ES_URIS = $EsUrl

if (-not $env:DB_USERNAME) {
    $env:DB_USERNAME = "root"
}

if (-not $SkipDbCredentialCheck -and [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    throw 'DB_PASSWORD is empty. Set it before startup, e.g. $env:DB_PASSWORD="your_mysql_password" (or use -SkipDbCredentialCheck).'
}

Write-Host "[4/4] Starting backend with ES enabled..." -ForegroundColor Cyan
Write-Host "APP_SEARCH_ES_ENABLED=$env:APP_SEARCH_ES_ENABLED" -ForegroundColor Green
Write-Host "ES_URIS=$env:ES_URIS" -ForegroundColor Green
Write-Host "DB_USERNAME=$env:DB_USERNAME" -ForegroundColor Green
if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    Write-Host "DB_PASSWORD=(empty)" -ForegroundColor Yellow
} else {
    Write-Host "DB_PASSWORD=(set)" -ForegroundColor Green
}

Push-Location $ProjectRoot
try {
    & $MavenCommand spring-boot:run
}
finally {
    Pop-Location
}
