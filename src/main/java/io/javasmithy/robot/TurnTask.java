package io.javasmithy.robot;

import java.awt.event.KeyEvent;
import java.util.concurrent.Callable;

public class TurnTask extends RobotTask implements Callable<Boolean> {

    int milliseconds;
    int keyPress;

    public TurnTask(){
        super();
    }

    public TurnTask(int milliseconds, int keyPress){
        super();
        this.milliseconds = milliseconds;
        this.keyPress = keyPress;
    }

    @Override
    public void run(){
        this.robot.keyPress(this.keyPress);
        this.robot.delay(this.milliseconds);
        this.robot.keyRelease(this.keyPress);
    }

    @Override
    public Boolean call(){
        this.robot.keyPress(this.keyPress);
        this.robot.delay(this.milliseconds);
        this.robot.keyRelease(this.keyPress);
        return true;
    }

}
