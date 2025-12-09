package com.electronica.servicio.repository;

import com.electronica.servicio.models.Servicio;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServicioRepository {

    private final DataSource dataSource;

    public ServicioRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Servicio save(Servicio servicio) {
        String sql = "INSERT INTO servicios (id, folio_servicio, equipo_id, tecnico_id, problema_reportado, fecha_ingreso, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        // Nota: folio_servicio es AUTO_INCREMENT, no lo insertamos si es null/0, pero
        // aquí lo manejamos como insertable si queremos controlarlo,
        // o dejamos que la BD lo genere. Si la BD lo genera, no lo incluimos en el
        // INSERT.
        // Dado que es AUTO_INCREMENT, mejor no lo incluimos en el INSERT y lo
        // recuperamos.

        String sqlInsert = "INSERT INTO servicios (id, equipo_id, tecnico_id, problema_reportado, fecha_ingreso, estado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, servicio.getId());
            stmt.setString(2, servicio.getEquipoId());
            stmt.setString(3, servicio.getTecnicoId());
            stmt.setString(4, servicio.getProblemaReportado());
            stmt.setTimestamp(5, Timestamp.valueOf(servicio.getFechaIngreso()));
            stmt.setString(6, servicio.getEstado());

            stmt.executeUpdate();

            // Recuperar el folio generado si es necesario, aunque el ID UUID es el
            // principal.
            // Para obtener el folio, tendríamos que hacer un SELECT o usar getGeneratedKeys
            // si el driver lo soporta para columnas no-PK (folio es unique key).
            // Simplificación: No recuperamos el folio inmediatamente a menos que sea
            // crítico.

            return servicio;

        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar servicio", e);
        }
    }

    public List<Servicio> findAll() {
        List<Servicio> servicios = new ArrayList<>();
        // Use direct JOIN to include tecnico_id which is missing in the view
        String sql = "SELECT " +
                "s.id AS servicio_id, " +
                "s.folio_servicio, " +
                "CONCAT(c.nombre, ' ', c.apellidos) AS nombre_cliente, " +
                "c.numero_celular, " +
                "m.nombre_marca AS marca, " +
                "e.modelo, " +
                "s.problema_reportado, " +
                "s.diagnostico_tecnico, " +
                "s.tecnico_id, " + // Added tecnico_id
                "u.nombre_completo AS tecnico_nombre, " +
                "s.estado, " +
                "s.fecha_ingreso, " +
                "s.fecha_entrega_cliente, " +
                "s.costo_reparacion " +
                "FROM servicios s " +
                "JOIN equipos e ON s.equipo_id = e.id " +
                "JOIN clientes c ON e.cliente_id = c.id " +
                "JOIN marcas m ON e.marca_id = m.id " +
                "JOIN users u ON s.tecnico_id = u.id " +
                "ORDER BY s.fecha_ingreso DESC";

        try (Connection conn = dataSource.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                servicios.add(mapRowToServicioView(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al listar servicios", e);
        }
        return servicios;
    }

    public Optional<Servicio> findById(String id) {
        // Use the view to get expanded details for editing too
        // Note: The view uses 'servicio_id' instead of 'id'
        // FIX: Join with services table to get tecnico_id which is missing in the view
        String sql = "SELECT v.*, s.tecnico_id FROM vista_servicios_completa v JOIN servicios s ON v.servicio_id = s.id WHERE v.servicio_id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRowToServicioView(rs));
            }

            // Fallback: If not found in view (maybe incomplete data?), try raw table
            // This ensures we can still edit even if joins fail (though they shouldn't with
            // inner joins)
            return findByIdRaw(id);

        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar servicio", e);
        }
    }

    private Optional<Servicio> findByIdRaw(String id) {
        String sql = "SELECT * FROM servicios WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToServicio(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar servicio raw", e);
        }
    }

    public void updateDiagnostico(String id, String diagnostico, String estado, LocalDateTime fechaEstimada) {
        String sql = "UPDATE servicios SET diagnostico_tecnico = ?, estado = ?, fecha_estimada_entrega = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, diagnostico);
            stmt.setString(2, estado);
            stmt.setTimestamp(3, fechaEstimada != null ? Timestamp.valueOf(fechaEstimada) : null);
            stmt.setString(4, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar diagnostico", e);
        }
    }

    public void finalizarServicio(String id, LocalDateTime fechaFinalizacion, LocalDateTime fechaEntrega,
            java.math.BigDecimal costo) {
        // Cuando el gerente finaliza el pedido, se marca como ENTREGADO
        // El técnico es quien marca como FINALIZADO cuando termina la reparación
        String sql = "UPDATE servicios SET estado = 'ENTREGADO', fecha_finalizacion = ?, fecha_entrega_cliente = ?, costo_reparacion = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(fechaFinalizacion));
            stmt.setTimestamp(2, fechaEntrega != null ? Timestamp.valueOf(fechaEntrega) : null);
            stmt.setBigDecimal(3, costo);
            stmt.setString(4, id);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al finalizar servicio", e);
        }
    }

    public void delete(String id) {
        String sql = "DELETE FROM servicios WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar servicio", e);
        }
    }

    // Mapper for the VIEW
    private Servicio mapRowToServicioView(ResultSet rs) throws SQLException {
        Servicio s = new Servicio();
        s.setId(rs.getString("servicio_id"));
        s.setFolioServicio(rs.getInt("folio_servicio"));
        // View doesn't have raw IDs, but we might not need them for display
        // If we need them for editing, we might need to adjust the view or fetch them
        // separately

        s.setNombreCliente(rs.getString("nombre_cliente"));
        s.setNumeroCelular(rs.getString("numero_celular"));
        s.setMarca(rs.getString("marca"));
        s.setModelo(rs.getString("modelo"));
        s.setTecnicoNombre(rs.getString("tecnico_nombre"));

        // Try to get tecnico_id if available (it won't be in the view, but will be in
        // our custom query)
        try {
            s.setTecnicoId(rs.getString("tecnico_id"));
        } catch (SQLException e) {
            // Ignore if column not found (e.g. when called from findById which still uses
            // the view)
        }

        s.setProblemaReportado(rs.getString("problema_reportado"));
        s.setDiagnosticoTecnico(rs.getString("diagnostico_tecnico"));
        s.setEstado(rs.getString("estado"));

        Timestamp fechaIng = rs.getTimestamp("fecha_ingreso");
        if (fechaIng != null)
            s.setFechaIngreso(fechaIng.toLocalDateTime());

        Timestamp fechaEnt = rs.getTimestamp("fecha_entrega_cliente");
        if (fechaEnt != null)
            s.setFechaEntregaCliente(fechaEnt.toLocalDateTime());

        s.setCostoReparacion(rs.getBigDecimal("costo_reparacion"));

        return s;
    }

    // Mapper for the RAW TABLE
    private Servicio mapRowToServicio(ResultSet rs) throws SQLException {
        Servicio s = new Servicio();
        s.setId(rs.getString("id"));
        s.setFolioServicio(rs.getInt("folio_servicio"));
        s.setEquipoId(rs.getString("equipo_id"));
        s.setTecnicoId(rs.getString("tecnico_id"));
        s.setProblemaReportado(rs.getString("problema_reportado"));
        s.setFechaIngreso(rs.getTimestamp("fecha_ingreso").toLocalDateTime());
        s.setDiagnosticoTecnico(rs.getString("diagnostico_tecnico"));
        s.setEstado(rs.getString("estado"));

        Timestamp fechaEst = rs.getTimestamp("fecha_estimada_entrega");
        if (fechaEst != null)
            s.setFechaEstimadaEntrega(fechaEst.toLocalDateTime());

        Timestamp fechaFin = rs.getTimestamp("fecha_finalizacion");
        if (fechaFin != null)
            s.setFechaFinalizacion(fechaFin.toLocalDateTime());

        Timestamp fechaEnt = rs.getTimestamp("fecha_entrega_cliente");
        if (fechaEnt != null)
            s.setFechaEntregaCliente(fechaEnt.toLocalDateTime());

        s.setCostoReparacion(rs.getBigDecimal("costo_reparacion"));

        return s;
    }
}
