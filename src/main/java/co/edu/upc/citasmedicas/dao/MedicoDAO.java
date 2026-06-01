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
