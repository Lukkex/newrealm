package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float movementDirection;
    private ProtocolClient protClient;

    //* Direction is 1 or -1 */
    public MoveAction(MyGame g, ProtocolClient p, float direction){
        game = g;
		protClient = p;
        movementDirection = direction;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.move(game.getMovementSpeed() * movementDirection * (float) game.getDeltaTime() * e.getValue());
        protClient.sendMoveMessage(obj.getWorldLocation());
    }
}