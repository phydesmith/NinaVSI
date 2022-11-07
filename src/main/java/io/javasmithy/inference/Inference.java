package io.javasmithy.inference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inference {
    private float[] detectionScores;
    private float[] detectionClass;
    private float[] xMin;
    private float[] yMin;
    private float[] xMax;
    private float[] yMax;
    private String fileName;
    private boolean normalized;

    public Inference(){

    }

    public Inference(String fileName, float[] detectionScores, float[] detectionClass, float[] xMin, float[] yMin, float[] xMax, float[] yMax) {
        this.fileName = fileName;
        this.detectionScores = detectionScores;
        this.detectionClass = detectionClass;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
    }

    public void pruneDetections(float confidenceThreshold){
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < this.detectionScores.length; i++){
            if (this.detectionScores[i] > confidenceThreshold){
                indices.add(i);
            }
        }

        int arrLength = indices.size();
        float[] newDetectionScores = new float[arrLength];
        float[] newDetectionClass = new float[arrLength];
        float[] newXMin = new float[arrLength];
        float[] newYMin = new float[arrLength];
        float[] newXMax = new float[arrLength];
        float[] newYMax = new float[arrLength];

        for (int i = 0; i < indices.size(); i++){
            int index = indices.get(i);
            newDetectionScores[i] = this.detectionScores[index];
            newDetectionClass[i] = this.detectionClass[index];
            newXMin[i] = this.xMin[index];
            newYMin[i] = this.yMin[index];
            newXMax[i] = this.xMax[index];
            newYMax[i] = this.yMax[index];
        }

        this.detectionClass = newDetectionClass;
        this.detectionScores = newDetectionScores;
        this.xMin = newXMin;
        this.yMin = newYMin;
        this.xMax = newXMax;
        this.yMax = newYMax;



    }

    public void normalize(){
        if (!this.normalized){
            for (int i = 0; i < this.xMax.length; i++){
                this.xMin[i] = this.xMin[i] *640;
                this.yMin[i] = this.yMin[i] *640;
                this.xMax[i] = this.xMax[i] *640;
                this.yMax[i] = this.yMax[i] *640;
            }
            this.normalized = true;
        }
    }

    @Override
    public String toString() {
        return "Inference{" +
                "detectionClass=" + Arrays.toString(detectionClass) +
                ", xMin=" + Arrays.toString(xMin) +
                ", yMin=" + Arrays.toString(yMin) +
                ", xMax=" + Arrays.toString(xMax) +
                ", yMax=" + Arrays.toString(yMax) +
                '}';
    }

    public String formattedToString(){
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 100; i++){
            sb.append(
                    this.fileName + "," +
                    this.detectionClass[i] + "," +
                    this.detectionScores[i] + "," +
                    this.xMin[i] + "," +
                    this.yMin[i] + "," +
                    this.xMax[i] + "," +
                    this.yMax[i] + "\n"
            );
        }
        return sb.toString();
    }

    public boolean isNormalized() {
        return normalized;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }

    public float[] getDetectionScores() {
        return detectionScores;
    }

    public void setDetectionScores(float[] detectionScores) {
        this.detectionScores = detectionScores;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public float[] getDetectionClass() {
        return detectionClass;
    }

    public void setDetectionClass(float[] detectionClass) {
        this.detectionClass = detectionClass;
    }

    public float[] getxMin() {
        return xMin;
    }

    public void setxMin(float[] xMin) {
        this.xMin = xMin;
    }

    public float[] getyMin() {
        return yMin;
    }

    public void setyMin(float[] yMin) {
        this.yMin = yMin;
    }

    public float[] getxMax() {
        return xMax;
    }

    public void setxMax(float[] xMax) {
        this.xMax = xMax;
    }

    public float[] getyMax() {
        return yMax;
    }

    public void setyMax(float[] yMax) {
        this.yMax = yMax;
    }
}
