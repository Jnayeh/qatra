# Use Cases Gap Analysis

Cross-referencing **Use Cases**, **Class Diagram**, and **User Flows** in `instructions-diagram-check.md`.

---

## A. Missing Use Case Numbers

| Gap | Details |
|-----|---------|
| **UC-CA08** | Jumps from CA07 → CA09. Undefined. |
| **UC-SA08** | Jumps from SA07 → SA10. Undefined. |
| **UC-SA09** | Same gap. Undefined. |

---

## B. Class Diagram vs Use Cases

### Entities in class diagram NOT covered by any use case

| Entity | Notes |
|--------|-------|
| `DaySchedule` | Sub-struct of `OperatingHours`. No UC explicitly describes creating/editing day schedules. |

### Entities referenced in use cases/flows but MISSING from class diagram

| Entity | Required By |
|--------|-------------|
| **`MetricsSnapshot`** | UC-SA10, UC-SYS15, System Audit flow, Super Admin Monitoring flow |
| **`DemandForecast`** | UC-SYS16, System Audit flow |
| **`Report`** | UC-CA10, UC-SA13 |

---

## C. Use Cases with No Corresponding Flow

| UC | Description |
|----|-------------|
| **UC-CS05** | View emergency history |
| **UC-CS06** | View daily schedule & own task queue |
| **UC-CA06** | Add or remove center staff (mentioned in passing, no dedicated flow) |
| **UC-CA09** | View analytics (trends, peak hours, blood type inventory) |
| **UC-CA10** | Generate & export center report |
| **UC-SA11** | Monitor system health (one-liner in flow, no substantive detail) |
| **UC-SYS07** | Track delivery & retry failed notifications |
| **UC-D25** | View center details and operating hours |
| **UC-D29** | View appointment & donation history |

### Flows referencing incomplete paths

| UC | Issue |
|----|-------|
| **UC-SA04** | Center Registration flow only covers **approve**. The **reject** path is missing. |

---

## D. Flows Referencing Entities Not in Class Diagram

| Entity | Flow |
|--------|------|
| `MetricsSnapshot` | System Audit & Health Flow, Super Admin System Monitoring Flow |
| `DemandForecast` | System Audit & Health Flow |

---

## E. Missing Relationships in Class Diagram

| Relationship | Evidence |
|--------------|----------|
| **`EmergencyRequest` ↔ `DonorResponse`** | `DonorResponse.emergencyId` exists but no relationship line. Critical gap. |
| **`Appointment` ↔ `Notification`** | `Notification.appointmentId` exists but no relationship line. Required for appointment reminders. |
| **`DonorResponse` ↔ `Slot`** | `DonorResponse.slotId` exists but no relationship line. |
| **`MatchResult` ↔ `DonationCenter`** | `MatchResult.centerId` exists but no relationship line. |
| **`DonorResponse` ↔ `Appointment`** | Accepting creates an Appointment, but no link from response to result. |

---

## F. Missing Notification Types

| Missing Type | Required By |
|-------------|-------------|
| `CENTER_REGISTRATION_ALERT` | UC-SA04 — notify super admin of new registration |
| `PERMANENT_RESTRICTION_ALERT` | Permanent Health Restriction Flow — notify admin team |
| `ACCOUNT_DELETION_ALERT` | UC-SA06 / GDPR flow — notify super admin |
| `DONOR_RESPONSE_SUMMARY` | Emergency Escalation Flow — staff monitoring |

---

## G. Flows with No Capturing Use Case

| Flow | Issue |
|------|-------|
| **Emergency Slot Allocation Flow** | Searches nearby centers, alerts staff for walk-in. No single UC captures this. |
| **Emergency Fulfillment Tracking Flow** | Auto-sums mlCollected, converts to units, auto-resolves. UC-CS04 only covers manual resolve. |
| **Failed Health Screening Flow** | Auto-cancels appointment, releases slot, logs deferral. UC-CS09 only covers recording screening. |

---

## H. Incomplete Enums

| Enum | Missing Value | Evidence |
|------|--------------|----------|
| `DonationOutcome` | `DEFERRED` | Failed Health Screening Flow describes deferral; only COMPLETED/CANCELLED exist. |
| `MatchStatus` | `ACCEPTED`, `DECLINED` | DonorResponse determines outcome but MatchResult stays `RESPONDED`. |
| `EmergencyStatus` | `PARTIALLY_FULFILLED` | Escalation flow implies in-progress state between OPEN and FULFILLED. |
| `NotificationType` | (see section F) | Multiple types missing. |
| `GDPRDeletionStatus` | `PENDING_REVIEW` | Super admin "processes" requests but no review state exists. |
| `AppointmentStatus` | `DEFERRED` | Failed screening flow mentions deferral but no status for it. |

---

## I. Missing Methods on Classes

| Method | Class | Referenced By |
|--------|-------|---------------|
| `updateAvailability()` | `DonorProfile` | Donor Profile & Settings Update Flow |
| `addStaff()` / `removeStaff()` | `DonationCenter` | UC-CA06, Center Registration Flow |
| `updateInfo()` | `DonationCenter` | UC-CA03, Center Admin Ongoing Management Flow |
| `block()` / `release()` | `Slot` | UC-SYS13, multiple flows |
| `clearEligibility()` | `DonorProfile` | Automated Eligibility Restoration Flow |
| `escalate()` | `EmergencyRequest` | Emergency Escalation Flow |
| `cancel()` | `DonorResponse` | Manual Emergency Management Flow |

---

## Bonus: Structural Issues

| Issue | Location |
|-------|----------|
| **Copy-paste error** | Authentication & Session Management Flow (line 765) starts with GDPR cancellation text duplicated from line 755. |
| **UC-SA04 reject path** | Center Registration flow only covers approval, not rejection. |
