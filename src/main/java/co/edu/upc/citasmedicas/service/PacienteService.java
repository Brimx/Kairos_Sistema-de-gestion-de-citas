package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.model.Paciente;

import java.util.List;

/**
 * Capa de servicios y lógica de negocio para la gestión de Pacientes.
 */
public class PacienteService {

    private final PacienteDAO pacienteDAO;

    public PacienteService() {
        this.pacienteDAO = new PacienteDAO();
    }

    /**
     * Registra un paciente aplicando las reglas de negocio necesarias.
     * 
     * @param paciente paciente a registrar
     */
    public void registrarPaciente(Paciente paciente) {
        // Regla de negocio: Validar que el paciente no sea nulo y tenga documento
        if (paciente == null || paciente.getNumeroDocumento() == null || paciente.getNumeroDocumento().isEmpty()) {
            throw new IllegalArgumentException("Datos del paciente inválidos");
        }
        System.out.println("[Service] Registrando paciente con documento: " + paciente.getNumeroDocumento());
        pacienteDAO.guardar(paciente);
    }

    public List<Paciente> listarPacientes() {
        return pacienteDAO.obtenerTodos();
    }

    public void actualizarDatos(String id, String nombre, String apellido, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido no puede estar vacio");
        }
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El telefono no puede estar vacio");
        }
        pacienteDAO.actualizarDatosBasicos(id, nombre.trim(), apellido.trim(), telefono.trim());
    }

    public void desactivarPaciente(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Selecciona un paciente");
        }
        pacienteDAO.desactivar(id);
    }
}
