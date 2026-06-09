package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.ServicioCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import java.time.LocalDate;
import java.time.LocalTime;

public class Cita {

    private String id;
    private Paciente paciente;
    private Medico medico;
    private Especialidad especialidad;
    private ServicioCita servicio;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private EstadoCita estado;
    private TipoCita tipo;
    private String motivo;

    private static final int DURACION_MINUTOS = 30;

    public Cita(String id, Paciente paciente, Medico medico,
                ServicioCita servicio, LocalDate fecha,
                LocalTime horaInicio, TipoCita tipo, String motivo) {
        this.id = id;
        this.paciente = paciente;
        this.medico = medico;
        this.especialidad = servicio.getEspecialidadRequerida();
        this.servicio = servicio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaInicio.plusMinutes(DURACION_MINUTOS);
        this.tipo = tipo;
        this.motivo = motivo;
        this.estado = EstadoCita.PENDIENTE;
    }

    public boolean confirmar() {
        if (this.estado == EstadoCita.PENDIENTE) {
            this.estado = EstadoCita.CONFIRMADA;
            return true;
        }
        return false;
    }

    public boolean cancelar() {
        if (this.estado == EstadoCita.PENDIENTE || this.estado == EstadoCita.CONFIRMADA) {
            this.estado = EstadoCita.CANCELADA;
            return true;
        }
        return false;
    }

    public boolean completar() {
        if (this.estado == EstadoCita.CONFIRMADA) {
            this.estado = EstadoCita.COMPLETADA;
            return true;
        }
        return false;
    }

    public int getDuracionMinutos() {
        return DURACION_MINUTOS;
    }

    @Override
    public String toString() {
        return "Cita{" +
                "id='" + id + '\'' +
                ", paciente=" + paciente.getNombre() + " " + paciente.getApellido() +
                ", medico=Dr. " + medico.getNombre() + " " + medico.getApellido() +
                ", servicio=" + servicio.getNombre() +
                ", especialidad=" + especialidad +
                ", fecha=" + fecha +
                ", hora=" + horaInicio + " - " + horaFin +
                ", estado=" + estado +
                ", tipo=" + tipo +
                '}';
    }

    public String getId()                           { return id; }
    public void setId(String id)                    { this.id = id; }

    public Paciente getPaciente()                   { return paciente; }
    public void setPaciente(Paciente paciente)      { this.paciente = paciente; }

    public Medico getMedico()                       { return medico; }
    public void setMedico(Medico medico)            { this.medico = medico; }

    public Especialidad getEspecialidad()           { return especialidad; }
    public void setEspecialidad(Especialidad esp)   { this.especialidad = esp; }

    public ServicioCita getServicio()               { return servicio; }
    public void setServicio(ServicioCita servicio)  { this.servicio = servicio; }

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
