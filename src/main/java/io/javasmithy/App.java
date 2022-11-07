package io.javasmithy;

import io.javasmithy.controller.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;


public class App extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/main-layout.fxml"));
            stage.setScene(new Scene( (Parent) loader.load(), 1200, 900));
            ((MainController)loader.getController()).setStage(stage);
            ((MainController)loader.getController()).setScene();
            ((MainController)loader.getController()).setKeyEventHandler();

            stage.setTitle("NinaVSI");
            // Visual Simulation Inference
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}