package com.electronica.marca.repository;

import com.electronica.marca.models.Marca;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MarcaRepository {

    private final DataSource dataSource;

    public MarcaRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Marca> findAll() {
        List<Marca> marcas = new ArrayList<>();
        String sql = "SELECT * FROM marcas";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                marcas.add(new Marca(
                        rs.getInt("id"),
                        rs.getString("nombre_marca")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar marcas", e);
        }
        return marcas;
    }

    public Marca save(Marca marca) {
        String sql = "INSERT INTO marcas (nombre_marca) VALUES (?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, marca.getNombreMarca());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    marca.setId(generatedKeys.getInt(1));
                }
            }
            return marca;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar marca", e);
        }
    }
}
