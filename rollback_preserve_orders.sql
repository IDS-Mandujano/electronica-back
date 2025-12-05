-- ⚠️ SCRIPT DE ROLLBACK - Revertir a ON DELETE CASCADE
-- Ejecutar SOLO si necesitas volver al comportamiento anterior
-- ADVERTENCIA: Si ya has eliminado clientes con la nueva configuración,
-- este rollback NO recuperará los datos eliminados.

USE electronica_domestica;

-- 1. Eliminar la constraint SET NULL
ALTER TABLE equipos 
DROP FOREIGN KEY fk_equipos_cliente;

-- 2. Hacer que cliente_id sea NOT NULL de nuevo
-- NOTA: Esto fallará si hay equipos con cliente_id = NULL
-- En ese caso, primero debes eliminar esos equipos o asignarles un cliente
ALTER TABLE equipos 
MODIFY cliente_id VARCHAR(36) NOT NULL;

-- 3. Restaurar la constraint original con CASCADE
ALTER TABLE equipos 
ADD CONSTRAINT equipos_ibfk_1 
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE;

-- 4. Restaurar la vista original (sin LEFT JOIN)
DROP VIEW IF EXISTS vista_servicios_completa;

CREATE VIEW vista_servicios_completa AS
SELECT 
    s.id AS servicio_id,
    s.folio_servicio,
    CONCAT(c.nombre, ' ', c.apellidos) AS nombre_cliente,
    c.numero_celular,
    m.nombre_marca AS marca,
    e.modelo,
    s.problema_reportado,
    s.diagnostico_tecnico,
    u.nombre_completo AS tecnico_nombre,
    s.estado,
    s.fecha_ingreso,
    s.fecha_entrega_cliente,
    s.costo_reparacion
FROM servicios s
JOIN equipos e ON s.equipo_id = e.id
JOIN clientes c ON e.cliente_id = c.id  -- Vuelve a JOIN normal
JOIN marcas m ON e.marca_id = m.id
JOIN users u ON s.tecnico_id = u.id;

-- Estado restaurado: ON DELETE CASCADE (comportamiento original)
