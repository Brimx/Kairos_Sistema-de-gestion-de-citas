package co.edu.upc.citasmedicas.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import java.io.IOException;

public class ViewManager {

    private static Stage primaryStage;
    private static final String STYLES_CSS;

    static {
        var url = ViewManager.class.getResource("/co/edu/upc/citasmedicas/css/styles.css");
        if (url == null) {
            throw new RuntimeException("styles.css not found");
        }
        STYLES_CSS = url.toExternalForm();
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void showView(String fxmlPath, String title) throws IOException {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage no ha sido inicializado");
        }

        FXMLLoader loader = new FXMLLoader(ViewManager.class.getResource(fxmlPath));
        Parent root = loader.load();

        double pw = root.prefWidth(-1);
        double ph = root.prefHeight(-1);
        Scene scene = (pw > 0 && ph > 0)
                ? new Scene(root, pw, ph)
                : new Scene(root);

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void styleDialog(Dialog<?> dialog) {
        dialog.getDialogPane().getStylesheets().add(STYLES_CSS);
    }

    public static void styleAlert(Alert alert) {
        alert.getDialogPane().getStylesheets().add(STYLES_CSS);
    }
}
