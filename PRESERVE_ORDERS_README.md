# 🛡️ Preservar Pedidos al Eliminar Clientes

## Problema
Actualmente, cuando se elimina un cliente, se eliminan **en cascada** todos sus equipos y servicios debido a `ON DELETE CASCADE`. Esto causa pérdida del historial de ventas y reparaciones.

## Solución
Cambiar la relación de `clientes` → `equipos` de **CASCADE** a **SET NULL**, permitiendo que los equipos y servicios se conserven incluso cuando el cliente es eliminado.

---

## 📋 Pasos para Aplicar

### Opción 1: Base de Datos Existente (Migración)

Si ya tienes datos en tu base de datos, ejecuta el script de migración:

```bash
mysql -u root -p electronica_domestica < migration_preserve_orders.sql
```

O ejecuta manualmente en MySQL Workbench o tu cliente SQL:

```sql
USE electronica_domestica;

-- 1. Eliminar constraint actual
ALTER TABLE equipos 
DROP FOREIGN KEY equipos_ibfk_1;

-- 2. Permitir NULL en cliente_id
ALTER TABLE equipos 
MODIFY cliente_id VARCHAR(36) NULL;

-- 3. Agregar nueva constraint con SET NULL
ALTER TABLE equipos 
ADD CONSTRAINT fk_equipos_cliente 
FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE SET NULL;

-- 4. Recrear la vista
DROP VIEW IF EXISTS vista_servicios_completa;

CREATE VIEW vista_servicios_completa AS
SELECT 
    s.id AS servicio_id,
    s.folio_servicio,
    COALESCE(CONCAT(c.nombre, ' ', c.apellidos), 'Cliente Eliminado') AS nombre_cliente,
    COALESCE(c.numero_celular, 'N/A') AS numero_celular,
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
LEFT JOIN clientes c ON e.cliente_id = c.id
JOIN marcas m ON e.marca_id = m.id
JOIN users u ON s.tecnico_id = u.id;
```

### Opción 2: Base de Datos Nueva (Recrear desde cero)

Si puedes recrear la base de datos desde cero:

```bash
mysql -u root -p < database_schema.sql
```

El esquema actualizado ya incluye estos cambios.

---

## 🔍 Qué Cambia

### Antes (CASCADE):
```
Cliente (id=123)
  └─> Equipo (cliente_id=123)
        └─> Servicio (equipo_id=xyz)

[Eliminar Cliente 123]
  ❌ Se elimina el Equipo
  ❌ Se elimina el Servicio
  ❌ Se pierde el historial
```

### Después (SET NULL):
```
Cliente (id=123)
  └─> Equipo (cliente_id=123)
        └─> Servicio (equipo_id=xyz)

[Eliminar Cliente 123]
  ✅ Equipo (cliente_id=NULL)  <- Queda sin cliente
  ✅ Servicio se mantiene intacto
  ✅ Historial preservado
  📊 En reportes aparece como "Cliente Eliminado"
```

---

## 📊 Cambios en el Frontend

### Vista de Servicios
Cuando un servicio pertenece a un cliente eliminado, se mostrará:
- **Nombre Cliente:** "Cliente Eliminado"
- **Celular:** "N/A"

El frontend NO requiere cambios, la vista SQL maneja esto automáticamente con `COALESCE()`.

### Lista de Clientes
La cuenta de `totalPedidos` seguirá funcionando correctamente usando LEFT JOIN.

---

## ✅ Ventajas

1. ✅ **Preserva historial completo** de servicios y reparaciones
2. ✅ **Estadísticas precisas** - los ingresos se mantienen
3. ✅ **Cumplimiento legal** - registros contables intactos
4. ✅ **Auditoría** - trazabilidad de todas las operaciones
5. ✅ **Recuperación** - datos no se pierden accidentalmente

---

## ⚠️ Consideraciones

### Validaciones en Backend
El backend debe manejar casos donde `cliente_id` es NULL. Actualmente, las queries que usan LEFT JOIN ya están preparadas para esto.

### Registros Huérfanos
Los equipos con `cliente_id = NULL` son "huérfanos". Si quieres limpiarlos eventualmente:

```sql
-- Ver equipos sin cliente
SELECT * FROM equipos WHERE cliente_id IS NULL;

-- Eliminar equipos huérfanos (OPCIONAL, evaluar primero)
-- DELETE FROM equipos WHERE cliente_id IS NULL;
```

---

## 🧪 Pruebas

Para verificar que funciona:

1. **Crear un cliente de prueba**
2. **Crear un servicio para ese cliente**
3. **Eliminar el cliente**
4. **Verificar**:
   - ✅ El servicio sigue existiendo
   - ✅ Aparece en "Pedidos" como "Cliente Eliminado"
   - ✅ Las estadísticas incluyen ese ingreso

```sql
-- Verificar que el servicio se mantiene después de eliminar cliente
SELECT * FROM vista_servicios_completa WHERE nombre_cliente = 'Cliente Eliminado';
```

---

## 📝 Notas Adicionales

- **Backup recomendado:** Haz un respaldo antes de aplicar la migración
- **Desarrollo vs Producción:** Prueba primero en desarrollo
- **Rollback:** Si necesitas revertir, cambia de nuevo a CASCADE (no recomendado)

---

**Fecha de implementación:** 2025-12-04  
**Versión:** 2.1.0
