package co.edu.upc.citasmedicas.enums;

/**
 * Representa el ciclo de vida de una cita médica.
 * Una cita nace como PENDIENTE, puede confirmarse, completarse o cancelarse.
 * Nunca se borra de la base de datos, solo cambia de estado (borrado lógico).
 */
public enum EstadoCita {
    PENDIENTE,      // Cita solicitada, aún no confirmada
    CONFIRMADA,     // Cita activa y en agenda
    COMPLETADA,     // El paciente fue atendido
    CANCELADA       // Cancelada por el paciente o el sistema
}
