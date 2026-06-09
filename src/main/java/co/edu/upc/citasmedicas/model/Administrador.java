package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Rol;

/**
 * Representa al personal administrativo de Kairos.
 * Tiene acceso total: gestiona médicos, pacientes y reportes.
 */
public class Administrador extends Usuario {

    // --- Atributos propios del Administrador ---
    private String codigoAdmin;     // Código interno del sistema para identificar al admin
    private String cargo;           // Ej: "Coordinador de agenda", "Jefe administrativo"

    // --- Constructor ---
    public Administrador(String id, String nombre, String apellido, String email,
                         String password, String telefono,
                         String codigoAdmin, String cargo) {

        super(id, nombre, apellido, email, password, telefono, Rol.ADMIN);
        this.codigoAdmin = codigoAdmin;
        this.cargo       = cargo;
    }

    // --- Implementación del método abstracto ---
    @Override
    public String[] getMenuOpciones() {
        return new String[]{
            "Gestionar pacientes",
            "Gestionar medicos",
            "Ver todas las citas",
            "Ver reportes",
            "Registrar nuevo medico",
            "Cerrar sesion"
        };
    }

    // --- Métodos de negocio ---
    public void gestionarUsuario(String accion, String usuarioId) {
    }

    public void verReportes() {
    }

    @Override
    public String toString() {
        return super.toString() + " | Cargo: " + cargo + " | Código: " + codigoAdmin;
    }

    // --- Getters y Setters ---
    public String getCodigoAdmin()              { return codigoAdmin; }
    public void setCodigoAdmin(String codigo)   { this.codigoAdmin = codigo; }

    public String getCargo()                    { return cargo; }
    public void setCargo(String cargo)          { this.cargo = cargo; }
}
