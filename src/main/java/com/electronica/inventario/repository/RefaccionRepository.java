package com.electronica.inventario.repository;

import com.electronica.inventario.models.Refaccion;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RefaccionRepository {

    private final DataSource dataSource;

    public RefaccionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Refaccion> findAll() {
        List<Refaccion> refacciones = new ArrayList<>();
        String sql = "SELECT * FROM inventario_refacciones";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Refaccion r = new Refaccion();
                r.setId(rs.getString("id"));
                r.setNombrePieza(rs.getString("nombre_pieza"));
                r.setCategoria(rs.getString("categoria"));
                r.setStockActual(rs.getInt("stock_actual"));
                r.setStockMinimo(rs.getInt("stock_minimo"));
                r.setUnidadMedida(rs.getString("unidad_medida"));
                r.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
                refacciones.add(r);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar refacciones", e);
        }
        return refacciones;
    }

    public java.util.Optional<Refaccion> findById(String id) {
        String sql = "SELECT * FROM inventario_refacciones WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Refaccion r = new Refaccion();
                    r.setId(rs.getString("id"));
                    r.setNombrePieza(rs.getString("nombre_pieza"));
                    r.setCategoria(rs.getString("categoria"));
                    r.setStockActual(rs.getInt("stock_actual"));
                    r.setStockMinimo(rs.getInt("stock_minimo"));
                    r.setUnidadMedida(rs.getString("unidad_medida"));
                    r.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
                    return java.util.Optional.of(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar refaccion", e);
        }
        return java.util.Optional.empty();
    }

    public void deleteById(String id) {
        String sql = "DELETE FROM inventario_refacciones WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar refaccion", e);
        }
    }

    public Refaccion save(Refaccion refaccion) {
        // Check if exists to decide INSERT or UPDATE
        if (findById(refaccion.getId()).isPresent()) {
            return update(refaccion);
        }

        String sql = "INSERT INTO inventario_refacciones (id, nombre_pieza, categoria, stock_actual, stock_minimo, unidad_medida, costo_unitario) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, refaccion.getId());
            stmt.setString(2, refaccion.getNombrePieza());
            stmt.setString(3, refaccion.getCategoria());
            stmt.setInt(4, refaccion.getStockActual());
            stmt.setInt(5, refaccion.getStockMinimo());
            stmt.setString(6, refaccion.getUnidadMedida());
            stmt.setBigDecimal(7, refaccion.getCostoUnitario());

            stmt.executeUpdate();
            return refaccion;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar refaccion", e);
        }
    }

    public Refaccion update(Refaccion refaccion) {
        String sql = "UPDATE inventario_refacciones SET nombre_pieza=?, categoria=?, stock_actual=?, stock_minimo=?, unidad_medida=?, costo_unitario=? WHERE id=?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, refaccion.getNombrePieza());
            stmt.setString(2, refaccion.getCategoria());
            stmt.setInt(3, refaccion.getStockActual());
            stmt.setInt(4, refaccion.getStockMinimo());
            stmt.setString(5, refaccion.getUnidadMedida());
            stmt.setBigDecimal(6, refaccion.getCostoUnitario());
            stmt.setString(7, refaccion.getId());
            stmt.executeUpdate();
            return refaccion;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar refaccion", e);
        }
    }
}
