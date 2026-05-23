package co.edu.upc.citasmedicas.controller;

import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.fxml.FXML;

import java.io.IOException;

/**
 * Controlador para la interfaz visual de Login.
 */
public class LoginController {

    @FXML
    public void initialize() {
        System.out.println("[Controller] LoginController inicializado");
    }

    @FXML
    private void handleLogin() throws IOException {
        System.out.println("[Controller] Login temporal aceptado");
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/dashboard.fxml", "Panel principal - Sistema EPS");
    }
}
