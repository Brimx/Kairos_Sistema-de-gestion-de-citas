package co.edu.upc.citasmedicas.model;

import co.edu.upc.citasmedicas.enums.Rol;
import java.time.LocalDate;

public class Administrador extends Usuario {

    private String codigoAdmin;
    private String cargo;
    private String tipoDocumento;
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String direccion;
    private String eps;

    public Administrador(String id, String nombre, String apellido, String email,
                         String password, String telefono,
                         String codigoAdmin, String cargo,
                         String tipoDocumento, String numeroDocumento,
                         LocalDate fechaNacimiento, String direccion, String eps) {

        super(id, nombre, apellido, email, password, telefono, Rol.ADMIN);
        this.codigoAdmin     = codigoAdmin;
        this.cargo           = cargo;
        this.tipoDocumento   = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion       = direccion;
        this.eps             = eps;
    }

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

    public void gestionarUsuario(String accion, String usuarioId) {
    }

    public void verReportes() {
    }

    @Override
    public String toString() {
        return super.toString() + " | Cargo: " + cargo + " | Codigo: " + codigoAdmin;
    }

    public String getCodigoAdmin()              { return codigoAdmin; }
    public void setCodigoAdmin(String codigo)   { this.codigoAdmin = codigo; }

    public String getCargo()                    { return cargo; }
    public void setCargo(String cargo)          { this.cargo = cargo; }

    public String getTipoDocumento()            { return tipoDocumento; }
    public void setTipoDocumento(String td)     { this.tipoDocumento = td; }

    public String getNumeroDocumento()          { return numeroDocumento; }
    public void setNumeroDocumento(String nd)   { this.numeroDocumento = nd; }

    public LocalDate getFechaNacimiento()       { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fn){ this.fechaNacimiento = fn; }

    public String getDireccion()                { return direccion; }
    public void setDireccion(String d)          { this.direccion = d; }

    public String getEps()                      { return eps; }
    public void setEps(String e)                { this.eps = e; }
}
