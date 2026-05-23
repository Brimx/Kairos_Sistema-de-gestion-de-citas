package co.edu.upc.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * Registra cada acción importante realizada en el sistema.
 * El administrador puede ver el historial completo usando una Stack (LIFO),
 * lo que permite ver la última acción de forma inmediata, como un log de auditoría.
 */
public class RegistroHistorial {

    // --- Tipos de acción que se pueden registrar ---
    public enum TipoAccion {
        CITA_CREADA,
        CITA_CANCELADA,
        CITA_COMPLETADA,
        PACIENTE_REGISTRADO,
        MEDICO_REGISTRADO,
        USUARIO_DESACTIVADO,
        INICIO_SESION,
        CIERRE_SESION
    }

    // --- Atributos ---
    private String id;
    private TipoAccion accion;
    private String descripcion;             // Detalle legible de lo que ocurrió
    private String usuarioResponsable;      // ID o nombre del usuario que ejecutó la acción
    private LocalDateTime fecha;

    // --- Constructor ---
    public RegistroHistorial(String id, TipoAccion accion,
                              String descripcion, String usuarioResponsable) {
        this.id                  = id;
        this.accion              = accion;
        this.descripcion         = descripcion;
        this.usuarioResponsable  = usuarioResponsable;
        this.fecha               = LocalDateTime.now(); // Se registra el momento exacto
    }

    @Override
    public String toString() {
        return "[" + fecha + "] " + accion + " — " + descripcion
                + " (por: " + usuarioResponsable + ")";
    }

    // --- Getters ---
    public String getId()                           { return id; }
    public TipoAccion getAccion()                   { return accion; }
    public String getDescripcion()                  { return descripcion; }
    public String getUsuarioResponsable()           { return usuarioResponsable; }
    public LocalDateTime getFecha()                 { return fecha; }
}
