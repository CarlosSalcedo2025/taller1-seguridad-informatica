CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    roles VARCHAR(255) NOT NULL DEFAULT 'ROLE_USER'
    );

CREATE TABLE IF NOT EXISTS note (
                                    id BIGSERIAL PRIMARY KEY,
                                    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    owner_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_note_owner_email ON note(owner_email);

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW(); RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_note_updated_at ON note;
CREATE TRIGGER trg_note_updated_at
    BEFORE UPDATE ON note
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
