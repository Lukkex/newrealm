package tage.nodeControllers;
import tage.*;
import org.joml.*;
import java.lang.Math;

/**
* A BobbingController is a node controller that, when enabled, causes any object
* it is attached to bob up (essentially, it will fly into the sky).
* functionality will be added eventually so you can also bob back and forth, up and down
*/
public class BobbingController extends NodeController
{
	private float movementSpeed, elapsedTime = 1.0f;
	private Vector3f curLocation, newLocation;
	private Engine engine;

	/** Creates a bobbing controller with vertical axis, and speed=1.0. */
	public BobbingController() { super(); }

	/** Creates a bobbing controller with rotation axis and speed as specified. */
	public BobbingController(Engine e, float speed)
	{	super();
		movementSpeed = speed;
		engine = e;
	}

	/** sets the movement speed when the controller is enabled */
	public void setSpeed(float s) { movementSpeed = s; }

	/** This is called automatically by the RenderSystem (via SceneGraph) once per frame
	*   during display().  It is for engine use and should not be called by the application.
	*/
	public void apply(GameObject go){	
		elapsedTime = super.getElapsedTime();
		curLocation = go.getLocalLocation();
		newLocation = curLocation.add(go.getLocalUpVector().mul((float) Math.sin(elapsedTime * movementSpeed)));
		go.setLocalLocation(newLocation);

		//If the object flies too high up, stop rendering it
		if (newLocation.y() > 40f){
			go.getRenderStates().disableRendering();
		}
	}
}