package com.electronica.stats.services;

import io.javalin.http.Context;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.sql.*;

public class StatsService {

        private final DataSource dataSource;

        public StatsService(DataSource dataSource) {
                this.dataSource = dataSource;
        }

        public void getSummary(Context ctx) {
                try (Connection conn = dataSource.getConnection()) {
                        LocalDate hoy = LocalDate.now();
                        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                        LocalDate inicioMes = hoy.withDayOfMonth(1);

                        // Ingresos de Hoy (Usando rangos para evitar problemas de timezone en DB)
                        String sqlHoy = "SELECT SUM(costo_reparacion) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ? AND fecha_finalizacion < ?";
                        BigDecimal ingresosHoy = BigDecimal.ZERO;
                        try (PreparedStatement stmt = conn.prepareStatement(sqlHoy)) {
                                stmt.setTimestamp(1, Timestamp.valueOf(hoy.atStartOfDay()));
                                stmt.setTimestamp(2, Timestamp.valueOf(hoy.plusDays(1).atStartOfDay()));
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                        BigDecimal val = rs.getBigDecimal(1);
                                        ingresosHoy = val != null ? val : BigDecimal.ZERO;
                                }
                        }

                        // Ingresos de la Semana
                        String sqlSemana = "SELECT SUM(costo_reparacion) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ?";
                        BigDecimal ingresosSemana = BigDecimal.ZERO;
                        try (PreparedStatement stmt = conn.prepareStatement(sqlSemana)) {
                                stmt.setTimestamp(1, Timestamp.valueOf(inicioSemana.atStartOfDay()));
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                        BigDecimal val = rs.getBigDecimal(1);
                                        ingresosSemana = val != null ? val : BigDecimal.ZERO;
                                }
                        }

                        // Ingresos del Mes
                        String sqlMes = "SELECT SUM(costo_reparacion) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ?";
                        BigDecimal ingresosMes = BigDecimal.ZERO;
                        try (PreparedStatement stmt = conn.prepareStatement(sqlMes)) {
                                stmt.setTimestamp(1, Timestamp.valueOf(inicioMes.atStartOfDay()));
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                        BigDecimal val = rs.getBigDecimal(1);
                                        ingresosMes = val != null ? val : BigDecimal.ZERO;
                                }
                        }

                        // Tarjetas finalizadas en el mes
                        String sqlCount = "SELECT COUNT(*) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ?";
                        long tarjetasFinalizadas = 0;
                        try (PreparedStatement stmt = conn.prepareStatement(sqlCount)) {
                                stmt.setTimestamp(1, Timestamp.valueOf(inicioMes.atStartOfDay()));
                                ResultSet rs = stmt.executeQuery();
                                if (rs.next()) {
                                        tarjetasFinalizadas = rs.getLong(1);
                                }
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("ingresosHoy", ingresosHoy);
                        data.put("ingresosSemana", ingresosSemana);
                        data.put("ingresosMes", ingresosMes);
                        data.put("tarjetasFinalizadas", tarjetasFinalizadas);

                        ctx.json(Map.of("success", true, "data", data));
                } catch (SQLException e) {
                        e.printStackTrace();
                        ctx.status(500).json(Map.of("success", false, "message", "Error al cargar estadísticas"));
                }
        }

        public void getChartData(Context ctx) {
                String tipo = ctx.queryParam("tipo"); // diario, semanal, mes
                LocalDate hoy = LocalDate.now();

                List<String> labels = new ArrayList<>();
                List<BigDecimal> valores = new ArrayList<>();

                try (Connection conn = dataSource.getConnection()) {
                        if ("diario".equals(tipo)) {
                                // Últimos 7 días
                                for (int i = 6; i >= 0; i--) {
                                        LocalDate fecha = hoy.minusDays(i);
                                        labels.add(fecha.toString());

                                        String sql = "SELECT SUM(costo_reparacion) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ? AND fecha_finalizacion < ?";
                                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                                stmt.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
                                                stmt.setTimestamp(2,
                                                                Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
                                                ResultSet rs = stmt.executeQuery();
                                                BigDecimal val = BigDecimal.ZERO;
                                                if (rs.next()) {
                                                        val = rs.getBigDecimal(1);
                                                        val = val != null ? val : BigDecimal.ZERO;
                                                }
                                                valores.add(val);
                                        }
                                }
                        } else if ("semanal".equals(tipo)) {
                                // Últimas 4 semanas
                                for (int i = 3; i >= 0; i--) {
                                        labels.add("Semana " + (4 - i));
                                        LocalDate startOfWeek = hoy.minusWeeks(i)
                                                        .minusDays(hoy.getDayOfWeek().getValue() - 1);
                                        LocalDate endOfWeek = startOfWeek.plusDays(6);

                                        String sql = "SELECT SUM(costo_reparacion) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ? AND fecha_finalizacion < ?";
                                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                                stmt.setTimestamp(1, Timestamp.valueOf(startOfWeek.atStartOfDay()));
                                                stmt.setTimestamp(2, Timestamp
                                                                .valueOf(endOfWeek.plusDays(1).atStartOfDay()));
                                                ResultSet rs = stmt.executeQuery();
                                                BigDecimal val = BigDecimal.ZERO;
                                                if (rs.next()) {
                                                        val = rs.getBigDecimal(1);
                                                        val = val != null ? val : BigDecimal.ZERO;
                                                }
                                                valores.add(val);
                                        }
                                }
                        } else {
                                // Últimos 6 meses
                                for (int i = 5; i >= 0; i--) {
                                        LocalDate mes = hoy.minusMonths(i);
                                        labels.add(mes.getMonth().toString());
                                        LocalDate start = mes.withDayOfMonth(1);
                                        LocalDate end = start.plusMonths(1).minusDays(1);

                                        String sql = "SELECT SUM(costo_reparacion) FROM servicios WHERE estado = 'ENTREGADO' AND fecha_finalizacion >= ? AND fecha_finalizacion < ?";
                                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                                                stmt.setTimestamp(1, Timestamp.valueOf(start.atStartOfDay()));
                                                stmt.setTimestamp(2, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
                                                ResultSet rs = stmt.executeQuery();
                                                BigDecimal val = BigDecimal.ZERO;
                                                if (rs.next()) {
                                                        val = rs.getBigDecimal(1);
                                                        val = val != null ? val : BigDecimal.ZERO;
                                                }
                                                valores.add(val);
                                        }
                                }
                        }

                        ctx.json(Map.of("success", true, "data", Map.of("labels", labels, "valores", valores)));
                } catch (SQLException e) {
                        e.printStackTrace();
                        ctx.status(500).json(Map.of("success", false, "message", "Error al cargar gráfica"));
                }
        }
}
