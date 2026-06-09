package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.BloqueoAgendaDAO;
import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.OrigenCita;
import co.edu.upc.citasmedicas.enums.ServicioCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.BloqueoAgenda;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.model.Usuario;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.DisponibilidadService;
import co.edu.upc.citasmedicas.service.PacienteService;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
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
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;



public class DashboardAdminController {

    @FXML private TextField searchPacientes;
    @FXML private TextField searchMedicos;
    @FXML private TextField searchCitas;
    @FXML private TableView<Paciente> tablaPacientes;
    @FXML private TableColumn<Paciente, String> colPacNombre;
    @FXML private TableColumn<Paciente, String> colPacEmail;
    @FXML private TableColumn<Paciente, String> colPacTipoDoc;
    @FXML private TableColumn<Paciente, String> colPacDoc;
    @FXML private TableColumn<Paciente, String> colPacTel;
    @FXML private TableColumn<Paciente, String> colPacEps;

    @FXML private TableView<Medico> tablaMedicos;
    @FXML private TableColumn<Medico, String> colMedNombre;
    @FXML private TableColumn<Medico, String> colMedEmail;
    @FXML private TableColumn<Medico, String> colMedEsp;
    @FXML private TableColumn<Medico, String> colMedRegistro;
    @FXML private TableColumn<Medico, String> colMedTel;
    @FXML private TableColumn<Medico, String> colMedCons;

    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, String> colCitaPac;
    @FXML private TableColumn<Cita, String> colCitaMed;
    @FXML private TableColumn<Cita, String> colCitaServicio;
    @FXML private TableColumn<Cita, String> colCitaFecha;
    @FXML private TableColumn<Cita, String> colCitaHora;
    @FXML private TableColumn<Cita, String> colCitaTipo;
    @FXML private TableColumn<Cita, String> colCitaEstado;
    @FXML private TableColumn<Cita, String> colCitaMotivo;

    @FXML private Label lblBienvenida;
    @FXML private Label lblContador;
    @FXML private Label lblMensaje;
    @FXML private Label lblMensajeMedicos;
    @FXML private Label lblMensajeCitas;

    private final PacienteService pacienteService = new PacienteService();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final CitaService citaService = new CitaService();
    private final CitaDAO citaDAO = new CitaDAO();
    private final BloqueoAgendaDAO bloqueoAgendaDAO = new BloqueoAgendaDAO();
    private final DisponibilidadService disponibilidadService = new DisponibilidadService();

    private FilteredList<Paciente> filteredPacientes;
    private FilteredList<Medico> filteredMedicos;
    private FilteredList<Cita> filteredCitas;
    private final ScheduledExecutorService inasistenciasScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "inasistencias-detector");
        t.setDaemon(true);
        return t;
    });

    @FXML
    public void initialize() {
        Usuario usuario = Session.getUsuarioActual();
        lblBienvenida.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());

        colPacNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNombre() + " " + d.getValue().getApellido()));
        colPacEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPacTipoDoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoDocumento()));
        colPacDoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroDocumento()));
        colPacTel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colPacEps.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEps()));

        colMedNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNombre() + " " + d.getValue().getApellido()));
        colMedEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colMedEsp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEspecialidad().getNombre()));
        colMedRegistro.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRegistroMedico()));
        colMedTel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colMedCons.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getConsultorio()));

        colCitaPac.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPaciente().getNombre() + " " + d.getValue().getPaciente().getApellido()));
        colCitaMed.setCellValueFactory(d -> new SimpleStringProperty(
                "Dr. " + d.getValue().getMedico().getNombre() + " " + d.getValue().getMedico().getApellido()));
        colCitaServicio.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getServicio().getNombre()));
        colCitaFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colCitaHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colCitaTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo().getNombre()));
        colCitaEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
        colCitaMotivo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMotivo()));

        conectarTooltips();

        tablaPacientes.setPlaceholder(new Label("No hay pacientes registrados"));
        tablaMedicos.setPlaceholder(new Label("No hay medicos registrados"));
        tablaCitas.setPlaceholder(new Label("No hay citas registradas"));

        searchPacientes.textProperty().addListener((obs, old, val) -> {
            if (filteredPacientes != null) {
                filteredPacientes.setPredicate(p -> val == null || val.isBlank()
                        || (p.getNombre() + " " + p.getApellido() + " " + p.getEmail() + " " + p.getNumeroDocumento() + " " + p.getTelefono() + " " + p.getTipoDocumento() + " " + p.getEps())
                                .toLowerCase().contains(val.toLowerCase()));
            }
        });
        searchMedicos.textProperty().addListener((obs, old, val) -> {
            if (filteredMedicos != null) {
                filteredMedicos.setPredicate(m -> val == null || val.isBlank()
                        || (m.getNombre() + " " + m.getApellido() + " " + m.getEmail() + " " + m.getEspecialidad().getNombre() + " " + m.getRegistroMedico() + " " + m.getConsultorio())
                                .toLowerCase().contains(val.toLowerCase()));
            }
        });
        searchCitas.textProperty().addListener((obs, old, val) -> {
            if (filteredCitas != null) {
                filteredCitas.setPredicate(c -> val == null || val.isBlank()
                        || (c.getPaciente().getNombre() + " " + c.getPaciente().getApellido()
                            + " " + c.getMedico().getNombre() + " " + c.getMedico().getApellido()
                            + " " + c.getServicio().getNombre()
                            + " " + c.getFecha() + " " + c.getHoraInicio() + " " + c.getEstado().name()
                            + " " + c.getTipo().getNombre() + " " + c.getMotivo())
                                .toLowerCase().contains(val.toLowerCase()));
                actualizarContador();
            }
        });

        cargarPacientes();
        cargarMedicos();
        cargarCitas();
        aplicarColorFilas(tablaCitas);
        iniciarDeteccionInasistencias();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void conectarTooltips() {
        for (TableColumn col : new TableColumn[]{
                colPacNombre, colPacEmail, colPacTipoDoc, colPacDoc, colPacTel, colPacEps,
                colMedNombre, colMedEmail, colMedEsp, colMedRegistro, colMedTel, colMedCons,
                colCitaPac, colCitaMed, colCitaServicio, colCitaFecha, colCitaHora, colCitaTipo, colCitaEstado, colCitaMotivo
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

    private void cargarPacientes() {
        try {
            ObservableList<Paciente> base = FXCollections.observableArrayList(pacienteService.listarPacientes());
            filteredPacientes = new FilteredList<>(base, p -> true);
            SortedList<Paciente> sorted = new SortedList<>(filteredPacientes);
            sorted.comparatorProperty().bind(tablaPacientes.comparatorProperty());
            tablaPacientes.setItems(sorted);
            actualizarContador();
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar pacientes.");
        }
    }

    private void cargarMedicos() {
        try {
            ObservableList<Medico> base = FXCollections.observableArrayList(medicoDAO.obtenerTodos());
            filteredMedicos = new FilteredList<>(base, m -> true);
            SortedList<Medico> sorted = new SortedList<>(filteredMedicos);
            sorted.comparatorProperty().bind(tablaMedicos.comparatorProperty());
            tablaMedicos.setItems(sorted);
        } catch (RuntimeException exception) {
            mostrarError(lblMensajeMedicos, "Error al cargar medicos.");
        }
    }

    private void cargarCitas() {
        try {
            ObservableList<Cita> base = FXCollections.observableArrayList(citaService.todasLasCitas());
            filteredCitas = new FilteredList<>(base, c -> true);
            SortedList<Cita> sorted = new SortedList<>(filteredCitas);
            sorted.comparatorProperty().bind(tablaCitas.comparatorProperty());
            tablaCitas.setItems(sorted);
            actualizarContador();
        } catch (RuntimeException exception) {
            mostrarError(lblMensajeCitas, "Error al cargar citas.");
        }
    }

    private void actualizarContador() {
        long pendientes = filteredCitas == null ? 0
                : filteredCitas.stream().filter(c -> c.getEstado() == EstadoCita.PENDIENTE).count();
        long totalPac = filteredPacientes == null ? 0 : filteredPacientes.size();
        lblContador.setText(totalPac + " pacientes | " + pendientes + " citas pendientes");
    }

    @FXML
    private void handleModificarPaciente() {
        Paciente sel = tablaPacientes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona un paciente.");
            return;
        }

        Dialog<Paciente> dialog = new Dialog<>();
        dialog.setTitle("Modificar paciente");
        dialog.setHeaderText("Editando a " + sel.getNombre() + " " + sel.getApellido());

        ButtonType btnGuardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField nombre = new TextField(sel.getNombre());
        TextField apellido = new TextField(sel.getApellido());
        TextField email = new TextField(sel.getEmail());
        TextField telefono = new TextField(sel.getTelefono());
        TextField tipoDoc = new TextField(sel.getTipoDocumento());
        TextField numDoc = new TextField(sel.getNumeroDocumento());
        DatePicker fechaNac = new DatePicker(sel.getFechaNacimiento());
        TextField direccion = new TextField(sel.getDireccion());
        TextField eps = new TextField(sel.getEps());

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
                            sel.getId(),
                            nombre.getText().trim(), apellido.getText().trim(),
                            email.getText().trim(), sel.getPassword(),
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

        dialog.showAndWait().ifPresent(paciente -> {
            try {
                pacienteDAO.actualizarTodo(paciente);
                mostrarExito(lblMensaje, "Paciente actualizado.");
                cargarPacientes();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleDesactivarPaciente() {
        Paciente sel = tablaPacientes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona un paciente.");
            return;
        }

        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Desactivar a " + sel.getNombre() + " " + sel.getApellido() + "?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    pacienteService.desactivarPaciente(sel.getId());
                    mostrarExito(lblMensaje, "Paciente desactivado.");
                    cargarPacientes();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensaje, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleNuevoPaciente() {
        Dialog<Paciente> dialog = new Dialog<>();
        dialog.setTitle("Registrar nuevo paciente");
        dialog.setHeaderText("Datos del nuevo paciente");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField nombre = new TextField(); nombre.setPromptText("Nombre");
        TextField apellido = new TextField(); apellido.setPromptText("Apellido");
        TextField email = new TextField(); email.setPromptText("Email");
        TextField password = new TextField(); password.setPromptText("Password");
        TextField telefono = new TextField(); telefono.setPromptText("Telefono");
        TextField tipoDoc = new TextField(); tipoDoc.setPromptText("Tipo doc (CC, TI, CE)");
        TextField numDoc = new TextField(); numDoc.setPromptText("Numero documento");
        DatePicker fechaNac = new DatePicker(); fechaNac.setPromptText("Fecha nacimiento");
        TextField direccion = new TextField(); direccion.setPromptText("Direccion");
        TextField eps = new TextField(); eps.setPromptText("EPS");

        grid.addRow(0, new Label("Nombre:"), nombre, new Label("Apellido:"), apellido);
        grid.addRow(1, new Label("Email:"), email, new Label("Password:"), password);
        grid.addRow(2, new Label("Telefono:"), telefono, new Label("Tipo doc:"), tipoDoc);
        grid.addRow(3, new Label("Num doc:"), numDoc, new Label("Fecha nac:"), fechaNac);
        grid.addRow(4, new Label("Direccion:"), direccion, new Label("EPS:"), eps);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    return new Paciente(
                            UUID.randomUUID().toString().substring(0, 8),
                            nombre.getText().trim(), apellido.getText().trim(),
                            email.getText().trim(), password.getText().trim(),
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

        dialog.showAndWait().ifPresent(paciente -> {
            try {
                pacienteService.registrarPaciente(paciente);
                mostrarExito(lblMensaje, "Paciente registrado correctamente.");
                cargarPacientes();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensaje, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleModificarMedico() {
        Medico sel = tablaMedicos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensajeMedicos, "Selecciona un medico.");
            return;
        }

        Dialog<Medico> dialog = new Dialog<>();
        dialog.setTitle("Modificar medico");
        dialog.setHeaderText("Editando a " + sel.getNombre() + " " + sel.getApellido());

        ButtonType btnGuardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField nombre = new TextField(sel.getNombre());
        TextField apellido = new TextField(sel.getApellido());
        TextField telefono = new TextField(sel.getTelefono());
        TextField registro = new TextField(sel.getRegistroMedico());
        ComboBox<String> especialidad = new ComboBox<>();
        especialidad.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(Especialidad.values()).map(Especialidad::getNombre).toList()
        ));
        especialidad.setValue(sel.getEspecialidad().getNombre());
        TextField consultorio = new TextField(sel.getConsultorio());

        grid.addRow(0, new Label("Nombre:"), nombre, new Label("Apellido:"), apellido);
        grid.addRow(1, new Label("Telefono:"), telefono, new Label("Registro:"), registro);
        grid.addRow(2, new Label("Especialidad:"), especialidad, new Label("Consultorio:"), consultorio);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    String espNombre = especialidad.getValue();
                    Especialidad esp = espNombre != null
                            ? Especialidad.valueOf(espNombre.toUpperCase().replace(' ', '_'))
                            : sel.getEspecialidad();
                    return new Medico(
                            sel.getId(),
                            nombre.getText().trim(), apellido.getText().trim(),
                            sel.getEmail(), sel.getPassword(),
                            telefono.getText().trim(),
                            registro.getText().trim(), esp,
                            consultorio.getText().trim()
                    );
                } catch (Exception e) {
                    mostrarError(lblMensajeMedicos, "Datos invalidos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(medico -> {
            try {
                medicoDAO.actualizar(medico);
                mostrarExito(lblMensajeMedicos, "Medico actualizado.");
                cargarMedicos();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensajeMedicos, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleEliminarMedico() {
        Medico sel = tablaMedicos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensajeMedicos, "Selecciona un medico.");
            return;
        }

        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Eliminar a " + sel.getNombre() + " " + sel.getApellido()
                + " (" + sel.getEspecialidad().getNombre() + ")?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    medicoDAO.eliminar(sel.getId());
                    mostrarExito(lblMensajeMedicos, "Medico eliminado.");
                    cargarMedicos();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensajeMedicos, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleModificarCita() {
        Cita sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensajeCitas, "Selecciona una cita.");
            return;
        }

        Dialog<Cita> dialog = new Dialog<>();
        dialog.setTitle("Modificar cita");
        dialog.setHeaderText("Editando cita de " + sel.getPaciente().getNombre()
                + " con Dr. " + sel.getMedico().getNombre());

        ButtonType btnGuardar = new ButtonType("Guardar cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        ComboBox<String> cbCategoria = new ComboBox<>();
        cbCategoria.setItems(FXCollections.observableArrayList(ServicioCita.grupos()));
        String grupoActual = sel.getServicio().getGrupo();
        cbCategoria.setValue(grupoActual);

        ComboBox<String> cbServicio = new ComboBox<>();
        cbServicio.setItems(FXCollections.observableArrayList(
                ServicioCita.porGrupo(grupoActual).stream().map(ServicioCita::getNombre).collect(Collectors.toList())));
        cbServicio.setValue(sel.getServicio().getNombre());

        cbCategoria.valueProperty().addListener((obs, old, grupo) -> {
            if (grupo != null) {
                cbServicio.setItems(FXCollections.observableArrayList(
                        ServicioCita.porGrupo(grupo).stream().map(ServicioCita::getNombre).collect(Collectors.toList())));
                cbServicio.setValue(null);
            }
        });

        List<Medico> medicos = medicoDAO.obtenerTodos();
        Map<String, String> mapaMedicos = new LinkedHashMap<>();
        for (Medico m : medicos) {
            String etiqueta = m.getNombre() + " " + m.getApellido() + " (" + m.getEspecialidad().getNombre() + ")";
            mapaMedicos.put(etiqueta, m.getId());
        }
        ComboBox<String> cbMedico = new ComboBox<>();
        String medActual = sel.getMedico().getNombre() + " " + sel.getMedico().getApellido()
                + " (" + sel.getMedico().getEspecialidad().getNombre() + ")";
        cbMedico.setValue(medActual);

        cbServicio.valueProperty().addListener((obs, old, sn) -> {
            if (sn == null) return;
            String enumName = sn.toUpperCase().replace(' ', '_').replace('-', '_').replace('/', '_');
            ServicioCita serv = ServicioCita.valueOf(enumName);
            if (serv == null) return;
            cbMedico.setItems(FXCollections.observableArrayList());
            for (Medico m : medicoDAO.obtenerTodos()) {
                if (m.getEspecialidad() == serv.getEspecialidadRequerida()) {
                    String etiqueta = m.getNombre() + " " + m.getApellido() + " (" + m.getEspecialidad().getNombre() + ")";
                    cbMedico.getItems().add(etiqueta);
                }
            }
            cbMedico.setValue(null);
        });
        cbServicio.setValue(sel.getServicio().getNombre());

        DatePicker dpFecha = new DatePicker(sel.getFecha());

        ComboBox<String> cbHora = new ComboBox<>();
        cbHora.setPromptText("Selecciona hora");

        ComboBox<String> cbTipoConsulta = new ComboBox<>();
        cbTipoConsulta.setItems(FXCollections.observableArrayList("Primera vez", "Control"));
        cbTipoConsulta.setValue("Primera vez");

        Runnable cargarHoras = () -> {
            LocalDate fecha = dpFecha.getValue();
            if (fecha == null || cbServicio.getValue() == null || cbMedico.getValue() == null) return;
            String servEnum = cbServicio.getValue().toUpperCase().replace(' ', '_')
                    .replace('-', '_').replace('/', '_');
            ServicioCita serv = ServicioCita.valueOf(servEnum);
            String medKey = cbMedico.getValue();
            String medId = mapaMedicos.get(medKey);
            if (serv == null || medId == null) return;

            int duracion = "Control".equals(cbTipoConsulta.getValue())
                    ? serv.getDuracionControlMinutos()
                    : serv.getDuracionMinutos();
            Task<List<LocalTime>> task = new Task<>() {
                @Override
                protected List<LocalTime> call() {
                    return disponibilidadService.obtenerHorasDisponibles(medId, fecha, duracion);
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
        };

        dpFecha.valueProperty().addListener((obs, old, fecha) -> cargarHoras.run());
        cbTipoConsulta.valueProperty().addListener((obs, old, t) -> cargarHoras.run());

        Platform.runLater(() -> {
            if (dpFecha.getValue() != null && cbServicio.getValue() != null && cbMedico.getValue() != null) {
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
        grid.addRow(1, new Label("Categoria:"), cbCategoria, new Label("Servicio:"), cbServicio);
        grid.addRow(2, new Label("Medico:"), cbMedico);
        grid.addRow(3, new Label("Fecha:"), dpFecha, new Label("Hora:"), cbHora);
        grid.addRow(4, new Label("Tipo:"), cbTipoConsulta, new Label("Modalidad:"), cbTipo);
        grid.addRow(5, new Label("Motivo:"), txtMotivo);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnGuardar) {
                try {
                    String servicioNombre = cbServicio.getValue();
                    if (servicioNombre == null) return null;
                    String servicioEnum = servicioNombre.toUpperCase().replace(' ', '_')
                            .replace('-', '_').replace('/', '_');
                    ServicioCita servicio = ServicioCita.valueOf(servicioEnum);

                    String medKey = cbMedico.getValue();
                    if (medKey == null) return null;
                    String medicoId = mapaMedicos.get(medKey);
                    if (medicoId == null) return null;
                    Medico medico = medicoDAO.buscarPorId(medicoId);
                    if (medico == null) return null;

                    String tipoConsulta = cbTipoConsulta.getValue();
                    int duracion = "Control".equals(tipoConsulta)
                            ? servicio.getDuracionControlMinutos()
                            : servicio.getDuracionMinutos();

                    String tipoNombre = cbTipo.getValue();
                    TipoCita tipo = TipoCita.valueOf(tipoNombre.toUpperCase().replace(' ', '_'));

                    return new Cita(
                            sel.getId(),
                            sel.getPaciente(), medico,
                            servicio,
                            dpFecha.getValue(), LocalTime.parse(cbHora.getValue()),
                            duracion,
                            tipo,
                            txtMotivo.getText().trim(),
                            sel.getOrigen()
                    );
                } catch (Exception e) {
                    mostrarError(lblMensajeCitas, "Datos invalidos: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(cita -> {
            try {
                citaDAO.actualizarCita(cita);
                mostrarExito(lblMensajeCitas, "Cita actualizada.");
                cargarCitas();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensajeCitas, exception.getMessage());
            }
        });
    }

    @FXML
    private void handleConfirmarCita() {
        Cita sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensajeCitas, "Selecciona una cita.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmar cita de " + sel.getPaciente().getNombre() + " con "
                + sel.getMedico().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    citaService.confirmarCita(sel.getId());
                    mostrarExito(lblMensajeCitas, "Cita confirmada.");
                    cargarCitas();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensajeCitas, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancelarCita() {
        Cita sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensajeCitas, "Selecciona una cita.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancelar cita de " + sel.getPaciente().getNombre() + " con "
                + sel.getMedico().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    citaService.cancelarCita(sel.getId());
                    mostrarExito(lblMensajeCitas, "Cita cancelada.");
                    cargarCitas();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensajeCitas, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleNoAsistio() {
        Cita sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensajeCitas, "Selecciona una cita.");
            return;
        }
        Alert dialogo = new Alert(Alert.AlertType.CONFIRMATION,
                "Marcar como inasistencia la cita de " + sel.getPaciente().getNombre() + "?",
                ButtonType.YES, ButtonType.NO);
        dialogo.showAndWait().ifPresent(boton -> {
            if (boton == ButtonType.YES) {
                try {
                    citaService.marcarNoAsistio(sel.getId());
                    mostrarExito(lblMensajeCitas, "Cita marcada como inasistencia.");
                    cargarCitas();
                } catch (IllegalArgumentException | IllegalStateException exception) {
                    mostrarError(lblMensajeCitas, exception.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleAgregarCita() {
        Dialog<Cita> dialog = new Dialog<>();
        dialog.setTitle("Agendar nueva cita");
        dialog.setHeaderText("Asignar una cita a un paciente");

        ButtonType btnAgendar = new ButtonType("Agendar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnAgendar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        List<Paciente> pacientes = pacienteService.listarPacientes();
        Map<String, String> mapaPacientes = new LinkedHashMap<>();
        for (Paciente p : pacientes) {
            String etiqueta = p.getNombre() + " " + p.getApellido() + " (" + p.getNumeroDocumento() + ")";
            mapaPacientes.put(etiqueta, p.getId());
        }
        ComboBox<String> cbPaciente = new ComboBox<>();
        cbPaciente.setItems(FXCollections.observableArrayList(mapaPacientes.keySet()));
        cbPaciente.setPromptText("Selecciona paciente");

        ComboBox<String> cbCategoria = new ComboBox<>();
        cbCategoria.setItems(FXCollections.observableArrayList(ServicioCita.grupos()));
        cbCategoria.setPromptText("Selecciona categoria");

        ComboBox<String> cbServicio = new ComboBox<>();
        cbServicio.setPromptText("Selecciona servicio");
        cbServicio.setDisable(true);

        ComboBox<String> cbMedico = new ComboBox<>();
        cbMedico.setPromptText("Selecciona medico");
        cbMedico.setDisable(true);

        Map<String, String> mapaMedicos = new LinkedHashMap<>();

        cbCategoria.valueProperty().addListener((obs, old, grupo) -> {
            if (grupo != null) {
                cbServicio.setDisable(false);
                cbServicio.setItems(FXCollections.observableArrayList(
                        ServicioCita.porGrupo(grupo).stream().map(ServicioCita::getNombre).collect(Collectors.toList())));
                cbServicio.setValue(null);
                cbMedico.setDisable(true);
                cbMedico.setValue(null);
            }
        });

        cbServicio.valueProperty().addListener((obs, old, servicioNombre) -> {
            if (servicioNombre == null) {
                cbMedico.setDisable(true);
                cbMedico.setValue(null);
                return;
            }
            cbMedico.setDisable(false);
            String enumName = servicioNombre.toUpperCase().replace(' ', '_').replace('-', '_').replace('/', '_');
            ServicioCita serv = ServicioCita.valueOf(enumName);
            if (serv == null) return;
            mapaMedicos.clear();
            List<Medico> docs = medicoDAO.obtenerTodos().stream()
                    .filter(m -> m.getEspecialidad() == serv.getEspecialidadRequerida())
                    .toList();
            for (Medico m : docs) {
                String etiqueta = m.getNombre() + " " + m.getApellido() + " (" + m.getEspecialidad().getNombre() + ")";
                mapaMedicos.put(etiqueta, m.getId());
            }
            cbMedico.setItems(FXCollections.observableArrayList(mapaMedicos.keySet()));
        });

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha");
        dpFecha.setDisable(true);

        ComboBox<String> cbHora = new ComboBox<>();
        cbHora.setPromptText("Hora");
        cbHora.setDisable(true);

        cbMedico.valueProperty().addListener((obs, old, medKey) -> {
            if (medKey != null) {
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

        ComboBox<String> cbTipoConsulta = new ComboBox<>();
        cbTipoConsulta.setItems(FXCollections.observableArrayList("Primera vez", "Control"));
        cbTipoConsulta.setValue("Primera vez");

        Runnable cargarHoras = () -> {
            LocalDate fecha = dpFecha.getValue();
            if (fecha == null || cbMedico.getValue() == null || cbServicio.getValue() == null) {
                cbHora.setDisable(true);
                cbHora.getItems().clear();
                return;
            }
            String servEnum = cbServicio.getValue().toUpperCase().replace(' ', '_')
                    .replace('-', '_').replace('/', '_');
            ServicioCita serv = ServicioCita.valueOf(servEnum);
            String medKey = cbMedico.getValue();
            String medId = mapaMedicos.get(medKey);
            if (serv == null || medId == null) {
                cbHora.setDisable(true);
                cbHora.getItems().clear();
                return;
            }

            int duracion = "Control".equals(cbTipoConsulta.getValue())
                    ? serv.getDuracionControlMinutos()
                    : serv.getDuracionMinutos();
            Task<List<LocalTime>> task = new Task<>() {
                @Override
                protected List<LocalTime> call() {
                    return disponibilidadService.obtenerHorasDisponibles(medId, fecha, duracion);
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
        };

        dpFecha.valueProperty().addListener((obs, old, fecha) -> cargarHoras.run());
        cbTipoConsulta.valueProperty().addListener((obs, old, t) -> cargarHoras.run());

        ComboBox<String> cbTipo = new ComboBox<>();
        cbTipo.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(TipoCita.values()).map(TipoCita::getNombre).collect(Collectors.toList())));
        cbTipo.setValue("Presencial");

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Motivo de consulta");

        CheckBox chkSobrecupo = new CheckBox("Sobrecupo (saltar disponibilidad)");

        grid.addRow(0, new Label("Paciente:"), cbPaciente);
        grid.addRow(1, new Label("Categoria:"), cbCategoria, new Label("Servicio:"), cbServicio);
        grid.addRow(2, new Label("Medico:"), cbMedico);
        grid.addRow(3, new Label("Fecha:"), dpFecha, new Label("Hora:"), cbHora);
        grid.addRow(4, new Label("Tipo:"), cbTipoConsulta, new Label("Modalidad:"), cbTipo);
        grid.addRow(5, new Label("Motivo:"), txtMotivo);
        grid.addRow(6, chkSobrecupo);

        dialog.getDialogPane().setContent(grid);

        boolean[] esSobrecupo = {false};

        dialog.setResultConverter(btn -> {
            if (btn == btnAgendar) {
                String pacKey = cbPaciente.getValue();
                String servicioNombre = cbServicio.getValue();
                String medKey = cbMedico.getValue();
                LocalDate fecha = dpFecha.getValue();
                String hora = cbHora.getValue();
                String tipoConsulta = cbTipoConsulta.getValue();
                String tipoNombre = cbTipo.getValue();
                String motivo = txtMotivo.getText();

                if (pacKey == null || servicioNombre == null || medKey == null || fecha == null || hora == null) {
                    return null;
                }

                esSobrecupo[0] = chkSobrecupo.isSelected();

                try {
                    String servicioEnum = servicioNombre.toUpperCase().replace(' ', '_')
                            .replace('-', '_').replace('/', '_');
                    ServicioCita servicio = ServicioCita.valueOf(servicioEnum);

                    String pacienteId = mapaPacientes.get(pacKey);
                    String medicoId = mapaMedicos.get(medKey);
                    Paciente paciente = pacienteService.listarPacientes().stream()
                            .filter(p -> p.getId().equals(pacienteId)).findFirst().orElse(null);
                    Medico medico = medicoDAO.buscarPorId(medicoId);

                    if (paciente == null || medico == null) return null;

                    int duracion = "Control".equals(tipoConsulta)
                            ? servicio.getDuracionControlMinutos()
                            : servicio.getDuracionMinutos();

                    String tipoEnum = tipoNombre.toUpperCase().replace(' ', '_')
                            .replace('-', '_').replace('/', '_');
                    TipoCita tipo = TipoCita.valueOf(tipoEnum);

                    return new Cita(
                            UUID.randomUUID().toString().substring(0, 8),
                            paciente, medico,
                            servicio,
                            fecha, LocalTime.parse(hora),
                            duracion,
                            tipo,
                            motivo == null || motivo.isBlank() ? "Consulta general" : motivo.trim(),
                            OrigenCita.PACIENTE.name()
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
                    mostrarExito(lblMensajeCitas, "Sobrecupo agendado correctamente.");
                } else {
                    citaService.agendarCita(cita);
                    mostrarExito(lblMensajeCitas, "Cita agendada correctamente.");
                }
                cargarCitas();
            } catch (IllegalArgumentException | IllegalStateException exception) {
                mostrarError(lblMensajeCitas, exception.getMessage());
            }
        });
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

    @FXML
    private void irARegistroMedico() throws IOException {
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/registro.fxml", "Registro de Usuario - Sistema EPS");
    }

    @FXML
    private void handleGestionarBloqueos() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Gestionar Bloqueos de Agenda");
        dialog.setHeaderText("Bloquear fechas/horas para un medico");

        ButtonType btnCerrar = new ButtonType("Cerrar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(btnCerrar);

        VBox content = new VBox(12);
        content.setPadding(new Insets(16));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        ComboBox<String> cbMedico = new ComboBox<>();
        Map<String, String> mapaMedicosBloq = new LinkedHashMap<>();
        for (Medico m : medicoDAO.obtenerTodos()) {
            String etiqueta = m.getNombre() + " " + m.getApellido() + " (" + m.getEspecialidad().getNombre() + ")";
            mapaMedicosBloq.put(etiqueta, m.getId());
        }
        cbMedico.setItems(FXCollections.observableArrayList(mapaMedicosBloq.keySet()));
        cbMedico.setPromptText("Selecciona medico");

        DatePicker dpFecha = new DatePicker();
        dpFecha.setPromptText("Fecha");
        dpFecha.setDisable(true);

        CheckBox chkDiaCompleto = new CheckBox("Todo el dia");
        chkDiaCompleto.setSelected(true);

        ComboBox<String> cbHoraInicio = new ComboBox<>();
        cbHoraInicio.setPromptText("Hora inicio");
        cbHoraInicio.setDisable(true);
        cbHoraInicio.setItems(FXCollections.observableArrayList(
                "06:00", "06:30", "07:00", "07:30", "08:00", "08:30",
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00"));

        ComboBox<String> cbHoraFin = new ComboBox<>();
        cbHoraFin.setPromptText("Hora fin");
        cbHoraFin.setDisable(true);
        cbHoraFin.setItems(FXCollections.observableArrayList(
                "06:00", "06:30", "07:00", "07:30", "08:00", "08:30",
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
                "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00"));

        chkDiaCompleto.selectedProperty().addListener((obs, old, val) -> {
            cbHoraInicio.setDisable(val);
            cbHoraFin.setDisable(val);
            if (val) {
                cbHoraInicio.setValue(null);
                cbHoraFin.setValue(null);
            }
        });

        TextField txtMotivo = new TextField();
        txtMotivo.setPromptText("Motivo del bloqueo (ej: Congreso, Vacaciones, Incapacidad)");

        Button btnAgregarBloqueo = new Button("Agregar bloqueo");
        btnAgregarBloqueo.getStyleClass().add("btn-danger");
        btnAgregarBloqueo.setDisable(true);

        form.addRow(0, new Label("Medico:"), cbMedico);
        form.addRow(1, new Label("Fecha:"), dpFecha);
        form.addRow(2, new Label("Horario:"), chkDiaCompleto, cbHoraInicio, new Label("a"), cbHoraFin);
        form.addRow(3, new Label("Motivo:"), txtMotivo);
        form.add(btnAgregarBloqueo, 0, 4, 4, 1);

        Separator sep = new Separator();
        Label lblBloqueosExistentes = new Label("Bloqueos existentes:");
        lblBloqueosExistentes.setStyle("-fx-font-weight: bold;");

        ListView<String> listaBloqueos = new ListView<>();
        listaBloqueos.setPrefHeight(120);
        ObservableList<String> itemsBloqueos = FXCollections.observableArrayList();
        listaBloqueos.setItems(itemsBloqueos);

        Button btnEliminarBloqueo = new Button("Eliminar bloqueo seleccionado");
        btnEliminarBloqueo.getStyleClass().add("btn-danger");
        btnEliminarBloqueo.setDisable(true);

        HBox bottomRow = new HBox(10, btnEliminarBloqueo);

        content.getChildren().addAll(form, sep, lblBloqueosExistentes, listaBloqueos, bottomRow);
        dialog.getDialogPane().setContent(content);

        cbMedico.valueProperty().addListener((obs, old, val) -> {
            dpFecha.setDisable(val == null);
            if (val == null) {
                dpFecha.setValue(null);
                btnAgregarBloqueo.setDisable(true);
            }
            actualizarListaBloqueos(itemsBloqueos, listaBloqueos,
                    mapaMedicosBloq.get(val), dpFecha.getValue(), btnEliminarBloqueo);
        });

        dpFecha.valueProperty().addListener((obs, old, val) -> {
            btnAgregarBloqueo.setDisable(val == null || cbMedico.getValue() == null);
            actualizarListaBloqueos(itemsBloqueos, listaBloqueos,
                    mapaMedicosBloq.get(cbMedico.getValue()), val, btnEliminarBloqueo);
        });

        listaBloqueos.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            btnEliminarBloqueo.setDisable(val == null);
        });

        btnAgregarBloqueo.setOnAction(e -> {
            String medKey = cbMedico.getValue();
            LocalDate fecha = dpFecha.getValue();
            String motivo = txtMotivo.getText() == null ? "" : txtMotivo.getText().trim();

            if (medKey == null || fecha == null || motivo.isBlank()) {
                mostrarError(lblMensajeCitas, "Completa medico, fecha y motivo.");
                return;
            }

            String medicoId = mapaMedicosBloq.get(medKey);
            LocalTime horaInicio = chkDiaCompleto.isSelected() ? null
                    : (cbHoraInicio.getValue() != null ? LocalTime.parse(cbHoraInicio.getValue()) : null);
            LocalTime horaFin = chkDiaCompleto.isSelected() ? null
                    : (cbHoraFin.getValue() != null ? LocalTime.parse(cbHoraFin.getValue()) : null);

            try {
                BloqueoAgenda bloqueo = new BloqueoAgenda(
                        UUID.randomUUID().toString().substring(0, 8),
                        medicoId, fecha, horaInicio, horaFin, motivo);
                bloqueoAgendaDAO.guardar(bloqueo);

                List<Cita> afectadas = citaDAO.obtenerActivasPorMedicoYFecha(medicoId, fecha);
                if (!afectadas.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Se agregó el bloqueo. Las siguientes citas quedan afectadas:\n\n");
                    for (Cita c : afectadas) {
                        sb.append("- ").append(c.getPaciente().getNombre()).append(" ")
                                .append(c.getPaciente().getApellido())
                                .append(" | ").append(c.getFecha()).append(" ")
                                .append(c.getHoraInicio()).append("\n");
                    }
                    sb.append("\nDeberas contactar a los pacientes para reubicarlos.");
                    Alert alert = new Alert(Alert.AlertType.WARNING, sb.toString(), ButtonType.OK);
                    alert.setTitle("Citas por Reubicar");
                    alert.setHeaderText(afectadas.size() + " cita(s) afectada(s)");
                    alert.show();
                } else {
                    mostrarExito(lblMensajeCitas, "Bloqueo agregado.");
                }

                actualizarListaBloqueos(itemsBloqueos, listaBloqueos, medicoId, fecha, btnEliminarBloqueo);
                txtMotivo.clear();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                mostrarError(lblMensajeCitas, ex.getMessage());
            }
        });

        btnEliminarBloqueo.setOnAction(e -> {
            int idx = listaBloqueos.getSelectionModel().getSelectedIndex();
            if (idx < 0) return;

            String item = itemsBloqueos.get(idx);
            String bloqueoId = item.substring(0, item.indexOf(" |"));
            try {
                bloqueoAgendaDAO.eliminar(bloqueoId);
                actualizarListaBloqueos(itemsBloqueos, listaBloqueos,
                        mapaMedicosBloq.get(cbMedico.getValue()), dpFecha.getValue(), btnEliminarBloqueo);
                mostrarExito(lblMensajeCitas, "Bloqueo eliminado.");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                mostrarError(lblMensajeCitas, ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    private void actualizarListaBloqueos(ObservableList<String> items, ListView<String> listView,
                                          String medicoId, LocalDate fecha, Button btnEliminar) {
        if (medicoId == null || fecha == null) {
            items.clear();
            return;
        }
        try {
            List<BloqueoAgenda> bloqueos = bloqueoAgendaDAO.obtenerPorMedicoYFecha(medicoId, fecha);
            items.clear();
            for (BloqueoAgenda b : bloqueos) {
                String rango = b.esDiaCompleto() ? "Todo el dia"
                        : b.getHoraInicio() + " - " + b.getHoraFin();
                items.add(b.getId() + " | " + rango + " | " + b.getMotivo());
            }
        } catch (RuntimeException e) {
            items.clear();
        }
    }

    private void iniciarDeteccionInasistencias() {
        Runnable tarea = () -> {
            try {
                citaService.autoDetectarInasistencias();
            } catch (Exception ignored) {
            }
        };
        inasistenciasScheduler.scheduleWithFixedDelay(tarea, 0, 5, TimeUnit.MINUTES);
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }
}
