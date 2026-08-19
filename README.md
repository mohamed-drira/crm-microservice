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
| api-gateway | ✅ built | Spring Cloud Gateway + OAuth2 resource server (JWKS validation) + rate limiting + audit logging, port 8080 |
| hr-service | ✅ built | Department, Employee, LeaveRequest + `employee-events` Kafka producer, port 8082 |
| payroll-service | ✅ built | Payroll, Fee, EmployeeSnapshot; consumes `employee-events`, publishes `payroll-events`, port 8083 |
| billing-service | ✅ built | Invoice; publishes `invoice-events`, port 8084 |
| crm-service | ✅ built | Contact, Interaction; consumes `employee-events` & `invoice-events`, port 8085 |
| notification-service | ✅ built | Consumes all Kafka events, stores & serves notifications, port 8086 |

## Layout

```
crm-microservices/
├── pom.xml                                 # aggregator: Spring Boot 3.2.5 + Spring Cloud 2023.0.1
├── docker-compose.yml                      # full topology for all services + Keycloak + Kafka + MySQL
├── README.md
│
├── .github/
│   └── workflows/
│       └── sonarqube.yml                   # GitHub Actions – SonarCloud CI
│
├── config-repo/                            # native-profile config files served by config-server
│   ├── application.yml                     # shared: Eureka, JPA, Jackson
│   ├── api-gateway.yml                     # routes, JWKS URI, Kafka producer, rate limits
│   ├── hr-service.yml                      # Kafka producer config
│   ├── payroll-service.yml                 # Kafka producer + consumer config
│   ├── billing-service.yml                 # Kafka producer config
│   ├── crm-service.yml                     # Kafka consumer config with type mapping
│   └── notification-service.yml            # Kafka consumer config for 3 topics
│
├── keycloak/
│   └── import/
│       └── crm-realm.json                  # Keycloak realm — imported on first start
│
├── discovery-server/                       # ── Eureka registry ─────────────────────────
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/com/crm/discovery/
│       └── DiscoveryServerApplication.java
│
├── config-server/                          # ── Spring Cloud Config (native → config-repo/) ──
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/crm/config/
│       └── ConfigServerApplication.java
│
├── api-gateway/                            # ── Gateway (WebFlux) ───────────────────────
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/crm/gateway/
│       ├── GatewayApplication.java
│       ├── config/
│       │   └── SecurityConfig.java          # OAuth2 resource server (Keycloak JWT)
│       ├── security/
│       │   └── JwtHeaderForwardFilter.java  # JWT → X-User-* headers
│       ├── filter/
│       │   ├── RateLimitFilter.java         # sliding-window rate limiter (per-user)
│       │   └── AuditLogFilter.java          # audit logging → Kafka
│       └── messaging/
│           ├── AuditEvent.java              # audit event record
│           └── AuditEventProducer.java      # Kafka audit producer
│
├── hr-service/                             # ── HR service (servlet) ────────────────────
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/crm/hr/
│       ├── HrServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java          # role-based path rules
│       │   ├── KafkaTopicConfig.java        # topics: employee-events, employee-commands
│       │   └── OpenApiConfig.java
│       ├── security/
│       │   ├── CurrentUser.java
│       │   └── TrustedHeadersFilter.java
│       ├── entity/
│       │   ├── Department.java
│       │   ├── Employee.java
│       │   ├── LeaveRequest.java
│       │   └── enums/
│       │       ├── EmployeeStatus.java
│       │       ├── LeaveRequestStatus.java
│       │       └── LeaveType.java
│       ├── dto/
│       │   ├── request/
│       │   │   ├── DepartmentRequest.java
│       │   │   ├── EmployeeRequest.java
│       │   │   ├── LeaveRequestRequest.java
│       │   │   └── LeaveRequestStatusRequest.java
│       │   └── response/
│       │       ├── DepartmentResponse.java
│       │       ├── EmployeeResponse.java
│       │       └── LeaveRequestResponse.java
│       ├── repository/
│       │   ├── DepartmentRepository.java
│       │   ├── EmployeeRepository.java
│       │   └── LeaveRequestRepository.java
│       ├── service/
│       │   ├── DepartmentService.java
│       │   ├── EmployeeService.java
│       │   ├── LeaveRequestService.java
│       │   └── impl/
│       │       ├── DepartmentServiceImpl.java
│       │       ├── EmployeeServiceImpl.java
│       │       └── LeaveRequestServiceImpl.java
│       ├── controller/
│       │   ├── DepartmentController.java
│       │   ├── EmployeeController.java
│       │   └── LeaveRequestController.java
│       ├── messaging/
│       │   ├── EmployeeEvent.java
│       │   └── EmployeeEventProducer.java
│       └── exception/
│           ├── ApiException.java
│           └── GlobalExceptionHandler.java
│
├── payroll-service/                        # ── Payroll service (servlet) ───────────────
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/crm/payroll/
│       ├── PayrollServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── KafkaTopicConfig.java        # topics: payroll-events, employee-commands
│       │   └── OpenApiConfig.java
│       ├── security/
│       │   ├── CurrentUser.java
│       │   └── TrustedHeadersFilter.java
│       ├── entity/
│       │   ├── EmployeeSnapshot.java
│       │   ├── Fee.java
│       │   ├── Payroll.java
│       │   └── enums/
│       │       ├── EmployeeStatus.java
│       │       ├── FeeType.java
│       │       └── PayrollStatus.java
│       ├── dto/
│       │   ├── request/
│       │   │   ├── FeeRequest.java
│       │   │   ├── PayrollRequest.java
│       │   │   └── PayrollStatusRequest.java
│       │   └── response/
│       │       ├── EmployeeSnapshotResponse.java
│       │       ├── FeeResponse.java
│       │       └── PayrollResponse.java
│       ├── repository/
│       │   ├── EmployeeSnapshotRepository.java
│       │   ├── FeeRepository.java
│       │   └── PayrollRepository.java
│       ├── service/
│       │   ├── EmployeeSnapshotService.java
│       │   ├── PayrollService.java
│       │   └── impl/
│       │       ├── EmployeeSnapshotServiceImpl.java
│       │       └── PayrollServiceImpl.java
│       ├── controller/
│       │   ├── EmployeeSnapshotController.java
│       │   └── PayrollController.java
│       ├── messaging/
│       │   ├── EmployeeEvent.java
│       │   ├── EmployeeEventConsumer.java
│       │   ├── PayrollEvent.java
│       │   └── PayrollEventProducer.java
│       └── exception/
│           ├── ApiException.java
│           └── GlobalExceptionHandler.java
│
├── billing-service/                        # ── Billing service (servlet) ───────────────
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/crm/billing/
│       ├── BillingServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── KafkaTopicConfig.java        # topics: invoice-events, invoice-commands
│       │   └── OpenApiConfig.java
│       ├── security/
│       │   ├── CurrentUser.java
│       │   └── TrustedHeadersFilter.java
│       ├── entity/
│       │   ├── Invoice.java
│       │   └── enums/
│       │       └── InvoiceStatus.java
│       ├── dto/
│       │   ├── request/
│       │   │   ├── InvoiceRequest.java
│       │   │   └── InvoiceStatusRequest.java
│       │   └── response/
│       │       └── InvoiceResponse.java
│       ├── repository/
│       │   └── InvoiceRepository.java
│       ├── service/
│       │   ├── InvoiceService.java
│       │   └── impl/
│       │       └── InvoiceServiceImpl.java
│       ├── controller/
│       │   └── InvoiceController.java
│       ├── messaging/
│       │   ├── InvoiceEvent.java
│       │   └── InvoiceEventProducer.java
│       └── exception/
│           ├── ApiException.java
│           └── GlobalExceptionHandler.java
│
├── crm-service/                            # ── CRM service (servlet) ──────────────────
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/resources/
│   │   └── application.yml
│   └── src/main/java/com/crm/crmservice/
│       ├── CrmServiceApplication.java
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── KafkaTopicConfig.java        # topics: employee-events, invoice-events
│       │   └── OpenApiConfig.java
│       ├── security/
│       │   ├── CurrentUser.java
│       │   └── TrustedHeadersFilter.java
│       ├── entity/
│       │   ├── Contact.java
│       │   ├── Interaction.java
│       │   └── enums/
│       │       ├── ContactStatus.java
│       │       └── InteractionType.java
│       ├── dto/
│       │   ├── request/
│       │   │   ├── ContactRequest.java
│       │   │   ├── ContactStatusRequest.java
│       │   │   └── InteractionRequest.java
│       │   └── response/
│       │       ├── ContactResponse.java
│       │       └── InteractionResponse.java
│       ├── repository/
│       │   ├── ContactRepository.java
│       │   └── InteractionRepository.java
│       ├── service/
│       │   ├── ContactService.java
│       │   ├── InteractionService.java
│       │   └── impl/
│       │       ├── ContactServiceImpl.java
│       │       └── InteractionServiceImpl.java
│       ├── controller/
│       │   ├── ContactController.java
│       │   └── InteractionController.java
│       ├── messaging/
│       │   ├── EmployeeEvent.java
│       │   ├── EmployeeEventConsumer.java
│       │   ├── InvoiceEvent.java
│       │   └── InvoiceEventConsumer.java
│       └── exception/
│           ├── ApiException.java
│           └── GlobalExceptionHandler.java
│
└── notification-service/                   # ── Notification service (servlet) ──────────
    ├── Dockerfile
    ├── pom.xml
    ├── src/main/resources/
    │   └── application.yml
    └── src/main/java/com/crm/notification/
        ├── NotificationServiceApplication.java
        ├── config/
        │   ├── SecurityConfig.java
        │   ├── KafkaTopicConfig.java        # topics: employee-events, payroll-events, invoice-events
        │   └── OpenApiConfig.java
        ├── security/
        │   ├── CurrentUser.java
        │   └── TrustedHeadersFilter.java
        ├── entity/
        │   ├── Notification.java
        │   └── enums/
        │       └── NotificationType.java
        ├── dto/
        │   └── response/
        │       ├── NotificationResponse.java
        │       └── UnreadCountResponse.java
        ├── repository/
        │   └── NotificationRepository.java
        ├── service/
        │   ├── NotificationService.java
        │   └── impl/
        │       └── NotificationServiceImpl.java
        ├── controller/
        │   └── NotificationController.java
        ├── messaging/
        │   ├── EmployeeEvent.java
        │   ├── InvoiceEvent.java
        │   ├── PayrollEvent.java
        │   └── NotificationEventConsumer.java
        └── exception/
            ├── ApiException.java
            └── GlobalExceptionHandler.java
```

## How to run

### Build & test

```
mvn package          # builds all modules + runs unit tests
```

### Start the stack (Docker)

```
mvn package
docker compose up -d --build
```

This starts: zookeeper, kafka, 5 MySQL instances, Eureka, config-server (with
`./config-repo` mounted), Keycloak (imports `keycloak/import/crm-realm.json`),
the gateway, hr-service, payroll-service, billing-service, crm-service, and
notification-service.

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

# 2. Create a department (requires ROLE_ADMIN)
curl -X POST localhost:8080/api/hr/departments -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d "{\"name\":\"Engineering\",\"description\":\"Build team\"}"

# 3. Create an employee (requires ROLE_ADMIN, publishes EmployeeCreated to Kafka)
curl -X POST localhost:8080/api/hr/employees -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"email\":\"jane@example.com\",\"position\":\"Engineer\",\"salary\":75000,\"departmentId\":1}"

# 4. List employees (requires ROLE_USER or ROLE_ADMIN)
curl localhost:8080/api/hr/employees -H "Authorization: Bearer $TOKEN"

# 5. Check notifications
curl localhost:8080/api/notifications -H "Authorization: Bearer $TOKEN"

# 6. Without a token you get 401
curl -i localhost:8080/api/hr/employees | head -1   # HTTP/1.1 401 Unauthorized

# 7. With ROLE_USER trying admin-only endpoint you get 403
USER_TOKEN=$(curl -s -X POST http://localhost:8090/realms/crm/protocol/openid-connect/token \
  -d "grant_type=password" -d "client_id=crm-frontend" \
  -d "username=johndoe" -d "password=password123" | jq -r .access_token)
curl -i -X POST localhost:8080/api/hr/departments -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" -d "{\"name\":\"HR\"}" | head -1   # HTTP/1.1 403 Forbidden

# Swagger UI: http://localhost:8082/swagger-ui.html · Eureka dashboard: http://localhost:8761
```

## Keycloak Integration

### Overview

Keycloak serves as the central **OpenID Connect (OIDC) identity provider** for
the entire microservices architecture. It manages authentication, token issuance,
role assignments, and user federation. The API gateway is the single point of
token validation — downstream services never contact Keycloak directly.

### Architecture Flow

```
┌────────────┐         ┌─────────────┐         ┌──────────────┐
│  Frontend  │──OIDC──▶│  Keycloak   │◀─JWKS──│  api-gateway │
│  (SPA/curl)│         │  (port 8090)│         │  (port 8080) │
└─────┬──────┘         └─────────────┘         └──────┬───────┘
      │                                                │
      │  1. User authenticates                         │
      │     → Keycloak issues JWT                      │
      │                                                │
      │  2. Client sends request                       │
      │     with Authorization: Bearer <token>         │
      │                                                │
      │                                                │  3. Gateway validates
      │                                                │     token signature via
      │                                                │     JWKS endpoint
      │                                                │
      │                                                │  4. Gateway extracts
      │                                                │     identity from JWT
      │                                                │
      │                                                │  5. Forwards headers:
      │                                                │     X-User-Id, X-Username,
      │                                                │     X-User-Roles
      │                                                │
      │                                                ▼
      │                                        ┌──────────────┐
      │                                        │  downstream  │
      │                                        │  service     │
      │                                        │  (trusts     │
      │                                        │   headers)   │
      │                                        └──────────────┘
```

### Realm Configuration

The realm `crm` is defined in `keycloak/import/crm-realm.json` and imported
automatically on first startup via `--import-realm` flag in docker-compose.

**Realm:** `crm`
**SSL:** none (development only — enable `REQUIRED` for production)

### Client: `crm-frontend`

| Property | Value |
|----------|-------|
| Client ID | `crm-frontend` |
| Protocol | OpenID Connect |
| Type | Public (no client secret) |
| Direct Access Grants | Enabled (password grant for curl/CLI) |
| Standard Flow | Enabled (authorization code for SPA) |
| Redirect URIs | `http://localhost:8080/*` |

### Roles

| Role | Description | Assigned To |
|------|-------------|-------------|
| `ROLE_USER` | Standard application user — read access | `admin`, `johndoe` |
| `ROLE_ADMIN` | Application administrator — full CRUD access | `admin` |

### Users

| Username | Password | Email | Roles |
|----------|----------|-------|-------|
| `admin` | `admin123` | `admin@example.com` | `ROLE_USER`, `ROLE_ADMIN` |
| `johndoe` | `password123` | `john@example.com` | `ROLE_USER` |

### JWT Token Structure

When a user authenticates, Keycloak issues a signed JWT containing:

```json
{
  "sub": "UUID-string",
  "preferred_username": "admin",
  "realm_access": {
    "roles": ["ROLE_USER", "ROLE_ADMIN"]
  },
  "email": "admin@example.com",
  "iss": "http://localhost:8090/realms/crm",
  "exp": 1234567890,
  "iat": 1234567800
}
```

### Gateway Token Validation (JWKS)

The api-gateway is configured as a Spring Security **OAuth2 Resource Server**:

```yaml
# config-repo/api-gateway.yml
spring.security.oauth2.resourceserver.jwt.jwk-set-uri:
  http://keycloak:8080/realms/crm/protocol/openid-connect/certs
```

The gateway's `SecurityConfig` enables JWT validation:

```java
// api-gateway/.../SecurityConfig.java
.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
```

This configures Spring Security to:
1. Fetch Keycloak's public RSA keys from the JWKS endpoint
2. Validate the token signature against those keys
3. Check token expiration (`exp` claim)
4. Verify the issuer (`iss` claim matches the realm URL)
5. Extract the `JwtAuthenticationToken` into the reactive SecurityContext

### Identity Forwarding (JwtHeaderForwardFilter)

After JWT validation, `JwtHeaderForwardFilter` (a reactive `WebFilter`) extracts
identity claims and forwards them as HTTP headers to downstream services:

| JWT Claim | Forwarded Header | Example Value |
|-----------|-----------------|---------------|
| `sub` | `X-User-Id` | `a1b2c3d4-...` |
| `preferred_username` | `X-Username` | `admin` |
| `realm_access.roles` | `X-User-Roles` | `ROLE_USER,ROLE_ADMIN` |

```java
// api-gateway/.../JwtHeaderForwardFilter.java
headers.set(HEADER_USER_ID, jwt.getSubject());
headers.set(HEADER_USERNAME, jwt.getClaimAsString("preferred_username"));
headers.set(HEADER_ROLES, realmRoles(jwt));  // comma-joined
```

### Downstream Trust Model (TrustedHeadersFilter)

Each downstream service (hr, payroll, billing, crm, notification) has a
`TrustedHeadersFilter` that:

1. Reads `X-User-Id`, `X-Username`, `X-User-Roles` from the request headers
2. Creates a `CurrentUser` principal (implements `UserDetails`)
3. Wraps it in a `UsernamePasswordAuthenticationToken`
4. Sets it in the `SecurityContextHolder`

This allows controllers to use `@AuthenticationPrincipal CurrentUser user`
to access the authenticated user's identity and roles.

**Security note:** Downstream services blindly trust these headers. If a request
bypasses the gateway (direct network access), headers can be spoofed. This is
by design — the gateway is the single entry point. In production, ensure
downstream services are not directly accessible from the internet.

### Adding New Users / Roles

1. Edit `keycloak/import/crm-realm.json`
2. Add users under the `"users"` array with credentials and role assignments
3. Restart Keycloak (realm reimports on startup)

Or use the Keycloak Admin Console at http://localhost:8090 (login `admin`/`admin`).

### Production Considerations

- Set `sslRequired: "all"` in the realm JSON
- Use confidential clients (with client secrets) instead of public clients
- Enable PKCE for the authorization code flow
- Configure token expiration and refresh token rotation
- Use HTTPS for all endpoints
- Store Keycloak credentials in a secrets manager, not in plaintext JSON

## Kafka Integration

### Overview

Apache Kafka provides **asynchronous, event-driven communication** between
microservices. Services produce events when domain changes occur, and other
services consume those events to update their local state. This decouples
services — producers don't know or care who consumes their events.

### Architecture

```
┌──────────────┐    ┌─────────┐    ┌──────────────────────────────┐
│  hr-service  │───▶│  Kafka  │◀───│  payroll-service (consumer)  │
│  (producer)  │    │  broker │    │  crm-service (consumer)      │
└──────────────┘    │         │    │  notification-service (cons.) │
                    │         │    └──────────────────────────────┘
┌──────────────┐    │         │    ┌──────────────────────────────┐
│payroll-svc   │───▶│         │◀───│  notification-service (cons.) │
│  (producer)  │    │         │    └──────────────────────────────┘
└──────────────┘    │         │
                    │         │    ┌──────────────────────────────┐
┌──────────────┐    │         │◀───│  crm-service (consumer)      │
│ billing-svc  │───▶│         │    │  notification-service (cons.) │
│  (producer)  │    └─────────┘    └──────────────────────────────┘
└──────────────┘
┌──────────────┐    ┌─────────┐
│ api-gateway  │───▶│  Kafka  │   (audit-events topic)
│  (producer)  │    └─────────┘
└──────────────┘
```

### Infrastructure

| Component | Image | Port | Purpose |
|-----------|-------|------|---------|
| Zookeeper | `confluentinc/cp-zookeeper:7.6.0` | 2181 | Kafka cluster coordination |
| Kafka Broker | `confluentinc/cp-kafka:7.6.0` | 9092 | Message broker |

Auto-create topics is enabled (`KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"`).
Topics are also created programmatically via `KafkaTopicConfig` beans with
1 partition and 1 replica each.

### Topic Registry

| Topic | Producer | Consumer(s) | Key | Partitions |
|-------|----------|-------------|-----|------------|
| `employee-events` | hr-service | payroll-service, crm-service, notification-service | `employeeId` | 1 |
| `payroll-events` | payroll-service | notification-service | `employeeId` | 1 |
| `invoice-events` | billing-service | crm-service, notification-service | `invoiceId` | 1 |
| `audit-events` | api-gateway | (external consumers) | `eventId` | 1 |

### Event Classes

Each event is a plain Java class (or record) serialized to JSON via Spring Kafka's
`JsonSerializer`. Services that consume events keep **local copies** of the event
class for loose coupling — no shared library dependency.

#### EmployeeEvent (produced by hr-service)

```java
public record EmployeeEvent(
    UUID eventId,           // unique event ID
    Type type,              // EMPLOYEE_CREATED or EMPLOYEE_UPDATED
    Long employeeId,        // business key
    String firstName,
    String lastName,
    String email,
    String department,
    BigDecimal salary,
    String status,
    LocalDateTime timestamp
) {}
```

#### PayrollEvent (produced by payroll-service)

```java
public record PayrollEvent(
    UUID eventId,
    Type type,              // PAYROLL_CREATED or PAYROLL_UPDATED
    Long payrollId,
    Long employeeId,
    String employeeName,
    LocalDate periodStart,
    LocalDate periodEnd,
    BigDecimal baseSalary,
    BigDecimal netAmount,
    Status status,          // DRAFT, PAID, CANCELLED
    LocalDateTime timestamp
) {}
```

#### InvoiceEvent (produced by billing-service)

```java
public record InvoiceEvent(
    UUID eventId,
    Type type,              // INVOICE_CREATED or INVOICE_STATUS_UPDATED
    Long invoiceId,
    String invoiceNumber,
    String customerName,
    String customerEmail,
    BigDecimal amount,
    LocalDate issueDate,
    LocalDate dueDate,
    String status,
    LocalDateTime timestamp
) {}
```

### Serialization Configuration

All services use Spring Kafka's `JsonSerializer`/`JsonDeserializer` with
`spring.json.type.mapping` to resolve logical type names to Java classes.

**Producer config** (example from `config-repo/hr-service.yml`):

```yaml
spring.kafka:
  bootstrap-servers: kafka:9092
  producer:
    key-serializer: org.apache.kafka.common.serialization.StringSerializer
    value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    properties:
      spring.json.type.mapping: "employee-event:com.crm.hr.messaging.EmployeeEvent"
```

**Consumer config** (example from `config-repo/crm-service.yml`):

```yaml
spring.kafka:
  bootstrap-servers: kafka:9092
  consumer:
    group-id: crm-service
    auto-offset-reset: earliest
    key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
    properties:
      spring.json.trusted.packages: "com.crm.hr.messaging,com.crm.billing.messaging,com.crm.crmservice.messaging"
      spring.json.type.mapping: "employee-event:com.crm.crmservice.messaging.EmployeeEvent,invoice-event:com.crm.crmservice.messaging.InvoiceEvent"
```

Key points:
- `spring.json.trusted.packages` restricts deserialization to known packages
  (prevents deserialization of arbitrary classes)
- `spring.json.type.mapping` maps the `__TypeId__` header value to the correct
  Java class for polymorphic deserialization
- Each consuming service has its own local copy of the event class

### Producing Events

Producers use `KafkaTemplate<String, Event>` with async send and callback logging:

```java
// hr-service/.../EmployeeEventProducer.java
@Component
public class EmployeeEventProducer {
    private final KafkaTemplate<String, EmployeeEvent> kafkaTemplate;
    private final String topic;

    public void publish(EmployeeEvent event) {
        kafkaTemplate.send(topic, String.valueOf(event.getEmployeeId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) log.warn("Failed to publish: {}", ex.getMessage());
                else log.debug("Published to offset {}", result.getRecordMetadata().offset());
            });
    }
}
```

The message key is the business identifier (e.g., `employeeId`), which ensures
all events for the same entity go to the same partition (ordering guarantee).

### Consuming Events

Consumers use `@KafkaListener` with topic and group ID:

```java
// crm-service/.../EmployeeEventConsumer.java
@Component
public class EmployeeEventConsumer {

    @KafkaListener(
        topics = "${app.kafka.topic.employee-events:employee-events}",
        groupId = "crm-service"
    )
    public void onEmployeeEvent(EmployeeEvent event) {
        if (event == null || event.getEmployeeId() == null) return;
        switch (event.getType()) {
            case EMPLOYEE_CREATED -> contactService.createFromEmployeeEvent(event);
            case EMPLOYEE_UPDATED -> contactService.updateFromEmployeeEvent(event);
        }
    }
}
```

### Consumer Group Strategy

| Service | Consumer Group ID | Topics Consumed | Purpose |
|---------|------------------|-----------------|---------|
| payroll-service | `payroll-service` | `employee-events` | Maintain local employee snapshots |
| crm-service | `crm-service` | `employee-events`, `invoice-events` | Sync contacts, build interaction timelines |
| notification-service | `notification-service` | `employee-events`, `payroll-events`, `invoice-events` | Create notifications for admins |

Each service uses a unique group ID so every consumer instance receives all
messages (no competing consumers within the same service).

### Topic Creation

Topics are created declaratively via `KafkaTopicConfig` beans:

```java
// hr-service/.../KafkaTopicConfig.java
@Configuration
public class KafkaTopicConfig {
    @Value("${app.kafka.topic.employee-events:employee-events}")
    private String employeeEventsTopic;

    @Bean
    public NewTopic employeeEventsTopic() {
        return TopicBuilder.name(employeeEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
```

Topic names are externalized via `app.kafka.topic.*` properties in each
service's config-repo YAML, making them overridable without code changes.

### Error Handling

- **Producer side:** Async callback logs failures but doesn't throw — the
  producing operation (e.g., creating an employee) still succeeds even if
  the event publish fails.
- **Consumer side:** If deserialization fails or an exception is thrown, the
  message is retried based on Kafka's consumer error handling (default:
 无限 retry with backoff). The `@KafkaListener` methods include null guards
  to skip malformed events.

### Adding a New Event

1. Define the event class in the producer service (e.g., `messaging/OrderEvent.java`)
2. Add a producer component (e.g., `OrderEventProducer.java`)
3. Add topic config in `KafkaTopicConfig.java`
4. Add `spring.json.type.mapping` to the producer's config-repo YAML
5. In the consumer service, create a local copy of the event class
6. Add a `@KafkaListener` consumer component
7. Add `spring.json.trusted.packages` and `spring.json.type.mapping` to the consumer's config-repo YAML

### Production Considerations

- Increase partitions (currently 1) for higher throughput
- Set replication factor > 1 for fault tolerance (requires multiple brokers)
- Disable `KAFKA_AUTO_CREATE_TOPICS_ENABLE` and manage topics explicitly
- Configure dead-letter queues for failed messages
- Enable SASL/SSL for broker authentication and encryption
- Monitor consumer lag via Kafka metrics + Prometheus/Grafana

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

### Role-Based Authorization

All downstream services enforce role-based access control via Spring Security:

| Operation | Required Role | Applies To |
|-----------|--------------|------------|
| **GET** (read) | `ROLE_USER` or `ROLE_ADMIN` | All endpoints |
| **POST** (create) | `ROLE_ADMIN` | hr-service, payroll-service, billing-service |
| **POST** (create) | `ROLE_USER` or `ROLE_ADMIN` | crm-service (contacts, interactions) |
| **PUT / PATCH** (update) | `ROLE_ADMIN` | hr-service, payroll-service, billing-service, crm-service |
| **PATCH** (mark read) | `ROLE_USER` or `ROLE_ADMIN` | notification-service (own notifications) |
| **DELETE** | `ROLE_ADMIN` | All endpoints |

Access denied returns a JSON `403 Forbidden` response:
```json
{"status":403,"error":"Forbidden","message":"Insufficient permissions"}
```

### Rate Limiting

The API gateway enforces per-user sliding-window rate limits:

| Operation Type | Limit | Window |
|----------------|-------|--------|
| **Read** (GET) | 100 requests | 1 minute |
| **Write** (POST/PUT/PATCH/DELETE) | 20 requests | 1 minute |

Exceeding the limit returns `429 Too Many Requests` with a `Retry-After` header.
Rate limits are configurable via `app.ratelimit.*` properties in `config-repo/api-gateway.yml`.

### Audit Logging

Every request through the gateway is logged and published to the `audit-events`
Kafka topic. Each audit event captures:

| Field | Description |
|-------|-------------|
| `eventId` | Unique event identifier (UUID) |
| `timestamp` | Event creation timestamp |
| `method` | HTTP method (GET, POST, etc.) |
| `path` | Request path |
| `userId` | Keycloak subject ID |
| `username` | Keycloak preferred_username |
| `roles` | Comma-separated realm roles |
| `statusCode` | HTTP response status code |
| `durationMs` | Request processing time in milliseconds |
| `clientIp` | Client IP (X-Forwarded-For or remote address) |

## Cross-service events (Kafka)

| Topic | Producer | Consumer(s) | Event Types |
|-------|----------|-------------|-------------|
| `employee-events` | hr-service | payroll-service, crm-service, notification-service | `EMPLOYEE_CREATED`, `EMPLOYEE_UPDATED` |
| `payroll-events` | payroll-service | notification-service | `PAYROLL_CREATED`, `PAYROLL_UPDATED` |
| `invoice-events` | billing-service | crm-service, notification-service | `INVOICE_CREATED`, `INVOICE_STATUS_UPDATED` |
| `audit-events` | api-gateway | (external consumers) | Audit log entries |

`hr-service` publishes `EmployeeEvent` to `employee-events`. `payroll-service`
consumes it to maintain local employee snapshots. `crm-service` consumes it to
sync employee data into contacts. `notification-service` consumes it to create
notifications for admins.

`billing-service` publishes `InvoiceEvent` to `invoice-events`. `crm-service`
consumes it to build interaction timelines. `notification-service` consumes it
to notify admins of invoice activity.

`payroll-service` publishes `PayrollEvent` to `payroll-events`.
`notification-service` consumes it to notify admins and employees of payroll
activity.

`api-gateway` publishes `AuditEvent` to `audit-events` for every API request.

## Notification Service

The notification-service consumes all domain events from Kafka and stores
notifications in a dedicated MySQL database (`notification_db`). It exposes a
REST API for querying and managing notifications.

### API Endpoints

| Method | Path | Description | Access |
|--------|------|-------------|--------|
| `GET` | `/api/notifications` | List current user's notifications | `ROLE_USER`, `ROLE_ADMIN` |
| `GET` | `/api/notifications/unread-count` | Count of unread notifications | `ROLE_USER`, `ROLE_ADMIN` |
| `PATCH` | `/api/notifications/{id}/read` | Mark a notification as read | `ROLE_USER`, `ROLE_ADMIN` |
| `PATCH` | `/api/notifications/read-all` | Mark all as read | `ROLE_USER`, `ROLE_ADMIN` |

### Event → Notification Mapping

| Event | Notification Title | Recipients |
|-------|--------------------|------------|
| `EMPLOYEE_CREATED` | "New Employee Added" | Admin users |
| `EMPLOYEE_UPDATED` | "Employee Updated" | Admin users |
| `PAYROLL_CREATED` | "Payroll Generated" | Admin users |
| `PAYROLL_UPDATED` | "Payroll Updated" | Admin users |
| `INVOICE_CREATED` | "Invoice Created" | Admin users |
| `INVOICE_STATUS_UPDATED` | "Invoice Status Updated" | Admin users |

Recipients are configurable via `app.notification.admin-usernames` in
`config-repo/notification-service.yml`.

## Port Map

| Service | Port | Host-accessible? |
|---------|------|-----------------|
| Keycloak | 8090 | Yes |
| Eureka | 8761 | Yes |
| Config Server | 8888 | Yes |
| API Gateway | 8080 | Yes |
| HR Service | 8082 | **No** — internal only |
| Payroll Service | 8083 | **No** — internal only |
| Billing Service | 8084 | **No** — internal only |
| CRM Service | 8085 | **No** — internal only |
| Notification Service | 8086 | **No** — internal only |
| HR MySQL | 3312 | Yes |
| Payroll MySQL | 3313 | Yes |
| Billing MySQL | 3314 | Yes |
| CRM MySQL | 3315 | Yes |
| Notification MySQL | 3316 | Yes |
| Kafka | 9092 | Yes |
| Zookeeper | 2181 | Yes |

Business services (HR, Payroll, Billing, CRM, Notification) are only reachable
through the API Gateway. To debug a single service locally, temporarily add a
`ports:` mapping back to its docker-compose entry, or use
`docker exec -it crm-hr sh` to shell into the running container.
