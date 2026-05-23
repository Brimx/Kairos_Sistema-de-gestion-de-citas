package co.edu.upc.citasmedicas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Administra la conexión a la base de datos SQLite.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:citasmedicas.db";

    /**
     * Retorna una nueva conexión activa con la base de datos SQLite.
     * 
     * @return Connection objeto de conexión
     * @throws SQLException si ocurre un error al conectar
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
