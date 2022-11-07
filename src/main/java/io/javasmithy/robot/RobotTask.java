package io.javasmithy.robot;

import java.awt.*;

public class RobotTask extends Thread {
    protected Robot robot;

    public RobotTask(){
        createRobot();
    }

    private void createRobot(){
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(250);
        } catch (AWTException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run(){}
}
