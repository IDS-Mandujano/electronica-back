package com.electronica.cliente.routes;

import com.electronica.config.JwtConfig;
import com.electronica.cliente.services.ClienteService;
import io.javalin.Javalin;

public class ClienteRoutes {

    public static void register(Javalin app, ClienteService service) {

        // Protegemos todas las rutas del módulo
        app.before("/api/clientes*", JwtConfig::validateToken);

        app.post("/api/clientes", service::create);
        app.get("/api/clientes", service::getAll);
        app.get("/api/clientes/id/{id}", service::getById);
        app.get("/api/clientes/{numero}", service::getByNumero);
        app.put("/api/clientes/{numero}", service::update);
        app.delete("/api/clientes/{numero}", service::delete);
    }
}
