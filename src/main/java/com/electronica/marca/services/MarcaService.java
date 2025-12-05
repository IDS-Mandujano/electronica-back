package com.electronica.marca.services;

import com.electronica.marca.models.Marca;
import com.electronica.marca.repository.MarcaRepository;
import io.javalin.http.Context;

import java.util.Map;

public class MarcaService {

    private final MarcaRepository repository;

    public MarcaService(MarcaRepository repository) {
        this.repository = repository;
    }

    public void getAll(Context ctx) {
        try {
            ctx.json(Map.of(
                    "success", true,
                    "data", repository.findAll()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al obtener marcas: " + e.getMessage()));
        }
    }

    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            String nombre = (String) body.get("nombreMarca");

            if (nombre == null || nombre.isEmpty()) {
                throw new IllegalArgumentException("El nombre de la marca es obligatorio");
            }

            Marca marca = new Marca();
            marca.setNombreMarca(nombre);

            Marca saved = repository.save(marca);
            ctx.status(201).json(Map.of(
                    "success", true,
                    "data", saved));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("success", false, "message", "Error al crear marca"));
        }
    }
}
