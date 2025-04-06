package a2;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class PitchAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float pitchSpeed;

    public PitchAction(MyGame g, float speed){
        game = g;
        pitchSpeed = speed;
    }

    @Override
    public void performAction(float time, Event e){
        obj = game.getAvatar();
        obj.pitch(pitchSpeed * e.getValue());
    }
}