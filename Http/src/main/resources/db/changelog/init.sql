-- liquibase formatted sql

-- changeset kanhaiya:1
DROP TYPE IF EXISTS your_enum_name;
CREATE TYPE your_enum_name AS ENUM (
    'value1',
    'value2',
    'value3'
);