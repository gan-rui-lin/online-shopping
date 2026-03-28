# Online Shopping Platform

A one-stop intelligent e-commerce platform built with Spring Boot 3.

## Tech Stack

- Spring Boot 3.3.4 + MyBatis-Plus 3.5.7
- Spring Security + JWT
- MySQL 8 + Redis
- Knife4j (Swagger)

## Quick Start

### 1. Prerequisites

- JDK 17+
- MySQL 8.x
- Redis 6.x+

### 2. Initialize Database

```bash
mysql -u root -p < src/main/resources/schema.sql
```

### 3. Configure Environment Variables

Copy the example and fill in your values:

```bash
cp .env.example .env
```

Set the variables before running:

```bash
export DB_PASSWORD=your_mysql_password
export REDIS_PASSWORD=your_redis_password  # leave empty if no auth
```

Or pass them inline:

```bash
DB_PASSWORD=yourpass ./mvnw spring-boot:run
```

All configurable variables:

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:mysql://localhost:3306/online_shopping?...` | JDBC URL |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | *(empty)* | MySQL password |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | *(empty)* | Redis password |
| `APP_SEARCH_ES_ENABLED` | `true` | Enable Elasticsearch search |
| `ES_URIS` | `http://127.0.0.1:9200` | Elasticsearch endpoint |
| `APP_AI_ENABLED` | `false` | Enable external AI gateway |
| `APP_AI_BASE_URL` | `https://openrouter.ai/api/v1/chat/completions` | OpenAI-compatible API URL |
| `APP_AI_API_KEY` | *(empty)* | AI API key |
| `APP_AI_MODEL` | `deepseek/deepseek-chat-v3-0324:free` | Model name |
| `JWT_SECRET` | *(built-in default)* | JWT signing key (Base64) |

### 4. Run

```bash
./mvnw spring-boot:run
```

### Windows: Start backend with Elasticsearch enabled

PowerShell script (checks ES, sets env vars, then starts backend):

```powershell
$env:DB_PASSWORD="your_mysql_password"
./scripts/start-es-and-backend.ps1
```

If you want the script to start ES using Docker automatically:

```powershell
./scripts/start-es-and-backend.ps1 -StartEsWithDocker
```

If you want the script to start local ES from D drive automatically:

```powershell
./scripts/start-es-and-backend.ps1 -StartEsLocal
```

Custom local ES path example:

```powershell
./scripts/start-es-and-backend.ps1 -StartEsLocal -EsBatPath "D:\Develop\elasticsearch-9.3.2\bin\elasticsearch.bat"
```

If ES startup is slow, extend wait time:

```powershell
./scripts/start-es-and-backend.ps1 -StartEsLocal -EsWaitSeconds 300
```

If your local MySQL allows empty password, you can bypass DB password pre-check:

```powershell
./scripts/start-es-and-backend.ps1 -SkipDbCredentialCheck
```

### 5. API Documentation

Open [http://localhost:8080/doc.html](http://localhost:8080/doc.html) after startup.

### Default Admin Account

| Username | Password |
|----------|----------|
| admin | admin123 |

## Project Structure

```
src/main/java/com/helloworld/onlineshopping/
├── common/          # Shared: Result, security, config, enums, exceptions
└── modules/
    ├── auth/        # Register, login, JWT
    ├── user/        # Profile, password
    ├── merchant/    # Shop apply, audit
    ├── product/     # SPU/SKU, category, search
    ├── cart/        # Shopping cart
    ├── address/     # Delivery address
    ├── order/       # Order lifecycle, payment, timeout
    ├── review/      # Product reviews
    └── admin/       # Dashboard, audit
```

## Detailed Documentation

See [docs/DEVELOPMENT_GUIDE.md](docs/DEVELOPMENT_GUIDE.md) for full API reference, database schema, and architecture details.
