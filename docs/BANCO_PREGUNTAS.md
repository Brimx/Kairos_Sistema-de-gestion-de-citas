# Banco de Preguntas para Defensa — Kairos

> Documento de estudio con las 30 preguntas y respuestas completas.
> Basado directamente en el código fuente del proyecto.

---

## 12.1 Arquitectura y Diseño (P1–P7)

### P1: ¿Por qué dividieron el proyecto en capas (controller, service, dao, model) y no todo en un solo paquete?

Sigue el principio de responsabilidad única. Cada capa tiene una responsabilidad bien definida y no se mezcla con otras:

- **model**: solo datos y lógica de negocio atómica (ej: `Cita.confirmar()` valida transición de estado)
- **dao**: solo SQL y mapeo `ResultSet → objeto`. Si mañana migramos a MySQL, solo tocamos este paquete
- **service**: orquestación y reglas de negocio compuestas (ej: `CitaService.agendarCita()` valida duplicados, disponibilidad, y persiste)
- **controller**: solo eventos de UI. No tiene SQL ni lógica de negocio
- **view**: navegación entre pantallas

Esto permite testear la lógica de negocio sin cargar la UI ni la BD real, y cambiar la UI (FXML) sin tocar una línea de servicio.

---

### P2: ¿Por qué no usaron Spring Boot o inyección de dependencias?

Es una aplicación de escritorio monolítica, no un microservicio. Spring Boot agregaría complejidad innecesaria (IoC container, proxies, configuración XML/anotaciones) para un proyecto donde los DAOs se instancian directamente (`new PacienteDAO()`) y los servicios son estáticos o instanciados manualmente. La simplicidad de `new` + métodos estáticos es suficiente para este alcance.

---

### P3: ¿Por qué `Session` y `ViewManager` son clases con métodos estáticos (singleton de facto) y no instancias?

Los controllers de JavaFX son creados por `FXMLLoader` usando el constructor sin parámetros — no podemos pasarles dependencias por constructor. Tener `Session` como clase estática permite que cualquier controller acceda al usuario logueado sin necesidad de un contenedor de DI. `ViewManager` sigue la misma lógica: el `Stage` principal se guarda una vez al iniciar y todos los controllers lo usan para cambiar de escena.

---

### P4: ¿Por qué existe `Launcher.java` si `Main.java` ya tiene el `main`?

NetBeans (IDE usado en el proyecto) a veces ejecuta con `exec-maven-plugin` en vez de `javafx-maven-plugin`. `Launcher` es un puente: llama a `Main.main()` que a su vez llama a `Application.launch()`. Así funciona tanto desde Maven (`mvn javafx:run`) como desde NetBeans (F6).

---

### P5: ¿Por qué `Usuario` es abstracta y no una interfaz?

`Usuario` tiene estado (id, nombre, email, password, etc.) y comportamiento concreto (`login()`, `toString()`). Una interfaz no puede tener estado ni métodos concretos (sin `default`). Usar clase abstracta permite:

- Compartir atributos y getters/setters entre `Paciente`, `Medico` y `Administrador`
- Definir un método abstracto `getMenuOpciones()` que fuerza a cada subclase a implementar su propio menú
- Tener el método `login()` con implementación base que compara email y password

---

### P6: ¿Dónde se aplica el polimorfismo en el proyecto?

En varios lugares:

1. **`getMenuOpciones()`**: cada subclase (`Paciente`, `Medico`, `Administrador`) implementa su propia versión. El controller puede llamar a `usuario.getMenuOpciones()` sin saber qué tipo concreto es.
2. **`UsuarioDAO.mapearUsuario()`**: el `switch` sobre `Rol` devuelve un `Paciente`, `Medico` o `Administrador`, pero el método retorna `Usuario`. El llamador (`autenticar()`) recibe un `Usuario` y JVM resuelve el método correcto en tiempo de ejecución.
3. **`toString()`**: sobrescrito en cada subclase, se invoca polimórficamente al mostrar en la UI.

---

### P7: ¿Por qué usaron `Stack` en `Paciente.historialCitas` y `Queue` en `Medico.agendaDelDia`?

Son estructuras de datos que reflejan casos de uso opuestos:

- **Paciente → Stack (LIFO)**: el paciente necesita ver la última cita (la más reciente). `peek()` la obtiene en O(1). Tiene sentido clínico: lo último que ocurrió es lo más relevante.
- **Médico → Queue (FIFO)**: el médico atiende en orden de llegada. `poll()` saca al primero que llegó. Refleja una sala de espera real.

Cada estructura expresa intencionalmente el comportamiento esperado, más allá de solo "guardar datos".

---

## 12.2 Persistencia y Datos (P8–P13)

### P8: ¿Por qué SQLite y no MySQL / PostgreSQL?

SQLite es una base de datos embebida: no requiere instalación de servidor, configuración ni credenciales. La base es un solo archivo (`citasmedicas.db`) que se crea automáticamente al ejecutar la app. Para una aplicación de escritorio académica, esto elimina toda la fricción de infraestructura. MySQL requeriría instalar y configurar un servidor aparte.

---

### P9: ¿Dónde se guarda físicamente el archivo `citasmedicas.db`?

En el directorio raíz del proyecto (donde está el `pom.xml`). La URL está hardcodeada en `DatabaseConnection.java`:

```java
private static final String URL = "jdbc:sqlite:citasmedicas.db";
```

Si el archivo no existe, SQLite lo crea automáticamente al abrir la conexión.

---

### P10: ¿Por qué usan transacciones (`setAutoCommit(false)` + `commit()`) en los DAOs?

Porque un `Paciente`, `Medico` o `Administrador` se guarda en dos tablas: `usuarios` (datos comunes) y la tabla específica (`pacientes`, `medicos`, `administradores`). Si el segundo INSERT falla, el primer registro quedaría huérfano. La transacción garantiza que ambos se insertan o ninguno. Si ocurre una excepción antes del commit, `connection.close()` (vía try-with-resources) hace rollback automático.

---

### P11: ¿Por qué LEFT JOIN en `autenticar()` y no tres consultas separadas?

No sabes qué tipo de usuario es hasta que lees el campo `rol`. Hacer tres consultas separadas (una a `pacientes`, otra a `medicos`, otra a `administradores`) sería ineficiente: viajarías tres veces a la BD. Un solo LEFT JOIN con todas las tablas trae toda la información en una ida y vuelta. Además, SQLite no tiene penalización significativa por JOIN en tablas pequeñas.

---

### P12: ¿Por qué borrado lógico (`activo = 0`) y no DELETE físico?

Dos razones:

1. **Integridad referencial**: las citas de un paciente referencian su `usuario_id`. Si haces DELETE, las citas quedarían huérfanas o violarían la FK.
2. **Historial**: desactivar un paciente no elimina su historial de citas. Se puede reactivar después sin perder datos.

---

### P13: ¿Por qué IDs alfanuméricos (UUID truncado a 8 caracteres) y no auto-increment?

`UUID.randomUUID()` genera identificadores únicos de 128 bits con probabilidad de colisión insignificante. Truncar a 8 caracteres (tomando los primeros caracteres hexadecimales) mantiene unicidad suficiente para el alcance del proyecto. Ventajas sobre auto-increment:

- Se generan en código, no dependen de la BD
- Son portables: si migras a otra BD, los IDs no cambian
- Son legibles en depuración (`"pac-001"`, `"med-003"`) vs números opacos

---

## 12.3 Validaciones y Casos de Borde (P14–P21)

### P14: ¿Qué impide que un paciente agende una cita en un horario ya ocupado por otro paciente del mismo médico?

El flujo completo:

1. `DisponibilidadService.obtenerHorasDisponibles()` genera slots candidatos basados en la agenda del médico
2. Filtra esos slots contra citas ocupadas y bloqueos
3. El solapamiento se detecta con lógica de intervalos: `inicio.isBefore(cFin) && cInicio.isBefore(fin)`
4. Los slots disponibles se muestran en un ComboBox — el usuario solo puede elegir horas que el sistema ya filtró como disponibles
5. Al confirmar, `agendarCita()` hace una validación final

Si dos pacientes intentaran agendar el mismo slot desde dos terminales, el segundo recibiría error. En un entorno single-user (app de escritorio), esto no ocurre.

---

### P15: ¿Qué pasa si el usuario escribe letras en el campo de número de documento?

El sistema no bloquea la escritura en tiempo real (no usa `TextFormatter`), pero al enviar el formulario valida el contenido. El regex `^\d+$` verifica que solo sean dígitos. Si no coincide, retorna "Solo se permiten numeros", el controller lo muestra en el Label de error y no se construye el objeto. El usuario debe corregir antes de continuar.

---

### P16: ¿Qué pasa si el usuario escribe números en el nombre o apellido?

Misma lógica que el documento, pero con regex inverso: `^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$`. Solo permite letras (incluyendo tildes y ñ) y espacios. Cualquier dígito o carácter especial es rechazado.

---

### P17: ¿Qué pasa si el usuario ingresa un email con dominio inexistente?

`ValidacionService.dominioTieneMX()` consulta los registros MX (Mail Exchange) del dominio mediante JNDI/DNS. Si el dominio no tiene MX, significa que no puede recibir correos, y el registro se rechaza. Ejemplo: `usuario@dominiofalso.com` sería rechazado porque ese dominio no tiene servidores de correo configurados.

---

### P18: ¿Cómo evitan que un paciente tenga dos citas activas en la misma especialidad?

Antes de guardar, `CitaService.agendarCita()` llama a `CitaDAO.existeCitaActivaPorPacienteYEspecialidad()`. Esta consulta cuenta cuántas citas del paciente en esa especialidad están en estado `PENDIENTE` o `CONFIRMADA`. Si el conteo es > 0, lanza `IllegalStateException` con el mensaje "Ya tienes una cita activa en esta especialidad".

---

### P19: ¿Qué pasa si el médico ya tiene una cita a las 9:00 con duración 30 min y alguien intenta agendar a las 9:10?

El algoritmo de `DisponibilidadService` usa solapamiento de intervalos, no solo comparación de horas exactas:

- Cita existente: [09:00, 09:30)
- Intento: [09:10, 09:40)
- ¿Se solapan? `09:10 < 09:30` y `09:00 < 09:40` → Sí, se solapan → el slot no está disponible

Esto funciona para cualquier duración, no solo para slots exactos de la agenda. Una cita de 45 min ocupará 1.5 slots de 30 min sin problema.

---

### P20: ¿Qué pasa si el DatePicker de fecha de nacimiento queda vacío en un modify dialog?

Antes de la corrección, `PacienteDAO.actualizarTodo()` llamaba a `fechaNacimiento.toString()` sin null check, lanzando `NullPointerException`. Se corrigió agregando null check tanto en el DAO como en el mapeo desde ResultSet:

```java
stmtPaciente.setString(3, paciente.getFechaNacimiento() != null
    ? paciente.getFechaNacimiento().toString() : null);
```

---

### P21: ¿Qué pasa si el administrador escribe "CCCC" en el campo de tipo de documento?

Originalmente `tipoDoc` era un `TextField` libre en los modify dialogs. Se corrigió cambiándolo a `ComboBox<String>` con opciones fijas: `"CC"`, `"TI"`, `"CE"`, `"Pasaporte"`. El usuario ya no puede escribir valores inválidos — solo selecciona entre las opciones predefinidas.

---

## 12.4 Programación Orientada a Objetos (P22–P25)

### P22: ¿Dónde están los cuatro pilares de la POO en el proyecto?

| Pilar | ¿Dónde? | Ejemplo concreto |
|-------|---------|-----------------|
| **Herencia** | `Usuario` → `Paciente`, `Medico`, `Administrador` | Las 3 subclases heredan id, nombre, email, password, etc. |
| **Polimorfismo** | `getMenuOpciones()` | Cada rol implementa su propio menú. El controller llama al método sin saber el tipo concreto |
| **Encapsulamiento** | Todos los modelos | Atributos `private`, acceso vía getters/setters. `Cita.setHoraInicio()` recalcula `horaFin` automáticamente |
| **Abstracción** | `Usuario` (abstracta) | Define el contrato `getMenuOpciones()` sin implementarlo. Las subclases proveen la implementación concreta |

---

### P23: ¿Por qué no usaron `record` de Java para los modelos?

Los `record` son inmutables (todos los campos son `final`). Los modelos necesitan setters porque el mapeo desde `ResultSet` (JDBC) requiere construir el objeto y luego asignar campos, o usar constructores enormes. Además, `Stack<Cita>` y `Queue<Cita>` necesitan mutabilidad para agregar elementos después de la construcción. Los records no soportan ninguna de las dos cosas.

---

### P24: ¿Por qué una interfaz funcional personalizada (`PreparedStatementBinder`) y no `Consumer<PreparedStatement>`?

`Consumer<PreparedStatement>` no declara `throws SQLException`, por lo que las lambdas tendrían que atrapar la excepción internamente. Al definir:

```java
@FunctionalInterface
private interface PreparedStatementBinder {
    void bind(PreparedStatement statement) throws SQLException;
}
```

las lambdas pueden lanzar `SQLException` sin try-catch, y el método que las recibe la maneja centralizadamente.

---

### P25: ¿Dónde se usa el `switch` con pattern matching de Java 17 y por qué es más seguro?

En `UsuarioDAO.mapearUsuario()`:

```java
return switch (rol) {
    case PACIENTE -> new Paciente(...);
    case MEDICO   -> new Medico(...);
    case ADMIN    -> new Administrador(...);
};
```

Es un switch exhaustivo: el compilador verifica que todos los valores del enum `Rol` están cubiertos. Si alguien agrega un nuevo rol (ej: `RECEPCIONISTA`), este código no compila hasta que se agregue el caso correspondiente. Esto previene errores en tiempo de compilación, no en ejecución.

---

## 12.5 Escenarios de Falla y Robustez (P26–P30)

### P26: ¿Qué pasa si la base de datos se corrompe o el archivo desaparece?

`DatabaseConnection.initialize()` está envuelto en un bloque `try-catch`. Si `DriverManager.getConnection()` falla (archivo corrupto, permisos, etc.), lanza `SQLException`. En `LoginController.initialize()`:

```java
try {
    DatabaseConnection.initialize();
} catch (SQLException exception) {
    errorLabel.setText("No se pudo inicializar SQLite");
}
```

La aplicación no crashea. Muestra un mensaje de error en la UI y el usuario puede reintentar. No hay pérdida de datos porque la BD demo se recrea desde cero al eliminar el archivo.

---

### P27: ¿Qué pasa si se cierra la aplicación mientras se está guardando un paciente (a mitad del INSERT)?

La transacción en `UsuarioDAO.guardarPaciente()` usa `connection.setAutoCommit(false)` y `connection.commit()`. Si la JVM se cierra antes del `commit()`, SQLite revierte automáticamente los cambios pendientes al cerrar la conexión. El paciente no queda a medio registrar.

---

### P28: ¿Qué pasa si la consulta DNS para validar el email es lenta?

La validación DNS (`ValidacionService.dominioTieneMX()`) se ejecuta en el hilo de JavaFX (el mismo de la UI), porque se dispara desde un listener de `focusedProperty`. Si el DNS tarda 3 segundos, la UI se congela 3 segundos. Es una limitación conocida. La solución ideal sería ejecutar la consulta DNS en un hilo separado (`Task` o `CompletableFuture`) y actualizar la UI cuando termine, pero eso escapa al alcance académico del proyecto.

---

### P29: ¿Qué pasa si un administrador elimina un médico que tiene citas futuras?

`MedicoDAO.eliminar()` hace UPDATE `activo = 0` (borrado lógico). Las citas futuras del médico quedan huérfanas: siguen en la BD con estado `PENDIENTE`/`CONFIRMADA`, referenciando a un médico inactivo. El sistema actualmente no cancela automáticamente esas citas. Es una limitación que requeriría notificar a los pacientes y cancelar las citas asociadas.

---

### P30: ¿Qué pasa si un paciente y un administrador intentan modificar el mismo dato simultáneamente?

En una aplicación de escritorio single-user (una instancia por PC), esto no ocurre. No hay concurrencia real porque JavaFX es single-thread. Si en el futuro se migrara a una arquitectura cliente-servidor, habría que implementar control de concurrencia con timestamps o versionado.

---

**P31: ¿Qué pasa cuando un administrador elimina un médico que tiene citas futuras agendadas?**

Se ejecuta `CitaService.reprogramarCitasDelMedico()`. El método:

1. Obtiene todas las citas futuras (`PENDIENTE` o `CONFIRMADA`) del médico eliminado.
2. Busca otros médicos activos de la **misma especialidad**.
3. Para cada cita, intenta reasignarla al **slot más cercano** (mismo día y hora aproximada) de algún médico alternativo.
4. Si no hay disponibilidad el mismo día, busca en los **próximos 14 días**.
5. Si no encuentra ningún slot disponible en ningún médico alternativo, **cancela la cita** por indisponibilidad.

El solapamiento se verifica con la misma lógica de intervalos (`inicio.isBefore(cFin) && cInicio.isBefore(fin)`) que usa `DisponibilidadService`. La integración se hace desde `DashboardAdminController.handleEliminarMedico()`, justo después del borrado lógico.

---

## 12.6 Resumen de Correcciones Aplicadas

| # | Problema | Archivos afectados | Solución |
|---|----------|-------------------|----------|
| 1 | `NullPointerException` en `PacienteDAO.actualizarTodo()` si `fechaNacimiento` es null | `PacienteDAO.java`, `UsuarioDAO.java` | Null check antes de `.toString()` |
| 2 | `tipoDoc` como `TextField` libre en modify dialogs (inconsistente con el registro) | `DashboardPacienteController`, `DashboardMedicoController`, `DashboardAdminController` | Cambiado a `ComboBox<String>` con opciones fijas |
| 3 | Sin validación de email en modify dialogs | `DashboardPacienteController`, `DashboardMedicoController`, `DashboardAdminController` | Agregada llamada a `ValidacionService.mensajeErrorEmail()` |
| 4 | Sin validación de formato para nombre, apellido, documento, EPS | `ValidacionService.java` + todos los controllers | Nuevos métodos `mensajeErrorNombre()`, `mensajeErrorNumeroDocumento()`, `mensajeErrorEps()` |
| 5 | Citas huérfanas al eliminar un médico con citas futuras | `CitaService.java`, `MedicoDAO.java`, `DashboardAdminController.java` | Nueva `reprogramarCitasDelMedico()` que reasigna citas a otros médicos de la misma especialidad o las cancela |

---

*Documento generado a partir del análisis completo del código fuente.*
*Última actualización: Junio 2026*
