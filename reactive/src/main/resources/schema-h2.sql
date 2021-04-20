CREATE TABLE IF NOT EXISTS state (
    id BIGINT(255),
    current_term BIGINT(255),
    voted_for VARCHAR(255),
    PRIMARY KEY (id)
);