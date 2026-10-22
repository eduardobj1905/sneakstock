CREATE TABLE product_variant (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT        NOT NULL,
    size_us     VARCHAR(10)   NOT NULL,
    size_eu     VARCHAR(10)   NOT NULL,
    barcode     VARCHAR(14)   NOT NULL UNIQUE,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_product_variant_product
        FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT uq_product_variant_product_size
        UNIQUE (product_id, size_us)
);