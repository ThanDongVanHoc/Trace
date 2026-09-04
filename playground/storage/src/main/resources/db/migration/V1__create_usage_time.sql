-- V1: usage-time vectors (one row per usage event / sighting).
-- Angles are radians in [0, 2*PI), computed in UTC from sighting.detected_at
-- (epoch millis). daytime_angle = 2*PI * millisOfDay / 86_400_000,
-- weekday_angle = 2*PI * dayOfWeek / 7 with Sunday = 0 (SQLite %w).
CREATE TABLE IF NOT EXISTS usage_time (
    id               TEXT    PRIMARY KEY,                -- = sightings.id (1:1 event)
    daytime_angle    REAL    NOT NULL,                   -- radians [0, 2*PI)
    weekday_angle    REAL    NOT NULL,                   -- radians [0, 2*PI)
    confidence_score REAL    NOT NULL,
    object_id        TEXT    NOT NULL REFERENCES objects(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_usage_time_object  ON usage_time(object_id);
CREATE INDEX IF NOT EXISTS idx_usage_time_daytime ON usage_time(daytime_angle);

-- Backfill historical sightings (idempotent: natural key = sighting id).
INSERT OR IGNORE INTO usage_time (id, daytime_angle, weekday_angle, confidence_score, object_id)
SELECT
    s.id,
    (CAST(s.detected_at % 86400000 AS REAL) / 86400000.0) * 6.283185307179586,
    (CAST(strftime('%w', s.detected_at / 1000, 'unixepoch') AS REAL) / 7.0) * 6.283185307179586,
    s.confidence,
    s.object_id
FROM sightings s;
