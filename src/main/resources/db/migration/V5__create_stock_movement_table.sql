CREATE TABLE stock_movement (
    id                   BIGINT        AUTO_INCREMENT PRIMARY KEY,
    stock_id             BIGINT        NOT NULL,
    type                 VARCHAR(20)   NOT NULL,
    quantity             INT           NOT NULL,
    destination_store_id BIGINT        NULL,
    transfer_out_id      BIGINT        NULL,
    note                 VARCHAR(255)  NULL,
    created_at           DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_stock_movement_stock
        FOREIGN KEY (stock_id) REFERENCES stock (id),
    CONSTRAINT fk_stock_movement_destination_store
        FOREIGN KEY (destination_store_id) REFERENCES store (id),
    CONSTRAINT fk_stock_movement_transfer_out
        FOREIGN KEY (transfer_out_id) REFERENCES stock_movement (id),
    CONSTRAINT chk_stock_movement_type
        CHECK (type IN ('ENTRY', 'SALE', 'ADJUSTMENT', 'TRANSFER_OUT', 'TRANSFER_IN')),
    CONSTRAINT chk_stock_movement_quantity_not_zero
        CHECK (quantity <> 0)
);