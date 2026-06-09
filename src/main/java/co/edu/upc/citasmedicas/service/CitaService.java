package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.model.Cita;

import java.time.LocalTime;
import java.util.List;

/**
 * Capa de servicios y logica de negocio para la gestion de citas.
 */
public class CitaService {

    private final CitaDAO citaDAO;
    private final DisponibilidadService disponibilidadService;

    public CitaService() {
        this.citaDAO = new CitaDAO();
        this.disponibilidadService = new DisponibilidadService();
    }

    public void agendarCita(Cita cita) {
        validarCita(cita);

        if (citaDAO.existeCitaActivaPorPacienteYEspecialidad(
                cita.getPaciente().getId(), cita.getEspecialidad().name())) {
            throw new IllegalArgumentException(
                    "Ya tienes una cita activa en " + cita.getEspecialidad().getNombre()
                    + ". Debes asistir o cancelarla antes de agendar otra.");
        }

        List<LocalTime> disponibles = disponibilidadService.obtenerHorasDisponibles(
                cita.getMedico().getId(), cita.getFecha(), cita.getDuracionMinutos());
        if (!disponibles.contains(cita.getHoraInicio())) {
            throw new IllegalArgumentException("La hora seleccionada no esta disponible");
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

    public int autoDetectarInasistencias() {
        List<String> vencidas = citaDAO.obtenerCitasVencidasActivas();
        for (String id : vencidas) {
            citaDAO.actualizarEstado(id, EstadoCita.NO_ASISTIO);
        }
        return vencidas.size();
    }

    public void agendarSobrecupo(Cita cita) {
        validarCita(cita);
        cita.setSobrecupo(true);
        citaDAO.guardar(cita);
    }

    public void marcarNoAsistio(String citaId) {
        Cita cita = obtenerCitaExistente(citaId);
        if (cita.getEstado() == EstadoCita.CANCELADA || cita.getEstado() == EstadoCita.COMPLETADA
                || cita.getEstado() == EstadoCita.NO_ASISTIO) {
            throw new IllegalArgumentException("No se puede marcar como inasistencia una cita "
                    + cita.getEstado().name().toLowerCase());
        }
        citaDAO.actualizarEstado(citaId, EstadoCita.NO_ASISTIO);
    }

    public void completarCita(String citaId) {
        Cita cita = obtenerCitaExistente(citaId);
        if (!cita.completar()) {
            throw new IllegalArgumentException("Solo se pueden completar citas confirmadas");
        }
        citaDAO.actualizarEstado(citaId, EstadoCita.COMPLETADA);
    }

    public void atenderCita(String citaId) {
        Cita cita = obtenerCitaExistente(citaId);
        if (cita.getEstado() == EstadoCita.PENDIENTE) {
            confirmarCita(citaId);
        }
        completarCita(citaId);
    }

    public List<Cita> citasDelPaciente(String pacienteId) {
        if (pacienteId == null || pacienteId.isBlank()) {
            throw new IllegalArgumentException("Paciente invalido");
        }
        return citaDAO.obtenerPorPacienteActivas(pacienteId);
    }

    public List<Cita> agendaDelMedico(String medicoId) {
        if (medicoId == null || medicoId.isBlank()) {
            throw new IllegalArgumentException("Medico invalido");
        }
        return citaDAO.obtenerAgendaMedico(medicoId);
    }

    public List<Cita> todasLasCitas() {
        return citaDAO.obtenerTodas();
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
        if (cita.getServicio() == null) {
            throw new IllegalArgumentException("Selecciona un servicio");
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
        if (cita.getDuracionMinutos() <= 0) {
            throw new IllegalArgumentException("Duracion de cita invalida");
        }
        if (cita.getOrigen() == null || cita.getOrigen().isBlank()) {
            throw new IllegalArgumentException("Origen de cita invalido");
        }
    }
}
