package co.edu.upc.citasmedicas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Administra la conexion a la base de datos SQLite.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:citasmedicas.db";
    private static boolean initialized;

    public static Connection getConnection() throws SQLException {
        initialize();
        return DriverManager.getConnection(URL);
    }

    public static synchronized void initialize() throws SQLException {
        if (initialized) {
            return;
        }

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
            crearTablas(statement);
            insertarDatosDemo(connection);
            initialized = true;
        }
    }

    private static void crearTablas(Statement statement) throws SQLException {
        statement.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id TEXT PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    apellido TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    telefono TEXT NOT NULL,
                    rol TEXT NOT NULL,
                    activo INTEGER NOT NULL DEFAULT 1
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS pacientes (
                    usuario_id TEXT PRIMARY KEY,
                    tipo_documento TEXT NOT NULL,
                    numero_documento TEXT NOT NULL,
                    fecha_nacimiento TEXT NOT NULL,
                    direccion TEXT NOT NULL,
                    eps TEXT NOT NULL,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS medicos (
                    usuario_id TEXT PRIMARY KEY,
                    registro_medico TEXT NOT NULL,
                    especialidad TEXT NOT NULL,
                    consultorio TEXT NOT NULL,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS administradores (
                    usuario_id TEXT PRIMARY KEY,
                    codigo_admin TEXT NOT NULL,
                    cargo TEXT NOT NULL,
                    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS citas (
                    id TEXT PRIMARY KEY,
                    paciente_id TEXT NOT NULL,
                    medico_id TEXT NOT NULL,
                    especialidad TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    estado TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    motivo TEXT NOT NULL,
                    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id),
                    FOREIGN KEY (medico_id) REFERENCES medicos(usuario_id)
                )
                """);
    }

    private static void insertarDatosDemo(Connection connection) throws SQLException {
        insertarUsuario(connection, "pac-001", "Laura", "Gomez", "paciente@demo.com", "1234", "3001112233", "PACIENTE");
        insertarUsuario(connection, "med-001", "Carlos", "Rojas", "medico@demo.com", "1234", "3002223344", "MEDICO");
        insertarUsuario(connection, "adm-001", "Andrea", "Torres", "admin@demo.com", "1234", "3003334455", "ADMIN");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-001", "CC", "100200300", "1999-04-12", "Calle 45 #12-20", "Salud Total");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO medicos
                (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """, "med-001", "RM-7788", "MEDICINA_GENERAL", "Consultorio 203");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO administradores
                (usuario_id, codigo_admin, cargo)
                VALUES (?, ?, ?)
                """, "adm-001", "ADM-001", "Coordinadora de agenda");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, fecha, hora_inicio, estado, tipo, motivo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-001", "pac-001", "med-001", "MEDICINA_GENERAL", "2026-05-28", "09:00",
                "CONFIRMADA", "PRESENCIAL", "Control general");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, fecha, hora_inicio, estado, tipo, motivo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-002", "pac-001", "med-001", "MEDICINA_GENERAL", "2026-06-02", "10:30",
                "PENDIENTE", "VIRTUAL", "Revision de resultados");
    }

    private static void insertarUsuario(Connection connection, String id, String nombre, String apellido,
                                        String email, String password, String telefono, String rol)
            throws SQLException {
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO usuarios
                (id, nombre, apellido, email, password, telefono, rol, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?, 1)
                """, id, nombre, apellido, email, password, telefono, rol);
    }

    private static void ejecutarInsert(Connection connection, String sql, String... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setString(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }
}
