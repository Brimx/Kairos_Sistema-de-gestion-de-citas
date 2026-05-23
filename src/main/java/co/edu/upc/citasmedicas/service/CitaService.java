package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.model.Cita;

/**
 * Capa de servicios y lógica de negocio para la gestión de Citas.
 */
public class CitaService {

    private final CitaDAO citaDAO;

    public CitaService() {
        this.citaDAO = new CitaDAO();
    }

    /**
     * Agenda una nueva cita en el sistema validando la agenda.
     * 
     * @param cita cita a agendar
     */
    public void agendarCita(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        System.out.println("[Service] Agendando cita para paciente: " + cita.getPaciente().getNombre());
        citaDAO.guardar(cita);
    }
}
