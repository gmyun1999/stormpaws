-- CREATE TABLE IF NOT EXISTS cities (
--     id VARCHAR(255) PRIMARY KEY,
--     city VARCHAR(255) NOT NULL,
--     latitude DOUBLE PRECISION NOT NULL,
--     longitude DOUBLE PRECISION NOT NULL
-- );

-- CREATE TABLE IF NOT EXISTS weather_log_model (
--     id VARCHAR(36) PRIMARY KEY,
--     city VARCHAR(255) NOT NULL,
--     weather_type VARCHAR(255) NOT NULL,
--     fetched_at TIMESTAMP NOT NULL
-- );

-- CREATE INDEX IF NOT EXISTS idx_city_fetched_at ON weather_log_model (city, fetched_at);