package com.electronica.equipo.models;

public class Equipo {
    private String id;
    private String clienteId;
    private int marcaId;
    private String modelo;
    private String tipoEquipo;
    private String numeroSerie;

    public Equipo() {
    }

    public Equipo(String id, String clienteId, int marcaId, String modelo, String tipoEquipo, String numeroSerie) {
        this.id = id;
        this.clienteId = clienteId;
        this.marcaId = marcaId;
        this.modelo = modelo;
        this.tipoEquipo = tipoEquipo;
        this.numeroSerie = numeroSerie;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public int getMarcaId() {
        return marcaId;
    }

    public void setMarcaId(int marcaId) {
        this.marcaId = marcaId;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getTipoEquipo() {
        return tipoEquipo;
    }

    public void setTipoEquipo(String tipoEquipo) {
        this.tipoEquipo = tipoEquipo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }
}
