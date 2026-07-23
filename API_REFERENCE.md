# Qatra Backend API Reference

> **Generated for frontend integration.** All endpoints go through Nginx on port 80 and 5090.
> Two backend services: **donation-service** (`:8080`) and **notification-service** (`:8082`).

---

## Table of Contents

- [Base URL & Routing](#base-url--routing)
- [Standard Response Wrapper](#standard-response-wrapper)
- [Authentication](#authentication)
- [Enums Reference](#enums-reference)
- [Auth Endpoints](#auth-endpoints-api-v1auth)
- [User Management (Admin)](#user-management-admin-api-v1adminusers)
- [Internal Users](#internal-users-api-v1internalusers)
- [Donor Endpoints](#donor-endpoints-api-v1donors)
- [Center Endpoints](#center-endpoints-api-v1centers)
- [Staff Endpoints](#staff-endpoints-api-v1staff)
- [Admin Profile](#admin-profile-api-v1admin)
- [Appointment Endpoints](#appointment-endpoints-api-v1appointments)
- [Emergency Endpoints](#emergency-endpoints-api-v1emergencies)
- [Analytics Endpoints](#analytics-endpoints-api-v1analytics)
- [System / GDPR Endpoints](#system--gdpr-endpoints-api-v1system)
- [Report Endpoints](#report-endpoints-api-v1centersidreport)
- [Notification Endpoints (notification-service)](#notification-endpoints-api-v1notifications)
- [WebSocket (notification-service)](#websocket-wsnotifications)

---

## Base URL & Routing

Nginx routes requests as follows:

| URL Pattern | Target |
|---|---|
| `/api/v1/notifications/**` | notification-service:8082 |
| `/ws/notifications` | notification-service:8082 (WebSocket upgrade) |
| `/api/**` (everything else) | donation-service:8080 |
| `/health` | donation-service:8080 `/actuator/health` |

---

## Standard Response Wrapper

Almost all endpoints return responses wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { ... },
  "page": {
    "number": 1,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  },
  "message": null,
  "code": null,
  "timestamp": "2026-07-19T12:00:00Z"
}
```

- `page` is only present on paginated endpoints.
- On error: `success` is `false`, `data` may be null, `message` and `code` contain error details.

---

## Authentication

### Login Response (`LoginResponse`)

Returned by `POST /api/v1/auth/login` and `POST /api/v1/auth/signup`:

```json
{
  "success": true,
  "data": {
    "token": "<JWT access token>",
    "tokenType": "Bearer",
    "refreshToken": "<refresh token>",
    "userId": 1,
    "email": "user@example.com",
    "displayName": "John Doe",
    "roles": ["DONOR"]
  }
}
```

### Auth Header

For authenticated requests, send:
```
Authorization: Bearer <token>
```

### Refresh Token

`POST /api/v1/auth/refresh` accepts:
```json
{ "refreshToken": "<refresh token>" }
```
Returns a new `LoginResponse` with fresh tokens.

---

## Enums Reference

All enum values are case-sensitive strings. Use them exactly as listed.

| Enum | Values |
|------|--------|
| **BloodType** | `A_POSITIVE`, `A_NEGATIVE`, `B_POSITIVE`, `B_NEGATIVE`, `AB_POSITIVE`, `AB_NEGATIVE`, `O_POSITIVE`, `O_NEGATIVE`, `UNKNOWN` |
| **Role** | `SUPER_ADMIN`, `CENTER_ADMIN`, `CENTER_STAFF`, `DONOR` |
| **UserStatus** | `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`, `PENDING_DELETION`, `DELETED` |
| **AvailabilityStatus** | `AVAILABLE`, `TEMPORARILY_UNAVAILABLE`, `VACATION_MODE`, `PERMANENTLY_RESTRICTED` |
| **NotificationFrequency** | `IMMEDIATE`, `DAILY_DIGEST`, `EMERGENCY_ONLY`, `DISABLED` |
| **FacilityType** | `BLOOD_BANK`, `HOSPITAL`, `CLINIC`, `MOBILE_UNIT`, `COMMUNITY_CENTER` |
| **CenterStatus** | `PENDING_APPROVAL`, `ACTIVE`, `SUSPENDED`, `CLOSED` |
| **AppointmentType** | `REGULAR`, `EMERGENCY` |
| **AppointmentStatus** | `SCHEDULED`, `CHECKED_IN`, `IN_SCREENING`, `COMPLETED`, `CANCELLED`, `NO_SHOW`, `RESCHEDULED` |
| **DonationOutcome** | `COMPLETED`, `CANCELLED` |
| **EmergencyUrgency** | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW` |
| **EmergencyStatus** | `OPEN`, `FULFILLED`, `CANCELLED`, `EXPIRED` |
| **ResponseStatus** | `ACCEPTED`, `DECLINED` |
| **GDPRDeletionStatus** | `IN_PROGRESS`, `CANCELED`, `COMPLETED` |
| **NotificationType** | `EMERGENCY_ALERT`, `APPOINTMENT_REMINDER`, `ELIGIBILITY_REMINDER`, `PROFILE_COMPLETION`, `EMAIL_VERIFICATION`, `PASSWORD_RESET`, `GENERAL` |
| **NotificationStatus** | `PENDING`, `SENT`, `DELIVERED`, `READ`, `FAILED` |
| **NotificationChannel** | `IN_APP`, `PUSH`, `EMAIL` |

---

## Auth Endpoints (`/api/v1/auth`)

| # | Method | Path | Auth | Body | Response |
|---|--------|------|------|------|----------|
| 1 | `POST` | `/api/v1/auth/login` | none | `LoginRequest` | `ApiResponse<LoginResponse>` |
| 2 | `POST` | `/api/v1/auth/signup` | none | `SignupRequest` | `ApiResponse<LoginResponse>` |
| 3 | `POST` | `/api/v1/auth/logout` | Bearer token in header | none | `ApiResponse<Void>` |
| 4 | `POST` | `/api/v1/auth/refresh` | none | `RefreshTokenRequest` | `ApiResponse<LoginResponse>` |
| 5 | `POST` | `/api/v1/auth/change-password` | authenticated | `ChangePasswordRequest` | `ApiResponse<Void>` |
| 6 | `POST` | `/api/v1/auth/forgot-password` | none | `ForgotPasswordRequest` | `ApiResponse<Void>` |
| 7 | `POST` | `/api/v1/auth/reset-password` | none | `ResetPasswordRequest` | `ApiResponse<Void>` |
| 8 | `POST` | `/api/v1/auth/request-verification` | authenticated | none | `ApiResponse<VerifyEmailResponse>` |
| 9 | `POST` | `/api/v1/auth/verify-email` | none | `VerifyEmailRequest` | `ApiResponse<VerifyEmailResponse>` |

### Request Bodies

```json
// LoginRequest
{ "email": "string", "password": "string" }

// SignupRequest
{ "email": "string", "phone": "string (optional)", "password": "string (min 8)", "firstName": "string", "familyName": "string", "displayName": "string (optional)" }

// RefreshTokenRequest
{ "refreshToken": "string" }

// ChangePasswordRequest
{ "currentPassword": "string", "newPassword": "string (min 8)" }

// ForgotPasswordRequest
{ "email": "string" }

// ResetPasswordRequest
{ "token": "string", "newPassword": "string (min 8)" }

// VerifyEmailRequest
{ "token": "string" }
```

---

## User Management (Admin) (`/api/v1/admin/users`)

> Auth: `SUPER_ADMIN` role required for all endpoints.

| # | Method | Path | Query Params | Body | Response |
|---|--------|------|-------------|------|----------|
| 1 | `GET` | `/api/v1/admin/users/` | `search?`, `sortBy?=id`, `sortDirection?=asc`, `page?=1`, `size?=20` | none | `ApiResponse<List<UserDetailResponse>>` + `Paginated` |
| 2 | `GET` | `/api/v1/admin/users/{id}` | none | none | `ApiResponse<UserDetailResponse>` |
| 3 | `POST` | `/api/v1/admin/users/` | none | `CreateUserRequest` | `ApiResponse<UserDetailResponse>` (201) |
| 4 | `PUT` | `/api/v1/admin/users/{id}` | none | `UpdateUserRequest` | `ApiResponse<UserDetailResponse>` |
| 5 | `PATCH` | `/api/v1/admin/users/{id}/status` | none | `UpdateUserStatusRequest` | `ApiResponse<Void>` |
| 6 | `POST` | `/api/v1/admin/users/{id}/roles` | none | `AssignRoleRequest` | `ApiResponse<Void>` |
| 7 | `DELETE` | `/api/v1/admin/users/{id}/roles?role=DONOR` | `role` (required) | none | `ApiResponse<String>` |
| 8 | `DELETE` | `/api/v1/admin/users/{id}` | none | none | `ApiResponse<String>` |

### Request Bodies

```json
// CreateUserRequest
{ "email": "string", "phone": "string", "password": "string (min 8)", "firstName": "string", "familyName": "string", "displayName": "string (optional)" }

// UpdateUserRequest
{ "email": "string", "phone": "string", "displayName": "string" }

// UpdateUserStatusRequest
{ "status": "ACTIVE | INACTIVE | SUSPENDED | PENDING_VERIFICATION | PENDING_DELETION | DELETED" }

// AssignRoleRequest
{ "role": "SUPER_ADMIN | CENTER_ADMIN | CENTER_STAFF | DONOR" }
```

### `UserDetailResponse`

```json
{
  "id": 1,
  "email": "user@example.com",
  "phone": "+213...",
  "displayName": "John Doe",
  "status": "ACTIVE",
  "emailVerified": true,
  "roles": ["DONOR"],
  "createdAt": "2026-01-01T00:00:00Z",
  "deletionRequestedAt": null,
  "deletedAt": null,
  "lastActiveAt": "2026-07-19T12:00:00Z"
}
```

---

## Internal Users (`/api/v1/internal/users`)

> Auth: `SUPER_ADMIN` role required.

| # | Method | Path | Params | Response |
|---|--------|------|--------|----------|
| 1 | `GET` | `/api/v1/internal/users/{id}` | path: `id` | `ApiResponse<UserSummary>` |
| 2 | `GET` | `/api/v1/internal/users/by-email?email=` | query: `email` | `ApiResponse<UserSummary>` |
| 3 | `GET` | `/api/v1/internal/users/by-phone?phone=` | query: `phone` | `ApiResponse<UserSummary>` |
| 4 | `GET` | `/api/v1/internal/users/{id}/roles` | path: `id` | `ApiResponse<List<Role>>` |
| 5 | `GET` | `/api/v1/internal/users/exists/by-email?email=` | query: `email` | `ApiResponse<Boolean>` |
| 6 | `GET` | `/api/v1/internal/users/exists/by-phone?phone=` | query: `phone` | `ApiResponse<Boolean>` |

### `UserSummary`

```json
{ "id": 1, "email": "user@example.com", "phone": "+213...", "displayName": "John Doe", "status": "ACTIVE" }
```

---

## Donor Endpoints (`/api/v1/donors`)

> Auth: `SUPER_ADMIN`, `DONOR` for `/me` endpoints. Staff roles for `/{id}` endpoints.

### Donor Profile — Self

| # | Method | Path | Body | Response |
|---|--------|------|------|----------|
| 1 | `GET` | `/api/v1/donors/me` | none | `ApiResponse<DonorProfileResponse>` |
| 2 | `PUT` | `/api/v1/donors/me` | `UpdateDonorRequest` | `ApiResponse<DonorProfileResponse>` |
| 3 | `PUT` | `/api/v1/donors/me/blood-type` | `UpdateBloodTypeRequest` | `ApiResponse<DonorProfileResponse>` |
| 4 | `PUT` | `/api/v1/donors/me/location` | `UpdateLocationRequest` | `ApiResponse<DonorProfileResponse>` |
| 5 | `PUT` | `/api/v1/donors/me/availability` | `UpdateAvailabilityRequest` | `ApiResponse<DonorProfileResponse>` |
| 6 | `PUT` | `/api/v1/donors/me/notification-prefs` | `UpdateNotificationPrefsRequest` | `ApiResponse<DonorProfileResponse>` |
| 7 | `GET` | `/api/v1/donors/me/health-questionnaire` | none | `ApiResponse<DonorHealthResponse>` |
| 8 | `PUT` | `/api/v1/donors/me/health-questionnaire` | `HealthQuestionnaireRequest` | `ApiResponse<DonorHealthResponse>` |
| 9 | `GET` | `/api/v1/donors/me/eligibility` | none | `ApiResponse<EligibilityResponse>` |
| 10 | `GET` | `/api/v1/donors/me/impact` | none | `ApiResponse<ImpactResponse>` |
| 11 | `GET` | `/api/v1/donors/me/certificates` | none | `ApiResponse<List<CertificateResponse>>` |
| 12 | `GET` | `/api/v1/donors/me/certificates/{id}/download` | path: `id` | `byte[]` (PDF, raw) |

### Donor — Admin/Staff View

| # | Method | Path | Body | Response |
|---|--------|------|------|----------|
| 13 | `GET` | `/api/v1/donors/{id}` | none | `ApiResponse<DonorDetailResponse>` |
| 14 | `GET` | `/api/v1/donors/{id}/eligibility` | none | `ApiResponse<EligibilityDetailResponse>` |
| 15 | `GET` | `/api/v1/donors/{id}/health-questionnaire` | none | `ApiResponse<DonorHealthResponse>` |
| 16 | `PATCH` | `/api/v1/donors/{id}/restriction` | `UpdateRestrictionRequest` | `ApiResponse<DonorProfileResponse>` |
| 17 | `PATCH` | `/api/v1/donors/{id}/flag` | `UpdateFlagRequest` | `ApiResponse<DonorProfileResponse>` |

### Request Bodies

```json
// UpdateDonorRequest
{ "displayName": "string (optional)", "phone": "string (optional)" }

// UpdateBloodTypeRequest
{ "bloodType": "A_POSITIVE | A_NEGATIVE | ..." }

// UpdateLocationRequest
{ "latitude": 36.75, "longitude": 3.06, "city": "Algiers (optional)", "country": "Algeria (optional)" }

// UpdateAvailabilityRequest
{ "status": "AVAILABLE | TEMPORARILY_UNAVAILABLE | VACATION_MODE | PERMANENTLY_RESTRICTED" }

// UpdateNotificationPrefsRequest
{ "preferences": { "frequency": "IMMEDIATE", "quietHours": { "start": "22:00:00", "end": "07:00:00" }, "allowEmergencyNotifications": true, "maxNotificationDistanceKm": 50 } }

// HealthQuestionnaireRequest
{ "hasChronicIllness": false, "medicalConditionsDetails": "string (optional)", "onMedication": false, "medicationDetails": "string (optional)", "lastSurgeryAt": "2026-01-01T00:00:00Z (optional)", "lastTravelAt": "2026-01-01T00:00:00Z (optional)", "lastTattooOrPiercingAt": "2026-01-01T00:00:00Z (optional)" }

// UpdateRestrictionRequest
{ "permanentlyRestricted": true, "restrictionReason": "string (optional)" }

// UpdateFlagRequest
{ "flaggedForManualReview": true }
```

### `DonorProfileResponse`

```json
{
  "id": 1,
  "userId": 1,
  "bloodType": "O_POSITIVE",
  "bloodTypeVerified": true,
  "latitude": 36.75,
  "longitude": 3.06,
  "city": "Algiers",
  "availability": "AVAILABLE",
  "notificationPreferences": { "frequency": "IMMEDIATE", "quietHours": { "start": "22:00:00", "end": "07:00:00" }, "allowEmergencyNotifications": true, "maxNotificationDistanceKm": 50 },
  "permanentlyRestricted": false,
  "restrictionReason": null,
  "flaggedForManualReview": false,
  "reliabilityScore": 0.95,
  "eligibleFromDate": "2026-07-01",
  "profileComplete": true,
  "totalDonations": 5,
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-07-19T12:00:00Z"
}
```

### `DonorHealthResponse`

```json
{
  "id": 1,
  "donorId": 1,
  "hasChronicIllness": false,
  "medicalConditionsDetails": null,
  "onMedication": false,
  "medicationDetails": null,
  "lastSurgeryAt": null,
  "lastTravelAt": null,
  "lastTattooOrPiercingAt": null,
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-07-19T12:00:00Z"
}
```

### `EligibilityResponse`

```json
{ "eligible": true, "eligibleFromDate": "2026-07-01", "reason": null }
```

### `EligibilityDetailResponse`

```json
{ "eligible": true, "eligibleFromDate": "2026-07-01", "permanentlyRestricted": false, "reason": null }
```

### `ImpactResponse`

```json
{ "totalDonations": 5, "milestones": ["First donation", "5 donations"] }
```

### `CertificateResponse`

```json
{ "id": 1, "appointmentId": 10, "donorName": "John Doe", "centerName": "Blood Bank Algiers", "mlCollected": 450, "donationDate": "2026-07-15", "downloadUrl": "/api/v1/donors/me/certificates/1/download" }
```

---

## Center Endpoints (`/api/v1/centers`)

| # | Method | Path | Auth | Query/Path Params | Body | Response |
|---|--------|------|------|-------------------|------|----------|
| 1 | `POST` | `/api/v1/centers/` | SUPER_ADMIN, CENTER_ADMIN | — | `CreateCenterRequest` | `ApiResponse<CenterResponse>` (201) |
| 2 | `PUT` | `/api/v1/centers/{id}` | SUPER_ADMIN, CENTER_ADMIN | path: `id` | `UpdateCenterRequest` | `ApiResponse<CenterResponse>` |
| 3 | `PATCH` | `/api/v1/centers/{id}/status` | SUPER_ADMIN, CENTER_ADMIN | path: `id` | `UpdateCenterStatusRequest` | `ApiResponse<Void>` |
| 4 | `DELETE` | `/api/v1/centers/{id}` | SUPER_ADMIN, CENTER_ADMIN | path: `id` | — | `ApiResponse<String>` |
| 5 | `GET` | `/api/v1/centers/{id}` | any | path: `id`, query: `fetchJoins?=false` | — | `ApiResponse<CenterResponse>` |
| 6 | `GET` | `/api/v1/centers/` | any | `sortBy?=id`, `sortDirection?=asc`, `page?=1`, `size?=20`, `search?` | — | `ApiResponse<List<CenterResponse>>` + `Paginated` |
| 7 | `POST` | `/api/v1/centers/{id}/closures` | SUPER_ADMIN, CENTER_ADMIN | path: `id` | `CreateClosureRequest` | `ApiResponse<ClosureResponse>` |
| 8 | `GET` | `/api/v1/centers/{id}/slots` | any | path: `id`, query: `date?` (ISO), `slotType?`, `fetchJoins?=false` | — | `ApiResponse<List<SlotResponse>>` |
| 9 | `PATCH` | `/api/v1/centers/{id}/slots/{slotId}/block` | SUPER_ADMIN, CENTER_ADMIN | path: `id`, `slotId` | `BlockSlotRequest` | `ApiResponse<SlotResponse>` |
| 10 | `GET` | `/api/v1/centers/{id}/staff` | any | path: `id` | — | `ApiResponse<List<StaffSummaryResponse>>` |
| 11 | `POST` | `/api/v1/centers/{id}/staff` | SUPER_ADMIN, CENTER_ADMIN | path: `id` | `AddStaffRequest` | `ApiResponse<StaffSummaryResponse>` (201) |
| 12 | `DELETE` | `/api/v1/centers/{id}/staff/{userId}` | SUPER_ADMIN, CENTER_ADMIN | path: `id`, `userId` | — | `ApiResponse<String>` |
| 13 | `GET` | `/api/v1/centers/pending` | any | `sortBy?=id`, `sortDirection?=asc`, `page?=1`, `size?=20` | — | `ApiResponse<List<CenterResponse>>` + `Paginated` |
| 14 | `PATCH` | `/api/v1/centers/{id}/approve` | SUPER_ADMIN | path: `id` | `ApproveCenterRequest` | `ApiResponse<CenterResponse>` |

### Request Bodies

```json
// CreateCenterRequest / UpdateCenterRequest
{
  "name": "string",
  "address": "string",
  "city": "string",
  "country": "string",
  "postalCode": "string (optional)",
  "phone": "string",
  "email": "string",
  "latitude": 36.75,
  "longitude": 3.06,
  "facilityType": "BLOOD_BANK | HOSPITAL | CLINIC | MOBILE_UNIT | COMMUNITY_CENTER",
  "operatingHours": {
    "monday": { "opens": "08:00:00", "closes": "17:00:00" },
    "tuesday": { "opens": "08:00:00", "closes": "17:00:00" },
    "wednesday": { "opens": "08:00:00", "closes": "17:00:00" },
    "thursday": { "opens": "08:00:00", "closes": "17:00:00" },
    "friday": { "opens": "08:00:00", "closes": "17:00:00" },
    "saturday": null,
    "sunday": null,
    "closedWindows": []
  },
  "totalCapacity": 50,
  "maxRegular": 40,
  "slotPeriod": 30
}

// UpdateCenterStatusRequest
{ "status": "ACTIVE | PENDING_APPROVAL | SUSPENDED | CLOSED" }

// CreateClosureRequest
{ "date": "2026-07-25", "startTime": "08:00 (optional)", "endTime": "17:00 (optional)", "allDay": false, "reason": "string" }

// BlockSlotRequest
{ "isBlocked": true }

// AddStaffRequest
{ "userId": 5 }

// ApproveCenterRequest
{ "approved": true, "reason": "string (optional)" }
```

### `CenterResponse`

```json
{
  "id": 1,
  "name": "Blood Bank Algiers",
  "address": "123 Rue Didouche Mourad",
  "city": "Algiers",
  "country": "Algeria",
  "postalCode": "16000",
  "phone": "+213...",
  "email": "center@example.com",
  "latitude": 36.75,
  "longitude": 3.06,
  "facilityType": "BLOOD_BANK",
  "operatingHours": { "monday": { "opens": "08:00:00", "closes": "17:00:00" }, "tuesday": { "opens": "08:00:00", "closes": "17:00:00" }, "wednesday": { "opens": "08:00:00", "closes": "17:00:00" }, "thursday": { "opens": "08:00:00", "closes": "17:00:00" }, "friday": { "opens": "08:00:00", "closes": "17:00:00" }, "saturday": null, "sunday": null, "closedWindows": [] },
  "status": "ACTIVE",
  "totalCapacity": 50,
  "maxRegular": 40,
  "slotPeriod": 30,
  "createdAt": "2026-01-01T00:00:00Z",
  "updatedAt": "2026-07-19T12:00:00Z"
}
```

### `SlotResponse`

```json
{
  "id": 10,
  "centerId": 1,
  "date": "2026-07-20",
  "startTime": "08:00:00",
  "endTime": "08:30:00",
  "maxBookings": 5,
  "maxRegularBookings": 4,
  "bookedCount": 2,
  "regularBookedCount": 1,
  "isBlocked": false
}
```

### `StaffSummaryResponse`

```json
{ "id": 1, "userId": 5, "centerId": 1, "verified": true, "createdAt": "2026-01-01T00:00:00Z" }
```

### `ClosureResponse`

```json
{ "blockedSlotCount": 16, "date": "2026-07-25", "reason": "Public holiday" }
```

---

## Staff Endpoints (`/api/v1/staff`)

| # | Method | Path | Auth | Response |
|---|--------|------|------|----------|
| 1 | `GET` | `/api/v1/staff/me` | CENTER_STAFF | `ApiResponse<StaffProfileResponse>` |

### `StaffProfileResponse`

```json
{ "id": 1, "userId": 5, "centerId": 1, "verified": true, "createdAt": "2026-01-01T00:00:00Z" }
```

---

## Admin Profile (`/api/v1/admin/`)

| # | Method | Path | Auth | Response |
|---|--------|------|------|----------|
| 1 | `GET` | `/api/v1/admin/me` | SUPER_ADMIN, CENTER_ADMIN | `ApiResponse<CenterAdminDTO>` |

### `CenterAdminDTO`

```json
{ "id": 1, "userId": 3, "centerId": 1, "createdAt": "2026-01-01T00:00:00Z" }
```

---

## Appointment Endpoints (`/api/v1/appointments`)

| # | Method | Path | Auth | Params/Path | Body | Response |
|---|--------|------|------|-------------|------|----------|
| 1 | `POST` | `/api/v1/appointments/` | SUPER_ADMIN, DONOR | — | `CreateAppointmentRequest` | `ApiResponse<AppointmentResponse>` (201) |
| 2 | `POST` | `/api/v1/appointments/{id}/check-in` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `id` | — | `ApiResponse<AppointmentResponse>` |
| 3 | `POST` | `/api/v1/appointments/{id}/screening` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `id` | — | `ApiResponse<HealthScreeningResponse>` |
| 4 | `POST` | `/api/v1/appointments/{id}/screening-results` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `id` | `ScreeningRequest` | `ApiResponse<HealthScreeningResponse>` |
| 5 | `POST` | `/api/v1/appointments/{id}/complete` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `id` | `CompleteAppointmentRequest` | `ApiResponse<AppointmentResponse>` |
| 6 | `POST` | `/api/v1/appointments/{id}/no-show` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `id` | — | `ApiResponse<AppointmentResponse>` |
| 7 | `POST` | `/api/v1/appointments/{id}/cancel` | SUPER_ADMIN, DONOR, CENTER_ADMIN | path: `id` | — | `ApiResponse<AppointmentResponse>` |
| 8 | `PUT` | `/api/v1/appointments/{id}/reschedule` | SUPER_ADMIN, DONOR | path: `id` | `RescheduleAppointmentRequest` | `ApiResponse<AppointmentResponse>` |
| 9 | `GET` | `/api/v1/appointments/{id}` | any | path: `id` | — | `ApiResponse<AppointmentResponse>` |
| 10 | `GET` | `/api/v1/appointments/` | any | `page?=0`, `size?=20` | — | `ApiResponse<List<AppointmentResponse>>` + `Paginated` |
| 11 | `GET` | `/api/v1/appointments/by-donor/{donorId}` | SUPER_ADMIN, DONOR, CENTER_ADMIN | path: `donorId` | — | `ApiResponse<List<AppointmentResponse>>` |
| 12 | `GET` | `/api/v1/appointments/by-center/{centerId}` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `centerId`, query: `date` (ISO, required) | — | `ApiResponse<List<AppointmentResponse>>` |
| 13 | `GET` | `/api/v1/appointments/queue` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | `centerId` (required), `fromDate?`, `toDate?`, `page?=1`, `size?=20` | — | `ApiResponse<List<AppointmentResponse>>` + `Paginated` |
| 14 | `GET` | `/api/v1/appointments/{id}/screening` | any | path: `id` | — | `ApiResponse<HealthScreeningResponse>` |

### Request Bodies

```json
// CreateAppointmentRequest
{ "type": "REGULAR | EMERGENCY", "donorId": 1, "slotId": 10, "emergencyId": null }

// CompleteAppointmentRequest
{ "outcome": "COMPLETED | CANCELLED", "mlCollected": 450, "notes": "string (optional)" }

// RescheduleAppointmentRequest
{ "slotId": 20 }

// ScreeningRequest
{ "weight": 70.5, "bloodPressure": "120/80 (optional)", "hemoglobin": 13.5, "temperature": 36.5, "eligible": true, "notes": "string (optional)" }
```

### `AppointmentResponse`

```json
{
  "id": 1,
  "donorId": 1,
  "slotId": 10,
  "centerId": 1,
  "emergencyId": null,
  "completedByStaffId": null,
  "appointmentType": "REGULAR",
  "status": "SCHEDULED",
  "bloodType": "O_POSITIVE",
  "outcome": null,
  "mlCollected": null,
  "qrCode": "...",
  "checkedInAt": null,
  "startedAt": null,
  "completedAt": null,
  "cancelledAt": null,
  "cancellationReason": null,
  "notes": null,
  "createdAt": "2026-07-19T12:00:00Z",
  "updatedAt": "2026-07-19T12:00:00Z"
}
```

### `HealthScreeningResponse`

```json
{
  "id": 1,
  "appointmentId": 1,
  "donorId": 1,
  "screenedByStaffId": 5,
  "weight": 70.5,
  "bloodPressure": "120/80",
  "hemoglobin": 13.5,
  "temperature": 36.5,
  "eligible": true,
  "notes": null,
  "screenedAt": "2026-07-19T12:30:00Z"
}
```

---

## Emergency Endpoints (`/api/v1/emergencies`)

| # | Method | Path | Auth | Params/Path | Body | Response |
|---|--------|------|------|-------------|------|----------|
| 1 | `POST` | `/api/v1/emergencies/` | any authenticated | — | `CreateEmergencyRequest` | `ApiResponse<EmergencyResponse>` (201) |
| 2 | `PUT` | `/api/v1/emergencies/{id}` | any authenticated | path: `id` | `UpdateEmergencyRequest` | `ApiResponse<EmergencyResponse>` |
| 3 | `POST` | `/api/v1/emergencies/{id}/cancel` | any authenticated | path: `id` | — | `ApiResponse<EmergencyResponse>` |
| 4 | `POST` | `/api/v1/emergencies/{id}/resolve` | SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF | path: `id` | `ResolveEmergencyRequest` | `ApiResponse<EmergencyResponse>` |
| 5 | `GET` | `/api/v1/emergencies/{id}` | any authenticated | path: `id` | — | `ApiResponse<EmergencyResponse>` |
| 6 | `GET` | `/api/v1/emergencies/` | any authenticated | `page?=0`, `size?=20` | — | `ApiResponse<List<EmergencyResponse>>` + `Paginated` |
| 7 | `GET` | `/api/v1/emergencies/open/{bloodType}` | any authenticated | path: `bloodType` | — | `ApiResponse<List<EmergencyResponse>>` |
| 8 | `GET` | `/api/v1/emergencies/nearby` | any authenticated | `latitude` (req), `longitude` (req), `radiusKm?=50` | — | `ApiResponse<List<EmergencyResponse>>` |
| 9 | `POST` | `/api/v1/emergencies/{emergencyId}/responses/accept` | SUPER_ADMIN, DONOR | path: `emergencyId` | `AcceptResponseRequest` | `ApiResponse<DonorResponseDTO>` |
| 10 | `POST` | `/api/v1/emergencies/{emergencyId}/responses/decline` | SUPER_ADMIN, DONOR | path: `emergencyId` | `DeclineResponseRequest` | `ApiResponse<DonorResponseDTO>` |
| 11 | `GET` | `/api/v1/emergencies/{id}/responses` | any authenticated | path: `id` | — | `ApiResponse<List<DonorResponseDTO>>` |
| 12 | `GET` | `/api/v1/emergencies/responses/donor/{donorId}` | SUPER_ADMIN, DONOR, CENTER_ADMIN | path: `donorId` | — | `ApiResponse<List<DonorResponseDTO>>` |

### Request Bodies

```json
// CreateEmergencyRequest / UpdateEmergencyRequest
{ "centerId": 1, "bloodType": "O_NEGATIVE", "unitsNeeded": 5, "urgency": "CRITICAL | HIGH | MEDIUM | LOW", "matchRadius": 50, "contactPhone": "+213... (optional)" }

// ResolveEmergencyRequest
{ "notes": "string (max 500, optional)" }

// AcceptResponseRequest
{ "slotId": 10 }

// DeclineResponseRequest
{ "reason": "string (optional)" }
```

### `EmergencyResponse`

```json
{
  "id": 1,
  "centerId": 1,
  "bloodType": "O_NEGATIVE",
  "unitsNeeded": 5,
  "urgency": "CRITICAL",
  "matchRadius": 50,
  "escalationLevel": 1,
  "contactPhone": "+213...",
  "status": "OPEN",
  "createdAt": "2026-07-19T12:00:00Z",
  "updatedAt": "2026-07-19T12:00:00Z",
  "expiresAt": "2026-07-20T12:00:00Z",
  "resolvedAt": null,
  "resolvedByUserId": null
}
```

### `DonorResponseDTO`

```json
{
  "id": 1,
  "emergencyId": 1,
  "donorId": 1,
  "slotId": 10,
  "status": "ACCEPTED | DECLINED",
  "respondedAt": "2026-07-19T12:30:00Z"
}
```

---

## Analytics Endpoints (`/api/v1/analytics`)

> Auth: `SUPER_ADMIN`, `CENTER_ADMIN` for all endpoints.

| # | Method | Path | Params | Response |
|---|--------|------|--------|----------|
| 1 | `GET` | `/api/v1/analytics/audit-logs` | `page?=0`, `size?=20`, `action?`, `fromDate?` (ISO), `toDate?` (ISO), `centerId?` | `ApiResponse<List<AuditLogResponse>>` + `Paginated` |
| 2 | `GET` | `/api/v1/analytics/audit-logs/export` | `action?`, `fromDate?`, `toDate?`, `centerId?` | `text/csv` (raw, `Content-Disposition: attachment`) |
| 3 | `GET` | `/api/v1/analytics/audit-logs/by-action/{action}` | path: `action` | `ApiResponse<List<AuditLogResponse>>` |
| 4 | `GET` | `/api/v1/analytics/audit-logs/by-user/{userId}` | path: `userId` | `ApiResponse<List<AuditLogResponse>>` |
| 5 | `GET` | `/api/v1/analytics/metrics` | none | `ApiResponse<List<MetricsResponse>>` |
| 6 | `GET` | `/api/v1/analytics/centers/{centerId}/metrics` | path: `centerId` | `ApiResponse<CenterMetricsResponse>` |

### `AuditLogResponse`

```json
{
  "id": 1,
  "userId": 1,
  "action": "USER_LOGIN",
  "entityType": "User",
  "entityId": 1,
  "oldValue": null,
  "newValue": { "email": "user@example.com" },
  "ipAddress": "192.168.1.1",
  "timestamp": "2026-07-19T12:00:00Z"
}
```

### `MetricsResponse`

```json
{ "metricName": "total_donations", "total": 150, "today": 5, "thisWeek": 30, "thisMonth": 120 }
```

### `CenterMetricsResponse`

```json
{
  "totalAppointments": 500,
  "completedAppointments": 450,
  "todayAppointments": 10,
  "weekAppointments": 60,
  "monthAppointments": 200,
  "totalEmergencies": 20,
  "fulfilledEmergencies": 18,
  "todayEmergencies": 1,
  "weekEmergencies": 5,
  "monthEmergencies": 15,
  "totalDonorResponses": 80,
  "responseRate30d": 75,
  "totalMlCollected": 202500,
  "activeEmergencies": 2,
  "appointmentsByDay": [{ "date": "2026-07-19", "count": 10 }],
  "emergenciesByDay": [{ "date": "2026-07-19", "count": 1 }]
}
```

---

## System / GDPR Endpoints (`/api/v1/system`)

| # | Method | Path | Auth | Body | Response |
|---|--------|------|------|------|----------|
| 1 | `POST` | `/api/v1/system/gdpr/request` | SUPER_ADMIN, DONOR | `GDPRRequestDeletionRequest` | `ApiResponse<GDPRDeletionResponse>` |
| 2 | `POST` | `/api/v1/system/gdpr/{id}/complete` | any | path: `id` | `ApiResponse<GDPRDeletionResponse>` |
| 3 | `POST` | `/api/v1/system/gdpr/{id}/cancel` | any | path: `id` | `ApiResponse<GDPRDeletionResponse>` |
| 4 | `GET` | `/api/v1/system/gdpr` | any | — | `ApiResponse<List<GDPRDeletionResponse>>` |
| 5 | `GET` | `/api/v1/system/gdpr/{id}` | any | path: `id` | `ApiResponse<GDPRDeletionResponse>` |

### Request Bodies

```json
// GDPRRequestDeletionRequest
{ "userId": 1, "reason": "string (optional)" }
```

### `GDPRDeletionResponse`

```json
{ "id": 1, "userId": 1, "reason": "No longer using the service", "status": "IN_PROGRESS | CANCELED | COMPLETED", "requestedAt": "2026-07-19T12:00:00Z", "processedAt": null }
```

---

## Report Endpoints (`/api/v1/centers/{id}/report`)

| # | Method | Path | Auth | Params | Response |
|---|--------|------|------|--------|----------|
| 1 | `GET` | `/api/v1/centers/{id}/report` | SUPER_ADMIN, CENTER_ADMIN | path: `id`, query: `startDate` (ISO, required), `endDate` (ISO, required) | `text/csv` (raw, `Content-Disposition: attachment; filename="center-{id}-report.csv"`) |

---

## Notification Endpoints (`/api/v1/notifications`)

> These are served by **notification-service** (port 8082), routed via `/api/v1/notifications`.

| # | Method | Path | Params | Response |
|---|--------|------|--------|----------|
| 1 | `GET` | `/api/v1/notifications/` | `type?` (NotificationType), `read?` (Boolean), `page?=1`, `size?=20` | `ApiResponse<List<NotificationResponse>>` + `Paginated` |
| 2 | `PATCH` | `/api/v1/notifications/{id}/read` | path: `id` | `NotificationResponse` (raw, NOT wrapped in ApiResponse) |
| 3 | `PATCH` | `/api/v1/notifications/read-all` | none | `{ "markedCount": 5 }` (raw) |
| 4 | `GET` | `/api/v1/notifications/unread-count` | none | `{ "count": 3 }` (raw) |

> **Note:** Notification endpoints return raw JSON objects, NOT wrapped in `ApiResponse`. Only the `GET /` list endpoint uses `ApiResponse`.

### `NotificationResponse`

```json
{
  "id": 1,
  "userId": 1,
  "emergencyId": null,
  "appointmentId": 10,
  "type": "APPOINTMENT_REMINDER",
  "channels": ["IN_APP", "PUSH"],
  "title": "Upcoming Appointment",
  "body": "You have an appointment tomorrow at 10:00 AM",
  "data": { "slotTime": "2026-07-20T10:00:00Z" },
  "status": "SENT",
  "createdAt": "2026-07-19T12:00:00Z",
  "sentAt": "2026-07-19T12:00:01Z",
  "readAt": null
}
```

---

## WebSocket (`/ws/notifications`)

> Raw WebSocket (not STOMP). Served by **notification-service**.

### Connection

```
ws://{host}/ws/notifications?token={JWT}
```

- Token is passed as a query parameter.
- On invalid/missing token, the handshake is rejected (HTTP 401).
- Multiple sessions per user are supported.

### Protocol

- **Text messages only** (JSON payloads).
- **Server -> Client:** Pushes `NotificationResponse` JSON when a new notification is created for the user.
- **Client -> Client:** Any `TextMessage` sent by the client is relayed to all other sessions of the same user.

### Server Push Format

```json
{
  "id": 1,
  "userId": 1,
  "emergencyId": null,
  "appointmentId": 10,
  "type": "APPOINTMENT_REMINDER",
  "channels": ["IN_APP", "PUSH"],
  "title": "Upcoming Appointment",
  "body": "You have an appointment tomorrow at 10:00 AM",
  "data": { "slotTime": "2026-07-20T10:00:00Z" },
  "status": "SENT",
  "createdAt": "2026-07-19T12:00:00Z",
  "sentAt": "2026-07-19T12:00:01Z",
  "readAt": null
}
```

### Example Connection Code

```javascript
const token = localStorage.getItem('token');
const ws = new WebSocket(`ws://localhost/ws/notifications?token=${token}`);

ws.onmessage = (event) => {
  const notification = JSON.parse(event.data);
  console.log('New notification:', notification);
};
```

---

## Common Mistakes to Avoid

1. **Notification endpoints return raw JSON**, not `ApiResponse` wrappers (except `GET /`).
2. **WebSocket is raw**, not STOMP. Use `new WebSocket(...)`, not SockJS/STOMP clients.
3. **Pagination is 1-indexed** on most endpoints (default `page=1`), but appointments use **0-indexed** (`page=0`).
4. **`/api/v1/notifications/**` routes to notification-service**, everything else under `/api/` routes to donation-service.
5. **All timestamps are ISO 8601** (`2026-07-19T12:00:00Z`).
6. **Enum values are SCREAMING_SNAKE_CASE** strings: `"O_POSITIVE"`, `"CENTER_ADMIN"`, etc.
7. **No endpoint uses camelCase for enum values** - they are always uppercase with underscores.
8. **Certificate download** returns raw `byte[]` (PDF), not `ApiResponse`.
9. **Audit log export** and **center report** return raw CSV, not JSON.
10. **`GET /api/v1/appointments/` uses 0-indexed pagination**, while most other endpoints use 1-indexed.
