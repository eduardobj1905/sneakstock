CREATE TABLE stock (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY,
    variant_id  BIGINT        NOT NULL,
    store_id    BIGINT        NOT NULL,
    quantity    INT           NOT NULL DEFAULT 0,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_stock_variant
        FOREIGN KEY (variant_id) REFERENCES product_variant (id),
    CONSTRAINT fk_stock_store
        FOREIGN KEY (store_id) REFERENCES store (id),
    CONSTRAINT uq_stock_variant_store
        UNIQUE (variant_id, store_id),
    CONSTRAINT chk_stock_quantity_non_negative
        CHECK (quantity >= 0)
);