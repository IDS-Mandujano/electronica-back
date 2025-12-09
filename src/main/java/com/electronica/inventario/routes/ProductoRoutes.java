package com.electronica.inventario.routes;

import com.electronica.inventario.services.InventarioService;
import io.javalin.Javalin;

public class ProductoRoutes {
    public static void register(Javalin app, InventarioService service) {
        app.get("/api/productos", service::getAllRefacciones);
        app.get("/api/productos/{id}", service::getRefaccionById);
        app.post("/api/productos", service::createRefaccion);
        app.put("/api/productos/{id}", service::updateRefaccion);
        app.delete("/api/productos/{id}", service::deleteRefaccion);
    }
}
