package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class MoveAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float movementSpeed;

    public MoveAction(MyGame g, float speed){
        game = g;
        movementSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.move(movementSpeed * e.getValue());
    }
}