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
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
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
    @FXML private TextField txtFecha;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtMotivo;

    @FXML private Label lblMensaje;

    private final CitaService citaService = new CitaService();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final Map<String, String> mapaIdsMedicos = new LinkedHashMap<>();

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

        cargarMedicos();
        cargarMisCitas();
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
            lblMensaje.setText("Error al cargar medicos.");
        }
    }

    private void cargarMisCitas() {
        try {
            Paciente paciente = (Paciente) Session.getUsuarioActual();
            tablaCitas.setItems(FXCollections.observableArrayList(
                    citaService.citasDelPaciente(paciente.getId())));
        } catch (RuntimeException exception) {
            lblMensaje.setText("Error al cargar tus citas.");
        }
    }

    @FXML
    private void handleAgendarCita() {
        lblMensaje.setText("");
        String medicoKey = cbMedico.getValue();
        String fechaTexto = txtFecha.getText().trim();
        String hora = cbHora.getValue();
        String tipo = cbTipo.getValue();
        String motivo = txtMotivo.getText() == null ? "" : txtMotivo.getText().trim();

        if (medicoKey == null || fechaTexto.isEmpty() || hora == null || tipo == null) {
            lblMensaje.setText("Completa medico, fecha y hora.");
            return;
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaTexto);
            LocalTime horaInicio = LocalTime.parse(hora);
            String medicoId = mapaIdsMedicos.get(medicoKey);
            Paciente paciente = pacienteDAO.buscarPorId(Session.getUsuarioActual().getId());
            Medico medico = medicoDAO.buscarPorId(medicoId);

            if (paciente == null || medico == null) {
                lblMensaje.setText("No se pudo cargar paciente o medico.");
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
            lblMensaje.setText("Cita agendada correctamente.");
            limpiarFormulario();
            cargarMisCitas();
        } catch (DateTimeParseException exception) {
            lblMensaje.setText("Formato de fecha u hora invalido (AAAA-MM-DD).");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            lblMensaje.setText(exception.getMessage());
        }
    }

    @FXML
    private void handleCancelarCita() {
        Cita sel = tablaCitas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Selecciona una cita para cancelar.");
            return;
        }
        if (sel.getEstado() == EstadoCita.COMPLETADA) {
            lblMensaje.setText("No puedes cancelar una cita ya completada.");
            return;
        }
        try {
            citaService.cancelarCita(sel.getId());
            lblMensaje.setText("Cita cancelada.");
            cargarMisCitas();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            lblMensaje.setText(exception.getMessage());
        }
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }

    private void limpiarFormulario() {
        cbMedico.setValue(null);
        txtFecha.clear();
        cbHora.setValue(null);
        txtMotivo.clear();
        cbTipo.setValue("PRESENCIAL");
    }
}
