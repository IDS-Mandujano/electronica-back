package com.electronica.cliente.repository;

import com.electronica.cliente.models.Cliente;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class ClienteRepository {

    private final DataSource dataSource;

    public ClienteRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    // ==========================
    // BUSCAR POR NOMBRE Y NUMERO
    // ==========================
    public Optional<Cliente> findByNombreYNumero(String nombre, String numero) {
        // Filtra clientes eliminados (deleted_at IS NULL)
        String sql = "SELECT * FROM clientes WHERE nombre = ? AND numero_celular = ? AND deleted_at IS NULL LIMIT 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, numero);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("deleted_at");
                java.time.LocalDateTime deletedAt = (ts != null) ? ts.toLocalDateTime() : null;

                Cliente c = new Cliente(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("numero_celular"),
                        deletedAt);
                return Optional.of(c);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }

    // ==========================
    // GUARDAR CLIENTE
    // ==========================
    public Cliente save(Cliente cliente) {
        String sql = "INSERT INTO clientes (id, nombre, apellidos, numero_celular) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getId());
            stmt.setString(2, cliente.getNombre());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getNumeroCelular());

            stmt.executeUpdate();
            return cliente;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente", e);
        }
    }

    // ==========================
    // BUSCAR POR NUMERO
    // ==========================
    public Optional<Cliente> findByNumero(String numero) {
        String sql = "SELECT * FROM clientes WHERE numero_celular = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("deleted_at");
                java.time.LocalDateTime deletedAt = (ts != null) ? ts.toLocalDateTime() : null;

                Cliente c = new Cliente(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("numero_celular"),
                        deletedAt);
                return Optional.of(c);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente por número", e);
        }
    }

    // ==========================
    // BUSCAR POR ID
    // ==========================
    public Optional<Cliente> findById(String id) {
        String sql = "SELECT * FROM clientes WHERE id = ? LIMIT 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("deleted_at");
                java.time.LocalDateTime deletedAt = (ts != null) ? ts.toLocalDateTime() : null;

                Cliente c = new Cliente(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("numero_celular"),
                        deletedAt);
                return Optional.of(c);
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente por ID", e);
        }
    }

    // ==========================
    // ACTUALIZAR CLIENTE
    // ==========================
    public void update(String numeroOriginal, Cliente cliente) {
        // Solo actualizamos la tabla clientes. La cascada se maneja por FK o triggers
        // si existen,
        // pero en el código anterior se hacía manual.
        // Con la nueva estructura normalizada, 'equipos' tiene FK a 'clientes'.
        // Si actualizamos el cliente, no necesitamos actualizar 'equipos' a menos que
        // cambiemos el ID (que no hacemos).

        String sqlCliente = "UPDATE clientes SET nombre = ?, apellidos = ?, numero_celular = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlCliente)) {

            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellidos());
            stmt.setString(3, cliente.getNumeroCelular());
            stmt.setString(4, cliente.getId());

            stmt.executeUpdate();
            System.out.println("✅ Cliente actualizado correctamente.");

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente", e);
        }
    }

    // ==========================
    // SOFT DELETE: Marcar cliente como eliminado (preserva pedidos)
    // ==========================
    public void softDelete(String numero) {
        String sql = "UPDATE clientes SET deleted_at = NOW() WHERE numero_celular = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al marcar cliente como eliminado", e);
        }
    }

    // ==========================
    // ELIMINAR CLIENTE POR NUMERO (FÍSICO - No se usa con soft delete)
    // ==========================
    public void deleteByNumero(String numero) {
        String sql = "DELETE FROM clientes WHERE numero_celular = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
    }

    // ==========================
    // REACTIVAR CLIENTE
    // ==========================
    public void reactivate(String id) {
        String sql = "UPDATE clientes SET deleted_at = NULL WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al reactivar cliente", e);
        }
    }

    // ==========================
    // BUSCAR SOLO ACTIVOS POR NUMERO
    // ==========================
    public Optional<Cliente> findActiveByNumero(String numero) {
        String sql = "SELECT * FROM clientes WHERE numero_celular = ? AND deleted_at IS NULL LIMIT 1";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                java.sql.Timestamp ts = rs.getTimestamp("deleted_at");
                java.time.LocalDateTime deletedAt = (ts != null) ? ts.toLocalDateTime() : null;

                Cliente c = new Cliente(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("numero_celular"),
                        deletedAt);
                return Optional.of(c);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente activo por número", e);
        }
    }
}