# 🔙 Cómo Revertir la Migración (Rollback)

## 📋 Situaciones de Rollback

### ✅ Cuándo puedes hacer rollback seguro:
- Aplicaste la migración pero aún NO has eliminado ningún cliente
- Quieres volver al comportamiento anterior por cualquier razón
- Detectaste algún problema inmediatamente después de aplicar

### ⚠️ Cuándo el rollback es problemático:
- Ya eliminaste clientes después de la migración
- Hay equipos con `cliente_id = NULL` en la base de datos

---

## 🔄 Proceso de Rollback

### Paso 1: Verificar si hay equipos sin cliente

Antes de hacer rollback, verifica si hay equipos "huérfanos":

```sql
-- Ver si hay equipos sin cliente
SELECT COUNT(*) as equipos_sin_cliente 
FROM equipos 
WHERE cliente_id IS NULL;
```

**Si el resultado es 0:** ✅ Puedes hacer rollback sin problemas

**Si el resultado es > 0:** ⚠️ Necesitas limpiar primero (ver más abajo)

---

### Paso 2: Ejecutar Rollback

En DataGrip:

1. **Abre** el archivo `rollback_preserve_orders.sql`
2. **Selecciona todo** (Ctrl+A)
3. **Ejecuta** (Ctrl+Enter)

O copia y pega este código:

```sql
USE electronica_domestica;

-- 1. Eliminar la constraint SET NULL
ALTER TABLE equipos 
DROP FOREIGN KEY fk_equipos_cliente;

-- 2. Hacer que cliente_id sea NOT NULL de nuevo
ALTER TABLE equipos 
MODIFY cliente_id VARCHAR(36) NOT NULL;

-- 3. Restaurar la constraint original con CASCADE
ALTER TABLE equipos 
ADD CONSTRAINT equipos_ibfk_1 
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE;

-- 4. Restaurar la vista original
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
JOIN clientes c ON e.cliente_id = c.id
JOIN marcas m ON e.marca_id = m.id
JOIN users u ON s.tecnico_id = u.id;
```

---

## 🧹 Si hay equipos sin cliente (Opción A: Eliminar)

Si hay equipos huérfanos y quieres eliminarlos antes del rollback:

```sql
-- ⚠️ CUIDADO: Esto eliminará servicios asociados también
DELETE FROM equipos WHERE cliente_id IS NULL;
```

Después de esto, ejecuta el rollback normalmente.

---

## 🔧 Si hay equipos sin cliente (Opción B: Asignar cliente temporal)

Si quieres preservar esos equipos, crea un cliente "dummy":

```sql
-- 1. Crear cliente temporal
INSERT INTO clientes (id, nombre, apellidos, numero_celular) 
VALUES ('cliente-eliminado', 'Cliente', 'Eliminado', '0000000000');

-- 2. Asignar ese cliente a los equipos huérfanos
UPDATE equipos 
SET cliente_id = 'cliente-eliminado' 
WHERE cliente_id IS NULL;

-- 3. Ahora sí, ejecutar rollback
```

---

## ✅ Verificar que el Rollback funcionó

```sql
-- Ver la estructura de equipos
SHOW CREATE TABLE equipos;

-- Deberías ver:
-- cliente_id varchar(36) NOT NULL
-- CONSTRAINT `equipos_ibfk_1` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`) ON DELETE CASCADE
```

---

## 🔄 Re-aplicar la migración después del rollback

Si hiciste rollback y quieres volver a aplicar SET NULL:

1. Ejecuta nuevamente `migration_preserve_orders.sql`
2. Recrea la vista con LEFT JOIN

---

## 💾 Backup Recomendado (Antes de Migrar)

**SIEMPRE haz backup antes de cambios estructurales:**

### Desde terminal:
```bash
mysqldump -u root -p electronica_domestica > backup_antes_migracion.sql
```

### Desde DataGrip:
1. Clic derecho en la base de datos `electronica_domestica`
2. **SQL Scripts** → **Dump Data to File**
3. Guarda como `backup_antes_migracion.sql`

### Para restaurar el backup:
```bash
mysql -u root -p electronica_domestica < backup_antes_migracion.sql
```

---

## 🆘 Errores Comunes y Soluciones

### Error: "Cannot drop index 'fk_equipos_cliente': needed in a foreign key constraint"
**Solución:** El nombre de la constraint es diferente. Averigua el nombre real:
```sql
SHOW CREATE TABLE equipos;
```
Luego úsalo en DROP FOREIGN KEY.

---

### Error: "Column 'cliente_id' cannot be null"
**Causa:** Intentas hacer rollback pero hay equipos con cliente_id = NULL

**Solución:** Sigue la "Opción A" o "Opción B" de arriba para limpiar primero.

---

### Error: "Duplicate foreign key constraint name"
**Causa:** Ya existe una constraint con ese nombre

**Solución:** Primero elimina la constraint existente con su nombre correcto.

---

## 📞 Ayuda Adicional

Si algo sale mal:

1. **No entres en pánico** 🧘
2. **Restaura el backup** si lo hiciste
3. **Revisa los logs de error** de DataGrip
4. **Verifica el estado actual** con `SHOW CREATE TABLE equipos;`

---

**Fecha:** 2025-12-04  
**Versión:** 1.0
