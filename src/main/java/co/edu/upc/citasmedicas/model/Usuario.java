package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Rol;

import java.util.UUID;

/**
 * Clase base de todos los usuarios del sistema.
 * Es abstracta porque nunca se crea un "Usuario" genérico,
 * siempre será un Paciente, Medico o Administrador.
 */
public abstract class Usuario {

    // --- Atributos ---
    private String id;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
    private String telefono;
    private Rol rol;
    private boolean activo;

    // --- Constructor (usado por DAOs que generan el ID) ---
    public Usuario(String id, String nombre, String apellido,
                   String email, String password, String telefono, Rol rol) {
        this.id       = id;
        this.nombre   = nombre;
        this.apellido = apellido;
        this.email    = email;
        this.password = password;
        this.telefono = telefono;
        this.rol      = rol;
        this.activo   = true; // Todo usuario arranca activo por defecto
    }

    // --- Constructor sin ID (genera ID automático) ---
    public Usuario(String nombre, String apellido, String email, String password, String telefono, Rol rol) {
        this.id = generarIdUnico();
        this.nombre   = nombre;
        this.apellido = apellido;
        this.email    = email;
        this.password = password;
        this.telefono = telefono;
        this.rol      = rol;
        this.activo   = true;
    }

    // --- Método estático para generar IDs únicos ---
    private static String generarIdUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // --- Método abstracto ---
    // Cada rol implementa este método devolviendo las opciones de su menú
    public abstract String[] getMenuOpciones();

    // --- Métodos comunes ---
    public boolean login(String emailIngresado, String passwordIngresado) {
        return this.email.equals(emailIngresado) && this.password.equals(passwordIngresado);
    }

    public void logout() {
        System.out.println("Sesión cerrada para: " + nombre + " " + apellido);
    }

    @Override
    public String toString() {
        return "[" + rol + "] " + nombre + " " + apellido + " | Email: " + email;
    }

    // --- Getters y Setters ---
    public String getId()                   { return id; }
    public void setId(String id)            { this.id = id; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public String getApellido()             { return apellido; }
    public void setApellido(String ap)      { this.apellido = ap; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public String getPassword()             { return password; }
    public void setPassword(String pw)      { this.password = pw; }

    public String getTelefono()             { return telefono; }
    public void setTelefono(String tel)     { this.telefono = tel; }

    public Rol getRol()                     { return rol; }
    public void setRol(Rol rol)             { this.rol = rol; }

    public boolean isActivo()               { return activo; }
    public void setActivo(boolean activo)   { this.activo = activo; }
}
