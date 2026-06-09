package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class DashboardMedicoController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblEspecialidad;
    @FXML private TableView<Cita> tablaAgenda;
    @FXML private TableColumn<Cita, String> colPaciente;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colTipo;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private Label lblMensaje;
    @FXML private VBox calendarContainer;

    private final CitaService citaService = new CitaService();
    private CalendarView calendarView;

    @FXML
    public void initialize() {
        Medico medico = (Medico) Session.getUsuarioActual();
        lblBienvenida.setText("Dr(a). " + medico.getNombre() + " " + medico.getApellido());
        lblEspecialidad.setText(medico.getEspecialidad().getNombre());

        colPaciente.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPaciente().getNombre() + " " + d.getValue().getPaciente().getApellido()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().name()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        inicializarCalendario();
        cargarAgenda();
        aplicarColorFilas(tablaAgenda);
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

        CalendarSource source = new CalendarSource("Agenda");
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
            Medico medico = (Medico) Session.getUsuarioActual();
            List<Cita> citas = citaService.agendaDelMedico(medico.getId());
            for (Cita c : citas) {
                agregarEntryCalendario(c, pendientes, confirmadas, completadas, canceladas);
            }
        } catch (RuntimeException e) {
            mostrarError(lblMensaje, "Error al cargar calendario.");
        }
    }

    private void agregarEntryCalendario(Cita c, Calendar<?> pend, Calendar<?> conf, Calendar<?> comp, Calendar<?> canc) {
        String title = c.getPaciente().getNombre() + " " + c.getPaciente().getApellido()
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

    private void cargarAgenda() {
        try {
            Medico medico = (Medico) Session.getUsuarioActual();
            tablaAgenda.setItems(FXCollections.observableArrayList(
                    citaService.agendaDelMedico(medico.getId())));
            cargarCalendario();
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar agenda.");
        }
    }

    @FXML
    private void handleAtender() {
        Cita sel = tablaAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona una cita.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Marcar como completada la cita de " + sel.getPaciente().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    citaService.atenderCita(sel.getId());
                    mostrarExito(lblMensaje, "Cita marcada como completada.");
                    cargarAgenda();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensaje, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleActualizar() {
        cargarAgenda();
        mostrarExito(lblMensaje, "Agenda actualizada.");
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
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
