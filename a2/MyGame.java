/* Hunter Brown, Prof. Gordon, CSC 165, Feb. 15 2025 */

package a2;

import tage.*;
import tage.shapes.*;

import java.lang.Math;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;
import tage.input.*;
import tage.input.action.*;
import tage.nodeControllers.*;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;

	private boolean hasWon, gameOver, isOnDolphin, sphereDisarmed, cubeDisarmed, torusDisarmed, isDisarming, callOnce = false;
	private boolean worldAxisOn = true;

	private double lastFrameTime, currFrameTime, elapsTime;
	private float yawAmount = 0.01f;
	private float pitchAmount = 0.01f;
	private final float DEFAULT_SPEED = 0.02f;
	private float movementSpeed = DEFAULT_SPEED;
	private float lineLength = 5.0f;
	private float disarmDistance = 3.5f;
	private float warningDistance = 6.0f;
	private float detonationDistance = 4.0f;

	private String broadcastMessage = "";

	private int numOfDisarmed, vp2X, vp2Y = 0;

	private Camera cam, minimap;

	private Vector3f hudWhiteColor = new Vector3f(1,1,1);
	private Vector3f hudGreenColor = new Vector3f(0.0f, 1.0f, 0.0f);
	private Vector3f hudYellowColor = new Vector3f(1.0f, 0.729f, 0.0f);
	private Vector3f hudRedColor = new Vector3f(1, 0, 0);
	private Vector3f hudColor = hudWhiteColor;

	private ManualCube manualCubeS;

	private GameObject dol, sphere_sat, cube_sat, torus_sat, lineX, lineY, lineZ, manualCube, floor, spaceText, wireR, wireG, wireB;
	private ObjShape dolS, sphereS, cubeS, torusS, lineSx, lineSy, lineSz, floorS, spaceTextS, wireS;
	private TextureImage doltx, spheretx, s_safetx, s_disarmtx, cubetx, c_safetx, c_disarmtx, torustx, t_safetx, t_disarmtx, linetx, manualCubetx, floortx, detonatedtx, spaceTexttx, wireRtx, wireGtx, wireBtx;

	private Light light1, light2, light3, enemyLight;

	private RotationController rc;
	private BobbingController bc;
	private CameraOrbit3D camOrbitController;
	private CameraMinimap minimapController;

	public MyGame() { 
		super(); 
	}

	public static void main(String[] args)
	{	MyGame game = new MyGame();
		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		dolS = new ImportedModel("dolphinHighPoly.obj");
		sphereS = new Sphere();
		cubeS = new Cube();
		torusS = new Torus();
		manualCubeS = new ManualCube();
		floorS = new Plane();

		//Custom space text for after disarms
		spaceTextS = new ImportedModel("spacetext.obj");

		//Satellite wire shape
		wireS = new ImportedModel("wire.obj");

		//X, Y, & Z Axes Lines
		lineSy = new Line(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, lineLength, 0.0f));
		lineSx = new Line(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(lineLength, 0.0f, 0.0f));
		lineSz = new Line(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, lineLength));
	}

	@Override
	public void loadTextures()
	{	
		doltx = new TextureImage("Dolphin_HighPolyUV.png");

		spheretx = new TextureImage("Sphere_Satellite.png");
		s_safetx = new TextureImage("Sphere_Close.png");
		s_disarmtx = new TextureImage("Sphere_Disarmed.png");

		cubetx = new TextureImage("Cube_Satellite.png");
		c_safetx = new TextureImage("Cube_Close.png");
		c_disarmtx = new TextureImage("Cube_Disarmed.png");

		torustx = new TextureImage("Torus_Satellite.png");
		t_safetx = new TextureImage("Torus_Close.png");
		t_disarmtx = new TextureImage("Torus_Disarmed.png");

		floortx = new TextureImage("ground.jpg");
		detonatedtx = new TextureImage("detonated.png");

		linetx = new TextureImage("line.png");

		manualCubetx = new TextureImage("ManualCube.png");

		spaceTexttx = new TextureImage("whiteText.png");

		wireRtx = new TextureImage("wireR.png");
		wireGtx = new TextureImage("wireG.png");
		wireBtx = new TextureImage("wireB.png");
	}	

	@Override
	public void buildObjects(){	
		// build dolphin in the center of the window
		dol = new GameObject(GameObject.root(), dolS, doltx);
		sphere_sat = new GameObject(GameObject.root(), sphereS, spheretx);
		cube_sat = new GameObject(GameObject.root(), cubeS, cubetx);
		torus_sat = new GameObject(GameObject.root(), torusS, torustx);

		lineX = new GameObject(GameObject.root(), lineSx, linetx);
		lineY = new GameObject(GameObject.root(), lineSy, linetx);
		lineZ = new GameObject(GameObject.root(), lineSz, linetx);
		lineX.getRenderStates().setColor(new Vector3f(1.0f, 0.0f, 0.0f));
		lineY.getRenderStates().setColor(new Vector3f(0.0f, 1.0f, 0.0f));
		lineZ.getRenderStates().setColor(new Vector3f(0.0f, 0.0f, 1.0f));

		floor = new GameObject(GameObject.root(), floorS, floortx);

		manualCube = new GameObject(GameObject.root(), manualCubeS, manualCubetx);

		spaceText = new GameObject(GameObject.root(), spaceTextS, spaceTexttx);

		//Satellite wires
		wireR = new GameObject(torus_sat, wireS, wireRtx);
		wireG = new GameObject(sphere_sat, wireS, wireGtx);
		wireB = new GameObject(cube_sat, wireS, wireBtx);

		wireR.setLocalTranslation((new Matrix4f()).translation(0f,1f,-0.2f));
		wireG.setLocalTranslation((new Matrix4f()).translation(0f,1f,0f));
		wireB.setLocalTranslation((new Matrix4f()).translation(0f,1f,0.2f));

		wireR.setLocalScale((new Matrix4f()).scaling(0.1f));
		wireG.setLocalScale((new Matrix4f()).scaling(0.1f));
		wireB.setLocalScale((new Matrix4f()).scaling(0.1f));

		wireR.applyParentRotationToPosition(false);
		wireG.applyParentRotationToPosition(false);
		wireB.applyParentRotationToPosition(false);

		wireR.propagateRotation(false);
		wireG.propagateRotation(false);
		wireB.propagateRotation(false);

		dol.setLocalTranslation((new Matrix4f()).translation(0f,0f,0f));
		dol.setLocalScale((new Matrix4f()).scaling(3.0f));

		sphere_sat.setLocalTranslation((new Matrix4f()).translation(15.0f, 0.0f, 15.0f));
		sphere_sat.setLocalScale((new Matrix4f()).scaling(1.0f));

		cube_sat.setLocalTranslation((new Matrix4f()).translation(-15.0f, 0.0f, 15.0f));
		cube_sat.setLocalScale((new Matrix4f()).scaling(1.1f));

		torus_sat.setLocalTranslation((new Matrix4f()).translation(-15.0f, 0.0f, -15.0f));
		torus_sat.setLocalScale((new Matrix4f()).scaling(0.9f));

		manualCube.setLocalTranslation((new Matrix4f()).translation(15.0f, 0.0f, -15.0f));
		manualCube.setLocalScale((new Matrix4f()).scaling(0.25f));

		floor.setLocalTranslation((new Matrix4f()).translation(0f, -1f, 0f));
		floor.setLocalScale((new Matrix4f()).scaling(25f));

		spaceText.setLocalTranslation((new Matrix4f()).translation(0f, 2f, 0f));
		spaceText.setLocalScale((new Matrix4f()).scaling(0.2f));
	}

	@Override
	public void initializeLights(){	
		Light.setGlobalAmbient(0.5f, 0.5f, 0.5f);

		//Light above Sphere
		light1 = new Light();
		light1.setLocation(new Vector3f(15.0f, 4.0f, 15.0f));
		light1.setDiffuse(0.0f, 1.0f, 0.0f);
		light1.setSpecular(0.0f, 1.0f, 0.0f);

		//Light above Cube
		light2 = new Light();
		light2.setLocation(new Vector3f(-14.0f, 4.0f, 14.0f));
		light2.setDiffuse(0.0f, 0.0f, 1.0f);
		light2.setSpecular(0.0f, 0.0f, 1.0f);

		//Light above Torus
		light3 = new Light();
		light3.setLocation(new Vector3f(-15.0f, 4.0f, -15.0f));
		light3.setDiffuse(1.0f, 0.729f, 0.0f);
		light3.setSpecular(1.0f, 0.729f, 0.0f);

		//Light that is inside of the enemy manualCube object
		enemyLight = new Light();
		enemyLight.setLocation(manualCube.getWorldLocation());
		enemyLight.setDiffuse(1.0f, 0.0f, 0.0f);
		enemyLight.setSpecular(1.0f, 0.0f, 0.0f);

		(engine.getSceneGraph()).addLight(light1);
		(engine.getSceneGraph()).addLight(light2);
		(engine.getSceneGraph()).addLight(light3);
		(engine.getSceneGraph()).addLight(enemyLight);
	}

	@Override
	public void initializeGame(){	
		im = engine.getInputManager();

		MoveAction move = new MoveAction(this, movementSpeed);
		MoveAction moveBackward = new MoveAction(this, -movementSpeed);
		TurnAction turn = new TurnAction(this, yawAmount);
		TurnAction turnRight = new TurnAction(this, -yawAmount);
		PitchAction pitchUp = new PitchAction(this, pitchAmount);
		PitchAction pitchDown = new PitchAction(this, -pitchAmount);
		DisarmAction disarm = new DisarmAction(this);
		DisableAxisAction disableAxis = new DisableAxisAction(this);

		//Controller actions
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, move, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, turn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		//Keyboard actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, move, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, moveBackward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, turn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, turnRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, pitchUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, pitchDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.E, disarm, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Q, disableAxis, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		spaceText.getRenderStates().disableRendering();

		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ------------- Setting up main camera and minimap camera -------------
		
		cam = (engine.getRenderSystem().getViewport("LEFT").getCamera());
		minimap = (engine.getRenderSystem().getViewport("RIGHT").getCamera());

		String gamepadName = im.getFirstGamepadName();
		camOrbitController = new CameraOrbit3D(cam, dol, gamepadName, engine);
		minimapController = new CameraMinimap(minimap, floor, gamepadName, engine);

		rc = new RotationController(engine, new Vector3f(0, 1, 0), 0.002f);
		bc = new BobbingController(engine, 0.0005f);

		(engine.getSceneGraph()).addNodeController(rc);
		(engine.getSceneGraph()).addNodeController(bc);

		rc.enable();
		bc.enable();
	}

	@Override
	public void update(){
		if (!gameOver && !hasWon){	
			//Update elapsed time based on current frame and last frame's time
			lastFrameTime = currFrameTime;
			currFrameTime = System.currentTimeMillis();
			elapsTime = (currFrameTime - lastFrameTime) / 10.0;

			//Movement speed is based on elapsed time; InputManager gets updated and Action classes use this speed variable
			movementSpeed = DEFAULT_SPEED * (float) elapsTime;

			isDisarming = false;
			im.update((float) elapsTime);

			camOrbitController.updateCameraPosition();
			minimapController.updateCameraPosition();

			//Dolpin cannot go beneath the Y level of the floor plane
			if (dol.getWorldLocation().y() < floor.getWorldLocation().y())
				dol.setLocalLocation((dol.getWorldLocation().mul(new Vector3f(1f, 0f, 1f))).add(new Vector3f(0f, -1f, 0f)));

			//Enemy ManualCube continues to look towards and follow the camera
			manualCube.lookAt(dol.getWorldLocation());
			manualCube.move(0.01f);
			enemyLight.setLocation(manualCube.getWorldLocation());
			
			//Enemy light is always inside of enemy cube, and will flicker based on the elapsed time
			if (Math.sin(currFrameTime/100) <= 0.5)
				enemyLight.enable();
			else
				enemyLight.disable();
			
			//Handles sphere's textures/states
			if (!sphereDisarmed){
				if (distance(sphere_sat.getWorldLocation(), dol.getWorldLocation()) <= detonationDistance){ //Sphere detonates
					sphere_sat.setTextureImage(detonatedtx);
					gameOver = true;
					broadcastMessage = " | SATELLITE DETONATED!";
					hudColor = hudRedColor;
				}
				else if (distance(sphere_sat.getWorldLocation(), dol.getWorldLocation()) <= warningDistance){ //Sphere is in warning distance
					sphere_sat.setTextureImage(s_safetx);
					broadcastMessage = " | WARNING DISTANCE!";
					hudColor = hudYellowColor;

					if (!gameOver && isDisarming){ //Sphere is disarmed
						sphere_sat.setTextureImage(s_disarmtx);
						sphereDisarmed = true;
						numOfDisarmed++;
						rc.addTarget(sphere_sat);
						bc.addTarget(sphere_sat);
						broadcastMessage = " | SATELLITE DISARMED!";
						hudColor = hudGreenColor;

						spaceText.propagateRotation(false);
						spaceText.applyParentRotationToPosition(false);
						spaceText.setParent(sphere_sat);
						spaceText.getRenderStates().enableRendering();

						wireG.setParent(dol);
					}
				}	
			}
			else {
				sphere_sat.setTextureImage(s_disarmtx);
			}

			//Handles cube's textures/states
			if (!cubeDisarmed){
				if (distance(cube_sat.getWorldLocation(), dol.getWorldLocation()) <= detonationDistance){ //Cube detonates
					cube_sat.setTextureImage(detonatedtx);
					gameOver = true;
					broadcastMessage = " | SATELLITE DETONATED!";
					hudColor = hudRedColor;
				}
				else if (distance(cube_sat.getWorldLocation(), dol.getWorldLocation()) <= warningDistance){ //Cube is in warning distance
					cube_sat.setTextureImage(c_safetx);
					broadcastMessage = " | WARNING DISTANCE!";
					hudColor = hudYellowColor;

					if (!gameOver && isDisarming){ //Cube is disarmed
						cube_sat.setTextureImage(c_disarmtx);
						cubeDisarmed = true;
						numOfDisarmed++;
						rc.addTarget(cube_sat);
						bc.addTarget(cube_sat);
						broadcastMessage = " | SATELLITE DISARMED!";
						hudColor = hudGreenColor;
						
						spaceText.propagateRotation(false);
						spaceText.applyParentRotationToPosition(false);
						spaceText.setParent(cube_sat);
						spaceText.getRenderStates().enableRendering();

						wireB.setParent(dol);
					}
				}
			}
			else {
				cube_sat.setTextureImage(c_disarmtx);
			}

			//Handles torus's textures/states
			if (!torusDisarmed){
				if (distance(torus_sat.getWorldLocation(), dol.getWorldLocation()) <= detonationDistance){ //Torus detonates
					torus_sat.setTextureImage(detonatedtx);
					gameOver = true;
					broadcastMessage = " | SATELLITE DETONATED!";
					hudColor = hudRedColor;
				}
				else if (distance(torus_sat.getWorldLocation(), dol.getWorldLocation()) <= warningDistance){ //Torus is in warning distance
					torus_sat.setTextureImage(t_safetx);
					broadcastMessage = " | WARNING DISTANCE!";
					hudColor = hudYellowColor;

					if (!gameOver && isDisarming){ //Torus is disarmed
						torus_sat.setTextureImage(t_disarmtx);
						torusDisarmed = true;
						numOfDisarmed++;
						rc.addTarget(torus_sat);
						bc.addTarget(torus_sat);
						broadcastMessage = " | SATELLITE DISARMED!";
						hudColor = hudGreenColor;

						spaceText.propagateRotation(false);
						spaceText.applyParentRotationToPosition(false);
						spaceText.setParent(torus_sat);
						spaceText.getRenderStates().enableRendering();

						wireR.setParent(dol);
					}
				}
			}
			else {
				torus_sat.setTextureImage(t_disarmtx);
			}
			
			if (distance(manualCube.getWorldLocation(), dol.getWorldLocation()) <= 4.0f){
				broadcastMessage = " | ENEMY IS CLOSING IN!";
				hudColor = hudRedColor;
			}
			else if (distance(sphere_sat.getWorldLocation(), dol.getWorldLocation()) > warningDistance &&
			distance(cube_sat.getWorldLocation(), dol.getWorldLocation()) > warningDistance && 
			distance(torus_sat.getWorldLocation(), dol.getWorldLocation()) > warningDistance){
				broadcastMessage = "";
				hudColor = hudWhiteColor;
			}

			if (numOfDisarmed == 3){
				hasWon = true; //Game is won - all satellites were disarmed
				callOnce = true;
			}
			else if (distance(manualCube.getWorldLocation(), dol.getWorldLocation()) <= 1.0f)
				gameOver = true; //Game is over - the enemy caught you
		
			// Broadcast any new message and update HUD 1
			broadcast(broadcastMessage, hudColor);

			// Update HUD 2 for the smaller viewport
			vp2X = (int) ((engine.getRenderSystem()).getViewport("RIGHT").getRelativeLeft() * (engine.getRenderSystem()).getWidth());
			vp2Y = (int) ((engine.getRenderSystem()).getViewport("RIGHT").getRelativeBottom() * (engine.getRenderSystem()).getHeight());
			(engine.getHUDmanager()).setHUD2("Location: X: " + Float.toString(dol.getWorldLocation().x()) + ", Y: " + Float.toString(dol.getWorldLocation().y()), hudWhiteColor, vp2X + 2, vp2Y + 2);
			
			spaceText.lookAt(cam.getLocation()); //Text will always face towards the camera

			//Disable space text if it gets too high up in the air
			if (spaceText.getWorldLocation().y() > 40f)
				spaceText.getRenderStates().disableRendering();
		}

		//Calls once more so that all child nodes properly snap to new parents (e.g. wires)
		if (callOnce){
			im.update((float) elapsTime);
			callOnce = false;
		}
		spaceText.lookAt(cam.getLocation());
		
		//Player has lost the game
		if (gameOver){
			broadcast(" | GAME OVER.", new Vector3f(1,0,0));
		}

		//Player has won the game!
		else if (hasWon){
			broadcast(" | VICTORY!", new Vector3f(0,1,0));
		}
	}

	@Override
	public void createViewports(){
		(engine.getRenderSystem()).addViewport("LEFT",0,0,1f,1f);
		(engine.getRenderSystem()).addViewport("RIGHT",.75f,0,.25f,.25f);
		Viewport leftVp = (engine.getRenderSystem()).getViewport("LEFT");
		Viewport rightVp = (engine.getRenderSystem()).getViewport("RIGHT");
		Camera leftCamera = leftVp.getCamera();
		Camera rightCamera = rightVp.getCamera();
		rightVp.setHasBorder(true);
		rightVp.setBorderWidth(1);
		rightVp.setBorderColor(1.0f, 1.0f, 1.0f);
		leftCamera.setLocation(new Vector3f(-2,0,2));
		leftCamera.setU(new Vector3f(1,0,0));
		leftCamera.setV(new Vector3f(0,1,0));
		leftCamera.setN(new Vector3f(0,0,-1));
		rightCamera.setLocation(new Vector3f(0,2,0));
		rightCamera.setU(new Vector3f(1,0,0));
		rightCamera.setV(new Vector3f(0,0,-1));
		rightCamera.setN(new Vector3f(0,-1,0));
	}

	//Broadcasting function that can announce a broadcast message on the HUD
	public void broadcast(String message){
		(engine.getHUDmanager()).setHUD1("Score: " + Integer.toString(numOfDisarmed) + message, hudWhiteColor, 15, 15);
	}

	//Overloaded in case you want something other than the default white color for the HUD
	public void broadcast(String message, Vector3f HUDColor){
		(engine.getHUDmanager()).setHUD1("Score: " + Integer.toString(numOfDisarmed) + message, HUDColor, 15, 15);
	}

	//Helper function for determining distance between 2 Vector3f Objects
	public float distance(Vector3f v1, Vector3f v2){
		//sqrt((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)
		return (float) Math.sqrt(Math.pow((double)(v2.x() - v1.x()), 2) + Math.pow((double)(v2.y() - v1.y()), 2) + Math.pow((double)(v2.z() - v1.z()), 2));
	}

	//Get & set functions needed in MoveAction, DisarmAction, and TurnAction
	public boolean isOnDolphin(){
		return isOnDolphin;
	}

	public Camera getMainCamera(){
		return (engine.getRenderSystem().getViewport("MAIN").getCamera());
	}

	public GameObject getAvatar(){
		return dol;
	}

	//Sets the disarming state to true or false
	//boolean is used to determine if player will disarm satellite when in warning proximity
	public void setDisarming(boolean disarmState){
		isDisarming = disarmState;
	}

	//Used by DisableAxisAction.java to toggle the central world axis line objects
	public void toggleXYZAxis(){
		if (worldAxisOn){
			lineX.getRenderStates().disableRendering();
			lineY.getRenderStates().disableRendering();
			lineZ.getRenderStates().disableRendering();
			worldAxisOn = false;
		}
		else {
			lineX.getRenderStates().enableRendering();
			lineY.getRenderStates().enableRendering();
			lineZ.getRenderStates().enableRendering();
			worldAxisOn = true;
		}
	}
}