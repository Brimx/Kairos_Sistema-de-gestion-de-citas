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
import co.edu.upc.citasmedicas.service.ValidacionService;
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
    private TextField confirmarTelefonoField;

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
        errorLabel.setText("");
        successLabel.setText("");

        emailField.focusedProperty().addListener((obs, viejo, nuevo) -> {
            if (!nuevo) {
                validarEmailCampo();
            }
        });

        telefonoField.focusedProperty().addListener((obs, viejo, nuevo) -> {
            if (!nuevo) {
                validarTelefonoCampo();
            }
        });

        confirmarTelefonoField.focusedProperty().addListener((obs, viejo, nuevo) -> {
            if (!nuevo && !confirmarTelefonoField.getText().isBlank()) {
                validarConfirmarTelefonoCampo();
            }
        });
    }

    @FXML
    private void handleRegistrar() {
        errorLabel.setText("");
        successLabel.setText("");

        String nombre = nombreField.getText() == null ? "" : nombreField.getText().trim();
        String apellido = apellidoField.getText() == null ? "" : apellidoField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();
        String password = passwordField.getText();
        String telefono = telefonoField.getText() == null ? "" : telefonoField.getText().trim();
        String confirmarTelefono = confirmarTelefonoField.getText() == null ? "" : confirmarTelefonoField.getText().trim();
        String rolSeleccionado = rolComboBox.getValue();

        if (nombre.isEmpty()) {
            errorLabel.setText("El nombre es obligatorio");
            marcarError(nombreField);
            return;
        }
        if (apellido.isEmpty()) {
            errorLabel.setText("El apellido es obligatorio");
            marcarError(apellidoField);
            return;
        }
        if (password == null || password.isBlank()) {
            errorLabel.setText("La contrasena es obligatoria");
            marcarError(passwordField);
            return;
        }
        if (rolSeleccionado == null) {
            errorLabel.setText("Selecciona un tipo de usuario");
            return;
        }

        String errorEmail = ValidacionService.mensajeErrorEmail(email);
        if (errorEmail != null) {
            errorLabel.setText(errorEmail);
            marcarError(emailField);
            return;
        }
        if (usuarioDAO.existeEmail(email)) {
            errorLabel.setText("El email ya esta registrado. Usa otro email.");
            marcarError(emailField);
            return;
        }

        String errorTelefono = ValidacionService.mensajeErrorTelefono(telefono);
        if (errorTelefono != null) {
            errorLabel.setText(errorTelefono);
            marcarError(telefonoField);
            return;
        }

        String errorConfirmar = ValidacionService.mensajeErrorConfirmarTelefono(telefono, confirmarTelefono);
        if (errorConfirmar != null) {
            errorLabel.setText(errorConfirmar);
            marcarError(confirmarTelefonoField);
            return;
        }

        if ("ADMINISTRADOR".equals(rolSeleccionado)) {
            if (codigoAdminField.getText() == null || !codigoAdminField.getText().equals(CODIGO_ADMIN_SECRETO)) {
                errorLabel.setText("Codigo de administrador incorrecto");
                marcarError(codigoAdminField);
                return;
            }
        }

        try {
            String id = generarIdUnico();

            if ("PACIENTE".equals(rolSeleccionado)) {
                Paciente paciente = new Paciente(
                        id, nombre, apellido, email, password, telefono,
                        "CC", "00000000", LocalDate.now(), "No especificada", "No especificada"
                );
                pacienteDAO.guardar(paciente);
            } else if ("MEDICO".equals(rolSeleccionado)) {
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

            successLabel.setText("Registro exitoso! Ahora puedes iniciar sesion.");
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
        confirmarTelefonoField.setText("");
        rolComboBox.setValue(null);
        codigoAdminField.setText("");
        limpiarEstilos();
    }

    private void limpiarEstilos() {
        nombreField.setStyle("");
        apellidoField.setStyle("");
        emailField.setStyle("");
        passwordField.setStyle("");
        telefonoField.setStyle("");
        confirmarTelefonoField.setStyle("");
        codigoAdminField.setStyle("");
    }

    private void marcarError(TextField campo) {
        campo.setStyle("-fx-border-color: #b91c1c; -fx-border-width: 2; -fx-border-radius: 4;");
    }

    private void marcarValido(TextField campo) {
        campo.setStyle("-fx-border-color: #047857; -fx-border-width: 2; -fx-border-radius: 4;");
    }

    private void validarEmailCampo() {
        String email = emailField.getText().trim().toLowerCase();
        if (email.isBlank()) {
            emailField.setStyle("");
            return;
        }
        String error = ValidacionService.mensajeErrorEmail(email);
        if (error != null) {
            marcarError(emailField);
        } else {
            marcarValido(emailField);
        }
    }

    private void validarTelefonoCampo() {
        String tel = telefonoField.getText().trim();
        if (tel.isBlank()) {
            telefonoField.setStyle("");
            return;
        }
        String error = ValidacionService.mensajeErrorTelefono(tel);
        if (error != null) {
            marcarError(telefonoField);
        } else {
            marcarValido(telefonoField);
        }
    }

    private void validarConfirmarTelefonoCampo() {
        String tel = telefonoField.getText().trim();
        String conf = confirmarTelefonoField.getText().trim();
        if (conf.isBlank()) {
            confirmarTelefonoField.setStyle("");
            return;
        }
        String error = ValidacionService.mensajeErrorConfirmarTelefono(tel, conf);
        if (error != null) {
            marcarError(confirmarTelefonoField);
        } else {
            marcarValido(confirmarTelefonoField);
        }
    }

    private String generarIdUnico() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
