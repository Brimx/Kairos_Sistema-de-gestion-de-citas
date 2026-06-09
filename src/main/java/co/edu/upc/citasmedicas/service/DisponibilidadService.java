package co.edu.upc.citasmedicas.service;

import co.edu.upc.citasmedicas.dao.AgendaMedicaDAO;
import co.edu.upc.citasmedicas.dao.BloqueoAgendaDAO;
import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.model.AgendaMedica;
import co.edu.upc.citasmedicas.model.BloqueoAgenda;
import co.edu.upc.citasmedicas.model.Cita;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DisponibilidadService {

    private final AgendaMedicaDAO agendaMedicaDAO = new AgendaMedicaDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final BloqueoAgendaDAO bloqueoAgendaDAO = new BloqueoAgendaDAO();

    public List<LocalTime> obtenerHorasDisponibles(String medicoId, LocalDate fecha, int duracionMinutos) {
        int diaSemana = fecha.getDayOfWeek().getValue();
        AgendaMedica agenda = agendaMedicaDAO.obtenerPorMedicoYDia(medicoId, diaSemana);

        if (agenda == null) {
            return new ArrayList<>();
        }

        List<LocalTime> candidatos = new ArrayList<>();
        LocalTime cursor = agenda.getHoraInicio();
        int slotSize = agenda.getSlotMinutos();
        LocalTime limite = agenda.getHoraFin();

        while (!cursor.isAfter(limite.minusMinutes(duracionMinutos))) {
            candidatos.add(cursor);
            cursor = cursor.plusMinutes(slotSize);
        }

        List<Cita> ocupadas = citaDAO.obtenerActivasPorMedicoYFecha(medicoId, fecha);
        List<BloqueoAgenda> bloqueos = bloqueoAgendaDAO.obtenerPorMedicoYFecha(medicoId, fecha);

        return candidatos.stream()
                .filter(h -> !tieneSolapamiento(h, duracionMinutos, ocupadas, bloqueos))
                .collect(Collectors.toList());
    }

    private boolean tieneSolapamiento(LocalTime inicio, int duracion,
                                       List<Cita> ocupadas, List<BloqueoAgenda> bloqueos) {
        LocalTime fin = inicio.plusMinutes(duracion);

        for (Cita c : ocupadas) {
            LocalTime cInicio = c.getHoraInicio();
            LocalTime cFin = cInicio.plusMinutes(c.getDuracionMinutos());
            if (inicio.isBefore(cFin) && cInicio.isBefore(fin)) {
                return true;
            }
        }

        for (BloqueoAgenda b : bloqueos) {
            LocalTime bInicio = b.getHoraInicio() != null ? b.getHoraInicio() : LocalTime.MIN;
            LocalTime bFin = b.getHoraFin() != null ? b.getHoraFin() : LocalTime.MAX;
            if (inicio.isBefore(bFin) && bInicio.isBefore(fin)) {
                return true;
            }
        }

        return false;
    }
}
