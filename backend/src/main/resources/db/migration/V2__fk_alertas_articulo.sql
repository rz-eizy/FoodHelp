-- Limpia alertas huérfanas antes de crear la restricción (si no, el ALTER fallaría)
DELETE FROM alertas
WHERE articulo_id NOT IN (SELECT id FROM articulos_despensa);

ALTER TABLE alertas
    ADD CONSTRAINT fk_alertas_articulo
    FOREIGN KEY (articulo_id) REFERENCES articulos_despensa (id) ON DELETE CASCADE;
