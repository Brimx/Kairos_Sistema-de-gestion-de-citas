package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.Rol;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Representa a un médico del sistema EPS.
 * Hereda de Usuario y agrega datos profesionales y su agenda del día (Queue).
 */
public class Medico extends Usuario {

    // --- Atributos propios del Médico ---
    private String registroMedico;          // Número de registro profesional
    private Especialidad especialidad;
    private String consultorio;
    private Queue<Cita> agendaDelDia;       // FIFO: primer paciente en llegar, primero en atenderse
    private int citasAsignadas;             // Contador de citas totales asignadas

    // --- Constructor ---
    public Medico(String id, String nombre, String apellido, String email,
                  String password, String telefono,
                  String registroMedico, Especialidad especialidad, String consultorio) {

        super(id, nombre, apellido, email, password, telefono, Rol.MEDICO);
        this.registroMedico  = registroMedico;
        this.especialidad    = especialidad;
        this.consultorio     = consultorio;
        this.agendaDelDia    = new LinkedList<>();
        this.citasAsignadas  = 0;
    }

    // --- Implementación del método abstracto ---
    @Override
    public String[] getMenuOpciones() {
        return new String[]{
            "1. Ver agenda del día",
            "2. Ver siguiente paciente",
            "3. Marcar asistencia",
            "4. Ver historial de paciente",
            "5. Cerrar sesión"
        };
    }

    // --- Métodos de negocio ---

    /**
     * Agrega una cita a la cola del día.
     * La cola respeta el orden de llegada (FIFO).
     */
    public void agregarCitaAAgenda(Cita cita) {
        agendaDelDia.offer(cita);
        citasAsignadas++;
    }

    /**
     * Retorna el siguiente paciente a atender sin sacarlo de la cola.
     */
    public Cita verSiguientePaciente() {
        return agendaDelDia.peek();
    }

    /**
     * Saca al siguiente paciente de la cola (cuando ya fue atendido).
     */
    public Cita atenderSiguiente() {
        return agendaDelDia.poll();
    }

    /**
     * Indica si el médico tiene espacio en su agenda.
     */
    public boolean tieneDisponibilidad() {
        return isActivo();
    }

    @Override
    public String toString() {
        return super.toString() + " | Especialidad: " + especialidad
                + " | Consultorio: " + consultorio;
    }

    // --- Getters y Setters ---
    public String getRegistroMedico()                   { return registroMedico; }
    public void setRegistroMedico(String reg)           { this.registroMedico = reg; }

    public Especialidad getEspecialidad()               { return especialidad; }
    public void setEspecialidad(Especialidad esp)       { this.especialidad = esp; }

    public String getConsultorio()                      { return consultorio; }
    public void setConsultorio(String cons)             { this.consultorio = cons; }

    public Queue<Cita> getAgendaDelDia()                { return agendaDelDia; }

    public int getCitasAsignadas()                      { return citasAsignadas; }
}
