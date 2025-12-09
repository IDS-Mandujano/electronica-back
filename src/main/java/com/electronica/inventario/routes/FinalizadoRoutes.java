package com.electronica.inventario.routes;

import com.electronica.servicio.services.ServicioService;
import io.javalin.Javalin;

public class FinalizadoRoutes {
    public static void register(Javalin app, ServicioService service) {
        app.get("/api/finalizado", service::getFinalizados);
        app.post("/api/finalizado", service::finalizarOrden);
        app.get("/api/finalizado/{id}", service::getFinalizadoById);
        app.delete("/api/finalizado/{id}", service::deleteFinalizado);
        app.put("/api/finalizado/{id}", service::updateFinalizado);
    }
}
