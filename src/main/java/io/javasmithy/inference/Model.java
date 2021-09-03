package io.javasmithy.inference;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.types.TFloat32;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
    private final String IMGPATH = "captures/inferences/currentCapture.jpeg";
    private final String OUTPUTPATH = "captures/inferences/currentInference.jpeg";
    SavedModelBundle smb;

    public Model(){
        this.smb = SavedModelBundle.load("src/main/resources/io/javasmithy/model_1/saved_model", "serve");
    }

    public List<DetectedObject> call(BufferedImage image){
        System.out.println("Calling Model.");
        List<DetectedObject> detectedObjects = new ArrayList<>();
        List<Tensor> outputTensorsList = null;
        Map<String, Tensor> outputMap = null;

        List<BufferedImage> inputs = new ArrayList<>();
        inputs.add(image);

        System.out.println("Preprocessing.");
        try (TFloat32 input = Preprocess.preprocess(inputs, image.getHeight(), image.getWidth(), 3)) {
            Map<String, Tensor> inputMap = new HashMap();
            inputMap.put("input_tensor", input);
            System.out.println("Inputs readied.");
            outputMap = smb.function("serving_Default").call(inputMap);
            System.out.println("Outputs completed.");
        }
        System.out.println("Call Finished.");

        TFloat32 scoresT = (TFloat32) outputMap.get("detection_scores");
        TFloat32 classesT = (TFloat32) outputMap.get("dection_classes");
        TFloat32 boxesT = (TFloat32) outputMap.get("detection_boxes");

        System.out.println("Scores get float 0: " + scoresT.asRawTensor().data().asFloats().getFloat(0));
        System.out.println("Scores datatype: " + scoresT.dataType());
        System.out.println("Scores getFloat 0 15: " + scoresT.getFloat(0, 15));
        System.out.println("Scores getFloat 0 2: " + scoresT.getFloat(0, 2));

        return detectedObjects;
    }
}