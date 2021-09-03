package io.javasmithy.controller;

import io.javasmithy.inference.Model;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    private Stage stage;
    private Model model;
    private Random random;
    private AnimationTimer animationTimer;
    private boolean capturing;
    private StringBuilder sb;
    private Robot robot;

    private Image currentImage;
    private BufferedImage currentBufferedImage;

    @FXML
    Canvas canvas;
    @FXML
    MenuItem exitMenuItem, toggleConCap, capFrameMenuItem;
    @FXML
    TextArea logArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.capturing = false;
        this.random = new Random();
        this.model = new Model();
        this.sb = new StringBuilder();
        try {
            this.robot = new Robot();
            robot.setAutoDelay(10);
        } catch (AWTException e) {
            log(e.toString());
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void captureSingleImage() {
        this.currentBufferedImage = this.robot.createScreenCapture(new Rectangle(640, 220, 640, 640));
        this.currentImage = (Image) SwingFXUtils.toFXImage( this.currentBufferedImage, null);
    }

    @FXML
    private void toggleContinuous() {
        if (animationTimer == null) {
            this.animationTimer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    log("Capturing single image.");
                    captureSingleImage();
                    log("Calling TF model.");
                    callModel();
                    log("Drawing caputre.");
                    drawCapture();
                }
            };
        }

        if (capturing) {
            this.capturing = false;
            log("Stopping continuous captures.");
            this.animationTimer.stop();
        } else {
            this.capturing = true;
            log("Starting continuous captures.");
            this.animationTimer.start();
        }
    }

    @FXML
    private void drawCapture() {
        this.canvas.getGraphicsContext2D().drawImage(this.currentImage, 0, 0);
    }

    public void callModel(){
        this.model.call(this.currentBufferedImage);
    }

    @FXML
    private void exit() {
        Platform.exit();
        System.exit(0);
    }

    private void log(String logEntry) {
        System.out.println("Log updated.");
        logArea.setText(this.sb.append(getTime()).append(" : ").append(logEntry).append("\n").toString());
    }

    private String getTime() {
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }


}