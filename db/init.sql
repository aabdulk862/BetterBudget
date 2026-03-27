CREATE SCHEMA IF NOT EXISTS project2;

CREATE TABLE IF NOT EXISTS project2.users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS project2.envelopes (
    envelope_id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES project2.users(user_id),
    envelope_description VARCHAR(255) NOT NULL,
    balance NUMERIC(19,2) NOT NULL,
    max_limit NUMERIC(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS project2.transactions (
    transaction_id SERIAL PRIMARY KEY,
    envelope_id INTEGER REFERENCES project2.envelopes(envelope_id),
    title VARCHAR(255) NOT NULL,
    transaction_description VARCHAR(255) NOT NULL,
    datetime TIMESTAMP NOT NULL,
    category VARCHAR(255) NOT NULL,
    transaction_amount NUMERIC(19,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS project2.envelope_histories (
    amount_history_id SERIAL PRIMARY KEY,
    envelope_id INTEGER REFERENCES project2.envelopes(envelope_id),
    transaction_id INTEGER REFERENCES project2.transactions(transaction_id),
    envelope_amount NUMERIC(19,2) NOT NULL
);
