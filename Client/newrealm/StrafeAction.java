package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class StrafeAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float movementDirection;
    private ProtocolClient protClient;

    //* Direction is 1 or -1 */
    public StrafeAction(MyGame g, ProtocolClient p, float direction){
        game = g;
		protClient = p;
        movementDirection = direction;
    }

    @Override
    public void performAction(float time, Event e){
        System.out.println("\nDirection: " + movementDirection);
        obj = game.getAvatar();
        obj.strafe(game.getMovementSpeed() * movementDirection * (float) game.getDeltaTime() * e.getValue());
        protClient.sendMoveMessage(obj.getWorldLocation());
    }
}