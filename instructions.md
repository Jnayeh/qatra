
# Backend Development Instructions — Blood Donation Platform (Qatra)

> **Audience:** An LLM (or human) implementing the backend from scratch.
> **Goal:** Produce a fully working Spring Boot 4 modular monolith + standalone
> notification microservice that satisfy every use case in the requirements,
> following the hexagonal architecture described below.

---

## 0. Quick Reference

| Concern | Choice |
|---|---|
| Language | Java 21 (Records, Virtual Threads, pattern matching) |
| Build | Maven, Spring Boot 4.0.6 parent |
| Architecture | Hexagonal + Spring Modulith (modular monolith) |
| API | REST `/api/v1/`, Springdoc OpenAPI 2.8.6 |
| Persistence | Spring Data JPA / Hibernate, PostgreSQL (prod), H2 (test) |
| Pool | HikariCP (Spring default) |
| Validation | Jakarta Validation (`@Valid`, `@NotNull`, `@Size`, …) |
| Monitoring | Spring Actuator — `/actuator/health`, `/actuator/info`, `/actuator/modulith` |
| Messaging | Apache Kafka — events from main service → notification microservice |
| Real-time | WebSocket (STOMP over SockJS) — IN_APP channel in notification service |
| Extras | Lombok, DevTools, Virtual Threads enabled globally |
| Testing | JUnit 5 + AssertJ + Spring Modulith verification tests |
| Frontend | None — pure REST API |

---

## 1. Project Layout

Two deployable services share this repository:

```
Qatra/
├── donation-service/                       ← Main Spring Modulith service
│   └── src/main/java/com/zayenha/qatra/
│       ├── QatraApplication.java           ← @SpringBootApplication
│       │                                     Seeds SUPER_ADMIN on first start (see §5.2)
│       ├── shared/                         ← Cross-cutting (never imports a module)
│       │   ├── domain/
│       │   │   ├── BloodType.java          (enum + canDonateTo() helper)
│       │   │   ├── GeoUtils.java           (Haversine helper)
│       │   │   └── PageResult.java         (record)
│       │   ├── exception/
│       │   │   ├── DomainException.java
│       │   │   ├── NotFoundException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── kafka/
│       │   │   └── NotificationKafkaProducer.java
│       │   └── web/
│       │       └── ApiResponse.java        (record wrapper)
│       │
│       ├── user/                           ← Module: user management, auth, sessions
│       ├── donor/                          ← Module: donor profile, health, eligibility
│       ├── center/                         ← Module: centers, slots, staff/admin profiles
│       ├── emergency/                      ← Module: emergencies, matching, responses
│       ├── appointment/                    ← Module: appointments, health screenings
│       ├── analytics/                      ← Module: metrics, forecasts, audit log
│       └── system/                         ← Module: config, feature flags, GDPR
│
└── notification-service/                   ← Standalone microservice
└── src/main/java/com/zayenha/qatra/notification/
├── NotificationServiceApplication.java
├── consumer/
│   └── NotificationKafkaConsumer.java
├── channel/
│   ├── NotificationChannel.java        (strategy interface)
│   ├── SendGridEmailChannel.java       (primary email provider)
│   ├── GmailSmtpEmailChannel.java      (fallback email provider)
│   └── InAppNotificationChannel.java   (WebSocket/STOMP)
├── domain/
│   ├── Notification.java               (entity, owned by this service)
│   ├── NotificationStatus.java         (enum)
│   └── port/...
├── application/
│   └── NotificationDispatchService.java
└── infrastructure/
├── persistence/...
└── websocket/
└── WebSocketConfig.java
```



Each backend module is a **Spring Modulith module** — enforced by the modulith
verification test. Modules communicate only through their **published API**
(the `api` sub-package). Direct cross-module entity access is forbidden.

The `notification` domain has been **extracted to its own service**. The main backend
publishes Kafka events and **never stores notification records**. The notification
service owns the `Notification` entity, its persistence, and all notification REST
endpoints for clients.

---

## 2. Hexagonal Architecture — Canonical Module Structure

Every module follows this internal layout. The `user` module is shown as the full example. \
This is an example not a strict template — adapt as needed per module, but keep the same principles of separation and dependency direction.

```
user/                                                                                      
├── api/                           ← PUBLIC — other modules may import only this                                                     
│   ├── dto/                                                                         
│   │   ├── UserCreatedEvent.java      (Spring ApplicationEvent published outward)                                                    
│   │   └── UserSummary.java           (record — read-only projection for other modules)                        
│   ├── package-info.java                                                            
│   └── UserApi.java                   ← Spring @Bean / interface other modules call                                                                                                         
├── application/                       ← USE-CASE IMPLEMENTATIONS (@Service, @Transactional)                                                                                           
│   ├── mapper/                                                                                         
│   │   └── UserDomainMapper.java       (converts domain ↔ application DTOs)                                                                                            
│   └── UserService.java                (implements use-case ports)                                                                                                     
├── domain/                             ← PURE JAVA — no Spring, no JPA                                                                                
│   ├── exception/                      (domain-specific business exceptions)                                                                                                         
│   │   ├── CannotDeleteActiveUserException.java                                                                             
│   │   ├── EmailAlreadyExistsException.java                                                                                 
│   │   ├── InvalidRoleAssignmentException.java                                                                              
│   │   ├── PhoneAlreadyExistsException.java                                                                                 
│   │   ├── RulesViolationException.java                                                                                     
│   │   ├── UserAlreadyExistsException.java                                                                                  
│   │   └── UserNotFoundException.java                                                                                       
│   ├── model/                          (JPA entities or plain domain objects)                                                                                                             
│   │   ├── Role.java                   (enum)                                                                                                        
│   │   ├── User.java                   (domain model)                                                                                                        
│   │   ├── UserRole.java               (domain model)                                                                                                  
│   │   ├── UserSearchCriteria.java     (value object for queries)                                                                                          
│   │   └── UserStatus.java             (enum)                                        
│   ├── port/                           (hexagonal architecture ports)                                                                                
│   │   ├── in/                         ← USE-CASE PORTS (what the outside world calls)                                                            
│   │   │   ├── package-info.java                                                                                            
│   │   │   ├── UserCommandUseCases.java   (write operations: register, update, delete, assign role)                                                                              
│   │   │   └── UserQueryUseCases.java     (read operations: findById, search, exists)                                               
│   │   └── out/                        ← OUTPUT PORTS (what the domain needs from infra)                                                                                                         
│   │       ├── UserRepositoryPort.java    (CRUD + custom queries)                                                                                      
│   │       └── UserRoleRepositoryPort.java  (role assignments)                                                                                  
│   └── service/                           (pure domain services)                                                                                                         
│       └── UserDomainValidator.java       (business rules validation)
└── infrastructure/                      ← SPRING & JPA IMPLEMENTATIONS                                                                                                      
├── api/                              (public API adapters)                                                                                                             
│   └── UserApiAdapter.java           (implements UserApi, calls UserService)                                                                                              
├── persistence/                      (database layer)                                                                                                         
│   ├── adapter/                      (implements domain output ports)                                                                                                         
│   │   ├── UserRepositoryAdapter.java                                                                                   
│   │   └── UserRoleRepositoryAdapter.java                                                                               
│   ├── entity/                       (JPA entities — separate from domain)                                                                                                          
│   │   ├── UserEntity.java                                                                                              
│   │   └── UserRoleEntity.java                                                                                          
│   └── repository/                   (Spring Data JPA interfaces)                                                                                                      
│       ├── UserJpaRepository.java                                                                                       
│       └── UserRoleJpaRepository.java                                                                            
├── security/                         (Spring Security config)                                                                            
│   └── SecurityConfig.java                                                                             
└── web/                              (internal REST controllers)                                                                                                                
├── dto/                          (request/response DTOs — NOT shared with other modules)                                                                                                             
│   ├── request/                                                                                                     
│   │   ├── AssignRoleRequest.java                                                                                   
│   │   ├── CreateUserRequest.java                                                                                   
│   │   ├── RevokeRoleRequest.java                                                                                   
│   │   ├── UpdateUserRequest.java                                                                                   
│   │   ├── UpdateUserStatusRequest.java                                                                             
│   │   └── UserSearchRequest.java                                                                                   
│   └── response/                                                                                                    
│       ├── UserDetailResponse.java                                                                                  
│       └── UserSummaryResponse.java                                                                                 
├── exception/                                                                                                       
│   └── UserExceptionHandler.java  (@ControllerAdvice)                                                                                    
├── mapper/                                                                                                          
│   └── UserResponseMapper.java    (converts domain → web response DTOs)                                                                                      
├── InternalUserController.java    (admin/internal endpoints)                                                                                      
└── UserController.java            (public user endpoints)
```

### 2.1 The Port Contracts (Interface First)

Define every use case as a Java interface in `domain/port/in/`. Services implement it.
Controllers depend only on the interface, never the service directly.

```java
// domain/port/in/RegisterUserUseCase.java
public interface RegisterUserUseCase {

    /**
     * UC-D01 / UC-D02 — Register a new donor or staff account.
     *
     * @param command all required registration data
     * @return the created user's id and email-verification status
     * @throws EmailAlreadyExistsException if the email is already registered
     * @throws PhoneAlreadyExistsException if the phone is already registered
     */
    RegistrationResult register(RegisterCommand command);

    record RegisterCommand(
        @NotBlank String email,
        @NotBlank String phone,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String displayName,
        @NotNull  Role   role
    ) {}

    record RegistrationResult(Long userId, String email, boolean emailVerificationSent) {}
}
```

```java
// domain/port/out/UserRepository.java
public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    User save(User user);
    void deleteById(Long id);
}
```

```java
// application/RegisterUserService.java
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepository            userRepository;
    private final PasswordEncoder           passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RegistrationResult register(RegisterCommand cmd) {
        if (userRepository.existsByEmail(cmd.email()))
            throw new EmailAlreadyExistsException(cmd.email());
        if (userRepository.existsByPhone(cmd.phone()))
            throw new PhoneAlreadyExistsException(cmd.phone());

        var user = new User(
            cmd.email(), cmd.phone(),
            passwordEncoder.encode(cmd.password()),
            cmd.displayName(), UserStatus.PENDING_VERIFICATION
        );
        var saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(saved.getId(), saved.getEmail()));
        return new RegistrationResult(saved.getId(), saved.getEmail(), true);
    }
}
```

```java
// infrastructure/web/AuthController.java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final RegisterUserUseCase     registerUser;   // ← interface only
    private final AuthenticateUserUseCase authenticateUser;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "UC-D01 — Register a new account")
    public ApiResponse<RegisterUserUseCase.RegistrationResult> register(
            @Valid @RequestBody RegisterRequest req) {
        return ApiResponse.success(registerUser.register(req.toCommand()));
    }

    @PostMapping("/login")
    @Operation(summary = "UC-D03 — Login")
    public ApiResponse<TokenPair> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.success(authenticateUser.login(req.toCommand()));
    }
}
```

---

## 3. JSON Value Objects

All JSON columns in JPA entities are mapped to strongly-typed Java records and
serialised with Jackson. Store and retrieve UTC times only.

### 3.1 `NotificationPreferences` — stored in `DonorProfile.notificationPreferences`

```java
/**
 * Stored as a single JSON column on donor_profile.
 * All times are UTC (HH:mm, 24-hour clock).
 *
 * Implemented channels: IN_APP, EMAIL.
 * PUSH is defined in the enum for completeness but is not implemented.
 */
public record NotificationPreferences(

    NotificationFrequency frequency,        // IMMEDIATE | DAILY_DIGEST |
                                            // EMERGENCY_ONLY | DISABLED

    /** null = notifications allowed at all hours. */
    @Nullable QuietHours quietHours,

    boolean allowEmergencyNotifications,

    /** Maximum km radius for which the donor wants to be notified. */
    int maxNotificationDistanceKm

) {
    /** Quiet window — both fields in UTC HH:mm (24-hour). */
    public record QuietHours(
        @JsonFormat(pattern = "HH:mm") LocalTime start,  // e.g. "22:00"
        @JsonFormat(pattern = "HH:mm") LocalTime end     // e.g. "07:00"
    ) {
        /**
         * Returns true when 'now' (UTC) falls inside the quiet window.
         * Handles overnight ranges (start > end).
         */
        public boolean isQuietNow() {
            LocalTime now = LocalTime.now(ZoneOffset.UTC);
            return start.isAfter(end)
                ? !now.isBefore(start) || now.isBefore(end)   // overnight
                : !now.isBefore(start) && now.isBefore(end);
        }
    }
}
```

Example JSON stored in the column:

```json
{
  "frequency": "IMMEDIATE",
  "quietHours": { "start": "22:00", "end": "07:00" },
  "allowEmergencyNotifications": true,
  "maxNotificationDistanceKm": 25
}
```

**Which channels to send notifications through are determined by system configuration, not stored per donor.**

### 3.2 `OperatingHours` — stored in `BloodDonationCenter.operatingHours`

```java
/**
 * Weekly schedule plus an ordered list of closure windows.
 * Missing days = center is closed that day.
 * All times UTC HH:mm.
 *
 * The slot generator reads closedWindows to skip covered intervals at creation time.
 * The closure endpoint appends to this list and immediately blocks any already-existing
 * future slots that overlap.
 */
public record OperatingHours(
    @Nullable DaySchedule monday,
    @Nullable DaySchedule tuesday,
    @Nullable DaySchedule wednesday,
    @Nullable DaySchedule thursday,
    @Nullable DaySchedule friday,
    @Nullable DaySchedule saturday,
    @Nullable DaySchedule sunday,
    @Nullable List<ClosureWindow> closedWindows   // ordered by date
) {
    public record DaySchedule(
        @JsonFormat(pattern = "HH:mm") LocalTime open,    // e.g. "08:00"
        @JsonFormat(pattern = "HH:mm") LocalTime close    // e.g. "17:00"
    ) {}

    public record ClosureWindow(
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") @Nullable LocalTime startTime,  // ignored if allDay
        @JsonFormat(pattern = "HH:mm") @Nullable LocalTime endTime,    // ignored if allDay
        boolean allDay,
        String reason
    ) {}

    /** Returns today's schedule, or empty if closed. */
    public Optional<DaySchedule> today() {
        return forDay(DayOfWeek.from(LocalDate.now(ZoneOffset.UTC)));
    }

    public Optional<DaySchedule> forDay(DayOfWeek day) {
        return Optional.ofNullable(switch (day) {
            case MONDAY    -> monday;
            case TUESDAY   -> tuesday;
            case WEDNESDAY -> wednesday;
            case THURSDAY  -> thursday;
            case FRIDAY    -> friday;
            case SATURDAY  -> saturday;
            case SUNDAY    -> sunday;
        });
    }
}
```

Example JSON:

```json
{
  "monday":    { "open": "08:00", "close": "17:00" },
  "tuesday":   { "open": "08:00", "close": "17:00" },
  "wednesday": { "open": "08:00", "close": "17:00" },
  "thursday":  { "open": "08:00", "close": "17:00" },
  "friday":    { "open": "08:00", "close": "14:00" },
  "saturday":  null,
  "sunday":    null,
  "closedWindows": [
    { "date": "2025-08-15", "allDay": true, "reason": "National Holiday" }
  ]
}
```

### 3.3 `FeatureFlag.rules` — stored in `FeatureFlag.rules`

```java
/**
 * Optional targeting rules for gradual rollouts.
 * All fields are optional; absent = rule not applied.
 */
public record FeatureFlagRules(
    @Nullable List<String> enabledForRoles,     // e.g. ["DONOR"]
    @Nullable List<Long>   enabledForUserIds,
    @Nullable Integer      rolloutPercentage    // 0–100
) {}
```

### 3.4 `SystemConfig.configValue` — stored in `SystemConfig.configValue`

`configValue` is a free-form `JsonNode`. Well-known keys and their expected shapes:

| `configKey` | Shape |
|---|---|
| `donation.cooldown.days` | `{ "value": 56 }` |
| `matching.initial.radius.km` | `{ "value": 10 }` |
| `matching.max.radius.km` | `{ "value": 100 }` |
| `emergency.urgency.levels` | `{ "CRITICAL": 2, "URGENT": 6, "MODERATE": 24 }` (hours to respond) |
| `slot.generation.period.weeks` | `{ "value": 3 }` |
| `permanent.medication.keywords` | `{ "keywords": ["insulin", "chemo", "immunosuppressant"] }` |
| `jwt.secret` | `{ "value": "base64-encoded-secret-key" }` |
| `jwt.expiration.ms` | `{ "value": 3600000 }` (1 hour default) |
| `jwt.refresh.expiration.ms` | `{ "value": 604800000 }` (7 days default) |
| `notification.channels` | `{ "channels": ["IN_APP", "EMAIL"] }` (ordered list) |

**Note:** All business configurations can be overridden via SystemConfig at runtime without restart. JWT secret changes are picked up dynamically; existing tokens remain valid until natural expiry.

---

## 4. Full Module & API Contract Catalogue

For each module: domain responsibilities, entities owned, use cases covered, and every
REST endpoint with method, path, request body, and response shape.

---

### MODULE: `user`

**Owns:** `User`, `UserRole`, `Session`, `VerificationToken`

**Use cases:** UC-D01–05, UC-D11, UC-CS01, UC-CA01, UC-SA01, UC-SYS01–02

#### REST Endpoints

```
POST   /api/v1/auth/register
       Body:    { email, phone, password, displayName, role }
       201:     { userId, email, emailVerificationSent }

POST   /api/v1/auth/verify-email
       Body:    { token }
       200:     { message }

POST   /api/v1/auth/login
       Body:    { email, password }
       200:     { accessToken, refreshToken, expiresIn }

POST   /api/v1/auth/logout
       Header:  Authorization: Bearer <accessToken>
       200:     { message }

POST   /api/v1/auth/refresh
       Body:    { refreshToken }
       200:     { accessToken, refreshToken, expiresIn }

POST   /api/v1/auth/forgot-password
       Body:    { email }
       200:     { message }

POST   /api/v1/auth/reset-password
       Body:    { token, newPassword }
       200:     { message }

GET    /api/v1/users                        [SUPER_ADMIN]
       Query:   page, size, status, role, search
       200:     Page<UserSummary>

GET    /api/v1/users/{id}                   [SUPER_ADMIN]
       200:     UserDetail

PATCH  /api/v1/users/{id}/status            [SUPER_ADMIN]
       Body:    { status: ACTIVE|SUSPENDED|DELETED }
       200:     UserSummary

PATCH  /api/v1/users/{id}/roles             [SUPER_ADMIN]
       Body:    { role, action: ASSIGN|REVOKE }
       200:     { userId, roles }

DELETE /api/v1/users/{id}                   [SUPER_ADMIN] (hard delete / GDPR)
       200:     { message }
```

**Notes:**
- Sessions are stored with hashed tokens only; raw tokens are returned once and never stored.
- `VerificationToken` records are consumed (deleted) on use.
- Virtual Threads handle blocking I/O in authentication flows without tuning.

---

### MODULE: `donor`

**Owns:** `DonorProfile`, `HealthQuestionnaire`

**Use cases:** UC-D06–11, UC-D12–19, UC-SYS09–12

**Derived field:** `distanceKm` in emergency-response contexts is computed at the API
layer (Haversine on donor lat/lng vs center lat/lng) — **never stored**.

#### Profile Complete Flag

```java
/**
 * profileComplete = true when:
 * - HealthQuestionnaire has been submitted at least once
 * - Location (latitude/longitude) has been set
 * 
 * Blood type is NOT required for profile completion.
 */
```

#### Blood Type Lock

`DonorProfile` carries `Boolean bloodTypeVerified`. Rules:

- Setting blood type to any value other than `UNKNOWN` → `bloodTypeVerified = true`;
  the field becomes **immutable for the donor**.
- Setting blood type to `UNKNOWN` is accepted but does **not** set `bloodTypeVerified = true`.
- Any subsequent `PUT /donors/me/blood-type` request when `bloodTypeVerified = true`
  → `422 BLOOD_TYPE_ALREADY_VERIFIED`.
- Only a `SUPER_ADMIN` can override via `PATCH /api/v1/admin/donors/{id}/blood-type`
  (audit-logged, resets to new type and keeps `bloodTypeVerified = true`).
- When staff completes an appointment (`POST /appointments/{id}/complete`) and optionally
  provides `bloodType`: if the donor's type is `UNKNOWN` or `bloodTypeVerified = false`,
  the profile is updated and `bloodTypeVerified` set to `true`; if already verified,
  the provided value is **silently ignored** — the completion proceeds normally.

#### Automatic Permanent Restriction (UC-SYS11)

Evaluated every time `PUT /donors/me/health-questionnaire` is called:

- `hasChronicIllness == true` → `permanentlyRestricted = true`,
  `restrictionReason = "Chronic illness indicated in health questionnaire"`.
- `onMedication == true` AND `medicationDetails` matches any keyword in the
  `permanent.medication.keywords` system config → `permanentlyRestricted = true`,
  `restrictionReason = "Permanent medication: <matched keyword>"`.
- Otherwise, `permanentlyRestricted` remains unchanged (temporary restrictions are
  handled via eligibility cooldown only).
- The system **never automatically clears** a permanent restriction; only a
  `SUPER_ADMIN` can do so via `PATCH /api/v1/donors/{id}/restriction`.

#### Extended `HealthQuestionnaire` Fields

Add to entity and DTOs:
- `String medicationDetails` — free text, required when `onMedication = true`.
- `String medicalConditionsDetails` — optional free text for additional context.

#### REST Endpoints

```
GET    /api/v1/donors/me                    [DONOR]
       200:     DonorProfileResponse { ...profile, bloodTypeVerified,
                                       eligibilityStatus, nextEligibleDate,
                                       reliabilityScore, impactSummary,
                                       profileComplete }

PUT    /api/v1/donors/me                    [DONOR]
       Body:    { displayName, phone }
       200:     DonorProfileResponse

PUT    /api/v1/donors/me/blood-type         [DONOR]
       Body:    { bloodType: A_POSITIVE|...|UNKNOWN }
       200:     { bloodType, bloodTypeVerified }
       422:     BLOOD_TYPE_ALREADY_VERIFIED   (if bloodTypeVerified == true)

PATCH  /api/v1/admin/donors/{id}/blood-type [SUPER_ADMIN]
       Body:    { bloodType }
       200:     { bloodType, bloodTypeVerified }
       Note:    Admin override — updates type, keeps bloodTypeVerified = true. Audit-logged.

PUT    /api/v1/donors/me/location           [DONOR]
       Body:    { latitude, longitude, city, country } | { useGps: true }
       200:     { latitude, longitude, city, country }
       Note:    Sets profileComplete = true if health questionnaire also submitted.

PUT    /api/v1/donors/me/availability       [DONOR]
       Body:    { status: AVAILABLE|TEMPORARILY_UNAVAILABLE|VACATION_MODE }
       200:     { availability }

PUT    /api/v1/donors/me/notification-prefs [DONOR]
       Body:    NotificationPreferences (see §3.1)
       200:     NotificationPreferences

GET    /api/v1/donors/me/health-questionnaire  [DONOR]
       200:     HealthQuestionnaireResponse

PUT    /api/v1/donors/me/health-questionnaire  [DONOR]
       Body:    { hasChronicIllness, medicalConditionsDetails?,
                  onMedication, medicationDetails?,
                  recentSurgery, recentTravel, recentTattooOrPiercing }
       200:     HealthQuestionnaireResponse
       Note:    Triggers automatic permanent restriction evaluation (UC-SYS11).
                medicationDetails required when onMedication = true.
                First submission sets profileComplete = true if location also set.

GET    /api/v1/donors/me/eligibility        [DONOR]
       200:     { eligible, nextEligibleDate, reason }

GET    /api/v1/donors/me/impact             [DONOR]
       200:     { totalDonations, estimatedLivesSaved, milestones: [...] }

GET    /api/v1/donors/me/certificates       [DONOR]
       200:     [ { donationDate, centerId, centerName, mlCollected, certificateUrl } ]

DELETE /api/v1/donors/me                    [DONOR] (request account deletion — UC-D11)
       200:     { message, requestId }

GET    /api/v1/donors/{id}                  [CENTER_STAFF, CENTER_ADMIN, SUPER_ADMIN]
       200:     DonorDetail { ...profile, healthQuestionnaire, appointmentCount }

GET    /api/v1/donors/{id}/eligibility      [CENTER_STAFF]
       200:     { eligible, nextEligibleDate, permanentlyRestricted, restrictionReason }

PATCH  /api/v1/donors/{id}/restriction      [SUPER_ADMIN]
       Body:    { permanentlyRestricted, restrictionReason }
       200:     { donorId, permanentlyRestricted }

PATCH  /api/v1/donors/{id}/flag             [SUPER_ADMIN]
       Body:    { flaggedForManualReview: false }
       200:     { donorId, flaggedForManualReview }
```

**Reliability scoring rules (UC-SYS12):**
- Score range: 0–100.
- Donation completed → score increases.
- No-show → score decreases.
- `consecutiveEmergencyDeclines` increments on each decline while `AVAILABLE`.
- 3 consecutive declines with no accept in between → `flaggedForManualReview = true`.
- Accepting any emergency → `consecutiveEmergencyDeclines = 0`, `lastAcceptAt = now()`.
- Declining alone carries no direct score penalty.
- Super-admin clears the flag via `PATCH /donors/{id}/flag` (persists to `AuditLog`).

---

### MODULE: `center`

**Owns:** `BloodDonationCenter`, `CenterStatus`, `FacilityType`, `Slot`,
`CenterStaffProfile`, `CenterAdminProfile`

**Use cases:** UC-D24–25, UC-CA02–10, UC-CS06, UC-SA04, UC-SYS13

#### Capacity Model

`BloodDonationCenter` has two capacity fields:

- `totalCapacity` — maximum total appointments (regular + emergency) per slot.
- `maxRegular` — maximum regular (non-emergency) appointments per slot.

Emergency appointments may fill any capacity not used by regular bookings.
`Slot` tracks four counters:

- `maxBookings` — mirrors `totalCapacity`.
- `maxRegularBookings` — mirrors `maxRegular`.
- `bookedCount` — total bookings (regular + emergency) currently on the slot.
- `regularBookedCount` — regular bookings only.

When `isBlocked` is set to `true`, the system sends a notification to any donors who
have existing appointments on that slot, instructing them to reschedule.

#### Slot Generation

`BloodDonationCenter.slotPeriod` (minutes) drives automatic slot creation.

**Recurring job:** Runs **once every 21 days at 00:00 UTC**. Generates slots covering
the **next 3 weeks** (today + 21 days inclusive). For each center, for each open day
in `operatingHours`, the open interval is divided into `slotPeriod` blocks. For each
block, one `Slot` is created with:
- `maxBookings = totalCapacity`, `maxRegularBookings = maxRegular`.
- `bookedCount = 0`, `regularBookedCount = 0`, `isBlocked = false`.

If a block overlaps an entry in `operatingHours.closedWindows`, the slot is **not
created**. Existing slots in the target window are skipped (idempotent).

**Startup job:** Runs once on service startup. Checks for and creates all missing slots
from today through the next 3 weeks, applying the same rules as the recurring job.

#### Slot Visibility Rules

| Actor | Visible range |
|---|---|
| `CENTER_STAFF` / `CENTER_ADMIN` | 2 months ago → 1 month ahead |
| `DONOR` | From the **next full hour** (UTC) onwards only |

#### Special Closures

Closures are managed through `operatingHours.closedWindows`. There is **no separate
`Closure` table**.

`POST /api/v1/centers/{id}/closures` accepts:

```json
{
  "date":      "2025-08-15",
  "startTime": "08:00",     // UTC HH:mm — ignored when allDay = true
  "endTime":   "17:00",     // UTC HH:mm — ignored when allDay = true
  "allDay":    false,
  "reason":    "National Holiday"
}
```

Behaviour:
1. Appends a `ClosureWindow` record to `operatingHours.closedWindows` and persists.
2. Immediately blocks all existing future `Slot` rows that overlap the window
   (`isBlocked = true`).
3. The recurring slot generator will automatically skip the closed window on future runs.
4. Records an `AuditLog` entry (`action = CENTER_CLOSURE`, `entityType = Slot`,
   `entityId` = first affected slot id, `newValue` = closure JSON).
5. Sends rescheduling notifications to donors with appointments on blocked slots.

Response: `{ blockedSlotCount, date, reason }`.

#### REST Endpoints

```
POST   /api/v1/centers                      [CENTER_ADMIN]
       Body:    { name, address, city, country, postalCode, phone, email,
                  latitude, longitude, facilityType, operatingHours,
                  totalCapacity, maxRegular, slotPeriod }
       201:     CenterResponse (status: PENDING_APPROVAL)

GET    /api/v1/centers                      [PUBLIC]
       Query:   lat, lng, radiusKm, city, isActive, page, size
       200:     Page<CenterSummary> (includes derived `distanceKm`)

GET    /api/v1/centers/{id}                 [PUBLIC]
       200:     CenterDetail { ...center, operatingHours, isOperatingNow, slotPeriod }

PUT    /api/v1/centers/{id}                 [CENTER_ADMIN (own center)]
       Body:    { name, address, phone, email, operatingHours,
                  totalCapacity, maxRegular, slotPeriod }
       200:     CenterResponse

PATCH  /api/v1/centers/{id}/status          [SUPER_ADMIN]
       Body:    { status: ACTIVE|SUSPENDED|CLOSED }
       200:     CenterResponse

POST   /api/v1/centers/{id}/closures        [CENTER_ADMIN]
       Body:    { date, startTime?, endTime?, allDay, reason }
       200:     { blockedSlotCount, date, reason }
       Note:    Appends to operatingHours.closedWindows, blocks existing future slots,
                notifies affected donors, writes AuditLog entry.

GET    /api/v1/centers/{id}/slots           [DONOR, CENTER_STAFF, CENTER_ADMIN]
       Query:   date, slotType
       200:     [ SlotResponse { id, startTime, endTime,
                  maxBookings, maxRegularBookings,
                  bookedCount, regularBookedCount, isBlocked } ]
       Note:    DONOR sees only future slots (from next full UTC hour onwards).
                STAFF/ADMIN see from 2 months ago to 1 month ahead.

PATCH  /api/v1/centers/{id}/slots/{slotId}/block  [CENTER_ADMIN]
       Body:    { isBlocked }
       200:     SlotResponse
       Note:    Setting isBlocked = true notifies donors with existing appointments
                on this slot to reschedule.

GET    /api/v1/centers/{id}/staff           [CENTER_ADMIN]
       200:     [ StaffSummary ]

POST   /api/v1/centers/{id}/staff           [CENTER_ADMIN]
       Body:    { userId }
       201:     StaffSummary

DELETE /api/v1/centers/{id}/staff/{userId}  [CENTER_ADMIN]
       200:     { message }

GET    /api/v1/centers/{id}/staff/activity  [CENTER_ADMIN]
       Query:   page, size, from, to
       200:     Page<StaffActivityEntry>

GET    /api/v1/centers/{id}/schedule        [CENTER_ADMIN, CENTER_STAFF]
       Query:   date
       200:     DailySchedule { slots: [ { slot, appointments: [ AppointmentSummary ] } ] }

GET    /api/v1/centers/{id}/analytics       [CENTER_ADMIN]
       Query:   from, to
       200:     CenterAnalytics { peakHours, bloodTypeTrends, totalDonations, noShowRate }

GET    /api/v1/centers/{id}/reports         [CENTER_ADMIN]
       Query:   from, to, format: JSON|CSV
       200:     CenterReport | CSV file

GET    /api/v1/centers/pending              [SUPER_ADMIN]
       200:     Page<CenterSummary> (status: PENDING_APPROVAL)

PATCH  /api/v1/centers/{id}/approve         [SUPER_ADMIN]
       Body:    { approved: true|false, reason }
       200:     CenterResponse
```

---

### MODULE: `emergency`

**Owns:** `Emergency`, `EmergencyUrgency`, `EmergencyStatus`, `EmergencyResponse`,
`MatchResult`

**Use cases:** UC-D20–23, UC-CS02–05, UC-SYS03–07, UC-SYS14

**Match log:** `MatchResult` is persisted to support analytics and "why wasn't donor X
notified" debugging. Fields per class diagram: `id`, `emergencyId`, `centerId`,
`donorId`, `radius`, `bloodType`, `escalationLevel`, `createdAt`.
Linked only to `Emergency`.

#### Blood Type Compatibility

`BloodType.canDonateTo(BloodType recipient)` lives in `shared/domain/BloodType.java`:

| Donor | Can donate to |
|---|---|
| O- | O- |
| O+ | O-, O+ |
| A- | A-, A+, AB-, AB+ |
| A+ | A+, AB+ |
| B- | B-, B+, AB-, AB+ |
| B+ | B+, AB+ |
| AB- | AB-, AB+ |
| AB+ | AB+ |
| UNKNOWN | never matched |

**O- and O+ donors are never notified for non-O emergencies** — this preserves rare
blood stock. Donors with `bloodType = UNKNOWN` are never matched.

#### Matching Priority

Within eligible donors, rank by:
1. `bloodTypeVerified = true` ranks above unverified donors.
2. Within each verification tier: higher `reliabilityScore` first.
3. Within equal reliability: closer distance first.

#### Emergency Acceptance Flow (UC-D20–23)

1. Donor receives a Kafka-delivered `EMERGENCY_ALERT` notification (IN_APP / EMAIL per
   preferences).
2. Donor views details via `GET /api/v1/emergencies/{id}` — response includes
   `distanceKm` (derived), center info, blood type, urgency.
3. Donor calls `POST /api/v1/emergencies/{id}/respond`:
    - **DECLINED** → `EmergencyResponse` saved as `DECLINED`;
      `consecutiveEmergencyDeclines` incremented.
    - **WILLING** → `EmergencyResponse` saved as `WILLING`; response body contains the
      list of available slots at the center within `[now, neededBy]` (unblocked, not full).
4. Donor selects a slot and calls the same endpoint again with
   `{ responseType: CONFIRMED, slotId }`.
5. The service atomically creates an `Appointment` (type `EMERGENCY`, status `CONFIRMED`)
   and updates `EmergencyResponse.responseType` to `CONVERTED_TO_APPOINTMENT`.
6. A Kafka event is published; the notification service sends a booking confirmation.

#### REST Endpoints

```
POST   /api/v1/emergencies                  [CENTER_STAFF]
       Body:    { centerId, bloodType, unitsNeeded, urgency, contactPhone, neededBy }
       201:     EmergencyDetail
       → publishes emergency.created to Kafka → async matching engine

GET    /api/v1/emergencies                  [CENTER_STAFF, CENTER_ADMIN, SUPER_ADMIN]
       Query:   centerId, status, urgency, bloodType, page, size
       200:     Page<EmergencySummary>

GET    /api/v1/emergencies/{id}             [DONOR, CENTER_STAFF, CENTER_ADMIN, SUPER_ADMIN]
       200:     EmergencyDetail { ...emergency, distanceKm (derived for DONOR),
                                  matchedDonorCount, responseStats }

PATCH  /api/v1/emergencies/{id}/status      [CENTER_STAFF]
       Body:    { action: ESCALATE|EXTEND|CANCEL, deadlineExtension? }
       200:     EmergencyDetail

POST   /api/v1/emergencies/{id}/resolve     [CENTER_STAFF]
       Body:    { notes }
       200:     EmergencyDetail

GET    /api/v1/emergencies/{id}/matches     [CENTER_ADMIN, SUPER_ADMIN]
       200:     [ MatchResult ]

GET    /api/v1/emergencies/history          [CENTER_STAFF, CENTER_ADMIN]
       Query:   centerId, from, to, page, size
       200:     Page<EmergencySummary>

POST   /api/v1/emergencies/{id}/respond     [DONOR]
       ─── Step A: express intent ──────────────────────────────────────────────
       Body:    { responseType: WILLING | DECLINED }
       200 (WILLING):    { emergencyId, responseType: WILLING,
                           availableSlots: [ SlotSummary ] }
                         SlotSummary = { slotId, startTime, endTime, availableCount }
                         filtered to slots within [now, neededBy] that are unblocked
                         and not full (bookedCount < maxBookings)
       200 (DECLINED):   { emergencyId, responseType: DECLINED }
       ─── Step B: confirm slot (WILLING only) ─────────────────────────────────
       Body:    { responseType: CONFIRMED, slotId: Long }
       201:     { emergencyId, responseType: CONVERTED_TO_APPOINTMENT,
                  appointmentId, qrCode }
       Note:    Step B atomically creates the Appointment and flips
                EmergencyResponse → CONVERTED_TO_APPOINTMENT.
                CONFIRMED without a prior WILLING → 422.

GET    /api/v1/donors/me/emergencies        [DONOR]
       200:     [ EmergencyNotificationSummary { ...emergency, centerName,
                  distanceKm (derived), responseType, respondedAt } ]
```

**Matching engine (UC-SYS03–05):**
1. On `Emergency` created → publish `emergency.created` to Kafka.
2. `MatchingService` (async Kafka consumer) filters eligible donors:
    - Blood type compatible using `canDonateTo()`.
    - O-/O+ donors excluded for non-O emergencies.
    - `AVAILABLE`, not permanently restricted.
    - Within `matchRadius` km.
    - If match results are not enough and the Matching extends the matchRadius by 10 km, the first match results must not get notified again, so we Always filter out the donors with matchResult on the same emergency.
3. Rank using the priority order above.
4. Persist one `MatchResult` row per donor per matching run.
5. Publish `notification.dispatch` events to Kafka; the notification service applies
   `quietHours` and `notificationFrequency` before delivery.
6. If insufficient responses within the urgency window → escalate radius, re-run.
7. `@Scheduled` task (every 5 min) expires `OPEN` emergencies past their `neededBy` deadline.
   The urgency window is set by superadmin as system config.
---

### MODULE: `appointment`

**Owns:** `Appointment`, `AppointmentStatus`, `AppointmentType`, `HealthScreening`

**Use cases:** UC-D26–30, UC-CS07–12

#### QR Code Specification

```java
/**
 * QR Code Generation for check-in:
 * 
 * Format: direct URL
 * Content: { "appointmentId": Long, "donorId": Long, "centerId": Long}
 * 
 * 
 * Scanning the QR code opens: https://{domain}/checkin?apponitmentId=apntId&donorId=donorId&centerId=centerId
 * 
 * Staff scans → backend validates staff belongs to same center → fetches appointment and donor details
 * Returns: AppointmentDetail + DonorProfile (name, blood type, eligibility)
 */
```

#### Booking Constraints

- `Slot.isAvailable()` for **regular** appointments:
  `!isBlocked AND bookedCount < maxBookings AND regularBookedCount < maxRegularBookings`.
- For **emergency** appointments:
  `!isBlocked AND bookedCount < maxBookings` (regular counter not checked or incremented).
- On successful regular booking: increment `bookedCount` and `regularBookedCount`.
- On successful emergency booking: increment `bookedCount` only.
- On cancellation of a non-`CANCELLED`/`NO_SHOW` appointment: decrement the
  corresponding counter(s).

#### REST Endpoints

```
POST   /api/v1/appointments                 [DONOR]
       Body:    { centerId, slotId, appointmentType: REGULAR|EMERGENCY, emergencyId? }
       201:     AppointmentResponse { id, qrCode, status, slot, center }
       Note:    EMERGENCY type appointments are created internally by the emergency
                respond flow (Step B). Donors must not call this directly for emergencies.
                qrCode is generated as a short-lived JWT (15 min) encoding appointmentId + donorId.

GET    /api/v1/appointments                 [DONOR — own; CENTER_STAFF — assigned center]
       Query:   status, from, to, page, size
       200:     Page<AppointmentSummary>

GET    /api/v1/appointments/{id}            [DONOR (own), CENTER_STAFF, CENTER_ADMIN]
       200:     AppointmentDetail

PATCH  /api/v1/appointments/{id}/reschedule [DONOR]
       Body:    { newSlotId }
       200:     AppointmentResponse

DELETE /api/v1/appointments/{id}            [DONOR]
       Body:    { cancellationReason }
       200:     { message }
       → decrements slot counters

POST   /api/v1/appointments/checkin         [CENTER_STAFF]
       Body:    { qrCode } | { appointmentId }
       200:     CheckInResult { appointment, donorProfile, eligibility }
       Note:    Scanning QR code validates the JWT and returns appointment + donor details.

POST   /api/v1/appointments/{id}/no-show    [CENTER_STAFF]
       200:     AppointmentResponse
       → reliability score decreases
       → publishes Kafka event → notification service messages donor

POST   /api/v1/appointments/{id}/screening  [CENTER_STAFF]
       Body:    { temperatureCelsius, hemoglobinGdL, bloodPressure, pulse,
                  medicalCheckPassed, notes }
       201:     HealthScreeningResponse

POST   /api/v1/appointments/{id}/complete   [CENTER_STAFF]
       Body:    { mlCollected, notes, bloodType? }
       200:     AppointmentResponse
       → sets DonorProfile.eligibleFromDate = today + cooldown (UC-SYS09)
       → reliability score increases
       → if donor bloodType = UNKNOWN or bloodTypeVerified = false: update profile
         and set bloodTypeVerified = true; if already verified: ignore bloodType silently
       → Appointment.bloodType always stored as submitted (or current donor type if omitted)
       → publishes Kafka event → notification service sends certificate/thank-you

GET    /api/v1/appointments/{id}/screening  [CENTER_STAFF]
       200:     HealthScreeningResponse

POST   /api/v1/appointments/{id}/message    [CENTER_STAFF]  (UC-CS12)
       Body:    { message }
       201:     { notificationId }
       → publishes Kafka event → notification service delivers STAFF_MESSAGE to donor

GET    /api/v1/donors/me/appointments       [DONOR]
       Query:   status, page, size
       200:     Page<AppointmentSummary>

GET    /api/v1/donors/me/donations          [DONOR]
       200:     Page<DonationHistoryEntry { date, center, mlCollected, certificateUrl }>

GET    /api/v1/staff/me/queue               [CENTER_STAFF]
       200:     [ AppointmentTask { appointment, donor, slotTime, status } ]
```

---

### MODULE: `analytics`

**Owns:** `AuditLog`, `SystemMetric`, `DemandForecast`

**Use cases:** UC-SA10–13, UC-SYS15–17

```
GET    /api/v1/admin/dashboard              [SUPER_ADMIN]
       200:     SystemDashboard { activeEmergencies, totalDonors, responseRate30d,
                                  topCenters, recentAlerts }

GET    /api/v1/admin/audit-logs             [SUPER_ADMIN]
       Query:   userId, entityType, from, to, page, size
       200:     Page<AuditLogEntry>

GET    /api/v1/admin/audit-logs/export      [SUPER_ADMIN]
       Query:   from, to, format: CSV|JSON
       200:     file download

GET    /api/v1/admin/reports                [SUPER_ADMIN]
       Query:   from, to, type: PLATFORM|REGION|BLOOD_TYPE
       200:     PlatformReport

GET    /api/v1/admin/metrics/system         [SUPER_ADMIN]
       200:     SystemHealth { services, errorRates, apiUsage, dbPoolStats }

GET    /api/v1/admin/forecasts              [SUPER_ADMIN, CENTER_ADMIN]
       Query:   region, bloodType, from, to
       200:     [ DemandForecast ]
```

**Audit log:** Every `@Service` method that mutates state must publish an `AuditLogEvent`
carrying `userId`, `action`, `entityType`, `entityId`, `oldValue`, `newValue`.
`AuditLogService` persists it. No direct `AuditLog` creation in controllers.

---

### MODULE: `system`

**Owns:** `SystemConfig`, `FeatureFlag`, `DataDeletionRequest`, `DeletionStatus`

**Use cases:** UC-SA06–09, UC-D11

```
GET    /api/v1/admin/config                 [SUPER_ADMIN]
       200:     [ SystemConfigEntry { key, value, description, updatedAt } ]

PUT    /api/v1/admin/config/{key}           [SUPER_ADMIN]
       Body:    { value, description }
       200:     SystemConfigEntry

GET    /api/v1/admin/feature-flags          [SUPER_ADMIN]
       200:     [ FeatureFlag ]

PUT    /api/v1/admin/feature-flags/{name}   [SUPER_ADMIN]
       Body:    { enabled, rules }
       200:     FeatureFlag

GET    /api/v1/admin/deletion-requests      [SUPER_ADMIN]
       Query:   status, page, size
       200:     Page<DeletionRequest>

POST   /api/v1/admin/deletion-requests/{id}/process  [SUPER_ADMIN]
       Body:    { approved: true|false, reason }
       200:     DeletionRequest
       → if approved: anonymise User fields, hard-delete DonorProfile & HealthQuestionnaire,
         set User.status = DELETED, User.deletedAt = now()
```

---

## 5. Cross-Cutting Concerns

### 5.1 Security

- **Spring Security** with stateless JWT. The `Session` entity tracks active tokens for
  revocation: `Session.accessTokenHash` = SHA-256 of the raw JWT. On every request the
  filter hashes the incoming token and looks up the `Session` row.
- Roles map directly to `@PreAuthorize("hasRole('DONOR')")`, etc.
- `CENTER_STAFF` and `CENTER_ADMIN` carry an implicit `centerId` constraint — they can
  only act on resources belonging to their assigned center.
- The notification service validates JWTs using the same shared secret / public key as
  the main backend (no inter-service HTTP call needed for token validation).

**JWT Configuration:**

```java
/**
 * JWT Configuration:
 * - Secret key loaded from env var JWT_SECRET (base64, min 256 bits)
 * - Can be overridden via SystemConfig with key "jwt.secret"
 * - When SystemConfig changes, a @EventListener refreshes the key dynamically
 * - Expiration: jwt.expiration.ms (default 3600000 = 1 hour)
 * - Refresh token expiration: jwt.refresh.expiration.ms (default 604800000 = 7 days)
 * 
 * Key rotation process:
 * 1. SUPER_ADMIN adds new secret to SystemConfig (jwt.secret.v2)
 * 2. System validates new secret and applies it to new tokens
 * 3. Old tokens remain valid until their natural expiry
 * 4. After 7 days, old secret can be removed from SystemConfig
 */
```

Environment variables for initial setup:

```
JWT_SECRET=<base64-encoded-secret-min-256-bits>
```

### 5.2 Super-Admin Auto-Seeding

On application start, `QatraApplication` runs a `@Bean ApplicationRunner` that
checks whether any `SYSTEM_ADMIN` user exists. If not, it creates one from environment
variables and logs the generated credentials at `WARN` level. This runs **exactly once**
— subsequent restarts skip creation if the admin already exists.

```java
@Bean
ApplicationRunner seedSuperAdmin(SeedSuperAdminUseCase seeder) {
    return args -> seeder.seedIfAbsent();
}
```

Environment variables read at startup:

```
SUPER_ADMIN_EMAIL     (required)
SUPER_ADMIN_PHONE     (required)
SUPER_ADMIN_PASSWORD  (required, min 12 chars)
```

`SeedSuperAdminUseCase` lives inside the `user` module and is **not exposed as an HTTP
endpoint**.

### 5.3 Virtual Threads

```properties
# application.properties
spring.threads.virtual.enabled=true
```

Spring Boot 4 enables Tomcat virtual threads automatically when this flag is set. No
further configuration needed.

### 5.4 Spring Modulith Verification

```java
// One per module
@ApplicationModuleTest
class UserModuleTest {
    @Test
    void verifyModuleStructure(ApplicationModules modules) {
        modules.verify();
    }
}
```

Inter-module calls must go only through each module's `api` package. The verification
test fails if any `domain` or `application` package is imported by another module.

### 5.5 Distance Computation

Distance is **always derived at query time** — never stored.

```java
// shared/domain/GeoUtils.java
public final class GeoUtils {
    private GeoUtils() {}

    /** Haversine formula — returns kilometres. */
    public static double distanceKm(double lat1, double lon1,
                                    double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1))
                 * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
```

Response DTOs that include `distanceKm` compute it in the mapper, never from a column.

---

## 6. Notification Service (Standalone Microservice)

The notification service is a **separate Spring Boot application** that consumes Kafka
events and delivers messages via two channels: `IN_APP` (WebSocket) and `EMAIL`. The
`PUSH` channel enum value exists for completeness but is **not implemented**.

The notification service owns all `Notification` entity persistence and exposes its own
REST API. The main backend holds no notification records.

### 6.1 Kafka Topics (published by main backend)

| Topic | Key payload fields | Purpose |
|---|---|---|
| `notification.dispatch` | `userId, type, title, body, data, correlationId, occurredAt` | Generic dispatch. Channels determined by system config. |
| `emergency.created` | `emergencyId, matchedDonorIds[], correlationId, occurredAt` | Triggers emergency alert loop |
| `appointment.reminder` | `appointmentId, donorId, slotTime, correlationId, occurredAt` | Pre-appointment reminder |
| `eligibility.restored` | `donorId, eligibleFromDate, correlationId, occurredAt` | Eligibility restored nudge |

All events carry `correlationId` (UUID) for idempotency dedup and `occurredAt`
(UTC ISO-8601 instant).

### 6.2 Channel Strategy (`EMAIL` and `IN_APP` only)

The channels to use for each notification type are read from `SystemConfig` key `notification.channels`:

```json
{ "channels": ["IN_APP", "EMAIL"] }
```

**Email Channel with Two Adapters (SendGrid primary, Gmail fallback):**

```java
// channel/NotificationChannel.java  (Strategy interface)
public interface NotificationChannel {
    NotificationChannelType type();

    /**
     * Deliver the notification.
     * @throws NotificationDeliveryException on unrecoverable failure → triggers retry.
     */
    void deliver(NotificationPayload payload);
}

// channel/SendGridEmailChannel.java (Primary)
@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "sendgrid", matchIfMissing = true)
public class SendGridEmailChannel implements NotificationChannel {
    @Override public NotificationChannelType type() { return EMAIL; }

    @Override
    public void deliver(NotificationPayload payload) {
        // Send via SendGrid API
        // Uses SENDGRID_API_KEY environment variable
    }
}

// channel/GmailSmtpEmailChannel.java (Fallback)
@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "gmail")
public class GmailSmtpEmailChannel implements NotificationChannel {
    @Override public NotificationChannelType type() { return EMAIL; }

    @Override
    public void deliver(NotificationPayload payload) {
        // Send via JavaMailSender (Gmail SMTP)
        // Uses spring.mail.* configuration
    }
}
```

**Configuration in `application.yml`:**

```yaml
# For SendGrid (primary - default)
email:
  channel:
    provider: sendgrid

sendgrid:
  api-key: ${SENDGRID_API_KEY}

# For Gmail (fallback - uncomment to use)
# email:
#   channel:
#     provider: gmail
# spring:
#   mail:
#     host: smtp.gmail.com
#     port: 587
#     username: ${GMAIL_USERNAME}
#     password: ${GMAIL_APP_PASSWORD}
#     properties:
#       mail.smtp.auth: true
#       mail.smtp.starttls.enable: true
```

**In-App Channel:**

```java
// channel/InAppNotificationChannel.java
@Component
public class InAppNotificationChannel implements NotificationChannel {

    private final SimpMessagingTemplate websocket;
    private final NotificationRepository notificationRepository;

    @Override public NotificationChannelType type() { return IN_APP; }

    @Override
    public void deliver(NotificationPayload payload) {
        // 1. Persist the notification record (owned by this service)
        var notification = notificationRepository.save(
            new Notification(
                payload.userId(), payload.type(),
                payload.title(), payload.body(),
                payload.data(), NotificationStatus.SENT
            )
        );
        // 2. Push to authenticated WebSocket user queue
        websocket.convertAndSendToUser(
            payload.userId().toString(),
            "/queue/notifications",
            notification
        );
    }
}
```

`NotificationDispatchService` reads the configured channels from `SystemConfig` (key `notification.channels`) and calls `deliver()` on each matching implementation in order. `quietHours` and `notificationFrequency` are checked before iterating.

### 6.3 WebSocket Configuration

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notifications")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}
```

Clients subscribe to `/user/queue/notifications` after authenticating the WebSocket
handshake with the same JWT used for REST calls.

### 6.4 Retry & Delivery Guarantees

- Kafka consumer group: `notification-service`.
- Delivery is **at-least-once**; idempotency is enforced by checking whether a
  `Notification` record with the same `correlationId` is already `SENT` before
  attempting delivery.
- Failed deliveries retry up to 3 times with exponential backoff: 2 s → 4 s → 8 s.
- After 3 failures the event is forwarded to dead-letter topic
  `notification.dispatch.dlq` for manual inspection.

### 6.5 Notification REST API (exposed by notification service)

JWT validation uses the same shared secret / public key as the main backend.

```
GET    /api/v1/notifications                [DONOR, CENTER_STAFF]
       Query:   type, read, page, size
       200:     Page<NotificationResponse>

PATCH  /api/v1/notifications/{id}/read      [DONOR, CENTER_STAFF]
       200:     { id, readAt }

PATCH  /api/v1/notifications/read-all       [DONOR, CENTER_STAFF]
       200:     { markedCount }

GET    /api/v1/notifications/unread-count   [DONOR, CENTER_STAFF]
       200:     { count }
```

---

## 7. Shared Contracts

### 7.1 API Envelope

```java
// shared/web/ApiResponse.java
public record ApiResponse<T>(
    boolean success,
    T data,
    String message,
    Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now());
    }
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, Instant.now());
    }
}
```

### 7.2 Pagination

```java
// shared/domain/PageResult.java
public record PageResult<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
```

### 7.3 Error Responses

All domain exceptions extend `BaseException`, which carries an `httpStatus` and a `data` map.
The `GlobalExceptionHandler` handles them with a single method. Module exceptions extend
the shared types directly — no intermediate base classes.

```java
// shared/exception/BaseException.java
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;
    private final Map<String, Object> data;

    public BaseException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = new HashMap<>();
    }

    public BaseException addData(String key, Object value) {
        data.put(key, value);
        return this;
    }
    // getters …
}
```

| Exception | HTTP Status | Notes |
|---|---|---|
| `NotFoundException` | 404 | Resource does not exist |
| `ConflictException` | 409 | Duplicate or state conflict |
| `ValidationException` | 422 | Semantic validation failure |
| `@Valid` failure (`MethodArgumentNotValidException`) | 400 | Handled by Spring |
| `AccessDeniedException` / `ForbiddenException` | 403 | Handled by Spring Security |
| `AuthenticationException` | 401 | Handled by Spring Security |
| Uncaught `RuntimeException` | 500 | Logged, generic message |

#### Planned / Future Exceptions

These types are defined in `shared/exception/` for use when the corresponding feature is built:

| Exception | HTTP Status | When Needed |
|---|---|---|
| `LockedException` | 423 | Resource locked or being modified |
| `TooManyRequestsException` | 429 | Rate limiting |
| `PreconditionFailedException` | 412 | ETag/version mismatch |
| `DataIntegrityException` | 409/422 | Database constraint violation |
| `NotImplementedException` | 501 | Feature not yet available |
| `ExternalServiceException` | 502 | Third-party API failure |

```java
// shared/exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBase(BaseException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
        return ResponseEntity.status(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getData(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
            .collect(toMap(FieldError::getField, FieldError::getDefaultMessage));
        return new ApiResponse<>(false, errors, "Validation failed", Instant.now());
    }
}
```

---

## 8. Automated / Scheduled Behaviour

| Job | Trigger (UTC) | Module | UC | Details |
|---|---|---|---|---|
| Generate slots for next 3 weeks | Every 21 days at 00:00 + once on startup | `center` | UC-SYS13 | Creates slots for centers based on operatingHours |
| Expire stale emergencies past deadline | Every 5 min | `emergency` | UC-SYS14 | Closes OPEN emergencies past neededBy |
| Auto-restore donor eligibility | Every 1 hour | `donor` | UC-SYS10 | Sets eligibleFromDate = now() when cooldown passes |
| Send eligibility reminders | Daily 09:00 → Kafka | `donor` | UC-SYS08 | Notifies donors when eligible to donate again |
| Send appointment reminders | Daily 08:00 → Kafka | `appointment` | UC-D30 | **Checks appointments scheduled for today only.** Sends one reminder per donor with appointment time and center name. Does NOT send for past dates or future dates beyond today. |
| Escalate matching radius | Driven by urgency window | `emergency` | UC-SYS05 | Expands radius if insufficient donor responses |
| Batch analytics aggregation | Nightly 02:00 | `analytics` | UC-SYS15 | Aggregates metrics for dashboard |
| Demand forecast generation | Weekly Sunday 03:00 | `analytics` | UC-SYS16 | Forecasts blood demand by region/type |

---

## 9. Testing Requirements

Every backend module must have:

1. **Unit tests** — all `domain/` business logic with no Spring context.
2. **Slice tests** — `@WebMvcTest` per controller with the use-case port mocked.
3. **Integration tests** — `@SpringBootTest` + H2 for the happy path of each use case.
4. **Modulith verification** — `modules.verify()` must pass in CI (see §5.4).

The notification service has its own test suite: unit tests per channel strategy
implementation and a Kafka consumer integration test using an embedded Kafka broker.

---

## 10. `pom.xml` Key Dependencies

### Main backend

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.8.6</version>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>com.sendgrid</groupId>
        <artifactId>sendgrid-java</artifactId>
        <version>4.10.1</version>
    </dependency>
</dependencies>
```

### Notification service (additional)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.sendgrid</groupId>
    <artifactId>sendgrid-java</artifactId>
    <version>4.10.1</version>
</dependency>
```

---



---

Build the backend progressively, finish management of each module separately then continue to the next module AFTER I accept the work done. After finishing that, add the Kafka functionalities.


ALL APPLICATION EXCEPTIONS ARE HANDLED BY THE GLOBAL EXCEPTIONS HANDLER.
Module exceptions extend shared types **directly** — no intermediate base classes.

Example:
`ConflictException` in shared → `EmailAlreadyExistsException extends ConflictException` in the user module

```java
// user/domain/exception/EmailAlreadyExistsException.java
public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email, "EMAIL_ALREADY_EXISTS");
    }
}
```

## 11. Implementation Order
```
shared — envelope, exceptions, GeoUtils, base config, Kafka producer stub.

user — user management, super-admin seeding.

center — centers, slots, slot generation scheduler.

donor — profile, blood type lock, health questionnaire, eligibility rules.

appointment — booking, check-in, health screening, completion.

emergency — CRUD, matching engine, accept/decline flow with slot selection.

analytics — audit log listener, metrics, reports.

system — config, feature flags, GDPR deletion.
Add Kafka producers to module which need them
user — auth, sessions, token verification, spring security configuration

notification-service — Kafka consumer, strategy channels (EMAIL / IN_APP),
WebSocket, retry/DLQ.
```
*This document is the single source of truth for the backend implementation.
All endpoint paths, request/response shapes, and behavioural rules above supersede
any implicit assumptions. When in doubt, check the use-case table in the original
requirements document before adding any field or endpoint.*

**Main class:** `com.zayenha.qatra.QatraApplication` at `C:\PFE\Qatra\donation-service\src\main\java\com\zayenha\qatra\QatraApplication.java`
```

---

This is the complete instruction file with all your requested changes:
- `profileComplete` requires only HealthQuestionnaire + location (no blood type)
- JWT secret via env var with SystemConfig override
- SendGrid as primary email, Gmail SMTP as fallback
- QR code specification (JWT with appointmentId + donorId, 15 min expiry)
- Same-day appointment reminders only
- All package paths using `com.zayenha.qatra`