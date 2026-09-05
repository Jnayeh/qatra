# USE CASES:
````

1. DONOR
Authentication & Onboarding
UC-D01 Register with email or phone

UC-D02 Verify email address

UC-D03 Login

UC-D04 Logout

UC-D05 Reset password

UC-D06 Complete health profile & questionnaire

UC-D07 Set blood type (or mark unknown)

UC-D08 Set notification preferences (including quiet hours via JSON)

UC-D09 Set location (manual or GPS)

UC-D10 Set availability status

UC-D11 Request account deletion (GDPR)

Profile Management
UC-D12 View donor dashboard

UC-D13 Update personal information

UC-D14 Update health questionnaire

UC-D15 Update location & availability

UC-D16 View eligibility status & next eligible date

UC-D17 View reliability score

UC-D18 View impact dashboard (lives saved, milestones)

UC-D19 Download donation certificates

Emergency Response
UC-D20 Receive emergency notification

UC-D21 View emergency details (center, distance, blood type, urgency)

UC-D22 Accept emergency request

UC-D23 Decline emergency request (with optional reason)

Appointment Management
UC-D24 Browse nearby donation centers

UC-D25 View center details and operating hours

UC-D26 Schedule appointment (regular or emergency)

UC-D27 Reschedule or cancel appointment

UC-D28 Check in at donation center (QR scan → staff views appointment details, or staff selects from list)

UC-D29 View appointment & donation history

UC-D30 Receive appointment & eligibility reminders

2. CENTER STAFF
Authentication
UC-CS01 Login, logout & reset password

Emergency Management
UC-CS02 Create emergency (blood type, units, urgency, deadline)

UC-CS03 Track emergency progress (escalate, extend, cancel)

UC-CS04 Resolve emergency

UC-CS05 View emergency history

Appointment & Donor Operations
UC-CS06 View daily schedule & own task queue

UC-CS07 Check in donor on arrival (QR scan shows appointment details; otherwise select from list)

UC-CS08 View donor eligibility & health profile

UC-CS09 Conduct & record health screening

UC-CS10 Complete appointment (record ml collected)

UC-CS11 Mark donor as no-show

UC-CS12 Send message to donor (instructions, follow-up)

3. CENTER ADMIN
Authentication
UC-CA01 Login, logout & reset password

Center Management
UC-CA02 Submit center registration request (approved by super admin)

UC-CA03 Update center info (address, hours, contact)

UC-CA04 Configure capacity & appointment slots

UC-CA05 Set special closures & operating hour exceptions

Staff Management
UC-CA06 Add or remove center staff

UC-CA07 View staff activity log

Reporting & Oversight
UC-CA08 View daily & weekly schedule (oversight)

UC-CA09 View analytics (trends, peak hours, blood type inventory)

UC-CA10 Generate & export center report

4. SUPER ADMIN
Authentication
UC-SA01 Login, logout & reset password

User Management
UC-SA02 View, search & filter all users

UC-SA03 Activate, suspend or delete accounts

UC-SA04 Approve or reject center registration requests

UC-SA05 Assign & revoke roles

UC-SA06 Process GDPR data deletion requests

UC-SA07 Review & override permanent health restrictions


Monitoring & Reporting
UC-SA10 View system dashboard (emergencies, response rates, donor stats)

UC-SA11 Monitor system health (services, error logs, API usage)

UC-SA12 View & export audit logs

UC-SA13 Generate platform reports

5. SYSTEM (Automated)
Authentication & Sessions
UC-SYS01 Issue, refresh & expire sessions

UC-SYS02 Issue & expire verification tokens

Matching Engine
UC-SYS03 Scan & filter eligible donors on emergency creation

UC-SYS04 Score & rank matched donors (reliability, distance, history)

UC-SYS05 Execute tiered matching & escalate radius if needed

Notification Engine
UC-SYS06 Send emergency alert (channel routing, quiet hours, frequency limits)

UC-SYS07 Track delivery & retry failed notifications

UC-SYS08 Send appointment, eligibility & profile nudge reminders

Eligibility Management
UC-SYS09 Mark ineligible after donation & set eligible-from date

UC-SYS10 Auto-restore eligibility when cooldown passes

UC-SYS11 Flag permanent restrictions from health questionnaire

Reliability Scoring
UC-SYS12 Adjust reliability score (0–100) on donation completed, no-show, or repeated emergency declines while available
UC-SYS16 Forecast blood type demand by center and by region,
UC-SYS17 Generate audit log on every state-changing action
Three consecutive declines with no accept in between → soft flag for manual review

Accept resets the consecutive decline counter

Declining alone carries no penalty

Slot Management
UC-SYS13 Release, block or free slots automatically

UC-SYS14 Expire unresolved emergencies past deadline

