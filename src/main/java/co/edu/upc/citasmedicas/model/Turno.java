package co.edu.upc.citasmedicas.model;

import java.time.LocalDateTime;

/**
 * Representa el turno físico de un paciente en sala de espera.
 * Se usa cuando el paciente ya llegó al consultorio y espera ser llamado.
 * La gestión de turnos usa una Queue (FIFO) en el servicio.
 */
public class Turno {

    // --- Estados posibles de un turno ---
    public enum EstadoTurno {
        EN_ESPERA,      // El paciente llegó y está esperando
        LLAMADO,        // Se llamó al paciente
        ATENDIDO,       // El médico ya lo atendió
        AUSENTE         // Fue llamado y no respondió
    }

    // --- Atributos ---
    private String id;
    private Paciente paciente;
    private Medico medico;
    private int numero;                     // Número de turno visible en pantalla (ej: T-014)
    private LocalDateTime fechaLlegada;     // Cuando el paciente marcó su llegada
    private EstadoTurno estado;

    // --- Constructor ---
    public Turno(String id, Paciente paciente, Medico medico, int numero) {
        this.id           = id;
        this.paciente     = paciente;
        this.medico       = medico;
        this.numero       = numero;
        this.fechaLlegada = LocalDateTime.now();
        this.estado       = EstadoTurno.EN_ESPERA;
    }

    // --- Métodos de negocio ---

    /**
     * Llama al paciente. Cambia el estado a LLAMADO.
     */
    public void llamar() {
        this.estado = EstadoTurno.LLAMADO;
    }

    /**
     * Marca el turno como atendido.
     */
    public void atender() {
        this.estado = EstadoTurno.ATENDIDO;
    }

    /**
     * Marca el turno como ausente si el paciente no responde.
     */
    public void marcarAusente() {
        this.estado = EstadoTurno.AUSENTE;
    }

    @Override
    public String toString() {
        return "Turno T-" + String.format("%03d", numero)
                + " | " + paciente.getNombre() + " " + paciente.getApellido()
                + " | Estado: " + estado
                + " | Llegada: " + fechaLlegada;
    }

    // --- Getters y Setters ---
    public String getId()                           { return id; }
    public void setId(String id)                    { this.id = id; }

    public Paciente getPaciente()                   { return paciente; }
    public void setPaciente(Paciente paciente)      { this.paciente = paciente; }

    public Medico getMedico()                       { return medico; }
    public void setMedico(Medico medico)            { this.medico = medico; }

    public int getNumero()                          { return numero; }
    public void setNumero(int numero)               { this.numero = numero; }

    public LocalDateTime getFechaLlegada()          { return fechaLlegada; }

    public EstadoTurno getEstado()                  { return estado; }
    public void setEstado(EstadoTurno estado)       { this.estado = estado; }
}
