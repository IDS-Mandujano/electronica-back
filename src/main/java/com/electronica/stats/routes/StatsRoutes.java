package com.electronica.stats.routes;

import com.electronica.stats.services.StatsService;
import io.javalin.Javalin;

public class StatsRoutes {
    public static void register(Javalin app, StatsService service) {
        app.get("/api/stats/summary", service::getSummary);
        app.get("/api/stats/chart", service::getChartData);
    }
}
