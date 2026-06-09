package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.HistorialClinico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistorialClinicoDAO {

    public void guardar(HistorialClinico historial) {
        String sql = """
                INSERT INTO historial_clinico
                (id, cita_id, medico_id, paciente_id, fecha_consulta, diagnostico, enfermedad_actual, receta, remision, notas)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, historial.getId());
            statement.setString(2, historial.getCitaId());
            statement.setString(3, historial.getMedicoId());
            statement.setString(4, historial.getPacienteId());
            statement.setString(5, historial.getFechaConsulta().toString());
            statement.setString(6, historial.getDiagnostico());
            statement.setString(7, historial.getEnfermedadActual());
            statement.setString(8, historial.getReceta());
            statement.setString(9, historial.getRemision());
            statement.setString(10, historial.getNotas());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el historial clinico", exception);
        }
    }

    public HistorialClinico buscarPorCitaId(String citaId) {
        String sql = "SELECT * FROM historial_clinico WHERE cita_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, citaId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapear(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar el historial clinico", exception);
        }

        return null;
    }

    public List<HistorialClinico> obtenerPorPacienteId(String pacienteId) {
        String sql = """
                SELECT h.* FROM historial_clinico h
                JOIN citas c ON c.id = h.cita_id
                WHERE h.paciente_id = ?
                ORDER BY h.fecha_consulta DESC
                """;

        List<HistorialClinico> lista = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pacienteId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lista.add(mapear(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo obtener el historial del paciente", exception);
        }

        return lista;
    }

    public List<HistorialClinico> obtenerPorMedicoId(String medicoId) {
        String sql = "SELECT * FROM historial_clinico WHERE medico_id = ? ORDER BY fecha_consulta DESC";

        List<HistorialClinico> lista = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, medicoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lista.add(mapear(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo obtener el historial del medico", exception);
        }

        return lista;
    }

    private HistorialClinico mapear(ResultSet resultSet) throws SQLException {
        HistorialClinico h = new HistorialClinico();
        h.setId(resultSet.getString("id"));
        h.setCitaId(resultSet.getString("cita_id"));
        h.setMedicoId(resultSet.getString("medico_id"));
        h.setPacienteId(resultSet.getString("paciente_id"));
        h.setFechaConsulta(LocalDate.parse(resultSet.getString("fecha_consulta")));
        h.setDiagnostico(resultSet.getString("diagnostico"));
        h.setEnfermedadActual(resultSet.getString("enfermedad_actual"));
        h.setReceta(resultSet.getString("receta"));
        h.setRemision(resultSet.getString("remision"));
        h.setNotas(resultSet.getString("notas"));
        return h;
    }
}
