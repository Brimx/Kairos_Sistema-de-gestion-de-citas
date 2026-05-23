package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Rol;
import java.time.LocalDate;
import java.util.Stack;

/**
 * Representa a un paciente registrado en el sistema EPS.
 * Hereda de Usuario y agrega datos médicos y el historial de citas (Stack).
 */
public class Paciente extends Usuario {

    // --- Atributos propios del Paciente ---
    private String tipoDocumento;       // CC, TI, CE, Pasaporte
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String eps;
    private Stack<Cita> historialCitas; // LIFO: la última cita queda arriba

    // --- Constructor ---
    public Paciente(String id, String nombre, String apellido, String email,
                    String password, String telefono,
                    String tipoDocumento, String numeroDocumento,
                    LocalDate fechaNacimiento, String direccion, String eps) {

        super(id, nombre, apellido, email, password, telefono, Rol.PACIENTE);
        this.tipoDocumento    = tipoDocumento;
        this.numeroDocumento  = numeroDocumento;
        this.fechaNacimiento  = fechaNacimiento;
        this.direccion        = direccion;
        this.eps              = eps;
        this.historialCitas   = new Stack<>();
    }

    // --- Implementación del método abstracto ---
    @Override
    public String[] getMenuOpciones() {
        return new String[]{
            "1. Solicitar cita",
            "2. Ver mis citas",
            "3. Cancelar cita",
            "4. Ver mi historial",
            "5. Actualizar mis datos",
            "6. Cerrar sesión"
        };
    }

    // --- Métodos de negocio ---

    /**
     * Agrega una cita al historial del paciente.
     * Al usar Stack, la última cita siempre queda accesible en O(1).
     */
    public void agregarCitaAlHistorial(Cita cita) {
        historialCitas.push(cita);
    }

    /**
     * Retorna la cita más reciente sin eliminarla del historial.
     */
    public Cita verUltimaCita() {
        if (!historialCitas.isEmpty()) {
            return historialCitas.peek();
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " | Doc: " + tipoDocumento + " " + numeroDocumento;
    }

    // --- Getters y Setters ---
    public String getTipoDocumento()                    { return tipoDocumento; }
    public void setTipoDocumento(String tipo)           { this.tipoDocumento = tipo; }

    public String getNumeroDocumento()                  { return numeroDocumento; }
    public void setNumeroDocumento(String num)          { this.numeroDocumento = num; }

    public LocalDate getFechaNacimiento()               { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fecha)     { this.fechaNacimiento = fecha; }

    public String getDireccion()                        { return direccion; }
    public void setDireccion(String dir)                { this.direccion = dir; }

    public String getEps()                              { return eps; }
    public void setEps(String eps)                      { this.eps = eps; }

    public Stack<Cita> getHistorialCitas()              { return historialCitas; }
}
