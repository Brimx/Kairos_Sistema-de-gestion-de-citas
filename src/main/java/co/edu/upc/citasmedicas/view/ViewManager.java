package co.edu.upc.citasmedicas.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Inicializador y manejador de las vistas JavaFX en el sistema.
 */
public class ViewManager {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Carga y muestra una vista basada en su archivo FXML.
     * 
     * @param fxmlPath ruta relativa al recurso FXML (ej: "/co/edu/upc/citasmedicas/fxml/login.fxml")
     * @param title título de la ventana
     * @throws IOException si no se puede cargar el FXML
     */
    public static void showView(String fxmlPath, String title) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage no ha sido inicializado");
        }

        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);

        double pw = root.prefWidth(-1);
        double ph = root.prefHeight(-1);
        if (pw > 0) primaryStage.setWidth(pw);
        if (ph > 0) primaryStage.setHeight(ph);

        primaryStage.show();
    }
}
