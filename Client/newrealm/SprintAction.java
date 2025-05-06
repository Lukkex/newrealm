package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class SprintAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float pitchSpeed;
    private ProtocolClient protClient;

    public SprintAction(MyGame g, ProtocolClient p, float speed){
        game = g;
		protClient = p;
        pitchSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.pitch(pitchSpeed * e.getValue());
        protClient.sendRotateMessage(obj.getWorldRotation());
    }
}