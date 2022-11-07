module NinaVSI {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.datatransfer;
    requires java.desktop;
    requires javafx.swing;
    requires org.tensorflow.core.api;
    requires org.tensorflow.core.platform.gpu;
    requires org.tensorflow.ndarray;
    //requires org.tensorflow;
    //requires java.net.http;
    //requires com.google.gson;
    //requires java.desktop;

    opens io.javasmithy.controller to javafx.base, javafx.fxml, javafx.controls, javafx.graphics, com.google.gson;
    //opens io.javasmithy.geo to javafx.base, javafx.fxml, javafx.controls, javafx.graphics, com.google.gson;
    opens io.javasmithy to javafx.base, javafx.fxml, javafx.controls, javafx.graphics, com.google.gson;

}