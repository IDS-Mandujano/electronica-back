package com.electronica.auth.repositories;

import com.electronica.auth.models.User;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeDatabase();
    }

    private void initializeDatabase() {
        String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS users (
                        id VARCHAR(36) PRIMARY KEY,
                        nombre_completo VARCHAR(255) NOT NULL,
                        correo_electronico VARCHAR(255) UNIQUE NOT NULL,
                        contrasena VARCHAR(255) NOT NULL,
                        tipo VARCHAR(50) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Tabla 'users' inicializada");
        } catch (SQLException e) {
            throw new RuntimeException("Error al inicializar tabla users", e);
        }
    }

    public User save(User user) {
        String sql = """
                    INSERT INTO users (id, nombre_completo, correo_electronico, contrasena, tipo, created_at)
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getNombreCompleto());
            stmt.setString(3, user.getCorreoElectronico());
            stmt.setString(4, user.getContrasena());
            stmt.setString(5, user.getTipo());
            stmt.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt()));

            stmt.executeUpdate();
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE correo_electronico = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por email", e);
        }
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuario por id", e);
        }
    }

    public Optional<User> findByResetToken(String token) {
        // Not supported in current schema
        return Optional.empty();
    }

    public void updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET contrasena = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newPassword);
            stmt.setString(2, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar contraseña", e);
        }
    }

    public void updateResetToken(String userId, String token, LocalDateTime expiry) {
        // Not supported in current schema
        // We log a warning or just do nothing
        System.out.println("⚠️ updateResetToken called but schema does not support reset_token columns.");
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE correo_electronico = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de email", e);
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setNombreCompleto(rs.getString("nombre_completo"));
        user.setCorreoElectronico(rs.getString("correo_electronico"));
        user.setContrasena(rs.getString("contrasena"));
        user.setTipo(rs.getString("tipo"));

        // Reset token and updated_at columns are not in the schema provided by user
        // user.setResetToken(rs.getString("reset_token"));
        // Timestamp resetTokenExpiry = rs.getTimestamp("reset_token_expiry");
        // if (resetTokenExpiry != null) {
        // user.setResetTokenExpiry(resetTokenExpiry.toLocalDateTime());
        // }

        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        // user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        return user;
    }

    public List<User> findByTipo(String tipo) {
        String sql = "SELECT * FROM users WHERE tipo = ?";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar usuarios por tipo", e);
        }
        return users;
    }
}