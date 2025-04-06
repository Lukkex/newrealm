package newrealm;

import tage.input.action.*;
import net.java.games.input.Event;

public class DisableAxisAction extends AbstractInputAction{
    private MyGame game;

    public DisableAxisAction(MyGame g){
        game = g;
    }

    @Override
    public void performAction(float time, Event e){
        game.toggleXYZAxis();
    }
}