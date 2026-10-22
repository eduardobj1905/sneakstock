CREATE TABLE store (
    id            BIGINT         AUTO_INCREMENT PRIMARY KEY,
    store_number  VARCHAR(10)    NOT NULL UNIQUE,
    name          VARCHAR(100)   NOT NULL,
    address       VARCHAR(255)   NOT NULL,
    phone         VARCHAR(20)    NOT NULL,
    active        BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);