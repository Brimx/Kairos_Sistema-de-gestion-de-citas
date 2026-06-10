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
            stmtPaciente.setString(4, paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento().toString() : null);
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
                INSERT INTO medicos (usuario_id, registro_medico, especialidad, consultorio,
                    tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtMedico = connection.prepareStatement(sqlMedico)) {

            connection.setAutoCommit(false);

            stmtUsuario.setString(1, medico.getId());
            stmtUsuario.setString(2, medico.getNombre());
            stmtUsuario.setString(3, medico.getApellido());
            stmtUsuario.setString(4, medico.getEmail());
            stmtUsuario.setString(5, medico.getPassword());
            stmtUsuario.setString(6, medico.getTelefono());
            stmtUsuario.executeUpdate();

            stmtMedico.setString(1, medico.getId());
            stmtMedico.setString(2, medico.getRegistroMedico());
            stmtMedico.setString(3, medico.getEspecialidad().name());
            stmtMedico.setString(4, "");
            stmtMedico.setString(5, medico.getTipoDocumento());
            stmtMedico.setString(6, medico.getNumeroDocumento());
            stmtMedico.setString(7, medico.getFechaNacimiento() != null ? medico.getFechaNacimiento().toString() : null);
            stmtMedico.setString(8, medico.getDireccion());
            stmtMedico.setString(9, medico.getEps());
            stmtMedico.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el medico", exception);
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
                INSERT INTO administradores (usuario_id, codigo_admin, cargo,
                    tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtAdministrador = connection.prepareStatement(sqlAdministrador)) {

            connection.setAutoCommit(false);

            stmtUsuario.setString(1, administrador.getId());
            stmtUsuario.setString(2, administrador.getNombre());
            stmtUsuario.setString(3, administrador.getApellido());
            stmtUsuario.setString(4, administrador.getEmail());
            stmtUsuario.setString(5, administrador.getPassword());
            stmtUsuario.setString(6, administrador.getTelefono());
            stmtUsuario.executeUpdate();

            stmtAdministrador.setString(1, administrador.getId());
            stmtAdministrador.setString(2, administrador.getCodigoAdmin());
            stmtAdministrador.setString(3, administrador.getCargo());
            stmtAdministrador.setString(4, administrador.getTipoDocumento());
            stmtAdministrador.setString(5, administrador.getNumeroDocumento());
            stmtAdministrador.setString(6, administrador.getFechaNacimiento() != null ? administrador.getFechaNacimiento().toString() : null);
            stmtAdministrador.setString(7, administrador.getDireccion());
            stmtAdministrador.setString(8, administrador.getEps());
            stmtAdministrador.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo guardar el administrador", exception);
        }
    }

    public Usuario autenticar(String email, String password) {
        String sql = """
                SELECT u.*,
                       p.tipo_documento AS p_tipo_doc, p.numero_documento AS p_num_doc,
                       p.fecha_nacimiento AS p_fecha_nac, p.direccion AS p_dir, p.eps AS p_eps,
                       m.registro_medico, m.especialidad,
                       m.tipo_documento AS m_tipo_doc, m.numero_documento AS m_num_doc,
                       m.fecha_nacimiento AS m_fecha_nac, m.direccion AS m_dir, m.eps AS m_eps,
                       a.codigo_admin, a.cargo,
                       a.tipo_documento AS a_tipo_doc, a.numero_documento AS a_num_doc,
                       a.fecha_nacimiento AS a_fecha_nac, a.direccion AS a_dir, a.eps AS a_eps
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
                    resultSet.getString("p_tipo_doc"),
                    resultSet.getString("p_num_doc"),
                    LocalDate.parse(resultSet.getString("p_fecha_nac")),
                    resultSet.getString("p_dir"),
                    resultSet.getString("p_eps")
            );
            case MEDICO -> {
                String fnMed = resultSet.getString("m_fecha_nac");
                yield new Medico(
                    resultSet.getString("id"),
                    resultSet.getString("nombre"),
                    resultSet.getString("apellido"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("telefono"),
                    resultSet.getString("registro_medico"),
                    Especialidad.valueOf(resultSet.getString("especialidad")),
                    resultSet.getString("m_tipo_doc"),
                    resultSet.getString("m_num_doc"),
                    fnMed != null ? LocalDate.parse(fnMed) : null,
                    resultSet.getString("m_dir"),
                    resultSet.getString("m_eps")
                );
            }
            case ADMIN -> {
                String fnAdm = resultSet.getString("a_fecha_nac");
                yield new Administrador(
                    resultSet.getString("id"),
                    resultSet.getString("nombre"),
                    resultSet.getString("apellido"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("telefono"),
                    resultSet.getString("codigo_admin"),
                    resultSet.getString("cargo"),
                    resultSet.getString("a_tipo_doc"),
                    resultSet.getString("a_num_doc"),
                    fnAdm != null ? LocalDate.parse(fnAdm) : null,
                    resultSet.getString("a_dir"),
                    resultSet.getString("a_eps")
                );
            }
        };
    }
}
