package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class SprintAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float pitchSpeed;
    private ProtocolClient protClient;
    private float sprintSpeed;

    public SprintAction(MyGame g, ProtocolClient p, float speed){
        game = g;
		protClient = p;
        sprintSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        game.setMovementSpeed((float)(sprintSpeed * game.getDeltaTime() * e.getValue()));
    }
}