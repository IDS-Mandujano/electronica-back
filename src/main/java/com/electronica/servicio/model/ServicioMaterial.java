package com.electronica.servicio.model;

import java.time.LocalDateTime;

public class ServicioMaterial {
    private String id;
    private String servicioId;
    private String materialId;
    private int cantidadUsada;
    private LocalDateTime fechaUso;

    // Constructor vacío
    public ServicioMaterial() {
    }

    // Constructor completo
    public ServicioMaterial(String id, String servicioId, String materialId, int cantidadUsada,
            LocalDateTime fechaUso) {
        this.id = id;
        this.servicioId = servicioId;
        this.materialId = materialId;
        this.cantidadUsada = cantidadUsada;
        this.fechaUso = fechaUso;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServicioId() {
        return servicioId;
    }

    public void setServicioId(String servicioId) {
        this.servicioId = servicioId;
    }

    public String getMaterialId() {
        return materialId;
    }

    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    public int getCantidadUsada() {
        return cantidadUsada;
    }

    public void setCantidadUsada(int cantidadUsada) {
        this.cantidadUsada = cantidadUsada;
    }

    public LocalDateTime getFechaUso() {
        return fechaUso;
    }

    public void setFechaUso(LocalDateTime fechaUso) {
        this.fechaUso = fechaUso;
    }
}
