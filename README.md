# spring-boot-keycloak-sample4

[![Java CI with Maven](https://github.com/hendisantika/spring-boot-keycloak-sample4/actions/workflows/maven.yml/badge.svg)](https://github.com/hendisantika/spring-boot-keycloak-sample4/actions/workflows/maven.yml)
![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.x-6DB33F?logo=springboot)
![Keycloak](https://img.shields.io/badge/Keycloak-26-4D4D4D?logo=keycloak)

Sample **Spring Boot OAuth2 Resource Server** secured by **Keycloak**. JWT access tokens issued by Keycloak are
validated by Spring Security, and Keycloak **client roles** are mapped to Spring Security authorities for role-based
access control.

## Tech Stack

| Component          | Version                      |
|--------------------|------------------------------|
| Java               | 25 (Temurin)                 |
| Spring Boot        | 4.1.x                        |
| Spring Security    | OAuth2 Resource Server (JWT) |
| Keycloak           | 26.x                         |
| Lombok             | managed by Spring Boot       |
| Testing            | JUnit 5, Testcontainers (Keycloak), REST Assured |

## How It Works

1. A client obtains an access token from the Keycloak realm `PowerRanger` (client `green_ranger`).
2. The API validates the JWT signature against the realm JWK set (`issuer-uri`).
3. `JwtAuthConverter` reads `resource_access.<resource-id>.roles` from the token and maps every role to
   `ROLE_<role>` (e.g. `admin` → `ROLE_admin`). The principal name is taken from `preferred_username`
   (falls back to `sub`).
4. `SecurityConfig` applies role-based rules per endpoint. Sessions are **stateless**.

## Endpoints

| Method | Path                            | Access                     | Response                                    |
|--------|---------------------------------|----------------------------|---------------------------------------------|
| GET    | `/api/v1/demo/all`              | Public                     | `Hello All from Spring boot & Keycloak`     |
| GET    | `/api/v1/demo/user`             | role `user`                | `Hello from Spring boot & Keycloak`         |
| GET    | `/api/v1/demo/admin`            | role `admin`               | `Hello from Spring boot & Keycloak - ADMIN` |
| GET    | `/api/v1/demo/admin-and-user/**`| role `admin` or `user`     | –                                           |
| *      | anything else                   | Any authenticated user     | –                                           |

Without a token protected endpoints return `401 Unauthorized`; with a token that lacks the required role they
return `403 Forbidden`.

## Configuration

`src/main/resources/application.yml`

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:7080/realms/PowerRanger
          jwk-set-uri: ${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/certs
server:
  port: 8080

jwt:
  auth:
    converter:
      resource-id: green_ranger              # Keycloak client whose roles are mapped
      principle-attribute: preferred_username
```

## Getting Started

### Prerequisites

- JDK 25
- Docker (to run Keycloak locally and for the integration tests)

### 1. Run Keycloak

```shell
docker run --name keycloak -p 7080:8080 \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  -v "$(pwd)/src/test/resources/keycloak:/opt/keycloak/data/import" \
  quay.io/keycloak/keycloak:26.0 start-dev --import-realm
```

This imports the `PowerRanger` realm, including the `green_ranger` client with roles `admin` and `user` and two
demo users:

| Username     | Password   | Client role (`green_ranger`) |
|--------------|------------|------------------------------|
| `test-user`  | `password` | `user`                       |
| `test-admin` | `password` | `admin`                      |

> These credentials are for local development and tests only.

### 2. Run the application

```shell
./mvnw spring-boot:run
```

### 3. Call the API

```shell
# Public endpoint
curl http://localhost:8080/api/v1/demo/all

# Get a token for test-admin
TOKEN=$(curl -s -X POST http://localhost:7080/realms/PowerRanger/protocol/openid-connect/token \
  -d grant_type=password -d client_id=green_ranger \
  -d username=test-admin -d password=password | jq -r .access_token)

# Protected endpoint
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/demo/admin
```

## Testing

Integration tests start a real Keycloak in Docker using
[Testcontainers Keycloak](https://github.com/dasniko/testcontainers-keycloak), import the realm, obtain tokens for
`test-user` / `test-admin` and assert the `200` / `401` / `403` behaviour of each endpoint.

```shell
./mvnw verify
```

Tests are skipped automatically when Docker is not available.

## CI/CD & Dependency Management

- **GitHub Actions** (`.github/workflows/maven.yml`) builds and tests the project on JDK 25 for every push and pull
  request to `main`, uploads the Surefire reports, and submits the Maven dependency graph to GitHub on `main`.
- **Dependabot** (`.github/dependabot.yml`):
  - Maven updates run daily (05:00 Asia/Jakarta) and are **grouped** (Spring Boot, Testcontainers, REST Assured,
    Maven plugins, other minor/patch updates) to reduce PR noise.
  - GitHub Actions updates run weekly and are grouped into a single PR.
  - PRs are labeled and use conventional commit prefixes (`chore(deps)`, `ci(deps)`).

## Author

**Hendi Santika**

- Email: hendisantika@gmail.com
- Telegram: [@hendisantika34](https://t.me/hendisantika34)
- Link: [s.id/hendisantika](https://s.id/hendisantika)
