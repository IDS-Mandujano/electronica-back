package com.electronica.inventario.services;

import com.electronica.inventario.models.Refaccion;
import com.electronica.inventario.models.TarjetaVenta;
import com.electronica.inventario.repository.RefaccionRepository;
import com.electronica.inventario.repository.TarjetaVentaRepository;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public class InventarioService {

    private final RefaccionRepository refaccionRepo;
    private final TarjetaVentaRepository tarjetaVentaRepo;

    public InventarioService(RefaccionRepository refaccionRepo, TarjetaVentaRepository tarjetaVentaRepo) {
        this.refaccionRepo = refaccionRepo;
        this.tarjetaVentaRepo = tarjetaVentaRepo;
    }

    // --- REFACCIONES ---
    public void getAllRefacciones(Context ctx) {
        try {
            var refacciones = refaccionRepo.findAll().stream().map(r -> Map.of(
                    "id", r.getId(),
                    "nombreProducto", r.getNombrePieza(),
                    "categoria", r.getCategoria(),
                    "cantidad", r.getStockActual(),
                    "unidad", r.getUnidadMedida(),
                    "precioUnitario", r.getCostoUnitario(),
                    "stockMinimo", r.getStockMinimo())).toList();

            ctx.json(Map.of(
                    "success", true,
                    "data", refacciones));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al obtener productos: " + e.getMessage()));
        }
    }

    public void getRefaccionById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Refaccion r = refaccionRepo.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            Map<String, Object> data = Map.of(
                    "id", r.getId(),
                    "nombreProducto", r.getNombrePieza(),
                    "categoria", r.getCategoria(),
                    "cantidad", r.getStockActual(),
                    "unidad", r.getUnidadMedida(),
                    "precioUnitario", r.getCostoUnitario(),
                    "stockMinimo", r.getStockMinimo());

            ctx.json(Map.of("success", true, "data", data));
        } catch (Exception e) {
            ctx.status(404).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public void createRefaccion(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Refaccion r = new Refaccion();
            r.setId(UUID.randomUUID().toString());
            r.setNombrePieza((String) body.getOrDefault("nombreProducto", body.get("nombrePieza")));
            r.setCategoria((String) body.get("categoria"));
            // Handle cantidad/cantidadPiezas/stockActual
            Object stock = body.get("stockActual");
            if (stock == null)
                stock = body.get("cantidad");
            if (stock == null)
                stock = body.get("cantidadPiezas");
            r.setStockActual(stock != null ? Integer.parseInt(stock.toString()) : 0);

            r.setStockMinimo(
                    body.get("stockMinimo") != null ? Integer.parseInt(body.get("stockMinimo").toString()) : 0);
            r.setUnidadMedida((String) body.getOrDefault("unidad", body.get("unidadMedida")));
            r.setCostoUnitario(
                    body.get("precioUnitario") != null ? new BigDecimal(body.get("precioUnitario").toString())
                            : BigDecimal.ZERO);

            ctx.status(201).json(Map.of("success", true, "data", refaccionRepo.save(r)));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public void updateRefaccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            Refaccion r = refaccionRepo.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            if (body.containsKey("nombreProducto"))
                r.setNombrePieza((String) body.get("nombreProducto"));
            if (body.containsKey("categoria"))
                r.setCategoria((String) body.get("categoria"));

            Object stock = body.get("stockActual");
            if (stock == null)
                stock = body.get("cantidad");
            if (stock == null)
                stock = body.get("cantidadPiezas");
            if (stock != null)
                r.setStockActual(Integer.parseInt(stock.toString()));

            if (body.containsKey("unidad"))
                r.setUnidadMedida((String) body.get("unidad"));

            // Manejo null-safe para precioUnitario
            if (body.containsKey("precioUnitario")) {
                Object precioObj = body.get("precioUnitario");
                if (precioObj != null) {
                    r.setCostoUnitario(new BigDecimal(precioObj.toString()));
                }
            }

            // Manejo null-safe para cantidadOhms (si existe en el modelo)
            if (body.containsKey("cantidadOhms")) {
                Object ohmsObj = body.get("cantidadOhms");
                // Si tu modelo Refaccion tiene este campo, descomenta:
                // if (ohmsObj != null) {
                // r.setCantidadOhms(new BigDecimal(ohmsObj.toString()));
                // }
            }

            ctx.json(Map.of("success", true, "data", refaccionRepo.save(r)));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    public void deleteRefaccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            refaccionRepo.deleteById(id);
            ctx.json(Map.of("success", true, "message", "Producto eliminado"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- TARJETAS VENTA ---
    public void getAllTarjetasVenta(Context ctx) {
        try {
            ctx.json(Map.of(
                    "success", true,
                    "data", tarjetaVentaRepo.findAll()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al obtener tarjetas: " + e.getMessage()));
        }
    }

    public void createTarjetaVenta(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            TarjetaVenta t = new TarjetaVenta();
            t.setId(UUID.randomUUID().toString());
            t.setMarcaId((Integer) body.get("marcaId"));
            t.setModelo((String) body.get("modelo"));
            t.setDescripcion((String) body.get("descripcion"));
            t.setPrecioVenta(new BigDecimal(body.get("precioVenta").toString()));
            t.setEstado("DISPONIBLE");

            ctx.status(201).json(Map.of(
                    "success", true,
                    "data", tarjetaVentaRepo.save(t)));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // --- MATERIAL USAGE TRACKING ---
    private com.electronica.servicio.repository.ServicioMaterialRepository materialRepo;

    public void setMaterialRepository(com.electronica.servicio.repository.ServicioMaterialRepository materialRepo) {
        this.materialRepo = materialRepo;
    }

    /**
     * Registra el uso de un material en un servicio
     * POST /api/productos/uso
     * Body: { productoId, servicioId, cantidad }
     */
    public void registrarUsoMaterial(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String materialId = (String) body.get("productoId");
            String servicioId = (String) body.get("servicioId");
            int cantidad = Integer.parseInt(body.get("cantidad").toString());

            if (materialId == null || servicioId == null || cantidad <= 0) {
                ctx.status(400).json(Map.of("success", false, "message", "Datos inválidos"));
                return;
            }

            // Verificar stock disponible
            if (!materialRepo.verificarStock(materialId, cantidad)) {
                ctx.status(400).json(Map.of("success", false, "message", "Stock insuficiente"));
                return;
            }

            // Registrar uso y reducir stock
            materialRepo.registrarUso(servicioId, materialId, cantidad);
            materialRepo.restarStock(materialId, cantidad);

            ctx.status(200).json(Map.of(
                    "success", true,
                    "message", "Material registrado correctamente"));

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of("success", false, "message", "Cantidad inválida"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Obtiene los materiales usados en un servicio
     * GET /api/servicios/{id}/materiales
     */
    public void obtenerMaterialesServicio(Context ctx) {
        try {
            String servicioId = ctx.pathParam("id");
            var materiales = materialRepo.obtenerMaterialesConDetalles(servicioId);

            ctx.json(Map.of(
                    "success", true,
                    "data", materiales));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
