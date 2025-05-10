package tage.nodeControllers;
import tage.*;
import org.joml.*;
import java.lang.Math;

/**
* A ShootingController is a node controller that, when enabled, causes any object
* it is attached to shoot off forwards.
*/
public class ShootingController extends NodeController
{
	private float movementSpeed, elapsedTime = 1.0f;
	private Vector3f curLocation, newLocation;
	private Engine engine;
	private float distanceTraveled = 0.0f;
	private Matrix4f force;

	/** Creates a bobbing controller with vertical axis, and speed=1.0. */
	public ShootingController() { super(); }

	/** Creates a shooting controller with rotation axis and speed as specified. */
	public ShootingController(Engine e, float speed)
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
		//force = new Matrix4f().getLocalForwardVector().mul(movementSpeed);
		go.getPhysicsObject().applyForce(1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
	}
}