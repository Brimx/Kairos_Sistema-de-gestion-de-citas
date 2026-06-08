# Sistema de Gestión de Citas Médicas

Aplicación de escritorio desarrollada en **Java + JavaFX** para la gestión integral de citas médicas en una EPS. Permite a pacientes agendar citas, a médicos gestionar su agenda del día y a administradores supervisar el sistema completo.

![Java](https://img.shields.io/badge/Java-17+-orange?logo=java) ![JavaFX](https://img.shields.io/badge/JavaFX-21+-blue) ![Paradigma](https://img.shields.io/badge/Paradigma-POO-green) ![SQLite](https://img.shields.io/badge/Base%20de%20datos-SQLite-lightgrey?logo=sqlite)

---

## Tabla de contenidos

- [Descripción general](#descripción-general)
- [Tecnologías utilizadas](#tecnologías-utilizadas)
- [Arquitectura del proyecto](#arquitectura-del-proyecto)
- [Estructura de paquetes](#estructura-de-paquetes)
- [Modelos de datos](#modelos-de-datos)
- [Flujo de una cita](#flujo-de-una-cita)
- [Reglas de validación](#reglas-de-validación)
- [Roles del sistema](#roles-del-sistema)
- [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
- [Base de datos](#base-de-datos)
- [Mejoras futuras](#mejoras-futuras)

---

## Descripción general

El sistema gestiona el ciclo de vida completo de una cita médica: desde que el paciente la solicita hasta que el médico la atiende. Contempla tres tipos de usuario (paciente, médico, administrador), con dashboards separados por rol y autenticación real contra SQLite.

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17+ | Lenguaje principal |
| JavaFX | 21.0.2 | Interfaz gráfica de usuario |
| FXML | — | Definición declarativa de vistas |
| SQLite | — | Base de datos embebida |
| Xerial SQLite JDBC | 3.45.2.0 | Conector Java ↔ SQLite |
| Maven | — | Gestión de dependencias y build |
| NetBeans | — | IDE de desarrollo |

---

## Arquitectura del proyecto

El proyecto sigue una **arquitectura en capas** (Layered Architecture) combinada con el patrón **MVC**:

```
┌─────────────────────────────────────┐
│           VISTA (View)              │  JavaFX FXML + ViewManager
├─────────────────────────────────────┤
│        CONTROLADOR (Controller)     │  LoginController, RegistroController,
│                                     │  DashboardPacienteController,
│                                     │  DashboardMedicoController,
│                                     │  DashboardAdminController
├─────────────────────────────────────┤
│          SERVICIO (Service)         │  CitaService, PacienteService, Session
├─────────────────────────────────────┤
│            DAO (Data Access)        │  UsuarioDAO, CitaDAO, PacienteDAO,
│                                     │  MedicoDAO, AdministradorDAO
├─────────────────────────────────────┤
│          BASE DE DATOS              │  SQLite (citasmedicas.db)
└─────────────────────────────────────┘
        ↕ todas las capas usan ↕
┌─────────────────────────────────────┐
│       MODELOS + ENUMS               │  Cita, Paciente, Medico, Turno...
└─────────────────────────────────────┘
```

Cada capa solo se comunica con la inmediatamente inferior, nunca salta capas.

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
│   ├── RegistroController.java
│   ├── DashboardPacienteController.java
│   ├── DashboardMedicoController.java
│   └── DashboardAdminController.java
│
├── dao/                       ← Acceso a base de datos (operaciones CRUD)
│   ├── DatabaseConnection.java   (inicializa BD y datos demo)
│   ├── UsuarioDAO.java           (autenticación y guardado multi-rol)
│   ├── PacienteDAO.java
│   ├── MedicoDAO.java
│   ├── AdministradorDAO.java
│   └── CitaDAO.java
│
├── enums/                     ← Constantes tipadas del dominio
│   ├── Rol.java               (PACIENTE, MEDICO, ADMIN)
│   ├── EstadoCita.java        (PENDIENTE, CONFIRMADA, COMPLETADA, CANCELADA)
│   ├── Especialidad.java      (10 especialidades médicas)
│   └── TipoCita.java          (PRESENCIAL, VIRTUAL)
│
├── model/                     ← Entidades del dominio
│   ├── Usuario.java           (abstracta — base de todos los usuarios)
│   ├── Paciente.java
│   ├── Medico.java
│   ├── Administrador.java
│   ├── Cita.java              (entidad central — máquina de estados)
│   ├── Turno.java             (sala de espera)
│   └── RegistroHistorial.java (log de auditoría)
│
├── service/                   ← Reglas de negocio y validaciones
│   ├── CitaService.java
│   ├── PacienteService.java
│   └── Session.java           (sesión del usuario autenticado)
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

`Usuario` es una clase abstracta que obliga a cada rol a implementar `getMenuOpciones()`. Almacena datos comunes: id, nombre, apellido, email, password, teléfono, rol y estado activo.

### Entidades principales

**`Cita`** — Entidad central del sistema. Conecta un `Paciente` con un `Medico`.
- Nace siempre en estado `PENDIENTE`
- Duración fija: 30 minutos (regla de negocio)
- `horaFin` se calcula automáticamente a partir de `horaInicio`
- Nunca se borra físicamente (borrado lógico mediante cambio de estado)

**`Turno`** — Representa al paciente en la sala de espera física.
- Estados: `EN_ESPERA → LLAMADO → ATENDIDO` (o `AUSENTE`)

**`RegistroHistorial`** — Log de auditoría. Registra cada acción importante.

### Esquema de base de datos

```sql
-- Tabla principal de usuarios (hereda PACIENTE, MEDICO, ADMIN)
CREATE TABLE usuarios (
    id TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    telefono TEXT,
    rol TEXT NOT NULL CHECK(rol IN ('PACIENTE','MEDICO','ADMIN')),
    activo INTEGER DEFAULT 1
);

-- Tablas específicas por rol (relación 1:1 con usuarios)
CREATE TABLE pacientes (
    usuario_id TEXT PRIMARY KEY REFERENCES usuarios(id),
    tipo_documento TEXT,
    numero_documento TEXT UNIQUE,
    fecha_nacimiento TEXT,
    direccion TEXT,
    eps TEXT
);

CREATE TABLE medicos (
    usuario_id TEXT PRIMARY KEY REFERENCES usuarios(id),
    registro_medico TEXT UNIQUE,
    especialidad TEXT NOT NULL,
    consultorio TEXT
);

CREATE TABLE administradores (
    usuario_id TEXT PRIMARY KEY REFERENCES usuarios(id),
    codigo_admin TEXT UNIQUE,
    cargo TEXT
);

CREATE TABLE citas (
    id TEXT PRIMARY KEY,
    paciente_id TEXT NOT NULL REFERENCES usuarios(id),
    medico_id TEXT NOT NULL REFERENCES usuarios(id),
    especialidad TEXT NOT NULL,
    fecha TEXT NOT NULL,
    hora_inicio TEXT NOT NULL,
    hora_fin TEXT NOT NULL,
    tipo TEXT NOT NULL CHECK(tipo IN ('PRESENCIAL','VIRTUAL')),
    estado TEXT NOT NULL DEFAULT 'PENDIENTE' CHECK(estado IN ('PENDIENTE','CONFIRMADA','COMPLETADA','CANCELADA')),
    motivo TEXT
);
```

---

## Flujo de una cita

```
[Paciente solicita cita]
        │
        ▼
  Estado: PENDIENTE
        │
        ▼ (admin confirma)
  Estado: CONFIRMADA ──────────────► CANCELADA
        │
        ▼ (médico atiende al paciente)
  Estado: COMPLETADA
```

---

## Reglas de validación

- **Doble cita**: no se permite agendar una cita si el paciente o el médico ya tienen otra en el mismo horario.
- **Transiciones válidas**: una cita `COMPLETADA` no puede cancelarse; una `PENDIENTE` no puede saltar a `COMPLETADA`.
- **Campos requeridos**: paciente, médico, especialidad, fecha, hora y tipo son obligatorios.
- **Email duplicado**: no se permite registrar dos usuarios con el mismo email.
- **Código de administrador**: para registrar un admin se requiere un código secreto interno (`ADMIN-SECRET-2026`).
- **Borrado lógico**: las citas canceladas y usuarios desactivados permanecen en la BD con estado `CANCELADA` o `activo = 0`.

---

## Roles del sistema

### Paciente
- Registrarse en el sistema
- Solicitar cita médica (seleccionando médico, fecha, hora y tipo)
- Ver mis citas activas
- Cancelar una cita (excepto si ya fue completada)

### Médico
- Ver agenda del día (citas pendientes y confirmadas)
- Marcar paciente como atendido (confirma automáticamente si estaba pendiente)

### Administrador
- Gestionar pacientes (listar, editar nombre/teléfono, desactivar)
- Gestionar médicos (listar, registrar nuevo)
- Ver todas las citas del sistema
- Confirmar o cancelar cualquier cita

---

## Cómo ejecutar el proyecto

### Requisitos previos
- Java 17 o superior
- Maven 3.8+
- NetBeans 17+ (recomendado) o cualquier IDE con soporte Maven

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/Brimx/Sistema-de-gestion-de-citas.git
cd Sistema-de-gestion-de-citas

# 2. Compilar el proyecto
mvn clean compile

# 3. Ejecutar
mvn javafx:run
```

> Si usas NetBeans, abre el proyecto y presiona **Run** (F6). La primera ejecución crea automáticamente la base de datos SQLite con datos de demostración.

### Credenciales de demostración

| Rol | Email | Contraseña |
|-----|-------|------------|
| Paciente | `paciente@demo.com` | `1234` |
| Médico | `medico@demo.com` | `1234` |
| Administrador | `admin@demo.com` | `1234` |

---

## Base de datos

El sistema usa **SQLite** como base de datos embebida. El archivo `citasmedicas.db` se crea automáticamente en el directorio raíz del proyecto al ejecutar por primera vez.

**Ventajas de SQLite:**
- No requiere instalación de servidor
- Archivo único portátil
- Transacciones ACID
- `PRAGMA foreign_keys = ON` para integridad referencial

La conexión se gestiona a través de `DatabaseConnection.java`:
```java
// URL de conexión
"jdbc:sqlite:citasmedicas.db"
```

---

## Mejoras futuras

- **Supabase**: migrar a base de datos en la nube para permitir acceso multi-sede y sincronización en tiempo real.
- **Módulo de turnos**: integrar `Turno` y `RegistroHistorial` con DAO y UI para sala de espera y auditoría.
- **Reportes**: generar estadísticas de citas por médico, especialidad y periodo.
- **MaterialFX**: opcionalmente migrar la UI a Material Design para una experiencia visual más moderna.

---

## Autores

Proyecto académico — **Programación Orientada a Objetos**  
Universidad Popular del Cesar · Facultad de Ingenierías y Tecnológicas

| Rol | Nombre |
|---|---|
| Docente | Patricia Isabel Álvarez Ortega |
| Estudiante | Santiago Andrés Montenegro Muñoz |
| Estudiante | Kevin Jhosep Mercado Morón |
| Estudiante | Jose Daniel Pinzon Racero |
