-- ===================================
-- MIGRACIÓN: Rastreo de Materiales Usados en Servicios
-- ===================================
-- Fecha: 2025-12-04
-- Propósito: Permitir que técnicos registren qué materiales usan en cada servicio
--            y mostrar esta información en tarjetas finalizadas

USE electronica_domestica;

-- Tabla para relacionar servicios con materiales usados
CREATE TABLE IF NOT EXISTS servicios_materiales (
    id VARCHAR(36) PRIMARY KEY,
    servicio_id VARCHAR(36) NOT NULL,
    material_id VARCHAR(36) NOT NULL,
    cantidad_usada INT NOT NULL,
    fecha_uso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES inventario_refacciones(id),
    
    -- Evitar duplicados del mismo material en un servicio
    UNIQUE KEY unique_servicio_material (servicio_id, material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Índices para mejorar performance
CREATE INDEX idx_servicio_id ON servicios_materiales(servicio_id);
CREATE INDEX idx_material_id ON servicios_materiales(material_id);

-- Vista para facilitar consultas
CREATE OR REPLACE VIEW vista_materiales_servicios AS
SELECT 
    sm.id,
    sm.servicio_id,
    s.folio_servicio,
    sm.material_id,
    ir.nombre_pieza,
    ir.categoria,
    sm.cantidad_usada,
    sm.fecha_uso
FROM servicios_materiales sm
JOIN servicios s ON sm.servicio_id = s.id
JOIN inventario_refacciones ir ON sm.material_id = ir.id;

-- Comentarios para documentación
ALTER TABLE servicios_materiales 
COMMENT = 'Registra qué materiales del inventario se usaron en cada servicio';
