package com.electronica.servicio.services;

import com.electronica.servicio.models.Servicio;
import com.electronica.servicio.repository.ServicioRepository;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ServicioService {

    private final ServicioRepository repository;

    public ServicioService(ServicioRepository repository) {
        this.repository = repository;
    }

    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            Object equipoIdObj = body.get("equipoId");
            String equipoId = equipoIdObj != null ? String.valueOf(equipoIdObj) : null;

            Object tecnicoIdObj = body.get("tecnicoId");
            String tecnicoId = tecnicoIdObj != null ? String.valueOf(tecnicoIdObj) : null;

            String problema = (String) body.get("problemaReportado");

            if (equipoId == null || tecnicoId == null || problema == null) {
                throw new IllegalArgumentException("Datos incompletos para crear servicio");
            }

            Servicio servicio = new Servicio();
            servicio.setId(UUID.randomUUID().toString());
            servicio.setEquipoId(equipoId);
            servicio.setTecnicoId(tecnicoId);
            servicio.setProblemaReportado(problema);
            servicio.setFechaIngreso(LocalDateTime.now());
            servicio.setEstado("PENDIENTE");

            Servicio saved = repository.save(servicio);
            ctx.status(201).json(Map.of(
                    "success", true,
                    "data", saved));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("success", false, "message",
                    "Error al crear servicio: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")"));
        }
    }

    public void getAll(Context ctx) {
        try {
            ctx.json(Map.of(
                    "success", true,
                    "data", repository.findAll()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al obtener servicios: " + e.getMessage()));
        }
    }

    public void getById(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Servicio s = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

            ctx.json(Map.of(
                    "success", true,
                    "data", s));
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al obtener servicio: " + e.getMessage()));
        }
    }

    public void update(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            System.out.println("🔄 Update Request for ID: " + id);
            System.out.println("📦 Body: " + body);

            // Generic update - primarily for status/diagnosis
            String diagnostico = (String) body.getOrDefault("diagnosticoTecnico", "");
            String estado = (String) body.get("estado");
            String fechaEstStr = (String) body.get("fechaEstimadaEntrega");
            LocalDateTime fechaEst = fechaEstStr != null ? LocalDateTime.parse(fechaEstStr) : null;

            // Update diagnosis and status (técnico puede cambiar a FINALIZADO aquí)
            // Solo actualiza el estado, NO llama a finalizarServicio
            if (estado != null) {
                repository.updateDiagnostico(id, diagnostico, estado, fechaEst);
            }

            // NOTA: finalizarServicio() solo se llama desde finalizarOrden()
            // que es el método usado por el gerente en "Entrega de Producto"
            // El técnico solo cambia el estado a FINALIZADO, pero NO finaliza la orden

            ctx.json(Map.of("success", true, "message", "Servicio actualizado"));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500)
                    .json(Map.of("success", false, "message", "Error al actualizar servicio: " + e.getMessage()));
        }
    }

    public void updateDiagnostico(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String diagnostico = (String) body.get("diagnosticoTecnico");
            String estado = (String) body.get("estado");
            String fechaEstStr = (String) body.get("fechaEstimadaEntrega"); // ISO format expected

            LocalDateTime fechaEst = fechaEstStr != null ? LocalDateTime.parse(fechaEstStr) : null;

            repository.updateDiagnostico(id, diagnostico, estado, fechaEst);
            ctx.json(Map.of("success", true, "message", "Diagnóstico actualizado"));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al actualizar diagnostico"));
        }
    }

    public void finalizar(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            BigDecimal costo = new BigDecimal(body.get("costoReparacion").toString());

            repository.finalizarServicio(id, LocalDateTime.now(), null, costo);
            ctx.json(Map.of("success", true, "message", "Servicio finalizado"));

        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al finalizar servicio"));
        }
    }

    // --- Métodos para /api/finalizado (Compatibilidad Frontend) ---

    public void getFinalizados(Context ctx) {
        try {
            // Retorna solo los servicios finalizados
            // Por ahora retornamos todos y dejamos que el frontend filtre o idealmente
            // filtramos aquí
            // Simplificación: reutilizamos findAll pero podríamos filtrar
            ctx.json(Map.of(
                    "success", true,
                    "data", repository.findAll().stream()
                            .filter(s -> "FINALIZADO".equalsIgnoreCase(s.getEstado())
                                    || "ENTREGADO".equalsIgnoreCase(s.getEstado()))
                            .toList()));
        } catch (Exception e) {
            ctx.status(500)
                    .json(Map.of("success", false, "message", "Error al obtener finalizados: " + e.getMessage()));
        }
    }

    public void finalizarOrden(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            // El frontend envía 'registroTarjetaId' como el ID del servicio
            String id = (String) body.get("registroTarjetaId");

            if (id == null) {
                throw new IllegalArgumentException("ID de servicio no proporcionado (registroTarjetaId)");
            }

            Object costoObj = body.get("costoReparacion");
            BigDecimal costo = costoObj != null ? new BigDecimal(costoObj.toString()) : BigDecimal.ZERO;

            // Fecha de entrega estimada o real? El frontend manda 'fechaEntrega'
            String fechaEntregaStr = (String) body.get("fechaEntrega");
            LocalDateTime fechaEntrega = fechaEntregaStr != null ? LocalDateTime.parse(fechaEntregaStr + "T00:00:00")
                    : null;

            repository.finalizarServicio(id, LocalDateTime.now(), fechaEntrega, costo);

            ctx.json(Map.of("success", true, "message", "Orden finalizada correctamente"));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("success", false, "message", "Error al finalizar orden: " + e.getMessage()));
        }
    }

    public void getFinalizadoById(Context ctx) {
        String id = ctx.pathParam("id");
        Optional<Servicio> servicio = repository.findById(id);
        if (servicio.isPresent()) {
            ctx.json(Map.of("success", true, "data", servicio.get()));
        } else {
            ctx.status(404).json(Map.of("success", false, "message", "Servicio no encontrado"));
        }
    }

    public void deleteFinalizado(Context ctx) {
        String id = ctx.pathParam("id");
        try {
            repository.delete(id);
            ctx.json(Map.of("success", true, "message", "Servicio eliminado correctamente"));
        } catch (Exception e) {
            ctx.status(500)
                    .json(Map.of("success", false, "message", "Error al eliminar servicio: " + e.getMessage()));
        }
    }

    public void updateFinalizado(Context ctx) {
        String id = ctx.pathParam("id");
        System.out.println("🔵 updateFinalizado llamado con ID: " + id);
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            System.out.println("📦 Body recibido: " + body);

            // 1. Recuperar el servicio actual
            Optional<Servicio> opt = repository.findById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Servicio no encontrado con ID: " + id);
                ctx.status(404).json(Map.of("success", false, "message", "Servicio no encontrado"));
                return;
            }
            Servicio servicio = opt.get();

            // 2. Actualizar campos permitidos
            if (body.containsKey("costoReparacion")) {
                Object costoObj = body.get("costoReparacion");
                servicio.setCostoReparacion(costoObj != null ? new BigDecimal(costoObj.toString()) : BigDecimal.ZERO);
            }

            if (body.containsKey("problemaCambiado")) {
                String problema = (String) body.get("problemaCambiado");
                if (problema != null && !problema.isEmpty()) {
                    servicio.setDiagnosticoTecnico(problema);
                }
            }

            if (body.containsKey("fechaEntrega")) {
                String fechaStr = (String) body.get("fechaEntrega");
                if (fechaStr != null && !fechaStr.isEmpty()) {
                    if (fechaStr.length() == 10)
                        fechaStr += "T00:00:00";
                    servicio.setFechaEntregaCliente(LocalDateTime.parse(fechaStr));
                }
            }

            // 3. Asegurar que fechaFinalizacion existe
            if (servicio.getFechaFinalizacion() == null) {
                servicio.setFechaFinalizacion(LocalDateTime.now());
            }

            // 4. Guardar cambios
            repository.finalizarServicio(id, servicio.getFechaFinalizacion(), servicio.getFechaEntregaCliente(),
                    servicio.getCostoReparacion());

            ctx.json(Map.of("success", true, "message", "Servicio actualizado correctamente"));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500)
                    .json(Map.of("success", false, "message", "Error al actualizar servicio: " + e.getMessage()));
        }
    }
}
