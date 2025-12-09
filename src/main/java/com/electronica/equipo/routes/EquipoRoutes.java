package com.electronica.equipo.routes;

import com.electronica.equipo.services.EquipoService;
import io.javalin.Javalin;

public class EquipoRoutes {
    public static void register(Javalin app, EquipoService service) {
        app.post("/api/equipos", service::create);
        app.get("/api/equipos/cliente/{clienteId}", service::getByCliente);
    }
}
