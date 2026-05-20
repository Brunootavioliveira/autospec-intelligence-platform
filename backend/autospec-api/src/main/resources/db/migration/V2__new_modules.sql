-- 1. USER SESSIONS

CREATE TABLE IF NOT EXISTS user_sessions (
                                             id             BIGSERIAL       PRIMARY KEY,
                                             user_id        BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_token  VARCHAR(255)    NOT NULL UNIQUE,
    device_info    VARCHAR(100),
    ip_address     VARCHAR(50),
    browser_app    VARCHAR(100),
    created_at     TIMESTAMP       NOT NULL DEFAULT NOW(),
    last_active    TIMESTAMP       NOT NULL DEFAULT NOW(),
    active         BOOLEAN         NOT NULL DEFAULT TRUE
    );

CREATE INDEX IF NOT EXISTS idx_user_sessions_user_active
    ON user_sessions (user_id, active);

CREATE INDEX IF NOT EXISTS idx_user_sessions_token
    ON user_sessions (session_token);


-- 2. GARAGE VEHICLES

CREATE TABLE IF NOT EXISTS garage_vehicles (
                                               id               BIGSERIAL       PRIMARY KEY,
                                               user_id          BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_spec_id  BIGINT          NOT NULL REFERENCES vehicle_specs(id) ON DELETE CASCADE,
    fleet_type       VARCHAR(20)     NOT NULL CHECK (fleet_type IN ('PERSONAL', 'WORK')),
    nickname         VARCHAR(255),
    active           BOOLEAN         NOT NULL DEFAULT TRUE,

    created_by           VARCHAR(255),
    created_at           TIMESTAMP,
    last_modified_by     VARCHAR(255),
    last_modified_date   TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_garage_vehicles_user_active
    ON garage_vehicles (user_id, active);

CREATE INDEX IF NOT EXISTS idx_garage_vehicles_fleet
    ON garage_vehicles (user_id, fleet_type, active);

CREATE UNIQUE INDEX IF NOT EXISTS uq_garage_user_spec_active
    ON garage_vehicles (user_id, vehicle_spec_id)
    WHERE active = TRUE;


-- 3. USER HISTORY

CREATE TABLE IF NOT EXISTS user_history (
                                            id           BIGSERIAL       PRIMARY KEY,
                                            user_id      BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type  VARCHAR(30)     NOT NULL CHECK (action_type IN ('ANALYSIS', 'COMPARISON', 'SERVICE_RECORD')),
    title        VARCHAR(255)    NOT NULL,
    description  VARCHAR(500),
    reference_id BIGINT,
    created_at   TIMESTAMP       NOT NULL DEFAULT NOW(),
    deleted      BOOLEAN         NOT NULL DEFAULT FALSE
    );

CREATE INDEX IF NOT EXISTS idx_user_history_user_deleted
    ON user_history (user_id, deleted);

CREATE INDEX IF NOT EXISTS idx_user_history_user_type
    ON user_history (user_id, action_type, deleted);

CREATE INDEX IF NOT EXISTS idx_user_history_created_at
    ON user_history (created_at);


-- 4. SAVED COMPARISONS

CREATE TABLE IF NOT EXISTS saved_comparisons (
                                                 id              BIGSERIAL       PRIMARY KEY,
                                                 user_id         BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_a_id    BIGINT          NOT NULL REFERENCES vehicle_specs(id) ON DELETE CASCADE,
    vehicle_b_id    BIGINT          NOT NULL REFERENCES vehicle_specs(id) ON DELETE CASCADE,
    title           VARCHAR(255),

    -- Auditable columns
    created_by           VARCHAR(255),
    created_at           TIMESTAMP,
    last_modified_by     VARCHAR(255),
    last_modified_date   TIMESTAMP,

    CONSTRAINT chk_different_vehicles CHECK (vehicle_a_id <> vehicle_b_id)
    );

CREATE INDEX IF NOT EXISTS idx_saved_comparisons_user
    ON saved_comparisons (user_id);

-- 5. SEARCH INDEX IMPROVEMENT on vehicle_specs (já existente)

CREATE INDEX IF NOT EXISTS idx_vehicle_specs_brand_lower
    ON vehicle_specs (LOWER(brand));

CREATE INDEX IF NOT EXISTS idx_vehicle_specs_model_lower
    ON vehicle_specs (LOWER(model));

CREATE INDEX IF NOT EXISTS idx_vehicle_specs_year
    ON vehicle_specs (year);

CREATE INDEX IF NOT EXISTS idx_vehicle_specs_horsepower
    ON vehicle_specs (horsepower);
