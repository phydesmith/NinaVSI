package io.javasmithy.robot;

import io.javasmithy.inference.Model;

import java.awt.event.KeyEvent;

public class InferenceTask extends RobotTask{
    private Model model;

    public InferenceTask(){
        super();
    }

    public InferenceTask(Model model){
        super();
        this.model = model;
    }

    @Override
    public void run(){
        this.model.call("capture.jpg");
    }
}
