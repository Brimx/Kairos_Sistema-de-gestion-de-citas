package co.edu.upc.citasmedicas.controller;

import javafx.fxml.FXML;

/**
 * Controlador para la interfaz visual del Paciente (Dashboard).
 */
public class PacienteController {

    @FXML
    public void initialize() {
        System.out.println("[Controller] PacienteController inicializado");
    }

    @FXML
    private void handleSolicitarCita() {
        System.out.println("[Controller] Solicitando nueva cita...");
    }

    @FXML
    private void handleVerCitas() {
        System.out.println("[Controller] Mostrando citas del paciente...");
    }
}
