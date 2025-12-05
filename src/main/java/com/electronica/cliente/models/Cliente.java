package com.electronica.cliente.models;

import java.util.UUID;

public class Cliente {
    private String id;
    private String nombre;
    private String apellidos;
    private String numeroCelular;

    // Constructor para crear nuevo (genera ID)
    public Cliente(String nombre, String apellidos, String numeroCelular) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.numeroCelular = numeroCelular;
    }

    // Constructor para reconstruir desde BD
    public Cliente(String id, String nombre, String apellidos, String numeroCelular) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.numeroCelular = numeroCelular;
    }

    // --- GETTERS ---
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    // --- SETTERS ---
    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }
}