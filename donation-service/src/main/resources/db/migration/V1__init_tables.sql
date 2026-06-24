create sequence appointment_seq start with 1 increment by 1
create sequence audit_log_seq start with 1 increment by 1
create sequence center_admin_profile_seq start with 1 increment by 1
create sequence center_seq start with 1 increment by 1
create sequence center_staff_profile_seq start with 1 increment by 1
create sequence donation_certificate_seq start with 1 increment by 1
create sequence donor_profile_seq start with 2 increment by 2
create sequence donor_response_seq start with 1 increment by 1
create sequence emergency_request_seq start with 1 increment by 1
create sequence gdpr_deletion_request_seq start with 1 increment by 1
create sequence health_questionnaire_seq start with 1 increment by 1
create sequence health_screening_seq start with 1 increment by 1
create sequence match_result_seq start with 1 increment by 1
create sequence session_seq start with 1 increment by 1
create sequence slot_seq start with 1 increment by 1
create sequence user_role_seq start with 1 increment by 1
create sequence user_seq start with 1 increment by 2
create sequence verification_token_seq start with 1 increment by 1


create table appointments (ml_collected integer, cancelled_at timestamp(6) with time zone, center_id bigint not null, checked_in_at timestamp(6) with time zone, completed_at timestamp(6) with time zone, completed_by_staff_id bigint, created_at timestamp(6) with time zone not null, donor_id bigint not null, emergency_id bigint, id bigint not null, slot_id bigint not null, started_at timestamp(6) with time zone, updated_at timestamp(6) with time zone not null, user_id bigint, appointment_type varchar(255) not null check ((appointment_type in ('REGULAR','EMERGENCY'))), blood_type varchar(255) check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))), cancellation_reason varchar(255), notes TEXT, outcome varchar(255) check ((outcome in ('COMPLETED','CANCELLED'))), qr_code varchar(255), status varchar(255) not null check ((status in ('SCHEDULED','CHECKED_IN','IN_SCREENING','COMPLETED','CANCELLED','NO_SHOW','RESCHEDULED'))), primary key (id))

create table audit_logs (entity_id bigint, id bigint not null, timestamp timestamp(6) with time zone not null, user_id bigint, action varchar(255) not null, entity_type varchar(255), ip_address varchar(255), new_value jsonb, old_value jsonb, primary key (id))

create table center_admin_profiles (center_id bigint not null, created_at timestamp(6) with time zone not null, id bigint not null, user_id bigint not null unique, primary key (id))

create table center_staff_profiles (is_verified boolean not null, center_id bigint not null, created_at timestamp(6) with time zone not null, id bigint not null, user_id bigint not null unique, primary key (id))

create table donation_centers (latitude float(53), longitude float(53), max_regular integer, slot_period integer, total_capacity integer, created_at timestamp(6) with time zone not null, created_by_user_id bigint, id bigint not null, updated_at timestamp(6) with time zone not null, address varchar(255) not null, city varchar(255) not null, country varchar(255) not null, email varchar(255) not null, facility_type varchar(255) not null check ((facility_type in ('BLOOD_BANK','HOSPITAL','CLINIC','MOBILE_UNIT','COMMUNITY_CENTER'))), name varchar(255) not null unique, phone varchar(255) not null, postal_code varchar(255), status varchar(255) not null check ((status in ('PENDING_APPROVAL','ACTIVE','SUSPENDED','CLOSED'))), operating_hours jsonb, primary key (id))

create table donation_certificates (donation_date date not null, ml_collected integer, appointment_id bigint not null, center_id bigint not null, created_at timestamp(6) with time zone not null, donor_id bigint not null, id bigint not null, center_name varchar(255) not null, donor_name varchar(255) not null, primary key (id))

create table donor_profiles (allow_emergency_notifications boolean not null, blood_type_verified boolean not null, consecutive_emergency_declines integer not null, eligible_from_date date, flagged_for_manual_review boolean not null, last_donation_date date, latitude float(53), longitude float(53), permanently_restricted boolean not null, profile_complete boolean not null, reliability_score float(53) not null, total_donations integer not null, created_at timestamp(6) with time zone not null, deleted_at timestamp(6) with time zone, deletion_requested_at timestamp(6) with time zone, id bigint not null, last_accept_at timestamp(6) with time zone, updated_at timestamp(6) with time zone not null, user_id bigint not null unique, availability varchar(255) check ((availability in ('AVAILABLE','TEMPORARILY_UNAVAILABLE','VACATION_MODE','PERMANENTLY_RESTRICTED'))), blood_type varchar(255) check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))), city varchar(255), restriction_reason varchar(255), status varchar(255) check ((status in ('ACTIVE','INACTIVE','PENDING_DELETION','DELETED'))), notification_preferences jsonb, primary key (id))

create table donor_responses (created_at timestamp(6) with time zone not null, donor_id bigint not null, emergency_id bigint not null, id bigint not null, responded_at timestamp(6) with time zone, slot_id bigint, reason varchar(255), status varchar(255) not null check ((status in ('ACCEPTED','DECLINED'))), primary key (id))

create table emergency_requests (escalation_level integer not null, match_radius integer not null, units_needed integer not null, center_id bigint not null, created_at timestamp(6) with time zone not null, created_by_staff_id bigint not null, expires_at timestamp(6) with time zone, id bigint not null, resolved_at timestamp(6) with time zone, resolved_by_user_id bigint, updated_at timestamp(6) with time zone not null, blood_type varchar(255) not null check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))), contact_phone varchar(255), status varchar(255) not null check ((status in ('OPEN','FULFILLED','CANCELLED','EXPIRED'))), urgency varchar(255) not null check ((urgency in ('CRITICAL','HIGH','MEDIUM','LOW'))), primary key (id))

create table gdpr_deletion_requests (id bigint not null, processed_at timestamp(6) with time zone, requested_at timestamp(6) with time zone not null, user_id bigint not null, reason TEXT, status varchar(255) not null check ((status in ('IN_PROGRESS','CANCELED','COMPLETED'))), primary key (id))

create table health_questionnaires (has_chronic_illness boolean not null, on_medication boolean not null, created_at timestamp(6) with time zone not null, donor_id bigint not null unique, id bigint not null, last_surgery_at timestamp(6) with time zone, last_tattoo_or_piercing_at timestamp(6) with time zone, last_travel_at timestamp(6) with time zone, updated_at timestamp(6) with time zone not null, medical_conditions_details TEXT, medication_details TEXT, primary key (id))

create table health_screenings (eligible boolean not null, hemoglobin float(53), temperature float(53), weight float(53), appointment_id bigint not null, donor_id bigint not null, id bigint not null, screened_at timestamp(6) with time zone not null, screened_by_staff_id bigint not null, blood_pressure varchar(255), notes TEXT, primary key (id))

create table match_results (escalation_level integer not null, center_id bigint not null, created_at timestamp(6) with time zone not null, donor_id bigint not null, emergency_id bigint not null, id bigint not null, radius bigint not null, responded_at timestamp(6) with time zone, blood_type varchar(255) not null check ((blood_type in ('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE','UNKNOWN'))), status varchar(255) not null check ((status in ('PENDING','RESPONDED','EXPIRED'))), primary key (id))

create table sessions (created_at timestamp(6) with time zone not null, expires_at timestamp(6) with time zone not null, id bigint not null, user_id bigint not null, access_token_hash varchar(255) not null, ip_address varchar(255), refresh_token_hash varchar(255) not null, user_agent varchar(255), primary key (id))

create table slots (booked_count integer not null, date date not null, end_time time(0) not null, is_blocked boolean not null, max_bookings integer not null, max_regular_bookings integer not null, regular_booked_count integer not null, start_time time(0) not null, center_id bigint not null, created_at timestamp(6) with time zone not null, id bigint not null, primary key (id))

create table user_roles (assigned_at timestamp(6) with time zone not null, id bigint not null, user_id bigint not null, role varchar(255) not null check ((role in ('SUPER_ADMIN','CENTER_ADMIN','CENTER_STAFF','DONOR'))), primary key (id), unique (user_id, role))

create table users (email_verified boolean not null, created_at timestamp(6) with time zone not null, deleted_at timestamp(6) with time zone, deletion_requested_at timestamp(6) with time zone, id bigint not null, last_active_at timestamp(6) with time zone, display_name varchar(255) not null, email varchar(255) not null unique, family_name varchar(255), first_name varchar(255), hashed_password varchar(255) not null, phone varchar(255) not null unique, status varchar(255) not null check ((status in ('ACTIVE','INACTIVE','SUSPENDED','PENDING_VERIFICATION','PENDING_DELETION','DELETED'))), primary key (id))

create table verification_tokens (created_at timestamp(6) with time zone not null, expires_at timestamp(6) with time zone not null, id bigint not null, user_id bigint not null, token_hash varchar(255) not null, type varchar(255) not null check ((type in ('EMAIL_VERIFICATION','PASSWORD_RESET'))), primary key (id))


alter table if exists appointments add constraint FK8t6o6t5vjcc3uvifx86tn85ag foreign key (center_id) references donation_centers
alter table if exists appointments add constraint FKmh6grfeyubkv8qrjctiyesbvo foreign key (completed_by_staff_id) references users
alter table if exists appointments add constraint FK40kk6n2tjic65mfoti09s0e3h foreign key (donor_id) references donor_profiles
alter table if exists appointments add constraint FKbw429fhc3acws3demr0tubixy foreign key (emergency_id) references emergency_requests
alter table if exists appointments add constraint FKf8qrv9g386dae81yfkj1qgs77 foreign key (slot_id) references slots
alter table if exists audit_logs add constraint FKjs4iimve3y0xssbtve5ysyef0 foreign key (user_id) references users
alter table if exists center_admin_profiles add constraint FK3unair5cbae9fcdvffird0r5b foreign key (center_id) references donation_centers
alter table if exists center_admin_profiles add constraint FK2ie2ngbkls549c89j6t3xkks1 foreign key (user_id) references users
alter table if exists center_staff_profiles add constraint FK3kay45pxiu20mnc5llaet9en4 foreign key (center_id) references donation_centers
alter table if exists center_staff_profiles add constraint FK7op1e785odjk73vsh5tgsytq6 foreign key (user_id) references users
alter table if exists donation_centers add constraint FK6yvqj61il694ngcysknqj6b5 foreign key (created_by_user_id) references users
alter table if exists donor_profiles add constraint FK7h7kum7l387kv5d3o4yh3oyow foreign key (user_id) references users
alter table if exists donor_responses add constraint FKgecvahm07s7rkbkx2pg8qcoij foreign key (donor_id) references donor_profiles
alter table if exists donor_responses add constraint FKeehm5xbo3qc2qvjqfx73g3i3r foreign key (emergency_id) references emergency_requests
alter table if exists donor_responses add constraint FKbwybtjwfypg7tuguclm38agy5 foreign key (slot_id) references slots
alter table if exists emergency_requests add constraint FKntewm05gfckwf5pycr90fumom foreign key (center_id) references donation_centers
alter table if exists emergency_requests add constraint FKjlrdxs6d0yiqh69aqaee8c5vb foreign key (created_by_staff_id) references users
alter table if exists emergency_requests add constraint FKpv2453x026e5ee2bxsster4m0 foreign key (resolved_by_user_id) references users
alter table if exists gdpr_deletion_requests add constraint FKghl6ed4uf8mvpeuf7ne2x8r6s foreign key (user_id) references users
alter table if exists health_questionnaires add constraint FK35ank7o4ndsgn8uasq91c35v foreign key (donor_id) references donor_profiles
alter table if exists health_screenings add constraint FKqimkii6yl73wl65fjo9x9nurr foreign key (appointment_id) references appointments
alter table if exists health_screenings add constraint FKeggvph28i357upas6nrrggdby foreign key (donor_id) references donor_profiles
alter table if exists health_screenings add constraint FKhvlm8wduedhh358n6bae72su4 foreign key (screened_by_staff_id) references users
alter table if exists match_results add constraint FK3lve29d2fda04rwn85n9fqhwu foreign key (center_id) references donation_centers
alter table if exists match_results add constraint FKly5smiundxf050odbu2a57j6u foreign key (donor_id) references donor_profiles
alter table if exists match_results add constraint FK9dsts9wvoio2ctiq6l1aew7gc foreign key (emergency_id) references emergency_requests
alter table if exists sessions add constraint FKruie73rneumyyd1bgo6qw8vjt foreign key (user_id) references users
alter table if exists slots add constraint FKb7nnlkyh9mgb1jhklyu58nptp foreign key (center_id) references donation_centers
alter table if exists user_roles add constraint FKhfh9dx7w3ubf1co1vdev94g3f foreign key (user_id) references users
alter table if exists verification_tokens add constraint FK54y8mqsnq1rtyf581sfmrbp4f foreign key (user_id) references users