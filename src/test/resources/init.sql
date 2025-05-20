-- init.sql for PostgresSQL Testcontainers
-- This script will be executed when the PostgresSQL container starts.

CREATE TABLE users
(
    id       UUID DEFAULT gen_random_uuid() PRIMARY KEY NOT NULL,
    username VARCHAR(50)                                NOT NULL,
    password VARCHAR(255)                               NOT NULL,
    email    VARCHAR(255) UNIQUE                        NOT NULL
);

COMMENT ON TABLE users IS 'Table to store user account information.';
COMMENT ON COLUMN users.id IS 'Unique identifier for the user (UUID, primary key).';
COMMENT ON COLUMN users.username IS 'User''s chosen username (up to 50 characters, cannot be null).';
COMMENT ON COLUMN users.password IS 'User''s hashed password (up to 255 characters, cannot be null).';
COMMENT ON COLUMN users.email IS 'User''s email address (unique, cannot be null).';
