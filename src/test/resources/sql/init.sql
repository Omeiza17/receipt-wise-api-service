-- init.sql for PostgresSQL Testcontainers
-- This script will be executed when the PostgresSQL container starts.
CREATE USER mike WITH PASSWORD 'pwd';
CREATE DATABASE testdb;
CREATE SCHEMA TESTDB
GRANT ALL PRIVILEGES ON DATABASE testdb TO mike;
-- CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE TABLE IF NOT EXISTS TESTDB.users
(
    id       UUID DEFAULT gen_random_uuid() PRIMARY KEY NOT NULL,
    username VARCHAR(50)                                NOT NULL,
    password VARCHAR(255)                               NOT NULL,
    email    VARCHAR(255) UNIQUE                        NOT NULL
);
