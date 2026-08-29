CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  email varchar(320) NOT NULL UNIQUE,
  password_hash text NOT NULL,
  display_name varchar(80) NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE sessions (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  refresh_token_hash text NOT NULL,
  expires_at timestamptz NOT NULL,
  revoked_at timestamptz,
  last_used_at timestamptz,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_user_id ON sessions(user_id);

CREATE TYPE device_platform AS ENUM ('android', 'ios');
CREATE TABLE devices (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  installation_id varchar(100) NOT NULL,
  platform device_platform NOT NULL,
  push_token text,
  locale varchar(20) NOT NULL DEFAULT 'vi',
  notifications_enabled boolean NOT NULL DEFAULT true,
  last_seen_at timestamptz NOT NULL,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT uq_devices_user_installation UNIQUE(user_id, installation_id)
);

CREATE TABLE trace_objects (
  id uuid PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  tag varchar(80) NOT NULL,
  reference_revision integer NOT NULL DEFAULT 1,
  version integer NOT NULL DEFAULT 1,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  deleted_at timestamptz
);
CREATE INDEX idx_trace_objects_user_updated ON trace_objects(user_id, updated_at DESC);

CREATE TABLE sightings (
  id uuid PRIMARY KEY,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  object_id uuid NOT NULL REFERENCES trace_objects(id) ON DELETE CASCADE,
  detected_at timestamptz NOT NULL,
  latitude double precision,
  longitude double precision,
  accuracy_meters real,
  confidence real NOT NULL CHECK (confidence >= 0 AND confidence <= 1),
  evidence_asset_id uuid,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_sightings_object_time ON sightings(object_id, detected_at DESC);
CREATE INDEX idx_sightings_user_time ON sightings(user_id, detected_at DESC);

CREATE TYPE notification_status AS ENUM ('pending', 'sent', 'failed');
CREATE TABLE notification_outbox (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type varchar(50) NOT NULL,
  payload jsonb NOT NULL,
  status notification_status NOT NULL DEFAULT 'pending',
  scheduled_at timestamptz NOT NULL,
  attempts integer NOT NULL DEFAULT 0,
  last_error text,
  created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_notification_outbox_dispatch
  ON notification_outbox(status, scheduled_at);
