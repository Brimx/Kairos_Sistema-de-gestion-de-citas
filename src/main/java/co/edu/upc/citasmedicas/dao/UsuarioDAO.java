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

    /**
     * Verifica si un email ya está registrado en el sistema.
     * @param email email a verificar
     * @return true si el email ya existe, false en caso contrario
     */
    public boolean existeEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo verificar el email", exception);
        }

        return false;
    }

    /**
     * Guarda un paciente en la base de datos.
     * @param paciente paciente a guardar
     */
    public void guardarPaciente(Paciente paciente) {
        String sqlUsuario = """
                INSERT INTO usuarios (id, nombre, apellido, email, password, telefono, rol, activo)
                VALUES (?, ?, ?, ?, ?, ?, 'PACIENTE', 1)
                """;

        String sqlPaciente = """
                INSERT INTO pacientes (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtPaciente = connection.prepareStatement(sqlPaciente)) {

            connection.setAutoCommit(false);

            // Guardar en tabla usuarios
            stmtUsuario.setString(1, paciente.getId());
            stmtUsuario.setString(2, paciente.getNombre());
            stmtUsuario.setString(3, paciente.getApellido());
            stmtUsuario.setString(4, paciente.getEmail());
            stmtUsuario.setString(5, paciente.getPassword());
            stmtUsuario.setString(6, paciente.getTelefono());
            stmtUsuario.executeUpdate();

            // Guardar en tabla pacientes
            stmtPaciente.setString(1, paciente.getId());
            stmtPaciente.setString(2, paciente.getTipoDocumento());
            stmtPaciente.setString(3, paciente.getNumeroDocumento());
            stmtPaciente.setString(4, LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE));
            stmtPaciente.setString(5, paciente.getDireccion());
            stmtPaciente.setString(6, paciente.getEps());
            stmtPaciente.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el paciente", exception);
        }
    }

    /**
     * Guarda un médico en la base de datos.
     * @param medico médico a guardar
     */
    public void guardarMedico(Medico medico) {
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

    /**
     * Guarda un administrador en la base de datos.
     * @param administrador administrador a guardar
     */
    public void guardarAdministrador(Administrador administrador) {
        String sqlUsuario = """
                INSERT INTO usuarios (id, nombre, apellido, email, password, telefono, rol, activo)
                VALUES (?, ?, ?, ?, ?, ?, 'ADMIN', 1)
                """;

        String sqlAdministrador = """
                INSERT INTO administradores (usuario_id, codigo_admin, cargo)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtAdministrador = connection.prepareStatement(sqlAdministrador)) {

            connection.setAutoCommit(false);

            // Guardar en tabla usuarios
            stmtUsuario.setString(1, administrador.getId());
            stmtUsuario.setString(2, administrador.getNombre());
            stmtUsuario.setString(3, administrador.getApellido());
            stmtUsuario.setString(4, administrador.getEmail());
            stmtUsuario.setString(5, administrador.getPassword());
            stmtUsuario.setString(6, administrador.getTelefono());
            stmtUsuario.executeUpdate();

            // Guardar en tabla administradores
            stmtAdministrador.setString(1, administrador.getId());
            stmtAdministrador.setString(2, administrador.getCodigoAdmin());
            stmtAdministrador.setString(3, administrador.getCargo());
            stmtAdministrador.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el administrador", exception);
        }
    }

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
