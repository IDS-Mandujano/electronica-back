package com.electronica.config;

import io.javalin.Javalin;

public class CorsConfig {

    public static void configure(Javalin app) {
        app.before(ctx -> {
            ctx.header("Access-Control-Allow-Origin", "*");
            ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
            ctx.header("Access-Control-Max-Age", "3600");
        });

        app.options("/*", ctx -> {
            ctx.status(204);
        });
    }
}