package com.electronica.servicio.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Servicio {
    private String id;
    private int folioServicio;
    private String equipoId;
    private String tecnicoId;
    private String problemaReportado;
    private LocalDateTime fechaIngreso;
    private String diagnosticoTecnico;
    private String estado; // PENDIENTE, EN_PROCESO, etc.
    private LocalDateTime fechaEstimadaEntrega;
    private LocalDateTime fechaFinalizacion;
    private LocalDateTime fechaEntregaCliente;
    private BigDecimal costoReparacion;

    // Fields from View (vista_servicios_completa)
    private String nombreCliente;
    private String numeroCelular;
    private String marca;
    private String modelo;
    private String tecnicoNombre;

    public Servicio() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getFolioServicio() {
        return folioServicio;
    }

    public void setFolioServicio(int folioServicio) {
        this.folioServicio = folioServicio;
    }

    public String getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(String equipoId) {
        this.equipoId = equipoId;
    }

    public String getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(String tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    public String getProblemaReportado() {
        return problemaReportado;
    }

    public void setProblemaReportado(String problemaReportado) {
        this.problemaReportado = problemaReportado;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getDiagnosticoTecnico() {
        return diagnosticoTecnico;
    }

    public void setDiagnosticoTecnico(String diagnosticoTecnico) {
        this.diagnosticoTecnico = diagnosticoTecnico;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaEstimadaEntrega() {
        return fechaEstimadaEntrega;
    }

    public void setFechaEstimadaEntrega(LocalDateTime fechaEstimadaEntrega) {
        this.fechaEstimadaEntrega = fechaEstimadaEntrega;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public LocalDateTime getFechaEntregaCliente() {
        return fechaEntregaCliente;
    }

    public void setFechaEntregaCliente(LocalDateTime fechaEntregaCliente) {
        this.fechaEntregaCliente = fechaEntregaCliente;
    }

    public BigDecimal getCostoReparacion() {
        return costoReparacion;
    }

    public void setCostoReparacion(BigDecimal costoReparacion) {
        this.costoReparacion = costoReparacion;
    }

    // Getters and Setters for View Fields
    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public void setTecnicoNombre(String tecnicoNombre) {
        this.tecnicoNombre = tecnicoNombre;
    }
}
