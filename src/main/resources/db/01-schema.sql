-- ============================================================
--  Esquema MySQL 8 para SMD Stock
--  Se ejecuta automaticamente con docker-compose (initdb).
--  Para una base existente:
--    mysql -u root -p < 01-schema.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS item_inventario (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    codigo           VARCHAR(64)  NOT NULL,
    tipo             VARCHAR(20)  NOT NULL,
    estado           VARCHAR(20)  NOT NULL,
    cantidad         INT          NOT NULL,
    fecha_produccion DATE         NULL,
    fecha_alta       DATETIME(6)  NOT NULL,
    version_registro BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_item_inventario_codigo UNIQUE (codigo),
    CONSTRAINT ck_item_inventario_cantidad CHECK (cantidad >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
