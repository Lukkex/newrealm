package tage;
import org.joml.*;
import java.lang.Math;
import tage.GameObject;
import tage.Camera;
import tage.input.*;
import tage.input.action.*;
import net.java.games.input.Event;

/**
* Orbit camera controller around a given game object
*/
public class CameraOrbit3D { 
    private Engine engine;
    private Camera camera; // the camera being controlled
    private GameObject avatar; // the target avatar the camera looks at
    private float cameraAzimuth; // rotation around target Y axis
    private float cameraElevation; // elevation of camera above target
    private float cameraRadius; // distance between camera and target
    private float azimuthSpeed = 1f;
    private float zoomSpeed = 0.5f;
    private float elevationSpeed = 2f;
    private String gp;

    public CameraOrbit3D(Camera cam, GameObject av, String gpName, Engine e){ 
        engine = e;
        camera = cam;
        avatar = av;
        cameraAzimuth = 0.0f; // start BEHIND and ABOVE the target
        cameraElevation = 20.0f; // elevation is in degrees
        cameraRadius = 4.0f; // distance from camera to avatar
        gp = gpName;
        setupInputs(e);
        updateCameraPosition();
    }

    /**
     * Sets up inputs for Gamepads and Keyboards - for Gamepads the default is the X & Y axes for the right analog stick - for Keyboards, it is IJKL, and U & O
     * @param e
     */
    private void setupInputs(Engine e){ 
        OrbitAzimuthAction azmAction = new OrbitAzimuthAction(azimuthSpeed);
        OrbitAzimuthAction _azmAction = new OrbitAzimuthAction(-azimuthSpeed);
        OrbitRadiusAction radAction = new OrbitRadiusAction(zoomSpeed);
        OrbitRadiusAction _radAction = new OrbitRadiusAction(-zoomSpeed);
        OrbitElevationAction elvAction = new OrbitElevationAction(elevationSpeed);
        OrbitElevationAction _elvAction = new OrbitElevationAction(-elevationSpeed);

        InputManager im = e.getInputManager();

        if (gp != null){ //No gamepad, use keyboard and mouse instead
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RX, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
            im.associateAction(gp, net.java.games.input.Component.Identifier.Axis.RY, radAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        }
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.L, azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.J, _azmAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.I, radAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.K, _radAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.O, elvAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
        im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.U, _elvAction, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
    }

    /** Compute the camera’s azimuth, elevation, and distance, relative to
      * the target in spherical coordinates, then convert to world Cartesian
      * coordinates and set the camera position from that.*/
    public void updateCameraPosition(){ 
        Vector3f avatarRot = avatar.getWorldForwardVector();
        double avatarAngle = Math.toDegrees((double)

        avatarRot.angleSigned(new Vector3f(0,0,-1), new Vector3f(0,1,0)));

        float totalAz = cameraAzimuth - (float)avatarAngle;
        double theta = Math.toRadians(totalAz);
        double phi = Math.toRadians(cameraElevation);

        float x = cameraRadius * (float)(Math.cos(phi) * Math.sin(theta));
        float y = cameraRadius * (float)(Math.sin(phi));
        float z = cameraRadius * (float)(Math.cos(phi) * Math.cos(theta));

        camera.setLocation(new Vector3f(x,y,z).add(avatar.getWorldLocation()));
        camera.lookAt(avatar);
    }

    private class OrbitAzimuthAction extends AbstractInputAction { 
        float rotAmount;
       
        public OrbitAzimuthAction(float speed){
            rotAmount = speed;
        }

        public void performAction(float time, Event event){ 
            cameraAzimuth += rotAmount * event.getValue();
            updateCameraPosition();
        } 
    }

    private class OrbitElevationAction extends AbstractInputAction { 
        float rotAmount;
        
        public OrbitElevationAction(float speed){
            rotAmount = speed;
        }

        public void performAction(float time, Event event){ 
            cameraElevation += rotAmount * event.getValue();
            updateCameraPosition();
        } 
    }

    private class OrbitRadiusAction extends AbstractInputAction { 
        float rotAmount;
        
        public OrbitRadiusAction(float speed){
            rotAmount = speed;
        }

        public void performAction(float time, Event event){ 
            if ((cameraRadius + rotAmount * event.getValue()) > 1.0f && (cameraRadius + rotAmount * event.getValue()) < 40.0f)
                cameraRadius += rotAmount * event.getValue();
            updateCameraPosition();
        } 
    }
}
