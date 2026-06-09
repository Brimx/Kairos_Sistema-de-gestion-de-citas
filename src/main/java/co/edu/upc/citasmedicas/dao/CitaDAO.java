package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.ServicioCita;
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

public class CitaDAO {

    public void guardar(Cita cita) {
        String sql = """
                INSERT INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, estado, tipo, motivo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cita.getId());
            statement.setString(2, cita.getPaciente().getId());
            statement.setString(3, cita.getMedico().getId());
            statement.setString(4, cita.getEspecialidad().name());
            statement.setString(5, cita.getServicio().name());
            statement.setString(6, cita.getFecha().toString());
            statement.setString(7, cita.getHoraInicio().toString());
            statement.setString(8, cita.getEstado().name());
            statement.setString(9, cita.getTipo().name());
            statement.setString(10, cita.getMotivo());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar la cita", exception);
        }
    }

    public void actualizarEstado(String citaId, EstadoCita estado) {
        String sql = "UPDATE citas SET estado = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, estado.name());
            statement.setString(2, citaId);

            int filas = statement.executeUpdate();
            if (filas == 0) {
                throw new IllegalArgumentException("No existe una cita con el id indicado");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar el estado de la cita", exception);
        }
    }

    public void actualizarCita(Cita cita) {
        String sql = """
                UPDATE citas
                SET medico_id = ?, especialidad = ?, servicio = ?, fecha = ?, hora_inicio = ?, tipo = ?, motivo = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, cita.getMedico().getId());
            statement.setString(2, cita.getEspecialidad().name());
            statement.setString(3, cita.getServicio().name());
            statement.setString(4, cita.getFecha().toString());
            statement.setString(5, cita.getHoraInicio().toString());
            statement.setString(6, cita.getTipo().name());
            statement.setString(7, cita.getMotivo());
            statement.setString(8, cita.getId());

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe una cita con el id indicado");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar la cita", exception);
        }
    }

    public boolean existeCitaActivaParaMedico(String medicoId, LocalDate fecha, LocalTime horaInicio) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM citas
                WHERE medico_id = ?
                  AND fecha = ?
                  AND hora_inicio = ?
                  AND estado <> ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, medicoId);
            statement.setString(2, fecha.toString());
            statement.setString(3, horaInicio.toString());
            statement.setString(4, EstadoCita.CANCELADA.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt("total") > 0;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo validar la disponibilidad del medico", exception);
        }
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
        return obtenerPorFiltro(consultaBase() + " ORDER BY c.fecha DESC, c.hora_inicio DESC", (PreparedStatementBinder) null);
    }

    public List<Cita> obtenerPorPaciente(String pacienteId) {
        return obtenerPorFiltro(consultaBase() + " WHERE c.paciente_id = ? ORDER BY c.fecha DESC, c.hora_inicio DESC", pacienteId);
    }

    public List<Cita> obtenerPorMedico(String medicoId) {
        return obtenerPorFiltro(consultaBase() + " WHERE c.medico_id = ? ORDER BY c.fecha DESC, c.hora_inicio DESC", filtroId -> {
            filtroId.setString(1, medicoId);
        });
    }

    public List<Cita> obtenerPorPacienteActivas(String pacienteId) {
        String sql = consultaBase() + """
                 WHERE c.paciente_id = ? AND c.estado <> ?
                 ORDER BY c.fecha DESC, c.hora_inicio DESC
                """;
        return obtenerPorFiltro(sql, statement -> {
            statement.setString(1, pacienteId);
            statement.setString(2, EstadoCita.CANCELADA.name());
        });
    }

    public List<Cita> obtenerAgendaMedico(String medicoId) {
        String sql = consultaBase() + """
                 WHERE c.medico_id = ? AND c.estado IN (?, ?)
                 ORDER BY c.fecha, c.hora_inicio
                """;
        return obtenerPorFiltro(sql, statement -> {
            statement.setString(1, medicoId);
            statement.setString(2, EstadoCita.PENDIENTE.name());
            statement.setString(3, EstadoCita.CONFIRMADA.name());
        });
    }

    private List<Cita> obtenerPorFiltro(String sql, String filtroId) {
        return obtenerPorFiltro(sql, statement -> statement.setString(1, filtroId));
    }

    @FunctionalInterface
    private interface PreparedStatementBinder {
        void bind(PreparedStatement statement) throws SQLException;
    }

    private List<Cita> obtenerPorFiltro(String sql, PreparedStatementBinder binder) {
        List<Cita> citas = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            if (binder != null) {
                binder.bind(statement);
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
                SELECT c.id, c.especialidad, c.servicio, c.fecha, c.hora_inicio, c.estado, c.tipo, c.motivo,
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
                ServicioCita.valueOf(resultSet.getString("servicio")),
                LocalDate.parse(resultSet.getString("fecha")),
                LocalTime.parse(resultSet.getString("hora_inicio")),
                TipoCita.valueOf(resultSet.getString("tipo")),
                resultSet.getString("motivo")
        );
        cita.setEstado(EstadoCita.valueOf(resultSet.getString("estado")));
        return cita;
    }
}
