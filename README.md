# Spring Boot 3.5.14 CRUD REST API — Docker Deployment Guide

---
## Overview
This is a simple Spring Boot application using **MySQL database**. It also integrates with **multiple Docker strategies** for deployment.

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Configuration Files](#configuration-files)
- [Option 1 — Single-Stage Dockerfile](#option-1--single-stage-dockerfile)
- [Option 2 — Multi-Stage Dockerfile](#option-2--multi-stage-dockerfile)
- [Option 3 — Docker Compose with .env](#option-3--docker-compose-with-env)
- [Option 4 — Docker Compose with Docker Secrets](#option-4--docker-compose-with-docker-secrets)
- [Verify the Application](#verify-the-application)
- [Stopping and Cleaning Up](#stopping-and-cleaning-up)

---

## Prerequisites
Make sure the following tools are installed on your machine before you begin.

| Tool | Minimum Version | Check |
|------|----------------|-------|
| Docker | 24.x | `docker --version` |
| Docker Compose | 2.x (plugin) | `docker compose version` |
| Java JDK | 17 | `java -version` |
| Maven | 3.9.x | `mvn -version` |

> **Note:** Docker Compose v2 is bundled with Docker Desktop. On Linux, install the plugin with `sudo apt install docker-compose-plugin`.

---

## Configuration Files

### `.env`

Used by `docker-compose.yml` and `docker-compose.secrets.yml` to inject environment variables.

```dotenv
# Application
APP_NAME=sb-docker-stack
APP_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Database
DB_DATABASE=mydb
DB_USER=appuser
DB_PORT=3306
DB_PASSWORD=apppassword
DB_ROOT_PASSWORD=rootpassword
DB_JDBC_OPTS=?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

# JVM
JAVA_OPTS=-Xms512m -Xmx1024m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0
```

> **Security:** Add `.env` to `.gitignore`. Never commit real credentials.

### `secrets/mysql_password.txt`

Used by `docker-compose.secrets.yml` to mount a Docker secret.

```
apppassword
```

Create the file and directory:

```bash
mkdir -p secrets
echo "apppassword" > secrets/mysql_password.txt
```

> **Security:** Add the `secrets/` directory to `.gitignore`.

### `.gitignore` — Recommended entries

```gitignore
.env
secrets/
target/
*.class
```

---

## Option 1 — Single-Stage Dockerfile

**Use case:** You have already built the JAR locally with Maven and want the fastest image build time.

### Step 1 — Build the JAR locally

```bash
mvn clean package -DskipTests
```

This produces `target/sb-docker-stack.jar`.

### Step 2 — Build the Docker image

```bash
docker build --build-arg JAR_FILE=target/sb-docker-stack.jar -f Dockerfile.single -t sb_docker_stack_app:single .
```

### Step 3 — Start a MySQL container

```bash
docker network create docker_stack_network

docker run -d --name mysql_db -e MYSQL_DATABASE=mydb -e MYSQL_USER=appuser -e MYSQL_PASSWORD=apppassword -e MYSQL_ROOT_PASSWORD=rootpassword -v mysql_data:/var/lib/mysql --network docker_stack_network mysql:8.0
```

### Step 4 — Run the application container

```bash
docker run -d --name sb_docker_stack_app --network docker_stack_network -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql_db:3306/mydb -e SPRING_DATASOURCE_USERNAME=appuser -e SPRING_DATASOURCE_PASSWORD=apppassword sb_docker_stack_app:single
```

### Step 5 — Check the logs

```bash
docker logs -f sb_docker_stack_app
```

---

## Option 2 — Multi-Stage Dockerfile

**Use case:** You want Docker itself to compile and package the application — no local Maven or JDK required on the host.

### Step 1 — Build the Docker image

```bash
docker build -f Dockerfile.multi -t sb_docker_stack_app:multi .
```

> The first build downloads Maven dependencies and may take a few minutes. Subsequent builds are fast thanks to layer caching — dependency layers are only re-downloaded when `pom.xml` changes.

### Step 2 — Start a MySQL container

```bash
docker network create docker_stack_network

docker run -d --name mysql_db -e MYSQL_DATABASE=mydb -e MYSQL_USER=appuser -e MYSQL_PASSWORD=apppassword -e MYSQL_ROOT_PASSWORD=rootpassword -v mysql_data:/var/lib/mysql --network docker_stack_network mysql:8.0
```

### Step 3 — Run the application container

```bash
docker run -d --name sb_docker_stack_app --network docker_stack_network -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql_db:3306/mydb -e SPRING_DATASOURCE_USERNAME=appuser -e SPRING_DATASOURCE_PASSWORD=apppassword sb_docker_stack_app:multi
```

### Step 4 — Check the logs

```bash
docker logs -f sb_docker_stack_app
```

---

## Option 3 — Docker Compose with `.env`

**Use case:** Spin up both MySQL and the Spring Boot app together using a single command. Credentials are read from the `.env` file.

### Step 1 — Build and start all services

```bash
docker compose -f docker-compose.yml up --build -d
```

- `--build` forces a fresh image build from `Dockerfile.multi` (as specified in `docker-compose.yml`).
- `-d` runs containers in the background.

Docker Compose will automatically:
1. Build the Spring Boot image.
2. Start MySQL and wait for its health check to pass.
3. Start the app only after MySQL is healthy (`condition: service_healthy`).

### Step 2 — Watch the startup logs

```bash
docker compose -f docker-compose.yml logs -f
```

### Step 3 — Scale or rebuild a single service (optional)

```bash
# Rebuild only the app image
docker compose -f docker-compose.yml build app

# Restart only the app container
docker compose -f docker-compose.yml restart app
```

---

## Option 4 — Docker Compose with Docker Secrets

**Use case:** More secure deployment where the database password is mounted as a file inside the container rather than passed as a plain environment variable.

### How it works

Docker Compose mounts the contents of `secrets/mysql_password.txt` to `/run/secrets/mysql_password` inside each container that declares the secret. MySQL reads it via `MYSQL_PASSWORD_FILE`. The Spring Boot app container uses a custom `entrypoint` that reads the secret file at startup and exports it as `SPRING_DATASOURCE_PASSWORD` before launching the JVM — since Spring Boot does not natively support `_FILE`-style environment variables.

### Step 1 — Create the secrets directory and file

```bash
mkdir -p secrets
echo "apppassword" > secrets/mysql_password.txt
```

### Step 2 — Confirm the secret file path matches the compose file

Open `docker-compose.secrets.yml` and verify:

```yaml
secrets:
  mysql_password:
    file: ./secrets/mysql_password.txt   # must match the file you just created
```

### Step 3 — Understand how the app reads the secret

Spring Boot does **not** natively support `_FILE`-style environment variables. The `docker-compose.secrets.yml` handles this with a custom `entrypoint` that reads the secret file and injects it as a real environment variable before the JVM starts:

```yaml
entrypoint:
  - sh
  - -c
  - |
    export SPRING_DATASOURCE_PASSWORD=$(cat /run/secrets/mysql_password)
    exec java $JAVA_OPTS -jar app.jar
```

Your `application.properties` only needs the standard datasource property — no special code or `SecretsUtil` class is required:

```properties
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

### Step 4 — Build and start all services

```bash
docker compose -f docker-compose.secrets.yml up --build -d
```

### Step 5 — Watch the startup logs

```bash
docker compose -f docker-compose.secrets.yml logs -f
```

### Step 6 — Verify the secret is mounted (optional debugging)

```bash
docker exec -it myapp_spring cat /run/secrets/mysql_password
```

---

## Verify the Application

Once any of the options above is running, the API is available at:

```
http://localhost:8080/sb-docker-stack/api/v1/status
```

### Quick health check

```bash
curl -s http://localhost:8080/sb-docker-stack/actuator/health | jq .
```

Expected response:

```json
{
  "status": "UP"
}
```

> Requires `spring-boot-starter-actuator` in your `pom.xml`.

### Sample CRUD requests

```bash
# Create
curl -X POST http://localhost:8080/sb-docker-stack/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"user_name": "John", "email": "john@test.com", "date_of_birth": "1970-01-01", "date_of_leaving": "2030-01-01", "postal_code": 75850}'

# Read all
curl http://localhost:8080/sb-docker-stack/api/v1/users

# Read by ID
curl http://localhost:8080/sb-docker-stack/api/v1/users/1

# Update
curl -X PUT http://localhost:8080/sb-docker-stack/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{"user_name": "John_updated_by_put", "email": "john@test.com", "date_of_birth": "1970-01-01", "date_of_leaving": "2030-01-01", "postal_code": 75850}'

# Update
curl -X PATCH http://localhost:8080/sb-docker-stack/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{"user_name": "John_updated_by_patch", "email": "john@test.com", "date_of_birth": "1970-01-01", "date_of_leaving": "2030-01-01", "postal_code": 75850}'

# Delete
curl -X DELETE http://localhost:8080/sb-docker-stack/api/v1/users/1
```

---

## Stopping and Cleaning Up

### Option 1 & 2 — Standalone containers

```bash
# Stop and remove containers
docker stop sb_docker_stack_app mysql_db
docker rm sb_docker_stack_app mysql_db

# Remove the custom network
docker network rm docker_stack_network

# Remove the volume (WARNING: deletes all database data)
docker volume rm mysql_data
```

### Option 3 — Docker Compose (.env)

```bash
# Stop and remove containers, networks
docker compose -f docker-compose.yml down

# Also remove volumes (WARNING: deletes all database data)
docker compose -f docker-compose.yml down -v
```

### Option 4 — Docker Compose (Secrets)

```bash
docker compose -f docker-compose.secrets.yml down

# Also remove volumes
docker compose -f docker-compose.secrets.yml down -v
```

### Remove built images (all options)

```bash
docker rmi sb_docker_stack_app:single sb_docker_stack_app:multi
```

---

## Deployment Options — Quick Reference

| Option | Command | Builds JAR? | Uses Secrets? | Best For |
|--------|---------|------------|--------------|----------|
| Single-stage | `docker build -f Dockerfile.single` | ❌ Pre-built | ❌ | Fast iteration, CI artifacts |
| Multi-stage | `docker build -f Dockerfile.multi` | ✅ In Docker | ❌ | Clean builds, no local JDK needed |
| Compose + .env | `docker compose -f docker-compose.yml up` | ✅ | ❌ | Local development |
| Compose + Secrets | `docker compose -f docker-compose.secrets.yml up` | ✅ | ✅ | Staging / production-like |