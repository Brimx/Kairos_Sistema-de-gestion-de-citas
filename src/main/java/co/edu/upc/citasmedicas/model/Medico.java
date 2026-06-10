package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.Rol;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.Queue;

public class Medico extends Usuario {

    private String registroMedico;
    private Especialidad especialidad;
    private String tipoDocumento;
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String eps;
    private Queue<Cita> agendaDelDia;
    private int citasAsignadas;

    public Medico(String id, String nombre, String apellido, String email,
                  String password, String telefono,
                  String registroMedico, Especialidad especialidad,
                  String tipoDocumento, String numeroDocumento,
                  LocalDate fechaNacimiento, String direccion, String eps) {

        super(id, nombre, apellido, email, password, telefono, Rol.MEDICO);
        this.registroMedico  = registroMedico;
        this.especialidad    = especialidad;
        this.tipoDocumento   = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion       = direccion;
        this.eps             = eps;
        this.agendaDelDia    = new LinkedList<>();
        this.citasAsignadas  = 0;
    }

    @Override
    public String[] getMenuOpciones() {
        return new String[]{
            "Ver agenda del dia",
            "Ver siguiente paciente",
            "Marcar asistencia",
            "Ver historial de paciente",
            "Cerrar sesion"
        };
    }

    public void agregarCitaAAgenda(Cita cita) {
        agendaDelDia.offer(cita);
        citasAsignadas++;
    }

    public Cita verSiguientePaciente() {
        return agendaDelDia.peek();
    }

    public Cita atenderSiguiente() {
        return agendaDelDia.poll();
    }

    public boolean tieneDisponibilidad() {
        return isActivo();
    }

    @Override
    public String toString() {
        return super.toString() + " | Especialidad: " + especialidad;
    }

    public String getRegistroMedico()                   { return registroMedico; }
    public void setRegistroMedico(String reg)           { this.registroMedico = reg; }

    public Especialidad getEspecialidad()               { return especialidad; }
    public void setEspecialidad(Especialidad esp)       { this.especialidad = esp; }

    public String getTipoDocumento()                    { return tipoDocumento; }
    public void setTipoDocumento(String td)             { this.tipoDocumento = td; }

    public String getNumeroDocumento()                  { return numeroDocumento; }
    public void setNumeroDocumento(String nd)           { this.numeroDocumento = nd; }

    public LocalDate getFechaNacimiento()               { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fn)        { this.fechaNacimiento = fn; }

    public String getDireccion()                        { return direccion; }
    public void setDireccion(String d)                  { this.direccion = d; }

    public String getEps()                              { return eps; }
    public void setEps(String e)                        { this.eps = e; }

    public Queue<Cita> getAgendaDelDia()                { return agendaDelDia; }

    public int getCitasAsignadas()                      { return citasAsignadas; }
}
