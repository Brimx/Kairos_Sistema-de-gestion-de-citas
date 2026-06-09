package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.AdministradorDAO;
import co.edu.upc.citasmedicas.dao.MedicoDAO;
import co.edu.upc.citasmedicas.dao.PacienteDAO;
import co.edu.upc.citasmedicas.dao.UsuarioDAO;
import co.edu.upc.citasmedicas.enums.Especialidad;
import co.edu.upc.citasmedicas.model.Administrador;
import co.edu.upc.citasmedicas.model.Medico;
import co.edu.upc.citasmedicas.model.Paciente;

import co.edu.upc.citasmedicas.service.ValidacionService;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class RegistroController {

    @FXML private TextField nombreField;
    @FXML private TextField apellidoField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private TextField telefonoField;
    @FXML private TextField confirmarTelefonoField;
    @FXML private ComboBox<String> rolComboBox;
    @FXML private TextField codigoAdminField;

    @FXML private VBox documentoPanel;
    @FXML private ComboBox<String> tipoDocCombo;
    @FXML private TextField numeroDocField;
    @FXML private DatePicker fechaNacField;
    @FXML private TextField direccionField;
    @FXML private TextField epsField;

    @FXML private VBox medicoPanel;
    @FXML private TextField registroMedicoField;
    @FXML private ComboBox<String> especialidadCombo;
    @FXML private TextField consultorioField;

    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();
    private final AdministradorDAO administradorDAO = new AdministradorDAO();

    private static final String CODIGO_ADMIN_SECRETO = "ADMIN-SECRET-2026";

    @FXML
    public void initialize() {
        errorLabel.setText("");
        successLabel.setText("");
        rolComboBox.setItems(FXCollections.observableArrayList("PACIENTE", "MEDICO", "ADMINISTRADOR"));
        passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        Tooltip.install(togglePasswordBtn, new Tooltip("Mostrar contrasena"));

        tipoDocCombo.setItems(FXCollections.observableArrayList("CC", "TI", "CE", "Pasaporte"));
        tipoDocCombo.setValue("CC");

        especialidadCombo.setItems(FXCollections.observableArrayList(
                java.util.Arrays.stream(Especialidad.values()).map(Especialidad::getNombre).toList()
        ));

        rolComboBox.valueProperty().addListener((obs, old, rol) -> {
            boolean esPaciente = "PACIENTE".equals(rol);
            boolean esMedico = "MEDICO".equals(rol);
            boolean esAdmin = "ADMINISTRADOR".equals(rol);

            documentoPanel.setVisible(esPaciente);
            documentoPanel.setManaged(esPaciente);

            medicoPanel.setVisible(esMedico);
            medicoPanel.setManaged(esMedico);

            codigoAdminField.setVisible(esAdmin);
            codigoAdminField.setManaged(esAdmin);
        });

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
    private void togglePasswordVisibility() {
        boolean showing = passwordVisibleField.isVisible();
        passwordField.setVisible(showing);
        passwordField.setManaged(showing);
        passwordVisibleField.setVisible(!showing);
        passwordVisibleField.setManaged(!showing);
        togglePasswordBtn.setText(showing ? "\uD83D\uDE48" : "\uD83D\uDC41");
        Tooltip.install(togglePasswordBtn, new Tooltip(showing ? "Ocultar contrasena" : "Mostrar contrasena"));
        syncStyle(passwordField, passwordVisibleField);
        syncStyle(passwordVisibleField, passwordField);
    }

    private void syncStyle(TextField from, TextField to) {
        to.getStyleClass().removeAll("input-error", "input-valid");
        to.getStyleClass().addAll(from.getStyleClass().filtered(
            s -> s.equals("input-error") || s.equals("input-valid")
        ));
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
                String tipoDoc = tipoDocCombo.getValue();
                String numDoc = numeroDocField.getText() == null ? "" : numeroDocField.getText().trim();
                LocalDate fechaNac = fechaNacField.getValue();
                String direccion = direccionField.getText() == null ? "" : direccionField.getText().trim();
                String eps = epsField.getText() == null ? "" : epsField.getText().trim();

                if (numDoc.isEmpty()) {
                    errorLabel.setText("El numero de documento es obligatorio");
                    marcarError(numeroDocField);
                    return;
                }
                if (fechaNac == null) {
                    errorLabel.setText("La fecha de nacimiento es obligatoria");
                    return;
                }

                Paciente paciente = new Paciente(
                        id, nombre, apellido, email, password, telefono,
                        tipoDoc != null ? tipoDoc : "CC", numDoc, fechaNac,
                        direccion.isEmpty() ? "No especificada" : direccion,
                        eps.isEmpty() ? "No especificada" : eps
                );
                pacienteDAO.guardar(paciente);

            } else if ("MEDICO".equals(rolSeleccionado)) {
                String registro = registroMedicoField.getText() == null ? "" : registroMedicoField.getText().trim();
                String espNombre = especialidadCombo.getValue();
                String consultorio = consultorioField.getText() == null ? "" : consultorioField.getText().trim();

                Especialidad esp = Especialidad.MEDICINA_GENERAL;
                if (espNombre != null) {
                    esp = Especialidad.valueOf(espNombre.toUpperCase().replace(' ', '_'));
                }

                Medico medico = new Medico(
                        id, nombre, apellido, email, password, telefono,
                        registro.isEmpty() ? "REG-" + id : registro,
                        esp,
                        consultorio.isEmpty() ? "Consultorio no asignado" : consultorio
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
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Kairos - Iniciar Sesion");
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
        numeroDocField.setText("");
        fechaNacField.setValue(null);
        direccionField.setText("");
        epsField.setText("");
        registroMedicoField.setText("");
        consultorioField.setText("");
        limpiarEstilos();
    }

    private void limpiarEstilos() {
        for (var campo : List.of(nombreField, apellidoField, emailField, passwordField,
                passwordVisibleField, telefonoField, confirmarTelefonoField, codigoAdminField,
                numeroDocField, direccionField, epsField, registroMedicoField, consultorioField)) {
            limpiarEstiloCampo(campo);
        }
    }

    private void marcarError(TextField campo) {
        campo.getStyleClass().removeAll("input-valid");
        if (!campo.getStyleClass().contains("input-error")) {
            campo.getStyleClass().add("input-error");
        }
    }

    private void marcarValido(TextField campo) {
        campo.getStyleClass().removeAll("input-error");
        if (!campo.getStyleClass().contains("input-valid")) {
            campo.getStyleClass().add("input-valid");
        }
    }

    private void limpiarEstiloCampo(TextField campo) {
        campo.getStyleClass().removeAll("input-error", "input-valid");
    }

    private void validarEmailCampo() {
        String email = emailField.getText().trim().toLowerCase();
        if (email.isBlank()) {
            limpiarEstiloCampo(emailField);
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
            limpiarEstiloCampo(telefonoField);
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
            limpiarEstiloCampo(confirmarTelefonoField);
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
