package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.model.Usuario;
import co.edu.upc.citasmedicas.enums.EstadoCita;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.PacienteService;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import java.io.IOException;

public class DashboardAdminController {

    @FXML private TableView<Paciente> tablaPacientes;
    @FXML private TableColumn<Paciente, String> colPacNombre;
    @FXML private TableColumn<Paciente, String> colPacEmail;
    @FXML private TableColumn<Paciente, String> colPacDoc;
    @FXML private TableColumn<Paciente, String> colPacTel;
    @FXML private TextField txtEditNombre;
    @FXML private TextField txtEditTel;

    @FXML private TableView<Medico> tablaMedicos;
    @FXML private TableColumn<Medico, String> colMedNombre;
    @FXML private TableColumn<Medico, String> colMedEmail;
    @FXML private TableColumn<Medico, String> colMedEsp;

    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, String> colCitaPac;
    @FXML private TableColumn<Cita, String> colCitaMed;
    @FXML private TableColumn<Cita, String> colCitaFecha;
    @FXML private TableColumn<Cita, String> colCitaHora;
    @FXML private TableColumn<Cita, String> colCitaEstado;

    @FXML private Label lblBienvenida;
    @FXML private Label lblMensaje;
    @FXML private Label lblMensajeMedicos;
    @FXML private Label lblMensajeCitas;

    private final PacienteService pacienteService = new PacienteService();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final CitaService citaService = new CitaService();

    @FXML
    public void initialize() {
        Usuario usuario = Session.getUsuarioActual();
        lblBienvenida.setText("Bienvenido, " + usuario.getNombre() + " " + usuario.getApellido());

        colPacNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNombre() + " " + d.getValue().getApellido()));
        colPacEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colPacDoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroDocumento()));
        colPacTel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));

        colMedNombre.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNombre() + " " + d.getValue().getApellido()));
        colMedEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colMedEsp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEspecialidad().getNombre()));

        colCitaPac.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPaciente().getNombre() + " " + d.getValue().getPaciente().getApellido()));
        colCitaMed.setCellValueFactory(d -> new SimpleStringProperty(
                "Dr. " + d.getValue().getMedico().getNombre() + " " + d.getValue().getMedico().getApellido()));
        colCitaFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha().toString()));
        colCitaHora.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHoraInicio().toString()));
        colCitaEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));

        tablaPacientes.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null) {
                        txtEditNombre.setText(sel.getNombre());
                        txtEditTel.setText(sel.getTelefono());
                    }
                });

        tablaPacientes.setPlaceholder(new Label("No hay pacientes registrados"));
        tablaMedicos.setPlaceholder(new Label("No hay medicos registrados"));
        tablaCitas.setPlaceholder(new Label("No hay citas registradas"));

        cargarPacientes();
        cargarMedicos();
        cargarCitas();
        aplicarColorFilas(tablaCitas);
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

    private void cargarPacientes() {
        try {
            tablaPacientes.setItems(FXCollections.observableArrayList(pacienteService.listarPacientes()));
        } catch (RuntimeException exception) {
            mostrarError(lblMensaje, "Error al cargar pacientes.");
        }
    }

    private void cargarMedicos() {
        try {
            tablaMedicos.setItems(FXCollections.observableArrayList(medicoDAO.obtenerTodos()));
        } catch (RuntimeException exception) {
            mostrarError(lblMensajeMedicos, "Error al cargar medicos.");
        }
    }

    private void cargarCitas() {
        try {
            tablaCitas.setItems(FXCollections.observableArrayList(citaService.todasLasCitas()));
        } catch (RuntimeException exception) {
            mostrarError(lblMensajeCitas, "Error al cargar citas.");
        }
    }

    @FXML
    private void handleGuardarPaciente() {
        Paciente sel = tablaPacientes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            mostrarError(lblMensaje, "Selecciona un paciente.");
            return;
        }

        String nombre = txtEditNombre.getText().trim();
        String telefono = txtEditTel.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError(lblMensaje, "El nombre no puede estar vacio.");
            return;
        }

        try {
            pacienteService.actualizarDatos(sel.getId(), nombre, sel.getApellido(), telefono);
            mostrarExito(lblMensaje, "Paciente actualizado.");
            cargarPacientes();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            mostrarError(lblMensaje, exception.getMessage());
        }
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
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }
}
