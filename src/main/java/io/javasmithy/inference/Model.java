package io.javasmithy.inference;

import org.tensorflow.Graph;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.op.Ops;
import org.tensorflow.op.core.Constant;
import org.tensorflow.op.core.Reshape;
import org.tensorflow.op.image.DecodePng;
import org.tensorflow.op.image.DecodePng.Options;
import org.tensorflow.op.io.ReadFile;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TString;
import org.tensorflow.types.TUint8;

import java.util.HashMap;
import java.util.Map;

public class Model {
    //private final String INPUT_PATH = "target/classes/io.javasmithy/captures/capture.png";
    private final String DEFAULT_INPUT_PATH = "target/classes/io.javasmithy/captures/capture.png";
    private final String OUTPUT_PATH = "captures/inferences/currentInference.jpeg";
    //private  final String PATH = "target/classes/io.javasmithy/captures/";
    //private  final String PATH = "test-images/";
    private  final String PATH = "";

    //private final String SMB_PATH = "target/classes/io.javasmithy/model/saved_model";
    //private final String SMB_PATH = "D:\\projects\\NinaVSI\\target\\classes\\io\\javasmithy\\model\\ssd-resnet152\\saved_model";
    private final String SMB_PATH = "D:\\projects\\NinaVSI\\target\\classes\\io\\javasmithy\\model\\mobilenet\\saved_model";
    private final String SMB_SERVE = "serve";
    private final String INPUT_TENSOR = "input_tensor";
    private final String SMB_SERVING_DEFAULT = "serving_default";

    private final String OUTPUT_DETECTION_CLASSES = "detection_classes";
    private final String OUTPUT_DETECTION_BOXES = "detection_boxes";
    private final String OUTPUT_DETECTION_SCORES = "detection_scores";
    private SavedModelBundle smb;


    public Model(){
        this.smb = SavedModelBundle.load(SMB_PATH, SMB_SERVE);
    }

    public Inference call(String fileName){

        String inputPath = "";
        if (fileName.equals("")){
            inputPath = DEFAULT_INPUT_PATH;
        } else {
            inputPath = PATH + fileName;
        }
        Inference inference = new Inference();
        try (Graph g = new Graph(); Session s = new Session(g)) {
            Ops tf = Ops.create(g);
            Constant<TString> tfFileName = tf.constant(inputPath);
            ReadFile readFile = tf.io.readFile(tfFileName);
            Session.Runner runner = s.runner();
            Options[] options = new Options[1];
            options[0] = DecodePng.channels(3L);
            DecodePng decodeImage = tf.image.decodePng(readFile.contents(), options);
            Shape imageShape = runner.fetch(decodeImage).run().get(0).shape();
            Reshape<TUint8> reshape = tf.reshape(
                    decodeImage,
                    tf.array(1,
                            imageShape.asArray()[0],
                            imageShape.asArray()[1],
                            imageShape.asArray()[2]
                    )
            );

            try (TUint8 reshapeTensor = (TUint8) s.runner().fetch(reshape).run().get(0)) {
                Map<String, Tensor> feedDict = new HashMap<>();
                feedDict.put(INPUT_TENSOR, reshapeTensor);
                Map<String, Tensor> outputTensorMap = this.smb.function(SMB_SERVING_DEFAULT).call(feedDict);

                try (TFloat32 detectionClasses = (TFloat32) outputTensorMap.get(OUTPUT_DETECTION_CLASSES);
                     TFloat32 detectionBoxes = (TFloat32) outputTensorMap.get(OUTPUT_DETECTION_BOXES);
                     TFloat32 detectionScores = (TFloat32) outputTensorMap.get(OUTPUT_DETECTION_SCORES)) {
                    float[] detectionScoresArr = new float[100];
                    float[] detectionClassArr = new float[100];
                    float[] xMins = new float[100];
                    float[] yMins = new float[100];
                    float[] xMaxes = new float[100];
                    float[] yMaxes = new float[100];
                    for (int i = 0; i < 100; i += 1) {
                        detectionScoresArr[i] = detectionScores.getFloat(0, i);
                        detectionClassArr[i] = detectionClasses.getFloat(0, i);
                        yMins[i] = detectionBoxes.get(0).getFloat(i, 0);
                        xMins[i] = detectionBoxes.get(0).getFloat(i, 1);
                        yMaxes[i] = detectionBoxes.get(0).getFloat(i, 2);
                        xMaxes[i] = detectionBoxes.get(0).getFloat(i, 3);
                    }

                    long infStart = System.nanoTime();
                    inference.setFileName(fileName);
                    inference.setDetectionScores(detectionScoresArr);
                    inference.setDetectionClass(detectionClassArr);
                    inference.setxMin(xMins);
                    inference.setyMin(yMins);
                    inference.setxMax(xMaxes);
                    inference.setyMax(yMaxes);
                    long inferenceEnd = System.nanoTime();
                    System.out.println("INFERENCE CLASS INITIALIZATION TIME: " + (inferenceEnd - infStart));
                }
            }
        }
        return inference;
    }
}