package io.javasmithy.controller;

import io.javasmithy.inference.DetectionClass;
import io.javasmithy.inference.Inference;
import io.javasmithy.inference.Model;
import io.javasmithy.robot.MovementTask;
import io.javasmithy.robot.TurnTask;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.InputEvent;
//import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainController implements Initializable {
    private final float MAX_X_THRESHOLD = 640/3 * 2;
    private final float MIN_X_THRESHOLD = 640/3;
    private final float MAX_Y_THRESHOLD = 640;
    private final float MIN_Y_THRESHOLD = 640/3;
    private final long HALF_SECOND_INTERVAL = 500_000_000L;
    private final float QUARTER_CANVAS_AREA = 102400;
    private final float THIRD_CANVAS_AREA = 136320;
    private final float SEVEN_TWENTY_FOURTHS_CANVAS_AREA = 119466;
    private final long ONE_SECOND_INTERVAL = 1_000_000_000L;
    private final long FIVE_SECOND_INTERVAL = 5_000_000_000L;
    private final long TEN_SECOND_INTERVAL = 10_000_000_000L;
    private final long TWELVE_FPS_INTERVAL = 83_333_333L;
    private final long SIXTY_FPS_INTERVAL = 16_666_666L;
    private final long ONE_TWENTY_FPS_INTERVAL = 8_333_333L;

    private final ExecutorService MOVEMENT_THREAD_POOL = Executors.newFixedThreadPool(1);
    private final ExecutorService INFERENCE_THREAD_POOL = Executors.newFixedThreadPool(1);

    private final double MIN_CANVAS_COORD = 0;
    private final double MAX_CANVAS_COORD = 640;

    private final double X_MIN_AVOIDANCE_THRESHOLD = 213;
    private final double X_MAX_AVOIDANCE_THRESHOLD = 426;
    private final double Y_MIN_AVOIDANCE_THRESHOLD = 426;
    private final double Y_MAX_AVOIDANCE_THRESHOLD = 640;

    private int callCount = 0;
    private long inferenceInterval = ONE_SECOND_INTERVAL;
    private int movementPulseInterval = 100;
    private double confidencePruneThreshold = .15;
    private Scene scene;
    private Stage stage;
    private Model model;
    private Random random;
    private AnimationTimer animationTimer, moveTimer, avoidTimer;
    private boolean capturing, avoiding, modelLoaded;
    private boolean moving, screenActive = false, avoidObjects = false;

    private double rightThresholdOverlap, leftThresholdOverlap;

    private StringBuilder sb;
    private Robot robot;
    private Inference inference;
    private Image currentImage;
    private BufferedImage currentBufferedImage;

    float[] results;
    int[] rectDimensions;

    Future isAvoiding;

    @FXML
    Canvas canvas;
    @FXML
    MenuItem exitMenuItem, toggleConCap, captureMenuItem;
    @FXML
    TextArea logArea;
    @FXML
    TextField inferenceIntervalTextField, confidencePruneTextField, movementPulseTextField;
    @FXML
    Button parameterSetButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.results = new float[5];
        this.inference = new Inference();
        this.rectDimensions = new int[4];
        this.capturing = false;
        this.random = new Random();
        this.model = new Model();
        this.sb = new StringBuilder();
        try {
            this.robot = new Robot();
            robot.setAutoDelay(1);
        } catch (AWTException e) {
            log(e.toString());
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void setScene(){
        this.scene = this.stage.getScene();
    }
    public void setKeyEventHandler(){
        this.scene.setOnKeyPressed( new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.isControlDown() && event.getCode() == KeyCode.E ) {
                    log("Toggling Captures via Hotkey");
                    toggleContinuous();
                }

                if (event.isControlDown() && event.getCode() == KeyCode.M ) {
                    log("Toggling Movement via Hotkey");
                    //toggleMovement();

                    if (moving){
                        stopMovement();
                    } else {
                        startMovement();
                    }

                }
            }
        });
    }

    @FXML
    public void setInferenceInterval(){
        double rawInterval = Double.parseDouble(this.inferenceIntervalTextField.getText());
        this.inferenceInterval = (long) rawInterval * 1000000000;
    }

    @FXML
    public void setConfidencePruneThreshold(){
        this.confidencePruneThreshold = Double.parseDouble(this.confidencePruneTextField.getText());
    }

    @FXML
    public void setMovementPulse(){
        try {
            this.movementPulseInterval = Integer.parseInt(this.movementPulseTextField.getText());
        } catch (Exception e){
            log("Invalid data type. Enter in a whole number for movement pulse.");
        }
    }

    @FXML
    public void setParameters(){
        setInferenceInterval();
        setConfidencePruneThreshold();
        setMovementPulse();
    }

    @FXML
    private void captureSingleImage(boolean call) {
        long imageIOTimeStart = System.nanoTime();
        this.currentBufferedImage = this.robot.createScreenCapture(new java.awt.Rectangle(640, 220, 640, 640));

        //  To File Processing
        try {
            File captureFile = new File("capture.jpeg");
            ImageIO.write(this.currentBufferedImage, "jpeg", captureFile);
            log("Wrote image file.");
        } catch (IOException e) {
            log("Did not write file.");
        }
        long imageIOTimeEnd = System.nanoTime();
        System.out.println("ImageIO TIME: " + (imageIOTimeEnd-imageIOTimeStart));

        this.currentImage = (Image) SwingFXUtils.toFXImage( this.currentBufferedImage, null);
        log("Drawing image capture.");
        drawCapture();


        drawAvoidanceThresholds();

        if (call){
            this.callCount++;
            long callStart = System.nanoTime();
            log("Calling TF model.");
            this.inference = callModel();
            this.inference.normalize();
            this.inference.pruneDetections( (float)this.confidencePruneThreshold );
            //System.out.println(this.inference.toString());

            long callEnd = System.nanoTime();
            System.out.println("TOTAL MODEL CALL " + this.callCount + " TIME: " + (callEnd-callStart));
            drawInferenceRect();
        }

        log("Drawing bounding box.");
        //if (this.inference != null) drawInferenceRect();


        //log("TESTER" + i.toString());
        //log("Test 2");
        //System.out.println("WHY IS THIS NOT WORKING");

    }

    private void drawAvoidanceThresholds(){
        this.canvas.getGraphicsContext2D().setStroke(Color.YELLOW);

        //  Left/Min Vertical Line of threshold
        this.canvas.getGraphicsContext2D().strokeLine(
                X_MIN_AVOIDANCE_THRESHOLD,
                MIN_CANVAS_COORD,
                X_MIN_AVOIDANCE_THRESHOLD,
                MAX_CANVAS_COORD);

        //  Right/Max Vertical Line of threshold
        this.canvas.getGraphicsContext2D().strokeLine(
                X_MAX_AVOIDANCE_THRESHOLD,
                MIN_CANVAS_COORD,
                X_MAX_AVOIDANCE_THRESHOLD,
                MAX_CANVAS_COORD);

        //  Top/Min Horizontal Line of threshold
        this.canvas.getGraphicsContext2D().strokeLine(
                MIN_CANVAS_COORD,
                Y_MIN_AVOIDANCE_THRESHOLD,
                MAX_CANVAS_COORD,
                Y_MIN_AVOIDANCE_THRESHOLD);

    }

    @FXML
    private void toggleContinuous() {
        //activateScreen();
        if (animationTimer == null) {
            this.animationTimer = new AnimationTimer() {
                private long lastToggle;
                private long fps;

                @Override
                public void handle(long now) {
                    if (fps == 0L) {
                        captureSingleImage(false);
                        fps = now;
                    } else {
                        long diff = now - fps;
                        //if (diff >= ONE_TWENTY_FPS_INTERVAL ) {
                        if (diff >= inferenceInterval) {
                            log("Capturing single image.");
                            captureSingleImage(true);
                            fps = now;

                            //  Detecting and toggling movement if collision detected
                            if(checkInferencesForCollisions() && avoidObjects) {
                                if(moving){
                                    log("Collision detected while moving, toggling movement to off.");
                                    log("BEFORE STOP: " + moving);
                                    stopMovement();
                                    log("DEBUG AFTER STOP: " + moving);
                                    log("Avoiding on left.");
                                    log("AVOIDING STATUS: " + avoiding);
                                    if (!avoiding){

                                        // left threshold
                                        double left = (leftThresholdOverlap > 0) ? Math.abs(leftThresholdOverlap) : 0;
                                        double right = (rightThresholdOverlap < 0) ? Math.abs(rightThresholdOverlap) : 0;

                                        if (left > right){
                                            log("Avoiding on right as left(" + left + ") > right(" + right+")");
                                            avoidOnRight();
                                            //avoidOnLeft();
                                        } else if (right >left){
                                            log("Avoiding on left as right(" + right + ") > left(" +right+")");
                                            avoidOnLeft();
                                        } else {
                                            log("Avoiding randomly as neither overlaps.");
                                            int leftRight = random.nextInt(100);
                                            if(leftRight%2==0){
                                                avoidOnLeft();
                                            } else {
                                                avoidOnRight();
                                            }
                                        }


                                    }
                                    log("AVOIDING STATUS: " + avoiding);
                                    log("BEFORE START: " + moving);
                                    startMovement();
                                    log("AFTER START: " + moving);
                                }
                            }
                        } else {
                            captureSingleImage(false);
                        }
//
//                        if(moving){
//                            MovementTask mTask = new MovementTask(movementPulseInterval);
//                            mTask.run();
//                        }
//                        if (moving){
//                            robot.keyPress(java.awt.event.KeyEvent.VK_W);
//                            robot.delay(movementPulseInterval);
//                            robot.keyRelease(java.awt.event.KeyEvent.VK_W);
//                        }
                    }
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

    private boolean checkInferencesForCollisions(){
        boolean collisionDetected = false;
        int inferencesLength = this.inference.getDetectionClass().length;
        log("Checking collisions.");
        for (int i = 0; i < inferencesLength; i++){
            float xMinDetection = this.inference.getxMin()[i];
            float yMinDetection = this.inference.getyMin()[i];
            float xMaxDetection = this.inference.getxMax()[i];
            float yMaxDetection = this.inference.getyMax()[i];

            float height = yMaxDetection-yMinDetection;
            float width = xMaxDetection-xMinDetection;
            float area = height*width;
            if (area < SEVEN_TWENTY_FOURTHS_CANVAS_AREA) {
                //  upper left corner
                if ((xMinDetection > X_MIN_AVOIDANCE_THRESHOLD && xMinDetection < X_MAX_AVOIDANCE_THRESHOLD) &&
                        (yMinDetection > Y_MIN_AVOIDANCE_THRESHOLD && yMinDetection < Y_MAX_AVOIDANCE_THRESHOLD))collisionDetected = true;

                //  lower left corner
                if ((xMinDetection > X_MIN_AVOIDANCE_THRESHOLD && xMinDetection < X_MAX_AVOIDANCE_THRESHOLD) &&
                        (yMaxDetection > Y_MIN_AVOIDANCE_THRESHOLD && yMaxDetection < Y_MAX_AVOIDANCE_THRESHOLD))collisionDetected = true;

                //  upper right corner
                if (( xMaxDetection > X_MIN_AVOIDANCE_THRESHOLD && xMaxDetection < X_MAX_AVOIDANCE_THRESHOLD ) &&
                        (yMinDetection > Y_MIN_AVOIDANCE_THRESHOLD && yMinDetection < Y_MAX_AVOIDANCE_THRESHOLD)) collisionDetected = true;

                //  lower right corner
                if ((xMaxDetection > X_MIN_AVOIDANCE_THRESHOLD && xMaxDetection < X_MAX_AVOIDANCE_THRESHOLD) &&
                        (yMaxDetection > Y_MIN_AVOIDANCE_THRESHOLD && yMaxDetection < Y_MAX_AVOIDANCE_THRESHOLD)) collisionDetected = true;

                //  break if detection
                if(collisionDetected) {
                    //  we care about this being positive
                    this.leftThresholdOverlap = X_MIN_AVOIDANCE_THRESHOLD - xMinDetection;

                    //  we care about this being negative
                    this.rightThresholdOverlap = X_MAX_AVOIDANCE_THRESHOLD - xMaxDetection;
                    break;
                }
            } else {
                log("Detection ignored, greater than a quarter of canvas.");
            }
        }
        log("Collision status: " + collisionDetected);
        return collisionDetected;
    }

    @FXML
    private void toggleMovement() {
        if (!this.screenActive){
            activateScreen();
        }
        if (this.moveTimer == null) {
            this.moveTimer = new AnimationTimer() {
                private long lastToggle;
                private long fps;

                @Override
                public void handle(long now) {
                    if (fps == 0L) {
                        fps = now;
                    } else {
                        long diff = now - fps;
                        if (diff >= ONE_SECOND_INTERVAL ) {
                            log("Moving forward.");
                            long callStart = System.nanoTime();
                            if (isAvoiding == null || isAvoiding.isDone()){

                                MOVEMENT_THREAD_POOL.submit(new MovementTask(movementPulseInterval));
                            }
                            long callEnd = System.nanoTime();
                            System.out.println("Time to move: " + (callEnd-callStart));
                            fps = now;
                        }
                    }
                }
            };
        }

        if (moving) {
            this.moving = false;
            log("Stopping movement.");
            this.moveTimer.stop();
        } else {
            this.moving = true;
            log("Starting movement.");
            this.moveTimer.start();
        }
    }

    public void startMovement(){
        if (!screenActive){
            activateScreen();
        }
        if (this.moveTimer == null) initializeMoveTimer();
        this.moveTimer.start();
        this.moving = true;
    }
    public void stopMovement(){
        if (!screenActive){
            activateScreen();
        }
        if (this.moveTimer == null) initializeMoveTimer();
        this.moveTimer.stop();
        this.moving=false;
    }
    private void initializeMoveTimer(){
        this.moveTimer = new AnimationTimer() {
            private long lastToggle;
            private long fps;

            @Override
            public void handle(long now) {
                if (fps == 0L) {
                    fps = now;
                } else {
                    long diff = now - fps;
                    if (diff >= ONE_SECOND_INTERVAL ) {
                        log("Moving set to start.");
                        long callStart = System.nanoTime();
                        if (isAvoiding == null || isAvoiding.isDone()){
                            if (isAvoiding ==null || isAvoiding.isDone()) avoiding = false;
                            MOVEMENT_THREAD_POOL.submit(new MovementTask(movementPulseInterval));
                        } else {
                            log("AVOIDING NULL STATUS: " + (isAvoiding == null));
                            log("AVOIDING STATUS: " + isAvoiding.isDone());
                        }
                        long callEnd = System.nanoTime();
                        System.out.println("Time to move: " + (callEnd-callStart));
                        fps = now;
                    }
                }
            }
        };
    }

    @FXML
    private void avoidOnLeft() {
        this.avoiding = true;
        activateScreen();
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(150, java.awt.event.KeyEvent.VK_A));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(500, java.awt.event.KeyEvent.VK_W));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(150, java.awt.event.KeyEvent.VK_D));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(600, java.awt.event.KeyEvent.VK_W));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(150, java.awt.event.KeyEvent.VK_D));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(500, java.awt.event.KeyEvent.VK_W));
        this.isAvoiding = MOVEMENT_THREAD_POOL.submit( (Callable<? extends Object>) new TurnTask(150, java.awt.event.KeyEvent.VK_A));
    }

    @FXML
    private void avoidOnRight() {
        this.avoiding = true;
        activateScreen();
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(150, java.awt.event.KeyEvent.VK_D));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(500, java.awt.event.KeyEvent.VK_W));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(150, java.awt.event.KeyEvent.VK_A));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(600, java.awt.event.KeyEvent.VK_W));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(150, java.awt.event.KeyEvent.VK_A));
        MOVEMENT_THREAD_POOL.submit( (Runnable) new TurnTask(500, java.awt.event.KeyEvent.VK_W));
        this.isAvoiding = MOVEMENT_THREAD_POOL.submit( (Callable<? extends Object>) new TurnTask(150, java.awt.event.KeyEvent.VK_D));
    }



    @FXML
    private void drawCapture() {
        this.canvas.getGraphicsContext2D().drawImage(this.currentImage, 0, 0);
    }

    private void drawRect(){
        log("Drawing Rect");
        prepResults();
        this.canvas.getGraphicsContext2D().setStroke(Color.RED);
        this.canvas.getGraphicsContext2D().strokeRect(
                this.rectDimensions[0],
                this.rectDimensions[1],
                this.rectDimensions[2],
                this.rectDimensions[3]
        );
    }

    private void drawInferenceRect(){
        log("Drawing Rect");

        float[] xmin = this.inference.getxMin();
        float[] ymin = this.inference.getyMin();
        float[] xmax = this.inference.getxMax();
        float[] ymax = this.inference.getyMax();
        float[] detectionClasses = this.inference.getDetectionClass();

        log( "Inferences Detection Classes length: " + detectionClasses.length);

        this.canvas.getGraphicsContext2D().setStroke(Color.RED);
        for (int i = 0; i < xmin.length; i++) {
            String bboxLabel = "BBOX-"+i + " : " + DetectionClass.getEnumDescriptionById( (int)detectionClasses[i]);

            float currentXMin = xmin[i];
            float currentYMin = ymin[i];
            float currentXMax = xmax[i];
            float currentYMax = ymax[i];

            float xMid = currentXMax - currentXMin;
            float yMid = currentYMax - currentYMin;

            log("Midpoint: " + xMid + ", " + yMid);

            this.canvas.getGraphicsContext2D().strokeRect(
                    xmin[i],
                    ymin[i],
                    xmax[i]-xmin[i],
                    ymax[i]-ymin[i]
            );
            this.canvas.getGraphicsContext2D().strokeText(bboxLabel, xmin[i], ymin[i]);
        }
    }

    private void prepResults(){
        int xMin = (int) (this.results[1]*640);
        int yMin = (int) (this.results[2]*640);
        int xMax = (int) (this.results[3]*640);
        int yMax = (int) (this.results[4]*640);

        int width = xMax - xMin;
        int height = yMax - yMin;

        this.rectDimensions[0] = xMin;
        this.rectDimensions[1] = yMin;
        this.rectDimensions[2] = width;
        this.rectDimensions[3] = height;

        log(xMin + " " + yMin + " " + width + " " + height);
    }

    public Inference callModel(){
        //this.model.call(this.currentBufferedImage);
        return this.model.call("capture.jpeg");
    }

    @FXML
    private void exit() {
        Platform.exit();
        System.exit(0);
    }

    private void log(String logEntry) {
        //System.out.println("Log updated.");
        logArea.setText(this.sb.append(getTime()).append(" : ").append(logEntry).append("\n").toString());
        logArea.appendText("");
        //logArea.setScrollTop(Double.MAX_VALUE);
    }

    private String getTime() {
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private void activateScreen(){
        // BASH HAS TO BE RUN IN ADMIN MODE TO ALLOW AWT.ROBOT TO CLICK AND MOVE FOCUS TO OTHER PROGRAMS
        log("Activating Screen");
        this.robot.mouseMove(512,512);
        this.robot.delay(500);
        log("Mouse Click One");
        this.robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        this.robot.delay(1000);
        this.robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        log("Mouse Click Two");
        this.robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        this.robot.delay(1000);
        this.robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        this.screenActive = true;
    }

    @FXML
    private void loadModel(){
        log("Loading model.");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Model Directory");
        final File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null){
            log("Model loaded from: " + selectedDirectory.getAbsolutePath());
            model.setSmb(selectedDirectory.getAbsolutePath());
            captureSingleImage(false);
            callModel();
        }
        captureMenuItem.setDisable(false);
    }

    @FXML
    private void toggleObjectAvoidance(){
        this.avoidObjects = !this.avoidObjects;
        log("Toggling object avoidance to " + this.avoidObjects);
    }
}