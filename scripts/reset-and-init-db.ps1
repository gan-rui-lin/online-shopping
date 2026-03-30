param(
    [string]$DbUser = "root",
    [string]$DbPassword,
    [string]$DbName = "online_shopping",
    [string]$MysqlExe = "mysql"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($DbPassword)) {
    if (-not [string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        $DbPassword = $env:DB_PASSWORD
    } else {
        throw "DbPassword is required. Pass -DbPassword or set DB_PASSWORD environment variable."
    }
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$schemaPath = Join-Path $repoRoot "src/main/resources/schema.sql"
$initDataPath = Join-Path $repoRoot "src/main/resources/init-data.sql"

if (-not (Test-Path $schemaPath)) {
    throw "schema.sql not found: $schemaPath"
}
if (-not (Test-Path $initDataPath)) {
    throw "init-data.sql not found: $initDataPath"
}

$schemaForMysql = ($schemaPath -replace "\\", "/")
$initDataForMysql = ($initDataPath -replace "\\", "/")

Write-Host "[1/4] Drop and recreate database: $DbName"
$resetSql = "DROP DATABASE IF EXISTS $DbName; CREATE DATABASE $DbName DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
& $MysqlExe "-u$DbUser" "-p$DbPassword" -e $resetSql
if ($LASTEXITCODE -ne 0) {
    throw "Failed to recreate database $DbName"
}

Write-Host "[2/4] Apply schema.sql"
& $MysqlExe "-u$DbUser" "-p$DbPassword" $DbName -e "source $schemaForMysql"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to apply schema.sql"
}

Write-Host "[3/4] Apply init-data.sql"
& $MysqlExe "-u$DbUser" "-p$DbPassword" $DbName -e "source $initDataForMysql"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to apply init-data.sql"
}

Write-Host "[4/4] Verify seeded products"
& $MysqlExe "-u$DbUser" "-p$DbPassword" $DbName -e "SELECT COUNT(*) AS spu_count FROM product_spu WHERE id BETWEEN 1000 AND 1011; SELECT COUNT(*) AS image_count FROM product_image WHERE id BETWEEN 3000 AND 3011;"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to verify seeded data"
}

Write-Host "Database reset and seed completed."
