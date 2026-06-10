package co.edu.upc.citasmedicas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracionDAO {

    public String obtenerValor(String clave) {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, clave);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("valor");
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo leer la configuracion", exception);
        }

        return null;
    }

    public void actualizarValor(String clave, String valor) {
        String sql = "INSERT OR REPLACE INTO configuracion (clave, valor) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, clave);
            statement.setString(2, valor);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No se pudo actualizar la configuracion", exception);
        }
    }
}
