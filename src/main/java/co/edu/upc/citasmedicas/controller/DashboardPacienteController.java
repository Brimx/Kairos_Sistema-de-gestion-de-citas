package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.OrigenCita;
import co.edu.upc.citasmedicas.enums.ServicioCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.DisponibilidadService;
import co.edu.upc.citasmedicas.service.InasistenciaService;
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
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.stream.Collectors;

public class DashboardPacienteController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblContador;
    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colMedico;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colServicio;
    @FXML private TableColumn<Cita, String> colTipo;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private TextField searchCitas;

    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbServicio;
    @FXML private ComboBox<String> cbMedico;
    @FXML private DatePicker dateFecha;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtMotivo;

    @FXML private Label lblMensaje;
    @FXML private VBox calendarContainer;

    private ObservableList<Cita> listaCitas = FXCollections.observableArrayList();
    private FilteredList<Cita> filteredCitas;

    private final CitaService citaService = new CitaService();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DisponibilidadService disponibilidadService = new DisponibilidadService();
    private final Map<String, Medico> mapaMedicos = new LinkedHashMap<>();
    private ServicioCita servicioSeleccionado;
    private CalendarView calendarView;
    private CalendarSource calendarSource;

    @FXML
    public void initialize() {
        Paciente paciente = (Paciente) Session.getUsuarioActual();
        lblBienvenida.setText("Bienvenido, " + paciente.getNombre() + " " + paciente.getApellido());

        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colMedico.setCellValueFactory(d -> new SimpleStringProperty(
                "Dr. " + d.getValue().getMedico().getNombre() + " " + d.getValue().getMedico().getApellido()));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getServicio().getNombre()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().getNombre()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        cbTipo.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(TipoCita.values()).map(TipoCita::getNombre).collect(Collectors.toList())));
        cbTipo.setValue("Presencial");

        cbHora.setDisable(true);
        dateFecha.setDisable(true);

        cbCategoria.setItems(FXCollections.observableArrayList(ServicioCita.grupos()));
        cbCategoria.valueProperty().addListener((obs, old, grupo) -> {
            if (grupo != null) {
                List<String> servicios = ServicioCita.porGrupo(grupo).stream()
                        .map(ServicioCita::getNombre).collect(Collectors.toList());
                cbServicio.setItems(FXCollections.observableArrayList(servicios));
                cbServicio.setValue(null);
                cbMedico.setItems(FXCollections.observableArrayList());
                cbMedico.setValue(null);
            }
        });

        cbServicio.valueProperty().addListener((obs, old, servicioNombre) -> {
            if (servicioNombre != null) {
                servicioSeleccionado = ServicioCita.fromNombre(servicioNombre);
                filtrarMedicosPorEspecialidad(servicioSeleccionado.getEspecialidadRequerida());
                cbMedico.setDisable(false);
                dateFecha.setDisable(true);
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            } else {
                servicioSeleccionado = null;
                cbMedico.setDisable(true);
                dateFecha.setDisable(true);
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            }
        });

        cbMedico.valueProperty().addListener((obs, old, medicoKey) -> {
            if (medicoKey != null && servicioSeleccionado != null) {
                dateFecha.setDisable(false);
                dateFecha.setValue(null);
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            } else {
                dateFecha.setDisable(true);
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            }
        });

        dateFecha.valueProperty().addListener((obs, old, fecha) -> {
            if (fecha != null && cbMedico.getValue() != null && servicioSeleccionado != null) {
                cargarHorasDisponibles(fecha);
            } else {
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            }
        });

        tablaCitas.setPlaceholder(new Label("No tienes citas agendadas"));

        filteredCitas = new FilteredList<>(listaCitas, p -> true);
        SortedList<Cita> sorted = new SortedList<>(filteredCitas);
        sorted.comparatorProperty().bind(tablaCitas.comparatorProperty());
        tablaCitas.setItems(sorted);

        searchCitas.textProperty().addListener((obs, oldV, newV) -> {
            String filtro = newV == null ? "" : newV.toLowerCase();
            filteredCitas.setPredicate(cita -> {
                if (filtro.isEmpty()) return true;
                return cita.getMedico().getNombre().toLowerCase().contains(filtro)
                    || cita.getMedico().getApellido().toLowerCase().contains(filtro)
                    || cita.getFecha().toString().contains(filtro)
                    || cita.getEstado().name().toLowerCase().contains(filtro)
                    || cita.getServicio().getNombre().toLowerCase().contains(filtro)
                    || cita.getTipo().getNombre().toLowerCase().contains(filtro)
                    || cita.getMotivo().toLowerCase().contains(filtro);
            });
            actualizarContador();
        });

        conectarTooltips();
        inicializarCalendario();
        cargarMedicos();
        cargarMisCitas();
        aplicarColorFilas(tablaCitas);
        iniciarDeteccionInasistencias();
    }

    private void filtrarMedicosPorEspecialidad(co.edu.upc.citasmedicas.enums.Especialidad esp) {
        cbMedico.setItems(FXCollections.observableArrayList());
        mapaMedicos.clear();
        try {
            List<Medico> medicos = medicoDAO.obtenerTodos().stream()
                    .filter(m -> m.getEspecialidad() == esp)
                    .toList();
            for (Medico medico : medicos) {
                String etiqueta = medico.getNombre() + " " + medico.getApellido()
                        + " (" + medico.getEspecialidad().getNombre() + ")";
                mapaMedicos.put(etiqueta, medico);
            }
            cbMedico.setItems(FXCollections.observableArrayList(mapaMedicos.keySet()));
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar medicos.");
        }
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

        calendarSource = new CalendarSource("Mis citas");
        calendarSource.getCalendars().addAll(pendientes, confirmadas, completadas, canceladas);
        calendarView.getCalendarSources().setAll(calendarSource);

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
        try {
            calendarSource.getCalendars().forEach(cal -> cal.clear());

            Calendar<?> pendientes = calendarSource.getCalendars().get(0);
            Calendar<?> confirmadas = calendarSource.getCalendars().get(1);
            Calendar<?> completadas = calendarSource.getCalendars().get(2);
            Calendar<?> canceladas = calendarSource.getCalendars().get(3);

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
                getStyleClass().removeAll("row-pendiente", "row-confirmada", "row-completada",
                        "row-cancelada", "row-noasistio", "row-sobrecupo");
                if (item == null || empty) return;
                String css = switch (item.getEstado()) {
                    case PENDIENTE -> "row-pendiente";
                    case CONFIRMADA -> "row-confirmada";
                    case COMPLETADA -> "row-completada";
                    case CANCELADA -> "row-cancelada";
                    case NO_ASISTIO -> "row-noasistio";
                };
                getStyleClass().add(css);
                if (item.isSobrecupo()) {
                    getStyleClass().add("row-sobrecupo");
                }
            }
        });
    }

    private void cargarHorasDisponibles(LocalDate fecha) {
        String medicoKey = cbMedico.getValue();
        if (medicoKey == null || servicioSeleccionado == null) return;

        Medico medico = mapaMedicos.get(medicoKey);
        if (medico == null) return;

        int duracion = servicioSeleccionado.getDuracionMinutos();

        Task<List<LocalTime>> task = new Task<>() {
            @Override
            protected List<LocalTime> call() {
                return disponibilidadService.obtenerHorasDisponibles(medico.getId(), fecha, duracion);
            }
        };

        task.setOnSucceeded(e -> {
            List<LocalTime> horas = task.getValue();
            if (horas.isEmpty()) {
                cbHora.setItems(FXCollections.observableArrayList());
                cbHora.setPromptText("No hay horas disponibles");
                cbHora.setDisable(true);
            } else {
                cbHora.setItems(FXCollections.observableArrayList(
                        horas.stream().map(LocalTime::toString).collect(Collectors.toList())));
                cbHora.setDisable(false);
                cbHora.getSelectionModel().selectFirst();
            }
        });

        task.setOnFailed(e -> {
            cbHora.setItems(FXCollections.observableArrayList());
            cbHora.setPromptText("Error al cargar horas");
            cbHora.setDisable(true);
        });

        new Thread(task).start();
    }

    private void cargarMedicos() {
        try {
            List<Medico> medicos = medicoDAO.obtenerTodos();
            mapaMedicos.clear();
            for (Medico medico : medicos) {
                String etiqueta = medico.getNombre() + " " + medico.getApellido()
                        + " (" + medico.getEspecialidad().getNombre() + ")";
                mapaMedicos.put(etiqueta, medico);
            }
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar medicos.");
        }
    }

    private void cargarMisCitas() {
        try {
            Paciente paciente = (Paciente) Session.getUsuarioActual();
            listaCitas.setAll(citaService.citasDelPaciente(paciente.getId()));
            cargarCalendario();
            actualizarContador();
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar tus citas.");
        }
    }

    private void actualizarContador() {
        int total = filteredCitas.size();
        int pendientes = (int) listaCitas.stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count();
        lblContador.setText(total + " citas | " + pendientes + " pendientes");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void conectarTooltips() {
        for (TableColumn col : new TableColumn[]{colFecha, colMedico, colHora, colServicio, colTipo, colEstado}) {
            col.setCellFactory(tc -> new TableCell() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setTooltip(null); return; }
                    setText(item.toString());
                    setTooltip(new Tooltip(item.toString()));
                }
            });
        }
    }

    @FXML
    private void handleAgendarCita() {
        lblMensaje.setText("");
        String servicioNombre = cbServicio.getValue();
        String medicoKey = cbMedico.getValue();
        LocalDate fecha = dateFecha.getValue();
        String hora = cbHora.getValue();
        String tipoNombre = cbTipo.getValue();
        String motivo = txtMotivo.getText() == null ? "" : txtMotivo.getText().trim();

        if (servicioNombre == null || medicoKey == null || fecha == null || hora == null || tipoNombre == null) {
            mostrarError(lblMensaje, "Completa servicio, medico, fecha y hora.");
            return;
        }

        if (cbHora.isDisabled()) {
            mostrarError(lblMensaje, "No hay horas disponibles para la fecha seleccionada.");
            return;
        }

        try {
            ServicioCita servicio = ServicioCita.fromNombre(servicioNombre);

            LocalTime horaInicio = LocalTime.parse(hora);
            Medico medico = mapaMedicos.get(medicoKey);
            Paciente paciente = (Paciente) Session.getUsuarioActual();

            if (paciente == null || medico == null) {
                mostrarError(lblMensaje, "No se pudo cargar paciente o medico.");
                return;
            }

            TipoCita tipo = TipoCita.fromNombre(tipoNombre);

            Cita cita = new Cita(
                    UUID.randomUUID().toString(),
                    paciente,
                    medico,
                    servicio,
                    fecha,
                    horaInicio,
                    servicio.getDuracionMinutos(),
                    tipo,
                    motivo.isBlank() ? "Consulta general" : motivo,
                    OrigenCita.PACIENTE.name()
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
    private void handleModificarMisDatos() {
        Paciente paciente = (Paciente) Session.getUsuarioActual();

        Dialog<Paciente> dialog = new Dialog<>();
        dialog.setTitle("Mis datos");
        dialog.setHeaderText("Modifica tus datos personales");

        ButtonType btnGuardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField nombre = new TextField(paciente.getNombre());
        TextField apellido = new TextField(paciente.getApellido());
        TextField email = new TextField(paciente.getEmail());
        TextField telefono = new TextField(paciente.getTelefono());
        TextField tipoDoc = new TextField(paciente.getTipoDocumento());
        TextField numDoc = new TextField(paciente.getNumeroDocumento());
        DatePicker fechaNac = new DatePicker(paciente.getFechaNacimiento());
        TextField direccion = new TextField(paciente.getDireccion());
        TextField eps = new TextField(paciente.getEps());

        grid.addRow(0, new Label("Nombre:"), nombre, new Label("Apellido:"), apellido);
        grid.addRow(1, new Label("Email:"), email, new Label("Telefono:"), telefono);
        grid.addRow(2, new Label("Tipo doc:"), tipoDoc, new Label("Num doc:"), numDoc);
        grid.addRow(3, new Label("Fecha nac:"), fechaNac, new Label("Direccion:"), direccion);
        grid.addRow(4, new Label("EPS:"), eps);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    return new Paciente(
                            paciente.getId(),
                            nombre.getText().trim(), apellido.getText().trim(),
                            email.getText().trim(), paciente.getPassword(),
                            telefono.getText().trim(),
                            tipoDoc.getText().trim(), numDoc.getText().trim(),
                            fechaNac.getValue(), direccion.getText().trim(), eps.getText().trim()
                    );
                } catch (Exception e) {
                    mostrarError(lblMensaje, "Datos invalidos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                pacienteDAO.actualizarTodo(p);
                Session.setUsuarioActual(p);
                lblBienvenida.setText("Bienvenido, " + p.getNombre() + " " + p.getApellido());
                mostrarExito(lblMensaje, "Datos actualizados correctamente.");
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Kairos - Sistema de Gestion de Citas");
    }

    private void limpiarFormulario() {
        cbCategoria.setValue(null);
        cbServicio.setValue(null);
        servicioSeleccionado = null;
        cbMedico.setValue(null);
        cbMedico.setDisable(true);
        dateFecha.setValue(null);
        dateFecha.setDisable(true);
        cbHora.setValue(null);
        cbHora.setDisable(true);
        cbHora.getItems().clear();
        txtMotivo.clear();
        cbTipo.setValue("Presencial");
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

    private void iniciarDeteccionInasistencias() {
        InasistenciaService.getInstance().iniciar(citaService);
    }
}
