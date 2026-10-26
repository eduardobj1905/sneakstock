INSERT INTO product (style_code, brand, model, color, price)
VALUES ('TEST-DUNK', 'Nike', 'Dunk Low', 'Panda', 119.99);

INSERT INTO product_variant (product_id, size_us, size_eu, barcode)
SELECT id, '9', '42.5', '0000000000001' FROM product WHERE style_code = 'TEST-DUNK';

INSERT INTO store (store_number, name, address, phone, active) VALUES
    ('T001', 'Gran Vía',  'Calle Gran Vía 1', '+34 900 000 001', TRUE),
    ('T002', 'Sol',       'Puerta del Sol 2', '+34 900 000 002', TRUE),
    ('T003', 'Callao',    'Plaza Callao 3',   '+34 900 000 003', TRUE),
    ('T004', 'Preciados', 'Calle Preciados 4', '+34 900 000 004', FALSE);

INSERT INTO stock (variant_id, store_id, quantity)
SELECT v.id, s.id,
       CASE s.store_number WHEN 'T001' THEN 3 WHEN 'T002' THEN 5 WHEN 'T003' THEN 0 ELSE 7 END
FROM product_variant v, store s
WHERE v.barcode = '0000000000001';