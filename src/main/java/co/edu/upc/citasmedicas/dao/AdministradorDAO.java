package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.Administrador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class AdministradorDAO {

    public void guardar(Administrador administrador) {
        String sqlUsuario = """
                INSERT INTO usuarios (id, nombre, apellido, email, password, telefono, rol, activo)
                VALUES (?, ?, ?, ?, ?, ?, 'ADMIN', 1)
                """;

        String sqlAdministrador = """
                INSERT INTO administradores (usuario_id, codigo_admin, cargo, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
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

    public Administrador buscarPorId(String id) {
        String sql = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.password, u.telefono,
                       a.codigo_admin, a.cargo, a.tipo_documento, a.numero_documento,
                       a.fecha_nacimiento, a.direccion, a.eps
                FROM administradores a
                JOIN usuarios u ON u.id = a.usuario_id
                WHERE u.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearAdministrador(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo buscar el administrador", exception);
        }

        return null;
    }

    public void actualizarTodo(Administrador admin) {
        String sqlUsuario = """
                UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, telefono = ?
                WHERE id = ? AND rol = 'ADMIN' AND activo = 1
                """;
        String sqlAdmin = """
                UPDATE administradores SET cargo = ?, tipo_documento = ?, numero_documento = ?,
                    fecha_nacimiento = ?, direccion = ?, eps = ?
                WHERE usuario_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtUsuario = connection.prepareStatement(sqlUsuario);
             PreparedStatement stmtAdmin = connection.prepareStatement(sqlAdmin)) {

            connection.setAutoCommit(false);

            stmtUsuario.setString(1, admin.getNombre());
            stmtUsuario.setString(2, admin.getApellido());
            stmtUsuario.setString(3, admin.getEmail());
            stmtUsuario.setString(4, admin.getTelefono());
            stmtUsuario.setString(5, admin.getId());

            if (stmtUsuario.executeUpdate() == 0) {
                throw new IllegalArgumentException("No se encontro un administrador activo con ese id");
            }

            stmtAdmin.setString(1, admin.getCargo());
            stmtAdmin.setString(2, admin.getTipoDocumento());
            stmtAdmin.setString(3, admin.getNumeroDocumento());
            stmtAdmin.setString(4, admin.getFechaNacimiento() != null ? admin.getFechaNacimiento().toString() : null);
            stmtAdmin.setString(5, admin.getDireccion());
            stmtAdmin.setString(6, admin.getEps());
            stmtAdmin.setString(7, admin.getId());
            stmtAdmin.executeUpdate();

            connection.commit();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar el administrador", exception);
        }
    }

    private Administrador mapearAdministrador(ResultSet resultSet) throws SQLException {
        String fn = resultSet.getString("fecha_nacimiento");
        return new Administrador(
                resultSet.getString("id"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("telefono"),
                resultSet.getString("codigo_admin"),
                resultSet.getString("cargo"),
                resultSet.getString("tipo_documento"),
                resultSet.getString("numero_documento"),
                fn != null ? LocalDate.parse(fn) : null,
                resultSet.getString("direccion"),
                resultSet.getString("eps")
        );
    }
}
