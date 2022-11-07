package io.javasmithy.robot;

import java.awt.event.KeyEvent;


public class MovementTask extends RobotTask{
    int milliseconds;

    public MovementTask(){
        super();
    }

    public MovementTask(int milliseconds){
        super();
        this.milliseconds = milliseconds;
    }

    @Override
    public void run(){
        this.robot.keyPress(KeyEvent.VK_W);
        this.robot.delay(this.milliseconds);
        this.robot.keyRelease(KeyEvent.VK_W);
    }

}

