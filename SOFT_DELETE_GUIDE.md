# ✅ Solución: Soft Delete para Clientes

## 🎯 Objetivo
Poder eliminar clientes desde la interfaz pero **mantener todos sus pedidos y servicios** para preservar el historial de ventas.

---

## 📋 Ventajas del Soft Delete vs Modificar Foreign Keys

### ✅ Soft Delete (Implementado)
- ✅ **NO modifica foreign keys** - Base de datos intacta
- ✅ **Solo 1 columna nueva** - Cambio mínimo
- ✅ **Reversible** - Puedes "recuperar" clientes eliminados
- ✅ **Historial completo** - Pedidos permanecen intactos
- ✅ **Auditoría** - Sabes cuándo fue eliminado
- ✅ **Sin riesgo de CASCADE** - Los datos nunca se eliminan físicamente

### ❌ ON DELETE SET NULL (Alternativa descartada)
- ❌ Modifica estructura de BD con ALTER TABLE
- ❌ Requiere cambiar foreign keys
- ❌ Datos NULL difíciles de manejar
- ❌ NO sabes cuándo se eliminó
- ❌ Más complejo de implementar

---

## 🛠️ Implementación Completa

### Paso 1: Agregar columna `deleted_at` en BD

**Ejecuta en DataGrip:**

```sql
-- Solo agregar UNA columna a la tabla clientes
ALTER TABLE clientes 
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL;
```

**¡Eso es TODO en la base de datos!** ✅

---

### Paso 2: Código Backend (Ya modificado)

#### Archivos actualizados:

1. **`ClienteRepository.java`**
   - ✅ Agregado método `softDelete(String numero)`
   - ✅ Actualizado `findByNombreYNumero()` para filtrar eliminados

2. **`ClienteService.java`**
   - ✅ Método `delete()` ahora usa `soft Delete()` en lugar de `deleteByNumero()`
   - ✅ Método `getAll()` filtra clientes con `deleted_at IS NULL`

---

## 📊 Cómo Funciona

### Antes (DELETE físico):
```
Cliente "Juan Pérez"
  ├─> Equipo 1 (Lavadora)
  │     └─> Servicio 1 (Reparación $500)
  └─> Equipo 2 (Refrigerador)
        └─> Servicio 2 (Reparación $300)

[Eliminar Cliente Juan]
  ❌ Borra Cliente
  ❌ Borra Equipos (CASCADE)
  ❌ Borra Servicios (CASCADE)
  ❌ Pierdes historial de $800 en ventas
```

### Ahora (Soft Delete):
```
Cliente "Juan Pérez" (deleted_at = 2025-12-04 05:20:00)
  ├─> Equipo 1 (Lavadora) ← Sigue existiendo
  │     └─> Servicio 1 (Reparación $500) ← Sigue existiendo
  └─> Equipo 2 (Refrigerador) ← Sigue existiendo
        └─> Servicio 2 (Reparación $300) ← Sigue existiendo

[Soft Delete Cliente Juan]
  ✅ Cliente marcado como eliminado (deleted_at tiene fecha)
  ✅ Equipos intactos
  ✅ Servicios intactos
  ✅ Historial preservado ($800 en estadísticas)
  ✅ Ya NO aparece en lista de clientes
```

---

## 🔍 Queries SQL Importantes

### Ver clientes activos (lo que ve el frontend):
```sql
SELECT * FROM clientes WHERE deleted_at IS NULL;
```

### Ver clientes eliminados:
```sql
SELECT * FROM clientes WHERE deleted_at IS NOT NULL;
```

### Ver todos (incluyendo eliminados):
```sql
SELECT * FROM clientes;
```

### Recuperar un cliente eliminado:
```sql
UPDATE clientes 
SET deleted_at = NULL 
WHERE numero_celular = '1234567890';
```

---

## ✅ Para Aplicar

### 1. **En DataGrip:**
```sql
ALTER TABLE clientes 
ADD COLUMN deleted_at TIMESTAMP NULL DEFAULT NULL;
```

### 2. **Reinicia el backend:**
```bash
# Detén el servidor (Ctrl+C)
cd /Users/angelgabrielruizarreola/Desktop/proyecto/electronica-back
./gradlew run
```

### 3. **Prueba:**
1. Ve a la lista de **Clientes** en el frontend
2. **Elimina un cliente** que tenga pedidos
3 **Verifica**:
   - ✅ El cliente ya no aparece en la lista
   - ✅ Sus pedidos siguen existiendo en "Pedidos"
   - ✅ Las estadísticas incluyen sus ventas

---

## 🎯 Verificar que Funciona

### En DataGrip, después de eliminar un cliente:

```sql
-- Ver cliente eliminado
SELECT * FROM clientes WHERE deleted_at IS NOT NULL;

-- Ver que sus equipos siguen existiendo
SELECT e.* 
FROM equipos e
JOIN clientes c ON e.cliente_id = c.id
WHERE c.deleted_at IS NOT NULL;

-- Ver que sus servicios siguen existiendo
SELECT s.* 
FROM servicios s
JOIN equipos e ON s.equipo_id = e.id
JOIN clientes c ON e.cliente_id = c.id
WHERE c.deleted_at IS NOT NULL;
```

Si las 3 queries retornan datos, ¡está funcionando correctamente! ✅

---

## 🔄 Comparación Final

| Característica | Soft Delete | ON DELETE SET NULL |
|----------------|-------------|-------------------|
| Cambios en BD | ✅ 1 columna | ❌ Modificar FK |
| Complejidad | ✅ Baja | ❌ Media |
| Reversible | ✅ Sí | ❌ No |
| Historial | ✅ Completo | ⚠️ Parcial |
| Auditoría | ✅ Con fecha | ❌ Sin fecha |
| Riesgo | ✅ Muy bajo | ⚠️ Medio |

---

**Fecha:** 2025-12-04  
**Implementación:** Soft Delete  
**Estado:** ✅ Listo para usar

