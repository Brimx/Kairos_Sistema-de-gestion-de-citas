package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.model.Medico;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad Medico.
 */
public class MedicoDAO {

    public void guardar(Medico medico) {
        String sqlUsuario = """
                INSERT INTO usuarios (id, nombre, apellido, email, password, telefono, rol, activo)
                VALUES (?, ?, ?, ?, ?, ?, 'MEDICO', 1)
                """;

        String sqlMedico = """
                INSERT INTO medicos (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtMedico = connection.prepareStatement(sqlMedico)) {

            connection.setAutoCommit(false);

            // Guardar en tabla usuarios
            stmtUsuario.setString(1, medico.getId());
            stmtUsuario.setString(2, medico.getNombre());
            stmtUsuario.setString(3, medico.getApellido());
            stmtUsuario.setString(4, medico.getEmail());
            stmtUsuario.setString(5, medico.getPassword());
            stmtUsuario.setString(6, medico.getTelefono());
            stmtUsuario.executeUpdate();

            // Guardar en tabla medicos
            stmtMedico.setString(1, medico.getId());
            stmtMedico.setString(2, medico.getRegistroMedico());
            stmtMedico.setString(3, medico.getEspecialidad().name());
            stmtMedico.setString(4, medico.getConsultorio());
            stmtMedico.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el médico", exception);
        }
    }

    public List<Medico> obtenerTodos() {
        String sql = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.password, u.telefono,
                       m.registro_medico, m.especialidad, m.consultorio
                FROM medicos m
                JOIN usuarios u ON u.id = m.usuario_id
                WHERE u.activo = 1
                ORDER BY u.apellido, u.nombre
                """;

        List<Medico> medicos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                medicos.add(mapearMedico(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron cargar los medicos", exception);
        }

        return medicos;
    }

    public void actualizar(Medico medico) {
        String sqlUsuario = """
                UPDATE usuarios SET nombre = ?, apellido = ?, telefono = ?
                WHERE id = ? AND rol = 'MEDICO' AND activo = 1
                """;
        String sqlMedico = """
                UPDATE medicos SET registro_medico = ?, especialidad = ?, consultorio = ?
                WHERE usuario_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtMedico = connection.prepareStatement(sqlMedico)) {

            connection.setAutoCommit(false);

            stmtUsuario.setString(1, medico.getNombre());
            stmtUsuario.setString(2, medico.getApellido());
            stmtUsuario.setString(3, medico.getTelefono());
            stmtUsuario.setString(4, medico.getId());

            if (stmtUsuario.executeUpdate() == 0) {
                throw new IllegalArgumentException("No se encontro un medico activo con ese id");
            }

            stmtMedico.setString(1, medico.getRegistroMedico());
            stmtMedico.setString(2, medico.getEspecialidad().name());
            stmtMedico.setString(3, medico.getConsultorio());
            stmtMedico.setString(4, medico.getId());
            stmtMedico.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar el medico", exception);
        }
    }

    public void eliminar(String id) {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id = ? AND rol = 'MEDICO'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No se encontro un medico con ese id");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo eliminar el medico", exception);
        }
    }

    public Medico buscarPorId(String id) {
        String sql = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.password, u.telefono,
                       m.registro_medico, m.especialidad, m.consultorio
                FROM medicos m
                JOIN usuarios u ON u.id = m.usuario_id
                WHERE u.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearMedico(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar el medico", exception);
        }

        return null;
    }

    private Medico mapearMedico(ResultSet resultSet) throws SQLException {
        return new Medico(
                resultSet.getString("id"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("telefono"),
                resultSet.getString("registro_medico"),
                Especialidad.valueOf(resultSet.getString("especialidad")),
                resultSet.getString("consultorio")
        );
    }
}
