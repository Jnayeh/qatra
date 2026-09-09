ALTER TABLE donation_certificates ADD COLUMN donation_type VARCHAR(255);

UPDATE donation_certificates dc
SET donation_type = a.appointment_type
FROM appointments a
WHERE a.id = dc.appointment_id;
