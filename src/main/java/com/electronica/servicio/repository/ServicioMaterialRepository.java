package com.electronica.servicio.repository;

import com.electronica.servicio.model.ServicioMaterial;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServicioMaterialRepository {
    private final DataSource dataSource;

    public ServicioMaterialRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Registra el uso de un material en un servicio
     */
    public void registrarUso(String servicioId, String materialId, int cantidad) {
        String sql = "INSERT INTO servicios_materiales (id, servicio_id, material_id, cantidad_usada) VALUES (?, ?, ?, ?) "
                +
                "ON DUPLICATE KEY UPDATE cantidad_usada = cantidad_usada + ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, servicioId);
            stmt.setString(3, materialId);
            stmt.setInt(4, cantidad);
            stmt.setInt(5, cantidad); // Para el UPDATE si ya existe

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar uso de material", e);
        }
    }

    /**
     * Reduce el stock del material en el inventario
     */
    public void restarStock(String materialId, int cantidad) {
        String sql = "UPDATE inventario_refacciones SET stock_actual = stock_actual - ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cantidad);
            stmt.setString(2, materialId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalArgumentException("Material no encontrado");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al reducir stock", e);
        }
    }

    /**
     * Verifica si hay suficiente stock antes de usar
     */
    public boolean verificarStock(String materialId, int cantidadRequerida) {
        String sql = "SELECT stock_actual FROM inventario_refacciones WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, materialId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int stockActual = rs.getInt("stock_actual");
                return stockActual >= cantidadRequerida;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar stock", e);
        }
    }

    /**
     * Obtiene todos los materiales usados en un servicio específico
     */
    public List<ServicioMaterial> obtenerMaterialesPorServicio(String servicioId) {
        String sql = "SELECT sm.id, sm.servicio_id, sm.material_id, sm.cantidad_usada, sm.fecha_uso, " +
                "ir.nombre_pieza, ir.categoria " +
                "FROM servicios_materiales sm " +
                "JOIN inventario_refacciones ir ON sm.material_id = ir.id " +
                "WHERE sm.servicio_id = ?";

        List<ServicioMaterial> materiales = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servicioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ServicioMaterial sm = new ServicioMaterial();
                sm.setId(rs.getString("id"));
                sm.setServicioId(rs.getString("servicio_id"));
                sm.setMaterialId(rs.getString("material_id"));
                sm.setCantidadUsada(rs.getInt("cantidad_usada"));

                Timestamp timestamp = rs.getTimestamp("fecha_uso");
                if (timestamp != null) {
                    sm.setFechaUso(timestamp.toLocalDateTime());
                }

                materiales.add(sm);
            }

            return materiales;

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener materiales del servicio", e);
        }
    }

    /**
     * Obtiene la vista completa de materiales con información adicional
     */
    public List<java.util.Map<String, Object>> obtenerMaterialesConDetalles(String servicioId) {
        String sql = "SELECT * FROM vista_materiales_servicios WHERE servicio_id = ?";

        List<java.util.Map<String, Object>> materiales = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, servicioId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                java.util.Map<String, Object> material = new java.util.HashMap<>();
                material.put("id", rs.getString("id"));
                material.put("nombrePieza", rs.getString("nombre_pieza"));
                material.put("categoria", rs.getString("categoria"));
                material.put("cantidadUsada", rs.getInt("cantidad_usada"));
                material.put("fechaUso", rs.getTimestamp("fecha_uso"));

                materiales.add(material);
            }

            return materiales;

        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener materiales con detalles", e);
        }
    }
}
