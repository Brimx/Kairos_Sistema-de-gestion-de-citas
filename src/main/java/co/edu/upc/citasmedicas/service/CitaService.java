package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.AgendaMedicaDAO;
import co.edu.upc.citasmedicas.dao.BloqueoAgendaDAO;
import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.model.AgendaMedica;
import co.edu.upc.citasmedicas.model.BloqueoAgenda;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    public void reprogramarCitasDelMedico(String medicoId) {
        List<Cita> citasFuturas = citaDAO.obtenerAgendaMedico(medicoId).stream()
                .filter(c -> !c.getFecha().isBefore(LocalDate.now()))
                .toList();

        if (citasFuturas.isEmpty()) return;

        MedicoDAO medicoDAO = new MedicoDAO();
        Medico medicoOriginal = medicoDAO.buscarPorId(medicoId);
        if (medicoOriginal == null) return;

        List<Medico> alternativos = medicoDAO.obtenerPorEspecialidad(
                medicoOriginal.getEspecialidad(), medicoId);

        AgendaMedicaDAO agendaDAO = new AgendaMedicaDAO();
        BloqueoAgendaDAO bloqueoDAO = new BloqueoAgendaDAO();

        for (Cita cita : citasFuturas) {
            SlotReasignado slot = buscarSlotDisponible(
                    cita, alternativos, agendaDAO, bloqueoDAO);

            if (slot != null) {
                cita.setMedico(slot.medico());
                cita.setFecha(slot.fecha());
                cita.setHoraInicio(slot.hora());
                citaDAO.actualizarCita(cita);
            } else {
                cita.cancelar();
                citaDAO.actualizarEstado(cita.getId(), EstadoCita.CANCELADA);
            }
        }
    }

    private record SlotReasignado(Medico medico, LocalDate fecha, LocalTime hora) {}

    private SlotReasignado buscarSlotDisponible(Cita cita, List<Medico> alternativos,
                                                  AgendaMedicaDAO agendaDAO, BloqueoAgendaDAO bloqueoDAO) {
        if (alternativos.isEmpty()) return null;

        int duracion = cita.getDuracionMinutos();
        List<SlotReasignado> candidatos = new ArrayList<>();

        for (Medico medico : alternativos) {
            int diaSemana = cita.getFecha().getDayOfWeek().getValue();
            AgendaMedica agenda = agendaDAO.obtenerPorMedicoYDia(medico.getId(), diaSemana);
            if (agenda == null) continue;

            List<Cita> ocupadas = citaDAO.obtenerActivasPorMedicoYFecha(medico.getId(), cita.getFecha());
            List<BloqueoAgenda> bloqueos = bloqueoDAO.obtenerPorMedicoYFecha(medico.getId(), cita.getFecha());

            LocalTime cursor = agenda.getHoraInicio();
            int slotSize = agenda.getSlotMinutos();
            LocalTime limite = agenda.getHoraFin();

            while (!cursor.isAfter(limite.minusMinutes(duracion))) {
                if (!tieneSolapamiento(cursor, duracion, ocupadas, bloqueos)) {
                    long diferencia = Math.abs(
                            cursor.toSecondOfDay() - cita.getHoraInicio().toSecondOfDay());
                    candidatos.add(new SlotReasignado(medico, cita.getFecha(), cursor));
                }
                cursor = cursor.plusMinutes(slotSize);
            }
        }

        if (candidatos.isEmpty()) {
            return buscarEnDiasProximos(cita, alternativos, agendaDAO, bloqueoDAO, 14);
        }

        candidatos.sort(Comparator.comparingLong(
                s -> Math.abs(s.hora().toSecondOfDay() - cita.getHoraInicio().toSecondOfDay())));
        return candidatos.get(0);
    }

    private SlotReasignado buscarEnDiasProximos(Cita cita, List<Medico> alternativos,
                                                  AgendaMedicaDAO agendaDAO, BloqueoAgendaDAO bloqueoDAO,
                                                  int maxDias) {
        List<SlotReasignado> candidatos = new ArrayList<>();
        int duracion = cita.getDuracionMinutos();

        for (int offset = 1; offset <= maxDias; offset++) {
            LocalDate fechaCandidata = cita.getFecha().plusDays(offset);
            int diaSemana = fechaCandidata.getDayOfWeek().getValue();

            for (Medico medico : alternativos) {
                AgendaMedica agenda = agendaDAO.obtenerPorMedicoYDia(medico.getId(), diaSemana);
                if (agenda == null) continue;

                List<Cita> ocupadas = citaDAO.obtenerActivasPorMedicoYFecha(medico.getId(), fechaCandidata);
                List<BloqueoAgenda> bloqueos = bloqueoDAO.obtenerPorMedicoYFecha(medico.getId(), fechaCandidata);

                LocalTime cursor = agenda.getHoraInicio();
                int slotSize = agenda.getSlotMinutos();
                LocalTime limite = agenda.getHoraFin();

                while (!cursor.isAfter(limite.minusMinutes(duracion))) {
                    if (!tieneSolapamiento(cursor, duracion, ocupadas, bloqueos)) {
                        candidatos.add(new SlotReasignado(medico, fechaCandidata, cursor));
                    }
                    cursor = cursor.plusMinutes(slotSize);
                }
            }

            if (!candidatos.isEmpty()) {
                candidatos.sort(Comparator.comparing(
                        s -> Math.abs(s.fecha().toEpochDay() - cita.getFecha().toEpochDay())));
                return candidatos.get(0);
            }
        }

        return null;
    }

    private boolean tieneSolapamiento(LocalTime inicio, int duracion,
                                       List<Cita> ocupadas, List<BloqueoAgenda> bloqueos) {
        LocalTime fin = inicio.plusMinutes(duracion);

        for (Cita c : ocupadas) {
            LocalTime cInicio = c.getHoraInicio();
            LocalTime cFin = cInicio.plusMinutes(c.getDuracionMinutos());
            if (inicio.isBefore(cFin) && cInicio.isBefore(fin)) return true;
        }

        for (BloqueoAgenda b : bloqueos) {
            LocalTime bInicio = b.getHoraInicio() != null ? b.getHoraInicio() : LocalTime.MIN;
            LocalTime bFin = b.getHoraFin() != null ? b.getHoraFin() : LocalTime.MAX;
            if (inicio.isBefore(bFin) && bInicio.isBefore(fin)) return true;
        }

        return false;
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
