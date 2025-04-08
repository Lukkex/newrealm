package newrealm;

import tage.*;
import tage.shapes.*;

import java.lang.Math;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import org.joml.*;
import tage.input.*;
import tage.input.action.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.nodeControllers.*;
import java.util.Scanner;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	private Scanner scan = new Scanner(System.in);

	private final String DEFAULT_IP_ADDRESS = "192.168.1.19";
	private final int DEFAULT_PORT_NUMBER = 6010;

	private int fluffyClouds; //skyboxes

	private boolean hasWon, gameOver, sphereDisarmed, cubeDisarmed, torusDisarmed, isDisarming, callOnce = false;
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
	private float height = 0.0f;

	private String broadcastMessage = "";

	private int numOfDisarmed, vp2X, vp2Y = 0;

	private Camera cam, minimap;

	private Vector3f hudWhiteColor = new Vector3f(1,1,1);
	private Vector3f hudGreenColor = new Vector3f(0.0f, 1.0f, 0.0f);
	private Vector3f hudYellowColor = new Vector3f(1.0f, 0.729f, 0.0f);
	private Vector3f hudRedColor = new Vector3f(1, 0, 0);
	private Vector3f hudColor = hudWhiteColor;
	private Vector3f loc;

	private GameObject avatar, sphere_sat, 
	cube_sat, torus_sat, lineX, lineY, lineZ, 
	enemy, floor, ground, spaceText, wireR, wireG, wireB,
	chamber;

	private ObjShape avatarS, ghostS, sphereS, cubeS, torusS, lineSx, 
	lineSy, lineSz, enemyS, floorS, groundS, spaceTextS, wireS, chamberS;

	private TextureImage avatartx, ghosttx, spheretx, s_safetx, s_disarmtx, 
	cubetx, c_safetx, c_disarmtx, torustx, t_safetx, t_disarmtx, 
	linetx, enemytx, floortx, groundtx, floorheightmap, groundheightmap, 
	detonatedtx, spaceTexttx, wireRtx, wireGtx, wireBtx, chambertx;

	private Light light1, light2, light3, enemyLight;

	private RotationController rc;
	private BobbingController bc;
	private CameraOrbit3D camOrbitController;
	private CameraMinimap minimapController;

	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	public MyGame(){
		super();
		try {
			gm = new GhostManager(this);

			System.out.println("\nIP Address (Enter for default - " + DEFAULT_IP_ADDRESS + "): ");
			String address = scan.nextLine();

			if (address.equals("")) this.serverAddress = DEFAULT_IP_ADDRESS;
			else this.serverAddress = address;

			System.out.println("\nPort Number (Enter for default - " + DEFAULT_PORT_NUMBER + "): ");
			String port = scan.nextLine();

			if (port.equals("")) this.serverPort = DEFAULT_PORT_NUMBER;
			else this.serverPort = Integer.parseInt(port);

			System.out.println("\nProtocol [UDP/TCP] (Enter for default - UDP): ");
			String protocol = scan.nextLine();

			if (protocol.toUpperCase().compareTo("TCP") == 0)
				this.serverProtocol = ProtocolType.TCP;
			else
				this.serverProtocol = ProtocolType.UDP;
		}
		catch (Exception f){
			System.out.println("\nInvalid parameters for network connection.\n\nIP, Port, Protocol (TCP | UDP)\n");
		}
	}

	public MyGame(String serverAddress, int serverPort, String protocol){ 
		super(); 
		gm = new GhostManager(this);
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		if (protocol.toUpperCase().compareTo("TCP") == 0)
			this.serverProtocol = ProtocolType.TCP;
		else
			this.serverProtocol = ProtocolType.UDP;
	}

	public static void main(String[] args){	
		MyGame game;

		//If parameters passed in through terminal / batch file, skip the prompts
		if (args.length == 3 && args[0] != null && args[1] != null && args[2] != null && args.length < 4)
			game = new MyGame(args[0], Integer.parseInt(args[1]), args[2]);
		else
			game = new MyGame();

		engine = new Engine(game);
		game.initializeSystem();
		game.game_loop();
	}

	@Override
	public void loadShapes()
	{	
		avatarS = new ImportedModel("GHOULhead.obj");
		ghostS = new ImportedModel("GHOULhead.obj");
		sphereS = new Sphere();
		cubeS = new Cube();
		torusS = new Torus();
		enemyS = new ImportedModel("GHOUL.obj");
		floorS = new TerrainPlane(1000);
		groundS = new TerrainPlane(100);

		//Custom space text for after disarms
		spaceTextS = new ImportedModel("spacetext.obj");

		chamberS = new ImportedModel("Chamber.obj");

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
		avatartx = new TextureImage("GHOUL.jpg");
		ghosttx = new TextureImage("GHOUL.jpg");

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
		floorheightmap = new TextureImage("floorheightmap.jpg");

		groundtx = new TextureImage("wireG.png");
		groundheightmap = new TextureImage("sanfrancisco.jpg");

		detonatedtx = new TextureImage("detonated.png");

		linetx = new TextureImage("line.png");

		enemytx = new TextureImage("GHOUL.jpg");

		spaceTexttx = new TextureImage("whiteText.png");

		chambertx = new TextureImage("Chamber.jpg");

		wireRtx = new TextureImage("wireR.png");
		wireGtx = new TextureImage("wireG.png");
		wireBtx = new TextureImage("wireB.png");
	}

	@Override
	public void loadSkyBoxes() {
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

	@Override
	public void buildObjects(){	
		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);
		
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

		ground = new GameObject(GameObject.root(), groundS, groundtx);

		enemy = new GameObject(GameObject.root(), enemyS, enemytx);

		spaceText = new GameObject(GameObject.root(), spaceTextS, spaceTexttx);

		chamber = new GameObject(GameObject.root(), chamberS, chambertx);

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

		avatar.setLocalTranslation((new Matrix4f()).translation(0f,0f,0f));
		//avatar.setLocalScale((new Matrix4f()).scaling(3.0f));

		sphere_sat.setLocalTranslation((new Matrix4f()).translation(15.0f, 0.0f, 15.0f));
		sphere_sat.setLocalScale((new Matrix4f()).scaling(1.0f));

		cube_sat.setLocalTranslation((new Matrix4f()).translation(-15.0f, 0.0f, 15.0f));
		cube_sat.setLocalScale((new Matrix4f()).scaling(1.1f));

		torus_sat.setLocalTranslation((new Matrix4f()).translation(-15.0f, 0.0f, -15.0f));
		torus_sat.setLocalScale((new Matrix4f()).scaling(0.9f));

		enemy.setLocalTranslation((new Matrix4f()).translation(15.0f, 0.0f, -15.0f));
		enemy.setLocalScale((new Matrix4f()).scaling(50f));

		floor.setLocalTranslation((new Matrix4f()).translation(0f, -1f, 0f));
		floor.setLocalScale((new Matrix4f()).scaling(25f));
		floor.setHeightMap(floorheightmap);
		floor.getRenderStates().setTiling(1);
		floor.getRenderStates().setTileFactor(10);

		ground.setLocalTranslation((new Matrix4f()).translation(0f, -200f, 0f));
		ground.setLocalScale((new Matrix4f()).scaling(100f));
		ground.setHeightMap(groundheightmap);
		ground.getRenderStates().setTiling(1);
		ground.getRenderStates().setTileFactor(10);

		spaceText.setLocalTranslation((new Matrix4f()).translation(0f, 2f, 0f));
		spaceText.setLocalScale((new Matrix4f()).scaling(0.2f));

		chamber.setLocalTranslation((new Matrix4f()).translation(0f, 15f, 0f));
		chamber.setLocalScale((new Matrix4f()).scaling(8f));
		chamber.getRenderStates().setRenderHiddenFaces(true);;
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

		//Light that is inside of the enemy enemy object
		enemyLight = new Light();
		enemyLight.setLocation(enemy.getWorldLocation());
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

		setupNetworking();

		MoveAction move = new MoveAction(this, protClient, movementSpeed);
		MoveAction moveBackward = new MoveAction(this, protClient, -movementSpeed);
		TurnAction turn = new TurnAction(this, protClient, yawAmount);
		TurnAction turnRight = new TurnAction(this, protClient, -yawAmount);
		PitchAction pitchUp = new PitchAction(this, protClient, pitchAmount);
		PitchAction pitchDown = new PitchAction(this, protClient, -pitchAmount);
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
		camOrbitController = new CameraOrbit3D(cam, avatar, gamepadName, engine);
		minimapController = new CameraMinimap(minimap, floor, gamepadName, engine);

		rc = new RotationController(engine, new Vector3f(0, 1, 0), 0.002f);
		bc = new BobbingController(engine, 0.0005f);

		(engine.getSceneGraph()).addNodeController(rc);
		(engine.getSceneGraph()).addNodeController(bc);

		rc.enable();
		bc.enable();

		rc.addTarget(chamber);
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
			processNetworking((float) elapsTime);

			camOrbitController.updateCameraPosition();
			minimapController.updateCameraPosition();

			//avatarpin cannot go beneath the Y level of the floor plane
			if (avatar.getWorldLocation().y() < floor.getWorldLocation().y())
				avatar.setLocalLocation((avatar.getWorldLocation().mul(new Vector3f(1f, 0f, 1f))).add(new Vector3f(0f, -1f, 0f)));

			//Set avatarphin to height of height map to make them go over the peaks
			loc = avatar.getWorldLocation();
			height = floor.getHeight(loc.x(), loc.z());
			avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

			//Enemy ManualCube continues to look towards and follow the camera
			enemy.lookAt(avatar.getWorldLocation());
			enemy.move(0.01f);
			enemyLight.setLocation(enemy.getWorldLocation());
			
			//Enemy light is always inside of enemy cube, and will flicker based on the elapsed time
			if (Math.sin(currFrameTime/100) <= 0.5)
				enemyLight.enable();
			else
				enemyLight.disable();
			
			//Handles sphere's textures/states
			if (!sphereDisarmed){
				if (distance(sphere_sat.getWorldLocation(), avatar.getWorldLocation()) <= detonationDistance){ //Sphere detonates
					sphere_sat.setTextureImage(detonatedtx);
					gameOver = true;
					broadcastMessage = " | SATELLITE DETONATED!";
					hudColor = hudRedColor;
				}
				else if (distance(sphere_sat.getWorldLocation(), avatar.getWorldLocation()) <= warningDistance){ //Sphere is in warning distance
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

						wireG.setParent(avatar);
					}
				}	
			}
			else {
				sphere_sat.setTextureImage(s_disarmtx);
			}

			//Handles cube's textures/states
			if (!cubeDisarmed){
				if (distance(cube_sat.getWorldLocation(), avatar.getWorldLocation()) <= detonationDistance){ //Cube detonates
					cube_sat.setTextureImage(detonatedtx);
					gameOver = true;
					broadcastMessage = " | SATELLITE DETONATED!";
					hudColor = hudRedColor;
				}
				else if (distance(cube_sat.getWorldLocation(), avatar.getWorldLocation()) <= warningDistance){ //Cube is in warning distance
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

						wireB.setParent(avatar);
					}
				}
			}
			else {
				cube_sat.setTextureImage(c_disarmtx);
			}

			//Handles torus's textures/states
			if (!torusDisarmed){
				if (distance(torus_sat.getWorldLocation(), avatar.getWorldLocation()) <= detonationDistance){ //Torus detonates
					torus_sat.setTextureImage(detonatedtx);
					gameOver = true;
					broadcastMessage = " | SATELLITE DETONATED!";
					hudColor = hudRedColor;
				}
				else if (distance(torus_sat.getWorldLocation(), avatar.getWorldLocation()) <= warningDistance){ //Torus is in warning distance
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

						wireR.setParent(avatar);
					}
				}
			}
			else {
				torus_sat.setTextureImage(t_disarmtx);
			}
			
			if (distance(enemy.getWorldLocation(), avatar.getWorldLocation()) <= 4.0f){
				broadcastMessage = " | ENEMY IS CLOSING IN!";
				hudColor = hudRedColor;
			}
			else if (distance(sphere_sat.getWorldLocation(), avatar.getWorldLocation()) > warningDistance &&
			distance(cube_sat.getWorldLocation(), avatar.getWorldLocation()) > warningDistance && 
			distance(torus_sat.getWorldLocation(), avatar.getWorldLocation()) > warningDistance){
				broadcastMessage = "";
				hudColor = hudWhiteColor;
			}

			if (numOfDisarmed == 3){
				hasWon = true; //Game is won - all satellites were disarmed
				callOnce = true;
			}
			else if (distance(enemy.getWorldLocation(), avatar.getWorldLocation()) <= 1.0f)
				gameOver = true; //Game is over - the enemy caught you
		
			// Broadcast any new message and update HUD 1
			broadcast(broadcastMessage, hudColor);

			// Update HUD 2 for the smaller viewport
			vp2X = (int) ((engine.getRenderSystem()).getViewport("RIGHT").getRelativeLeft() * (engine.getRenderSystem()).getWidth());
			vp2Y = (int) ((engine.getRenderSystem()).getViewport("RIGHT").getRelativeBottom() * (engine.getRenderSystem()).getHeight());
			(engine.getHUDmanager()).setHUD2("Location: X: " + Float.toString(avatar.getWorldLocation().x()) + ", Y: " + Float.toString(avatar.getWorldLocation().y()), hudWhiteColor, vp2X + 2, vp2Y + 2);
			
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
	public Camera getMainCamera(){
		return (engine.getRenderSystem().getViewport("MAIN").getCamera());
	}

	public GameObject getAvatar(){
		return avatar;
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

	// ---------- NETWORKING SECTION ----------------

	public ObjShape getGhostShape() { return ghostS; }
	public TextureImage getGhostTexture() { return ghosttx; }
	public GhostManager getGhostManager() { return gm; }
	public Engine getEngine() { return engine; }
	
	private void setupNetworking() {	
		isClientConnected = false;	
		try {	
			protClient = new ProtocolClient(InetAddress.getByName(serverAddress), serverPort, serverProtocol, this);
		} 	catch (UnknownHostException e) 
		{	e.printStackTrace();
		}	catch (IOException e) 
		{	e.printStackTrace();
		}
		if (protClient == null)
		{	System.out.println("missing protocol host");
		}
		else
		{	// Send the initial join message with a unique identifier for this client
			System.out.println("sending join message to protocol host");
			protClient.sendJoinMessage();
		}
	}
	
	protected void processNetworking(float elapsTime)
	{	// Process packets received by the client from the server
		if (protClient != null)
			protClient.processPackets();
	}

	public Vector3f getPlayerPosition() { return avatar.getWorldLocation(); }

	public void setIsConnected(boolean value) { this.isClientConnected = value; }
	
	private class SendCloseConnectionPacketAction extends AbstractInputAction
	{	@Override
		public void performAction(float time, net.java.games.input.Event evt) 
		{	if(protClient != null && isClientConnected == true)
			{	protClient.sendByeMessage();
			}
		}
	}
}