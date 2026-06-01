package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.Rol;
import co.edu.upc.citasmedicas.model.Administrador;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Acceso a datos para autenticacion y consulta de usuarios.
 */
public class UsuarioDAO {

    public Usuario autenticar(String email, String password) {
        String sql = """
                SELECT u.*, p.tipo_documento, p.numero_documento, p.fecha_nacimiento, p.direccion, p.eps,
                       m.registro_medico, m.especialidad, m.consultorio,
                       a.codigo_admin, a.cargo
                FROM usuarios u
                LEFT JOIN pacientes p ON p.usuario_id = u.id
                LEFT JOIN medicos m ON m.usuario_id = u.id
                LEFT JOIN administradores a ON a.usuario_id = u.id
                WHERE u.email = ? AND u.password = ? AND u.activo = 1
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearUsuario(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo autenticar el usuario", exception);
        }

        return null;
    }

    private Usuario mapearUsuario(ResultSet resultSet) throws SQLException {
        Rol rol = Rol.valueOf(resultSet.getString("rol"));

        return switch (rol) {
            case PACIENTE -> new Paciente(
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
            case MEDICO -> new Medico(
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
            case ADMIN -> new Administrador(
                    resultSet.getString("id"),
                    resultSet.getString("nombre"),
                    resultSet.getString("apellido"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("telefono"),
                    resultSet.getString("codigo_admin"),
                    resultSet.getString("cargo")
            );
        };
    }
}
