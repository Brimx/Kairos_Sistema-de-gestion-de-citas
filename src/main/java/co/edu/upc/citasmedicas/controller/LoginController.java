package co.edu.upc.citasmedicas.controller;

import javafx.fxml.FXML;

/**
 * Controlador para la interfaz visual de Login.
 */
public class LoginController {

    @FXML
    public void initialize() {
        System.out.println("[Controller] LoginController inicializado");
    }

    @FXML
    private void handleLogin() {
        System.out.println("[Controller] Intentando iniciar sesión...");
    }
}
