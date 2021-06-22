CREATE TABLE IF NOT EXISTS node (
    created_index BIGINT(255) IDENTITY ,
    key VARCHAR(255) NOT NULL UNIQUE ,
    value VARCHAR(255) ,
    PRIMARY KEY (created_index)
);

CREATE TABLE IF NOT EXISTS state (
    id BIGINT(255),
    current_term BIGINT(255),
    voted_for VARCHAR(255),
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS log_state (
    id BIGINT(255),
    committed_index BIGINT(255),
    committed_term BIGINT(255),
    last_applied BIGINT(255),
    PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS entry (
    index BIGINT(255),
    term BIGINT(255),
    command VARCHAR(max),
    PRIMARY KEY (index)
    );