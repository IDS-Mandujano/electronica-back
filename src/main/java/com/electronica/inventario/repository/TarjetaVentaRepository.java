package com.electronica.inventario.repository;

import com.electronica.inventario.models.TarjetaVenta;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TarjetaVentaRepository {

    private final DataSource dataSource;

    public TarjetaVentaRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<TarjetaVenta> findAll() {
        List<TarjetaVenta> tarjetas = new ArrayList<>();
        String sql = "SELECT * FROM tarjetas_venta";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TarjetaVenta t = new TarjetaVenta();
                t.setId(rs.getString("id"));
                t.setMarcaId(rs.getInt("marca_id"));
                t.setModelo(rs.getString("modelo"));
                t.setDescripcion(rs.getString("descripcion"));
                t.setPrecioVenta(rs.getBigDecimal("precio_venta"));
                t.setEstado(rs.getString("estado"));

                Timestamp fecha = rs.getTimestamp("fecha_venta");
                if (fecha != null)
                    t.setFechaVenta(fecha.toLocalDateTime());

                tarjetas.add(t);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar tarjetas en venta", e);
        }
        return tarjetas;
    }

    public TarjetaVenta save(TarjetaVenta tarjeta) {
        String sql = "INSERT INTO tarjetas_venta (id, marca_id, modelo, descripcion, precio_venta, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tarjeta.getId());
            stmt.setInt(2, tarjeta.getMarcaId());
            stmt.setString(3, tarjeta.getModelo());
            stmt.setString(4, tarjeta.getDescripcion());
            stmt.setBigDecimal(5, tarjeta.getPrecioVenta());
            stmt.setString(6, tarjeta.getEstado());

            stmt.executeUpdate();
            return tarjeta;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar tarjeta venta", e);
        }
    }
}
