# Kairos — Documentación Técnica

> **Versión del proyecto:** `1.0-SNAPSHOT`  
> **Grupo Maven:** `co.edu.upc`  
> **Artefacto:** `SistemaCitasMedicas`  
> **Fecha:** Junio 2026

---

## Índice

1. [Stack Tecnológico](#1-stack-tecnológico)
2. [Estructura del Proyecto](#2-estructura-del-proyecto)
3. [Arquitectura en Capas](#3-arquitectura-en-capas)
4. [Punto de Entrada](#4-punto-de-entrada)
5. [Capa de Modelos](#5-capa-de-modelos)
6. [Capa de Enumeraciones](#6-capa-de-enumeraciones)
7. [Capa de Servicios (Service)](#7-capa-de-servicios)
8. [Capa de Acceso a Datos (DAO)](#8-capa-de-acceso-a-datos)
9. [Capa de Presentación (Vistas y Controladores)](#9-capa-de-presentación)
10. [Patrones de Diseño Implementados](#10-patrones-de-diseño)
11. [Características de Java Utilizadas](#11-características-de-java)

---

## 1. Stack Tecnológico

| Componente | Versión | Propósito | ¿Por qué se eligió? |
|---|---|---|---|
| **Java** | 17 (LTS) | Lenguaje base | Versión LTS más reciente al inicio del proyecto; ofrece `switch` con pattern matching, `records` potenciales, `sealed classes`, y `text blocks` (`"""..."""`) que se usan extensivamente en las queries SQL. La última LTS anterior (11) no tiene text blocks ni switch expressions pulidas. |
| **Maven** | 3.13.0 (plugin) | Build y dependencias | Estándar de la industria Java; el `pom.xml` declara todo explícitamente. Se usa `maven-compiler-plugin` 3.13.0 con `<release>17</release>` para garantizar bytecode compatible con JRE 17+. |
| **JavaFX** | 21.0.2 | UI de escritorio | Framework oficial de Oracle para GUIs modernas en Java. Se eligió sobre Swing porque ofrece FXML (separación de UI y lógica), CSS styling, y componentes ricos (`TableView`, `DatePicker`, `ChoiceBox`) listos para usar. Se integra con CalendarFX. |
| **SQLite (Xerial JDBC)** | 3.45.2.0 | Base de datos embebida | No requiere instalación de servidor; la base es un solo archivo (`citasmedicas.db`). Ideal para un sistema de escritorio sin infraestructura. El driver Xerial es el estándar de facto para SQLite en Java. |
| **CalendarFX** | 11.12.7 | Componente calendario visual | Única librería de calendario madura para JavaFX. Permite vista mensual/semanal con eventos solapables. Se eligió sobre construir uno propio porque ahorra cientos de horas de desarrollo de UI calendárica. |
| **SLF4J-NOP** | 1.7.36 | Silenciar logs de SQLite | SQLite-JDBC emite warnings de SLF4J si no encuentra una implementación; `slf4j-nop` es un "agujero negro" de logs: elimina el ruido en consola sin peso de configuración. |

### plugin.xml — Explicación de plugins

- **`maven-compiler-plugin` 3.13.0**: Configura `--release 17`, que equivale a `-source 17 -target 17` y además verifica que no uses APIs de versiones posteriores.
- **`javafx-maven-plugin` 0.0.8**: Ejecuta la app JavaFX desde Maven sin necesidad de configurar manualmente los módulos de JavaFX en el classpath. El `mainClass` apunta a `Launcher` (no a `Main` directamente) para compatibilidad con NetBeans.
- **`maven-assembly-plugin` 3.7.1**: Empaqueta un fat-JAR (`jar-with-dependencies`) que incluye todas las dependencias. Útil para distribuir un solo archivo ejecutable.

### module-info.java — Sistema de Módulos (JPMS)

Se usa el sistema de módulos de Java 9+ (JPMS) para:
- Declarar dependencias explícitas (`requires transitive javafx.controls`, `requires java.sql`, etc.)
- Abrir paquetes a JavaFX (`opens ... to javafx.fxml`) para que FXML pueda inyectar componentes con `@FXML`
- Exportar paquetes para que el motor JavaFX pueda acceder a controllers y views

El `requires transitive java.sql` hace que cualquier módulo que dependa de `co.edu.upc.citasmedicas` también vea `java.sql`, lo cual es correcto porque los DAOs exponen `SQLException`.

---

## 2. Estructura del Proyecto

```
src/main/java/co/edu/upc/citasmedicas/
├── Launcher.java              # Entry point puente (NetBeans compat)
├── Main.java                  # Application JavaFX
├── module-info.java           # JPMS module descriptor
├── controller/                # Controladores FXML (5 archivos)
├── dao/                       # Data Access Objects (9 archivos)
├── enums/                     # Enumeraciones del dominio (6 archivos)
├── model/                     # Clases de dominio (8 archivos)
├── service/                   # Lógica de negocio (6 archivos)
└── view/                      # Gestión de vistas (ViewManager)

src/main/resources/co/edu/upc/citasmedicas/
├── fxml/                      # Archivos FXML de UI (5 archivos)
└── fonts/                     # Tipografías IBM Plex (13 archivos .ttf)
```

---

## 3. Arquitectura en Capas

```
┌─────────────────────────────────────────────────────────────┐
│                    VIEW (FXML)                               │
│  login.fxml / registro.fxml / dashboard_*.fxml               │
└──────────────────┬──────────────────────────────────────────┘
                   │ Inyección de eventos @FXML
┌──────────────────▼──────────────────────────────────────────┐
│              CONTROLLER (controller/)                        │
│  LoginController / RegistroController / Dashboard*Controller │
│  Recibe eventos UI, llama a Services, actualiza la UI        │
└──────────────────┬──────────────────────────────────────────┘
                   │ Llamadas a métodos de servicio
┌──────────────────▼──────────────────────────────────────────┐
│              SERVICE (service/)                              │
│  CitaService / PacienteService / DisponibilidadService /     │
│  ValidacionService / InasistenciaService / Session           │
│  Aquí viven las REGLAS DE NEGOCIO                            │
└──────────────────┬──────────────────────────────────────────┘
                   │ Llamadas a DAOs
┌──────────────────▼──────────────────────────────────────────┐
│              DAO (dao/)                                      │
│  CitaDAO / UsuarioDAO / PacienteDAO / MedicoDAO /            │
│  AgendaMedicaDAO / BloqueoAgendaDAO / HistorialClinicoDAO    │
│  SQL puro, mapeo ResultSet → Modelos                         │
└──────────────────┬──────────────────────────────────────────┘
                   │ JDBC
┌──────────────────▼──────────────────────────────────────────┐
│              SQLite (citasmedicas.db)                         │
└─────────────────────────────────────────────────────────────┘

Los MODELOS (model/) y ENUMS (enums/) son transversales:
los usa TODAS las capas.
```

**¿Por qué esta arquitectura?**  
Se eligió una arquitectura en capas clásica (no Spring Boot, no microservicios) porque:
- Es una aplicación de escritorio monolítica — no necesita IoC container ni inyección de dependencias compleja.
- La separación View → Controller → Service → DAO permite testear la lógica de negocio sin cargar la UI ni la base de datos real.
- No se usa interfaz entre Service y DAO (como se haría en una app enterprise) porque no hay múltiples implementaciones de persistencia — solo SQLite.

---

## 4. Punto de Entrada

### `Launcher.java`

```java
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
```

**¿Por qué existe?** NetBeans (IDE usado en el proyecto) a veces ejecuta con `exec-maven-plugin` en vez de `javafx-maven-plugin`. El `Launcher` es un puente: llama a `Main.main()` que a su vez llama a `Application.launch()`. Así funciona tanto desde Maven como desde NetBeans.

### `Main.java`

```java
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        loadFonts();
        ViewManager.setPrimaryStage(primaryStage);
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml",
            "Kairos - Sistema de Gestion de Citas");
    }
}
```

- **`extends Application`**: Clase base de toda app JavaFX. Su método `start()` es el equivalente al `main()` de JavaFX.
- **`loadFonts()`**: Carga 9 variantes de IBM Plex Sans y Mono desde resources. Se hace manualmente (no CSS) porque JavaFX no siempre descubre fuentes empaquetadas en JARs.
- **`ViewManager.setPrimaryStage()`**: Almacena la ventana principal (Stage) para que `ViewManager` pueda cambiar de escena globalmente. **¿Por qué no pasar el Stage como parámetro?** Porque los controllers se crean via FXML (FXMLLoader) y no tienen constructor parametrizado; tenerlo en una variable estática es la solución más pragmática sin recurrir a DI.

---

## 5. Capa de Modelos

### `Usuario` (abstracta) — `model/Usuario.java`

```java
public abstract class Usuario {
    private String id;
    private String nombre, apellido, email, password, telefono;
    private Rol rol;
    private boolean activo;
    public abstract String[] getMenuOpciones();
}
```

**¿Por qué abstracta?**  
No tiene sentido crear un `Usuario` genérico. Todo usuario del sistema es `Paciente`, `Medico` o `Administrador`. La abstracción fuerza a que cada subclase implemente `getMenuOpciones()`, que devuelve las opciones de menú específicas de su rol (patrón **Template Method**).

**Generación de IDs:**  
```java
private static String generarIdUnico() {
    return UUID.randomUUID().toString().substring(0, 8);
}
```
Se usa `UUID.randomUUID()` (identificador único universal, 128 bits, probabilidad de colisión insignificante) truncado a 8 caracteres por legibilidad. **¿Por qué no auto-increment?** SQLite puede tener auto-increment, pero los IDs alfanuméricos cortos son más amigables para depuración y se pueden generar en código sin depender de la BD.

**`login()`:** Es un método de instancia (no estático) que compara email y password. No hace hash porque es demo. **¿Por qué no estático?** Porque necesitas obtener el usuario primero (desde la BD) y luego verificar su contraseña contra el objeto.

**Uso de `@Override` en `toString()`:** Se sobrescribe para mostrar `[ROL] Nombre Apellido | Email: ...`. Útil para depuración y para mostrar en ComboBoxes de la UI.

### `Paciente` — `model/Paciente.java`

```java
public class Paciente extends Usuario {
    private String tipoDocumento;     // CC, TI, CE, Pasaporte
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String eps;
    private Stack<Cita> historialCitas;  // LIFO
}
```

**¿Por qué `Stack<Cita>` para el historial?**  
`Stack` es LIFO (Last In, First Out). En un historial clínico, la cita más reciente es la más relevante. Con `push()` se agrega al tope y `peek()` la obtiene en O(1) sin recorrer toda la lista. **¿Por qué no `ArrayList` y ordenar por fecha?** Porque `Stack` expresa intencionalmente que "lo último es lo primero", mientras que un ArrayList necesitaría ordenarse cada vez.

**`getMenuOpciones()`:** Devuelve opciones como "Solicitar cita", "Ver mis citas", etc. Cada rol tiene su propio menú, personalizado según sus permisos.

### `Medico` — `model/Medico.java`

```java
public class Medico extends Usuario {
    private String registroMedico;
    private Especialidad especialidad;
    private String consultorio;
    private Queue<Cita> agendaDelDia;    // FIFO
    private int citasAsignadas;
}
```

**¿Por qué `Queue<Cita>` (FIFO con `LinkedList`)?**  
Un médico atende pacientes en orden de llegada. FIFO (First In, First Out) modela exactamente eso. `LinkedList` implementa `Queue` y ofrece `offer()` para agregar al final y `poll()` para sacar del frente, ambos O(1).

**Contraste con `Paciente.historialCitas`:** El paciente necesita ver la última cita (LIFO → Stack). El médico necesita atender en orden de llegada (FIFO → Queue). Cada estructura de datos refleja el caso de uso específico.

### `Administrador` — `model/Administrador.java`

```java
public class Administrador extends Usuario {
    private String codigoAdmin;
    private String cargo;
}
```

El más simple de los tres. No tiene estructuras de datos internas porque el administrador no agenda citas directas ni tiene historial. Su `getMenuOpciones()` incluye "Gestionar pacientes", "Gestionar medicos", "Ver reportes", etc.

**`gestionarUsuario()` y `verReportes()`** están declarados pero vacíos — son placeholders para expansión futura.

### `Cita` — `model/Cita.java`

```java
public class Cita {
    private String id;
    private Paciente paciente;
    private Medico medico;
    private Especialidad especialidad;
    private ServicioCita servicio;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int duracion;
    private String origen;        // "PACIENTE" o "CONTROL"
    private boolean sobrecupo;
    private EstadoCita estado;
    private TipoCita tipo;
    private String motivo;
}
```

**Máquina de estados (métodos `confirmar()`, `cancelar()`, `completar()`):**

```
PENDIENTE ──confirmar()──> CONFIRMADA ──completar()──> COMPLETADA
    │                          │
    └────cancelar()────────────┘
    (cualquier estado excepto COMPLETADA/NO_ASISTIO puede ir a CANCELADA via CitaService)
```

Cada método verifica que la transición sea válida y retorna `boolean`. **¿Por qué no lanzar excepción aquí?** Porque la clase modelo no debe conocer la capa de servicio; retorna `false` y el service decide si lanza excepción (separación de responsabilidades).

**`setHoraInicio()`** recalcula automáticamente `horaFin = horaInicio + duracion`. Esto garantiza consistencia sin depender de que el llamador actualice ambos campos.

**Dos constructores:** Uno con 9 parámetros (establece `origen = "PACIENTE"` por defecto) y otro con 10 (permite definir origen). Esto evita romper código existente mientras se añade la funcionalidad de citas de control.

### `AgendaMedica` — `model/AgendaMedica.java`

Define el horario recurrente de un médico: días de la semana que trabaja, hora de inicio/fin, y duración de cada slot (20, 30 o 45 minutos según especialidad).

**`slotMinutos`:** Es el intervalo entre inicios de cita. No es necesariamente la duración de la cita (un slot de 20 min con una cita de 15 min dejaría 5 min libres).

### `BloqueoAgenda` — `model/BloqueoAgenda.java`

Bloquea parcial o totalmente la agenda de un médico en una fecha específica.

**`esDiaCompleto()`:** Retorna `true` si `horaInicio == null && horaFin == null`. **¿Por qué no un booleano explícito?** Porque SQL almacena NULLs. Si ambos son NULL, significa "todo el día". Esto evita una columna extra.

### `HistorialClinico` — `model/HistorialClinico.java`

Registro de una consulta completada: diagnóstico, enfermedad actual, receta, remisión, notas.

**Constructor vacío:** Se incluye explícitamente porque también hay uno con parámetros. Java solo provee el vacío si no declaras ningún constructor. Aquí se declara porque el DAO `HistorialClinicoDAO` usa setters para mapear desde ResultSet (en vez del constructor con parámetros). Esto es deliberado: la clase tiene muchos campos (10) y el constructor con parámetros es propenso a errores de orden.

---

## 6. Capa de Enumeraciones

### `Rol` — `enums/Rol.java`

Tres valores: `PACIENTE`, `MEDICO`, `ADMIN`. Se usa en `Usuario` para diferenciar tipos.

**¿Por qué enum y no una clase con subtipos?** Ya hay subtipos (`Paciente`, `Medico`, `Administrador`). El `Rol` como enum permite hacer switch-case sobre el tipo sin usar `instanceof`. Clave en `UsuarioDAO.mapearUsuario()`:

```java
return switch (rol) {
    case PACIENTE -> new Paciente(...);
    case MEDICO   -> new Medico(...);
    case ADMIN    -> new Administrador(...);
};
```

Este `switch` es **exhaustivo** (el compilador verifica que cubres todos los casos). Si alguien agrega un nuevo rol al enum, el código no compila hasta que se agregue el caso.

### `EstadoCita` — `enums/EstadoCita.java`

Ciclo de vida de la cita: `PENDIENTE → CONFIRMADA → COMPLETADA / CANCELADA / NO_ASISTIO`.

**¿Por qué enum en vez de constantes `static final`?** Tipado fuerte. No puedes pasar un string inválido. Además, el switch exhaustivo funciona igual que con `Rol`.

### `TipoCita` — `enums/TipoCita.java`

`PRESENCIAL` y `VIRTUAL`. Tiene un atributo `nombre` para mostrar en UI ("Presencial", "Virtual") y el método estático `fromNombre()` para parsear desde strings.

**¿Por qué no usar solo `name()`?** Porque `PRESENCIAL` en UI se ve mal. El `nombre` (con mayúscula inicial sin guión bajo) es más presentable.

### `OrigenCita` — `enums/OrigenCita.java`

Dos valores: `PACIENTE` (autogestionada) y `CONTROL` (agendada por el médico para seguimiento). Esto permite distinguir el flujo: las citas de control pueden saltar ciertas validaciones.

### `Especialidad` — `enums/Especialidad.java`

17 especialidades médicas. Similar a `TipoCita`, tiene `getNombre()` para display y `fromNombre()` para parseo.

### `ServicioCita` — `enums/ServicioCita.java`

El enum más complejo. 17 servicios, cada uno con:
- **`grupo`**: Categoría lógica (ej: "Especialidades Medicas") — usado para agrupar en la UI
- **`especialidadRequerida`**: Qué especialidad médica puede prestar este servicio
- **`duracionMinutos`**: Duración para primera vez
- **`duracionControlMinutos`**: Duración para citas de control/seguimiento

**¿Por qué duración dinámica?** Una cita de CONTROL suele ser más corta que una primera vez. Ej: `PSICOLOGIA` primera vez = 45 min, control = 30 min.

**Métodos estáticos:**
- `porGrupo(String)`: Filtra servicios por grupo. Útil para llenar ChoiceBoxes agrupados.
- `grupos()`: Lista única de grupos, en orden de definición.
- `fromNombre(String)`: Parsea desde string (para mapeo desde BD). Tolera espacios, guiones.

---

## 7. Capa de Servicios

### `Session` — `service/Session.java`

```java
public final class Session {
    private static Usuario usuarioActual;
    private Session() {}
    public static Usuario getUsuarioActual() { return usuarioActual; }
    public static void setUsuarioActual(Usuario u) { usuarioActual = u; }
    public static void cerrar() { usuarioActual = null; }
}
```

**¿Por qué Singleton estático y no instancia?** El usuario autenticado es un estado global de la aplicación. Todos los controllers necesitan accederlo. Usar un singleton estático evita pasarlo por constructor (los controllers son instanciados por FXMLLoader, no por ti). Es la solución más pragmática para este contexto.

**`final class` con constructor privado:** Evita instanciación y herencia. No hay razón para extender Session.

### `CitaService` — `service/CitaService.java`

Contiene toda la lógica de negocio de las citas. Métodos principales:

| Método | ¿Qué hace? | Regla de negocio clave |
|---|---|---|
| `agendarCita(Cita)` | Valida y guarda | No puede haber 2 citas activas en misma especialidad para el mismo paciente. La hora debe estar disponible. |
| `confirmarCita(String)` | PENDIENTE → CONFIRMADA | Solo si está PENDIENTE |
| `cancelarCita(String)` | → CANCELADA | Solo si PENDIENTE o CONFIRMADA |
| `completarCita(String)` | CONFIRMADA → COMPLETADA | Solo si CONFIRMADA |
| `atenderCita(String)` | Auto-confirma + completa | Si está PENDIENTE, la confirma automáticamente y luego la completa |
| `marcarNoAsistio(String)` | → NO_ASISTIO | No permite si ya está CANCELADA, COMPLETADA o NO_ASISTIO |
| `agendarSobrecupo(Cita)` | Guarda sin validar disponibilidad | `cita.setSobrecupo(true)` — usado para urgencias |
| `autoDetectarInasistencias()` | Busca citas vencidas y las marca NO_ASISTIO | Citas PENDIENTE/CONFIRMADA cuya fecha+hora ya pasaron |
| `citasDelPaciente(String)` | Lista citas activas de un paciente | Excluye CANCELADAS |
| `agendaDelMedico(String)` | Lista citas PENDIENTE/CONFIRMADA de un médico | La "agenda del día" |

**Validación en `agendarCita()`:** Llama primero a `validarCita()` que chequea nulos y blancos en TODOS los campos. Luego verifica duplicados por especialidad, y finalmente verifica disponibilidad horaria.

**¿Por qué `atenderCita()` auto-confirma?** Porque en la práctica un médico puede atender una cita que aún está PENDIENTE (nunca se confirmó explícitamente). El flujo es: el paciente llega → el médico lo atiende → la cita se completa. Si estaba PENDIENTE, se auto-confirma en el proceso.

### `DisponibilidadService` — `service/DisponibilidadService.java`

**Algoritmo de `obtenerHorasDisponibles()`:**

1. Obtiene la `AgendaMedica` del médico para ese día de la semana (ej: Lunes 08:00-12:00, slot 20 min)
2. Genera todos los slots candidatos desde `horaInicio` hasta `horaFin - duracionSolicitada`
3. Filtra contra citas ocupadas (no canceladas) y bloqueos de agenda

**Detección de solapamiento (`tieneSolapamiento()`):** Usa la lógica de intervalos:
```java
inicio.isBefore(cFin) && cInicio.isBefore(fin)
```
Dos intervalos [A, B) y [C, D) se solapan si A < D y C < B. Esto funciona para cualquier duración, no solo para slots exactos.

**¿Por qué no validar contra slots exactos?** Porque la duración de la cita puede diferir del `slotMinutos`. Una cita de 30 min puede ocupar 1.5 slots de 20 min. La lógica de intervalos detecta correctamente estos casos.

### `ValidacionService` — `service/ValidacionService.java`

Validación de email y teléfono colombiano.

**Validación de email — dos niveles:**

1. **Formato (`emailValidoFormato`)**: Regex que verifica `usuario@dominio.extension`
2. **Dominio MX (`dominioTieneMX`)**: Consulta DNS para verificar que el dominio tenga registros MX (Mail Exchange). **¿Por qué?** Para evitar que usuarios se registren con emails de dominios inexistentes o que no reciben correo. Usa JNDI (`javax.naming`) con `DnsContextFactory` para resolver DNS. Si el dominio no tiene MX, el email no podría recibir notificaciones.

**Validación de teléfono colombiano:** Regex `^(\\+57)?3\\d{9}$` — opcionalmente +57 seguido de 3 y 9 dígitos (total 10 dígitos). Los móviles colombianos siempre empiezan con 3.

**Métodos `mensajeErrorEmail()` y `mensajeErrorTelefono()`:** Retornan el mensaje de error específico o `null` si es válido. **¿Por qué no excepciones?** Porque la UI necesita mostrar el mensaje exacto al lado del campo. Una excepción solo diría "inválido", sin indicar qué está mal.

### `InasistenciaService` — `service/InasistenciaService.java`

```java
public final class InasistenciaService {
    private static final InasistenciaService INSTANCE = new InasistenciaService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "inasistencias-detector");
        t.setDaemon(true);
        return t;
    });
}
```

**¿Por qué Singleton (`getInstance()`)?** Solo debe haber UN detector de inasistencias ejecutándose. Si cada controller creara uno, habría múltiples schedulers corriendo en paralelo.

**`ScheduledExecutorService` con `scheduleWithFixedDelay()`:** Ejecuta `autoDetectarInasistencias()` cada 5 minutos, con un delay de 0 al inicio. **¿Por qué `scheduleWithFixedDelay` y no `scheduleAtFixedRate`?** Porque si la ejecución se demora (muchas citas vencidas), `scheduleAtFixedRate` apilaría ejecuciones. `scheduleWithFixedDelay` espera 5 min después de que termina la anterior.

**`setDaemon(true)`:** El hilo se marca como daemon, lo que significa que la JVM puede terminar aunque este hilo siga ejecutándose. No bloquearía el cierre de la aplicación.

**¿Por qué no `Timer`/`TimerTask`?** `ScheduledExecutorService` es más moderno, maneja mejor excepciones y permite `shutdown()` controlado.

---

## 8. Capa de Acceso a Datos

### `DatabaseConnection` — `dao/DatabaseConnection.java`

```java
public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:citasmedicas.db";
    private static boolean initialized;
}
```

**`initialize()` sincronizado:** Garantiza que la creación de tablas y datos demo ocurra una sola vez, incluso si múltiples hilos llaman a `getConnection()` simultáneamente.

**`PRAGMA foreign_keys = ON`:** Habilita restricciones de clave foránea en SQLite (por defecto están desactivadas). Sin esto, los `FOREIGN KEY` en DDL serían ignorados.

**`crearTablas()`:** 7 tablas con todas las claves foráneas y constraints. Incluye alteraciones post-creación (para columnas agregadas después de la creación inicial) envueltas en try-catch — si la columna ya existe, el ALTER falla pero se ignora.

**`insertarDatosDemo()`:** Población inicial con 6 pacientes, 5 médicos, 1 admin, 13 citas, 3 bloqueos y 19 registros de agenda. Los `INSERT OR IGNORE` permiten ejecutar múltiples veces sin duplicar datos.

**`ejecutarInsert(Connection, String, String...)`:** Método auxiliar que usa varargs para simplificar inserts con PreparedStatement. Toma cualquier número de parámetros y los bindea secuencialmente. **¿Por qué varargs en vez de arrays?** Porque en el código llamante se ve más limpio: `ejecutarInsert(conn, sql, "a", "b", "c")` vs `ejecutarInsert(conn, sql, new String[]{"a", "b", "c"})`.

### `UsuarioDAO` — `dao/UsuarioDAO.java`

**`autenticar(String email, String password)`:** LEFT JOIN entre `usuarios`, `pacientes`, `medicos` y `administradores` en una sola query. **¿Por no tres consultas separadas?** Porque no sabes qué tipo de usuario es hasta que lees el `rol`. Una sola consulta es más eficiente: viaja una vez a la BD.

**`mapearUsuario(ResultSet)`:** Usa `switch` con pattern matching (Java 17):
```java
return switch (rol) {
    case PACIENTE -> new Paciente(...);
    case MEDICO   -> new Medico(...);
    case ADMIN    -> new Administrador(...);
};
```
Este switch es **exhaustivo**: el compilador verifica que todos los valores de `Rol` están cubiertos. Si alguien agrega un nuevo rol, este código no compila hasta que se agregue el caso.

**`guardarPaciente()`, `guardarMedico()`, `guardarAdministrador()`:** Todos siguen el mismo patrón: insert en `usuarios` + insert en la tabla específica, dentro de una transacción (`setAutoCommit(false)` + `commit()`). **¿Por qué transacción?** Si el segundo insert falla, el primero queda huérfano. Con transacción, ambos se revierten.

### `CitaDAO` — `dao/CitaDAO.java`

**`consultaBase()`:** Retorna el JOIN base de 5 tablas (`citas JOIN pacientes JOIN usuarios JOIN medicos JOIN usuarios`). Todos los métodos `obtener*()` usan esta base y agregan WHERE + ORDER BY. **¿Por qué no un PreparedStatement constante?** Porque el WHERE varía según el método. `consultaBase()` evita duplicar el JOIN gigante 8 veces.

**`PreparedStatementBinder` (interfaz funcional):**
```java
@FunctionalInterface
private interface PreparedStatementBinder {
    void bind(PreparedStatement statement) throws SQLException;
}
```
**¿Por qué una interfaz funcional personalizada y no `Consumer<PreparedStatement>`?** Porque `Consumer` no declara `throws SQLException`. Al usar `@FunctionalInterface`, los métodos pueden lanzar `SQLException` sin try-catch interno. Se usa con lambdas:

```java
obtenerPorFiltro(sql, statement -> {
    statement.setString(1, medicoId);
    statement.setString(2, fecha.toString());
    statement.setString(3, EstadoCita.CANCELADA.name());
});
```

**Métodos de consulta:**
- `obtenerPorPacienteActivas()`: Excluye `CANCELADA` — el paciente solo ve citas que aún importan.
- `obtenerAgendaMedico()`: Solo `PENDIENTE` y `CONFIRMADA` — lo que el médico necesita atender hoy.
- `obtenerActivasPorMedicoYFecha()`: Excluye `CANCELADA` — las canceladas no ocupan slot.
- `obtenerCitasVencidasActivas()`: `fecha < hoy OR (fecha = hoy AND hora_inicio < ahora)` — detecta las que ya debieron ocurrir.
- `existeCitaActivaPorPacienteYEspecialidad()`: Previene doble agendamiento en misma especialidad.

### `PacienteDAO` — `dao/PacienteDAO.java`

**`guardar(Paciente)`:** Delega a `UsuarioDAO.guardarPaciente()`. **¿Por qué no duplicar el código?** Para mantener un único punto de guardado. Si cambia la lógica de la tabla `usuarios`, solo se modifica `UsuarioDAO`.

**`obtenerTodos()`:** JOIN entre `usuarios` y `pacientes`, filtrando `activo = 1`. Ordena por apellido y nombre.

**`desactivar(String)`:** UPDATE `activo = 0` — borrado lógico. **¿Por qué no DELETE?** Por integridad referencial: las citas del paciente referencian su ID. Además, borrado lógico permite "reactivar" pacientes sin perder datos.

### `MedicoDAO` — `dao/MedicoDAO.java`

Similar a `PacienteDAO`, con la misma lógica de guardado en dos tablas con transacción. Incluye `buscarPorId()`, `obtenerTodos()`, `actualizar()` y `eliminar()` (borrado lógico).

### `AdministradorDAO` — `dao/AdministradorDAO.java`

El más simple — solo `guardar()` y `buscarPorId()`. Los administradores normalmente son creados desde la BD y no desde la UI.

### `AgendaMedicaDAO` — `dao/AgendaMedicaDAO.java`

CRUD completo para la agenda de médicos. `obtenerPorMedicoYDia()` es usado por `DisponibilidadService` para determinar qué slots están disponibles.

### `BloqueoAgendaDAO` — `dao/BloqueoAgendaDAO.java`

`obtenerPorMedicoYFecha()` es usado por `DisponibilidadService` para filtrar slots bloqueados. `eliminar()` permite remover bloqueos.

### `HistorialClinicoDAO` — `dao/HistorialClinicoDAO.java`

`buscarPorCitaId()`: Cada cita completada tiene un registro de historial. `obtenerPorPacienteId()` y `obtenerPorMedicoId()` listan el historial completo ordenado por fecha descendente.

---

## 9. Capa de Presentación

### `ViewManager` — `view/ViewManager.java`

```java
public class ViewManager {
    private static Stage primaryStage;

    public static void showView(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        // Ajustar tamaño preferido
        double pw = root.prefWidth(-1);
        double ph = root.prefHeight(-1);
        if (pw > 0) primaryStage.setWidth(pw);
        if (ph > 0) primaryStage.setHeight(ph);
        primaryStage.show();
    }
}
```

**`prefWidth(-1)` / `prefHeight(-1)`:** JavaFX permite que el layout calcule su tamaño preferido basado en su contenido. El -1 indica "usa el valor computado". Esto evita ventanas con tamaños fijos y permite que cada vista tenga su tamaño natural.

**¿Por qué estático?** Igual que `Session`, los métodos son estáticos porque los controllers se crean via FXMLLoader y no reciben dependencias por constructor.

### Controladores (controller/)

| Archivo | Líneas | Propósito |
|---|---|---|
| `LoginController.java` | 98 | Autenticación, redirección por rol |
| `RegistroController.java` | 352 | Registro de pacientes con validación |
| `DashboardPacienteController.java` | 586 | UI del paciente |
| `DashboardMedicoController.java` | 951 | UI del médico con calendario CalendarFX |
| `DashboardAdminController.java` | 1394 | UI del administrador |

**Anotaciones comunes:**
- `@FXML` en campos: Inyecta los componentes del FXML (Button, TableView, ChoiceBox, etc.)
- `@FXML` en métodos: Los vincula como event handlers del FXML
- `@Override` en `initialize()`: Método del ciclo de vida de JavaFX, se ejecuta automáticamente después de que el FXML carga todos los componentes

---

## 10. Patrones de Diseño

| Patrón | ¿Dónde? | ¿Por qué? |
|---|---|---|
| **Template Method** | `Usuario.getMenuOpciones()` abstracto, implementado por subclases | Cada rol tiene su propio menú; el esqueleto (llamar al método) es el mismo |
| **Singleton** | `Session`, `InasistenciaService` | Estado global (usuario logueado) y servicio único (detector de inasistencias) no deben tener múltiples instancias |
| **DAO (Data Access Object)** | Todo el paquete `dao/` | Separa la lógica de persistencia (SQL) de la lógica de negocio (service). Si migras a MySQL, solo cambias los DAOs |
| **MVC (Model-View-Controller)** | `model/` ↔ `fxml/` ↔ `controller/` | Separación de responsabilidades: modelos son datos puros, FXML es la vista declarativa, controllers manejan eventos |
| **State (Máquina de estados)** | `Cita.confirmar()`, `cancelar()`, `completar()` | Cada método solo permite transiciones válidas; el estado actual determina qué transiciones son posibles |
| **Strategy** | `ValidacionService` (validación de email con regex + DNS) | La estrategia de validación combina dos técnicas (formato + verificación MX) de forma componible |
| **Interfaz Funcional** | `CitaDAO.PreparedStatementBinder` | Permite pasar lógica de binding como lambda, evitando duplicar el try-with-resources en cada método de consulta |
| **LIFO (Stack)** | `Paciente.historialCitas` | Última cita es la más relevante, acceso O(1) |
| **FIFO (Queue)** | `Medico.agendaDelDia` | Orden de llegada, primer paciente es el primero en atenderse |
| **Scheduler (Programador)** | `InasistenciaService` con `ScheduledExecutorService` | Ejecución periódica sin bloqueo del hilo de UI |

---

## 11. Características de Java Utilizadas

### Java 17 (LTS)

| Característica | ¿Dónde se usa? | ¿Por qué es relevante? |
|---|---|---|
| **Text Blocks** (`"""..."""`) | En todos los DAOs para queries SQL y en `DatabaseConnection.crearTablas()` | SQL multilineal legible sin concatenación de strings. Antes de Java 13, las queries de 10+ líneas eran ilegibles. |
| **Switch Expressions** (`switch (x) { case A -> ... }`) | `UsuarioDAO.mapearUsuario()` | Switch exhaustivo sin `break`, más seguro y conciso. No hay riesgo de "fall-through" accidental. |
| **Pattern Matching (Switch)** | `UsuarioDAO.mapearUsuario()` | `switch (rol)` directamente sobre un enum. El compilador verifica que todos los casos están cubiertos. |
| **`LocalDate`, `LocalTime`** | `Cita`, `BloqueoAgenda`, `AgendaMedica`, `DisponibilidadService` | API de fecha/hora inmutable y thread-safe de Java 8+. `LocalDate.parse()` y `LocalTime.parse()` evitan SimpleDateFormat (no thread-safe). |
| **`record`** (no usado) | No se usa explícitamente | Los modelos usan clases tradicionales por necesidad de setters (mapeo desde ResultSet). Los records son inmutables y no tendrían setters. |
| **`@FunctionalInterface`** | `CitaDAO.PreparedStatementBinder` | Marca una interfaz con un solo método abstracto, permitiendo uso como lambda. |
| **`java.util.UUID`** | `Usuario.generarIdUnico()` | IDs únicos universales de 128 bits. Truncados a 8 caracteres para legibilidad. |
| **`java.util.concurrent`** | `InasistenciaService` | `ScheduledExecutorService` para tareas periódicas. Más moderno y robusto que `Timer`/`TimerTask`. |
| **`javax.naming.directory`** | `ValidacionService.dominioTieneMX()` | Consulta DNS vía JNDI para verificar existencia de registros MX en el dominio del email. |
| **JPMS (module-info.java)** | `module-info.java` | Sistema de módulos de Java 9+. Declara dependencias, exporta paquetes, abre paquetes a JavaFX para reflexión. |
| **Try-with-resources** | En todos los DAOs (`try (Connection conn = ...; PreparedStatement stmt = ...)`) | Cierre automático de recursos JDBC. Garantiza que Connection, Statement y ResultSet se cierren incluso si hay excepción. |
| **Varargs** | `DatabaseConnection.ejecutarInsert(Connection, String, String...)` | Sintaxis limpia para pasar múltiples parámetros a PreparedStatement sin crear un array explícito. |
| **`isBlank()`** (Java 11+) | `CitaService.validarCita()`, `PacienteService.actualizarDatos()` | Más completo que `trim().isEmpty()`: también detecta strings con solo espacios Unicode. |
| **`@Override`** | En todos los controladores, modelos, DAOs | Marca explícitamente métodos que sobrescriben de superclase o interfaz. El compilador da error si no sobrescribe nada, previniendo errores tipográficos. |

### Anotaciones

| Anotación | ¿Dónde? | Propósito |
|---|---|---|
| `@FXML` | Campos y métodos en controllers | Inyección de componentes FXML y binding de event handlers |
| `@Override` | En toda la jerarquía de clases | Sobrescritura verificada por compilador |
| `@SuppressWarnings("rawtypes", "unchecked")` | Dashboards | TableCell genéricos sin tipo específico; necesario por limitaciones de JavaFX TableView con genéricos |
| `@FunctionalInterface` | `CitaDAO.PreparedStatementBinder` | Permite uso de lambda y da error de compilación si la interfaz tiene más de un método abstracto |

### Organización de Paquetes

```
co.edu.upc.citasmedicas
├── controller   → Lógica de UI (eventos, binding, actualización de componentes)
├── dao          → SQL y mapeo ResultSet → Modelos
├── enums        → Constantes del dominio con tipo seguro
├── model        → Clases de datos (POJOs con lógica de validación interna)
├── service      → Reglas de negocio, orquestación de DAOs
└── view         → Gestión de cambios de vista (ViewManager)
```

---

## Apéndice: Flujo de Agendamiento de Cita (Ejemplo Completo)

1. Usuario (Paciente) hace clic en "Solicitar cita" en el Dashboard
2. `DashboardPacienteController.manejarSolicitud()` procesa el evento `@FXML`
3. Controller construye objeto `Cita` con datos de la UI (medico, servicio, fecha, hora)
4. Llama a `CitaService.agendarCita(cita)`
5. Service valida todos los campos (nulos, blancos)
6. Service verifica que el paciente no tenga otra cita activa en la misma especialidad (`CitaDAO.existeCitaActivaPorPacienteYEspecialidad()`)
7. Service consulta disponibilidad (`DisponibilidadService.obtenerHorasDisponibles()`):
   - Obtiene agenda del médico para ese día de la semana (`AgendaMedicaDAO.obtenerPorMedicoYDia()`)
   - Genera slots candidatos (08:00, 08:20, 08:40, ...)
   - Filtra contra citas ocupadas (`CitaDAO.obtenerActivasPorMedicoYFecha()`)
   - Filtra contra bloqueos (`BloqueoAgendaDAO.obtenerPorMedicoYFecha()`)
8. Si la hora está disponible, guarda (`CitaDAO.guardar()`) con estado `PENDIENTE`
9. Service retorna. Controller muestra mensaje de éxito.

---

## 12. Banco de Preguntas y Respuestas para Defensa

> Esta sección recopila las preguntas críticas que pueden surgir en una sustentación,
> organizadas por tema. Las respuestas se basan **directamente en el código fuente**
> y explican el "por qué" de cada decisión de diseño.

---

### 12.1 Arquitectura y Diseño

**P1: ¿Por qué dividieron el proyecto en capas (controller, service, dao, model) y no todo en un solo paquete?**

La separación en capas sigue el principio de **responsabilidad única**. Cada capa tiene una responsabilidad bien definida y no se mezcla con otras:
- **model**: solo datos y lógica de negocio atómica (ej: `Cita.confirmar()` valida transición de estado).
- **dao**: solo SQL y mapeo `ResultSet → objeto`. Si mañana migramos a MySQL, solo tocamos este paquete.
- **service**: orquestación y reglas de negocio compuestas (ej: `CitaService.agendarCita()` valida duplicados, disponibilidad, y persiste).
- **controller**: solo eventos de UI. No tiene SQL ni lógica de negocio.
- **view**: navegación entre pantallas.

Esto permite **testear la lógica de negocio sin cargar la UI ni la BD real**, y **cambiar la UI (FXML) sin tocar una línea de servicio**.

---

**P2: ¿Por qué no usaron Spring Boot o inyección de dependencias?**

Es una aplicación de **escritorio monolítica**, no un microservicio. Spring Boot agregaría complejidad innecesaria (IoC container, proxies, configuración XML/anotaciones) para un proyecto donde los DAOs se instancian directamente (`new PacienteDAO()`) y los servicios son estáticos o instanciados manualmente. La simplicidad de `new` + métodos estáticos es suficiente para este alcance.

---

**P3: ¿Por qué `Session` y `ViewManager` son clases con métodos estáticos (singleton de facto) y no instancias?**

Los controllers de JavaFX son creados por `FXMLLoader` usando el constructor sin parámetros — **no podemos pasarles dependencias por constructor**. Tener `Session` como clase estática permite que cualquier controller acceda al usuario logueado sin necesidad de un contenedor de DI. `ViewManager` sigue la misma lógica: el `Stage` principal se guarda una vez al iniciar y todos los controllers lo usan para cambiar de escena.

---

**P4: ¿Por qué existe `Launcher.java` si `Main.java` ya tiene el `main`?**

NetBeans (IDE usado en el proyecto) a veces ejecuta con `exec-maven-plugin` en vez de `javafx-maven-plugin`. `Launcher` es un **puente**: llama a `Main.main()` que a su vez llama a `Application.launch()`. Así funciona tanto desde Maven (`mvn javafx:run`) como desde NetBeans (F6).

---

**P5: ¿Por qué `Usuario` es abstracta y no una interfaz?**

`Usuario` tiene **estado** (id, nombre, email, password, etc.) y **comportamiento concreto** (`login()`, `toString()`). Una interfaz no puede tener estado ni métodos concretos (sin `default`). Usar clase abstracta permite:
- Compartir atributos y getters/setters entre `Paciente`, `Medico` y `Administrador`.
- Definir un método abstracto `getMenuOpciones()` que **fuerza** a cada subclase a implementar su propio menú.
- Tener el método `login()` con implementación base que compara email y password.

---

**P6: ¿Dónde se aplica el polimorfismo en el proyecto?**

En varios lugares:
1. **`getMenuOpciones()`**: cada subclase (`Paciente`, `Medico`, `Administrador`) implementa su propia versión. El controller puede llamar a `usuario.getMenuOpciones()` sin saber qué tipo concreto es.
2. **`UsuarioDAO.mapearUsuario()`**: el `switch` sobre `Rol` devuelve un `Paciente`, `Medico` o `Administrador`, pero el método retorna `Usuario`. El llamador (`autenticar()`) recibe un `Usuario` y JVM resuelve el método correcto en tiempo de ejecución.
3. **`toString()`**: sobrescrito en cada subclase, se invoca polimórficamente al mostrar en la UI.

---

**P7: ¿Por qué usaron `Stack` en `Paciente.historialCitas` y `Queue` en `Medico.agendaDelDia`?**

Son estructuras de datos que reflejan casos de uso opuestos:
- **Paciente → Stack (LIFO)**: el paciente necesita ver la **última** cita (la más reciente). `peek()` la obtiene en O(1). Tiene sentido clínico: lo último que ocurrió es lo más relevante.
- **Médico → Queue (FIFO)**: el médico atiende en **orden de llegada**. `poll()` saca al primero que llegó. Refleja una sala de espera real.

Cada estructura expresa **intencionalmente** el comportamiento esperado, más allá de solo "guardar datos".

---

### 12.2 Persistencia y Datos

**P8: ¿Por qué SQLite y no MySQL / PostgreSQL?**

SQLite es una base de datos **embebida**: no requiere instalación de servidor, configuración ni credenciales. La base es un solo archivo (`citasmedicas.db`) que se crea automáticamente al ejecutar la app. Para una aplicación de escritorio académica, esto elimina toda la fricción de infraestructura. MySQL requeriría instalar y configurar un servidor aparte.

---

**P9: ¿Dónde se guarda físicamente el archivo `citasmedicas.db`?**

En el directorio raíz del proyecto (donde está el `pom.xml`). La URL está hardcodeada en `DatabaseConnection.java`:
```java
private static final String URL = "jdbc:sqlite:citasmedicas.db";
```
Si el archivo no existe, SQLite lo crea automáticamente al abrir la conexión.

---

**P10: ¿Por qué usan transacciones (`setAutoCommit(false)` + `commit()`) en los DAOs?**

Porque un `Paciente`, `Medico` o `Administrador` se guarda en **dos tablas**: `usuarios` (datos comunes) y la tabla específica (`pacientes`, `medicos`, `administradores`). Si el segundo INSERT falla, el primer registro quedaría **huérfano**. La transacción garantiza que **ambos se insertan o ninguno**:
```java
connection.setAutoCommit(false);
// INSERT en usuarios
// INSERT en pacientes
connection.commit();  // solo aquí se persisten ambos
```
Si ocurre una excepción antes del commit, `connection.close()` (vía try-with-resources) hace rollback automático.

---

**P11: ¿Por qué LEFT JOIN en `autenticar()` y no tres consultas separadas?**

No sabes qué tipo de usuario es hasta que lees el campo `rol`. Hacer tres consultas separadas (una a `pacientes`, otra a `medicos`, otra a `administradores`) sería ineficiente: viajarías tres veces a la BD. Un solo LEFT JOIN con todas las tablas trae toda la información en una ida y vuelta. Además, SQLite no tiene penalización significativa por JOIN en tablas pequeñas.

---

**P12: ¿Por qué borrado lógico (`activo = 0`) y no DELETE físico?**

Dos razones:
1. **Integridad referencial**: las citas de un paciente referencian su `usuario_id`. Si haces DELETE, las citas quedarían huérfanas o violarían la FK.
2. **Historial**: desactivar un paciente no elimina su historial de citas. Se puede reactivar después sin perder datos.

---

**P13: ¿Por qué IDs alfanuméricos (UUID truncado a 8 caracteres) y no auto-increment?**

`UUID.randomUUID()` genera identificadores únicos de 128 bits con probabilidad de colisión insignificante. Truncar a 8 caracteres (tomando los primeros caracteres hexadecimales) mantiene unicidad suficiente para el alcance del proyecto. Ventajas sobre auto-increment:
- Se generan en **código**, no dependen de la BD.
- Son **portables**: si migras a otra BD, los IDs no cambian.
- Son **legibles** en depuración (`"pac-001"`, `"med-003"`) vs números opacos.

---

### 12.3 Validaciones y Casos de Borde

**P14: ¿Qué impide que un paciente agende una cita en un horario ya ocupado por otro paciente del mismo médico?**

El flujo completo es:

1. **`DisponibilidadService.obtenerHorasDisponibles()`** genera slots candidatos basados en la agenda del médico (`AgendaMedicaDAO.obtenerPorMedicoYDia()`).
2. Filtra esos slots contra **citas ocupadas** (`CitaDAO.obtenerActivasPorMedicoYFecha()`) y **bloqueos** (`BloqueoAgendaDAO.obtenerPorMedicoYFecha()`).
3. El solapamiento se detecta con lógica de intervalos:
   ```java
   // Dos intervalos [A, B) y [C, D) se solapan si A < D y C < B
   inicio.isBefore(cFin) && cInicio.isBefore(fin)
   ```
4. Los slots disponibles se muestran en un `ComboBox`. El usuario **solo puede elegir horas que el sistema ya filtró como disponibles**.
5. Al confirmar, `CitaService.agendarCita()` hace una validación final llamando nuevamente a `DisponibilidadService`.

Si dos pacientes intentaran agendar el mismo slot desde dos terminales distintas, el segundo recibiría error porque `CitaDAO` ya registró la primera cita. En un entorno single-user (app de escritorio), esto no ocurre.

---

**P15: ¿Qué pasa si el usuario escribe letras en el campo de número de documento?**

El sistema no bloquea la escritura en tiempo real (no usa `TextFormatter`), pero al enviar el formulario **valida el contenido**:
```java
public static String mensajeErrorNumeroDocumento(String doc) {
    if (doc == null || doc.isBlank()) return null;
    if (!SOLO_DIGITOS.matcher(doc.trim()).matches())
        return "Solo se permiten numeros";
    return null;
}
```
Si el regex `^\d+$` no coincide (porque hay letras), retorna un mensaje de error, el controller lo muestra en el `Label` de error y **no se construye el objeto**. El usuario debe corregir antes de continuar.

---

**P16: ¿Qué pasa si el usuario escribe números en el nombre o apellido?**

Misma lógica que el documento, pero con regex inverso: `^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$`. Solo permite letras (incluyendo tildes y ñ) y espacios. Cualquier dígito o carácter especial es rechazado.

---

**P17: ¿Qué pasa si el usuario ingresa un email con dominio inexistente?**

`ValidacionService.dominioTieneMX()` consulta los registros **MX** (Mail Exchange) del dominio mediante JNDI/DNS. Si el dominio no tiene MX, significa que no puede recibir correos, y el registro se rechaza. Ejemplo: `usuario@dominiofalso.com` sería rechazado porque ese dominio no tiene servidores de correo configurados.

---

**P18: ¿Cómo evitan que un paciente tenga dos citas activas en la misma especialidad?**

Antes de guardar, `CitaService.agendarCita()` llama a `CitaDAO.existeCitaActivaPorPacienteYEspecialidad()`. Esta consulta cuenta cuántas citas del paciente en esa especialidad están en estado `PENDIENTE` o `CONFIRMADA`. Si el conteo es > 0, lanza `IllegalStateException` con el mensaje "Ya tienes una cita activa en esta especialidad".

---

**P19: ¿Qué pasa si el médico ya tiene una cita a las 9:00 con duración 30 min y alguien intenta agendar a las 9:10?**

El algoritmo de `DisponibilidadService` usa **solapamiento de intervalos**, no solo comparación de horas exactas.
- Cita existente: [09:00, 09:30)
- Intento: [09:10, 09:40)
- ¿Se solapan? `09:10 < 09:30` y `09:00 < 09:40` → **Sí, se solapan** → el slot no está disponible.

Esto funciona para **cualquier duración**, no solo para slots exactos de la agenda. Una cita de 45 min ocupará 1.5 slots de 30 min sin problema.

---

**P20: ¿Qué pasa si el DatePicker de fecha de nacimiento queda vacío en un modify dialog?**

Antes de la corrección, `PacienteDAO.actualizarTodo()` llamaba a `fechaNacimiento.toString()` sin null check, lanzando `NullPointerException`. Se corrigió agregando null check tanto en el DAO como en el mapeo desde ResultSet:
```java
stmtPaciente.setString(3, paciente.getFechaNacimiento() != null
    ? paciente.getFechaNacimiento().toString() : null);
```

---

**P21: ¿Qué pasa si el administrador escribe "CCCC" en el campo de tipo de documento?**

Originalmente `tipoDoc` era un `TextField` libre en los modify dialogs. Se corrigió cambiándolo a `ComboBox<String>` con opciones fijas: `"CC"`, `"TI"`, `"CE"`, `"Pasaporte"`. El usuario ya no puede escribir valores inválidos — solo selecciona entre las opciones predefinidas.

---

### 12.4 Programación Orientada a Objetos

**P22: ¿Dónde están los cuatro pilares de la POO en el proyecto?**

| Pilar | ¿Dónde? | Ejemplo concreto |
|---|---|---|
| **Herencia** | `Usuario` → `Paciente`, `Medico`, `Administrador` | Las 3 subclases heredan id, nombre, email, password, etc. |
| **Polimorfismo** | `getMenuOpciones()` | Cada rol implementa su propio menú. El controller llama al método sin saber el tipo concreto. |
| **Encapsulamiento** | Todos los modelos | Atributos `private`, acceso vía getters/setters. La lógica interna de `Cita.setHoraInicio()` recalcula `horaFin` automáticamente. |
| **Abstracción** | `Usuario` (abstracta) | Define el contrato `getMenuOpciones()` sin implementarlo. Las subclases proveen la implementación concreta. |

---

**P23: ¿Por qué no usaron `record` de Java para los modelos?**

Los `record` son inmutables (todos los campos son `final`). Los modelos necesitan **setters** porque el mapeo desde `ResultSet` (JDBC) requiere construir el objeto y luego asignar campos, o usar constructores enormes. Además, `Stack<Cita>` y `Queue<Cita>` necesitan mutabilidad para agregar elementos después de la construcción. Los records no soportan ninguna de las dos cosas.

---

**P24: ¿Por qué una interfaz funcional personalizada (`PreparedStatementBinder`) y no `Consumer<PreparedStatement>`?**

`Consumer<PreparedStatement>` no declara `throws SQLException`, por lo que las lambdas tendrían que atrapar la excepción internamente. Al definir:
```java
@FunctionalInterface
private interface PreparedStatementBinder {
    void bind(PreparedStatement statement) throws SQLException;
}
```
las lambdas pueden lanzar `SQLException` sin try-catch, y el método que las recibe la maneja centralizadamente.

---

**P25: ¿Dónde se usa el `switch` con pattern matching de Java 17 y por qué es más seguro?**

En `UsuarioDAO.mapearUsuario()`:
```java
return switch (rol) {
    case PACIENTE -> new Paciente(...);
    case MEDICO   -> new Medico(...);
    case ADMIN    -> new Administrador(...);
};
```
Es un **switch exhaustivo**: el compilador verifica que todos los valores del enum `Rol` están cubiertos. Si alguien agrega un nuevo rol (ej: `RECEPCIONISTA`), este código **no compila** hasta que se agregue el caso correspondiente. Esto previene errores en tiempo de compilación, no en ejecución.

---

### 12.5 Escenarios de Falla y Robustez

**P26: ¿Qué pasa si la base de datos se corrompe o el archivo desaparece?**

`DatabaseConnection.initialize()` está envuelto en un bloque `try-catch`. Si `DriverManager.getConnection()` falla (archivo corrupto, permisos, etc.), lanza `SQLException`. En `LoginController.initialize()`:
```java
try {
    DatabaseConnection.initialize();
} catch (SQLException exception) {
    errorLabel.setText("No se pudo inicializar SQLite");
}
```
La aplicación **no crashea**. Muestra un mensaje de error en la UI y el usuario puede reintentar. No hay pérdida de datos porque la BD demo se recrea desde cero al eliminar el archivo.

---

**P27: ¿Qué pasa si se cierra la aplicación mientras se está guardando un paciente (a mitad del INSERT)?**

La transacción en `UsuarioDAO.guardarPaciente()` usa `connection.setAutoCommit(false)` y `connection.commit()`. Si la JVM se cierra antes del `commit()`, SQLite revierte automáticamente los cambios pendientes al cerrar la conexión. El paciente no queda a medio registrar.

---

**P28: ¿Qué pasa si la consulta DNS para validar el email es lenta?**

La validación DNS (`ValidacionService.dominioTieneMX()`) se ejecuta en el **hilo de JavaFX** (el mismo de la UI), porque se dispara desde un listener de `focusedProperty`:
```java
emailField.focusedProperty().addListener((obs, viejo, nuevo) -> {
    if (!nuevo) validarEmailCampo();
});
```
Si el DNS tarda 3 segundos, la UI se congela 3 segundos. **Es una limitación conocida**. La solución ideal sería ejecutar la consulta DNS en un hilo separado (`Task` o `CompletableFuture`) y actualizar la UI cuando termine, pero eso escapa al alcance académico del proyecto.

---

**P29: ¿Qué pasa si un administrador elimina un médico que tiene citas futuras?**

`MedicoDAO.eliminar()` hace UPDATE `activo = 0` (borrado lógico). Las citas futuras del médico **quedan huérfanas**: siguen en la BD con estado `PENDIENTE`/`CONFIRMADA`, referenciando a un médico inactivo. El sistema actualmente **no cancela automáticamente** esas citas. Es una limitación que requeriría notificar a los pacientes y cancelar las citas asociadas.

---

**P30: ¿Qué pasa si un paciente y un administrador intentan modificar el mismo dato simultáneamente?**

En una aplicación de escritorio **single-user** (una instancia por PC), esto no ocurre. No hay concurrencia real porque JavaFX es single-thread. Si en el futuro se migrara a una arquitectura cliente-servidor, habría que implementar control de concurrencia con timestamps o versionado.

---

### 12.6 Resumen de Correcciones Aplicadas

Durante el desarrollo se identificaron y corrigieron los siguientes problemas:

| # | Problema | Archivos afectados | Solución |
|---|---|---|---|
| 1 | `NullPointerException` en `PacienteDAO.actualizarTodo()` si `fechaNacimiento` es null | `PacienteDAO.java`, `UsuarioDAO.java` | Null check antes de `.toString()` |
| 2 | `tipoDoc` como `TextField` libre en modify dialogs (inconsistente con el registro) | `DashboardPacienteController`, `DashboardMedicoController`, `DashboardAdminController` | Cambiado a `ComboBox<String>` con opciones fijas |
| 3 | Sin validación de email en modify dialogs | `DashboardPacienteController`, `DashboardMedicoController`, `DashboardAdminController` | Agregada llamada a `ValidacionService.mensajeErrorEmail()` |
| 4 | Sin validación de formato para nombre, apellido, documento, EPS | `ValidacionService.java` + todos los controllers | Nuevos métodos `mensajeErrorNombre()`, `mensajeErrorNumeroDocumento()`, `mensajeErrorEps()` |

---

*Documento generado a partir del análisis completo del código fuente.*  
*Última actualización: Junio 2026*
