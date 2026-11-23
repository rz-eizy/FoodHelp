-- Los articulos con cantidad 0 ya no existen: se eliminan al llegar a 0 (sus alertas caen por la FK ON DELETE CASCADE)
DELETE FROM articulos_despensa WHERE cantidad = 0;
