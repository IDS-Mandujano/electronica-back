package com.electronica;

import com.electronica.auth.repositories.UserRepository;
import com.electronica.auth.routes.AuthRoutes;
import com.electronica.auth.services.AuthService;
import com.electronica.auth.services.EmailService;
import com.electronica.cliente.repository.ClienteRepository;
import com.electronica.cliente.routes.ClienteRoutes;
import com.electronica.cliente.services.ClienteService;
import com.electronica.config.DatabaseConfig;
import com.electronica.config.EnvConfig;
import com.electronica.equipo.repository.EquipoRepository;
import com.electronica.equipo.routes.EquipoRoutes;
import com.electronica.equipo.services.EquipoService;
import com.electronica.inventario.repository.RefaccionRepository;
import com.electronica.inventario.repository.TarjetaVentaRepository;
import com.electronica.inventario.routes.InventarioRoutes;
import com.electronica.inventario.services.InventarioService;
import com.electronica.marca.repository.MarcaRepository;
import com.electronica.marca.routes.MarcaRoutes;
import com.electronica.marca.services.MarcaService;
import com.electronica.servicio.repository.ServicioRepository;
import com.electronica.servicio.routes.ServicioRoutes;
import com.electronica.servicio.services.ServicioService;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Main {

    private static final String VERSION = "2.0.0";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {
        try {
            EnvConfig.validateConfig();

            DataSource dataSource = DatabaseConfig.createDataSource();
            System.out.println("✅ Conexión a BD establecida");

            // --- AUTH ---
            UserRepository userRepo = new UserRepository(dataSource);
            EmailService emailService = new EmailService();
            AuthService authService = new AuthService(userRepo, emailService);

            // --- CLIENTE ---
            ClienteRepository clienteRepo = new ClienteRepository(dataSource);
            ClienteService clienteService = new ClienteService(clienteRepo);

            // --- MARCA ---
            MarcaRepository marcaRepo = new MarcaRepository(dataSource);
            MarcaService marcaService = new MarcaService(marcaRepo);

            // --- EQUIPO ---
            EquipoRepository equipoRepo = new EquipoRepository(dataSource);
            EquipoService equipoService = new EquipoService(equipoRepo);

            // --- SERVICIO ---
            ServicioRepository servicioRepo = new ServicioRepository(dataSource);
            ServicioService servicioService = new ServicioService(servicioRepo);

            // --- INVENTARIO ---
            RefaccionRepository refaccionRepo = new RefaccionRepository(dataSource);
            TarjetaVentaRepository tarjetaVentaRepo = new TarjetaVentaRepository(dataSource);
            InventarioService inventarioService = new InventarioService(refaccionRepo, tarjetaVentaRepo);

            // Material usage tracking
            com.electronica.servicio.repository.ServicioMaterialRepository materialRepo = new com.electronica.servicio.repository.ServicioMaterialRepository(
                    dataSource);
            inventarioService.setMaterialRepository(materialRepo);

            Javalin app = Javalin.create(config -> {
                config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
                config.jsonMapper(new JavalinJackson());
            });

            app.get("/api/health", ctx -> ctx.json(Map.of(
                    "status", "OK",
                    "version", VERSION,
                    "timestamp", LocalDateTime.now().format(FORMATTER))));

            // Register Routes
            AuthRoutes.register(app, authService);
            ClienteRoutes.register(app, clienteService);
            MarcaRoutes.register(app, marcaService);
            EquipoRoutes.register(app, equipoService);
            ServicioRoutes.register(app, servicioService);
            com.electronica.servicio.routes.TarjetaRoutes.register(app, servicioService);
            InventarioRoutes.register(app, inventarioService);

            // New Routes
            com.electronica.inventario.routes.ProductoRoutes.register(app, inventarioService);
            com.electronica.inventario.routes.FinalizadoRoutes.register(app, servicioService);

            com.electronica.stats.services.StatsService statsService = new com.electronica.stats.services.StatsService(
                    dataSource);
            com.electronica.stats.routes.StatsRoutes.register(app, statsService);

            app.error(404, ctx -> ctx.json(Map.of("success", false, "message", "Endpoint no encontrado")));
            app.exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();
                ctx.status(500).json(Map.of("success", false, "message", "Error interno del servidor"));
            });

            int port = EnvConfig.getServerPort();
            app.start(port);

            System.out.println("✅ API iniciada en http://localhost:" + port);

        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}