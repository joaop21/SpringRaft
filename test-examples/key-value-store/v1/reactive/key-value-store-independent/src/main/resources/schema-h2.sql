CREATE TABLE IF NOT EXISTS node (
    created_index BIGINT(255) IDENTITY ,
    key VARCHAR(255) NOT NULL UNIQUE ,
    value VARCHAR(255) ,
    PRIMARY KEY (created_index)
);