package com.electronica.marca.routes;

import com.electronica.marca.services.MarcaService;
import io.javalin.Javalin;

public class MarcaRoutes {
    public static void register(Javalin app, MarcaService service) {
        app.get("/api/marcas", service::getAll);
        app.post("/api/marcas", service::create);
    }
}
