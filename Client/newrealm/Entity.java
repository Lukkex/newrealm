package newrealm;

import java.util.UUID;

import tage.*;
import tage.shapes.*;
import org.joml.*;

// A ghost MUST be connected as a child of the root,
// so that it will be rendered, and for future removal.
// The ObjShape and TextureImage associated with the ghost
// must have already been created during loadShapes() and
// loadTextures(), before the game loop is started.

public class Entity extends GameObject
{
	private UUID uuid;
	private int int_id;
	private GameObject go;
	private float movementSpeed = 0.01f; //Default Speed
	private String type = "";
	private final int DEFAULT_FIRING_COOLDOWN = 50000; //500 ticks
	private int firingCooldown;
	private int firingCooldownDecrement = 1;
	private boolean state = false;

	public Entity(){
		super();
	}

	public Entity(GameObject go){
		this.go = go;
	}

	public Entity(int id, ObjShape s, TextureImage t, String type) {	
		super(GameObject.root(), s, t);
		int_id = id;
		this.type = type;
		this.firingCooldown = DEFAULT_FIRING_COOLDOWN;
	}
	
	public Entity(int id, AnimatedShape s, TextureImage t, Vector3f p, boolean renderHidden, String type) {	
		super(GameObject.root(), s, t);
		int_id = id;
		setPosition(p);
		this.getRenderStates().setRenderHiddenFaces(renderHidden);
		this.type = type;
		this.firingCooldown = DEFAULT_FIRING_COOLDOWN;
	}

	public Entity(UUID id, AnimatedShape s, TextureImage t, Vector3f p, boolean renderHidden, String type) {	
		super(GameObject.root(), s, t);
		uuid = id;
		setPosition(p);
		this.getRenderStates().setRenderHiddenFaces(renderHidden);
		this.type = type;
	}

	public Entity(int id, ObjShape s, TextureImage t, Vector3f p, boolean renderHidden, String type) {	
		super(GameObject.root(), s, t);
		int_id = id;
		setPosition(p);
		this.getRenderStates().setRenderHiddenFaces(renderHidden);
		this.type = type;
		this.firingCooldown = DEFAULT_FIRING_COOLDOWN;
	}

	public Entity(UUID id, ObjShape s, TextureImage t, Vector3f p, boolean renderHidden, String type) {	
		super(GameObject.root(), s, t);
		uuid = id;
		setPosition(p);
		this.getRenderStates().setRenderHiddenFaces(renderHidden);
		this.type = type;
	}
	
	public UUID getID() { return uuid; }

	public GameObject getGameObject(){
		return this.go;
	}

	public void setPosition(Vector3f m) { setLocalLocation(m); }
	public Vector3f getPosition() { return getWorldLocation(); }

	public void setRotation(Matrix4f m) { setLocalRotation(m); }
	public Matrix4f getRotation() { return getWorldRotation(); }

	public void setSpeed(float speed) { movementSpeed = speed; }
	public float getSpeed() { return movementSpeed; }

	public void setType(String type) { this.type = type; }
	public String getType() { return this.type; }

	public void setState(boolean state) { this.state = state; }
	public boolean getState() { return this.state; }

	public void setFiringCooldown(int cooldown) { this.firingCooldown = cooldown; }
	public int getFiringCooldown() { return this.firingCooldown; }

	public int getDefaultFiringCooldown() { return this.DEFAULT_FIRING_COOLDOWN; }

	/** Initial cooldown of 0, since has not fired yet */
	public void initFiringCooldown(){
		this.firingCooldown = 0;
	}

	public void resetFiringCooldown(float deltaTime){
		this.firingCooldown = (int) (DEFAULT_FIRING_COOLDOWN * deltaTime);
	}

	/** Decrements firing cooldown by firingCooldownDecrement and returns true if it has gone below 0 (cooldown is up) and resets it */
	public boolean updateFiringCooldown(float deltaTime){
		this.firingCooldown -= (deltaTime * 100) * firingCooldownDecrement;
		if (firingCooldown <= 0){
			resetFiringCooldown(deltaTime);
			return true;
		}
		else {
			return false;
		}
	}
}
