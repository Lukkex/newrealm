package newrealm;

import tage.*;
import tage.audio.AudioResource;
import tage.audio.AudioResourceType;
import tage.audio.IAudioManager;
import tage.audio.Sound;
import tage.audio.SoundType;
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
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;

import java.util.Scanner;

public class MyGame extends VariableFrameRateGame
{
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;

	//Sound
	private IAudioManager audioMgr;
	private Sound AttackSound, BGSound;

	//Physics
	private PhysicsEngine physicsEngine;
	private PhysicsObject caps1P, caps2P, planeP;

	private Robot robot;
	private float curMouseX, curMouseY, centerX, centerY;
	private float prevMouseX, prevMouseY;
	private float cameraMoveSpeed = 0.01f;
	private boolean isRecentering;

	private Scanner scan = new Scanner(System.in);

	private final String DEFAULT_IP_ADDRESS = "192.168.1.19";
	private final int DEFAULT_PORT_NUMBER = 6010;

	private int fluffyClouds; //skyboxes

	private boolean hasWon, gameOver, callOnce = false;
	private boolean worldAxisOn = true;

	private double lastFrameTime, currFrameTime, elapsTime;
	private float yawAmount = 0.01f;
	private float pitchAmount = 0.01f;
	private final float DEFAULT_SPEED = 0.02f;
	private float movementSpeed = DEFAULT_SPEED;
	private float lineLength = 5.0f;
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
	private Matrix4f cameraRotation;

	private GameObject avatar, lineX, lineY, lineZ, 
	ghoul, floor, ground, chamber;

	private ObjShape ghostS, lineSx, 
	lineSy, lineSz, floorS, groundS, chamberS;

	private AnimatedShape avatarS, ghoulS;

	private TextureImage avatartx, ghosttx, linetx, ghoultx, 
	floortx, groundtx, floorheightmap, groundheightmap, chambertx;

	private Light light1, light2, light3, enemyLight;

	private RotationController rc;
	private BobbingController bc;

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
		avatarS = new AnimatedShape("Arms.rkm", "Arms.rks");
		avatarS.loadAnimation("IDLE", "ArmsIdle.rka");
		ghostS = new ImportedModel("GHOULhead.obj");
		ghoulS = new AnimatedShape("Ghoul.rkm", "Ghoul.rks");
		ghoulS.loadAnimation("IDLE", "GhoulIdle.rka");
		floorS = new TerrainPlane(1000);
		groundS = new TerrainPlane(100);

		chamberS = new ImportedModel("Chamber.obj");

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

		floortx = new TextureImage("ground.jpg");
		floorheightmap = new TextureImage("floorheightmap.jpg");

		groundtx = new TextureImage("wireG.png");
		groundheightmap = new TextureImage("sanfrancisco.jpg");

		linetx = new TextureImage("line.png");

		ghoultx = new TextureImage("GHOUL.jpg");

		chambertx = new TextureImage("Chamber.jpg");
	}

	@Override
	public void loadSkyBoxes() {
		fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(fluffyClouds);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

		@Override
	public void loadSounds()
	{ AudioResource resource1, resource2;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("AttackSound.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("KnowMyName.wav", AudioResourceType.AUDIO_SAMPLE);
		AttackSound = new Sound(resource1, SoundType.SOUND_EFFECT, 10, false);
		BGSound = new Sound(resource2, SoundType.SOUND_EFFECT, 10, true);
		AttackSound.initialize(audioMgr);
		BGSound.initialize(audioMgr);
		AttackSound.setMaxDistance(10.0f);
		AttackSound.setMinDistance(0.5f);
		AttackSound.setRollOff(5.0f);
		BGSound.setMaxDistance(10.0f);
		BGSound.setMinDistance(0.5f);
		BGSound.setRollOff(5.0f);
	}

	@Override
	public void buildObjects(){	
		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);

		avatar.getRenderStates().setRenderHiddenFaces(false);
		
		lineX = new GameObject(GameObject.root(), lineSx, linetx);
		lineY = new GameObject(GameObject.root(), lineSy, linetx);
		lineZ = new GameObject(GameObject.root(), lineSz, linetx);
		lineX.getRenderStates().setColor(new Vector3f(1.0f, 0.0f, 0.0f));
		lineY.getRenderStates().setColor(new Vector3f(0.0f, 1.0f, 0.0f));
		lineZ.getRenderStates().setColor(new Vector3f(0.0f, 0.0f, 1.0f));

		floor = new GameObject(GameObject.root(), floorS, floortx);

		ground = new GameObject(GameObject.root(), groundS, groundtx);

		ghoul = new GameObject(GameObject.root(), ghoulS, ghoultx);
		chamber = new GameObject(GameObject.root(), chamberS, chambertx);

		avatar.setLocalTranslation((new Matrix4f()).translation(0f,0f,0f));
		avatar.setLocalScale((new Matrix4f()).scaling(0.3f));

		ghoul.setLocalTranslation((new Matrix4f()).translation(15.0f, 0.0f, -15.0f));
		ghoul.setLocalScale((new Matrix4f()).scaling(1f));

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
		enemyLight.setLocation(ghoul.getWorldLocation());
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
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Q, disableAxis, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ------------- Setting up main camera -------------
		
		cam = (engine.getRenderSystem().getViewport("LEFT").getCamera());

		String gamepadName = im.getFirstGamepadName();

		rc = new RotationController(engine, new Vector3f(0, 1, 0), 0.002f);
		bc = new BobbingController(engine, 0.0005f);

		(engine.getSceneGraph()).addNodeController(rc);
		(engine.getSceneGraph()).addNodeController(bc);

		rc.enable();
		bc.enable();

		rc.addTarget(chamber);

		initMouseMode();

		// Animation
		avatarS.playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);
		ghoulS.playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);
	
		// Camera
		

		// Sound
		AttackSound.setLocation(avatar.getWorldLocation());
		BGSound.setLocation(ground.getWorldLocation());
		setEarParameters();
		BGSound.play();
	}

	public void setEarParameters(){
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(cam.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
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

			//Animations
			avatarS.updateAnimation();
			ghoulS.updateAnimation();

			//Input Manager
			im.update((float) elapsTime);

			//Networking
			processNetworking((float) elapsTime);

			//Sound operations
			AttackSound.setLocation(avatar.getWorldLocation());
			setEarParameters();

			//Camera operations
			cam.setLocation(avatar.getWorldLocation());
			cameraRotation = cam.getLocalRotation();

			//avatarpin cannot go beneath the Y level of the floor plane
			if (avatar.getWorldLocation().y() < floor.getWorldLocation().y())
				avatar.setLocalLocation((avatar.getWorldLocation().mul(new Vector3f(1f, 0f, 1f))).add(new Vector3f(0f, -1f, 0f)));

			//Set avatarphin to height of height map to make them go over the peaks
			loc = avatar.getWorldLocation();
			height = floor.getHeight(loc.x(), loc.z());
			avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

			//Enemy ManualCube continues to look towards and follow the camera
			ghoul.lookAt(avatar.getWorldLocation());
			ghoul.move(0.01f);
			enemyLight.setLocation(ghoul.getWorldLocation());
			
			//Enemy light is always inside of enemy cube, and will flicker based on the elapsed time
			if (Math.sin(currFrameTime/100) <= 0.5)
				enemyLight.enable();
			else
				enemyLight.disable();
			
			// Broadcast any new message and update HUD 1
			broadcast(broadcastMessage, hudColor);
		}

		//Calls once more so that all child nodes properly snap to new parents (e.g. wires)
		if (callOnce){
			im.update((float) elapsTime);
			callOnce = false;
		}
		
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
		Viewport leftVp = (engine.getRenderSystem()).getViewport("LEFT");
		Camera leftCamera = leftVp.getCamera();
		leftCamera.setLocation(new Vector3f(-2,0,2));
		leftCamera.setU(new Vector3f(1,0,0));
		leftCamera.setV(new Vector3f(0,1,0));
		leftCamera.setN(new Vector3f(0,0,-1));
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
		return (engine.getRenderSystem().getViewport("LEFT").getCamera());
	}

	public GameObject getAvatar(){
		return avatar;
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

	public void initMouseMode()
	{
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("LEFT");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		centerX = (int) (left + width/2);
		centerY = (int) (bottom - height/2);
		isRecentering = false;
		try // note that some platforms may not support the Robot class
		{ 
			robot = new Robot(); 
		} 
		catch (AWTException ex)
		{ 
			throw new RuntimeException("Couldn't create Robot!"); 
		}
		recenterMouse();
		prevMouseX = centerX; // 'prevMouse' defines the initial
		prevMouseY = centerY; // mouse position
		// also change the cursor
		Image faceImage = new ImageIcon("./assets/textures/ManualCube.png").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().
		createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
		rs.getGLCanvas().setCursor(faceCursor);
	}

	@Override
	public void mousePressed(MouseEvent e){
		AttackSound.play();
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{ 	// if robot is recentering and the MouseEvent location is in the center,
		// then this event was generated by the robot
		if (isRecentering &&
		centerX == e.getXOnScreen() && centerY == e.getYOnScreen())
		{ // mouse recentered, recentering complete
			isRecentering = false;
		}
		else
		{ // event was due to a user mouse-move, and must be processed
			curMouseX = e.getXOnScreen();
			curMouseY = e.getYOnScreen();
			float mouseDeltaX = prevMouseX - curMouseX;
			float mouseDeltaY = prevMouseY - curMouseY;
			cam.yaw(mouseDeltaX  * cameraMoveSpeed / (float)(elapsTime));
			cam.pitch(mouseDeltaY  * cameraMoveSpeed / (float)(elapsTime));
			prevMouseX = curMouseX;
			prevMouseY = curMouseY;
			// tell robot to put the cursor to the center (since user just moved it)
			recenterMouse();
			prevMouseX = centerX; // reset prev to center
			prevMouseY = centerY;
		}
	}

	private void recenterMouse()
	{ 	// use the robot to move the mouse to the center point.
		// Note that this generates one MouseEvent.
		RenderSystem rs = engine.getRenderSystem();
		Viewport vw = rs.getViewport("LEFT");
		float left = vw.getActualLeft();
		float bottom = vw.getActualBottom();
		float width = vw.getActualWidth();
		float height = vw.getActualHeight();
		centerX = (int) (left + width/2.0f);
		centerY = (int) (bottom - height/2.0f);
		isRecentering = true;
		robot.mouseMove((int)centerX, (int)centerY);
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