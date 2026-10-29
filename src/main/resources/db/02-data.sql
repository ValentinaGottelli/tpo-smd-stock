-- ============================================================
--  Datos de ejemplo para MySQL / docker-compose.
--  Borrar este archivo del initdb si no se quieren datos de prueba.
-- ============================================================
INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, version_registro)
VALUES ('COLCHON-QR-0001', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-01', CURRENT_TIMESTAMP(6), 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, version_registro)
VALUES ('COLCHON-QR-0002', 'COLCHON', 'EN_DEPOSITO', 1, '2026-09-05', CURRENT_TIMESTAMP(6), 0);

INSERT INTO item_inventario (codigo, tipo, estado, cantidad, fecha_produccion, fecha_alta, version_registro)
VALUES ('LOTE-QR-0001', 'LOTE', 'PRODUCIDO', 5, '2026-09-10', CURRENT_TIMESTAMP(6), 0);
