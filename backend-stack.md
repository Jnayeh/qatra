# Qatra — Backend Stack

## Overview

Qatra is a **distributed backend system** for blood donation management, built as two independent Spring Boot microservices communicating asynchronously via Apache Kafka. The infrastructure includes an API gateway, a full observability stack (metrics, logs, traces), and a real-time push layer via WebSocket.

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Nginx (:80)                                │
│                     API Gateway / Reverse Proxy                     │
└──────────┬──────────────────────────────┬───────────────────────────┘
           │                              │
           ▼                              ▼
┌─────────────────────┐     ┌──────────────────────────┐
│  donation-service   │     │   notification-service   │
│      (:8080)        │     │         (:8082)          │
│  Modular Monolith   │     │  Hexagonal Architecture  │
│  (Spring Modulith)  │     │                          │
└──┬──────┬───────┬───┘     └──┬───────────────────┬───┘
   │      │       │            │                   │
   ▼      ▼       ▼            ▼                   ▼
┌─────┐ ┌─────┐ ┌─────┐  ┌─────────┐        ┌──────────┐
│ PG  │ │Redis│ │Kafka│◄─┤  Kafka  │        │ WebSocket│
│:5432│ │:6379│ │     │  │Consumer │        │  (STOMP) │
└─────┘ └─────┘ └──┬──┘  └─────────┘        └──────────┘
                   │
        ┌──────────┼──────────┐
        ▼          ▼          ▼
   ┌─────────┐ ┌────────┐ ┌──────────────────────────┐
   │Prometheus│ │ Jaeger │ │ Promtail → Loki → Grafana│
   │  :9090   │ │:16686  │ │     (Logs Pipeline)      │
   └─────────┘ └────────┘ └──────────────────────────┘
```

---

## 1. Language & Runtime

| Aspect | Detail |
|---|---|
| **Language** | Java 21 |
| **Runtime** | Eclipse Temurin JRE 21 (production), JDK 21 (build) |
| **Virtual Threads** | Enabled (`spring.threads.virtual.enabled=true`) — Project Loom |
| **Java Features** | Records, text blocks, `var`, pattern matching, switch expressions |

---

## 2. Build Tool

| Aspect | Detail |
|---|---|
| **Build Tool** | Apache Maven 3.9 (wrapper) |
| **Parent POM** | `spring-boot-starter-parent` 4.0.6 |
| **Code Coverage** | JaCoCo 0.8.12 (donation-service) |
| **Annotation Processors** | Lombok, MapStruct 1.6.3, lombok-mapstruct-binding 0.2.0, spring-boot-configuration-processor |

---

## 3. Frameworks & Core

| Technology | Version | Scope |
|---|---|---|
| **Spring Boot** | 4.0.6 | Application framework (both services) |
| **Spring Framework** | 7.0.0 | Core IoC, MVC, AOP |
| **Spring Modulith** | 2.0.6 | Modular monolith enforcement (donation-service) |
| **Spring Web MVC** | — | REST API controllers |
| **Spring Data JPA** | — | Repository abstraction over Hibernate |
| **Spring Security** | — | Authentication & authorization |
| **Spring Validation** | — | Jakarta Bean Validation (`@Valid`, `@NotNull`) |
| **Spring Scheduling** | — | Cron & fixed-rate task execution |
| **Spring Retry** | 2.0.11 | `@Retryable` with exponential backoff (notification-service) |
| **Spring Aspects** | 7.0.0 | AOP proxy support for `@Retryable` / `@Transactional` |
| **Spring WebSocket** | — | STOMP over SockJS (notification-service) |
| **Spring Kafka** | — | Kafka producer/consumer integration |

---

## 4. Microservices

### 4.1 donation-service (`:8080`)

The core domain service — manages users, donors, donation centers, appointments, emergency requests, health screenings, and audit logs.

**Architecture: Modular Monolith (Spring Modulith) with Hexagonal Architecture**

| Module | Responsibility |
|---|---|
| `user` | Authentication, roles, sessions, JWT, email verification, password reset |
| `donor` | Donor profiles, health questionnaires, eligibility, certificates |
| `center` | Donation centers, slots, staff, closures |
| `appointment` | Booking, health screenings, check-in/complete |
| `emergency` | Emergency requests, geo-based donor matching, responses |
| `system` | GDPR deletion requests |
| `analytics` | Audit logs, metrics dashboard |
| `_shared` | Cross-cutting: events, exceptions, cache, domain types |
| `_config` | Kafka, CORS, cache, tracing configuration |

**Layered structure per module (Hexagonal / Ports & Adapters):**

```
module/
  domain/
    model/          Pure domain objects (no JPA annotations)
    port/in/        CommandUseCases + QueryUseCases interfaces
    port/out/       Repository port interfaces
    service/        Domain validators
    exception/      Domain exceptions & error codes
  application/
    *Service.java   Application services (implements use-case ports)
    proxy/          Inter-module anti-corruption layer
    api/            Public module API (named interfaces)
  infrastructure/
    persistence/
      entity/       JPA entities
      repository/   Spring Data JPA repos
      adapter/      Port → JPA adapter
    web/
      *Controller   REST controllers
      dto/          Request/response records
    mapper/         MapStruct (Domain ↔ Entity ↔ DTO)
    event/          Event publishers & listeners
    scheduler/      Scheduled tasks
    security/       Security configuration
```

**Design Patterns:**
- CQRS-light (separate `CommandUseCases` / `QueryUseCases`)
- Repository Pattern (port interfaces → adapter implementations)
- Anti-Corruption Layer (proxy classes for inter-module calls)
- Event-Driven (in-process Spring events + Kafka for external)
- Strategy Pattern (cache service: Redis / NoOp)
- Observer Pattern (`@EventListener` for domain events)
- Rich Domain Model (behavior on domain objects)

**JPA Entities:** 22 entities including `UserEntity`, `DonorProfileEntity`, `AppointmentEntity`, `EmergencyRequestEntity`, `AuditLogEntity`, `NotificationEntity`, etc.

**Scheduled Tasks:**

| Scheduler | Schedule | Purpose |
|---|---|---|
| SlotGenerationScheduler | `0 0 0 */21 * *` | Generate time slots (21-day lookahead) |
| AppointmentReminderScheduler | `0 0 8 * * *` | Daily 8am appointment reminders |
| EligibilityReminderScheduler | `0 0 9 * * *` | Daily 9am eligibility reminders |
| EligibilityRestorationScheduler | Every 1 hour | Restore expired donor eligibility |
| EmergencyMonitoringService | Every 5 minutes | Expire/escalate emergencies |

### 4.2 notification-service (`:8082`)

Receives events from donation-service via Kafka, dispatches notifications through multiple channels, and pushes real-time updates via WebSocket.

**Architecture: Hexagonal Architecture (Ports & Adapters)**

```
notification/
  domain/
    model/          Notification, NotificationPayload, enums
    port/in/        CommandUseCases + QueryUseCases
    port/out/       RepositoryPort
  application/
    service/        DispatchService, QueryService, ChannelHandler
    dto/            Kafka event records
  infrastructure/
    adapter/persistence/  JPA entity, repo, adapter, MapStruct mapper
    channel/              Notification channel implementations
    kafka/                Kafka consumer (6 listeners)
    web/                  REST controllers (user + internal)
    config/               Security, Kafka, WebSocket, OpenAPI
```

**Notification Channels (Strategy Pattern):**

| Channel | Implementation | Activation |
|---|---|---|
| **IN_APP** | WebSocket push (STOMP) | Always active |
| **EMAIL (SendGrid)** | SendGrid API | `email.channel.provider=sendgrid` |
| **EMAIL (Resend)** | Resend API | `email.channel.provider=resend` |
| **EMAIL (Gmail)** | Spring `JavaMailSender` SMTP | `email.channel.provider=gmail` |
| **PUSH** | Stub (log only) | Always active (not implemented) |

**Resilience:** `@Retryable` with exponential backoff (max 3 attempts, 2s base delay). Idempotent consumption via `correlationId` deduplication.

---

## 5. Database & ORM

### PostgreSQL 16 (Alpine)

| Aspect | Detail |
|---|---|
| **Instance** | Single PostgreSQL server, two databases |
| **Donation DB** | `qatra` — 22 tables, HikariCP pool (max 15, min 2) |
| **Notification DB** | `qatra_notification` — 1 table, HikariCP pool (max 10, min 2) |
| **DDL Management** | Hibernate `ddl-auto` (donation: `create-drop` / notification: `validate`) |
| **No Migrations** | No Flyway or Liquibase — schema managed entirely by Hibernate |
| **OSIV** | Disabled (both services) |
| **JSON Columns** | Hibernate `@JdbcTypeCode(SqlTypes.JSON)` for PostgreSQL `jsonb` |
| **Named Entity Graphs** | Used for eager fetching where needed |
| **Init Script** | `postgres/init.sql` creates both users and databases |

### Redis 7 (Alpine)

| Aspect | Detail |
|---|---|
| **Purpose** | Cache backend for donation-service only |
| **Client** | Lettuce |
| **Serialization** | `GenericJackson2JsonRedisSerializer` (values) + `StringRedisSerializer` (keys) |
| **Pattern** | Cache-aside with pattern-based eviction on writes |
| **Per-Entity TTL** | 60s (emergencies) → 3600s (users/donors) |
| **Fallback** | `NoOpCacheService` when Redis unavailable |

---

## 6. Messaging

### Apache Kafka (Confluent, KRaft mode — no ZooKeeper)

| Aspect | Detail |
|---|---|
| **Mode** | KRaft combined broker + controller (single node) |
| **Cluster ID** | `qatra-kafka-cluster` |
| **Listeners** | `PLAINTEXT://0.0.0.0:9092` (clients), `CONTROLLER://0.0.0.0:9093` |
| **Replication** | Factor 1 (single node) |
| **Serialization** | String key/value with Jackson `ObjectMapper` for JSON payloads |

**Kafka Topics (9):**

| Topic | Partitions | Producer | Consumer |
|---|---|---|---|
| `appointment-events` | 3 | donation-service | — |
| `emergency-events` | 3 | donation-service | — |
| `audit-events` | 2 | donation-service | — |
| `emergency.created` | 3 | donation-service | notification-service |
| `appointment.reminder` | 3 | donation-service | notification-service |
| `eligibility.restored` | 3 | donation-service | notification-service |
| `eligibility.reminder` | 3 | donation-service | notification-service |
| `password.reset` | 3 | donation-service | notification-service |
| `email.verification` | 3 | donation-service | notification-service |

**In-Process Events (Spring ApplicationEventPublisher — donation-service):**
- `AuditEvent` → `AuditLogService`
- `DonationCompletedEvent` → `DonationCertificateEventListener`
- `GDPRDeletionRequestedEvent` → `DonorDeletionEventListener`
- `UserLoggedInEvent` → `DonorLoginEventListener`

---

## 7. Security

| Aspect | donation-service | notification-service |
|---|---|---|
| **Auth Model** | Stateless JWT | OAuth2 Resource Server (JWT) |
| **JWT Library** | JJWT 0.12.6 | NimbusJwtDecoder (Spring) |
| **Signing** | HMAC-SHA256 (symmetric) | HMAC-SHA256 (symmetric) |
| **Password Hashing** | BCrypt | — |
| **Refresh Tokens** | SHA-256 hashed, stored in DB, 30d expiry, rotation | — |
| **Session Policy** | STATELESS | STATELESS |
| **CSRF** | Disabled | Disabled |
| **Method Security** | `@EnableMethodSecurity` + `@PreAuthorize` | `@EnableMethodSecurity` + `@PreAuthorize` |
| **Roles** | `SUPER_ADMIN`, `DONOR`, `CENTER_ADMIN`, `CENTER_STAFF` | `SUPER_ADMIN`, `DONOR`, `CENTER_ADMIN`, `CENTER_STAFF` |
| **JWT Filter** | Custom `JwtAuthenticationFilter` (OncePerRequestFilter) | Auto-configured via OAuth2 resource server |

---

## 8. Real-Time Push

| Aspect | Detail |
|---|---|
| **Protocol** | STOMP over WebSocket with SockJS fallback |
| **Endpoint** | `/ws/notifications` |
| **Broker** | Simple in-memory broker (`/topic`, `/queue`, `/user` prefixes) |
| **Push Mechanism** | `SimpMessagingTemplate.convertAndSendToUser()` → `/user/{userId}/queue/notifications` |
| **Nginx Proxy** | `/ws/` → WebSocket upgrade with 24h read/send timeout |

---

## 9. Email Providers

| Provider | Library | Activation |
|---|---|---|
| **SendGrid** | `sendgrid-java` 4.10.1 | `email.channel.provider=sendgrid` |
| **Resend** | `resend-java` 4.11.0 | `email.channel.provider=resend` |
| **Gmail SMTP** | Spring `JavaMailSender` | `email.channel.provider=gmail` |

Only one provider active at a time. Switched via `email.channel.provider` property.

---

## 10. API Documentation

| Aspect | Detail |
|---|---|
| **Library** | SpringDoc OpenAPI 2.8.6 |
| **Spec** | `/v3/api-docs` |
| **UI** | `/swagger-ui.html` |
| **Public Access** | Swagger endpoints are unauthenticated |

---

## 11. Monitoring & Observability

### Three Pillars

| Pillar | Technology | Details |
|---|---|---|
| **Metrics** | Prometheus + Micrometer | Scrape interval 15s, 30d retention. Both services expose `/actuator/prometheus` |
| **Logs** | Promtail → Loki → Grafana | Docker SD with relabeling, 7d retention, TSDB v13 |
| **Traces** | OpenTelemetry → Jaeger (OTLP) | `opentelemetry-spring-boot-starter` 2.29.0, trace/span IDs in log MDC |

### Application-Side

| Feature | Detail |
|---|---|
| **Spring Actuator** | Exposed: `health`, `info`, `modules` (modulith), `prometheus` |
| **Micrometer** | Prometheus registry for JVM, HTTP, custom metrics |
| **OpenTelemetry** | OTLP/HTTP exporter, `parentbased_traceidratio` sampler |
| **Log Pattern** | `%5p [${spring.application.name:},%X{trace_id:-},%X{span_id:-}]` |
| **Modulith Observability** | Event publication tracking enabled |

### Grafana

- Auto-provisioned datasources: Prometheus (default) + Loki
- Persistent dashboards via `grafana_data` volume

---

## 12. API Gateway

**Nginx (Alpine)**

| Route | Target | Notes |
|---|---|---|
| `/api/v1/notifications` | notification-service:8082 | Notification REST API |
| `/ws/` | notification-service:8082 | WebSocket upgrade, 24h timeout |
| `/ws/notifications/info` | notification-service:8082 | SockJS info endpoint |
| `/api/` | donation-service:8080 | Catch-all REST API |
| `/health` | donation-service:8080 `/actuator/health` | Health check |
| `/` | — | 302 → `/health` |

- `client_max_body_size 10M`
- Forwards: `Host`, `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto`

---

## 13. Containerization

### Docker Multi-Stage Builds

| Service | Build Image | Runtime Image | JAR Strategy |
|---|---|---|---|
| donation-service | `maven:3.9-eclipse-temurin-21` | `eclipse-temurin:21-jre` | Layered JAR extraction (`-Djarmode=layertools`) for optimal cache |
| notification-service | `maven:3.9-eclipse-temurin-21` | `eclipse-temurin:21-jre` | Standard fat JAR |

**Build optimizations:**
- `pom.xml` copied first → `mvn dependency:go-offline -B` → source copied → build
- donation-service skips tests in Docker (`-DskipTests`), notification-service runs them

### Docker Compose

| Component | Service | Image | Port |
|---|---|---|---|
| **Database** | postgres | `postgres:16-alpine` | 5432 |
| **Cache** | redis | `redis:7-alpine` | 6379 |
| **Messaging** | kafka | `confluentinc/cp-kafka:latest` | — (internal) |
| **Application** | donation-service | local build | 8080 |
| **Application** | notification-service | local build | 8082 |
| **Gateway** | nginx | `nginx:alpine` | 80 |
| **Tracing** | jaeger | `jaegertracing/all-in-one:latest` | 16686, 4318 |
| **Metrics** | prometheus | `prom/prometheus:latest` | 9090 |
| **Log Aggregation** | loki | `grafana/loki:latest` | 3100 |
| **Log Shipping** | promtail | `grafana/promtail:latest` | — |
| **Dashboards** | grafana | `grafana/grafana:latest` | 3000 |

**6 named volumes:** `postgres_data`, `kafka_data`, `redis_data`, `prometheus_data`, `loki_data`, `grafana_data`

**Health checks:** PostgreSQL (`pg_isready`), Redis (`redis-cli ping`). Application services depend on infrastructure health.

**Configuration:** All values externalized via `.env` file (root level), referenced as `${VAR}` in `docker-compose.yml`.

---

## 14. PDF Generation

| Aspect | Detail |
|---|---|
| **Library** | OpenPDF 2.0.3 (com.github.librepdf) |
| **Use Case** | Donation certificate PDF generation (donation-service) |

---

## 15. Testing

| Aspect | donation-service | notification-service |
|---|---|---|
| **Framework** | JUnit 5 | JUnit 5 |
| **Mocking** | Mockito | Mockito |
| **Test DB** | H2 in-memory | H2 in-memory |
| **Kafka Test** | — | `spring-kafka-test` (embedded Kafka) |
| **Security Test** | `spring-security-test` | `spring-security-test` |
| **Modulith Test** | `spring-modulith-starter-test` (structure verification) | — |
| **Coverage** | JaCoCo 0.8.12 | — |
| **Test Data** | `data.sql` seeding | — |

---

## 16. Technology Matrix

| Layer | Technology |
|---|---|
| Language | Java 21 (virtual threads) |
| Framework | Spring Boot 4.0.6, Spring Framework 7.0.0 |
| Architecture | Spring Modulith 2.0.6 (monolith), Hexagonal (both services) |
| Build | Maven 3.9 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 (Lettuce) |
| Messaging | Apache Kafka (KRaft, Confluent) |
| Security | Spring Security, JJWT 0.12.6, OAuth2 Resource Server |
| Real-Time | STOMP WebSocket (SockJS) |
| Email | SendGrid 4.10.1, Resend 4.11.0, Gmail SMTP |
| PDF | OpenPDF 2.0.3 |
| Object Mapping | MapStruct 1.6.3 |
| Boilerplate | Lombok |
| Retry | Spring Retry 2.0.11 |
| API Docs | SpringDoc OpenAPI 2.8.6 |
| Metrics | Prometheus, Micrometer, Spring Actuator |
| Logs | Promtail, Loki, Grafana |
| Traces | OpenTelemetry 2.29.0, Jaeger |
| Gateway | Nginx Alpine |
| Containerization | Docker (multi-stage), Docker Compose |
| Code Quality | JaCoCo 0.8.12, Spring Modulith structural tests |



Hibernate:
2026-07-12T15:48:14.130816490Z     create table appointments (
2026-07-12T15:48:14.130838890Z         id bigint generated by default as identity,
2026-07-12T15:48:14.130848490Z         appointment_type varchar(255) not null check ((appointment_type in ('REGULAR','EMERGENCY'))),
2026-07-12T15:48:14.130882790Z         blood_type varchar(255) check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))),
2026-07-12T15:48:14.130889490Z         cancellation_reason varchar(255),
2026-07-12T15:48:14.130893590Z         cancelled_at timestamp(6) with time zone,
2026-07-12T15:48:14.130898190Z         checked_in_at timestamp(6) with time zone,
2026-07-12T15:48:14.130901690Z         completed_at timestamp(6) with time zone,
2026-07-12T15:48:14.130905490Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.130909490Z         ml_collected integer,
2026-07-12T15:48:14.130913590Z         notes TEXT,
2026-07-12T15:48:14.130917390Z         outcome varchar(255) check ((outcome in ('COMPLETED','CANCELLED'))),
2026-07-12T15:48:14.130921590Z         qr_code varchar(255),
2026-07-12T15:48:14.130925390Z         started_at timestamp(6) with time zone,
2026-07-12T15:48:14.130929490Z         status varchar(255) not null check ((status in ('SCHEDULED','CHECKED_IN','IN_SCREENING','COMPLETED','CANCELLED','NO_SHOW','RESCHEDULED'))),
2026-07-12T15:48:14.130933790Z         updated_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.130937590Z         center_id bigint not null,
2026-07-12T15:48:14.130941190Z         completed_by_staff_id bigint,
2026-07-12T15:48:14.130944990Z         donor_id bigint not null,
2026-07-12T15:48:14.130949090Z         emergency_id bigint,
2026-07-12T15:48:14.130952890Z         slot_id bigint not null,
2026-07-12T15:48:14.130956890Z         primary key (id)
2026-07-12T15:48:14.130961290Z     )
2026-07-12T15:48:14.189187491Z Hibernate:
2026-07-12T15:48:14.189234291Z     create table audit_logs (
2026-07-12T15:48:14.189241891Z         id bigint generated by default as identity,
2026-07-12T15:48:14.189246691Z         action varchar(255) not null,
2026-07-12T15:48:14.189251191Z         entity_id bigint,
2026-07-12T15:48:14.189255591Z         entity_type varchar(255),
2026-07-12T15:48:14.189259891Z         ip_address varchar(255),
2026-07-12T15:48:14.189263891Z         new_value jsonb,
2026-07-12T15:48:14.189267991Z         old_value jsonb,
2026-07-12T15:48:14.189271791Z         timestamp timestamp(6) with time zone not null,
2026-07-12T15:48:14.189275991Z         user_id bigint,
2026-07-12T15:48:14.189279791Z         primary key (id)
2026-07-12T15:48:14.189283191Z     )
2026-07-12T15:48:14.228022892Z Hibernate:
2026-07-12T15:48:14.228111992Z     create table center_admin_profiles (
2026-07-12T15:48:14.228124192Z         id bigint generated by default as identity,
2026-07-12T15:48:14.228129292Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.228155792Z         center_id bigint not null,
2026-07-12T15:48:14.228161092Z         user_id bigint not null,
2026-07-12T15:48:14.228165392Z         primary key (id)
2026-07-12T15:48:14.228169792Z     )
2026-07-12T15:48:14.248621393Z Hibernate:
2026-07-12T15:48:14.248665793Z     create table center_staff_profiles (
2026-07-12T15:48:14.248672693Z         id bigint generated by default as identity,
2026-07-12T15:48:14.248676693Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.248680593Z         is_verified boolean not null,
2026-07-12T15:48:14.248684193Z         center_id bigint not null,
2026-07-12T15:48:14.248687893Z         user_id bigint not null,
2026-07-12T15:48:14.248691593Z         primary key (id)
2026-07-12T15:48:14.248695193Z     )
2026-07-12T15:48:14.261764893Z Hibernate:
2026-07-12T15:48:14.261876393Z     create table donation_centers (
2026-07-12T15:48:14.261888393Z         id bigint generated by default as identity,
2026-07-12T15:48:14.261893293Z         address varchar(255) not null,
2026-07-12T15:48:14.261897393Z         city varchar(255) not null,
2026-07-12T15:48:14.261901493Z         country varchar(255) not null,
2026-07-12T15:48:14.261905493Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.261909593Z         email varchar(255) not null,
2026-07-12T15:48:14.261914193Z         facility_type varchar(255) not null check ((facility_type in ('BLOOD_BANK','HOSPITAL','CLINIC','MOBILE_UNIT','COMMUNITY_CENTER'))),
2026-07-12T15:48:14.261918993Z         latitude float(53),
2026-07-12T15:48:14.261923093Z         longitude float(53),
2026-07-12T15:48:14.261927093Z         max_regular integer,
2026-07-12T15:48:14.261931193Z         name varchar(255) not null,
2026-07-12T15:48:14.261935293Z         operating_hours jsonb,
2026-07-12T15:48:14.261939293Z         phone varchar(255) not null,
2026-07-12T15:48:14.261943193Z         postal_code varchar(255),
2026-07-12T15:48:14.261947493Z         slot_period integer,
2026-07-12T15:48:14.261951393Z         status varchar(255) not null check ((status in ('PENDING_APPROVAL','ACTIVE','SUSPENDED','CLOSED'))),
2026-07-12T15:48:14.261955993Z         total_capacity integer,
2026-07-12T15:48:14.261960193Z         updated_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.261964293Z         created_by_user_id bigint,
2026-07-12T15:48:14.261968193Z         primary key (id)
2026-07-12T15:48:14.261972193Z     )
2026-07-12T15:48:14.288420593Z Hibernate:
2026-07-12T15:48:14.288474893Z     create table donation_certificates (
2026-07-12T15:48:14.288503193Z         id bigint generated by default as identity,
2026-07-12T15:48:14.288508993Z         appointment_id bigint not null,
2026-07-12T15:48:14.288762393Z         center_id bigint not null,
2026-07-12T15:48:14.288773893Z         center_name varchar(255) not null,
2026-07-12T15:48:14.288779093Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.288783593Z         donation_date date not null,
2026-07-12T15:48:14.288787893Z         donor_id bigint not null,
2026-07-12T15:48:14.288792993Z         donor_name varchar(255) not null,
2026-07-12T15:48:14.288797493Z         ml_collected integer,
2026-07-12T15:48:14.288802193Z         primary key (id)
2026-07-12T15:48:14.288806293Z     )
2026-07-12T15:48:14.316115494Z Hibernate:
2026-07-12T15:48:14.316162294Z     create table donor_profiles (
2026-07-12T15:48:14.316168494Z         id bigint generated by default as identity,
2026-07-12T15:48:14.316172394Z         allow_emergency_notifications boolean not null,
2026-07-12T15:48:14.316176194Z         availability varchar(255) check ((availability in ('AVAILABLE','TEMPORARILY_UNAVAILABLE','VACATION_MODE','PERMANENTLY_RESTRICTED'))),
2026-07-12T15:48:14.316180594Z         blood_type varchar(255) check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))),
2026-07-12T15:48:14.316184894Z         blood_type_verified boolean not null,
2026-07-12T15:48:14.316188494Z         city varchar(255),
2026-07-12T15:48:14.316192094Z         consecutive_emergency_declines integer not null,
2026-07-12T15:48:14.316195794Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.316199994Z         deleted_at timestamp(6) with time zone,
2026-07-12T15:48:14.316203794Z         deletion_requested_at timestamp(6) with time zone,
2026-07-12T15:48:14.316207994Z         eligible_from_date date,
2026-07-12T15:48:14.316211794Z         flagged_for_manual_review boolean not null,
2026-07-12T15:48:14.316215694Z         last_accept_at timestamp(6) with time zone,
2026-07-12T15:48:14.316219694Z         last_donation_date date,
2026-07-12T15:48:14.316223594Z         latitude float(53),
2026-07-12T15:48:14.316232494Z         longitude float(53),
2026-07-12T15:48:14.316236394Z         notification_preferences jsonb,
2026-07-12T15:48:14.316240394Z         permanently_restricted boolean not null,
2026-07-12T15:48:14.316244694Z         profile_complete boolean not null,
2026-07-12T15:48:14.316248794Z         reliability_score float(53) not null,
2026-07-12T15:48:14.316252794Z         restriction_reason varchar(255),
2026-07-12T15:48:14.316256794Z         status varchar(255) check ((status in ('ACTIVE','INACTIVE','PENDING_DELETION','DELETED'))),
2026-07-12T15:48:14.316285494Z         total_donations integer not null,
2026-07-12T15:48:14.316290294Z         updated_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.316294494Z         user_id bigint not null,
2026-07-12T15:48:14.316298394Z         primary key (id)
2026-07-12T15:48:14.316302594Z     )
2026-07-12T15:48:14.345252095Z Hibernate:
2026-07-12T15:48:14.345303095Z     create table donor_responses (
2026-07-12T15:48:14.345308195Z         id bigint generated by default as identity,
2026-07-12T15:48:14.345311095Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.345314395Z         reason varchar(255),
2026-07-12T15:48:14.345318695Z         responded_at timestamp(6) with time zone,
2026-07-12T15:48:14.345322895Z         status varchar(255) not null check ((status in ('ACCEPTED','DECLINED'))),
2026-07-12T15:48:14.345327695Z         donor_id bigint not null,
2026-07-12T15:48:14.345331895Z         emergency_id bigint not null,
2026-07-12T15:48:14.345335895Z         slot_id bigint,
2026-07-12T15:48:14.345340295Z         primary key (id)
2026-07-12T15:48:14.345343895Z     )
2026-07-12T15:48:14.368632395Z Hibernate:
2026-07-12T15:48:14.368719295Z     create table emergency_requests (
2026-07-12T15:48:14.368728595Z         id bigint generated by default as identity,
2026-07-12T15:48:14.368733395Z         blood_type varchar(255) not null check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))),
2026-07-12T15:48:14.368737795Z         contact_phone varchar(255),
2026-07-12T15:48:14.368741395Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.368745195Z         escalation_level integer not null,
2026-07-12T15:48:14.368749395Z         expires_at timestamp(6) with time zone,
2026-07-12T15:48:14.368752995Z         match_radius integer not null,
2026-07-12T15:48:14.368756395Z         resolved_at timestamp(6) with time zone,
2026-07-12T15:48:14.368760295Z         status varchar(255) not null check ((status in ('OPEN','FULFILLED','CANCELLED','EXPIRED'))),
2026-07-12T15:48:14.368764295Z         units_needed integer not null,
2026-07-12T15:48:14.368767695Z         updated_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.368771495Z         urgency varchar(255) not null check ((urgency in ('CRITICAL','HIGH','MEDIUM','LOW'))),
2026-07-12T15:48:14.368775295Z         center_id bigint not null,
2026-07-12T15:48:14.368779095Z         created_by_staff_id bigint not null,
2026-07-12T15:48:14.368782595Z         resolved_by_user_id bigint,
2026-07-12T15:48:14.368786495Z         primary key (id)
2026-07-12T15:48:14.368814495Z     )
2026-07-12T15:48:14.391378396Z Hibernate:
2026-07-12T15:48:14.391425096Z     create table gdpr_deletion_requests (
2026-07-12T15:48:14.391431996Z         id bigint generated by default as identity,
2026-07-12T15:48:14.391436296Z         processed_at timestamp(6) with time zone,
2026-07-12T15:48:14.391440496Z         reason TEXT,
2026-07-12T15:48:14.391444496Z         requested_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.391448296Z         status varchar(255) not null check ((status in ('IN_PROGRESS','CANCELED','COMPLETED'))),
2026-07-12T15:48:14.391452296Z         user_id bigint not null,
2026-07-12T15:48:14.391456196Z         primary key (id)
2026-07-12T15:48:14.391460096Z     )
2026-07-12T15:48:14.413321996Z Hibernate:
2026-07-12T15:48:14.413377296Z     create table health_questionnaires (
2026-07-12T15:48:14.413385396Z         id bigint generated by default as identity,
2026-07-12T15:48:14.413390496Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.413394396Z         has_chronic_illness boolean not null,
2026-07-12T15:48:14.413398296Z         last_surgery_at timestamp(6) with time zone,
2026-07-12T15:48:14.413402696Z         last_tattoo_or_piercing_at timestamp(6) with time zone,
2026-07-12T15:48:14.413406896Z         last_travel_at timestamp(6) with time zone,
2026-07-12T15:48:14.413411196Z         medical_conditions_details TEXT,
2026-07-12T15:48:14.413415696Z         medication_details TEXT,
2026-07-12T15:48:14.413419996Z         on_medication boolean not null,
2026-07-12T15:48:14.413424296Z         updated_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.413428296Z         donor_id bigint not null,
2026-07-12T15:48:14.413431796Z         primary key (id)
2026-07-12T15:48:14.413436096Z     )
2026-07-12T15:48:14.435521397Z Hibernate:
2026-07-12T15:48:14.435607897Z     create table health_screenings (
2026-07-12T15:48:14.435618897Z         id bigint generated by default as identity,
2026-07-12T15:48:14.435624097Z         blood_pressure varchar(255),
2026-07-12T15:48:14.435629597Z         eligible boolean not null,
2026-07-12T15:48:14.435633497Z         hemoglobin float(53),
2026-07-12T15:48:14.435637497Z         notes TEXT,
2026-07-12T15:48:14.435641597Z         screened_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.435645597Z         temperature float(53),
2026-07-12T15:48:14.435649397Z         weight float(53),
2026-07-12T15:48:14.435653397Z         appointment_id bigint not null,
2026-07-12T15:48:14.435657297Z         donor_id bigint not null,
2026-07-12T15:48:14.435683097Z         screened_by_staff_id bigint not null,
2026-07-12T15:48:14.435688797Z         primary key (id)
2026-07-12T15:48:14.435692897Z     )
2026-07-12T15:48:14.462725897Z Hibernate:
2026-07-12T15:48:14.462779597Z     create table match_results (
2026-07-12T15:48:14.462787697Z         id bigint generated by default as identity,
2026-07-12T15:48:14.462793297Z         blood_type varchar(255) not null check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))),
2026-07-12T15:48:14.462798497Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.462802797Z         escalation_level integer not null,
2026-07-12T15:48:14.462807397Z         radius bigint not null,
2026-07-12T15:48:14.462811397Z         responded_at timestamp(6) with time zone,
2026-07-12T15:48:14.462815697Z         status varchar(255) not null check ((status in ('PENDING','RESPONDED','EXPIRED'))),
2026-07-12T15:48:14.462820297Z         center_id bigint not null,
2026-07-12T15:48:14.462824597Z         donor_id bigint not null,
2026-07-12T15:48:14.462829097Z         emergency_id bigint not null,
2026-07-12T15:48:14.462833597Z         primary key (id)
2026-07-12T15:48:14.462837997Z     )
2026-07-12T15:48:14.487736898Z Hibernate:
2026-07-12T15:48:14.487799698Z     create table sessions (
2026-07-12T15:48:14.487842298Z         id bigint generated by default as identity,
2026-07-12T15:48:14.487852598Z         access_token_hash varchar(255) not null,
2026-07-12T15:48:14.487858198Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.487862998Z         expires_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.487867298Z         ip_address varchar(255),
2026-07-12T15:48:14.487871798Z         refresh_token_hash varchar(255) not null,
2026-07-12T15:48:14.487876398Z         user_agent varchar(255),
2026-07-12T15:48:14.487881198Z         user_id bigint not null,
2026-07-12T15:48:14.487885798Z         primary key (id)
2026-07-12T15:48:14.487890298Z     )
2026-07-12T15:48:14.511098498Z Hibernate:
2026-07-12T15:48:14.511194998Z     create table slots (
2026-07-12T15:48:14.511207298Z         id bigint generated by default as identity,
2026-07-12T15:48:14.511217398Z         booked_count integer not null,
2026-07-12T15:48:14.511221898Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.511226198Z         date date not null,
2026-07-12T15:48:14.511230498Z         end_time time(0) not null,
2026-07-12T15:48:14.511234898Z         is_blocked boolean not null,
2026-07-12T15:48:14.511267098Z         max_bookings integer not null,
2026-07-12T15:48:14.511273698Z         max_regular_bookings integer not null,
2026-07-12T15:48:14.511277798Z         regular_booked_count integer not null,
2026-07-12T15:48:14.511281398Z         start_time time(0) not null,
2026-07-12T15:48:14.511285198Z         center_id bigint not null,
2026-07-12T15:48:14.511288998Z         primary key (id)
2026-07-12T15:48:14.511292598Z     )
2026-07-12T15:48:14.527962999Z Hibernate:
2026-07-12T15:48:14.528058699Z     create table user_roles (
2026-07-12T15:48:14.528070099Z         id bigint generated by default as identity,
2026-07-12T15:48:14.528073499Z         assigned_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.528076299Z         role varchar(255) not null check ((role in ('SUPER_ADMIN','CENTER_ADMIN','CENTER_STAFF','DONOR'))),
2026-07-12T15:48:14.528079099Z         user_id bigint not null,
2026-07-12T15:48:14.528081699Z         primary key (id)
2026-07-12T15:48:14.528084699Z     )
2026-07-12T15:48:14.542553099Z Hibernate:
2026-07-12T15:48:14.542646299Z     create table users (
2026-07-12T15:48:14.542659299Z         id bigint generated by default as identity,
2026-07-12T15:48:14.542664199Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.542668699Z         deleted_at timestamp(6) with time zone,
2026-07-12T15:48:14.542673099Z         deletion_requested_at timestamp(6) with time zone,
2026-07-12T15:48:14.542678199Z         display_name varchar(255) not null,
2026-07-12T15:48:14.542682599Z         email varchar(255) not null,
2026-07-12T15:48:14.542686999Z         email_verified boolean not null,
2026-07-12T15:48:14.542691299Z         family_name varchar(255),
2026-07-12T15:48:14.542695699Z         first_name varchar(255),
2026-07-12T15:48:14.542699799Z         hashed_password varchar(255) not null,
2026-07-12T15:48:14.542704199Z         last_active_at timestamp(6) with time zone,
2026-07-12T15:48:14.542708399Z         phone varchar(255) not null,
2026-07-12T15:48:14.542712799Z         status varchar(255) not null check ((status in ('ACTIVE','INACTIVE','SUSPENDED','PENDING_VERIFICATION','PENDING_DELETION','DELETED'))),
2026-07-12T15:48:14.542717899Z         primary key (id)
2026-07-12T15:48:14.542722499Z     )
2026-07-12T15:48:14.566708999Z Hibernate:
2026-07-12T15:48:14.566790399Z     create table verification_tokens (
2026-07-12T15:48:14.566801499Z         id bigint generated by default as identity,
2026-07-12T15:48:14.566806599Z         created_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.566811399Z         expires_at timestamp(6) with time zone not null,
2026-07-12T15:48:14.566842399Z         token_hash varchar(255) not null,
2026-07-12T15:48:14.566847999Z         type varchar(255) not null check ((type in ('EMAIL_VERIFICATION','PASSWORD_RESET'))),
2026-07-12T15:48:14.566852299Z         user_id bigint not null,
2026-07-12T15:48:14.566855999Z         primary key (id)
2026-07-12T15:48:14.566859699Z     )
2026-07-12T15:48:14.593067000Z Hibernate:
2026-07-12T15:48:14.593130600Z     alter table if exists center_admin_profiles
2026-07-12T15:48:14.593139900Z        drop constraint if exists UKp67a9rntlncmiks6s5kuop6wv
2026-07-12T15:48:14.603638900Z 2026-07-12T15:48:14.602Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.605955600Z 2026-07-12T15:48:14.604Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "ukp67a9rntlncmiks6s5kuop6wv" of relation "center_admin_profiles" does not exist, skipping
2026-07-12T15:48:14.607791200Z Hibernate:
2026-07-12T15:48:14.607822700Z     alter table if exists center_admin_profiles
2026-07-12T15:48:14.607830100Z        add constraint UKp67a9rntlncmiks6s5kuop6wv unique (user_id)
2026-07-12T15:48:14.621310401Z Hibernate:
2026-07-12T15:48:14.621392201Z     alter table if exists center_staff_profiles
2026-07-12T15:48:14.621405701Z        drop constraint if exists UK73s9a7kvh2tghfh89qln5bby5
2026-07-12T15:48:14.625774301Z 2026-07-12T15:48:14.625Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.626015101Z 2026-07-12T15:48:14.625Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "uk73s9a7kvh2tghfh89qln5bby5" of relation "center_staff_profiles" does not exist, skipping
2026-07-12T15:48:14.627215501Z Hibernate:
2026-07-12T15:48:14.627239901Z     alter table if exists center_staff_profiles
2026-07-12T15:48:14.627246101Z        add constraint UK73s9a7kvh2tghfh89qln5bby5 unique (user_id)
2026-07-12T15:48:14.638407801Z Hibernate:
2026-07-12T15:48:14.638499901Z     alter table if exists donation_centers
2026-07-12T15:48:14.638511401Z        drop constraint if exists UKkrv6x08cvcuqclu70wwdb3rxc
2026-07-12T15:48:14.642941801Z 2026-07-12T15:48:14.642Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.643064001Z 2026-07-12T15:48:14.642Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "ukkrv6x08cvcuqclu70wwdb3rxc" of relation "donation_centers" does not exist, skipping
2026-07-12T15:48:14.644488001Z Hibernate:
2026-07-12T15:48:14.644521901Z     alter table if exists donation_centers
2026-07-12T15:48:14.644528001Z        add constraint UKkrv6x08cvcuqclu70wwdb3rxc unique (name)
2026-07-12T15:48:14.654571701Z Hibernate:
2026-07-12T15:48:14.654687401Z     alter table if exists donor_profiles
2026-07-12T15:48:14.654702101Z        drop constraint if exists UKg4i8jo94ircw8dchtf2e5rlmg
2026-07-12T15:48:14.667119701Z 2026-07-12T15:48:14.661Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.667175701Z 2026-07-12T15:48:14.661Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "ukg4i8jo94ircw8dchtf2e5rlmg" of relation "donor_profiles" does not exist, skipping
2026-07-12T15:48:14.667184601Z Hibernate:
2026-07-12T15:48:14.667189401Z     alter table if exists donor_profiles
2026-07-12T15:48:14.667193701Z        add constraint UKg4i8jo94ircw8dchtf2e5rlmg unique (user_id)
2026-07-12T15:48:14.677744502Z Hibernate:
2026-07-12T15:48:14.677792702Z     alter table if exists health_questionnaires
2026-07-12T15:48:14.677799802Z        drop constraint if exists UKd0eencndc28p59g0xsd8d62mc
2026-07-12T15:48:14.682513802Z 2026-07-12T15:48:14.681Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.682851802Z 2026-07-12T15:48:14.682Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "ukd0eencndc28p59g0xsd8d62mc" of relation "health_questionnaires" does not exist, skipping
2026-07-12T15:48:14.683761202Z Hibernate:
2026-07-12T15:48:14.683790502Z     alter table if exists health_questionnaires
2026-07-12T15:48:14.683796702Z        add constraint UKd0eencndc28p59g0xsd8d62mc unique (donor_id)
2026-07-12T15:48:14.695183402Z Hibernate:
2026-07-12T15:48:14.695275902Z     alter table if exists user_roles
2026-07-12T15:48:14.695289002Z        drop constraint if exists UKs23eulmpf814we7643dhwwrkq
2026-07-12T15:48:14.700857802Z 2026-07-12T15:48:14.699Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.700979902Z 2026-07-12T15:48:14.700Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "uks23eulmpf814we7643dhwwrkq" of relation "user_roles" does not exist, skipping
2026-07-12T15:48:14.701659002Z Hibernate:
2026-07-12T15:48:14.701694902Z     alter table if exists user_roles
2026-07-12T15:48:14.701702102Z        add constraint UKs23eulmpf814we7643dhwwrkq unique (user_id, role)
2026-07-12T15:48:14.714624803Z Hibernate:
2026-07-12T15:48:14.714708003Z     alter table if exists users
2026-07-12T15:48:14.714718703Z        drop constraint if exists UK6dotkott2kjsp8vw4d0m25fb7
2026-07-12T15:48:14.719822203Z 2026-07-12T15:48:14.718Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.721129803Z 2026-07-12T15:48:14.720Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "uk6dotkott2kjsp8vw4d0m25fb7" of relation "users" does not exist, skipping
2026-07-12T15:48:14.721462703Z Hibernate:
2026-07-12T15:48:14.721480303Z     alter table if exists users
2026-07-12T15:48:14.721485403Z        add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email)
2026-07-12T15:48:14.733791903Z Hibernate:
2026-07-12T15:48:14.733857303Z     alter table if exists users
2026-07-12T15:48:14.733866503Z        drop constraint if exists UKdu5v5sr43g5bfnji4vb8hg5s3
2026-07-12T15:48:14.738465803Z 2026-07-12T15:48:14.737Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : HHH000247: ErrorCode: 0, SQLState: 00000
2026-07-12T15:48:14.738650603Z 2026-07-12T15:48:14.738Z  WARN 1 --- [Qatra] [           main] org.hibernate.orm.jdbc.warn              : constraint "ukdu5v5sr43g5bfnji4vb8hg5s3" of relation "users" does not exist, skipping
2026-07-12T15:48:14.739258003Z Hibernate:
2026-07-12T15:48:14.739276603Z     alter table if exists users
2026-07-12T15:48:14.739282903Z        add constraint UKdu5v5sr43g5bfnji4vb8hg5s3 unique (phone)
2026-07-12T15:48:14.752200203Z Hibernate:
2026-07-12T15:48:14.752267503Z     alter table if exists appointments
2026-07-12T15:48:14.752276403Z        add constraint FK8t6o6t5vjcc3uvifx86tn85ag
2026-07-12T15:48:14.752281903Z        foreign key (center_id)
2026-07-12T15:48:14.752286603Z        references donation_centers
2026-07-12T15:48:14.766629304Z Hibernate:
2026-07-12T15:48:14.766722704Z     alter table if exists appointments
2026-07-12T15:48:14.766737804Z        add constraint FKmh6grfeyubkv8qrjctiyesbvo
2026-07-12T15:48:14.766743204Z        foreign key (completed_by_staff_id)
2026-07-12T15:48:14.766747304Z        references users
2026-07-12T15:48:14.774925804Z Hibernate:
2026-07-12T15:48:14.775020104Z     alter table if exists appointments
2026-07-12T15:48:14.775034304Z        add constraint FK40kk6n2tjic65mfoti09s0e3h
2026-07-12T15:48:14.775041104Z        foreign key (donor_id)
2026-07-12T15:48:14.775045704Z        references donor_profiles
2026-07-12T15:48:14.782833504Z Hibernate:
2026-07-12T15:48:14.782928304Z     alter table if exists appointments
2026-07-12T15:48:14.782942904Z        add constraint FKbw429fhc3acws3demr0tubixy
2026-07-12T15:48:14.782948404Z        foreign key (emergency_id)
2026-07-12T15:48:14.782952904Z        references emergency_requests
2026-07-12T15:48:14.791226704Z Hibernate:
2026-07-12T15:48:14.791321204Z     alter table if exists appointments
2026-07-12T15:48:14.791335704Z        add constraint FKf8qrv9g386dae81yfkj1qgs77
2026-07-12T15:48:14.791340904Z        foreign key (slot_id)
2026-07-12T15:48:14.791373704Z        references slots
2026-07-12T15:48:14.801003304Z Hibernate:
2026-07-12T15:48:14.801108304Z     alter table if exists audit_logs
2026-07-12T15:48:14.801123104Z        add constraint FKjs4iimve3y0xssbtve5ysyef0
2026-07-12T15:48:14.801128804Z        foreign key (user_id)
2026-07-12T15:48:14.801174304Z        references users
2026-07-12T15:48:14.810286005Z Hibernate:
2026-07-12T15:48:14.810383305Z     alter table if exists center_admin_profiles
2026-07-12T15:48:14.810396105Z        add constraint FK3unair5cbae9fcdvffird0r5b
2026-07-12T15:48:14.810401305Z        foreign key (center_id)
2026-07-12T15:48:14.810405705Z        references donation_centers
2026-07-12T15:48:14.819328105Z Hibernate:
2026-07-12T15:48:14.819372905Z     alter table if exists center_admin_profiles
2026-07-12T15:48:14.819378005Z        add constraint FK2ie2ngbkls549c89j6t3xkks1
2026-07-12T15:48:14.819380805Z        foreign key (user_id)
2026-07-12T15:48:14.819384205Z        references users
2026-07-12T15:48:14.827333405Z Hibernate:
2026-07-12T15:48:14.827397605Z     alter table if exists center_staff_profiles
2026-07-12T15:48:14.827405805Z        add constraint FK3kay45pxiu20mnc5llaet9en4
2026-07-12T15:48:14.827411705Z        foreign key (center_id)
2026-07-12T15:48:14.827416405Z        references donation_centers
2026-07-12T15:48:14.835736405Z Hibernate:
2026-07-12T15:48:14.835827305Z     alter table if exists center_staff_profiles
2026-07-12T15:48:14.835841305Z        add constraint FK7op1e785odjk73vsh5tgsytq6
2026-07-12T15:48:14.835847505Z        foreign key (user_id)
2026-07-12T15:48:14.835852105Z        references users
2026-07-12T15:48:14.843904305Z Hibernate:
2026-07-12T15:48:14.843962105Z     alter table if exists donation_centers
2026-07-12T15:48:14.843969905Z        add constraint FK6yvqj61il694ngcysknqj6b5
2026-07-12T15:48:14.843974505Z        foreign key (created_by_user_id)
2026-07-12T15:48:14.843979705Z        references users
2026-07-12T15:48:14.851767405Z Hibernate:
2026-07-12T15:48:14.851815905Z     alter table if exists donor_profiles
2026-07-12T15:48:14.851823305Z        add constraint FK7h7kum7l387kv5d3o4yh3oyow
2026-07-12T15:48:14.851827705Z        foreign key (user_id)
2026-07-12T15:48:14.851831905Z        references users
2026-07-12T15:48:14.859442506Z Hibernate:
2026-07-12T15:48:14.859494606Z     alter table if exists donor_responses
2026-07-12T15:48:14.859502106Z        add constraint FKgecvahm07s7rkbkx2pg8qcoij
2026-07-12T15:48:14.859507306Z        foreign key (donor_id)
2026-07-12T15:48:14.859541206Z        references donor_profiles
2026-07-12T15:48:14.867893506Z Hibernate:
2026-07-12T15:48:14.867937006Z     alter table if exists donor_responses
2026-07-12T15:48:14.867944206Z        add constraint FKeehm5xbo3qc2qvjqfx73g3i3r
2026-07-12T15:48:14.867948106Z        foreign key (emergency_id)
2026-07-12T15:48:14.867951506Z        references emergency_requests
2026-07-12T15:48:14.874981206Z Hibernate:
2026-07-12T15:48:14.875018906Z     alter table if exists donor_responses
2026-07-12T15:48:14.875022906Z        add constraint FKbwybtjwfypg7tuguclm38agy5
2026-07-12T15:48:14.875025706Z        foreign key (slot_id)
2026-07-12T15:48:14.875028206Z        references slots
2026-07-12T15:48:14.882767606Z Hibernate:
2026-07-12T15:48:14.882862106Z     alter table if exists emergency_requests
2026-07-12T15:48:14.882875506Z        add constraint FKntewm05gfckwf5pycr90fumom
2026-07-12T15:48:14.882882206Z        foreign key (center_id)
2026-07-12T15:48:14.882886706Z        references donation_centers
2026-07-12T15:48:14.890300106Z Hibernate:
2026-07-12T15:48:14.890394506Z     alter table if exists emergency_requests
2026-07-12T15:48:14.890407006Z        add constraint FKjlrdxs6d0yiqh69aqaee8c5vb
2026-07-12T15:48:14.890412006Z        foreign key (created_by_staff_id)
2026-07-12T15:48:14.890416006Z        references users
2026-07-12T15:48:14.899074506Z Hibernate:
2026-07-12T15:48:14.899161006Z     alter table if exists emergency_requests
2026-07-12T15:48:14.899174806Z        add constraint FKpv2453x026e5ee2bxsster4m0
2026-07-12T15:48:14.899180606Z        foreign key (resolved_by_user_id)
2026-07-12T15:48:14.899185006Z        references users
2026-07-12T15:48:14.908477307Z Hibernate:
2026-07-12T15:48:14.908526107Z     alter table if exists gdpr_deletion_requests
2026-07-12T15:48:14.908532607Z        add constraint FKghl6ed4uf8mvpeuf7ne2x8r6s
2026-07-12T15:48:14.908537307Z        foreign key (user_id)
2026-07-12T15:48:14.908541507Z        references users
2026-07-12T15:48:14.916942707Z Hibernate:
2026-07-12T15:48:14.917034207Z     alter table if exists health_questionnaires
2026-07-12T15:48:14.917046607Z        add constraint FK35ank7o4ndsgn8uasq91c35v
2026-07-12T15:48:14.917051707Z        foreign key (donor_id)
2026-07-12T15:48:14.917056007Z        references donor_profiles
2026-07-12T15:48:14.924154407Z Hibernate:
2026-07-12T15:48:14.924248407Z     alter table if exists health_screenings
2026-07-12T15:48:14.924262107Z        add constraint FKqimkii6yl73wl65fjo9x9nurr
2026-07-12T15:48:14.924288607Z        foreign key (appointment_id)
2026-07-12T15:48:14.924293907Z        references appointments
2026-07-12T15:48:14.932835907Z Hibernate:
2026-07-12T15:48:14.932881607Z     alter table if exists health_screenings
2026-07-12T15:48:14.932889107Z        add constraint FKeggvph28i357upas6nrrggdby
2026-07-12T15:48:14.932893707Z        foreign key (donor_id)
2026-07-12T15:48:14.932898007Z        references donor_profiles
2026-07-12T15:48:14.939607907Z Hibernate:
2026-07-12T15:48:14.939664307Z     alter table if exists health_screenings
2026-07-12T15:48:14.939671607Z        add constraint FKhvlm8wduedhh358n6bae72su4
2026-07-12T15:48:14.939675907Z        foreign key (screened_by_staff_id)
2026-07-12T15:48:14.939679407Z        references users
2026-07-12T15:48:14.947220207Z Hibernate:
2026-07-12T15:48:14.947314307Z     alter table if exists match_results
2026-07-12T15:48:14.947327407Z        add constraint FK3lve29d2fda04rwn85n9fqhwu
2026-07-12T15:48:14.947333207Z        foreign key (center_id)
2026-07-12T15:48:14.947337507Z        references donation_centers
2026-07-12T15:48:14.955034408Z Hibernate:
2026-07-12T15:48:14.955082808Z     alter table if exists match_results
2026-07-12T15:48:14.955089408Z        add constraint FKly5smiundxf050odbu2a57j6u
2026-07-12T15:48:14.955094008Z        foreign key (donor_id)
2026-07-12T15:48:14.955097908Z        references donor_profiles
2026-07-12T15:48:14.963168808Z Hibernate:
2026-07-12T15:48:14.963261008Z     alter table if exists match_results
2026-07-12T15:48:14.963273708Z        add constraint FK9dsts9wvoio2ctiq6l1aew7gc
2026-07-12T15:48:14.963278508Z        foreign key (emergency_id)
2026-07-12T15:48:14.963281908Z        references emergency_requests
2026-07-12T15:48:14.971014608Z Hibernate:
2026-07-12T15:48:14.971112308Z     alter table if exists sessions
2026-07-12T15:48:14.971122708Z        add constraint FKruie73rneumyyd1bgo6qw8vjt
2026-07-12T15:48:14.971125908Z        foreign key (user_id)
2026-07-12T15:48:14.971128308Z        references users
2026-07-12T15:48:14.979162308Z Hibernate:
2026-07-12T15:48:14.979217908Z     alter table if exists slots
2026-07-12T15:48:14.979226208Z        add constraint FKb7nnlkyh9mgb1jhklyu58nptp
2026-07-12T15:48:14.979230708Z        foreign key (center_id)
2026-07-12T15:48:14.979235008Z        references donation_centers
2026-07-12T15:48:14.986229608Z Hibernate:
2026-07-12T15:48:14.986276608Z     alter table if exists user_roles
2026-07-12T15:48:14.986306208Z        add constraint FKhfh9dx7w3ubf1co1vdev94g3f
2026-07-12T15:48:14.986343908Z        foreign key (user_id)
2026-07-12T15:48:14.986350208Z        references users
2026-07-12T15:48:14.996396109Z Hibernate:
2026-07-12T15:48:14.996489809Z     alter table if exists verification_tokens
2026-07-12T15:48:14.996502509Z        add constraint FK54y8mqsnq1rtyf581sfmrbp4f
2026-07-12T15:48:14.996507809Z        foreign key (user_id)
2026-07-12T15:48:14.996512009Z        references users