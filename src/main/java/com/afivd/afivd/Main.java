package com.afivd.afivd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    /**
     * Starts the application. Make sure this is the first class run in the jar
     * @param stage Javafx Window
     * @throws IOException Exception thrown if FXML file not found
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("mainScene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 600);
        stage.setTitle("FaultHunter: Automatic Fault Injection Vulnerability Detector");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("lockClipart.png"))));
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}