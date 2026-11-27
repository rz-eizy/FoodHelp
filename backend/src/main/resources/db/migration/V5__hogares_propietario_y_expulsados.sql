-- Elimina hogares fantasma (sin integrantes); sus articulos caen por ON DELETE CASCADE y las alertas por la FK de articulos
DELETE FROM hogares h
WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.hogar_id = h.id);

ALTER TABLE usuarios ADD COLUMN hogar_desde TIMESTAMP WITH TIME ZONE;
UPDATE usuarios SET hogar_desde = CURRENT_TIMESTAMP WHERE hogar_id IS NOT NULL;

ALTER TABLE hogares ADD COLUMN propietario_id UUID REFERENCES usuarios (id) ON DELETE SET NULL;
UPDATE hogares h
SET propietario_id = (
    SELECT u.id FROM usuarios u WHERE u.hogar_id = h.id ORDER BY u.hogar_desde, u.id LIMIT 1
);

CREATE TABLE hogar_expulsados (
    id           UUID PRIMARY KEY,
    hogar_id     UUID NOT NULL REFERENCES hogares (id)  ON DELETE CASCADE,
    usuario_id   UUID NOT NULL REFERENCES usuarios (id) ON DELETE CASCADE,
    expulsado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_hogar_expulsado UNIQUE (hogar_id, usuario_id)
);
