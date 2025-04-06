package a2;

import org.joml.*;
import tage.GameObject;
import tage.Camera;
import tage.input.action.*;
import net.java.games.input.Event;

public class DisarmAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;

    public DisarmAction(MyGame g){
        game = g;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        game.setDisarming(true);
    }
}