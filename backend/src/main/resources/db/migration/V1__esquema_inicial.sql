CREATE TABLE hogares (
    id            UUID         PRIMARY KEY,
    nombre        VARCHAR(255) NOT NULL,
    codigo_hogar  VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
    id          UUID         PRIMARY KEY,
    nombre      VARCHAR(255) NOT NULL,
    contrasena  VARCHAR(255) NOT NULL,
    correo      VARCHAR(255) NOT NULL UNIQUE,
    hogar_id    UUID         REFERENCES hogares (id)
);
CREATE INDEX idx_usuarios_hogar ON usuarios (hogar_id);

CREATE TABLE articulos_despensa (
    id                 UUID         PRIMARY KEY,
    nombre             VARCHAR(255) NOT NULL,
    fecha_vencimiento  DATE         NOT NULL,
    cantidad           INTEGER      NOT NULL CHECK (cantidad >= 0),
    categoria          VARCHAR(255) NOT NULL
        CHECK (categoria IN ('LACTEOS', 'CARNES', 'VEGETALES', 'FRUTAS', 'BEBIDAS',
                             'PANADERIA', 'DESPENSA', 'CONGELADOS', 'OTROS')),
    hogar_id           UUID         NOT NULL REFERENCES hogares (id) ON DELETE CASCADE
);
CREATE INDEX idx_articulos_hogar       ON articulos_despensa (hogar_id);
CREATE INDEX idx_articulos_vencimiento ON articulos_despensa (fecha_vencimiento);

CREATE TABLE alertas (
    id               UUID         PRIMARY KEY,
    articulo_id      UUID         NOT NULL UNIQUE,
    nombre_producto  VARCHAR(255) NOT NULL,
    dias_restantes   INTEGER      NOT NULL,
    hogar_id         UUID         NOT NULL,
    leida            BOOLEAN      NOT NULL,
    fecha_creacion   DATE         NOT NULL
);
CREATE INDEX idx_alertas_hogar ON alertas (hogar_id);
