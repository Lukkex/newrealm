package tage;
import org.joml.*;
import java.lang.Math;
import tage.GameObject;
import tage.Camera;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.Event;

/**
* Minimap camera controller
* permits movement along the X and Z axes, with zooming functionality along the Y axis
* @author Hunter Brown
*/
public class CameraMinimap { 
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraX; // rotation around target Y axis
    private float cameraZ; // elevation of camera above target
    private float cameraZoom; // distance between camera and target
    private float XSpeed = 5f;
    private float ZSpeed = 5f;
    private float zoomSpeed = 0.5f;
    private String gp;

    public CameraMinimap(Camera cam, GameObject av, String gpName, Engine e){ 
        engine = e;
        camera = cam;
        avatar = av;
        cameraX = 0.0f; // Start at origin
        cameraZ = 0.0f; // Start at origin
        cameraZoom = 10.0f; // distance from camera to ground level
        gp = gpName;
        setupInputs(e);
        updateCameraPosition();
    }

    /**
     * Sets up inputs for Gamepads and Keyboards - for Gamepads the default is the X & Y axes for the right analog stick - for Keyboards, it is IJKL, and U & O
     * @param e
     */
    private void setupInputs(Engine e){ 
        PanXAction XAction = new PanXAction(XSpeed);
        PanXAction _XAction = new PanXAction(-XSpeed);
        PanYAction ZAction = new PanYAction(ZSpeed);
        PanYAction _ZAction = new PanYAction(-ZSpeed);
        ZoomAction zoomAction = new ZoomAction(zoomSpeed);
        ZoomAction _zoomAction = new ZoomAction(-zoomSpeed);

        InputManager im = e.getInputManager();

        if (gp != null){ //No gamepad, use keyboard and mouse instead
            //im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            //im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, radAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.F, _XAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.H, XAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.T, _ZAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.G, ZAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Y, _zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.R, zoomAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    /**
     * Set the camera's new position based on the cameraX, cameraZ, and cameraZoom values and force the camera to look straight down
     */
    public void updateCameraPosition(){ 
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math.toDegrees((double)

        avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));

        float totalAz = cameraX;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraZ);

        float x = (float) theta;
        float y = cameraZoom;
        float z = (float) phi;

        camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation()));
        camera.lookAt(camera.getLocation().mul(new Vector3f(1f, 0f, 1f)).add(new Vector3f(0f, -1f, 0f))); //Look down always
    }

    private class PanXAction extends AbstractInputAction { 
        float movementSpeed;
       
        public PanXAction(float speed){
            movementSpeed = speed;
        }

        public void performAction(float time, Event event){ 
            cameraX += movementSpeed * event.getValue();
            updateCameraPosition();
        } 
    }

    private class PanYAction extends AbstractInputAction { 
        float movementSpeed;
        
        public PanYAction(float speed){
            movementSpeed = speed;
        }

        public void performAction(float time, Event event){ 
            cameraZ += movementSpeed * event.getValue();
            updateCameraPosition();
        } 
    }

    private class ZoomAction extends AbstractInputAction { 
        float movementSpeed;
        
        public ZoomAction(float speed){
            movementSpeed = speed;
        }

        public void performAction(float time, Event event){ 
            if ((cameraZoom + movementSpeed * event.getValue()) > 1.0f && (cameraZoom + movementSpeed * event.getValue()) < 40.0f)
                cameraZoom += movementSpeed * event.getValue();
            updateCameraPosition();
        } 
    }
}
