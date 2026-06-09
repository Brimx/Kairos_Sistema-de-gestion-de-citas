package co.edu.upc.citasmedicas.model;

import java.time.LocalTime;

public class AgendaMedica {

    private String id;
    private String medicoId;
    private int diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int slotMinutos;

    public AgendaMedica(String id, String medicoId, int diaSemana,
                        LocalTime horaInicio, LocalTime horaFin, int slotMinutos) {
        this.id = id;
        this.medicoId = medicoId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.slotMinutos = slotMinutos;
    }

    public String getId()                          { return id; }
    public void setId(String id)                   { this.id = id; }

    public String getMedicoId()                    { return medicoId; }
    public void setMedicoId(String medicoId)       { this.medicoId = medicoId; }

    public int getDiaSemana()                      { return diaSemana; }
    public void setDiaSemana(int diaSemana)        { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio()               { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin()                  { return horaFin; }
    public void setHoraFin(LocalTime horaFin)      { this.horaFin = horaFin; }

    public int getSlotMinutos()                    { return slotMinutos; }
    public void setSlotMinutos(int slotMinutos)    { this.slotMinutos = slotMinutos; }
}
