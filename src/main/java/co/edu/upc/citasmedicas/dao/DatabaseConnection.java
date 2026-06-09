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
                    servicio TEXT NOT NULL DEFAULT 'MEDICINA_GENERAL',
                    fecha TEXT NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    duracion INTEGER NOT NULL DEFAULT 20,
                    estado TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    motivo TEXT NOT NULL,
                    origen TEXT NOT NULL DEFAULT 'PACIENTE',
                    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id),
                    FOREIGN KEY (medico_id) REFERENCES medicos(usuario_id)
                )
                """);

        try {
            statement.execute("ALTER TABLE citas ADD COLUMN servicio TEXT NOT NULL DEFAULT 'MEDICINA_GENERAL'");
        } catch (Exception ignored) {
        }

        try {
            statement.execute("ALTER TABLE citas ADD COLUMN duracion INTEGER NOT NULL DEFAULT 20");
        } catch (Exception ignored) {
        }

        try {
            statement.execute("ALTER TABLE citas ADD COLUMN origen TEXT NOT NULL DEFAULT 'PACIENTE'");
        } catch (Exception ignored) {
        }

        try {
            statement.execute("ALTER TABLE citas ADD COLUMN sobrecupo INTEGER NOT NULL DEFAULT 0");
        } catch (Exception ignored) {
        }

        statement.execute("""
                CREATE TABLE IF NOT EXISTS agenda_medica (
                    id TEXT PRIMARY KEY,
                    medico_id TEXT NOT NULL,
                    dia_semana INTEGER NOT NULL,
                    hora_inicio TEXT NOT NULL,
                    hora_fin TEXT NOT NULL,
                    slot_minutos INTEGER NOT NULL DEFAULT 20,
                    FOREIGN KEY (medico_id) REFERENCES medicos(usuario_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS bloqueos_agenda (
                    id TEXT PRIMARY KEY,
                    medico_id TEXT NOT NULL,
                    fecha TEXT NOT NULL,
                    hora_inicio TEXT,
                    hora_fin TEXT,
                    motivo TEXT NOT NULL,
                    FOREIGN KEY (medico_id) REFERENCES medicos(usuario_id)
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS historial_clinico (
                    id TEXT PRIMARY KEY,
                    cita_id TEXT NOT NULL,
                    medico_id TEXT NOT NULL,
                    paciente_id TEXT NOT NULL,
                    fecha_consulta TEXT NOT NULL,
                    diagnostico TEXT,
                    enfermedad_actual TEXT,
                    receta TEXT,
                    remision TEXT,
                    notas TEXT,
                    FOREIGN KEY (cita_id) REFERENCES citas(id),
                    FOREIGN KEY (medico_id) REFERENCES medicos(usuario_id),
                    FOREIGN KEY (paciente_id) REFERENCES pacientes(usuario_id)
                )
                """);
    }

    private static void insertarDatosDemo(Connection connection) throws SQLException {
        insertarUsuario(connection, "pac-001", "Laura", "Gomez", "paciente@demo.com", "1234", "3001112233", "PACIENTE");
        insertarUsuario(connection, "pac-002", "Pedro", "Martinez", "pedro@demo.com", "1234", "3004445566", "PACIENTE");
        insertarUsuario(connection, "pac-003", "Ana", "Lopez", "ana@demo.com", "1234", "3005556677", "PACIENTE");
        insertarUsuario(connection, "pac-004", "Sofia", "Ramirez", "sofia@demo.com", "1234", "3008889900", "PACIENTE");
        insertarUsuario(connection, "pac-005", "Diego", "Castillo", "diego@demo.com", "1234", "3001113344", "PACIENTE");
        insertarUsuario(connection, "pac-006", "Carolina", "Mendoza", "carolina@demo.com", "1234", "3002225566", "PACIENTE");

        insertarUsuario(connection, "med-001", "Carlos", "Rojas", "medico@demo.com", "1234", "3002223344", "MEDICO");
        insertarUsuario(connection, "med-002", "Pedro", "Perez", "odontologia@demo.com", "1234", "3006667788", "MEDICO");
        insertarUsuario(connection, "med-003", "Maria", "Gomez", "pediatria@demo.com", "1234", "3007778899", "MEDICO");
        insertarUsuario(connection, "med-004", "Liliana", "Torres", "dermatologia@demo.com", "1234", "3003336677", "MEDICO");
        insertarUsuario(connection, "med-005", "Andres", "Mendoza", "psicologia@demo.com", "1234", "3004447788", "MEDICO");

        insertarUsuario(connection, "adm-001", "Andrea", "Torres", "admin@demo.com", "1234", "3003334455", "ADMIN");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-001", "CC", "100200300", "1999-04-12", "Calle 45 #12-20", "Salud Total");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-002", "CC", "100400500", "1985-08-23", "Cra 10 #20-30", "Coomeva");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-003", "TI", "100600700", "1992-11-15", "Av 5 #15-25", "Sura");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-004", "CC", "101200300", "1990-02-18", "Carrera 12 #34-56", "Medimas");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-005", "CE", "PZ-456789", "1978-07-30", "Calle 8 #15-40", "Sanitas");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO pacientes
                (usuario_id, tipo_documento, numero_documento, fecha_nacimiento, direccion, eps)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "pac-006", "CC", "102300400", "2000-10-05", "Av Siempre Viva #123", "Compensar");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO medicos
                (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """, "med-001", "RM-7788", "MEDICINA_GENERAL", "Consultorio 203");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO medicos
                (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """, "med-002", "RM-9900", "ODONTOLOGIA", "Consultorio 205");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO medicos
                (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """, "med-003", "RM-1122", "PEDIATRIA", "Consultorio 110");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO medicos
                (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """, "med-004", "RM-3344", "DERMATOLOGIA", "Consultorio 302");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO medicos
                (usuario_id, registro_medico, especialidad, consultorio)
                VALUES (?, ?, ?, ?)
                """, "med-005", "RM-5566", "PSICOLOGIA", "Consultorio 401");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO administradores
                (usuario_id, codigo_admin, cargo)
                VALUES (?, ?, ?)
                """, "adm-001", "ADM-001", "Coordinadora de agenda");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-001", "pac-001", "med-001", "MEDICINA_GENERAL", "MEDICINA_GENERAL",
                "2026-05-28", "09:00", "20", "CONFIRMADA", "PRESENCIAL", "Control general", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-002", "pac-001", "med-001", "MEDICINA_GENERAL", "MEDICINA_GENERAL",
                "2026-06-10", "10:30", "20", "PENDIENTE", "VIRTUAL", "Revision de resultados", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-003", "pac-002", "med-002", "ODONTOLOGIA", "ODONTOLOGIA_GENERAL",
                "2026-06-11", "09:30", "30", "CONFIRMADA", "PRESENCIAL", "Limpieza dental", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-004", "pac-003", "med-003", "PEDIATRIA", "PEDIATRIA",
                "2026-05-15", "15:00", "20", "COMPLETADA", "PRESENCIAL", "Control pediatrico", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-005", "pac-004", "med-004", "DERMATOLOGIA", "DERMATOLOGIA",
                "2026-05-10", "10:00", "20", "COMPLETADA", "PRESENCIAL", "Revision lunar", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-006", "pac-005", "med-005", "PSICOLOGIA", "PSICOLOGIA",
                "2026-05-20", "14:00", "45", "NO_ASISTIO", "PRESENCIAL", "Primera consulta", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-007", "pac-002", "med-002", "ODONTOLOGIA", "ODONTOLOGIA_GENERAL",
                "2026-04-30", "11:00", "30", "CANCELADA", "PRESENCIAL", "Caries urgente", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-008", "pac-001", "med-001", "MEDICINA_GENERAL", "MEDICINA_GENERAL",
                "2026-05-05", "08:00", "20", "COMPLETADA", "PRESENCIAL", "Dolor de cabeza", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-009", "pac-006", "med-001", "MEDICINA_GENERAL", "VACUNACION",
                "2026-06-12", "08:20", "15", "PENDIENTE", "PRESENCIAL", "Vacuna influenza", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-010", "pac-003", "med-003", "PEDIATRIA", "CRECIMIENTO_DESARROLLO",
                "2026-06-15", "14:40", "30", "CONFIRMADA", "PRESENCIAL", "Control crecimiento", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-011", "pac-005", "med-005", "PSICOLOGIA", "PSICOLOGIA",
                "2026-06-09", "16:00", "45", "CONFIRMADA", "VIRTUAL", "Terapia semanal", "PACIENTE", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-012", "pac-004", "med-004", "DERMATOLOGIA", "DERMATOLOGIA",
                "2026-06-16", "09:00", "20", "PENDIENTE", "PRESENCIAL", "Control lunar", "CONTROL", "0");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO citas
                (id, paciente_id, medico_id, especialidad, servicio, fecha, hora_inicio, duracion, estado, tipo, motivo, origen, sobrecupo)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, "cit-013", "pac-002", "med-002", "ODONTOLOGIA", "ODONTOLOGIA_GENERAL",
                "2026-06-11", "10:00", "30", "PENDIENTE", "PRESENCIAL", "Sobrecupo urgente", "PACIENTE", "1");

        insertarHistorial(connection, "his-001", "cit-004", "med-003", "pac-003", "2026-05-15",
                "Paciente pediatrico sano. Control de rutina sin hallazgos.",
                "Ninguna", "N/A", "N/A", "Peso y talla dentro de lo normal. Vacunacion al dia.");
        insertarHistorial(connection, "his-002", "cit-005", "med-004", "pac-004", "2026-05-10",
                "Nevo melanocitico benigno en brazo izquierdo. Sin signos de malignidad.",
                "Ninguna", "N/A", "N/A", "Se recomienda vigilancia cada 6 meses. Proteccion solar.");
        insertarHistorial(connection, "his-003", "cit-008", "med-001", "pac-001", "2026-05-05",
                "Cefalea tensional. Sin signos de alarma.",
                "Ninguna", "Acetaminofen 500mg cada 8 horas por 5 dias", "N/A",
                "Paciente estable. Se recomienda hidratacion y evitar estres visual.");

        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO bloqueos_agenda
                (id, medico_id, fecha, hora_inicio, hora_fin, motivo)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "blo-001", "med-001", "2026-06-10", "08:00", "09:00", "Junta medica");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO bloqueos_agenda
                (id, medico_id, fecha, hora_inicio, hora_fin, motivo)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "blo-002", "med-002", "2026-06-11", null, null, "Vacaciones");
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO bloqueos_agenda
                (id, medico_id, fecha, hora_inicio, hora_fin, motivo)
                VALUES (?, ?, ?, ?, ?, ?)
                """, "blo-003", "med-003", "2026-06-15", "14:00", "15:00", "Capacitacion");

        insertarAgenda(connection, "age-001", "med-001", 1, "08:00", "12:00", 20);
        insertarAgenda(connection, "age-002", "med-001", 2, "08:00", "12:00", 20);
        insertarAgenda(connection, "age-003", "med-001", 3, "08:00", "12:00", 20);
        insertarAgenda(connection, "age-004", "med-001", 4, "08:00", "12:00", 20);
        insertarAgenda(connection, "age-005", "med-001", 5, "08:00", "12:00", 20);

        insertarAgenda(connection, "age-006", "med-002", 1, "08:00", "17:00", 30);
        insertarAgenda(connection, "age-007", "med-002", 2, "08:00", "17:00", 30);
        insertarAgenda(connection, "age-008", "med-002", 3, "08:00", "17:00", 30);

        insertarAgenda(connection, "age-009", "med-003", 1, "14:00", "18:00", 20);
        insertarAgenda(connection, "age-010", "med-003", 2, "14:00", "18:00", 20);
        insertarAgenda(connection, "age-011", "med-003", 3, "14:00", "18:00", 20);
        insertarAgenda(connection, "age-012", "med-003", 4, "14:00", "18:00", 20);
        insertarAgenda(connection, "age-013", "med-003", 5, "14:00", "18:00", 20);

        insertarAgenda(connection, "age-014", "med-004", 2, "08:00", "12:00", 20);
        insertarAgenda(connection, "age-015", "med-004", 4, "08:00", "12:00", 20);
        insertarAgenda(connection, "age-016", "med-004", 4, "14:00", "17:00", 20);

        insertarAgenda(connection, "age-017", "med-005", 1, "14:00", "19:00", 45);
        insertarAgenda(connection, "age-018", "med-005", 3, "14:00", "19:00", 45);
        insertarAgenda(connection, "age-019", "med-005", 5, "14:00", "19:00", 45);
    }

    private static void insertarAgenda(Connection connection, String id, String medicoId, int diaSemana,
                                       String horaInicio, String horaFin, int slotMinutos) throws SQLException {
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO agenda_medica
                (id, medico_id, dia_semana, hora_inicio, hora_fin, slot_minutos)
                VALUES (?, ?, ?, ?, ?, ?)
                """, id, medicoId, String.valueOf(diaSemana), horaInicio, horaFin, String.valueOf(slotMinutos));
    }

    private static void insertarHistorial(Connection connection, String id, String citaId, String medicoId,
                                           String pacienteId, String fecha, String diagnostico,
                                           String enfermedadActual, String receta, String remision,
                                           String notas) throws SQLException {
        ejecutarInsert(connection, """
                INSERT OR IGNORE INTO historial_clinico
                (id, cita_id, medico_id, paciente_id, fecha_consulta, diagnostico, enfermedad_actual, receta, remision, notas)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id, citaId, medicoId, pacienteId, fecha, diagnostico, enfermedadActual, receta, remision, notas);
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
