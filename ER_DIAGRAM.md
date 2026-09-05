# Qatra - Data Entity Relationship Diagram

```mermaid
erDiagram
    USERS {
        bigint id PK
        varchar email UK
        varchar phone UK
        varchar hashed_password
        varchar display_name
        varchar first_name
        varchar family_name
        varchar status
        boolean email_verified
        timestamp created_at
        timestamp last_active_at
        timestamp deleted_at
        timestamp deletion_requested_at
    }

    USER_ROLES {
        bigint id PK
        bigint user_id FK
        varchar role
        timestamp assigned_at
    }

    VERIFICATION_TOKENS {
        bigint id PK
        bigint user_id FK
        varchar token_hash
        varchar type
        timestamp expires_at
        timestamp created_at
    }

    SESSIONS {
        bigint id PK
        bigint user_id FK
        varchar access_token_hash
        varchar refresh_token_hash
        varchar ip_address
        varchar user_agent
        timestamp expires_at
        timestamp created_at
    }

    DONOR_PROFILES {
        bigint id PK
        bigint user_id FK UK
        varchar blood_type
        boolean blood_type_verified
        boolean profile_complete
        varchar status
        double latitude
        double longitude
        varchar city
        varchar availability
        date last_donation_date
        date eligible_from_date
        json notification_preferences
        boolean allow_emergency_notifications
        int consecutive_emergency_declines
        boolean flagged_for_manual_review
        boolean permanently_restricted
        text restriction_reason
        double reliability_score
        int total_donations
        timestamp created_at
        timestamp updated_at
        timestamp deleted_at
        timestamp deletion_requested_at
        timestamp last_accept_at
    }

    HEALTH_QUESTIONNAIRES {
        bigint id PK
        bigint donor_id FK UK
        boolean has_chronic_illness
        text medical_conditions_details
        boolean on_medication
        text medication_details
        timestamp last_surgery_at
        timestamp last_travel_at
        timestamp last_tattoo_or_piercing_at
        timestamp created_at
        timestamp updated_at
    }

    DONATION_CENTERS {
        bigint id PK
        varchar name UK
        varchar address
        varchar city
        varchar country
        varchar postal_code
        varchar phone
        varchar email
        double latitude
        double longitude
        varchar facility_type
        json operating_hours
        bigint created_by_user_id FK
        varchar status
        int total_capacity
        int max_regular
        int slot_period
        timestamp created_at
        timestamp updated_at
    }

    CENTER_STAFF_PROFILES {
        bigint id PK
        bigint user_id FK UK
        bigint center_id FK
        boolean is_verified
        timestamp created_at
    }

    CENTER_ADMIN_PROFILES {
        bigint id PK
        bigint user_id FK UK
        bigint center_id FK
        timestamp created_at
    }

    SLOTS {
        bigint id PK
        bigint center_id FK
        date date
        time start_time
        time end_time
        int max_bookings
        int max_regular_bookings
        int booked_count
        int regular_booked_count
        boolean is_blocked
        timestamp created_at
    }

    APPOINTMENTS {
        bigint id PK
        bigint donor_id FK
        bigint user_id
        bigint slot_id FK
        bigint center_id FK
        bigint emergency_id FK
        bigint completed_by_staff_id FK
        varchar appointment_type
        varchar status
        varchar blood_type
        varchar outcome
        int ml_collected
        varchar qr_code
        timestamp checked_in_at
        timestamp started_at
        timestamp completed_at
        timestamp cancelled_at
        varchar cancellation_reason
        text notes
        timestamp created_at
        timestamp updated_at
    }

    HEALTH_SCREENINGS {
        bigint id PK
        bigint appointment_id FK
        bigint donor_id FK
        bigint screened_by_staff_id FK
        double weight
        varchar blood_pressure
        double hemoglobin
        double temperature
        boolean eligible
        text notes
        timestamp screened_at
    }

    EMERGENCY_REQUESTS {
        bigint id PK
        bigint center_id FK
        bigint created_by_staff_id FK
        varchar blood_type
        int units_needed
        varchar urgency
        int match_radius
        int escalation_level
        varchar contact_phone
        varchar status
        timestamp resolved_at
        bigint resolved_by_user_id FK
        timestamp created_at
        timestamp updated_at
        timestamp expires_at
    }

    DONOR_RESPONSES {
        bigint id PK
        bigint emergency_id FK
        bigint donor_id FK
        bigint slot_id FK
        varchar status
        varchar reason
        timestamp responded_at
        timestamp created_at
    }

    MATCH_RESULTS {
        bigint id PK
        bigint emergency_id FK
        bigint center_id FK
        bigint donor_id FK
        bigint radius
        varchar blood_type
        int escalation_level
        varchar status
        timestamp responded_at
        timestamp created_at
    }

    GDPR_DELETION_REQUESTS {
        bigint id PK
        bigint user_id FK
        text reason
        varchar status
        timestamp requested_at
        timestamp processed_at
    }

    AUDIT_LOGS {
        bigint id PK
        bigint user_id FK
        varchar action
        varchar entity_type
        bigint entity_id
        json old_value
        json new_value
        varchar ip_address
        timestamp timestamp
    }

    DONATION_CERTIFICATES {
        bigint id PK
        bigint donor_id
        bigint appointment_id
        varchar donor_name
        varchar center_name
        bigint center_id
        int ml_collected
        date donation_date
        timestamp created_at
    }

    NOTIFICATIONS {
        bigint id PK
        bigint user_id
        varchar email
        bigint emergency_id
        bigint appointment_id
        varchar type
        varchar title
        text body
        json data
        varchar correlation_id
        text channels
        varchar status
        timestamp created_at
        timestamp sent_at
        timestamp read_at
    }

    USERS ||--o{ USER_ROLES : "has"
    USERS ||--o{ VERIFICATION_TOKENS : "has"
    USERS ||--o{ SESSIONS : "has"
    USERS ||--o| DONOR_PROFILES : "has"
    USERS ||--o| CENTER_STAFF_PROFILE : "has"
    USERS ||--o| CENTER_ADMIN_PROFILE : "has"
    USERS ||--o{ GDPR_DELETION_REQUESTS : "has"
    USERS ||--o{ AUDIT_LOGS : "generates"

    DONOR_PROFILES ||--o| HEALTH_QUESTIONNAIRES : "has"
    DONOR_PROFILES ||--o{ APPOINTMENTS : "books"
    DONOR_PROFILES ||--o{ HEALTH_SCREENINGS : "undergoes"
    DONOR_PROFILES ||--o{ DONOR_RESPONSES : "responds"
    DONOR_PROFILES ||--o{ MATCH_RESULTS : "matched_in"

    DONATION_CENTERS ||--o{ CENTER_STAFF_PROFILES : "employs"
    DONATION_CENTERS ||--o{ CENTER_ADMIN_PROFILES : "managed_by"
    DONATION_CENTERS ||--o{ SLOTS : "offers"
    DONATION_CENTERS ||--o{ APPOINTMENTS : "hosts"
    DONATION_CENTERS ||--o{ EMERGENCY_REQUESTS : "creates"
    DONATION_CENTERS ||--o{ MATCH_RESULTS : "involved_in"
    DONATION_CENTERS }o--|| USERS : "created_by"

    CENTER_STAFF_PROFILES }o--|| USERS : "belongs_to_user"
    CENTER_STAFF_PROFILES }o--|| DONATION_CENTERS : "belongs_to_center"
    CENTER_ADMIN_PROFILES }o--|| USERS : "belongs_to_user"
    CENTER_ADMIN_PROFILES }o--|| DONATION_CENTERS : "belongs_to_center"

    SLOTS }o--|| DONATION_CENTERS : "belongs_to"
    SLOTS ||--o{ APPOINTMENTS : "booked_in"
    SLOTS ||--o{ DONOR_RESPONSES : "reserved_in"

    APPOINTMENTS }o--|| DONOR_PROFILES : "belongs_to"
    APPOINTMENTS }o--|| SLOTS : "in_slot"
    APPOINTMENTS }o--|| DONATION_CENTERS : "at_center"
    APPOINTMENTS }o--o| EMERGENCY_REQUESTS : "linked_to"
    APPOINTMENTS }o--o| USERS : "completed_by"
    APPOINTMENTS ||--o| HEALTH_SCREENINGS : "has"

    HEALTH_SCREENINGS }o--|| APPOINTMENTS : "for_appointment"
    HEALTH_SCREENINGS }o--|| DONOR_PROFILES : "for_donor"
    HEALTH_SCREENINGS }o--|| USERS : "screened_by"

    EMERGENCY_REQUESTS }o--|| DONATION_CENTERS : "at_center"
    EMERGENCY_REQUESTS }o--|| USERS : "created_by_staff"
    EMERGENCY_REQUESTS }o--o| USERS : "resolved_by"
    EMERGENCY_REQUESTS ||--o{ DONOR_RESPONSES : "receives"
    EMERGENCY_REQUESTS ||--o{ MATCH_RESULTS : "generates"

    DONOR_RESPONSES }o--|| EMERGENCY_REQUESTS : "for_emergency"
    DONOR_RESPONSES }o--|| DONOR_PROFILES : "from_donor"
    DONOR_RESPONSES }o--o| SLOTS : "for_slot"

    MATCH_RESULTS }o--|| EMERGENCY_REQUESTS : "for_emergency"
    MATCH_RESULTS }o--|| DONATION_CENTERS : "at_center"
    MATCH_RESULTS }o--|| DONOR_PROFILES : "for_donor"

    GDPR_DELETION_REQUESTS }o--|| USERS : "for_user"
    AUDIT_LOGS }o--o| USERS : "by_user"
```

## Relationship Summary

| Relationship | Type | Description |
|---|---|---|
| User → UserRoles | 1:N | A user can have multiple roles |
| User → VerificationTokens | 1:N | A user can have multiple verification tokens |
| User → Sessions | 1:N | A user can have multiple active sessions |
| User → DonorProfile | 1:1 | A user can have one donor profile |
| User → CenterStaffProfile | 1:1 | A user can be staff at one center |
| User → CenterAdminProfile | 1:1 | A user can be admin at one center |
| DonorProfile → HealthQuestionnaire | 1:1 | Each donor has one health questionnaire |
| Center → Slots | 1:N | A center offers multiple time slots |
| Center → Appointments | 1:N | A center hosts multiple appointments |
| Center → EmergencyRequests | 1:N | A center can have multiple emergency requests |
| DonorProfile → Appointments | 1:N | A donor can book multiple appointments |
| Slot → Appointments | 1:N | A slot can have multiple appointments |
| EmergencyRequest → DonorResponses | 1:N | An emergency can receive multiple donor responses |
| EmergencyRequest → MatchResults | 1:N | An emergency generates multiple match results |
| Appointment → HealthScreening | 1:1 | An appointment has one health screening |
