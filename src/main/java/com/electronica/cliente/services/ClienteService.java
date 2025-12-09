package com.electronica.cliente.services;

import com.electronica.cliente.models.Cliente;
import com.electronica.cliente.repository.ClienteRepository;
import io.javalin.http.Context;

import java.util.*;

public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    // ==========================
    // CREATE
    // ==========================
    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String nombre = (String) body.get("nombre");
            String apellidos = (String) body.get("apellidos");
            String numero = (String) body.get("numeroCelular");

            if (nombre == null || nombre.isEmpty())
                throw new IllegalArgumentException("El nombre es obligatorio");

            if (apellidos == null || apellidos.isEmpty())
                throw new IllegalArgumentException("Los apellidos son obligatorios");

            if (numero == null || numero.isEmpty())
                throw new IllegalArgumentException("El número de celular es obligatorio");

            // Validar que no exista ya el cliente
            // Validar que no exista ya el cliente
            repository.findByNumero(numero).ifPresent(c -> {
                if (c.getDeletedAt() != null) {
                    // Si está eliminado, lo reactivamos
                    repository.reactivate(c.getId());
                    // Actualizamos datos si cambiaron
                    repository.update(numero, new Cliente(c.getId(), nombre, apellidos, numero, null));
                } else {
                    throw new IllegalArgumentException("Ya existe un cliente con ese número");
                }
            });

            // Si se reactivó, el flujo continúa pero intentará guardar otro? No.
            // Debemos manejarlo.
            // Simplificación: Si reactivamos, lo devolvemos y salimos.

            Optional<Cliente> existente = repository.findByNumero(numero);
            if (existente.isPresent()) {
                // Ya fue reactivado o ya existía (si pasamos aquí es que fue reactivado porque
                // si no, lanzamos excepción)
                ctx.status(200).json(Map.of(
                        "success", true,
                        "data", toMap(existente.get())));
                return;
            }

            Cliente nuevo = new Cliente(nombre, apellidos, numero);
            Cliente saved = repository.save(nuevo);

            ctx.status(201).json(Map.of(
                    "success", true,
                    "data", toMap(saved)));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==========================
    // GET ALL
    // ==========================
    public void getAll(Context ctx) {
        try (var conn = repository.getDataSource().getConnection()) {

            // Query con JOIN para obtener el total de pedidos por cliente
            // Filtra clientes eliminados (deleted_at IS NULL)
            String sql = "SELECT c.*, COUNT(s.id) as total_pedidos " +
                    "FROM clientes c " +
                    "LEFT JOIN equipos e ON c.id = e.cliente_id " +
                    "LEFT JOIN servicios s ON e.id = s.equipo_id " +
                    "WHERE c.deleted_at IS NULL " +
                    "GROUP BY c.id, c.nombre, c.apellidos, c.numero_celular " +
                    "ORDER BY c.nombre";

            var stmt = conn.prepareStatement(sql);
            var rs = stmt.executeQuery();

            List<Map<String, Object>> lista = new ArrayList<>();

            while (rs.next()) {
                Map<String, Object> clienteMap = new HashMap<>();
                clienteMap.put("id", rs.getString("id"));
                clienteMap.put("nombre", rs.getString("nombre"));
                clienteMap.put("apellidos", rs.getString("apellidos"));
                clienteMap.put("numeroCelular", rs.getString("numero_celular"));
                clienteMap.put("totalPedidos", rs.getInt("total_pedidos"));
                lista.add(clienteMap);
            }

            ctx.json(Map.of(
                    "success", true,
                    "data", lista));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error al obtener clientes: " + e.getMessage()));
        }
    }

    // ==========================
    // GET BY ID
    // ==========================
    public void getById(Context ctx) {
        try {
            String id = ctx.pathParam("id");

            Cliente c = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

            ctx.json(Map.of(
                    "success", true,
                    "data", toMap(c)));

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==========================
    // GET BY NUMERO
    // ==========================
    public void getByNumero(Context ctx) {
        try {
            String numero = ctx.pathParam("numero");

            Cliente c = repository.findActiveByNumero(numero)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

            ctx.json(Map.of(
                    "success", true,
                    "data", toMap(c)));

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==========================
    // UPDATE
    // ==========================
    public void update(Context ctx) {
        try {
            // 1. El número original viene en la URL
            String numeroOriginal = ctx.pathParam("numero");
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            // 2. Buscar el cliente existente
            Cliente existente = repository.findByNumero(numeroOriginal)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

            // 3. Obtenemos los datos nuevos del JSON
            String nuevoNombre = (String) body.get("nombre");
            String nuevosApellidos = (String) body.get("apellidos");
            String nuevoCelular = (String) body.get("numeroCelular");

            // 4. Actualizamos el objeto en memoria
            if (nuevoNombre != null && !nuevoNombre.isEmpty()) {
                existente.setNombre(nuevoNombre);
            }

            if (nuevosApellidos != null && !nuevosApellidos.isEmpty()) {
                existente.setApellidos(nuevosApellidos);
            }

            if (nuevoCelular != null && !nuevoCelular.isEmpty()) {
                existente.setNumeroCelular(nuevoCelular);
            }

            // 5. Pasamos el número original (para el WHERE) y el objeto actualizado (para
            // el SET)
            repository.update(numeroOriginal, existente);

            ctx.json(Map.of(
                    "success", true,
                    "data", toMap(existente)));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of("success", false, "message", "Error interno: " + e.getMessage()));
        }
    }

    // ==========================
    // DELETE
    // ==========================
    // SOFT DELETE: Marca el cliente como eliminado sin borrarlo físicamente
    // Esto preserva el historial de pedidos y servicios
    public void delete(Context ctx) {
        try {
            String numero = ctx.pathParam("numero");

            Cliente cliente = repository.findByNumero(numero)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

            // Soft delete: marcar como eliminado en lugar de borrar
            repository.softDelete(numero);

            ctx.status(204);

        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        }
    }

    // ==========================
    // MAP RESPONSE
    // ==========================
    private Map<String, Object> toMap(Cliente c) {
        return Map.of(
                "id", c.getId(),
                "nombre", c.getNombre(),
                "apellidos", c.getApellidos(),
                "numeroCelular", c.getNumeroCelular());
    }
}
