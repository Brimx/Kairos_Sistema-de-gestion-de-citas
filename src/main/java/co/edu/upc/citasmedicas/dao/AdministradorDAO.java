package co.edu.upc.citasmedicas.dao;

import co.edu.upc.citasmedicas.model.Administrador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos para la entidad Administrador.
 */
public class AdministradorDAO {

    public void guardar(Administrador administrador) {
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

    public Administrador buscarPorId(String id) {
        String sql = """
                SELECT u.id, u.nombre, u.apellido, u.email, u.password, u.telefono,
                       a.codigo_admin, a.cargo
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

    private Administrador mapearAdministrador(ResultSet resultSet) throws SQLException {
        return new Administrador(
                resultSet.getString("id"),
                resultSet.getString("nombre"),
                resultSet.getString("apellido"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("telefono"),
                resultSet.getString("codigo_admin"),
                resultSet.getString("cargo")
        );
    }
}
