package co.edu.upc.citasmedicas.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class BloqueoAgenda {

    private String id;
    private String medicoId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;

    public BloqueoAgenda(String id, String medicoId, LocalDate fecha,
                         LocalTime horaInicio, LocalTime horaFin, String motivo) {
        this.id = id;
        this.medicoId = medicoId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.motivo = motivo;
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getMedicoId()                    { return medicoId; }
    public void setMedicoId(String medicoId)       { this.medicoId = medicoId; }

    public LocalDate getFecha()                    { return fecha; }
    public void setFecha(LocalDate fecha)          { this.fecha = fecha; }

    public LocalTime getHoraInicio()               { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin()                  { return horaFin; }
    public void setHoraFin(LocalTime horaFin)      { this.horaFin = horaFin; }

    public String getMotivo()                      { return motivo; }
    public void setMotivo(String motivo)           { this.motivo = motivo; }

    public boolean esDiaCompleto() {
        return horaInicio == null && horaFin == null;
    }
}
