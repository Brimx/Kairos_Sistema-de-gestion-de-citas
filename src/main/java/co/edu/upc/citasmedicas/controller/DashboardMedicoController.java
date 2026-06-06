package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.model.Cita;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.service.CitaService;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;

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

    private final CitaService citaService = new CitaService();

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

        cargarAgenda();
    }

    private void cargarAgenda() {
        try {
            Medico medico = (Medico) Session.getUsuarioActual();
            tablaAgenda.setItems(FXCollections.observableArrayList(
                    citaService.agendaDelMedico(medico.getId())));
        } catch (RuntimeException exception) {
            lblMensaje.setText("Error al cargar agenda.");
        }
    }

    @FXML
    private void handleAtender() {
        Cita sel = tablaAgenda.getSelectionModel().getSelectedItem();
        if (sel == null) {
            lblMensaje.setText("Selecciona una cita.");
            return;
        }
        try {
            citaService.atenderCita(sel.getId());
            lblMensaje.setText("Cita marcada como completada.");
            cargarAgenda();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            lblMensaje.setText(exception.getMessage());
        }
    }

    @FXML
    private void handleActualizar() {
        cargarAgenda();
        lblMensaje.setText("Agenda actualizada.");
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }
}
