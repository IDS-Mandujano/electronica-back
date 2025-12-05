package com.electronica.marca.models;

public class Marca {
    private int id;
    private String nombreMarca;

    public Marca() {
    }

    public Marca(int id, String nombreMarca) {
        this.id = id;
        this.nombreMarca = nombreMarca;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreMarca() {
        return nombreMarca;
    }

    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }
}
