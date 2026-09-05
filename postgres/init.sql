DO $$ BEGIN
  CREATE USER qatra_notification WITH PASSWORD 'qatra_notification';
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE DATABASE qatra_notification OWNER qatra_notification;
