CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT chk_name_length CHECK (LENGTH(name) >= 2)
);

CREATE INDEX idx_users_email ON users(email);