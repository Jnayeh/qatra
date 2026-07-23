INSERT INTO users (id, email, phone, hashed_password, display_name, status, email_verified, created_at, first_name, family_name) VALUES
  (0, 'system@qatra.app', '0000000000', '$2a$10$placeholder', 'System', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'System', 'User'),
  (1, 'donor1@test.com', '0000000001', '$2a$10$placeholder', 'Donor One', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'One'),
  (2, 'donor2@test.com', '0000000002', '$2a$10$placeholder', 'Donor Two', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Two'),
  (3, 'donor3@test.com', '0000000003', '$2a$10$placeholder', 'Donor Three', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Three'),
  (4, 'donor4@test.com', '0000000004', '$2a$10$placeholder', 'Donor Four', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Four'),
  (5, 'donor5@test.com', '0000000005', '$2a$10$placeholder', 'Donor Five', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Five'),
  (6, 'donor6@test.com', '0000000006', '$2a$10$placeholder', 'Donor Six', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Six'),
  (7, 'donor7@test.com', '0000000007', '$2a$10$placeholder', 'Donor Seven', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Seven'),
  (8, 'donor8@test.com', '0000000008', '$2a$10$placeholder', 'Donor Eight', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Eight'),
  (9, 'donor9@test.com', '0000000009', '$2a$10$placeholder', 'Donor Nine', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Nine'),
  (10, 'donor10@test.com', '0000000010', '$2a$10$placeholder', 'Donor Ten', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Ten'),
  (11, 'donor11@test.com', '0000000011', '$2a$10$placeholder', 'Donor Eleven', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Eleven'),
  (12, 'donor12@test.com', '0000000012', '$2a$10$placeholder', 'Donor Twelve', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Donor', 'Twelve'),
  (99, 'admin@test.com', '9900000099', '$2a$10$placeholder', 'Admin', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Admin', 'User'),
  (999999, 'staff@test.com', '9999999999', '$2a$10$placeholder', 'Staff', 'ACTIVE', TRUE, CURRENT_TIMESTAMP, 'Staff', 'User');

INSERT INTO donor_profiles (id, user_id, blood_type, blood_type_verified, profile_complete, status, latitude, longitude, availability, allow_emergency_notifications, consecutive_emergency_declines, flagged_for_manual_review, permanently_restricted, reliability_score, total_donations, created_at, updated_at)
SELECT u.id, u.id, 'UNKNOWN', FALSE, FALSE, 'ACTIVE', 0.0, 0.0, 'AVAILABLE', TRUE, 0, FALSE, FALSE, 1.0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM users u WHERE u.id BETWEEN 1 AND 12;

ALTER TABLE users ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE donor_profiles ALTER COLUMN id RESTART WITH 1000;
