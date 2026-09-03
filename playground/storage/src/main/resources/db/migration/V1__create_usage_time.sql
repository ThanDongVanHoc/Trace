CREATE TABLE IF NOT EXISTS usage_time (
    id TEXT PRIMARY KEY REFERENCES sightings(id) ON DELETE CASCADE,
    daytime_angle REAL NOT NULL,
    weekday_angle REAL NOT NULL,
    confidence_score REAL NOT NULL,
    object_id TEXT NOT NULL REFERENCES objects(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_usage_time_object_id
ON usage_time(object_id);

CREATE INDEX IF NOT EXISTS idx_usage_time_periodic
ON usage_time(daytime_angle, weekday_angle);
