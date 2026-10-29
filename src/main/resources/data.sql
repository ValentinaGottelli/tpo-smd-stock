-- ============================================================
--  Datos de ejemplo para el perfil dev (H2 en memoria).
--  Se cargan en cada arranque porque la base es efimera.
-- ============================================================
INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('COLCHON-QR-0001', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-01', CURRENT_TIMESTAMP, NULL, 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('COLCHON-QR-0002', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-05', CURRENT_TIMESTAMP, NULL, 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('LOTE-QR-0001', 'LOTE', 'EN_DEPOSITO', 5, '2026-09-10', CURRENT_TIMESTAMP, NULL, 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('LOTE-QR-0001-01', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-10', CURRENT_TIMESTAMP, 'LOTE-QR-0001', 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('LOTE-QR-0001-02', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-10', CURRENT_TIMESTAMP, 'LOTE-QR-0001', 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('LOTE-QR-0001-03', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-10', CURRENT_TIMESTAMP, 'LOTE-QR-0001', 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('LOTE-QR-0001-04', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-10', CURRENT_TIMESTAMP, 'LOTE-QR-0001', 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, codigo_padre, version_registro)
VALUES ('LOTE-QR-0001-05', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-10', CURRENT_TIMESTAMP, 'LOTE-QR-0001', 0);
