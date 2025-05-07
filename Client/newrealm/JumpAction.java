package newrealm;

import tage.GameObject;
import tage.input.action.*;
import net.java.games.input.Event;

public class JumpAction extends AbstractInputAction{
    private MyGame game;
    private GameObject obj;
    private float jumpAmount;
    private float amt = 0.0f;
    private boolean isJumping = false;
    private ProtocolClient protClient;

    public JumpAction(MyGame g, ProtocolClient p, float jumpAmt){
        game = g;
		protClient = p;
        jumpAmount = jumpAmt;
    }

    @Override
    public void performAction(float time, Event e){
        if (!isJumping){
            obj = game.getAvatar();
            obj.jump(jumpAmount);
            protClient.sendMoveMessage(obj.getWorldLocation());
        }
    }
}