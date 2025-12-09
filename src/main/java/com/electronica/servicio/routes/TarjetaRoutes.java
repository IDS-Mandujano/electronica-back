package com.electronica.servicio.routes;

import com.electronica.servicio.services.ServicioService;
import io.javalin.Javalin;

public class TarjetaRoutes {
    public static void register(Javalin app, ServicioService service) {
        // Map /api/tarjetas to ServicioService methods
        app.get("/api/tarjetas", service::getAll);
        app.post("/api/tarjetas", service::create);
        app.get("/api/tarjetas/{id}", service::getById);
        app.put("/api/tarjetas/{id}", service::update);
    }
}
