CREATE TABLE product (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    style_code  VARCHAR(20)    NOT NULL UNIQUE,
    brand       VARCHAR(50)    NOT NULL,
    model       VARCHAR(100)   NOT NULL,
    color       VARCHAR(50)    NOT NULL,
    price       DECIMAL(10,2)  NOT NULL,
    created_at  DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_product_price_positive CHECK (price > 0)
);
