# Kairos — Sistema de Gestión de Citas Médicas

> **Curso:** Programación Orientada a Objetos  
> **Docente:** PATRICIA ISABEL ALVAREZ ORTEGA  
> **Lenguaje:** Java  ·  **UI:** JavaFX / MaterialFX  ·  **Paradigma:** POO  
> **Presentación:** 15 minutos  
> **Stack:** Java 17 + JavaFX 21 + SQLite · **Arquitectura:** MVC en capas

**Estudiantes:**
- Santiago Andres Montenegro Muñoz
- Kevin Jhosep Mercado Morón
- Jose Daniel Pinzon Racero

---

## 1. ¿Qué es?

Aplicación de escritorio para administrar **citas médicas** de principio a fin: desde que el paciente solicita la cita hasta que el médico la atiende y deja registro en el historial clínico.

---

## 2. Roles del sistema (3 actores)

| Rol | ¿Qué puede hacer? |
|---|---|
| **Paciente** | Registrarse, solicitar citas, ver/cancelar sus citas, ver historial, actualizar datos |
| **Médico** | Ver agenda del día, ver siguiente paciente, marcar asistencia, consultar historial del paciente |
| **Administrador** | Gestionar pacientes y médicos, ver todas las citas del sistema, ver reportes, registrar nuevos médicos |

---

## 3. Funcionalidades principales

### 3.1 Registro de paciente
- Validación de email (formato + consulta DNS para verificar que el dominio existe y recibe correos)
- Validación de teléfono móvil colombiano (10 dígitos, empieza con 3)
- Confirmación de teléfono (dos campos deben coincidir)
- Validación de nombre y apellido (solo letras con tildes y espacios — números y caracteres especiales son rechazados)
- Validación de número de documento (solo dígitos)
- Validación de EPS (solo letras y espacios)

### 3.2 Inicio de sesión
- Autenticación contra BD con LEFT JOIN a 3 tablas (pacientes, médicos, administradores)
- Redirección automática al dashboard según el rol del usuario

### 3.3 Solicitud de cita (Paciente)
El paciente selecciona:
1. **Especialidad** (17 disponibles: Medicina General, Pediatría, Odontología, Psicología, etc.)
2. **Servicio** dentro de esa especialidad (ej: en Pediatría hay: Pediatría general, Crecimiento y Desarrollo, Asesoría de Lactancia, Vacunación)
3. **Médico** disponible para ese servicio
4. **Fecha** y **hora** — el sistema muestra solo las horas disponibles, calculadas automáticamente según la agenda del médico, citas ya agendadas y bloqueos
5. **Tipo** de cita: Presencial o Virtual
6. **Motivo** de la consulta

**Reglas de negocio que se aplican automáticamente:**
- Un paciente **no puede tener 2 citas activas en la misma especialidad** (debe asistir o cancelar la anterior)
- La hora seleccionada debe estar **disponible** en ese momento
- La duración varía según el servicio y si es primera vez o control

### 3.4 Sobrecupo (Administrador)
- Permite agendar una cita **saltando las validaciones de disponibilidad**
- Útil para urgencias o casos especiales donde el médico acepta un paciente adicional

### 3.5 Gestión de citas por estado (máquina de estados)
Toda cita pasa por este ciclo de vida:

```
Solicitada (PENDIENTE)
    → Confirmada (CONFIRMADA) 
    → Atendida (COMPLETADA) — queda registro en historial clínico
    → Cancelada (CANCELADA) — el paciente o admin pueden cancelar
    → No asistió (NO_ASISTIO) — automático o manual
```

- El paciente ve solo sus citas activas (excluye canceladas)
- El médico ve solo lo que debe atender (PENDIENTE + CONFIRMADA)
- El administrador ve todo

### 3.6 Detección automática de inasistencias
Cada **5 minutos**, un proceso en segundo plano:
- Busca citas PENDIENTE o CONFIRMADA cuya fecha+hora ya pasaron
- Las marca automáticamente como NO_ASISTIO
- El médico también puede marcar inasistencia manualmente desde su dashboard

### 3.7 Agenda del médico
- Vista con calendario visual (CalendarFX) mostrando los eventos del mes
- Lista de citas del día ordenadas por hora
- Botón para "Ver siguiente paciente" (FIFO: el que llegó primero)
- Al atender una cita, si estaba PENDIENTE se auto-confirma y se completa en un solo paso
- Permite consultar el historial clínico del paciente antes de atenderlo

### 3.8 Historial clínico
Cada cita completada deja registro con:
- Diagnóstico, enfermedad actual, receta, remisión, notas
- Visible para el médico (al atender) y para el paciente (en "Ver mi historial")

### 3.9 Bloqueos de agenda
El administrador puede bloquear parcial o totalmente la agenda de un médico en una fecha específica:
- **Día completo**: el médico no agenda nada ese día (ej: vacaciones)
- **Rango horario**: bloquea solo un tramo (ej: "Junta médica de 8 a 9")

### 3.10 Dashboard del Administrador
Vista completa con:
- Tablas de pacientes, médicos y citas con filtros y búsqueda
- Registro de nuevos médicos
- Desactivación de pacientes/médicos (borrado lógico, no se pierden datos históricos)
- Asignación y edición de agenda de médicos
- Gestión de bloqueos

---

## 4. Datos demo incluidos

El sistema arranca con datos de prueba precargados automáticamente:

| Usuario | Email | Contraseña |
|---|---|---|
| Laura Gómez (paciente) | `paciente@demo.com` | `1234` |
| Carlos Rojas (médico) | `medico@demo.com` | `1234` |
| Andrea Torres (admin) | `admin@demo.com` | `1234` |

También hay cuentas demo de otras especialidades: odontología, pediatría, dermatología, psicología.

---

## 5. Cómo ejecutar

```bash
# Compilar y ejecutar con JavaFX
mvn clean javafx:run

# O generar JAR ejecutable con todas las dependencias
mvn clean package assembly:single
java -jar target/SistemaCitasMedicas-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Requiere:** JDK 17+ (no necesita instalar JavaFX por separado, Maven lo maneja)

---

## 6. Decisiones técnicas clave (píldoras)

| Decisión | ¿Por qué? |
|---|---|
| **SQLite embebida** | Cero instalación, un solo archivo, ideal para escritorio |
| **JavaFX (no Swing)** | FXML separa UI de lógica; componentes modernos (TableView, DatePicker) |
| **Stack en Paciente (LIFO)** | Última cita es la más relevante, acceso inmediato |
| **Queue en Médico (FIFO)** | Atiende en orden de llegada, el primero es el primero en salir |
| **Transacciones en DAOs** | Si falla el insert en tabla hija, no queda registro huérfano en tabla padre |
| **Switch con pattern matching** | El compilador verifica que todos los roles estén cubiertos — a prueba de errores |
| **Consulta DNS para email** | Evita registros con dominios falsos o que no reciben correo |
| **Text blocks para SQL** | Queries multilínea legibles sin concatenar strings |
| **Singleton en Session** | El usuario logueado es estado global; FXMLLoader no pasa parámetros a controllers |
| **ScheduledExecutorService** | Detección de inasistencias cada 5 min sin bloquear la UI |
| **Validación de nombres con regex** | Solo letras (con tildes y ñ) evita que el usuario ingrese números en nombre/apellido |
| **Validación de documento con regex** | Solo dígitos (`^\d+$`) evita letras en el número de documento |
| **ComboBox fijo para tipoDoc** | En todos los modify dialogs se cambió TextField por ComboBox con opciones CC/TI/CE/Pasaporte |
| **Null safety en fechaNacimiento** | Se agregaron null checks en DAOs para evitar NPE al actualizar pacientes |

---

## 7. Demo rápida (flujo recomendado)

```
1. Abrir app → Login
2. Entrar como admin@demo.com / 1234
   → Ver dashboard con citas, médicos, pacientes
   → Crear un nuevo médico
   → Bloquear agenda de un médico
3. Cerrar sesión
4. Registrar un paciente nuevo
   → Ver validación de email y teléfono
5. Iniciar sesión como paciente@demo.com / 1234
   → Solicitar una cita (elegir especialidad, servicio, médico, fecha, hora)
   → Ver la cita agendada
6. Iniciar sesión como medico@demo.com / 1234
   → Ver la cita en la agenda
   → Atender al paciente (completar cita + llenar historial clínico)
```
