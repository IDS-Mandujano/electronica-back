package com.electronica.servicio.routes;

import com.electronica.servicio.services.ServicioService;
import io.javalin.Javalin;

public class ServicioRoutes {
    public static void register(Javalin app, ServicioService service) {
        app.get("/api/servicios", service::getAll);
        app.post("/api/servicios", service::create);
        app.put("/api/servicios/{id}/diagnostico", service::updateDiagnostico);
        app.put("/api/servicios/{id}/finalizar", service::finalizar);
    }
}
