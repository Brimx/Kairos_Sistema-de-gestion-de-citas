package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.dao.DatabaseConnection;
import co.edu.upc.citasmedicas.dao.UsuarioDAO;
import co.edu.upc.citasmedicas.model.Usuario;
import co.edu.upc.citasmedicas.service.Session;
import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador para la interfaz visual de login.
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        try {
            DatabaseConnection.initialize();
            errorLabel.setText("Demo: paciente@demo.com, medico@demo.com o admin@demo.com / 1234");
        } catch (SQLException exception) {
            errorLabel.setText("No se pudo inicializar SQLite");
        }
    }

    @FXML
    private void handleLogin() throws IOException {
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            errorLabel.setText("Ingresa email y contrasena");
            return;
        }

        Usuario usuario = usuarioDAO.autenticar(email, password);
        if (usuario == null) {
            errorLabel.setText("Credenciales incorrectas o usuario inactivo");
            return;
        }

        Session.setUsuarioActual(usuario);

        String fxmlPath = switch (usuario.getRol()) {
            case ADMIN -> "/co/edu/upc/citasmedicas/fxml/dashboard_admin.fxml";
            case MEDICO -> "/co/edu/upc/citasmedicas/fxml/dashboard_medico.fxml";
            case PACIENTE -> "/co/edu/upc/citasmedicas/fxml/dashboard_paciente.fxml";
        };
        String titulo = switch (usuario.getRol()) {
            case ADMIN -> "Panel administrador - Sistema EPS";
            case MEDICO -> "Panel medico - Sistema EPS";
            case PACIENTE -> "Panel paciente - Sistema EPS";
        };
        ViewManager.showView(fxmlPath, titulo);
    }

    @FXML
    private void handleIrRegistro() throws IOException {
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/registro.fxml", "Registro de Usuario - Sistema EPS");
    }
}
