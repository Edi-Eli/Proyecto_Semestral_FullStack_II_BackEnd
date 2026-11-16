-- =====================================================================
-- Seed de ejemplo del catálogo (Ferretería Los Maestros)
-- Datos base tomados del catálogo de muestra del front (src/data/db.js).
-- Se reemplazará/extenderá con el Excel real en una migración posterior.
-- =====================================================================

INSERT INTO categorias (id, nombre, imagen_url) VALUES
    (1, 'Mat. Construcción',      '/assets/images/bloque-hormigon.jpeg'),
    (2, 'Herramientas Manuales',  '/assets/images/alicate.png'),
    (3, 'Herramientas Eléctricas','/assets/images/taladro-in.png'),
    (4, 'Jardinería',             '/assets/images/images-2.jpeg'),
    (5, 'Seguridad',              '/assets/images/descargar.jpeg');

INSERT INTO subcategorias (id, categoria_id, nombre) VALUES
    (1, 1, 'Cementos'),
    (2, 1, 'Áridos'),
    (3, 1, 'Albañilería'),
    (4, 2, 'Herramientas de mano'),
    (5, 3, 'Herramientas eléctricas'),
    (6, 4, 'Jardín'),
    (7, 5, 'Seguridad');

INSERT INTO productos
    (id, codigo, subcategoria_id, nombre, descripcion, marca, unidad,
     precio_compra, precio_venta, en_oferta, precio_oferta, stock, stock_minimo, imagen_url, activo)
VALUES
    (1,  'CON001', 1, 'Cemento Polpaico gris 25KG', 'Cemento de alta calidad gris de 25KG para construcción.', 'Polpaico', 'Saco',   4193.00,  5990.00, TRUE,  5490.00, 20,  5, '/assets/images/88883_1.jpg', TRUE),
    (2,  'CON002', 1, 'Cemento Melon Blanco 25KG', 'Cemento Melón Blanco 25KG para terminaciones.', 'Melón', 'Saco',           5243.00,  7490.00, FALSE, NULL,    10,  4, '/assets/images/descargar.png', TRUE),
    (3,  'CON003', 1, 'Mortero Cola Cerámica 25KG', 'Mortero cola cerámica de 25KG para instalación de cerámicos.', 'Genérico', 'Saco', 3640.00,  5200.00, FALSE, NULL, 15, 5, '/assets/images/mortero.png', TRUE),
    (4,  'CON004', 1, 'Mortero nivelador piso 25KG', 'Mortero nivelador de piso 25KG.', 'Genérico', 'Saco',                4543.00,  6490.00, FALSE, NULL,    10,  4, '/assets/images/mortero_nive.png', TRUE),
    (5,  'CON005', 2, 'Arena fina construcción 25KG', 'Arena fina de 25KG para mezclas y terminaciones.', 'Genérico', 'Saco', 0.00,     0.00, TRUE,  NULL,    20,  5, '/assets/images/arena.png', TRUE),
    (6,  'CON006', 2, 'Ripio 25KG', 'Ripio 25KG para mezclas de hormigón.', 'Genérico', 'Saco',                            1050.00,  1500.00, FALSE, NULL,    20,  5, '/assets/images/ripio.jpeg', TRUE),
    (7,  'CON007', 3, 'Ladrillo fiscal N°5', 'Ladrillo fiscal N°5 de alta resistencia.', 'Genérico', 'Unidad',            266.00,   380.00, FALSE, NULL,   100, 20, '/assets/images/ladrillo.jpeg', TRUE),
    (8,  'CON008', 3, 'Ladrillo prensado', 'Ladrillo prensado 6x14x29 cm.', 'Genérico', 'Unidad',                          385.00,   550.00, FALSE, NULL,    80, 15, '/assets/images/ladrillo-pren.jpeg', TRUE),
    (9,  'CON009', 3, 'Bloque de hormigón', 'Bloque de hormigón 19x19x39cm.', 'Genérico', 'Unidad',                        840.00,  1200.00, FALSE, NULL,    50, 10, '/assets/images/bloque-hormigon.jpeg', TRUE),
    (10, 'CON010', 3, 'Bloque liviano', 'Bloque liviano 10x20x40cm.', 'Genérico', 'Unidad',                              1183.00,  1690.00, FALSE, NULL,    30,  8, '/assets/images/bloque-liviano.jpeg', TRUE),
    (11, 'FER001', 4, 'Caja De Herramientas', 'Caja de herramientas con 20 piezas.', 'Stanley', 'Unidad',                  6293.00,  8990.00, FALSE, NULL,    15,  4, '/assets/images/images.jpeg', TRUE),
    (12, 'FER002', 5, 'Esmeril Angular Electrico', 'Esmeril angular para metal y concreto.', 'Bosch', 'Unidad',           35000.00, 50000.00, TRUE, 45990.00,   9,  3, '/assets/images/8739196_01.jpg', TRUE),
    (13, 'FER003', 5, 'Pack Taladro Inalambrico', 'Taladro inalámbrico con batería y cargador.', 'Bosch', 'Unidad',       66500.00, 95000.00, FALSE, NULL,     3,  5, '/assets/images/taladro-in.png', TRUE),
    (14, 'FER004', 5, 'Cepillo Electrico', 'Cepillo eléctrico multiuso.', 'Bosch', 'Unidad',                              82103.00,117290.00, FALSE, NULL,     6,  2, '/assets/images/images-1.jpeg', TRUE),
    (15, 'FER005', 4, '13 En 1 Destornillador', 'Set destornillador 13 en 1.', 'Truper', 'Unidad',                       16730.00, 23900.00, TRUE, 19990.00,  20,  5, '/assets/images/images-3.jpeg', TRUE),
    (16, 'FER006', 4, 'Mazo 16', 'Mazo de 16 onzas.', 'Truper', 'Unidad',                                            14000.00, 20000.00, FALSE, NULL,    12,  4, '/assets/images/images-2.jpeg', TRUE),
    (17, 'FER007', 4, 'Martillo Carpintero Madera', 'Martillo de carpintero con mango de madera.', 'Truper', 'Unidad', 10150.00, 14500.00, FALSE, NULL,     2,  3, '/assets/images/descargar.jpeg', TRUE),
    (18, 'FER008', 4, 'Set de Alicates Universal', 'Set de alicates universales.', 'Stanley', 'Unidad',                21000.00, 30000.00, FALSE, NULL,    10,  3, '/assets/images/alicate.png', TRUE);

SELECT setval(pg_get_serial_sequence('categorias', 'id'),    (SELECT MAX(id) FROM categorias));
SELECT setval(pg_get_serial_sequence('subcategorias', 'id'), (SELECT MAX(id) FROM subcategorias));
SELECT setval(pg_get_serial_sequence('productos', 'id'),     (SELECT MAX(id) FROM productos));
