# Qatra - Domain Class Diagram

```mermaid
classDiagram
    direction TB

    class User {
        -Long id
        -String email
        -String phone
        -String hashedPassword
        -String displayName
        -String firstName
        -String familyName
        -UserStatus status
        -boolean emailVerified
        -Instant createdAt
        -Instant lastActiveAt
        -Instant deletionRequestedAt
        -Instant deletedAt
        -List~Role~ roles
        +User(email, phone, hashedPassword, displayName, firstName, familyName)
        +update(email, phone, displayName, firstName, familyName)
        +updateStatus(status)
        +markDeleted()
        +markDeletionRequested()
        +changePassword(newEncodedPassword)
        +verifyEmail()
        +isActive() boolean
        +isEnabled() boolean
        +isDisabled() boolean
        +isDeleted() boolean
    }

    class UserRole {
        -Long id
        -Long userId
        -Role role
        -Instant assignedAt
        +UserRole(userId, role)
        +reconstruct(id, userId, role, assignedAt)$ UserRole
    }

    class Session {
        -Long id
        -Long userId
        -String accessTokenHash
        -String refreshTokenHash
        -String ipAddress
        -String userAgent
        -Instant expiresAt
        -Instant createdAt
        +Session(userId, accessTokenHash, refreshTokenHash, ipAddress, userAgent, expiresAt)
        +validate() boolean
        +refresh(newExpiresAt)
        +revoke()
        +rotateTokens(newAccessTokenHash, newRefreshTokenHash, newExpiresAt)
        +reconstruct(id, userId, accessTokenHash, refreshTokenHash, ipAddress, userAgent, expiresAt, createdAt)$ Session
    }

    class VerificationToken {
        -Long id
        -Long userId
        -String tokenHash
        -VerificationTokenType type
        -Instant expiresAt
        -Instant createdAt
        +VerificationToken(userId, tokenHash, type, expiresAt)
        +validate() boolean
        +consume()
        +reconstruct(id, userId, tokenHash, type, expiresAt, createdAt)$ VerificationToken
    }

    class DonorProfile {
        -Long id
        -Long userId
        -BloodType bloodType
        -Boolean bloodTypeVerified
        -Boolean profileComplete
        -DonorStatus status
        -Double latitude
        -Double longitude
        -String city
        -AvailabilityStatus availability
        -LocalDate lastDonationDate
        -LocalDate eligibleFromDate
        -NotificationPreferences notificationPreferences
        -Boolean allowEmergencyNotifications
        -Integer consecutiveEmergencyDeclines
        -Boolean flaggedForManualReview
        -Boolean permanentlyRestricted
        -String restrictionReason
        -Double reliabilityScore
        -int totalDonations
        -Instant createdAt
        -Instant updatedAt
        -Instant deletionRequestedAt
        -Instant deletedAt
        -Instant lastAcceptAt
        +DonorProfile(userId)
        +canDonate() boolean
        +updateLocation(latitude, longitude, city)
        +resetConsecutiveDeclinesOnAccept()
    }

    class HealthQuestionnaire {
        -Long id
        -Long donorId
        -Boolean hasChronicIllness
        -Instant lastSurgeryAt
        -Instant lastTravelAt
        -Instant lastTattooOrPiercingAt
        -Boolean onMedication
        -String medicalConditionsDetails
        -String medicationDetails
        -Instant createdAt
        -Instant updatedAt
        +HealthQuestionnaire(donorId)
    }

    class DonationCenter {
        -Long id
        -Long createdByUserId
        -String name
        -Double latitude
        -Double longitude
        -String address
        -String city
        -String country
        -String postalCode
        -String phone
        -String email
        -OperatingHours operatingHours
        -Integer totalCapacity
        -Integer maxRegular
        -Integer slotPeriod
        -FacilityType facilityType
        -CenterStatus status
        -Instant createdAt
        -Instant updatedAt
        +DonationCenter(name, address, city, country, postalCode, phone, email, latitude, longitude, facilityType, operatingHours, totalCapacity, maxRegular, slotPeriod, createdByUserId)
        +isOperatingNow() boolean
    }

    class Slot {
        -Long id
        -Long centerId
        -LocalDate date
        -LocalTime startTime
        -LocalTime endTime
        -int maxBookings
        -int maxRegularBookings
        -int bookedCount
        -int regularBookedCount
        -boolean isBlocked
        -Instant createdAt
        +Slot(centerId, date, startTime, endTime, maxBookings, maxRegularBookings)
        +isAvailable() boolean
        +isAvailableForRegular() boolean
        +bookRegular()
        +book()
        +release()
        +releaseRegular()
        +validateCounts()
    }

    class CenterStaffProfile {
        -Long id
        -Long userId
        -Long centerId
        -boolean verified
        -Instant createdAt
        +CenterStaffProfile(userId, centerId)
    }

    class CenterAdminProfile {
        -Long id
        -Long userId
        -Long centerId
        -Instant createdAt
        +CenterAdminProfile(userId, centerId)
    }

    class Appointment {
        -Long id
        -Long slotId
        -Long userId
        -Long donorId
        -Long centerId
        -Long emergencyId
        -Long completedByStaffId
        -AppointmentStatus status
        -AppointmentType appointmentType
        -BloodType bloodType
        -Integer mlCollected
        -String notes
        -String qrCode
        -Instant createdAt
        -Instant updatedAt
        -Instant checkedInAt
        -Instant startedAt
        -Instant completedAt
        -Instant cancelledAt
        -String cancellationReason
        -DonationOutcome outcome
        -LocalDate slotDate
        -LocalTime slotTime
        +Appointment(donorId, slotId, centerId, emergencyId, appointmentType, bloodType)
        +checkIn()
        +startScreening()
        +complete(outcome, notes)
        +complete(outcome, notes, mlCollected, staffId)
        +markNoShow()
        +cancel()
        +cancel(reason)
        +reschedule(newSlotId)
    }

    class HealthScreening {
        -Long id
        -Long appointmentId
        -Long donorId
        -Double weight
        -String bloodPressure
        -Double hemoglobin
        -Long screenedByStaffId
        -Double temperature
        -String notes
        -Boolean eligible
        -Instant screenedAt
        +HealthScreening(appointmentId, donorId)
    }

    class EmergencyRequest {
        -Long id
        -Long centerId
        -Long createdByStaffId
        -BloodType bloodType
        -Integer unitsNeeded
        -EmergencyUrgency urgency
        -String contactPhone
        -EmergencyStatus status
        -Integer matchRadius
        -Integer escalationLevel
        -Instant expiresAt
        -Instant createdAt
        -Instant updatedAt
        -Instant resolvedAt
        -Long resolvedByUserId
        +EmergencyRequest(centerId, createdByStaffId, bloodType, unitsNeeded, urgency, contactPhone, matchRadius, expiresAt)
        +matchDonors()
        +updateStatus(status)
        +resolve(resolvedByUserId)
        +cancel()
        +fulfill()
    }

    class DonorResponse {
        -Long id
        -Long emergencyId
        -Long donorId
        -Long slotId
        -ResponseStatus status
        -String reason
        -Instant respondedAt
        +DonorResponse(emergencyId, donorId)
        +accept(slotId)
        +decline(reason)
    }

    class MatchResult {
        -Long id
        -Long emergencyId
        -Long centerId
        -Long donorId
        -Long radius
        -BloodType bloodType
        -Integer escalationLevel
        -MatchStatus status
        -Instant respondedAt
        -Instant createdAt
        +MatchResult(emergencyId, centerId, donorId, radius, bloodType, escalationLevel)
    }

    class GDPRDeletionRequest {
        -Long id
        -Long userId
        -String reason
        -GDPRDeletionStatus status
        -Instant requestedAt
        -Instant processedAt
        +GDPRDeletionRequest(userId, reason)
        +complete()
        +cancel()
    }

    class AuditLog {
        -Long id
        -Long userId
        -String action
        -String entityType
        -Long entityId
        -Map~String,Object~ oldValue
        -Map~String,Object~ newValue
        -String ipAddress
        -Instant timestamp
        +AuditLog(userId, action, entityType, entityId, oldValue, newValue, ipAddress)
    }

    class CenterMetrics {
        <<record>>
        -long totalAppointments
        -long completedAppointments
        -long todayAppointments
        -long weekAppointments
        -long monthAppointments
        -long totalEmergencies
        -long fulfilledEmergencies
        -long todayEmergencies
        -long weekEmergencies
        -long monthEmergencies
        -long totalDonorResponses
        -long responseRate30d
        -long totalMlCollected
        -long activeEmergencies
        -List~Map~String,Object~~ appointmentsByDay
        -List~Map~String,Object~~ emergenciesByDay
    }

    class Notification {
        -Long id
        -Long userId
        -String email
        -Long emergencyId
        -Long appointmentId
        -NotificationType type
        -String title
        -String body
        -Map~String,Object~ data
        -String correlationId
        -List~NotificationChannel~ channels
        -NotificationStatus status
        -Instant createdAt
        -Instant sentAt
        -Instant readAt
        +Notification(userId, email, emergencyId, appointmentId, type, title, body, data, correlationId, channels)
        +markSent()
        +markRead()
        +markFailed()
    }

    class NotificationPayload {
        <<record>>
        -Long userId
        -String email
        -Long emergencyId
        -Long appointmentId
        -NotificationType type
        -String title
        -String body
        -String htmlBody
        -Map~String,Object~ data
        -String correlationId
        -Instant occurredAt
        -List~NotificationChannel~ requestedChannels
    }

    class OperatingHours {
        <<record>>
        -DaySchedule monday
        -DaySchedule tuesday
        -DaySchedule wednesday
        -DaySchedule thursday
        -DaySchedule friday
        -DaySchedule saturday
        -DaySchedule sunday
        -List~ClosureWindow~ closedWindows
        +today() Optional~DaySchedule~
        +forDay(day) Optional~DaySchedule~
    }

    class DaySchedule {
        <<record>>
        -LocalTime opens
        -LocalTime closes
    }

    class ClosureWindow {
        <<record>>
        -LocalDate date
        -LocalTime startTime
        -LocalTime endTime
        -boolean allDay
        -String reason
    }

    class NotificationPreferences {
        <<record>>
        -NotificationFrequency frequency
        -QuietHours quietHours
        -boolean allowEmergencyNotifications
        -int maxNotificationDistanceKm
    }

    class QuietHours {
        <<record>>
        -LocalTime start
        -LocalTime end
    }

    class SearchCriteria {
        <<record>>
        -String search
        -String sortBy
        -String sortDirection
        -int page
        -int size
    }

    class PageResult {
        <<record>>
        -List~T~ content
        -int page
        -int size
        -long totalElements
        -int totalPages
    }

    class BloodType {
        <<enumeration>>
        A_POSITIVE
        A_NEGATIVE
        B_POSITIVE
        B_NEGATIVE
        AB_POSITIVE
        AB_NEGATIVE
        O_POSITIVE
        O_NEGATIVE
        UNKNOWN
        +canDonateTo(recipient) boolean
    }

    class AppointmentType {
        <<enumeration>>
        REGULAR
        EMERGENCY
    }

    class UserStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
        SUSPENDED
        PENDING_VERIFICATION
        PENDING_DELETION
        DELETED
    }

    class Role {
        <<enumeration>>
        SUPER_ADMIN
        CENTER_ADMIN
        CENTER_STAFF
        DONOR
    }

    class VerificationTokenType {
        <<enumeration>>
        EMAIL_VERIFICATION
        PASSWORD_RESET
    }

    class DonorStatus {
        <<enumeration>>
        ACTIVE
        INACTIVE
        PENDING_DELETION
        DELETED
    }

    class AvailabilityStatus {
        <<enumeration>>
        AVAILABLE
        TEMPORARILY_UNAVAILABLE
        VACATION_MODE
        PERMANENTLY_RESTRICTED
    }

    class NotificationFrequency {
        <<enumeration>>
        IMMEDIATE
        DAILY_DIGEST
        EMERGENCY_ONLY
        DISABLED
    }

    class CenterStatus {
        <<enumeration>>
        PENDING_APPROVAL
        ACTIVE
        SUSPENDED
        CLOSED
    }

    class FacilityType {
        <<enumeration>>
        BLOOD_BANK
        HOSPITAL
        CLINIC
        MOBILE_UNIT
        COMMUNITY_CENTER
    }

    class AppointmentStatus {
        <<enumeration>>
        SCHEDULED
        CHECKED_IN
        IN_SCREENING
        COMPLETED
        CANCELLED
        NO_SHOW
        RESCHEDULED
    }

    class DonationOutcome {
        <<enumeration>>
        COMPLETED
        CANCELLED
    }

    class EmergencyStatus {
        <<enumeration>>
        OPEN
        FULFILLED
        CANCELLED
        EXPIRED
    }

    class EmergencyUrgency {
        <<enumeration>>
        CRITICAL
        HIGH
        MEDIUM
        LOW
    }

    class ResponseStatus {
        <<enumeration>>
        ACCEPTED
        DECLINED
    }

    class MatchStatus {
        <<enumeration>>
        PENDING
        RESPONDED
        EXPIRED
    }

    class GDPRDeletionStatus {
        <<enumeration>>
        IN_PROGRESS
        CANCELED
        COMPLETED
    }

    class NotificationType {
        <<enumeration>>
        EMERGENCY_ALERT
        APPOINTMENT_REMINDER
        ELIGIBILITY_REMINDER
        PROFILE_COMPLETION
        EMAIL_VERIFICATION
        PASSWORD_RESET
        GENERAL
    }

    class NotificationStatus {
        <<enumeration>>
        PENDING
        SENT
        DELIVERED
        READ
        FAILED
    }

    class NotificationChannel {
        <<enumeration>>
        IN_APP
        PUSH
        EMAIL
    }

    User "1" --> "0..*" UserRole : has
    User "1" --> "0..*" Session : has
    User "1" --> "0..*" VerificationToken : has
    User "1" --> "0..1" DonorProfile : has
    User "1" --> "0..1" CenterStaffProfile : staff
    User "1" --> "0..1" CenterAdminProfile : admin
    User "1" --> "0..*" GDPRDeletionRequest : requests
    User "1" --> "0..*" AuditLog : generates
    User "1" --> "0..*" Notification : receives

    DonorProfile "1" --> "0..1" HealthQuestionnaire : has
    DonorProfile "1" --> "0..*" Appointment : books
    DonorProfile "1" --> "0..*" DonorResponse : responds
    DonorProfile "1" --> "0..*" MatchResult : matched_in

    DonationCenter "1" --> "0..*" Slot : offers
    DonationCenter "1" --> "0..*" CenterStaffProfile : employs
    DonationCenter "1" --> "0..*" CenterAdminProfile : managed_by
    DonationCenter "1" --> "0..*" Appointment : hosts
    DonationCenter "1" --> "0..*" EmergencyRequest : creates
    DonationCenter "1" --> "0..*" MatchResult : involved_in

    Slot "1" --> "0..*" Appointment : booked_in
    Slot "1" --> "0..*" DonorResponse : reserved_in

    Appointment "1" --> "0..1" HealthScreening : has
    Appointment "0..*" --> "0..1" EmergencyRequest : linked_to

    EmergencyRequest "1" --> "0..*" DonorResponse : receives
    EmergencyRequest "1" --> "0..*" MatchResult : generates

    CenterStaffProfile }o--|| User : belongs_to_user
    CenterStaffProfile }o--|| DonationCenter : belongs_to_center
    CenterAdminProfile }o--|| User : belongs_to_user
    CenterAdminProfile }o--|| DonationCenter : belongs_to_center

    UserRole --> Role
    User --> UserStatus
    DonorProfile --> BloodType
    DonorProfile --> DonorStatus
    DonorProfile --> AvailabilityStatus
    NotificationPreferences --> NotificationFrequency
    NotificationPreferences --> QuietHours
    OperatingHours --> DaySchedule
    OperatingHours --> ClosureWindow
    DonationCenter --> FacilityType
    DonationCenter --> CenterStatus
    Appointment --> AppointmentStatus
    Appointment --> AppointmentType
    Appointment --> BloodType
    Appointment --> DonationOutcome
    EmergencyRequest --> EmergencyStatus
    EmergencyRequest --> EmergencyUrgency
    EmergencyRequest --> BloodType
    DonorResponse --> ResponseStatus
    MatchResult --> MatchStatus
    MatchResult --> BloodType
    GDPRDeletionRequest --> GDPRDeletionStatus
    VerificationToken --> VerificationTokenType
    Notification --> NotificationType
    Notification --> NotificationStatus
    Notification --> NotificationChannel
    NotificationPayload --> NotificationType
    NotificationPayload --> NotificationChannel
```

## Domain Models Summary

| Module | Class | Type | Description |
|--------|-------|------|-------------|
| **User** | `User` | Aggregate Root | Core user account with status, roles, and lifecycle methods |
| | `UserRole` | Entity | Role assignment (SUPER_ADMIN, CENTER_ADMIN, CENTER_STAFF, DONOR) |
| | `Session` | Value Object | Active user session with token rotation and validation |
| | `VerificationToken` | Value Object | Email/password verification tokens with expiry |
| **Donor** | `DonorProfile` | Aggregate Root | Donor-specific data: blood type, location, availability, reliability |
| | `HealthQuestionnaire` | Entity | Pre-donation health screening questionnaire |
| | `NotificationPreferences` | Record | Donor notification settings (frequency, quiet hours, distance) |
| **Center** | `DonationCenter` | Aggregate Root | Blood donation center with capacity, hours, and status |
| | `Slot` | Value Object | Time slot with booking capacity management |
| | `CenterStaffProfile` | Entity | Staff assignment to a center |
| | `CenterAdminProfile` | Entity | Admin assignment to a center |
| | `OperatingHours` | Record | Weekly schedule with closure windows |
| **Appointment** | `Appointment` | Aggregate Root | Donation appointment with full lifecycle (schedule → screen → complete) |
| | `HealthScreening` | Entity | Pre-donation health check (weight, BP, hemoglobin, eligibility) |
| **Emergency** | `EmergencyRequest` | Aggregate Root | Emergency blood request with urgency, radius, and escalation |
| | `DonorResponse` | Entity | Donor accept/decline response to emergency |
| | `MatchResult` | Entity | Donor-emergency matching with radius and escalation level |
| **System** | `GDPRDeletionRequest` | Entity | GDPR data deletion request lifecycle |
| **Analytics** | `AuditLog` | Entity | System audit trail with old/new value snapshots |
| | `CenterMetrics` | Record | Aggregated center analytics |
| **Notification** | `Notification` | Aggregate Root | Multi-channel notification (IN_APP, PUSH, EMAIL) |
| | `NotificationPayload` | Record | Notification event payload |
