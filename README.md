# 🏥 Sistema de Gestión de Citas Médicas

Aplicación de escritorio desarrollada en **Java + JavaFX / MaterialFX** para la gestión integral de citas médicas en una EPS. Permite a pacientes agendar citas, a médicos gestionar su agenda del día y a administradores supervisar el sistema completo.

![Java](https://img.shields.io/badge/Java-17+-orange?logo=java) ![JavaFX](https://img.shields.io/badge/JavaFX-17+-blue) ![MaterialFX](https://img.shields.io/badge/MaterialFX-UI-blueviolet) ![Paradigma](https://img.shields.io/badge/Paradigma-POO-green) ![SQLite](https://img.shields.io/badge/Base%20de%20datos-SQLite-lightgrey?logo=sqlite)

---

## 📋 Tabla de contenidos

- [Descripción general](#descripción-general)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura del proyecto](#arquitectura-del-proyecto)
- [Estructura de paquetes](#estructura-de-paquetes)
- [Modelos de datos](#modelos-de-datos)
- [Flujo de una cita](#flujo-de-una-cita)
- [Estructuras de datos utilizadas](#estructuras-de-datos-utilizadas)
- [Roles del sistema](#roles-del-sistema)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
- [Base de datos](#base-de-datos)

---

## Descripción general

El sistema gestiona el ciclo de vida completo de una cita médica: desde que el paciente la solicita hasta que el médico la atiende. Contempla tres tipos de usuario (paciente, médico, administrador), manejo de turnos en sala de espera, historial de citas y un log de auditoría de acciones del sistema.

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17+ | Lenguaje principal |
| JavaFX | 17+ | Interfaz gráfica de usuario |
| MaterialFX | — | Componentes UI con Material Design para JavaFX |
| FXML | — | Definición declarativa de vistas |
| SQLite | — | Base de datos embebida |
| Xerial SQLite JDBC | — | Conector Java ↔ SQLite |
| Maven | — | Gestión de dependencias y build |
| NetBeans | — | IDE de desarrollo |

---

## Arquitectura del proyecto

El proyecto sigue una **arquitectura en capas** (Layered Architecture) combinada con el patrón **MVC** que JavaFX impone naturalmente:

```
┌─────────────────────────────────────┐
│           VISTA (View)              │  JavaFX FXML + ViewManager
├─────────────────────────────────────┤
│        CONTROLADOR (Controller)     │  LoginController, MedicoController...
├─────────────────────────────────────┤
│          SERVICIO (Service)         │  CitaService, PacienteService
├─────────────────────────────────────┤
│            DAO (Data Access)        │  CitaDAO, PacienteDAO
├─────────────────────────────────────┤
│          BASE DE DATOS              │  SQLite (citasmedicas.db)
└─────────────────────────────────────┘
        ↕ todas las capas usan ↕
┌─────────────────────────────────────┐
│       MODELOS + ENUMS               │  Cita, Paciente, Medico, Turno...
└─────────────────────────────────────┘
```

Cada capa solo se comunica con la inmediatamente inferior, nunca salta capas. El controlador llama al servicio, el servicio llama al DAO, el DAO accede a la base de datos.

---

## Estructura de paquetes

```
co.edu.upc.citasmedicas/
│
├── Launcher.java              ← Punto de entrada compatible con exec-maven-plugin
├── Main.java                  ← Clase principal de JavaFX (extiende Application)
│
├── controller/                ← Controlan la lógica de cada pantalla FXML
│   ├── LoginController.java
│   ├── MedicoController.java
│   └── PacienteController.java
│
├── dao/                       ← Acceso a base de datos (operaciones CRUD)
│   ├── CitaDAO.java
│   ├── PacienteDAO.java
│   └── DatabaseConnection.java
│
├── enums/                     ← Constantes tipadas del dominio
│   ├── Especialidad.java      (10 especialidades médicas)
│   ├── EstadoCita.java        (PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA)
│   ├── Rol.java               (PACIENTE, MEDICO, ADMIN)
│   └── TipoCita.java          (PRESENCIAL, VIRTUAL)
│
├── model/                     ← Entidades del dominio (lógica de negocio pura)
│   ├── Usuario.java           (abstracta — base de todos los usuarios)
│   ├── Administrador.java
│   ├── Medico.java
│   ├── Paciente.java
│   ├── Cita.java              (entidad central del sistema)
│   ├── Turno.java
│   └── RegistroHistorial.java
│
├── service/                   ← Reglas de negocio y validaciones
│   ├── CitaService.java
│   └── PacienteService.java
│
└── view/
    └── ViewManager.java       ← Carga y muestra vistas FXML
```

---

## Modelos de datos

### Jerarquía de usuarios

```
Usuario (abstracta)
├── Administrador   → gestiona médicos, pacientes y reportes
├── Medico          → gestiona su agenda del día
└── Paciente        → solicita y consulta sus citas
```

`Usuario` es una clase abstracta que obliga a cada rol a implementar `getMenuOpciones()`, devolviendo las opciones del menú disponibles para ese tipo de usuario.

### Entidades principales

**`Cita`** — Entidad central del sistema. Conecta un `Paciente` con un `Medico`.
- Nace siempre en estado `PENDIENTE`
- Duración fija: 30 minutos (regla de negocio)
- `horaFin` se calcula automáticamente a partir de `horaInicio`
- Nunca se borra físicamente (borrado lógico mediante cambio de estado)

**`Turno`** — Representa al paciente en la sala de espera física.
- Estados: `EN_ESPERA → LLAMADO → ATENDIDO` (o `AUSENTE`)
- Número visible en pantalla en formato `T-014`

**`RegistroHistorial`** — Log de auditoría. Registra cada acción importante con timestamp automático.

---

## Flujo de una cita

```
[Paciente solicita cita]
        │
        ▼
  Estado: PENDIENTE
        │
        ▼ (admin o sistema confirma)
  Estado: CONFIRMADA ──────────────► CANCELADA
        │
        ▼ (médico atiende al paciente)
  Estado: COMPLETADA
```

Las transiciones tienen validaciones: por ejemplo, una cita `COMPLETADA` no puede cancelarse, y una `PENDIENTE` no puede completarse directamente.

---

## Estructuras de datos utilizadas

El proyecto usa estructuras de datos clásicas con propósito específico en cada caso:

| Estructura | Dónde se usa | Por qué |
|---|---|---|
| `Stack<Cita>` | `Paciente.historialCitas` | LIFO: la última cita queda accesible en O(1) |
| `Queue<Cita>` (LinkedList) | `Medico.agendaDelDia` | FIFO: el primero en llegar es el primero en atenderse |
| `Stack<RegistroHistorial>` | Log de auditoría del admin *(diseño definido, implementación pendiente)* | LIFO: la acción más reciente siempre visible primero |

---

## Roles del sistema

### 👤 Paciente
- Solicitar cita médica
- Ver mis citas activas
- Cancelar una cita
- Ver historial de citas anteriores
- Actualizar datos personales

### 🩺 Médico
- Ver agenda del día (cola de pacientes)
- Ver siguiente paciente en cola
- Marcar asistencia / atender paciente
- Ver historial de un paciente

### 🛡️ Administrador
- Gestionar pacientes y médicos
- Ver todas las citas del sistema
- Generar reportes
- Registrar nuevos médicos
- Acceder al log de auditoría

---

## Cómo ejecutar el proyecto

### Requisitos previos

- Java 17 o superior
- Maven 3.8+
- NetBeans 17+ (recomendado) o cualquier IDE con soporte Maven

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/tu-usuario/citas-medicas-eps.git
cd citas-medicas-eps

# 2. Compilar el proyecto
mvn clean compile

# 3. Ejecutar
mvn javafx:run
```

> Si usas NetBeans, basta con abrir el proyecto y presionar **Run** (F6). El `Launcher.java` sirve como punto de entrada alternativo cuando NetBeans usa `exec-maven-plugin` en lugar de `javafx-maven-plugin`.

---

## Base de datos

El sistema usa **SQLite** como base de datos embebida. El archivo `citasmedicas.db` se crea automáticamente en el directorio raíz del proyecto al ejecutar por primera vez.

La conexión se gestiona a través de `DatabaseConnection.java`:

```java
// URL de conexión
"jdbc:sqlite:citasmedicas.db"
```

No requiere instalación de servidor de base de datos — SQLite es completamente embebido en la aplicación.

---

## 👥 Autores

Proyecto académico — **Programación Orientada a Objetos**  
Universidad Popular del Cesar · Facultad de Ingenierías y Tecnológicas

| Rol | Nombre |
|---|---|
| Docente | Patricia Isabel Álvarez Ortega |
| Estudiante | Santiago Andrés Montenegro Muñoz |
| Estudiante | Kevin Jhosep Mercado Morón |
| Estudiante | Jose Daniel Pinzon Racero |
