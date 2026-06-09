package co.edu.upc.citasmedicas.model;

import java.time.LocalDate;

public class HistorialClinico {

    private String id;
    private String citaId;
    private String medicoId;
    private String pacienteId;
    private LocalDate fechaConsulta;
    private String diagnostico;
    private String enfermedadActual;
    private String receta;
    private String remision;
    private String notas;

    public HistorialClinico() {
    }

    public HistorialClinico(String id, String citaId, String medicoId, String pacienteId,
                            LocalDate fechaConsulta, String diagnostico, String enfermedadActual,
                            String receta, String remision, String notas) {
        this.id = id;
        this.citaId = citaId;
        this.medicoId = medicoId;
        this.pacienteId = pacienteId;
        this.fechaConsulta = fechaConsulta;
        this.diagnostico = diagnostico;
        this.enfermedadActual = enfermedadActual;
        this.receta = receta;
        this.remision = remision;
        this.notas = notas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCitaId() {
        return citaId;
    }

    public void setCitaId(String citaId) {
        this.citaId = citaId;
    }

    public String getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(String medicoId) {
        this.medicoId = medicoId;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public LocalDate getFechaConsulta() {
        return fechaConsulta;
    }

    public void setFechaConsulta(LocalDate fechaConsulta) {
        this.fechaConsulta = fechaConsulta;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getEnfermedadActual() {
        return enfermedadActual;
    }

    public void setEnfermedadActual(String enfermedadActual) {
        this.enfermedadActual = enfermedadActual;
    }

    public String getReceta() {
        return receta;
    }

    public void setReceta(String receta) {
        this.receta = receta;
    }

    public String getRemision() {
        return remision;
    }

    public void setRemision(String remision) {
        this.remision = remision;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
