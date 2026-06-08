package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.Paciente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la entidad Paciente.
 */
public class PacienteDAO {

    public void guardar(Paciente paciente) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        usuarioDAO.guardarPaciente(paciente);
    }

    public Paciente buscarPorId(String id) {
        String sql = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.password, u.telefono,
                       p.tipo_documento, p.numero_documento, p.fecha_nacimiento, p.direccion, p.eps
                FROM pacientes p
                JOIN usuarios u ON u.id = p.usuario_id
                WHERE u.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearPaciente(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar el paciente", exception);
        }

        return null;
    }

    public List<Paciente> obtenerTodos() {
        String sql = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.password, u.telefono,
                       p.tipo_documento, p.numero_documento, p.fecha_nacimiento, p.direccion, p.eps
                FROM pacientes p
                JOIN usuarios u ON u.id = p.usuario_id
                WHERE u.activo = 1
                ORDER BY u.apellido, u.nombre
                """;

        List<Paciente> pacientes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                pacientes.add(mapearPaciente(resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudieron cargar los pacientes", exception);
        }

        return pacientes;
    }

    public void actualizarDatosBasicos(String id, String nombre, String apellido, String telefono) {
        String sql = """
                UPDATE usuarios
                SET nombre = ?, apellido = ?, telefono = ?
                WHERE id = ? AND rol = 'PACIENTE' AND activo = 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, telefono);
            statement.setString(4, id);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No se encontro un paciente activo con ese id");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar el paciente", exception);
        }
    }

    public void desactivar(String id) {
        String sql = "UPDATE usuarios SET activo = 0 WHERE id = ? AND rol = 'PACIENTE'";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            if (statement.executeUpdate() == 0) {
                throw new IllegalArgumentException("No se encontro un paciente con ese id");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo desactivar el paciente", exception);
        }
    }

    private Paciente mapearPaciente(ResultSet resultSet) throws SQLException {
        return new Paciente(
                resultSet.getString("id"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("telefono"),
                resultSet.getString("tipo_documento"),
                resultSet.getString("numero_documento"),
                LocalDate.parse(resultSet.getString("fecha_nacimiento")),
                resultSet.getString("direccion"),
                resultSet.getString("eps")
        );
    }
}
