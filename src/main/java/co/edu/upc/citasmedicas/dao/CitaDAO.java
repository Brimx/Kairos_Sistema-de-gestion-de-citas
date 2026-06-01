package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad Cita.
 */
public class CitaDAO {

    public void guardar(Cita cita) {
        throw new UnsupportedOperationException("Guardar citas desde SQLite aun no esta implementado");
    }

    public Cita buscarPorId(String id) {
        String sql = consultaBase() + " WHERE c.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearCita(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar la cita", exception);
        }

        return null;
    }

    public List<Cita> obtenerTodas() {
        return obtenerPorFiltro(consultaBase() + " ORDER BY c.fecha, c.hora_inicio", null);
    }

    public List<Cita> obtenerPorPaciente(String pacienteId) {
        return obtenerPorFiltro(consultaBase() + " WHERE c.paciente_id = ? ORDER BY c.fecha, c.hora_inicio", pacienteId);
    }

    public List<Cita> obtenerPorMedico(String medicoId) {
        return obtenerPorFiltro(consultaBase() + " WHERE c.medico_id = ? ORDER BY c.fecha, c.hora_inicio", medicoId);
    }

    private List<Cita> obtenerPorFiltro(String sql, String filtro) {
        List<Cita> citas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (filtro != null) {
                statement.setString(1, filtro);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    citas.add(mapearCita(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron cargar las citas", exception);
        }

        return citas;
    }

    private String consultaBase() {
        return """
                SELECT c.id, c.especialidad, c.fecha, c.hora_inicio, c.estado, c.tipo, c.motivo,
                       pu.id AS paciente_id, pu.nombre AS paciente_nombre, pu.apellido AS paciente_apellido,
                       pu.email AS paciente_email, pu.password AS paciente_password, pu.telefono AS paciente_telefono,
                       p.tipo_documento, p.numero_documento, p.fecha_nacimiento, p.direccion, p.eps,
                       mu.id AS medico_id, mu.nombre AS medico_nombre, mu.apellido AS medico_apellido,
                       mu.email AS medico_email, mu.password AS medico_password, mu.telefono AS medico_telefono,
                       m.registro_medico, m.especialidad AS medico_especialidad, m.consultorio
                FROM citas c
                JOIN pacientes p ON p.usuario_id = c.paciente_id
                JOIN usuarios pu ON pu.id = p.usuario_id
                JOIN medicos m ON m.usuario_id = c.medico_id
                JOIN usuarios mu ON mu.id = m.usuario_id
                """;
    }

    private Cita mapearCita(ResultSet resultSet) throws SQLException {
        Paciente paciente = new Paciente(
                resultSet.getString("paciente_id"),
                resultSet.getString("paciente_nombre"),
                resultSet.getString("paciente_apellido"),
                resultSet.getString("paciente_email"),
                resultSet.getString("paciente_password"),
                resultSet.getString("paciente_telefono"),
                resultSet.getString("tipo_documento"),
                resultSet.getString("numero_documento"),
                LocalDate.parse(resultSet.getString("fecha_nacimiento")),
                resultSet.getString("direccion"),
                resultSet.getString("eps")
        );

        Medico medico = new Medico(
                resultSet.getString("medico_id"),
                resultSet.getString("medico_nombre"),
                resultSet.getString("medico_apellido"),
                resultSet.getString("medico_email"),
                resultSet.getString("medico_password"),
                resultSet.getString("medico_telefono"),
                resultSet.getString("registro_medico"),
                Especialidad.valueOf(resultSet.getString("medico_especialidad")),
                resultSet.getString("consultorio")
        );

        Cita cita = new Cita(
                resultSet.getString("id"),
                paciente,
                medico,
                Especialidad.valueOf(resultSet.getString("especialidad")),
                LocalDate.parse(resultSet.getString("fecha")),
                LocalTime.parse(resultSet.getString("hora_inicio")),
                TipoCita.valueOf(resultSet.getString("tipo")),
                resultSet.getString("motivo")
        );
        cita.setEstado(EstadoCita.valueOf(resultSet.getString("estado")));
        return cita;
    }
}
