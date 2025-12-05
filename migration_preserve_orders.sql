-- Script de migración para preservar pedidos al eliminar clientes
-- Ejecutar este script en la base de datos existente

USE electronica_domestica;

-- 1. Eliminar la constraint (foreign key) actual
ALTER TABLE equipos 
DROP FOREIGN KEY equipos_ibfk_1;

-- 2. Hacer que cliente_id pueda ser NULL
ALTER TABLE equipos 
MODIFY cliente_id VARCHAR(36) NULL;

-- 3. Agregar la nueva constraint con ON DELETE SET NULL
ALTER TABLE equipos 
ADD CONSTRAINT fk_equipos_cliente 
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL;

-- Ahora cuando se elimine un cliente:
-- - Los equipos asociados NO se eliminan
-- - El campo cliente_id se establece en NULL
-- - Los servicios asociados a esos equipos se mantienen intactos
-- - Se preserva todo el historial de ventas y reparaciones
