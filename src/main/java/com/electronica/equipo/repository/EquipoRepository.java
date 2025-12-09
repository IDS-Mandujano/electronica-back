package com.electronica.equipo.repository;

import com.electronica.equipo.models.Equipo;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipoRepository {

    private final DataSource dataSource;

    public EquipoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Equipo save(Equipo equipo) {
        String sql = "INSERT INTO equipos (id, cliente_id, marca_id, modelo, tipo_equipo, numero_serie) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, equipo.getId());
            stmt.setString(2, equipo.getClienteId());
            stmt.setInt(3, equipo.getMarcaId());
            stmt.setString(4, equipo.getModelo());
            stmt.setString(5, equipo.getTipoEquipo());
            stmt.setString(6, equipo.getNumeroSerie());

            stmt.executeUpdate();
            return equipo;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar equipo", e);
        }
    }

    public List<Equipo> findByClienteId(String clienteId) {
        List<Equipo> equipos = new ArrayList<>();
        String sql = "SELECT * FROM equipos WHERE cliente_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, clienteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                equipos.add(new Equipo(
                        rs.getString("id"),
                        rs.getString("cliente_id"),
                        rs.getInt("marca_id"),
                        rs.getString("modelo"),
                        rs.getString("tipo_equipo"),
                        rs.getString("numero_serie")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar equipos por cliente", e);
        }
        return equipos;
    }

    public Optional<Equipo> findById(String id) {
        String sql = "SELECT * FROM equipos WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new Equipo(
                        rs.getString("id"),
                        rs.getString("cliente_id"),
                        rs.getInt("marca_id"),
                        rs.getString("modelo"),
                        rs.getString("tipo_equipo"),
                        rs.getString("numero_serie")));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar equipo por ID", e);
        }
    }
}
