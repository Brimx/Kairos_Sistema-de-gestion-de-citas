package co.edu.upc.citasmedicas;

import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Punto de entrada de la aplicación JavaFX del sistema EPS.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        ViewManager.setPrimaryStage(primaryStage);
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Sistema de Citas Medicas EPS");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
