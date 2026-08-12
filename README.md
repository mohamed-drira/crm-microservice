# CRM Microservices — Spring Boot 3.2 / Spring Cloud 2023.0

Event-driven CRM built as microservices: **Keycloak (OIDC) for identity**, JWT
validation at the gateway via Keycloak's JWKS, Eureka service discovery, Spring
Cloud Config, Kafka for cross-service events, one MySQL DB per service.
**Java 17.**

## Status

| Service | State | Notes |
|---|---|---|
| keycloak | ✅ wired | Identity provider, realm `crm` imported on startup, port 8090 |
| discovery-server | ✅ built | Eureka registry (port 8761) |
| config-server | ✅ built | Serves `config-repo/` (native), port 8888 |
| api-gateway | ✅ built | Spring Cloud Gateway + OAuth2 resource server (JWKS validation), port 8080 |
| hr-service | ✅ built | Department, Employee, LeaveRequest + `employee-events` Kafka producer, port 8082 |
| payroll-service | ✅ built | Payroll, Fee, EmployeeSnapshot; consumes `employee-events`, publishes `payroll-events`, port 8083 |
| billing-service | ✅ built | Invoice; publishes `invoice-events`, port 8084 |
| crm-service | ✅ built | Contact, Interaction; consumes `employee-events` & `invoice-events`, port 8085 |

> The former in-house `auth-service` (custom JWT issuing) has been **replaced by
> Keycloak**. The gateway now validates RS256 tokens signed by Keycloak instead
> of HMAC tokens signed by a shared secret.

## Layout

```
crm-microservices/
├── pom.xml                  # aggregator: Spring Boot 3.2.5 + Spring Cloud 2023.0.1
├── docker-compose.yml       # full topology for built services + Keycloak
├── config-repo/             # Git-style config files served by config-server
├── keycloak/import/         # crm-realm.json — imported by Keycloak on first start
│
├── discovery-server/        # Eureka registry
├── config-server/           # Spring Cloud Config (native profile → config-repo/)
├── api-gateway/             # routes /api/hr, /api/payroll, /api/billing, /api/crm; validates Keycloak JWT
├── hr-service/              # Department, Employee, LeaveRequest; EmployeeEvent → Kafka
├── payroll-service/         # Payroll, Fee, EmployeeSnapshot; consumes employee-events, publishes payroll-events
├── billing-service/         # Invoice; publishes invoice-events
└── crm-service/             # Contact, Interaction; consumes employee-events & invoice-events
```

## How to run

### Build & test

```
mvn package          # builds all modules + runs unit tests (8 tests)
```

### Start the stack (Docker)

```
mvn package
docker compose up -d --build
```

This starts: zookeeper, kafka, 5 MySQL instances, Eureka, config-server (with
`./config-repo` mounted), Keycloak (imports `keycloak/import/crm-realm.json`),
the gateway, hr-service, payroll-service, billing-service, and crm-service.

Keycloak first start imports realm **crm** with:

- client `crm-frontend` (public, direct access grants enabled — usable from curl)
- users: `admin` / `admin123` (roles `ROLE_USER`, `ROLE_ADMIN`), `johndoe` / `password123` (role `ROLE_USER`)
- Admin console: http://localhost:8090 (login `admin` / `admin`)

### Smoke test

```
# 1. Get a Keycloak access token (Direct Access Grant)
TOKEN=$(curl -s -X POST http://localhost:8090/realms/crm/protocol/openid-connect/token \
  -d "grant_type=password" \
  -d "client_id=crm-frontend" \
  -d "username=admin" \
  -d "password=admin123" | jq -r .access_token)

# 2. Create a department (protected — token required)
curl -X POST localhost:8080/api/hr/departments -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d "{\"name\":\"Engineering\",\"description\":\"Build team\"}"

# 3. Create an employee (publishes EmployeeCreated to Kafka)
curl -X POST localhost:8080/api/hr/employees -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane@example.com\",\"position\":\"Engineer\",\"salary\":75000,\"departmentId\":1}"

# 4. Without a token you get 401
curl -i localhost:8080/api/hr/employees | head -1   # HTTP/1.1 401 Unauthorized

# Swagger UI: http://localhost:8082/swagger-ui.html · Eureka dashboard: http://localhost:8761
```

## Security model

- **Keycloak** (realm `crm`) is the identity provider and issues RS256 JWTs.
- **api-gateway** is a Spring Security **OAuth2 resource server**: it validates
  the token signature and expiry against Keycloak's JWKS endpoint
  (`/realms/crm/protocol/openid-connect/certs`, configured via
  `spring.security.oauth2.resourceserver.jwt.jwk-set-uri` in `config-repo/api-gateway.yml`).
  Every request must carry `Authorization: Bearer <token>`.
- After validation the gateway forwards identity as `X-User-Id` (subject),
  `X-Username` (`preferred_username`) and `X-User-Roles` (realm roles) headers.
- Business services trust those forwarded headers (`TrustedHeadersFilter`) — no
  cross-service DB joins, no synchronous calls, no shared secrets.

## Cross-service events (Kafka)

`hr-service` publishes `EmployeeEvent` (`EMPLOYEE_CREATED` / `EMPLOYEE_UPDATED`)
to topic `employee-events`. `payroll-service` and `crm-service` consume it to
keep local read-only copies of employees.

`billing-service` publishes `InvoiceEvent` (`INVOICE_CREATED` / `INVOICE_STATUS_UPDATED`)
to topic `invoice-events`. `crm-service` consumes it to build interaction timelines.

`payroll-service` publishes `PayrollEvent` (`PAYROLL_CREATED` / `PAYROLL_UPDATED`)
to topic `payroll-events` for downstream consumers.

## What's next

All core services are implemented and wired. Future enhancements:

1. Add integration tests with Testcontainers
2. Implement API gateway rate limiting and circuit breakers
3. Add distributed tracing (Micrometer + Zipkin/Jaeger)
4. Enhance security with fine-grained RBAC per service
5. Add metrics and alerting (Prometheus + Grafana)
