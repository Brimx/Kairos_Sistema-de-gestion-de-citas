package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Rol;

/**
 * Representa al personal administrativo del sistema EPS.
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
            "1. Gestionar pacientes",
            "2. Gestionar médicos",
            "3. Ver todas las citas",
            "4. Ver reportes",
            "5. Registrar nuevo médico",
            "6. Cerrar sesión"
        };
    }

    // --- Métodos de negocio ---
    public void gestionarUsuario(String accion, String usuarioId) {
        System.out.println("Admin [" + codigoAdmin + "] ejecutó '" + accion
                + "' sobre usuario ID: " + usuarioId);
    }

    public void verReportes() {
        System.out.println("Generando reportes del sistema...");
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
