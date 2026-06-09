package co.edu.upc.citasmedicas;

import co.edu.upc.citasmedicas.view.ViewManager;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        loadFonts();
        ViewManager.setPrimaryStage(primaryStage);
        ViewManager.showView("/co/edu/upc/citasmedicas/fxml/login.fxml", "Kairos - Sistema de Gestion de Citas");
    }

    private void loadFonts() {
        String[] fonts = {
            "IBMPlexSans-Regular.ttf",
            "IBMPlexSans-Bold.ttf",
            "IBMPlexSans-Medium.ttf",
            "IBMPlexSans-Italic.ttf",
            "IBMPlexSans-Light.ttf",
            "IBMPlexSans-Thin.ttf",
            "IBMPlexMono-Regular.ttf",
            "IBMPlexMono-Bold.ttf",
            "IBMPlexMono-Medium.ttf"
        };
        for (String font : fonts) {
            String path = "/co/edu/upc/citasmedicas/fonts/" + font;
            try (var is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    Font loaded = Font.loadFont(is, 14);
                    if (loaded != null) {
                        System.out.println("Loaded font: " + loaded.getName() + " (" + loaded.getFamily() + ")");
                    } else {
                        System.err.println("Font.loadFont returned null: " + path);
                    }
                } else {
                    System.err.println("Font resource not found: " + path);
                }
            } catch (IOException e) {
                System.err.println("Error loading font " + path + ": " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
