package co.edu.upc.citasmedicas.controller;

import javafx.fxml.FXML;

/**
 * Controlador para la interfaz visual del Médico.
 */
public class MedicoController {

    @FXML
    public void initialize() {
        System.out.println("[Controller] MedicoController inicializado");
    }

    @FXML
    private void handleVerAgenda() {
        System.out.println("[Controller] Cargando agenda del día...");
    }

    @FXML
    private void handleAtenderPaciente() {
        System.out.println("[Controller] Atendiendo siguiente paciente de la cola...");
    }
}
