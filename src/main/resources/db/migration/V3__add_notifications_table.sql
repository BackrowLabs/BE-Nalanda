CREATE TABLE notifications (
    id             BIGSERIAL PRIMARY KEY,
    message        TEXT NOT NULL,
    target_role    VARCHAR(30) NOT NULL,
    related_entity_id BIGINT,
    is_read        BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_name VARCHAR(255),
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
