-- Datos de ejemplo para probar SneakStock en local: 4 tiendas (una cerrada), 2 modelos y su stock.
-- Cómo cargarlos: ver la sección "Cómo arrancarlo" del README.

INSERT INTO store (store_number, name, address, phone, active) VALUES
    ('0001', 'Gran Vía', 'Calle Gran Vía 30, Madrid',  '+34 910 000 001', TRUE),
    ('0002', 'Sol',      'Puerta del Sol 5, Madrid',   '+34 910 000 002', TRUE),
    ('0003', 'Goya',     'Calle Goya 50, Madrid',      '+34 910 000 003', TRUE),
    ('0004', 'Alcalá',   'Calle Alcalá 100, Madrid',   '+34 910 000 004', FALSE);

INSERT INTO product (style_code, brand, model, color, price) VALUES
    ('DD1391-100', 'Nike',   'Dunk Low', 'White/Black', 119.99),
    ('IE3437',     'Adidas', 'Samba OG', 'White/Black', 120.00);

INSERT INTO product_variant (product_id, size_us, size_eu, barcode)
SELECT id, '9',  '42.5',   '8400000000011' FROM product WHERE style_code = 'DD1391-100' UNION ALL
SELECT id, '10', '44',     '8400000000028' FROM product WHERE style_code = 'DD1391-100' UNION ALL
SELECT id, '9',  '42 2/3', '8400000000035' FROM product WHERE style_code = 'IE3437';

INSERT INTO stock (variant_id, store_id, quantity)
SELECT v.id, s.id, x.qty
FROM (
    SELECT '8400000000011' AS barcode, '0001' AS store, 1 AS qty UNION ALL
    SELECT '8400000000011', '0002', 3 UNION ALL
    SELECT '8400000000011', '0003', 1 UNION ALL
    SELECT '8400000000011', '0004', 6 UNION ALL
    SELECT '8400000000028', '0001', 2 UNION ALL
    SELECT '8400000000028', '0003', 4 UNION ALL
    SELECT '8400000000035', '0002', 5
) x
JOIN product_variant v ON v.barcode = x.barcode
JOIN store s ON s.store_number = x.store;
