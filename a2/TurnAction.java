package a2;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class TurnAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float turnSpeed;

    public TurnAction(MyGame g, float speed){
        game = g;
        turnSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.yaw(turnSpeed * e.getValue());
    }
}