package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.AdministradorDAO;
import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.dao.UsuarioDAO;
import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.model.Administrador;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Controlador para la interfaz visual de registro de usuarios.
 */
public class RegistroController {

    @FXML
    private TextField nombreField;

    @FXML
    private TextField apellidoField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField telefonoField;

    @FXML
    private ComboBox<String> rolComboBox;

    @FXML
    private TextField codigoAdminField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final AdministradorDAO administradorDAO = new AdministradorDAO();

    // Código interno para registrar administradores
    private static final String CODIGO_ADMIN_SECRETO = "ADMIN-SECRET-2026";

    @FXML
    public void initialize() {
        // No se requiere inicialización adicional
        errorLabel.setText("");
        successLabel.setText("");
    }

    @FXML
    private void handleRegistrar() {
        errorLabel.setText("");
        successLabel.setText("");

        // Validar campos obligatorios
        if (nombreField.getText() == null || nombreField.getText().isBlank()) {
            errorLabel.setText("El nombre es obligatorio");
            return;
        }
        if (apellidoField.getText() == null || apellidoField.getText().isBlank()) {
            errorLabel.setText("El apellido es obligatorio");
            return;
        }
        if (emailField.getText() == null || emailField.getText().isBlank()) {
            errorLabel.setText("El email es obligatorio");
            return;
        }
        if (passwordField.getText() == null || passwordField.getText().isBlank()) {
            errorLabel.setText("La contraseña es obligatoria");
            return;
        }
        if (telefonoField.getText() == null || telefonoField.getText().isBlank()) {
            errorLabel.setText("El teléfono es obligatorio");
            return;
        }
        if (rolComboBox.getValue() == null) {
            errorLabel.setText("Selecciona un tipo de usuario");
            return;
        }

        String nombre = nombreField.getText().trim();
        String apellido = apellidoField.getText().trim();
        String email = emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();
        String telefono = telefonoField.getText().trim();
        String rolSeleccionado = rolComboBox.getValue();

        // Validar email duplicado
        if (usuarioDAO.existeEmail(email)) {
            errorLabel.setText("El email ya está registrado. Usa otro email.");
            return;
        }

        // Validar código para administrador
        if ("ADMINISTRADOR".equals(rolSeleccionado)) {
            if (codigoAdminField.getText() == null || !codigoAdminField.getText().equals(CODIGO_ADMIN_SECRETO)) {
                errorLabel.setText("Código de administrador incorrecto");
                return;
            }
        }

        try {
            // Generar ID único
            String id = generarIdUnico();

            // Crear y guardar según el rol
            if ("PACIENTE".equals(rolSeleccionado)) {
                Paciente paciente = new Paciente(
                        id, nombre, apellido, email, password, telefono,
                        "CC", "00000000", LocalDate.now(), "No especificada", "No especificada"
                );
                pacienteDAO.guardar(paciente);
            } else if ("MÉDICO".equals(rolSeleccionado)) {
                Medico medico = new Medico(
                        id, nombre, apellido, email, password, telefono,
                        "REG-00000", Especialidad.MEDICINA_GENERAL, "Consultorio no asignado"
                );
                medicoDAO.guardar(medico);
            } else if ("ADMINISTRADOR".equals(rolSeleccionado)) {
                Administrador administrador = new Administrador(
                        id, nombre, apellido, email, password, telefono,
                        "ADM-" + id.substring(id.length() - 3), "Registro general"
                );
                administradorDAO.guardar(administrador);
            }

            successLabel.setText("¡Registro exitoso! Ahora puedes iniciar sesión.");

            // Limpiar campos
            limpiarCampos();

        } catch (Exception exception) {
            errorLabel.setText("Error al registrar: " + exception.getMessage());
        }
    }

    @FXML
    private void handleVolverLogin() throws IOException {
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Iniciar Sesión - Sistema EPS");
    }

    private void limpiarCampos() {
        nombreField.setText("");
        apellidoField.setText("");
        emailField.setText("");
        passwordField.setText("");
        telefonoField.setText("");
        rolComboBox.setValue(null);
        codigoAdminField.setText("");
    }

    private String generarIdUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
