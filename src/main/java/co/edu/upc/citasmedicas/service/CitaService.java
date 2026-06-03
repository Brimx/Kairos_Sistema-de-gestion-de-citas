package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.model.Cita;

/**
 * Capa de servicios y logica de negocio para la gestion de citas.
 */
public class CitaService {

    private final CitaDAO citaDAO;

    public CitaService() {
        this.citaDAO = new CitaDAO();
    }

    public void agendarCita(Cita cita) {
        validarCita(cita);
        if (citaDAO.existeCitaActivaParaMedico(cita.getMedico().getId(), cita.getFecha(), cita.getHoraInicio())) {
            throw new IllegalArgumentException("El medico ya tiene una cita activa en esa fecha y hora");
        }
        citaDAO.guardar(cita);
    }

    public void confirmarCita(String citaId) {
        Cita cita = obtenerCitaExistente(citaId);
        if (!cita.confirmar()) {
            throw new IllegalArgumentException("Solo se pueden confirmar citas pendientes");
        }
        citaDAO.actualizarEstado(citaId, EstadoCita.CONFIRMADA);
    }

    public void cancelarCita(String citaId) {
        Cita cita = obtenerCitaExistente(citaId);
        if (!cita.cancelar()) {
            throw new IllegalArgumentException("Solo se pueden cancelar citas pendientes o confirmadas");
        }
        citaDAO.actualizarEstado(citaId, EstadoCita.CANCELADA);
    }

    public void completarCita(String citaId) {
        Cita cita = obtenerCitaExistente(citaId);
        if (!cita.completar()) {
            throw new IllegalArgumentException("Solo se pueden completar citas confirmadas");
        }
        citaDAO.actualizarEstado(citaId, EstadoCita.COMPLETADA);
    }

    private Cita obtenerCitaExistente(String citaId) {
        if (citaId == null || citaId.isBlank()) {
            throw new IllegalArgumentException("Selecciona una cita");
        }

        Cita cita = citaDAO.buscarPorId(citaId);
        if (cita == null) {
            throw new IllegalArgumentException("La cita seleccionada no existe");
        }
        return cita;
    }

    private void validarCita(Cita cita) {
        if (cita == null) {
            throw new IllegalArgumentException("La cita no puede ser nula");
        }
        if (cita.getPaciente() == null) {
            throw new IllegalArgumentException("Selecciona un paciente");
        }
        if (cita.getMedico() == null) {
            throw new IllegalArgumentException("Selecciona un medico");
        }
        if (cita.getEspecialidad() == null) {
            throw new IllegalArgumentException("Selecciona una especialidad");
        }
        if (cita.getFecha() == null) {
            throw new IllegalArgumentException("Selecciona una fecha");
        }
        if (cita.getHoraInicio() == null) {
            throw new IllegalArgumentException("Ingresa una hora valida");
        }
        if (cita.getTipo() == null) {
            throw new IllegalArgumentException("Selecciona el tipo de cita");
        }
        if (cita.getMotivo() == null || cita.getMotivo().isBlank()) {
            throw new IllegalArgumentException("Ingresa el motivo de la cita");
        }
    }
}
