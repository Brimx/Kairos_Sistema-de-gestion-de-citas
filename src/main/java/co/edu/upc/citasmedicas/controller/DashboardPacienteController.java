package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DashboardPacienteController {

    @FXML private Label lblBienvenida;
    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colMedico;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colTipo;
    @FXML private TableColumn<Cita, String> colEstado;

    @FXML private ComboBox<String> cbMedico;
    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtMotivo;

    @FXML private Label lblMensaje;
    @FXML private VBox calendarContainer;

    private final CitaService citaService = new CitaService();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final Map<String, String> mapaIdsMedicos = new LinkedHashMap<>();
    private CalendarView calendarView;

    @FXML
    public void initialize() {
        Paciente paciente = (Paciente) Session.getUsuarioActual();
        lblBienvenida.setText("Bienvenido, " + paciente.getNombre() + " " + paciente.getApellido());

        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colMedico.setCellValueFactory(d -> new SimpleStringProperty(
                "Dr. " + d.getValue().getMedico().getNombre() + " " + d.getValue().getMedico().getApellido()));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        cbTipo.setItems(FXCollections.observableArrayList("PRESENCIAL", "VIRTUAL"));
        cbTipo.setValue("PRESENCIAL");
        cbHora.setItems(FXCollections.observableArrayList(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00"
        ));

        inicializarCalendario();
        cargarMedicos();
        cargarMisCitas();
        aplicarColorFilas(tablaCitas);
    }

    private void inicializarCalendario() {
        calendarView = new CalendarView();

        Calendar<?> pendientes = new Calendar<>("Pendientes");
        Calendar<?> confirmadas = new Calendar<>("Confirmadas");
        Calendar<?> completadas = new Calendar<>("Completadas");
        Calendar<?> canceladas = new Calendar<>("Canceladas");

        pendientes.setStyle(Style.STYLE1);
        confirmadas.setStyle(Style.STYLE2);
        completadas.setStyle(Style.STYLE3);
        canceladas.setStyle(Style.STYLE4);

        CalendarSource source = new CalendarSource("Mis citas");
        source.getCalendars().addAll(pendientes, confirmadas, completadas, canceladas);
        calendarView.getCalendarSources().add(source);

        calendarView.setRequestedTime(LocalTime.now());

        Thread timeThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    calendarView.setToday(LocalDate.now());
                    calendarView.setTime(LocalTime.now());
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        timeThread.setDaemon(true);
        timeThread.start();

        calendarView.setPrefHeight(500);
        calendarContainer.getChildren().add(calendarView);
    }

    private void cargarCalendario() {
        CalendarSource source = calendarView.getCalendarSources().get(0);
        source.getCalendars().forEach(cal -> cal.clear());

        Calendar<?> pendientes = source.getCalendars().get(0);
        Calendar<?> confirmadas = source.getCalendars().get(1);
        Calendar<?> completadas = source.getCalendars().get(2);
        Calendar<?> canceladas = source.getCalendars().get(3);

        try {
            Paciente paciente = (Paciente) Session.getUsuarioActual();
            List<Cita> citas = citaService.citasDelPaciente(paciente.getId());
            for (Cita c : citas) {
                agregarEntryCalendario(c, pendientes, confirmadas, completadas, canceladas);
            }
        } catch (RuntimeException e) {
            mostrarError(lblMensaje, "Error al cargar calendario.");
        }
    }

    private void agregarEntryCalendario(Cita c, Calendar<?> pend, Calendar<?> conf, Calendar<?> comp, Calendar<?> canc) {
        String title = "Dr. " + c.getMedico().getNombre() + " " + c.getMedico().getApellido()
                + " - " + c.getMotivo();
        Entry<String> entry = new Entry<>(title);
        ZonedDateTime inicio = ZonedDateTime.of(c.getFecha(), c.getHoraInicio(), ZoneId.systemDefault());
        ZonedDateTime fin = ZonedDateTime.of(c.getFecha(), c.getHoraFin(), ZoneId.systemDefault());
        entry.setInterval(inicio, fin);

        Calendar<?> target = switch (c.getEstado()) {
            case CONFIRMADA -> conf;
            case COMPLETADA -> comp;
            case CANCELADA -> canc;
            default -> pend;
        };
        target.addEntry(entry);
    }

    private void aplicarColorFilas(TableView<Cita> tabla) {
        tabla.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Cita item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-pendiente", "row-confirmada", "row-completada", "row-cancelada");
                if (item == null || empty) return;
                String css = switch (item.getEstado()) {
                    case PENDIENTE -> "row-pendiente";
                    case CONFIRMADA -> "row-confirmada";
                    case COMPLETADA -> "row-completada";
                    case CANCELADA -> "row-cancelada";
                };
                getStyleClass().add(css);
            }
        });
    }

    private void cargarMedicos() {
        try {
            List<Medico> medicos = medicoDAO.obtenerTodos();
            mapaIdsMedicos.clear();
            for (Medico medico : medicos) {
                String etiqueta = medico.getNombre() + " " + medico.getApellido()
                        + " (" + medico.getEspecialidad().getNombre() + ")";
                mapaIdsMedicos.put(etiqueta, medico.getId());
            }
            cbMedico.setItems(FXCollections.observableArrayList(mapaIdsMedicos.keySet()));
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar medicos.");
        }
    }

    private void cargarMisCitas() {
        try {
            Paciente paciente = (Paciente) Session.getUsuarioActual();
            tablaCitas.setItems(FXCollections.observableArrayList(
                    citaService.citasDelPaciente(paciente.getId())));
            cargarCalendario();
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar tus citas.");
        }
    }

    @FXML
    private void handleAgendarCita() {
        lblMensaje.setText("");
        String medicoKey = cbMedico.getValue();
        LocalDate fecha = dateFecha.getValue();
        String hora = cbHora.getValue();
        String tipo = cbTipo.getValue();
        String motivo = txtMotivo.getText() == null ? "" : txtMotivo.getText().trim();

        if (medicoKey == null || fecha == null || hora == null || tipo == null) {
            mostrarError(lblMensaje, "Completa medico, fecha y hora.");
            return;
        }

        try {
            LocalTime horaInicio = LocalTime.parse(hora);
            String medicoId = mapaIdsMedicos.get(medicoKey);
            Paciente paciente = pacienteDAO.buscarPorId(Session.getUsuarioActual().getId());
            Medico medico = medicoDAO.buscarPorId(medicoId);

            if (paciente == null || medico == null) {
                mostrarError(lblMensaje, "No se pudo cargar paciente o medico.");
                return;
            }

            Cita cita = new Cita(
                    UUID.randomUUID().toString(),
                    paciente,
                    medico,
                    medico.getEspecialidad(),
                    fecha,
                    horaInicio,
                    TipoCita.valueOf(tipo),
                    motivo.isBlank() ? "Consulta general" : motivo
            );
            citaService.agendarCita(cita);
            mostrarExito(lblMensaje, "Cita agendada correctamente.");
            limpiarFormulario();
            cargarMisCitas();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarError(lblMensaje, exception.getMessage());
        }
    }

    @FXML
    private void handleCancelarCita() {
        Cita sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona una cita para cancelar.");
            return;
        }
        if (sel.getEstado() == EstadoCita.COMPLETADA) {
            mostrarError(lblMensaje, "No puedes cancelar una cita ya completada.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancelar la cita con " + sel.getMedico().getNombre() + " del " + sel.getFecha() + "?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    citaService.cancelarCita(sel.getId());
                    mostrarExito(lblMensaje, "Cita cancelada.");
                    cargarMisCitas();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensaje, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }

    private void limpiarFormulario() {
        cbMedico.setValue(null);
        dateFecha.setValue(null);
        cbHora.setValue(null);
        txtMotivo.clear();
        cbTipo.setValue("PRESENCIAL");
    }

    private void mostrarExito(Label label, String mensaje) {
        label.setText(mensaje);
        label.getStyleClass().removeAll("feedback-error");
        if (!label.getStyleClass().contains("feedback-ok")) {
            label.getStyleClass().add("feedback-ok");
        }
    }

    private void mostrarError(Label label, String mensaje) {
        label.setText(mensaje);
        label.getStyleClass().removeAll("feedback-ok");
        if (!label.getStyleClass().contains("feedback-error")) {
            label.getStyleClass().add("feedback-error");
        }
    }
}
