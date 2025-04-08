package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class TurnAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float turnSpeed;
    private ProtocolClient protClient;

    public TurnAction(MyGame g, ProtocolClient p, float speed){
        game = g;
		protClient = p;
        turnSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.yaw(turnSpeed * e.getValue());
        System.out.println("Rotation: " + obj.getWorldRotation());
        protClient.sendRotateMessage(obj.getWorldRotation());
    }
}