package io.javasmithy.inference;

public class DetectedObject {

    private float detectionScore;
    private float detectionClass;
    private float[] detectionBox;

    public DetectedObject(float detectionScore, float detectionClass, float[] detectionBox) {
        this.detectionBox = detectionBox;
        this.detectionScore = detectionScore;
        this.detectionClass = detectionClass;
    }

    public float getDetectionScore() {
        return detectionScore;
    }

    public float getDetectionClass() {
        return detectionClass;
    }

    public void setDetectionClass(float detectionClass) {
        this.detectionClass = detectionClass;
    }

    public float[] getDetectionBox() {
        return detectionBox;
    }

    public void setDetectionBox(float[] detectionBox) {
        this.detectionBox = detectionBox;
    }

    public void setDetectionScore(float detectionScore) {
        this.detectionScore = detectionScore;
    }

    public double[] getCoords(){
        // TODO
        return new double[4];
    }
}
