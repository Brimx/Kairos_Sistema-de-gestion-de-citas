package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.BloqueoAgenda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class BloqueoAgendaDAO {

    public void guardar(BloqueoAgenda bloqueo) {
        String sql = """
                INSERT INTO bloqueos_agenda (id, medico_id, fecha, hora_inicio, hora_fin, motivo)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, bloqueo.getId());
            statement.setString(2, bloqueo.getMedicoId());
            statement.setString(3, bloqueo.getFecha().toString());
            statement.setString(4, bloqueo.getHoraInicio() != null ? bloqueo.getHoraInicio().toString() : null);
            statement.setString(5, bloqueo.getHoraFin() != null ? bloqueo.getHoraFin().toString() : null);
            statement.setString(6, bloqueo.getMotivo());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el bloqueo", exception);
        }
    }

    public List<BloqueoAgenda> obtenerPorMedicoYFecha(String medicoId, LocalDate fecha) {
        String sql = """
                SELECT id, medico_id, fecha, hora_inicio, hora_fin, motivo
                FROM bloqueos_agenda
                WHERE medico_id = ? AND fecha = ?
                ORDER BY hora_inicio
                """;

        List<BloqueoAgenda> list = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, medicoId);
            statement.setString(2, fecha.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapearBloqueo(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron cargar los bloqueos", exception);
        }

        return list;
    }

    public List<BloqueoAgenda> obtenerTodos() {
        String sql = """
                SELECT id, medico_id, fecha, hora_inicio, hora_fin, motivo
                FROM bloqueos_agenda
                ORDER BY fecha, hora_inicio
                """;

        List<BloqueoAgenda> list = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapearBloqueo(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron cargar los bloqueos", exception);
        }

        return list;
    }

    public void eliminar(String id) {
        String sql = "DELETE FROM bloqueos_agenda WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe un bloqueo con ese id");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo eliminar el bloqueo", exception);
        }
    }

    private BloqueoAgenda mapearBloqueo(ResultSet rs) throws SQLException {
        String horaInicioStr = rs.getString("hora_inicio");
        String horaFinStr = rs.getString("hora_fin");
        LocalTime horaInicio = horaInicioStr != null ? LocalTime.parse(horaInicioStr) : null;
        LocalTime horaFin = horaFinStr != null ? LocalTime.parse(horaFinStr) : null;

        return new BloqueoAgenda(
                rs.getString("id"),
                rs.getString("medico_id"),
                LocalDate.parse(rs.getString("fecha")),
                horaInicio,
                horaFin,
                rs.getString("motivo")
        );
    }
}
