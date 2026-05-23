package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa una cita médica en el sistema EPS.
 * Es la entidad central del proyecto: conecta Paciente con Medico.
 * Las citas NUNCA se borran físicamente, solo cambian de estado (borrado lógico).
 */
public class Cita {

    // --- Atributos ---
    private String id;                      // UUID único de la cita
    private Paciente paciente;
    private Medico medico;
    private Especialidad especialidad;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;              // Se calcula: horaInicio + 30 minutos
    private EstadoCita estado;
    private TipoCita tipo;
    private String motivo;                  // Motivo de consulta escrito por el paciente

    private static final int DURACION_MINUTOS = 30; // Regla de negocio fija

    // --- Constructor ---
    public Cita(String id, Paciente paciente, Medico medico,
                Especialidad especialidad, LocalDate fecha,
                LocalTime horaInicio, TipoCita tipo, String motivo) {

        this.id           = id;
        this.paciente     = paciente;
        this.medico       = medico;
        this.especialidad = especialidad;
        this.fecha        = fecha;
        this.horaInicio   = horaInicio;
        this.horaFin      = horaInicio.plusMinutes(DURACION_MINUTOS); // Se calcula automático
        this.tipo         = tipo;
        this.motivo       = motivo;
        this.estado       = EstadoCita.PENDIENTE; // Toda cita nace como PENDIENTE
    }

    // --- Métodos de negocio ---

    /**
     * Confirma la cita. Solo puede confirmarse si está PENDIENTE.
     */
    public boolean confirmar() {
        if (this.estado == EstadoCita.PENDIENTE) {
            this.estado = EstadoCita.CONFIRMADA;
            return true;
        }
        return false;
    }

    /**
     * Cancela la cita. Solo puede cancelarse si está PENDIENTE o CONFIRMADA.
     */
    public boolean cancelar() {
        if (this.estado == EstadoCita.PENDIENTE || this.estado == EstadoCita.CONFIRMADA) {
            this.estado = EstadoCita.CANCELADA;
            return true;
        }
        return false;
    }

    /**
     * Marca la cita como completada. Solo si está CONFIRMADA.
     */
    public boolean completar() {
        if (this.estado == EstadoCita.CONFIRMADA) {
            this.estado = EstadoCita.COMPLETADA;
            return true;
        }
        return false;
    }

    /**
     * Retorna la duración fija de las citas (regla de negocio).
     */
    public int getDuracionMinutos() {
        return DURACION_MINUTOS;
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id='" + id + '\'' +
                ", paciente=" + paciente.getNombre() + " " + paciente.getApellido() +
                ", medico=Dr. " + medico.getNombre() + " " + medico.getApellido() +
                ", especialidad=" + especialidad +
                ", fecha=" + fecha +
                ", hora=" + horaInicio + " - " + horaFin +
                ", estado=" + estado +
                ", tipo=" + tipo +
                '}';
    }

    // --- Getters y Setters ---
    public String getId()                           { return id; }
    public void setId(String id)                    { this.id = id; }

    public Paciente getPaciente()                   { return paciente; }
    public void setPaciente(Paciente paciente)      { this.paciente = paciente; }

    public Medico getMedico()                       { return medico; }
    public void setMedico(Medico medico)            { this.medico = medico; }

    public Especialidad getEspecialidad()           { return especialidad; }
    public void setEspecialidad(Especialidad esp)   { this.especialidad = esp; }

    public LocalDate getFecha()                     { return fecha; }
    public void setFecha(LocalDate fecha)           { this.fecha = fecha; }

    public LocalTime getHoraInicio()                { return horaInicio; }
    public void setHoraInicio(LocalTime hora)       { this.horaInicio = hora; }

    public LocalTime getHoraFin()                   { return horaFin; }

    public EstadoCita getEstado()                   { return estado; }
    public void setEstado(EstadoCita estado)        { this.estado = estado; }

    public TipoCita getTipo()                       { return tipo; }
    public void setTipo(TipoCita tipo)              { this.tipo = tipo; }

    public String getMotivo()                       { return motivo; }
    public void setMotivo(String motivo)            { this.motivo = motivo; }
}
