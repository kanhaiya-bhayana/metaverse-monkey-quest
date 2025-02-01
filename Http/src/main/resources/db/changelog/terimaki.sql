-- liquibase formatted sql

-- changeset kanhaiya:2
DROP TYPE IF EXISTS user_role;
CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'USER',
    'BAAP'
);