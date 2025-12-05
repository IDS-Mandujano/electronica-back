package com.electronica.inventario.routes;

import com.electronica.inventario.services.InventarioService;
import io.javalin.Javalin;

public class InventarioRoutes {
    public static void register(Javalin app, InventarioService service) {
        // Refacciones
        app.get("/api/inventario/refacciones", service::getAllRefacciones);
        app.post("/api/inventario/refacciones", service::createRefaccion);

        // Tarjetas Venta
        app.get("/api/inventario/tarjetas-venta", service::getAllTarjetasVenta);
        app.post("/api/inventario/tarjetas-venta", service::createTarjetaVenta);

        // Material Usage Tracking
        app.post("/api/productos/uso", service::registrarUsoMaterial);
        app.get("/api/servicios/{id}/materiales", service::obtenerMaterialesServicio);
    }
}
