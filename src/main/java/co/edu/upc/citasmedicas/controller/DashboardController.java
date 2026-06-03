package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.CitaDAO;
import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.enums.TipoCita;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.model.Usuario;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Controlador del panel principal. Permite consultar, crear y cambiar estado de citas persistidas en SQLite.
 */
public class DashboardController {

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label rolLabel;

    @FXML
    private Label resumenLabel;

    @FXML
    private Label mensajeLabel;

    @FXML
    private ListView<String> menuList;

    @FXML
    private ListView<String> citasList;

    @FXML
    private ListView<String> modelosList;

    @FXML
    private ComboBox<Paciente> pacienteCombo;

    @FXML
    private ComboBox<Medico> medicoCombo;

    @FXML
    private ComboBox<Especialidad> especialidadCombo;

    @FXML
    private DatePicker fechaPicker;

    @FXML
    private TextField horaField;

    @FXML
    private ComboBox<TipoCita> tipoCombo;

    @FXML
    private TextArea motivoArea;

    @FXML
    private Button crearButton;

    @FXML
    private Button confirmarButton;

    @FXML
    private Button cancelarButton;

    @FXML
    private Button completarButton;

    private final CitaDAO citaDAO = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final CitaService citaService = new CitaService();

    private List<Cita> citasActuales = new ArrayList<>();
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        usuarioActual = Session.getUsuarioActual();
        if (usuarioActual == null) {
            mostrarSesionVacia();
            return;
        }

        usuarioLabel.setText(usuarioActual.getNombre() + " " + usuarioActual.getApellido());
        rolLabel.setText("Rol: " + usuarioActual.getRol());
        menuList.setItems(FXCollections.observableArrayList(usuarioActual.getMenuOpciones()));
        configurarMenu();

        prepararFormulario();
        configurarPermisosPorRol();
        cargarCitas(usuarioActual);
        cargarModelosYEnums();
    }

    @FXML
    private void handleCrearCita() {
        try {
            Cita cita = new Cita(
                    UUID.randomUUID().toString(),
                    pacienteCombo.getValue(),
                    medicoCombo.getValue(),
                    especialidadCombo.getValue(),
                    fechaPicker.getValue(),
                    parseHora(),
                    tipoCombo.getValue(),
                    motivoArea.getText() == null ? "" : motivoArea.getText().trim()
            );

            citaService.agendarCita(cita);
            limpiarFormularioCita();
            recargarDashboard("Cita creada correctamente");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarMensaje(exception.getMessage(), true);
        }
    }

    @FXML
    private void handleConfirmarCita() {
        cambiarEstadoSeleccionado(EstadoCita.CONFIRMADA);
    }

    @FXML
    private void handleCancelarCita() {
        cambiarEstadoSeleccionado(EstadoCita.CANCELADA);
    }

    @FXML
    private void handleCompletarCita() {
        cambiarEstadoSeleccionado(EstadoCita.COMPLETADA);
    }

    @FXML
    private void handleRefrescar() {
        if (usuarioActual != null) {
            recargarDashboard("Datos actualizados");
        }
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }

    private void prepararFormulario() {
        pacienteCombo.setItems(FXCollections.observableArrayList(pacienteDAO.obtenerTodos()));
        medicoCombo.setItems(FXCollections.observableArrayList(medicoDAO.obtenerTodos()));
        especialidadCombo.setItems(FXCollections.observableArrayList(Especialidad.values()));
        tipoCombo.setItems(FXCollections.observableArrayList(TipoCita.values()));

        fechaPicker.setValue(LocalDate.now());
        horaField.setText("08:00");
        tipoCombo.getSelectionModel().select(TipoCita.PRESENCIAL);

        medicoCombo.valueProperty().addListener((observable, anterior, medico) -> {
            if (medico != null) {
                especialidadCombo.getSelectionModel().select(medico.getEspecialidad());
            }
        });

        if (usuarioActual instanceof Paciente paciente) {
            pacienteCombo.setValue(paciente);
        }
    }

    private void configurarPermisosPorRol() {
        boolean puedeCrear = usuarioActual.getRol().name().equals("ADMIN") || usuarioActual instanceof Paciente;
        boolean esAdmin = usuarioActual.getRol().name().equals("ADMIN");
        boolean esMedico = usuarioActual instanceof Medico;
        boolean esPaciente = usuarioActual instanceof Paciente;

        crearButton.setDisable(!puedeCrear);
        confirmarButton.setDisable(!esAdmin);
        cancelarButton.setDisable(!(esAdmin || esPaciente));
        completarButton.setDisable(!(esAdmin || esMedico));

        pacienteCombo.setDisable(!esAdmin);
        medicoCombo.setDisable(!puedeCrear);
        especialidadCombo.setDisable(!puedeCrear);
        fechaPicker.setDisable(!puedeCrear);
        horaField.setDisable(!puedeCrear);
        tipoCombo.setDisable(!puedeCrear);
        motivoArea.setDisable(!puedeCrear);

        if (!puedeCrear) {
            mostrarMensaje("Selecciona una cita para cambiar su estado", false);
        }
    }

    private void configurarMenu() {
        menuList.setOnMouseClicked(event -> {
            String opcion = menuList.getSelectionModel().getSelectedItem();
            if (opcion != null) {
                ejecutarOpcionMenu(opcion);
            }
        });
    }

    private void ejecutarOpcionMenu(String opcion) {
        switch (opcion) {
            case "Solicitar cita" -> enfocarFormularioCita("Completa el formulario para solicitar una cita");
            case "Ver mis citas", "Ver agenda del dia", "Ver todas las citas" -> enfocarListaCitas("Citas cargadas desde SQLite");
            case "Cancelar cita" -> handleCancelarCita();
            case "Ver mi historial", "Ver historial de paciente", "Ver reportes" -> enfocarResumen("Resumen disponible en la seccion inferior");
            case "Actualizar mis datos" -> mostrarMensaje("La actualizacion de datos va en el siguiente bloque del proyecto", false);
            case "Ver siguiente paciente" -> seleccionarPrimeraCita("Siguiente cita seleccionada");
            case "Marcar asistencia" -> handleCompletarCita();
            case "Gestionar pacientes" -> enfocarGestionPacientes();
            case "Gestionar medicos", "Registrar nuevo medico" -> enfocarGestionMedicos(opcion);
            case "Cerrar sesion" -> cerrarSesionDesdeMenu();
            default -> mostrarMensaje("Opcion no disponible", true);
        }
    }

    private void enfocarFormularioCita(String mensaje) {
        if (crearButton.isDisabled()) {
            mostrarMensaje("Tu rol no puede crear citas desde este formulario", true);
            return;
        }
        medicoCombo.requestFocus();
        mostrarMensaje(mensaje, false);
    }

    private void enfocarListaCitas(String mensaje) {
        citasList.requestFocus();
        if (!citasActuales.isEmpty() && citasList.getSelectionModel().getSelectedIndex() < 0) {
            citasList.getSelectionModel().selectFirst();
        }
        mostrarMensaje(mensaje, false);
    }

    private void enfocarResumen(String mensaje) {
        modelosList.requestFocus();
        mostrarMensaje(mensaje, false);
    }

    private void seleccionarPrimeraCita(String mensaje) {
        if (citasActuales.isEmpty()) {
            mostrarMensaje("No hay citas en agenda", true);
            return;
        }
        citasList.getSelectionModel().selectFirst();
        citasList.requestFocus();
        mostrarMensaje(mensaje, false);
    }

    private void enfocarGestionPacientes() {
        pacienteCombo.requestFocus();
        mostrarMensaje("Pacientes disponibles en el formulario de citas", false);
    }

    private void enfocarGestionMedicos(String opcion) {
        medicoCombo.requestFocus();
        String mensaje = opcion.equals("Registrar nuevo medico")
                ? "El registro de medicos va en el siguiente bloque; ahora puedes asignarlos a citas"
                : "Medicos disponibles en el formulario de citas";
        mostrarMensaje(mensaje, false);
    }

    private void cerrarSesionDesdeMenu() {
        try {
            handleCerrarSesion();
        } catch (IOException exception) {
            mostrarMensaje("No se pudo cerrar sesion", true);
        }
    }

    private void cargarCitas(Usuario usuario) {
        citasActuales = switch (usuario.getRol()) {
            case PACIENTE -> citaDAO.obtenerPorPaciente(usuario.getId());
            case MEDICO -> citaDAO.obtenerPorMedico(usuario.getId());
            case ADMIN -> citaDAO.obtenerTodas();
        };

        citasList.setItems(FXCollections.observableArrayList(
                citasActuales.stream().map(this::formatearCita).toList()
        ));

        resumenLabel.setText("Citas visibles: " + citasActuales.size());
    }

    private void cargarModelosYEnums() {
        List<String> resumen = List.of(
                "Pacientes registrados: " + pacienteDAO.obtenerTodos().size(),
                "Medicos registrados: " + medicoDAO.obtenerTodos().size(),
                "Especialidades: " + unirEnums(Especialidad.values()),
                "Estados de cita: " + unirEnums(EstadoCita.values()),
                "Tipos de cita: " + unirEnums(TipoCita.values())
        );

        modelosList.setItems(FXCollections.observableArrayList(resumen));
    }

    private void cambiarEstadoSeleccionado(EstadoCita estado) {
        Cita cita = obtenerCitaSeleccionada();
        if (cita == null) {
            mostrarMensaje("Selecciona una cita primero", true);
            return;
        }

        try {
            switch (estado) {
                case CONFIRMADA -> citaService.confirmarCita(cita.getId());
                case CANCELADA -> citaService.cancelarCita(cita.getId());
                case COMPLETADA -> citaService.completarCita(cita.getId());
                case PENDIENTE -> throw new IllegalArgumentException("No se puede devolver una cita a pendiente");
            }
            recargarDashboard("Estado actualizado a " + estado);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarMensaje(exception.getMessage(), true);
        }
    }

    private Cita obtenerCitaSeleccionada() {
        int indice = citasList.getSelectionModel().getSelectedIndex();
        if (indice < 0 || indice >= citasActuales.size()) {
            return null;
        }
        return citasActuales.get(indice);
    }

    private LocalTime parseHora() {
        String hora = horaField.getText() == null ? "" : horaField.getText().trim();
        try {
            return LocalTime.parse(hora);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Ingresa la hora en formato HH:mm, por ejemplo 09:30");
        }
    }

    private void recargarDashboard(String mensaje) {
        cargarCitas(usuarioActual);
        cargarModelosYEnums();
        mostrarMensaje(mensaje, false);
    }

    private void limpiarFormularioCita() {
        if (!(usuarioActual instanceof Paciente)) {
            pacienteCombo.getSelectionModel().clearSelection();
        }
        medicoCombo.getSelectionModel().clearSelection();
        especialidadCombo.getSelectionModel().clearSelection();
        fechaPicker.setValue(LocalDate.now());
        horaField.setText("08:00");
        tipoCombo.getSelectionModel().select(TipoCita.PRESENCIAL);
        motivoArea.clear();
    }

    private String formatearCita(Cita cita) {
        return cita.getFecha() + " " + cita.getHoraInicio()
                + " | " + cita.getEspecialidad()
                + " | " + cita.getTipo()
                + " | " + cita.getEstado()
                + " | Paciente: " + cita.getPaciente().getNombre() + " " + cita.getPaciente().getApellido()
                + " | Medico: " + cita.getMedico().getNombre() + " " + cita.getMedico().getApellido()
                + " | " + cita.getMotivo();
    }

    private String unirEnums(Enum<?>[] valores) {
        return String.join(", ", java.util.Arrays.stream(valores).map(Enum::name).toList());
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        mensajeLabel.setText(mensaje == null ? "" : mensaje);
        mensajeLabel.getStyleClass().removeAll("error-label", "success-label");
        mensajeLabel.getStyleClass().add(error ? "error-label" : "success-label");
    }

    private void mostrarSesionVacia() {
        usuarioLabel.setText("Sin usuario activo");
        rolLabel.setText("Rol: -");
        resumenLabel.setText("Inicia sesion para cargar datos");
        if (mensajeLabel != null) {
            mostrarMensaje("", false);
        }
        menuList.setItems(FXCollections.observableArrayList());
        citasList.setItems(FXCollections.observableArrayList());
        modelosList.setItems(FXCollections.observableArrayList());
    }
}
