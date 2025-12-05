DROP DATABASE IF EXISTS electronica_domestica;
CREATE DATABASE electronica_domestica;
USE electronica_domestica;

-- 1. TABLA DE USUARIOS (Técnicos y Gerentes)
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    nombre_completo VARCHAR(255) NOT NULL,
    correo_electronico VARCHAR(255) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    tipo ENUM('tecnico', 'gerente') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. TABLA DE CLIENTES
CREATE TABLE clientes (
    id VARCHAR(36) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    numero_celular VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. CATÁLOGO DE MARCAS
CREATE TABLE marcas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_marca VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO marcas (nombre_marca) VALUES 
('Whirlpool'), 
('K24'), 
('Samsung'), 
('LG display izquierdo'), 
('LG display derecho'), 
('Mabe isy'), 
('Mabe normal'), 
('LG'), 
('Daewoo'), 
('K27');

-- 4. TABLA DE EQUIPOS
CREATE TABLE equipos (
    id VARCHAR(36) PRIMARY KEY,
    cliente_id VARCHAR(36) NOT NULL,
    marca_id INT NOT NULL,
    modelo VARCHAR(100),
    tipo_equipo VARCHAR(50) DEFAULT 'Tarjeta Electrónica',
    numero_serie VARCHAR(100),
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE,
    FOREIGN KEY (marca_id) REFERENCES marcas(id)
);

-- 5. TABLA DE SERVICIOS
CREATE TABLE servicios (
    id VARCHAR(36) PRIMARY KEY,
    folio_servicio INT AUTO_INCREMENT UNIQUE,
    equipo_id VARCHAR(36) NOT NULL,
    tecnico_id VARCHAR(36) NOT NULL,
    
    problema_reportado TEXT NOT NULL,
    fecha_ingreso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    diagnostico_tecnico TEXT,
    estado ENUM('PENDIENTE', 'EN_PROCESO', 'ESPERA_REFACCION', 'FINALIZADO', 'ENTREGADO', 'CANCELADO') DEFAULT 'PENDIENTE',
    
    fecha_estimada_entrega DATETIME,
    fecha_finalizacion DATETIME,
    fecha_entrega_cliente DATETIME,
    costo_reparacion DECIMAL(10,2) DEFAULT 0.00,
    
    FOREIGN KEY (equipo_id) REFERENCES equipos(id) ON DELETE CASCADE,
    FOREIGN KEY (tecnico_id) REFERENCES users(id)
);

-- 6. INVENTARIO DE REFACCIONES
CREATE TABLE inventario_refacciones (
    id VARCHAR(36) PRIMARY KEY,
    nombre_pieza VARCHAR(255) NOT NULL,
    categoria VARCHAR(100),
    stock_actual INT DEFAULT 0,
    stock_minimo INT DEFAULT 5,
    unidad_medida VARCHAR(20),
    costo_unitario DECIMAL(10,2)
);

-- 7. INVENTARIO DE TARJETAS EN VENTA
CREATE TABLE tarjetas_venta (
    id VARCHAR(36) PRIMARY KEY,
    marca_id INT NOT NULL,
    modelo VARCHAR(100),
    descripcion TEXT,
    precio_venta DECIMAL(10,2) NOT NULL,
    estado ENUM('DISPONIBLE', 'VENDIDO') DEFAULT 'DISPONIBLE',
    fecha_venta DATETIME,
    FOREIGN KEY (marca_id) REFERENCES marcas(id)
);

-- VISTA PARA EL FRONTEND
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
JOIN clientes c ON e.cliente_id = c.id
JOIN marcas m ON e.marca_id = m.id
JOIN users u ON s.tecnico_id = u.id;


-- Ejecuta esto en DataGrip
ALTER TABLE clientes
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL;


-- Agregar el estado CANCELADO al ENUM

ALTER TABLE servicios
CHANGE COLUMN estado estado ENUM('PENDIENTE', 'EN_PROCESO', 'ESPERA_REFACCION', 'FINALIZADO', 'ENTREGADO', 'CANCELADO') DEFAULT 'PENDIENTE';




CREATE TABLE servicios_materiales (
    id VARCHAR(36) PRIMARY KEY,
    servicio_id VARCHAR(36) NOT NULL,
    material_id VARCHAR(36) NOT NULL,
    cantidad_usada INT NOT NULL,
    fecha_uso TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (servicio_id) REFERENCES servicios(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES inventario_refacciones(id)
);

CREATE INDEX idx_servicio_id ON servicios_materiales(servicio_id);
CREATE INDEX idx_material_id ON servicios_materiales(material_id);

ALTER TABLE servicios_materiales
ADD UNIQUE KEY unique_servicio_material (servicio_id, material_id);

CREATE VIEW vista_materiales_servicios AS
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
