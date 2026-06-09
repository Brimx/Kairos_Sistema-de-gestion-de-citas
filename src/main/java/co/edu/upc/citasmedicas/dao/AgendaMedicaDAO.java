package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.AgendaMedica;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AgendaMedicaDAO {

    public void guardar(AgendaMedica agenda) {
        String sql = """
                INSERT INTO agenda_medica (id, medico_id, dia_semana, hora_inicio, hora_fin, slot_minutos)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, agenda.getId());
            statement.setString(2, agenda.getMedicoId());
            statement.setInt(3, agenda.getDiaSemana());
            statement.setString(4, agenda.getHoraInicio().toString());
            statement.setString(5, agenda.getHoraFin().toString());
            statement.setInt(6, agenda.getSlotMinutos());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar la agenda medica", exception);
        }
    }

    public AgendaMedica obtenerPorMedicoYDia(String medicoId, int diaSemana) {
        String sql = """
                SELECT id, medico_id, dia_semana, hora_inicio, hora_fin, slot_minutos
                FROM agenda_medica
                WHERE medico_id = ? AND dia_semana = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, medicoId);
            statement.setInt(2, diaSemana);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearAgenda(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo consultar la agenda medica", exception);
        }

        return null;
    }

    public void actualizar(AgendaMedica agenda) {
        String sql = """
                UPDATE agenda_medica
                SET dia_semana = ?, hora_inicio = ?, hora_fin = ?, slot_minutos = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, agenda.getDiaSemana());
            statement.setString(2, agenda.getHoraInicio().toString());
            statement.setString(3, agenda.getHoraFin().toString());
            statement.setInt(4, agenda.getSlotMinutos());
            statement.setString(5, agenda.getId());

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la agenda con el id indicado");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar la agenda medica", exception);
        }
    }

    public void eliminar(String id) {
        String sql = "DELETE FROM agenda_medica WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo eliminar la agenda medica", exception);
        }
    }

    public List<AgendaMedica> listarPorMedico(String medicoId) {
        String sql = """
                SELECT id, medico_id, dia_semana, hora_inicio, hora_fin, slot_minutos
                FROM agenda_medica
                WHERE medico_id = ?
                ORDER BY dia_semana
                """;

        List<AgendaMedica> list = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, medicoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    list.add(mapearAgenda(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo consultar la agenda medica", exception);
        }

        return list;
    }

    private AgendaMedica mapearAgenda(ResultSet resultSet) throws SQLException {
        return new AgendaMedica(
                resultSet.getString("id"),
                resultSet.getString("medico_id"),
                resultSet.getInt("dia_semana"),
                LocalTime.parse(resultSet.getString("hora_inicio")),
                LocalTime.parse(resultSet.getString("hora_fin")),
                resultSet.getInt("slot_minutos")
        );
    }
}
