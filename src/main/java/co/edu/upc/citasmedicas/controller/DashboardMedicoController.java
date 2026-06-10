package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.dao.AgendaMedicaDAO;
import co.edu.upc.citasmedicas.dao.HistorialClinicoDAO;
import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.model.AgendaMedica;
import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.OrigenCita;
import co.edu.upc.citasmedicas.enums.ServicioCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.HistorialClinico;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.DisponibilidadService;
import co.edu.upc.citasmedicas.service.InasistenciaService;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.service.ValidacionService;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.Separator;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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

public class DashboardMedicoController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblEspecialidad;
    @FXML private Label lblContador;
    @FXML private TableView<Cita> tablaAgenda;
    @FXML private TableColumn<Cita, String> colPaciente;
    @FXML private TableColumn<Cita, String> colTipoDocPac;
    @FXML private TableColumn<Cita, String> colDocPac;
    @FXML private TableColumn<Cita, String> colEmailPac;
    @FXML private TableColumn<Cita, String> colDireccionPac;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colHora;
    @FXML private TableColumn<Cita, String> colServicio;
    @FXML private TableColumn<Cita, String> colTipo;
    @FXML private TableColumn<Cita, String> colMotivo;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private TextField searchCitas;
    @FXML private Label lblMensaje;
    @FXML private VBox calendarContainer;

    private final CitaService citaService = new CitaService();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final CitaDAO citaDAO = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final AgendaMedicaDAO agendaMedicaDAO = new AgendaMedicaDAO();
    private final HistorialClinicoDAO historialDAO = new HistorialClinicoDAO();
    private final DisponibilidadService disponibilidadService = new DisponibilidadService();
    private CalendarView calendarView;
    private CalendarSource calendarSource;
    private ObservableList<Cita> listaCitas = FXCollections.observableArrayList();
    private FilteredList<Cita> filteredCitas;
    @FXML
    public void initialize() {
        Medico medico = (Medico) Session.getUsuarioActual();
        lblBienvenida.setText("Dr(a). " + medico.getNombre() + " " + medico.getApellido());
        lblEspecialidad.setText(medico.getEspecialidad().getNombre());

        colPaciente.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPaciente().getNombre() + " " + d.getValue().getPaciente().getApellido()));
        colTipoDocPac.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaciente().getTipoDocumento()));
        colDocPac.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaciente().getNumeroDocumento()));
        colEmailPac.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaciente().getEmail()));
        colDireccionPac.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaciente().getDireccion()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getServicio().getNombre()));
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().getNombre()));
        colMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        tablaAgenda.setPlaceholder(new Label("No hay citas para hoy"));

        filteredCitas = new FilteredList<>(listaCitas, p -> true);
        SortedList<Cita> sorted = new SortedList<>(filteredCitas);
        sorted.comparatorProperty().bind(tablaAgenda.comparatorProperty());
        tablaAgenda.setItems(sorted);

        searchCitas.textProperty().addListener((obs, oldV, newV) -> {
            String filtro = newV == null ? "" : newV.toLowerCase();
            filteredCitas.setPredicate(cita -> {
                if (filtro.isEmpty()) return true;
                return cita.getPaciente().getNombre().toLowerCase().contains(filtro)
                    || cita.getPaciente().getApellido().toLowerCase().contains(filtro)
                    || cita.getPaciente().getNumeroDocumento().toLowerCase().contains(filtro)
                    || cita.getPaciente().getEmail().toLowerCase().contains(filtro)
                    || cita.getPaciente().getDireccion().toLowerCase().contains(filtro)
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
        cargarAgenda();
        aplicarColorFilas(tablaAgenda);
        iniciarDeteccionInasistencias();
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

        calendarSource = new CalendarSource("Agenda");
        calendarSource.getCalendars().addAll(pendientes, confirmadas, completadas, canceladas);
        calendarView.getCalendarSources().setAll(calendarSource);

        calendarView.setRequestedTime(LocalTime.now());
        // calendarView.setShowSourceButton(false); // not in this CalendarFX version

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
            listaCitas.setAll(citaService.agendaDelMedico(medico.getId()));
            cargarCalendario();
            actualizarContador();
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar agenda.");
        }
    }

    private void actualizarContador() {
        int total = filteredCitas.size();
        int pendientes = (int) listaCitas.stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count();
        lblContador.setText(total + " citas | " + pendientes + " pendientes");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void conectarTooltips() {
        for (TableColumn col : new TableColumn[]{
                colPaciente, colTipoDocPac, colDocPac, colEmailPac, colDireccionPac,
                colFecha, colHora, colServicio, colTipo, colMotivo, colEstado
        }) {
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
    private void handleAtender() {
        Cita sel = tablaAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona una cita.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Marcar como completada la cita de " + sel.getPaciente().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        ViewManager.styleAlert(dialogo);
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
    private void handleNoAsistio() {
        Cita sel = tablaAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona una cita.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Marcar como inasistencia la cita de " + sel.getPaciente().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        ViewManager.styleAlert(dialogo);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    citaService.marcarNoAsistio(sel.getId());
                    mostrarExito(lblMensaje, "Cita marcada como inasistencia.");
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
    private void handleGestionarHorarios() {
        Medico medico = (Medico) Session.getUsuarioActual();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mis Horarios");
        dialog.setHeaderText("Gestionar horarios de atencion");

        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(btnCerrar);
        ViewManager.styleDialog(dialog);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        TableView<AgendaMedica> tablaHorarios = new TableView<>();
        tablaHorarios.setPlaceholder(new Label("No hay horarios configurados"));
        tablaHorarios.setPrefHeight(200);

        TableColumn<AgendaMedica, String> colDia = new TableColumn<>("Dia");
        colDia.setPrefWidth(100);
        colDia.setCellValueFactory(d -> {
            String[] dias = {"", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"};
            return new SimpleStringProperty(dias[d.getValue().getDiaSemana()]);
        });

        TableColumn<AgendaMedica, String> colInicio = new TableColumn<>("Inicio");
        colInicio.setPrefWidth(80);
        colInicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));

        TableColumn<AgendaMedica, String> colFin = new TableColumn<>("Fin");
        colFin.setPrefWidth(80);
        colFin.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraFin().toString()));

        TableColumn<AgendaMedica, String> colSlot = new TableColumn<>("Slot (min)");
        colSlot.setPrefWidth(80);
        colSlot.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSlotMinutos())));

        tablaHorarios.getColumns().addAll(List.of(colDia, colInicio, colFin, colSlot));

        ObservableList<AgendaMedica> listaHorarios = FXCollections.observableArrayList();
        listaHorarios.setAll(agendaMedicaDAO.listarPorMedico(medico.getId()));
        tablaHorarios.setItems(listaHorarios);

        Separator sep = new Separator();

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<String> cbDia = new ComboBox<>();
        cbDia.setItems(FXCollections.observableArrayList(
                "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado", "Domingo"));
        cbDia.setPromptText("Dia");

        ComboBox<String> cbHoraInicio = new ComboBox<>();
        cbHoraInicio.setPromptText("Inicio");
        cbHoraInicio.setItems(FXCollections.observableArrayList(
                "06:00","06:30","07:00","07:30","08:00","08:30","09:00","09:30",
                "10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30",
                "14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00"));

        ComboBox<String> cbHoraFin = new ComboBox<>();
        cbHoraFin.setPromptText("Fin");
        cbHoraFin.setItems(FXCollections.observableArrayList(
                "06:00","06:30","07:00","07:30","08:00","08:30","09:00","09:30",
                "10:00","10:30","11:00","11:30","12:00","12:30","13:00","13:30",
                "14:00","14:30","15:00","15:30","16:00","16:30","17:00","17:30","18:00","18:30","19:00","19:30","20:00"));

        ComboBox<Integer> cbSlot = new ComboBox<>();
        cbSlot.setItems(FXCollections.observableArrayList(15, 20, 30, 45, 60));
        cbSlot.setPromptText("Slot (min)");

        form.addRow(0, new Label("Dia:"), cbDia, new Label("Inicio:"), cbHoraInicio);
        form.addRow(1, new Label("Fin:"), cbHoraFin, new Label("Slot:"), cbSlot);

        HBox btnRow = new HBox(10);
        Button btnAgregar = new Button("Agregar");
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setDisable(true);
        btnRow.getChildren().addAll(btnAgregar, btnEliminar);

        tablaHorarios.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            btnEliminar.setDisable(sel == null);
        });

        btnAgregar.setOnAction(e -> {
            Integer diaIdx = cbDia.getSelectionModel().getSelectedIndex();
            if (diaIdx < 0) { mostrarError(lblMensaje, "Selecciona un dia."); return; }
            int dia = diaIdx + 1;
            String hIni = cbHoraInicio.getValue();
            String hFin = cbHoraFin.getValue();
            Integer slot = cbSlot.getValue();
            if (hIni == null || hFin == null || slot == null) {
                mostrarError(lblMensaje, "Completa inicio, fin y slot."); return;
            }
            LocalTime ini = LocalTime.parse(hIni);
            LocalTime fin = LocalTime.parse(hFin);
            if (!fin.isAfter(ini)) { mostrarError(lblMensaje, "La hora fin debe ser posterior a inicio."); return; }

            AgendaMedica existente = agendaMedicaDAO.obtenerPorMedicoYDia(medico.getId(), dia);
            if (existente != null) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Ya existe un horario para este dia. Sobrescribir?", ButtonType.YES, ButtonType.NO);
                ViewManager.styleAlert(a);
                if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
                agendaMedicaDAO.eliminar(existente.getId());
            }

            agendaMedicaDAO.guardar(new AgendaMedica(
                    UUID.randomUUID().toString().substring(0, 8),
                    medico.getId(), dia, ini, fin, slot));
            listaHorarios.setAll(agendaMedicaDAO.listarPorMedico(medico.getId()));
            mostrarExito(lblMensaje, "Horario agregado.");
        });

        btnEliminar.setOnAction(e -> {
            AgendaMedica sel = tablaHorarios.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Eliminar el horario del dia seleccionado?", ButtonType.YES, ButtonType.NO);
            ViewManager.styleAlert(a);
            if (a.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) return;
            agendaMedicaDAO.eliminar(sel.getId());
            listaHorarios.setAll(agendaMedicaDAO.listarPorMedico(medico.getId()));
            mostrarExito(lblMensaje, "Horario eliminado.");
        });

        content.getChildren().addAll(tablaHorarios, sep, form, btnRow);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    @FXML
    private void handleIniciarConsulta() {
        Cita sel = tablaAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona una cita para iniciar consulta.");
            return;
        }
        if (sel.getEstado() != EstadoCita.CONFIRMADA && sel.getEstado() != EstadoCita.PENDIENTE) {
            mostrarError(lblMensaje, "Solo se puede iniciar consulta en citas pendientes o confirmadas.");
            return;
        }

        Dialog<HistorialClinico> dialog = new Dialog<>();
        dialog.setTitle("Historial Clinico");
        dialog.setHeaderText("Consulta de " + sel.getPaciente().getNombre() + " " + sel.getPaciente().getApellido()
                + " - " + sel.getServicio().getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar y finalizar consulta", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        ViewManager.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField txtDiagnostico = new TextField();
        txtDiagnostico.setPromptText("Diagnostico principal");

        TextArea txtEnfermedadActual = new TextArea();
        txtEnfermedadActual.setPromptText("Enfermedad actual");
        txtEnfermedadActual.setPrefRowCount(3);

        TextArea txtReceta = new TextArea();
        txtReceta.setPromptText("Receta / Medicamentos");
        txtReceta.setPrefRowCount(3);

        TextField txtRemision = new TextField();
        txtRemision.setPromptText("Remision a especialidad (opcional)");

        TextArea txtNotas = new TextArea();
        txtNotas.setPromptText("Notas adicionales");
        txtNotas.setPrefRowCount(3);

        grid.add(new Label("Diagnostico:"), 0, 0);
        grid.add(txtDiagnostico, 1, 0);
        grid.add(new Label("Enfermedad actual:"), 0, 1);
        grid.add(txtEnfermedadActual, 1, 1);
        grid.add(new Label("Receta:"), 0, 2);
        grid.add(txtReceta, 1, 2);
        grid.add(new Label("Remision:"), 0, 3);
        grid.add(txtRemision, 1, 3);
        grid.add(new Label("Notas:"), 0, 4);
        grid.add(txtNotas, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                String diagnostico = txtDiagnostico.getText().trim();
                if (diagnostico.isEmpty()) {
                    mostrarError(lblMensaje, "El diagnostico es obligatorio.");
                    return null;
                }
                return new HistorialClinico(
                        UUID.randomUUID().toString().substring(0, 8),
                        sel.getId(),
                        sel.getMedico().getId(),
                        sel.getPaciente().getId(),
                        LocalDate.now(),
                        diagnostico,
                        txtEnfermedadActual.getText().trim(),
                        txtReceta.getText().trim(),
                        txtRemision.getText().trim(),
                        txtNotas.getText().trim()
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(historial -> {
            try {
                historialDAO.guardar(historial);
                citaService.completarCita(sel.getId());
                mostrarExito(lblMensaje, "Consulta finalizada. Historial clinico guardado.");
                cargarAgenda();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleModificarMisDatos() {
        Medico medico = (Medico) Session.getUsuarioActual();

        Dialog<Medico> dialog = new Dialog<>();
        dialog.setTitle("Mis datos");
        dialog.setHeaderText("Modifica tus datos personales");

        ButtonType btnGuardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        ViewManager.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField nombre = new TextField(medico.getNombre());
        TextField apellido = new TextField(medico.getApellido());
        TextField email = new TextField(medico.getEmail());
        TextField telefono = new TextField(medico.getTelefono());
        TextField registro = new TextField(medico.getRegistroMedico());
        ComboBox<String> especialidad = new ComboBox<>();
        especialidad.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(Especialidad.values()).map(Especialidad::getNombre).toList()
        ));
        especialidad.setValue(medico.getEspecialidad().getNombre());
        ComboBox<String> tipoDoc = new ComboBox<>();
        tipoDoc.setItems(FXCollections.observableArrayList("CC", "TI", "CE", "Pasaporte"));
        tipoDoc.setValue(medico.getTipoDocumento());
        TextField numDoc = new TextField(medico.getNumeroDocumento());
        DatePicker fechaNac = new DatePicker(medico.getFechaNacimiento());
        TextField direccion = new TextField(medico.getDireccion());
        TextField eps = new TextField(medico.getEps());

        grid.addRow(0, new Label("Nombre:"), nombre, new Label("Apellido:"), apellido);
        grid.addRow(1, new Label("Email:"), email, new Label("Telefono:"), telefono);
        grid.addRow(2, new Label("Registro:"), registro, new Label("Especialidad:"), especialidad);
        grid.addRow(3, new Label("Tipo doc:"), tipoDoc, new Label("Num doc:"), numDoc);
        grid.addRow(4, new Label("Fecha nac:"), fechaNac, new Label("Direccion:"), direccion);
        grid.addRow(5, new Label("EPS:"), eps);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                String nombreVal = nombre.getText().trim();
                String apellidoVal = apellido.getText().trim();
                String numDocVal = numDoc.getText().trim();
                String epsVal = eps.getText().trim();

                String errNom = ValidacionService.mensajeErrorNombre(nombreVal);
                if (errNom != null) { mostrarError(lblMensaje, errNom); return null; }
                String errApe = ValidacionService.mensajeErrorNombre(apellidoVal);
                if (errApe != null) { mostrarError(lblMensaje, errApe); return null; }
                String errDoc = ValidacionService.mensajeErrorNumeroDocumento(numDocVal);
                if (errDoc != null) { mostrarError(lblMensaje, errDoc); return null; }
                String errEps = ValidacionService.mensajeErrorEps(epsVal);
                if (errEps != null) { mostrarError(lblMensaje, errEps); return null; }

                String emailValMed = email.getText().trim().toLowerCase();
                String errEmailMed = ValidacionService.mensajeErrorEmail(emailValMed);
                if (errEmailMed != null) { mostrarError(lblMensaje, errEmailMed); return null; }

                try {
                    String espNombre = especialidad.getValue();
                    Especialidad esp = espNombre != null
                            ? Especialidad.fromNombre(espNombre)
                            : medico.getEspecialidad();
                    return new Medico(
                            medico.getId(),
                            nombreVal, apellidoVal,
                            emailValMed, medico.getPassword(),
                            telefono.getText().trim(),
                            registro.getText().trim(), esp,
                            tipoDoc.getValue(), numDocVal,
                            fechaNac.getValue(), direccion.getText().trim(), epsVal
                    );
                } catch (Exception e) {
                    mostrarError(lblMensaje, "Datos invalidos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(m -> {
            try {
                medicoDAO.actualizar(m);
                Session.setUsuarioActual(m);
                lblBienvenida.setText("Dr(a). " + m.getNombre() + " " + m.getApellido());
                lblEspecialidad.setText(m.getEspecialidad().getNombre());
                mostrarExito(lblMensaje, "Datos actualizados correctamente.");
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleReprogramarCita() {
        Cita sel = tablaAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona una cita para reprogramar.");
            return;
        }

        Dialog<Cita> dialog = new Dialog<>();
        dialog.setTitle("Reprogramar cita");
        dialog.setHeaderText("Reprogramando cita de " + sel.getPaciente().getNombre()
                + " " + sel.getPaciente().getApellido());

        ButtonType btnGuardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        ViewManager.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        DatePicker dpFecha = new DatePicker(sel.getFecha());

        ComboBox<String> cbHora = new ComboBox<>();
        cbHora.setPromptText("Selecciona hora");

        dpFecha.valueProperty().addListener((obs, old, fecha) -> {
            if (fecha != null) {
                Medico medico = (Medico) Session.getUsuarioActual();
                int duracion = sel.getDuracionMinutos();
                Task<List<LocalTime>> task = new Task<>() {
                    @Override
                    protected List<LocalTime> call() {
                        return disponibilidadService.obtenerHorasDisponibles(medico.getId(), fecha, duracion);
                    }
                };
                task.setOnSucceeded(e2 -> {
                    List<LocalTime> horas = task.getValue();
                    if (horas.isEmpty()) {
                        cbHora.getItems().clear();
                        cbHora.setPromptText("No hay horas disponibles");
                    } else {
                        cbHora.setItems(FXCollections.observableArrayList(
                                horas.stream().map(LocalTime::toString).collect(Collectors.toList())));
                        if (horas.contains(sel.getHoraInicio())) {
                            cbHora.setValue(sel.getHoraInicio().toString());
                        } else {
                            cbHora.getSelectionModel().selectFirst();
                        }
                    }
                });
                new Thread(task).start();
            }
        });

        Platform.runLater(() -> {
            if (dpFecha.getValue() != null) {
                LocalDate tmp = dpFecha.getValue();
                dpFecha.setValue(null);
                dpFecha.setValue(tmp);
            }
        });

        ComboBox<String> cbTipo = new ComboBox<>();
        cbTipo.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(TipoCita.values()).map(TipoCita::getNombre).collect(Collectors.toList())));
        cbTipo.setValue(sel.getTipo().getNombre());

        TextField txtMotivo = new TextField(sel.getMotivo());

        grid.addRow(0, new Label("Paciente:"), new Label(sel.getPaciente().getNombre() + " " + sel.getPaciente().getApellido()));
        grid.addRow(1, new Label("Servicio:"), new Label(sel.getServicio().getNombre()));
        grid.addRow(2, new Label("Fecha:"), dpFecha, new Label("Hora:"), cbHora);
        grid.addRow(3, new Label("Modalidad:"), cbTipo, new Label("Motivo:"), txtMotivo);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    String tipoNombre = cbTipo.getValue();
                    TipoCita tipo = TipoCita.fromNombre(tipoNombre);
                    return new Cita(
                            sel.getId(),
                            sel.getPaciente(), sel.getMedico(),
                            sel.getServicio(),
                            dpFecha.getValue(), LocalTime.parse(cbHora.getValue()),
                            sel.getDuracionMinutos(),
                            tipo,
                            txtMotivo.getText().trim(),
                            sel.getOrigen()
                    );
                } catch (Exception e) {
                    mostrarError(lblMensaje, "Datos invalidos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cita -> {
            try {
                citaDAO.actualizarCita(cita);
                mostrarExito(lblMensaje, "Cita reprogramada correctamente.");
                cargarAgenda();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleAgendarControl() {
        Medico medico = (Medico) Session.getUsuarioActual();

        Dialog<Cita> dialog = new Dialog<>();
        dialog.setTitle("Agendar cita de control");
        dialog.setHeaderText("Agendar cita de control para Dr(a). " + medico.getNombre() + " " + medico.getApellido());

        ButtonType btnAgendar = new ButtonType("Agendar control", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgendar, ButtonType.CANCEL);
        ViewManager.styleDialog(dialog);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        List<Paciente> pacientes = pacienteDAO.obtenerTodos();
        Map<String, String> mapaPacientes = new LinkedHashMap<>();
        for (Paciente p : pacientes) {
            String etiqueta = p.getNombre() + " " + p.getApellido() + " (" + p.getNumeroDocumento() + ")";
            mapaPacientes.put(etiqueta, p.getId());
        }
        ComboBox<String> cbPaciente = new ComboBox<>();
        cbPaciente.setItems(FXCollections.observableArrayList(mapaPacientes.keySet()));
        cbPaciente.setPromptText("Selecciona paciente");

        ComboBox<String> cbCategoria = new ComboBox<>();
        List<String> grupos = ServicioCita.grupos().stream()
                .filter(g -> ServicioCita.porGrupo(g).stream()
                        .anyMatch(s -> s.getEspecialidadRequerida() == medico.getEspecialidad()))
                .collect(Collectors.toList());
        cbCategoria.setItems(FXCollections.observableArrayList(grupos));
        cbCategoria.setPromptText("Selecciona categoria");

        ComboBox<String> cbServicio = new ComboBox<>();
        cbServicio.setPromptText("Selecciona servicio");
        cbServicio.setDisable(true);

        cbCategoria.valueProperty().addListener((obs, old, grupo) -> {
            if (grupo != null) {
                List<String> servicios = ServicioCita.porGrupo(grupo).stream()
                        .filter(s -> s.getEspecialidadRequerida() == medico.getEspecialidad())
                        .map(ServicioCita::getNombre).collect(Collectors.toList());
                cbServicio.setItems(FXCollections.observableArrayList(servicios));
                cbServicio.setDisable(false);
                cbServicio.setValue(null);
            }
        });

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha");
        dpFecha.setDisable(true);

        ComboBox<String> cbHora = new ComboBox<>();
        cbHora.setPromptText("Hora");
        cbHora.setDisable(true);

        cbServicio.valueProperty().addListener((obs, old, sn) -> {
            if (sn != null) {
                dpFecha.setDisable(false);
                dpFecha.setValue(null);
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            } else {
                dpFecha.setDisable(true);
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            }
        });

        dpFecha.valueProperty().addListener((obs, old, fecha) -> {
            if (fecha != null && cbServicio.getValue() != null) {
                ServicioCita serv = ServicioCita.fromNombre(cbServicio.getValue());

                int duracion = serv.getDuracionControlMinutos();
                Task<List<LocalTime>> task = new Task<>() {
                    @Override
                    protected List<LocalTime> call() {
                        return disponibilidadService.obtenerHorasDisponibles(medico.getId(), fecha, duracion);
                    }
                };
                task.setOnSucceeded(e2 -> {
                    List<LocalTime> horas = task.getValue();
                    if (horas.isEmpty()) {
                        cbHora.getItems().clear();
                        cbHora.setPromptText("No hay horas disponibles");
                        cbHora.setDisable(true);
                    } else {
                        cbHora.setItems(FXCollections.observableArrayList(
                                horas.stream().map(LocalTime::toString).collect(Collectors.toList())));
                        cbHora.setDisable(false);
                        cbHora.getSelectionModel().selectFirst();
                    }
                });
                task.setOnFailed(e2 -> {
                    cbHora.getItems().clear();
                    cbHora.setPromptText("Error al cargar horas");
                    cbHora.setDisable(true);
                });
                new Thread(task).start();
            } else {
                cbHora.setDisable(true);
                cbHora.getItems().clear();
            }
        });

        ComboBox<String> cbTipo = new ComboBox<>();
        cbTipo.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(TipoCita.values()).map(TipoCita::getNombre).collect(Collectors.toList())));
        cbTipo.setValue("Presencial");

        CheckBox chkSobrecupo = new CheckBox("Sobrecupo (saltar disponibilidad)");

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Motivo del control");

        grid.addRow(0, new Label("Paciente:"), cbPaciente);
        grid.addRow(1, new Label("Categoria:"), cbCategoria, new Label("Servicio:"), cbServicio);
        grid.addRow(2, new Label("Fecha:"), dpFecha, new Label("Hora:"), cbHora);
        grid.addRow(3, new Label("Modalidad:"), cbTipo, new Label("Motivo:"), txtMotivo);
        grid.addRow(4, chkSobrecupo);

        dialog.getDialogPane().setContent(grid);

        boolean[] esSobrecupo = {false};

        dialog.setResultConverter(btn -> {
            if (btn == btnAgendar) {
                String pacKey = cbPaciente.getValue();
                String servicioNombre = cbServicio.getValue();
                LocalDate fecha = dpFecha.getValue();
                String hora = cbHora.getValue();
                String tipoNombre = cbTipo.getValue();
                String motivo = txtMotivo.getText();

                if (pacKey == null || servicioNombre == null || fecha == null || hora == null) {
                    return null;
                }

                esSobrecupo[0] = chkSobrecupo.isSelected();

                try {
                    ServicioCita serv = ServicioCita.fromNombre(servicioNombre);

                    String pacienteId = mapaPacientes.get(pacKey);
                    Paciente paciente = pacienteDAO.buscarPorId(pacienteId);

                    if (paciente == null) return null;

                    TipoCita tipo = TipoCita.fromNombre(tipoNombre);

                    return new Cita(
                            UUID.randomUUID().toString().substring(0, 8),
                            paciente, medico,
                            serv,
                            fecha, LocalTime.parse(hora),
                            serv.getDuracionControlMinutos(),
                            tipo,
                            motivo == null || motivo.isBlank() ? "Control medico" : motivo.trim(),
                            OrigenCita.CONTROL.name()
                    );
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cita -> {
            try {
                if (esSobrecupo[0]) {
                    citaService.agendarSobrecupo(cita);
                    mostrarExito(lblMensaje, "Sobrecupo de control agendado correctamente.");
                } else {
                    citaService.agendarCita(cita);
                    mostrarExito(lblMensaje, "Cita de control agendada correctamente.");
                }
                cargarAgenda();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    private void iniciarDeteccionInasistencias() {
        InasistenciaService.getInstance().iniciar(citaService);
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Kairos - Sistema de Gestion de Citas");
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
