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
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.List;

/**
 * Controlador del panel principal. Carga modelos, enums y datos persistidos en SQLite.
 */
public class DashboardController {

    @FXML
    private Label usuarioLabel;

    @FXML
    private Label rolLabel;

    @FXML
    private Label resumenLabel;

    @FXML
    private ListView<String> menuList;

    @FXML
    private ListView<String> citasList;

    @FXML
    private ListView<String> modelosList;

    private final CitaDAO citaDAO = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();

    @FXML
    public void initialize() {
        Usuario usuario = Session.getUsuarioActual();
        if (usuario == null) {
            mostrarSesionVacia();
            return;
        }

        usuarioLabel.setText(usuario.getNombre() + " " + usuario.getApellido());
        rolLabel.setText("Rol: " + usuario.getRol());
        menuList.setItems(FXCollections.observableArrayList(usuario.getMenuOpciones()));

        cargarCitas(usuario);
        cargarModelosYEnums();
    }

    @FXML
    private void handleRefrescar() {
        Usuario usuario = Session.getUsuarioActual();
        if (usuario != null) {
            cargarCitas(usuario);
            cargarModelosYEnums();
        }
    }

    @FXML
    private void handleCerrarSesion() throws IOException {
        Session.cerrar();
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }

    private void cargarCitas(Usuario usuario) {
        List<Cita> citas = switch (usuario.getRol()) {
            case PACIENTE -> citaDAO.obtenerPorPaciente(usuario.getId());
            case MEDICO -> citaDAO.obtenerPorMedico(usuario.getId());
            case ADMIN -> citaDAO.obtenerTodas();
        };

        citasList.setItems(FXCollections.observableArrayList(
                citas.stream().map(this::formatearCita).toList()
        ));

        resumenLabel.setText("Citas visibles: " + citas.size());
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

    private void mostrarSesionVacia() {
        usuarioLabel.setText("Sin usuario activo");
        rolLabel.setText("Rol: -");
        resumenLabel.setText("Inicia sesion para cargar datos");
        menuList.setItems(FXCollections.observableArrayList());
        citasList.setItems(FXCollections.observableArrayList());
        modelosList.setItems(FXCollections.observableArrayList());
    }
}
