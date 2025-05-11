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
import tage.networking.*;
import tage.networking.IGameConnection.ProtocolType;
import tage.nodeControllers.*;
import tage.physics.PhysicsEngine;
import tage.physics.PhysicsObject;
import tage.physics.JBullet.JBulletPhysicsEngine;
import tage.physics.JBullet.JBulletPhysicsObject;

import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;
import java.util.Vector;

public class MyGame extends VariableFrameRateGame
{
	//Engines and Managers
	private static Engine engine;
	private InputManager im;
	private GhostManager gm;
	private MapManager mm;
	private EntityManager em;

	//Temp Values
	double[] temp;

	//Avatar
	private GameObject avatar;
	private ObjShape avatarS;
	private TextureImage avatartx;
	private float avatarHeight = 0.3f;
	private PhysicsObject avatarHitbox;
	private int PlayerHP = 100;
	private int killCount = 0;
	private double totalElapsedTime = 0.0f;
	private double gameStartTime = 0.0f;
	private String sVal = (String) String.format("%.2f", totalElapsedTime);
	private boolean isPlayerInvincible = false;
	private float iFrameDuration = 5; //5 Ticks
	private float cooldownCounter = iFrameDuration;
	private Vector3f previousAvatarLocation = new Vector3f(); //Used if collides with a wall to revert to non-colliding position

	//Sound
	private IAudioManager audioMgr;
	private Sound AttackSound, BGSound, GhoulAttackSound;

	//Pseudo Physics
	private Vector<GameObject> pseudoPhysicsObjects = new Vector<GameObject>();

	//Physics
	private PhysicsEngine physicsEngine;
	private PhysicsObject caps1P, caps2P, planeP;
	private float vals[] = new float[16];
	private boolean isPhysicsWorldRendered = false;

	//Camera Handling
	private Camera cam, minimap;
	CameraMinimap minimapController;

	//Mouse Handling
	private Robot robot;
	private float curMouseX, curMouseY, centerX, centerY;
	private float prevMouseX, prevMouseY;
	private float cameraMoveSpeed = 0.01f;
	private boolean recenteringAllowed = true;

	//Skyboxes
	private int fluffyClouds, eyeRedSky; 

	private boolean hasWon, gameOver, callOnce = false;
	private boolean worldAxisOn = true;

	//Movement
	private double lastFrameTime, currFrameTime, elapsTime;
	private float yawAmount = 0.01f;
	private float pitchAmount = 0.01f;
	private final float DEFAULT_SPEED = 0.02f;
	private float movementSpeed = DEFAULT_SPEED;
	private float jumpAmount = 0.2f;
	private boolean isJumping = false;
	private float bulletForce = DEFAULT_SPEED * 20.0f;
	private float sprintSpeed = DEFAULT_SPEED * 2.0f;
	private float lineLength = 5.0f;
	private float height = 0.0f;

	//HUD
	private String broadcastMessage = "";

	private int numOfDisarmed, vp2X, vp2Y = 0;

	private Vector3f hudColor = Constants.hudWhiteColor;
	private Vector3f loc;

	//Node Controllers
	private RotationController rc, rc_reverse;
	private BobbingController bc;
	private ShootingController sc;

	private Vector<GameObject> rc_ObjectsToAddLater = new Vector<GameObject>();
	private Vector<GameObject> rc_reverse_ObjectsToAddLater = new Vector<GameObject>();

	//Networking
	private String serverAddress;
	private int serverPort;
	private ProtocolType serverProtocol;
	private ProtocolClient protClient;
	private boolean isClientConnected = false;

	private Scanner scan = new Scanner(System.in);

	private final String DEFAULT_IP_ADDRESS = "192.168.1.19";
	private final int DEFAULT_PORT_NUMBER = 6010;

	// -/-/------------ GAME OBJECTS & ATTRIBUTES ------------/-/- //
	private GameObject lineX, lineY, lineZ, 
	floor, ground, chamber, cone, eye, stars1, stars2, sanctum, starLarge, upperChamber;

	private ObjShape ghostS, lineSx, 
	lineSy, lineSz, floorS, groundS, chamberS, coneS, eyeS, stars1S, stars2S, sanctumS, starLargeS, upperChamberS;

	private TextureImage ghosttx, linetx, 
	floortx, groundtx, floorheightmap, groundheightmap, chambertx,
	conetx, eyetx, stars1tx, stars2tx, sanctumtx, starLargetx, upperChambertx;

	// ---------- LIGHT ---------------- //
	private Light light1, light2, light3, light4;

	
	// ---------- ENTITIES ---------------- //
	//NextLevelPad
	private ObjShape padS, padRing0S, padRing1S, padRing2S, padRing3S, padRing4S, padArrowS;
	private TextureImage padtx, padRingtx, padArrowtx;
	private float padScaling = 0.4f;

	//Ghoul
	private AnimatedShape ghoulS;
	private TextureImage ghoultx;
	private float ghoulScaling = 0.1f;

	
	// ---------- MAP COMPONENTS & EXTRAS ---------------- //
	//Eye of Ego
	private GameObject eyeOfEgo;
	private ObjShape eyeOfEgoS;
	private TextureImage eyeOfEgotx;
	private float eyeOfEgoScaling = 20f;

	// ---------- AMMO TYPE ---------------- //
	private ObjShape egoOrbS;
	private TextureImage egoOrbtx;

	// ---------- MAP #1 ---------------- //
	private ObjShape wallS, windowS, doorS, doorhorizS, chestS;
	private TextureImage walltx, windowtx, doortx, chesttx;
	private Vector<PhysicsObject> mapHitboxes = new Vector<PhysicsObject>();
	private Vector<PhysicsObject> entityHitboxes = new Vector<PhysicsObject>();

	private float mapUnitSize = 4.0f;
	private float wallWidth = mapUnitSize/2.0f;
	private float wallHeight = 2.5f;
	private float floorSize = 0.85555f;
	private float floorYLevel = -1f;

	//General-Use Temps
	private double[] tempTrans;
	private Matrix4f tempMat4;

	public MyGame(){
		super();
		gm = new GhostManager(this);
		em = new EntityManager(this, protClient);
		mm = new MapManager();

		floorSize = mm.getMapHeight(1) * mapUnitSize * floorSize;

		try {
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
		em = new EntityManager(this, protClient);
		mm = new MapManager();
		
		floorSize = mm.getMapHeight(1) * mapUnitSize * floorSize;

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
		avatarS = new ImportedModel("Arms.obj");
		//avatarS = new AnimatedShape("Arms.rkm", "Arms.rks");
		//avatarS.loadAnimation("IDLE", "ArmsIdle.rka");
		ghostS = new ImportedModel("GHOULhead.obj");
		ghoulS = new AnimatedShape("Ghoul.rkm", "Ghoul.rks");
		ghoulS.loadAnimation("IDLE", "GhoulIdle.rka");
		floorS = new TerrainPlane(1000);
		groundS = new TerrainPlane(100);

		//Map Parts
		wallS = new ImportedModel("wall.obj");
		windowS = new ImportedModel("window.obj");
		doorS = new ImportedModel("door.obj");
		doorhorizS = new ImportedModel("doorhoriz.obj");
		chestS = new ImportedModel("chest.obj");

		chamberS = new ImportedModel("Chamber.obj");

		coneS = new ImportedModel("Cone.obj");
		eyeS = new ImportedModel("crazyeye.obj");
		stars1S = new ImportedModel("outerStars1.obj");
		stars2S = new ImportedModel("outerStars2.obj");
		sanctumS = new ImportedModel("Sanctum.obj");
		starLargeS = new ImportedModel("Star.obj");
		upperChamberS = new ImportedModel("UpperChamber.obj");

		//X, Y, & Z Axes Lines
		lineSy = new Line(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, lineLength, 0.0f));
		lineSx = new Line(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(lineLength, 0.0f, 0.0f));
		lineSz = new Line(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, lineLength));
		
		// ---------- AMMO TYPES ----------------
		egoOrbS = new ImportedModel("egoSpike.obj");

		// ---------- MAP COMPONENTS & EXTRAS ----------------
		//Level Pad
		padS = new ImportedModel("NextLevelPad/NextLevel_Pad.obj");
		padRing0S = new ImportedModel("NextLevelPad/NextLevel_0.obj");
		padRing1S = new ImportedModel("NextLevelPad/NextLevel_1.obj");
		padRing2S = new ImportedModel("NextLevelPad/NextLevel_2.obj");
		padRing3S = new ImportedModel("NextLevelPad/NextLevel_3.obj");
		padRing4S = new ImportedModel("NextLevelPad/NextLevel_4.obj");
		padArrowS = new ImportedModel("NextLevelPad/Arrow.obj");

		eyeOfEgoS = new ImportedModel("eyeOfEgo.obj");
	}

	@Override
	public void loadTextures()
	{	
		avatartx = new TextureImage("GHOUL.jpg");
		ghosttx = new TextureImage("GHOUL.jpg");

		floortx = new TextureImage("floor.png");
		floorheightmap = new TextureImage("floorheightmap.jpg");

		groundtx = new TextureImage("wireG.png");
		groundheightmap = new TextureImage("sanfrancisco.jpg");

		linetx = new TextureImage("line.png");

		ghoultx = new TextureImage("GHOUL.jpg");

		chambertx = new TextureImage("Chamber.jpg");

		conetx = new TextureImage("brick1.jpg");

		eyetx = new TextureImage("crazyeye.png");

		stars1tx = new TextureImage("Star.jpg");

		stars2tx = new TextureImage("Star.jpg");

		starLargetx = new TextureImage("Star.jpg");

		sanctumtx = new TextureImage("Sanctum.jpg");

		upperChambertx = new TextureImage("Star.jpg");

		//Map Parts
		walltx = new TextureImage("wall.png");
		windowtx = new TextureImage("window.png");
		doortx = new TextureImage("door.png");
		chesttx = new TextureImage("Star.jpg");

		//Level Pad
		padtx = new TextureImage("pad.png");
		padArrowtx = new TextureImage("padarrow.png");
		padRingtx = new TextureImage("padring.png");

		// ---------- AMMO TYPES ----------------
		egoOrbtx = new TextureImage("ego.png");

		// ---------- MAP COMPONENTS & EXTRAS ----------------
		eyeOfEgotx = new TextureImage("eyeOfEgo_withBG.png");
	}

	@Override
	public void loadSkyBoxes() {
		//fluffyClouds = (engine.getSceneGraph()).loadCubeMap("fluffyClouds");
		eyeRedSky = (engine.getSceneGraph()).loadCubeMap("eyeRedSky");
		(engine.getSceneGraph()).setActiveSkyBoxTexture(eyeRedSky);
		(engine.getSceneGraph()).setSkyBoxEnabled(true);
	}

		@Override
	public void loadSounds()
	{ AudioResource resource1, resource2, resource3;
		audioMgr = engine.getAudioManager();
		resource1 = audioMgr.createAudioResource("AttackSound.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = audioMgr.createAudioResource("FatalistLoop.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = audioMgr.createAudioResource("GhoulAttackSound.wav", AudioResourceType.AUDIO_SAMPLE);
		AttackSound = new Sound(resource1, SoundType.SOUND_EFFECT, 100, false);
		BGSound = new Sound(resource2, SoundType.SOUND_EFFECT, 2, true);
		GhoulAttackSound = new Sound(resource3, SoundType.SOUND_EFFECT, 100, false);
		
		AttackSound.initialize(audioMgr);

		GhoulAttackSound.initialize(audioMgr);

		BGSound.initialize(audioMgr);

		AttackSound.setMaxDistance(10.0f);
		AttackSound.setMinDistance(0.5f);
		AttackSound.setRollOff(5.0f);

		GhoulAttackSound.setMaxDistance(10.0f);
		GhoulAttackSound.setMinDistance(0.5f);
		GhoulAttackSound.setRollOff(5.0f);

		BGSound.setMaxDistance(10.0f);
		BGSound.setMinDistance(0.5f);
		BGSound.setRollOff(5.0f);
	}

	@Override
	public void buildObjects(){	
		// build avatar in the center of the window
		avatar = new GameObject(GameObject.root(), avatarS, avatartx);

		// ---------- MAP COMPONENTS & EXTRAS ----------------

		lineX = new GameObject(GameObject.root(), lineSx, linetx);
		lineY = new GameObject(GameObject.root(), lineSy, linetx);
		lineZ = new GameObject(GameObject.root(), lineSz, linetx);
		lineX.getRenderStates().setColor(new Vector3f(1.0f, 0.0f, 0.0f));
		lineY.getRenderStates().setColor(new Vector3f(0.0f, 1.0f, 0.0f));
		lineZ.getRenderStates().setColor(new Vector3f(0.0f, 0.0f, 1.0f));

		floor = new GameObject(GameObject.root(), floorS, floortx);

		ground = new GameObject(GameObject.root(), groundS, groundtx);

		sanctum = new GameObject(GameObject.root(), sanctumS, sanctumtx);
		sanctum.getRenderStates().hasLighting(false);

		chamber = new GameObject(sanctum, chamberS, chambertx);

		floor.setLocalTranslation((new Matrix4f()).translation(0f, floorYLevel, 0f));
		floor.setLocalScale((new Matrix4f()).scaling(floorSize));
		//floor.setHeightMap(floorheightmap);
		floor.getRenderStates().setTiling(1);
		floor.getRenderStates().setTileFactor((int) floorSize/2);
		floor.getRenderStates().hasLighting(false);

		ground.setLocalTranslation((new Matrix4f()).translation(0f, -10f, 0f));
		ground.setLocalScale((new Matrix4f()).scaling(100f));
		ground.setLocalRotation((new Matrix4f()).rotationX((float) Math.toRadians(180)));
		ground.setHeightMap(groundheightmap);
		ground.getRenderStates().setTiling(1);
		ground.getRenderStates().setTileFactor(10);

		//chamber.setLocalTranslation((new Matrix4f()).translation(0f, 15f, 0f));
		//chamber.setLocalScale((new Matrix4f()).scaling(8f));
		chamber.getRenderStates().setRenderHiddenFaces(true);

		cone = new GameObject(GameObject.root(), coneS, conetx);
		eye = new GameObject(sanctum, eyeS, eyetx);

		//Utilizes Scene Graph
		stars1 = new GameObject(sanctum, stars1S, stars1tx);
		stars2 = new GameObject(stars1, stars2S, stars2tx);
		starLarge = new GameObject(sanctum, starLargeS, starLargetx);
		upperChamber = new GameObject(starLarge, upperChamberS, upperChambertx);

		cone.setLocalScale((new Matrix4f()).scaling(8f));
		eye.setLocalTranslation((new Matrix4f()).translation(0f, 35f, 0f));
		//stars1.setLocalScale((new Matrix4f()).scaling(8f));
		//stars1.setLocalTranslation((new Matrix4f()).translation(0f, 3f, 0f));
		//stars2.setLocalScale((new Matrix4f()).scaling(8f));
		stars2.setLocalTranslation((new Matrix4f()).translation(0f, 3f, 0f));
		//starLarge.setLocalScale((new Matrix4f()).scaling(8f));
		//starLarge.setLocalTranslation((new Matrix4f()).translation(0f, 15f, 0f));
		sanctum.setLocalScale((new Matrix4f()).scaling(20f));
		sanctum.setLocalRotation(new Matrix4f().rotationY((float) Math.toRadians(180)));
		sanctum.setLocalTranslation((new Matrix4f()).translation(0f, 45f, 95f));
		upperChamber.setLocalScale((new Matrix4f()).scaling(25f));
		upperChamber.setLocalTranslation((new Matrix4f()).translation(0f, 35f, 0f));
		upperChamber.getRenderStates().setRenderHiddenFaces(true);

		//Generate the map
		buildMap(1);

		avatar.setLocalScale((new Matrix4f()).scaling(avatarHeight));
		
		avatar.getRenderStates().setRenderHiddenFaces(false);
		double[ ] tempTransform;
		Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		avatarHitbox = (engine.getSceneGraph()).addPhysicsCylinder(
			0f, tempTransform, 0.5f, 1f);
		avatarHitbox.setBounciness(0f);
		avatarHitbox.setFriction(0f);

		//Eye of Ego
		eyeOfEgo = new GameObject(GameObject.root(), eyeOfEgoS, eyeOfEgotx);
		eyeOfEgo.setLocalScale((new Matrix4f()).scaling(eyeOfEgoScaling));
		eyeOfEgo.setLocalTranslation((new Matrix4f()).translation(avatar.getWorldLocation().add(new Vector3f(50f, 150f, -23f))));
		eyeOfEgo.getRenderStates().hasLighting(false);
		eyeOfEgo.getRenderStates().isTransparent(true);
	}

	/** Builds the world's map using the Map.java class */
	public void buildMap(int mapID){
		//temp variables
		int x = 0;
		int y = 0;
		int entityListSize;
		int locState = 0;

		for (int i = 0; i < mm.getMapWidth(mapID); i++){
			for (int j = 0; j < mm.getMapHeight(mapID); j++){
				entityListSize = em.getEntityListSize();
				
				locState = mm.getMapLocationState(mapID, i, j);

				//Create Wall at location
				if (locState != 0){
					x = i - (mm.getMapWidth(1)/2);
					y = j- (mm.getMapHeight(1)/2);
				}
				if (locState == 1 || locState == 8 || locState == 9){
					Entity wall = new Entity();
					if (locState == 8){ //Windowed Wall
						try {
							wall = em.createEntity(entityListSize, windowS, windowtx, "Door");
						}
						catch (Exception e){
							System.out.println("\nCouldn't create Entity with ID " + entityListSize);
						}
					}
					else {
						try {
							wall = em.createEntity(entityListSize, wallS, walltx, "Door");
						}
						catch (Exception e){
							System.out.println("\nCouldn't create Entity with ID " + entityListSize);
						}
					}
					
					float[] wallVerts = wallS.getVertices();
					float modelOffset = (float) Math.abs(wallVerts[6] - wallVerts[7]);

					wall.setLocalScale((new Matrix4f()).scale(wallWidth, wallHeight, wallWidth));
					wall.setLocalTranslation((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)));
					wall.setLocalRotation((new Matrix4f()).rotationX((float) Math.toRadians(-90)));
					if (locState == 9)
						wall.getRenderStates().disableRendering();
					else if (locState == 8){
						wall.getRenderStates().setRenderHiddenFaces(true);
						wall.getRenderStates().setTiling(0);
						wall.getRenderStates().hasLighting(false);
					}
					else {
						wall.getRenderStates().setRenderHiddenFaces(true);
						//wall.getRenderStates().setTiling(1);
						//wall.getRenderStates().setTileFactor((int) wallWidth);
						wall.getRenderStates().hasLighting(false);
					}

					float[] size = {modelOffset * wallWidth, modelOffset * wallHeight, modelOffset * wallWidth};
					PhysicsObject cube = (engine.getSceneGraph()).addPhysicsBox(0.0f, toDoubleArray((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)).get(vals)), size, true);
					cube.setBounciness(0);
					mapHitboxes.add(cube);
					wall.setPhysicsObject(cube);
				}
				//Create W-E door at location
				else if (locState == 2){
					Entity door = new Entity();

					float[] wallVerts = wallS.getVertices();
					float modelOffset = (float) Math.abs(wallVerts[6] - wallVerts[7]);

					try {
						door = em.createEntity(entityListSize, doorS, doortx, "Door");
					}
					catch (Exception e){
						System.out.println("\nCouldn't create Entity with ID " + entityListSize);
					}

					door.setLocalScale((new Matrix4f()).scale(wallWidth, wallHeight, wallWidth));
					door.setLocalTranslation((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)));
					//door.yaw(180);;
					
					door.getRenderStates().setRenderHiddenFaces(true);
					door.getRenderStates().setTiling(1);
					door.getRenderStates().hasLighting(false);
					

					//17.53% the width of typical wall
					float[] size = {modelOffset * wallWidth, modelOffset * wallHeight, modelOffset * wallWidth * 0.1753f};
					
					PhysicsObject cube = (engine.getSceneGraph()).addPhysicsBox(0.0f, toDoubleArray((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)).get(vals)), size);
					cube.setBounciness(0);

					mapHitboxes.add(cube);
					door.setPhysicsObject(cube);
				}
				//Create N-S door at location
				else if (locState == 3){
					Entity door = new Entity();

					float[] wallVerts = wallS.getVertices();
					float modelOffset = (float) Math.abs(wallVerts[6] - wallVerts[7]);
					
					try {
						door = em.createEntity(entityListSize, doorhorizS, doortx, "Door");
					}
					catch (Exception e){
						System.out.println("\nCouldn't create Entity with ID " + entityListSize);
					}

					door.setLocalScale((new Matrix4f()).scale(wallWidth, wallHeight, wallWidth));
					door.setLocalTranslation((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)));
					door.setLocalRotation((new Matrix4f()).rotateLocalY((float) Math.toRadians(-90)).rotationX((float) Math.toRadians(-90)));
					
					door.getRenderStates().setRenderHiddenFaces(true);
					door.getRenderStates().setTiling(1);
					door.getRenderStates().hasLighting(false);

					//17.53% the width of typical wall
					float[] size = {modelOffset * wallWidth, modelOffset * wallHeight, modelOffset * wallWidth * 0.1753f};
					
					PhysicsObject cube = (engine.getSceneGraph()).addPhysicsBox(0.0f, toDoubleArray((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)).get(vals)), size);
					cube.setBounciness(0);

					mapHitboxes.add(cube);
					door.setPhysicsObject(cube);
				}
				//Create a Ghoul Spawn at location
				else if (locState == 'G'){
					Entity ghoul = new Entity();
					try {
						ghoul = em.createEntity(entityListSize, ghoulS, ghoultx, new Vector3f((float) (x * mapUnitSize), 0f, (float) (y * mapUnitSize)), true, "Ghoul", ghoulScaling);
					}
					catch (Exception e){
						System.out.println("\nCouldn't create Entity with ID " + entityListSize);
					}

					ghoul.setID(entityListSize);

					tempMat4 = new Matrix4f(avatar.getLocalTranslation());
					tempTrans = toDoubleArray(tempMat4.get(vals));
					PhysicsObject ghoulHitbox = (engine.getSceneGraph()).addPhysicsCylinder(
						0f, tempTrans, 0.5f, 1f);
					ghoulHitbox.setBounciness(0f);
					ghoulHitbox.setFriction(0f);
					ghoulHitbox.setType("Ghoul");
					ghoulHitbox.setDisconnectedParent(ghoul);
					entityHitboxes.add(ghoulHitbox); //Will now update every frame to go to this specific ghoul object
				}
				else if (locState == '@'){ //Can re-use these to save space
					try {
						em.createEntity(entityListSize, padS, padtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling);
						entityListSize++;
						rc_reverse_ObjectsToAddLater.add(em.createEntity(entityListSize, padRing0S, padRingtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling));
						entityListSize++;
						rc_ObjectsToAddLater.add(em.createEntity(entityListSize, padRing1S, padRingtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling));
						entityListSize++;
						rc_reverse_ObjectsToAddLater.add(em.createEntity(entityListSize, padRing2S, padRingtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling));
						entityListSize++;
						rc_ObjectsToAddLater.add(em.createEntity(entityListSize, padRing3S, padRingtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling));
						entityListSize++;
						rc_reverse_ObjectsToAddLater.add(em.createEntity(entityListSize, padRing4S, padRingtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling));
						entityListSize++;
						rc_ObjectsToAddLater.add(em.createEntity(entityListSize, padArrowS, padArrowtx, new Vector3f((float) (x * mapUnitSize), floorYLevel, (float) (y * mapUnitSize)), true, "NextLevelPad", padScaling));
						
					}
					catch (Exception e){
						System.out.println("\nCouldn't create Entity with ID " + entityListSize);
					}
				}
				else if (locState == 5){
					Entity chest = new Entity();
					try {
						chest = em.createEntity(entityListSize, chestS, chesttx, "Chest");
					}
					catch (Exception e){
						System.out.println("\nCouldn't create Entity with ID " + entityListSize);
					}

					float[] wallVerts = wallS.getVertices();
					float modelOffset = (float) Math.abs(wallVerts[6] - wallVerts[7]);

					chest.setID(entityListSize);

					chest.setLocalScale((new Matrix4f()).scale(wallWidth, wallHeight, wallWidth));
					chest.setLocalTranslation((new Matrix4f()).translation((float) (x * mapUnitSize), (modelOffset * wallHeight/2.0f) - (modelOffset/2.0f), (float) (y * mapUnitSize)));
					
					chest.getRenderStates().setRenderHiddenFaces(true);
					chest.getRenderStates().setTiling(1);
					chest.getRenderStates().hasLighting(false);
				}
				entityListSize++;
			}
		}
		x = (mm.getPlayerLocation(mapID)[0] - (mm.getMapWidth(1)/2));
		y= (mm.getPlayerLocation(mapID)[1] - (mm.getMapHeight(1)/2));

		avatar.setLocalTranslation((new Matrix4f()).translation(x * mapUnitSize, 0f, y * mapUnitSize));
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

		//Victory light - fully green
		light4 = new Light();
		light4.setDiffuse(0.0f, 1f, 0.0f);
		light4.setSpecular(0.0f, 1f, 0.0f);

		(engine.getSceneGraph()).addLight(light1);
		(engine.getSceneGraph()).addLight(light2);
		(engine.getSceneGraph()).addLight(light3);
	}

	@Override
	public void initializeGame(){	
		im = engine.getInputManager();

		setupNetworking();

		MoveAction move = new MoveAction(this, protClient, 1);
		MoveAction moveBackward = new MoveAction(this, protClient, -1);
		StrafeAction strafe = new StrafeAction(this, protClient, 1);
		StrafeAction strafeRight = new StrafeAction(this, protClient, -1);
		TurnAction turn = new TurnAction(this, protClient, yawAmount); //Gamepad only
		PitchAction pitchUp = new PitchAction(this, protClient, pitchAmount);
		PitchAction pitchDown = new PitchAction(this, protClient, -pitchAmount);
		DisableAxisAction disableAxis = new DisableAxisAction(this);
		SprintAction sprint = new SprintAction(this, protClient, sprintSpeed);
		JumpAction jump = new JumpAction(this, protClient, jumpAmount);

		//Controller actions
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.Y, move, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllGamepads(net.java.games.input.Component.Identifier.Axis.X, turn, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);

		//Keyboard actions
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.W, move, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.S, moveBackward, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.A, strafe, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.D, strafeRight, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.UP, pitchUp, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.DOWN, pitchDown, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.Q, disableAxis, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.LSHIFT, sprint, InputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateActionWithAllKeyboards(net.java.games.input.Component.Identifier.Key.SPACE, jump, InputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		
		lastFrameTime = System.currentTimeMillis();
		currFrameTime = System.currentTimeMillis();
		elapsTime = 0.0;

		(engine.getRenderSystem()).setWindowDimensions(1900,1000);

		// ------------- Setting up main camera -------------
		cam = (engine.getRenderSystem().getViewport("LEFT").getCamera());

		//* Minimap Camera for Debugging */
		minimap = (engine.getRenderSystem().getViewport("RIGHT").getCamera());

		String gamepadName = im.getFirstGamepadName();

		minimapController = new CameraMinimap(minimap, floor, gamepadName, engine);

		rc = new RotationController(engine, new Vector3f(0, 1, 0), 0.002f);
		rc_reverse = new RotationController(engine, new Vector3f(0, 1, 0), -0.002f);
		bc = new BobbingController(engine, 0.0005f);
		sc = new ShootingController(engine, 0.005f);

		(engine.getSceneGraph()).addNodeController(rc);
		(engine.getSceneGraph()).addNodeController(bc);
		(engine.getSceneGraph()).addNodeController(sc);

		rc.enable();
		rc_reverse.enable();
		bc.enable();
		sc.enable();

		rc.addTarget(chamber);
		rc.addTarget(stars1);
		rc.addTarget(stars2);
		rc.addTarget(starLarge);

		for (GameObject go : rc_ObjectsToAddLater){
			rc.addTarget(go);
		}

		for (GameObject go : rc_reverse_ObjectsToAddLater){
			rc_reverse.addTarget(go);
		}

		bc.addTarget(upperChamber);

		initMouseMode();

		// Animation
		//avatarS.playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);
		ghoulS.playAnimation("IDLE", 0.1f, AnimatedShape.EndType.LOOP, 0);
	
		// Camera
		

		// Sound
		AttackSound.setLocation(avatar.getWorldLocation());
		GhoulAttackSound.setLocation(avatar.getWorldLocation());
		BGSound.setLocation(ground.getWorldLocation());
		setEarParameters();
		BGSound.play(); //Plays background music

		//Physics
		// --- initialize physics system ---
		float[] gravity = {0f, -5f, 0f};
		physicsEngine = (engine.getSceneGraph()).getPhysicsEngine();
		physicsEngine.setGravity(gravity);
		// --- create physics world ---
		float mass = 1.0f;
		float up[ ] = {0,1,0};
		float radius = 0.75f;
		float height = 2.0f;
		double[ ] tempTransform;
		Matrix4f translation = new Matrix4f();
		translation = new Matrix4f(floor.getLocalTranslation());
		tempTransform = toDoubleArray(translation.get(vals));
		planeP = (engine.getSceneGraph()).addPhysicsStaticPlane(
		tempTransform, up, 0.0f);
		planeP.setBounciness(1.0f);
		floor.setPhysicsObject(planeP);
		
		avatar.yaw(90);
		cam.yaw(90);
		engine.enableGraphicsWorldRender();
		//engine.enablePhysicsWorldRender();

		gameStartTime = System.currentTimeMillis();
		totalElapsedTime = System.currentTimeMillis();
	}

	public void setEarParameters(){
		audioMgr.getEar().setLocation(avatar.getWorldLocation());
		audioMgr.getEar().setOrientation(cam.getN(), new Vector3f(0.0f, 1.0f, 0.0f));
	}

	@Override
	public void update(){
		if (!gameOver && !hasWon){	
			//Update elapsed time based on current frame and last frame's time
			recenterMouse();
			previousAvatarLocation = avatar.getLocalLocation();
			movementSpeed = DEFAULT_SPEED; //Resets; if not sprinting, will be normal speed
			lastFrameTime = currFrameTime;
			currFrameTime = System.currentTimeMillis();
			elapsTime = (currFrameTime - lastFrameTime) / 10.0;

			//Player Updates
			minimapController.updateCameraPosition();
			avatar.setLocalRotation(cam.getLocalRotation());

			if (avatar.isJumping()){
				avatar.incrementJump((float) elapsTime, protClient);
			}

			invincibilityWindow((float) elapsTime); //Check if invincible, aka has been recently damaged

			//Update hitboxes
			Matrix4f translation = new Matrix4f(avatar.getLocalTranslation());
			temp = toDoubleArray((new Matrix4f(avatar.getLocalTranslation())).get(vals));
			avatarHitbox.setTransform(temp);

			//Updates all entity hitboxes to go to where their disconnected parent is
			for (PhysicsObject po : entityHitboxes){
				translation = new Matrix4f(po.getDisconnectedParent().getLocalTranslation());
				temp = toDoubleArray((new Matrix4f(po.getDisconnectedParent().getLocalTranslation())).get(vals));
				po.setTransform(temp);
			}

			// ---------- MAP COMPONENTS & EXTRAS ----------------
			eyeOfEgo.lookAt(avatar);

			//Pseudo Physics -- Jumping objects
			updateAllPseudoPhysicsObjects();

			//Physics
			Matrix4f currentTranslation, currentRotation;
			AxisAngle4f aa = new AxisAngle4f();
			Matrix4f mat = new Matrix4f();
			Matrix4f mat2 = new Matrix4f().identity();
			Matrix4f mat3 = new Matrix4f().identity();
			checkForCollisions();
			physicsEngine.update((float) elapsTime * 7);
			for (GameObject go:engine.getSceneGraph().getGameObjects()){ 
				if (go.getPhysicsObject() != null){ 
					// set translation
					mat.set(toFloatArray(go.getPhysicsObject().getTransform()));
					mat2.set(3,0,mat.m30());
					mat2.set(3,1,mat.m31());
					mat2.set(3,2,mat.m32());
					go.setLocalTranslation(mat2);
					// set rotation
					mat.getRotation(aa);
					mat3.rotation(aa);
					go.setLocalRotation(mat3);
				}
			} 

			//Movement speed is based on elapsed time; InputManager gets updated and Action classes use this speed variable
			movementSpeed = DEFAULT_SPEED * (float) elapsTime;

			//Animations
			//avatarS.updateAnimation();
			ghoulS.updateAnimation();

			eye.lookAt(avatar.getWorldLocation());

			//Input Manager
			im.update((float) elapsTime);

			//Entity Manager iterates through all entities
			em.updateAllEntities((float) elapsTime);

			//Networking
			processNetworking((float) elapsTime);

			//Sound operations
			AttackSound.setLocation(avatar.getWorldLocation());
			setEarParameters();

			//Camera operations
			cam.setLocation(avatar.getWorldLocation());

			//avatar cannot go beneath the Y level of the floor plane
			if (avatar.getWorldLocation().y() < floor.getWorldLocation().y())
				avatar.setLocalLocation((avatar.getWorldLocation().mul(new Vector3f(1f, 0f, 1f))).add(new Vector3f(0f, -1f, 0f)));

			//Set avatar to height of height map to make them go over the peaks
			loc = avatar.getWorldLocation();
			height = floor.getHeight(loc.x(), loc.z());
			avatar.setLocalLocation(new Vector3f(loc.x(), height, loc.z()));

			if (PlayerHP <= 0){
				PlayerHP = 0;
				GameOver();
			}

			if (hasWon){
				light4.setLocation(avatar.getWorldLocation().add(new Vector3f(0, 2, 0)));
			}

			// Broadcast any new message and update HUD 1
			broadcast(broadcastMessage, hudColor);

			
			sVal = (String) String.format("%.2f", (System.currentTimeMillis() - gameStartTime)/1000);
			totalElapsedTime = Double.parseDouble(sVal);
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

		//Minimap
		(engine.getRenderSystem()).addViewport("RIGHT",.75f,0,.25f,.25f);
		Viewport rightVp = (engine.getRenderSystem()).getViewport("RIGHT");
		Camera rightCamera = rightVp.getCamera();
		rightVp.setHasBorder(true);
		rightVp.setBorderWidth(1);
		rightVp.setBorderColor(1.0f, 1.0f, 1.0f);
		rightCamera.setLocation(new Vector3f(0,2,0));
		rightCamera.setU(new Vector3f(1,0,0));
		rightCamera.setV(new Vector3f(0,0,-1));
		rightCamera.setN(new Vector3f(0,-1,0));
	}

	//Broadcasting function that can announce a broadcast message on the HUD
	public void broadcast(String message){
		(engine.getHUDmanager()).setHUD1("HP: " + Integer.toString(PlayerHP) + " | KILLS: " + Integer.toString(killCount) + "| TIME: " + Double.toString(totalElapsedTime) + message, Constants.hudWhiteColor, 15, 15);
	}

	//Overloaded in case you want something other than the default white color for the HUD
	public void broadcast(String message, Vector3f HUDColor){
		(engine.getHUDmanager()).setHUD1("HP: " + Integer.toString(PlayerHP) + " | KILLS: " + Integer.toString(killCount) + "| TIME: " + Double.toString(totalElapsedTime) + message, HUDColor, 15, 15);
	}

	public void setBroadcastMessage(String message){
		broadcastMessage = message;
	}

	public void setBroadcastMessageColor(Vector3f HUDColor){
		this.hudColor = HUDColor;
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

	public double getDeltaTime(){
		return elapsTime;
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

	// ---------- MOUSE SECTION ----------------

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
		Image faceImage = new ImageIcon("./assets/textures/eyecrosshair.png").getImage();
		Cursor faceCursor = Toolkit.getDefaultToolkit().
		createCustomCursor(faceImage, new Point(0,0), "FaceCursor");
		rs.getGLCanvas().setCursor(faceCursor);
	}

	//Add this later for controller, Action class specifically added for gamepads
	@Override
	public void mousePressed(MouseEvent e){
		shootOrbFrom(avatar, "Eye");
	}

	@Override
	public void mouseMoved(MouseEvent e)
	{ 	// if robot is recentering and the MouseEvent location is in the center,
		// then this event was generated by the robot
 		// event was due to a user mouse-move, and must be processed
		if (recenteringAllowed){
			curMouseX = e.getXOnScreen();
			curMouseY = e.getYOnScreen();
			float mouseDeltaX = prevMouseX - curMouseX;
			float mouseDeltaY = prevMouseY - curMouseY;
			cam.yaw(mouseDeltaX  * cameraMoveSpeed / (float)(elapsTime));
			cam.pitch(mouseDeltaY  * cameraMoveSpeed / (float)(elapsTime));
			avatar.setLocalRotation(cam.getLocalRotation());
			protClient.sendRotateMessage(avatar.getWorldRotation());
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
		if (recenteringAllowed){
			RenderSystem rs = engine.getRenderSystem();
			Viewport vw = rs.getViewport("LEFT");
			float left = vw.getActualLeft();
			float bottom = vw.getActualBottom();
			float width = vw.getActualWidth();
			float height = vw.getActualHeight();
			centerX = (int) (left + width/2.0f);
			centerY = (int) (bottom - height/2.0f);
			robot.mouseMove((int)centerX, (int)centerY);
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

	// ---------- PHYSICS SECTION ----------------

	public void addPseudoPhysicsObject(GameObject obj){
		pseudoPhysicsObjects.add(obj);
	}

	public void updateAllPseudoPhysicsObjects(){
		GameObject temp;
		Iterator<GameObject> it = pseudoPhysicsObjects.iterator();

		while(it.hasNext()){
			temp = it.next();
		}
	}

	private void checkForCollisions(){	
		com.bulletphysics.dynamics.DynamicsWorld dynamicsWorld;
		com.bulletphysics.collision.broadphase.Dispatcher dispatcher;
		com.bulletphysics.collision.narrowphase.PersistentManifold manifold;
		com.bulletphysics.dynamics.RigidBody object1, object2;
		com.bulletphysics.collision.narrowphase.ManifoldPoint contactPoint;

		dynamicsWorld = ((JBulletPhysicsEngine)physicsEngine).getDynamicsWorld();
		dispatcher = dynamicsWorld.getDispatcher();
		int manifoldCount = dispatcher.getNumManifolds();
		for (int i=0; i < manifoldCount; i++)
		{	manifold = dispatcher.getManifoldByIndexInternal(i);
			object1 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody0();
			object2 = (com.bulletphysics.dynamics.RigidBody)manifold.getBody1();
			JBulletPhysicsObject obj1 = JBulletPhysicsObject.getJBulletPhysicsObject(object1);
			JBulletPhysicsObject obj2 = JBulletPhysicsObject.getJBulletPhysicsObject(object2);
			for (int j = 0; j < manifold.getNumContacts(); j++)
			{	contactPoint = manifold.getContactPoint(j);
				if (contactPoint.getDistance() < 0.1f)
				{	System.out.println("---- hit between " + obj1 + " and " + obj2);
					if (!isPlayerInvincible && (obj1.equals(avatarHitbox) || obj2.equals(avatarHitbox)) && (obj1.getType() == "Ego" || obj2.getType() == "Ego" || obj1.getType() == "Ghoul" || obj2.getType() == "Ghoul" )){
						if (!obj1.equals(avatarHitbox))
							PlayerHP -= obj1.getDamage();
						else
							PlayerHP -= obj2.getDamage(); 
						System.out.println("\nPlayer hit!");
						isPlayerInvincible = true; //IFrames to prevent spam damage
					}
					else if ((obj1.equals(avatarHitbox) || obj2.equals(avatarHitbox))){
						avatar.setLocalLocation(previousAvatarLocation);
						System.out.println("\nPlayer collided with an object!");
					}
					else if ((obj1.getType() == "Ghoul"  || obj2.getType() == "Ghoul" ) && (obj1.getType() == "Eye"  || obj2.getType() == "Eye" )){
						if (obj1.getType() == "Ghoul"){
							obj1.getDisconnectedParent().takeDamage(50);
							if (!((Entity) (obj1.getDisconnectedParent())).isKilled() && ((Entity) (obj1.getDisconnectedParent())).getHP() <= 0){
								((Entity) (obj1.getDisconnectedParent())).setIsKilled(true);
								killCount++;
							}
						}
						else {
							obj2.getDisconnectedParent().takeDamage(50);
							obj2.getDisconnectedParent().takeDamage(50);
							if (!((Entity) (obj2.getDisconnectedParent())).isKilled() && ((Entity) (obj2.getDisconnectedParent())).getHP() <= 0){
								((Entity) (obj2.getDisconnectedParent())).setIsKilled(true);
								killCount++;
							}
						}
						System.out.println("\nEnemy damaged!"); //50 HP taken away out of default 100

					}
					break;
				}
			}
		}
	}

	private float[] toFloatArray(double[] arr){ 
		if (arr == null) return null;
		int n = arr.length;
		float[] ret = new float[n];
		for (int i = 0; i < n; i++){ 
			ret[i] = (float)arr[i];
		}
		return ret;
	}

	private double[] toDoubleArray(float[] arr){ 
		if (arr == null) return null;
		int n = arr.length;
		double[] ret = new double[n];
		for (int i = 0; i < n; i++){ 
			ret[i] = (double)arr[i];
		}
		return ret;
	}

	private Matrix4f toMatrix4f(double[] arr){
		if (arr == null) return null;
		int n = arr.length;
		Matrix4f mat4 = new Matrix4f();
		float[] ret = new float[n];
		for (int i = 0; i < n; i++){ 
			ret[i] = (float)arr[i];
		}

		mat4 = mat4.set(ret);
		
		return mat4;
	}

	// ---------- COMBAT SECTION ----------------
	public void shootOrbFrom(GameObject fromObject, String type){
		protClient.sendShootMessage(fromObject.getLocalLocation(), fromObject.getLocalForwardVector(), type);

		GameObject bulletOrb;
		switch (type){
			case "Ego": //Ghoul spawn orb ammo type
				GhoulAttackSound.play();
				bulletOrb = new GameObject(GameObject.root(), egoOrbS, egoOrbtx);
				bulletOrb.setLocalScale((new Matrix4f()).scaling(0.05f));
				break;
			default: 
				AttackSound.play();
				bulletOrb = new GameObject(GameObject.root(), eyeS, eyetx);
				bulletOrb.setLocalScale((new Matrix4f()).scaling(0.1f));
		}

		Vector3f direction = new Vector3f();
		if (fromObject.equals(avatar)){
			bulletOrb.setLocalLocation(cam.getLocation());
			direction = cam.getN();
		}
		else{
			bulletOrb.setLocalLocation(fromObject.getLocalLocation());
			direction = fromObject.getLocalForwardVector();
		}

		double[ ] tempTransform;
		Matrix4f translation = new Matrix4f(bulletOrb.getLocalTranslation());

		tempTransform = toDoubleArray(translation.get(vals));
		caps2P = (engine.getSceneGraph()).addPhysicsSphere(
			0.001f, tempTransform, 0.1f);
		caps2P.setBounciness(1f);
		caps2P.setType(type);

		bulletOrb.setPhysicsObject(caps2P);

		Vector3f force = new Vector3f().add(fromObject.getLocalForwardVector().mul(bulletForce));
		bulletOrb.getPhysicsObject().applyForce(force.x(), force.y(), force.z(), 0.0f, 0.0f, 0.0f);
	}

	public void shootOrbFrom(Vector3f position, Vector3f direction, String type){
		protClient.sendShootMessage(position, direction, type);
		
		AttackSound.play();

		GameObject bulletOrb;
		switch (type){
			case "Ego": //Ghoul spawn orb ammo type
				bulletOrb = new GameObject(GameObject.root(), egoOrbS, egoOrbtx);
				bulletOrb.setLocalScale((new Matrix4f()).scaling(0.05f));
				break;
			default: 
				bulletOrb = new GameObject(GameObject.root(), eyeS, eyetx);
				bulletOrb.setLocalScale((new Matrix4f()).scaling(0.1f));
		}

		bulletOrb.setLocalLocation(position);

		double[ ] tempTransform;
		Matrix4f translation = new Matrix4f(bulletOrb.getLocalTranslation());

		tempTransform = toDoubleArray(translation.get(vals));
		caps2P = (engine.getSceneGraph()).addPhysicsSphere(
			0.001f, tempTransform, 0.1f);
		caps2P.setBounciness(1f);
		caps2P.setType(type);

		bulletOrb.setPhysicsObject(caps2P);

		Vector3f force = new Vector3f().add(direction.mul(bulletForce));
		bulletOrb.getPhysicsObject().applyForce(force.x(), force.y(), force.z(), 0.0f, 0.0f, 0.0f);
	}

	public void invincibilityWindow(float deltaTime){
		if (isPlayerInvincible){
			cooldownCounter -= deltaTime/10;
			if (cooldownCounter <= 0){
				isPlayerInvincible = false; //Invincibility period is over
				cooldownCounter = iFrameDuration; //Reset to default
			}
		}
	}

	/** Returns true if object can see other object (no other objects in the way, uses ray) */
	public boolean hasClearViewToObject(GameObject obj){
		//Implement this later
		return true;
	}

	// ---------- MISC. KEYBOARD-ONLY INPUTS ----------------
	//KeyListener implemented functions
	@Override
	public void keyPressed(KeyEvent e){	
		switch (e.getKeyCode()){
			case KeyEvent.VK_0:
				isPhysicsWorldRendered = !isPhysicsWorldRendered;
				if (isPhysicsWorldRendered)
					engine.enablePhysicsWorldRender();
				else
					engine.disablePhysicsWorldRender();
				break;
			case KeyEvent.VK_ESCAPE:
				//Toggle auto-centering for mouse cursor
				recenteringAllowed = !recenteringAllowed;
		}
	}

	//Required implementations, but not used
	@Override
	public void keyReleased(KeyEvent e){}

	@Override
	public void keyTyped(KeyEvent e){}

	// ---------- REST OF GETTERS AND SETTERS ----------------
	public void Win(){
		hasWon = true;
	}
	
	public void GameOver(){
		gameOver = true;
	}

	public float getDefaultSpeed(){
		return this.DEFAULT_SPEED;
	}

	public float getMovementSpeed(){
		return this.movementSpeed;
	}

	public void setMovementSpeed(float speed){
		this.movementSpeed = speed;
	}
}