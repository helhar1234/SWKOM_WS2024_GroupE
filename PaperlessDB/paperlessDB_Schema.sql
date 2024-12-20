CREATE TABLE files (
    id VARCHAR(255) PRIMARY KEY,                 -- Unique identifier for the file
    filename VARCHAR(255) NOT NULL,              -- Original name of the file
    filesize BIGINT NOT NULL,                    -- Size of the file in bytes
    filetype VARCHAR(100) NOT NULL,              -- MIME type of the file
    file BYTEA NOT NULL,                         -- Binary data of the file
    uploaddate TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Upload timestamp
);
