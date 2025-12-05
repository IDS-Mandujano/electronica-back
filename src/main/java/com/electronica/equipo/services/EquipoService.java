package com.electronica.equipo.services;

import com.electronica.equipo.models.Equipo;
import com.electronica.equipo.repository.EquipoRepository;
import io.javalin.http.Context;

import java.util.Map;
import java.util.UUID;

public class EquipoService {

    private final EquipoRepository repository;

    public EquipoService(EquipoRepository repository) {
        this.repository = repository;
    }

    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            String clienteId = (String) body.get("clienteId");
            Integer marcaId = (Integer) body.get("marcaId");
            String modelo = (String) body.get("modelo");
            String tipoEquipo = (String) body.get("tipoEquipo");
            String numeroSerie = (String) body.get("numeroSerie");

            if (clienteId == null || marcaId == null) {
                throw new IllegalArgumentException("Cliente ID y Marca ID son obligatorios");
            }

            Equipo equipo = new Equipo();
            equipo.setId(UUID.randomUUID().toString());
            equipo.setClienteId(clienteId);
            equipo.setMarcaId(marcaId);
            equipo.setModelo(modelo);
            equipo.setTipoEquipo(tipoEquipo != null ? tipoEquipo : "Tarjeta Electrónica");
            equipo.setNumeroSerie(numeroSerie);

            Equipo saved = repository.save(equipo);
            ctx.status(201).json(saved);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", "Error al crear equipo"));
        }
    }

    public void getByCliente(Context ctx) {
        String clienteId = ctx.pathParam("clienteId");
        ctx.json(repository.findByClienteId(clienteId));
    }
}
