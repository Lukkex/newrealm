package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float movementSpeed;
    private ProtocolClient protClient;

    public MoveAction(MyGame g, ProtocolClient p, float speed){
        game = g;
		protClient = p;
        movementSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.move(movementSpeed * e.getValue());
        protClient.sendMoveMessage(obj.getWorldLocation());
    }
}