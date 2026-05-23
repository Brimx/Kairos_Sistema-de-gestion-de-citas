package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.model.Paciente;

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
}
