-- Número de albarán de las entradas de mercancía, para no registrar el mismo envío dos veces
ALTER TABLE stock_movement
    ADD COLUMN delivery_note VARCHAR(30) NULL AFTER note;

CREATE INDEX idx_stock_movement_delivery_note ON stock_movement (delivery_note);
