package newrealm;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;
import tage.shapes.*;

public class EntityManager
{
	private MyGame game;
	private ProtocolClient pc;
	private Vector<Entity> entities = new Vector<Entity>();
	private boolean hostile = false; //If hostile entities will act hostile or not
	private float ghoulHostileDistance = 15.0f; //How close for Ghoul to start going after player

	public EntityManager(VariableFrameRateGame vfrg, ProtocolClient pc){	
		this.game = (MyGame)vfrg;
		this.pc = pc;
	}

	/** Used to add a new entity to the entities list 
	 * that will be iterated through on every update call 
	 */
	 public void createEntity(int id, AnimatedShape s, TextureImage t, Vector3f position, boolean renderHidden, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		newEntity.initFiringCooldown();
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);
	}

	/** Allows for custom scaling of object rather than the default of 1.0f */
	public void createEntity(int id, AnimatedShape s, TextureImage t, Vector3f position, boolean renderHidden, String type, float scaleAmount) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(scaleAmount);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);
	}

	public void createEntity(UUID id, AnimatedShape s, TextureImage t, Vector3f position, boolean renderHidden, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);
	}
	
	public void createEntity(UUID id, ObjShape s, TextureImage t, Vector3f position, boolean renderHidden, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);
	}
	
	public void removeEntity(UUID id)
	{	Entity entity = findEntity(id);
		if(entity != null)
		{	game.getEngine().getSceneGraph().removeGameObject(entity);
			entities.remove(entity);
		}
		else
		{	System.out.println("tried to remove, but unable to find entity in list");
		}
	}

	private Entity findEntity(UUID id)
	{	Entity entity;
		Iterator<Entity> it = entities.iterator();
		while(it.hasNext())
		{	entity = it.next();
			if(entity.getID().compareTo(id) == 0)
			{	return entity;
			}
		}		
		return null;
	}
	
	public void updateEntityPosition(UUID id, Vector3f position)
	{	Entity entity = findEntity(id);
		if (entity != null)
		{	entity.setPosition(position);
		}
		else
		{	System.out.println("tried to update entity position, but unable to find entity in list");
		}
	}

	public void updateEntityRotation(UUID id, Matrix4f rotation)
	{	Entity entity = findEntity(id);
		if (entity != null)
		{	entity.setRotation(rotation);
		}
		else
		{	System.out.println("tried to update entity rotation, but unable to find entity in list");
		}
	}

	public int getEntityListSize(){
		return entities.size();
	}

	public void updateAllEntities(float deltaTime){
		String type;
		Entity temp;
		GameObject avatar = game.getAvatar();
		Vector3f avatarPos = game.getAvatar().getWorldLocation();
		Iterator<Entity> it = entities.iterator();
		
		while(it.hasNext()){
			temp = it.next();
			type = temp.getType();

			if (type == "Ghoul"){
				//Ghoul continues to look towards and follow the *nearest* player
				if (game.distance(temp.getWorldLocation(), avatar.getWorldLocation()) <= ghoulHostileDistance){
					temp.lookAt(avatar);
					if (hostile){
							temp.move(0.01f * deltaTime);

						if (game.distance(temp.getWorldLocation(), avatar.getWorldLocation()) <= 4.0f){
							game.setBroadcastMessage(" | ENEMY IS CLOSING IN!");
							game.setBroadcastMessageColor(Constants.hudRedColor);
						}
						else {
							game.setBroadcastMessage("");
							game.setBroadcastMessageColor(Constants.hudWhiteColor);
						}
					}

					if (temp.updateFiringCooldown(deltaTime))
						game.shootOrbFrom(temp, "Ego");
				}

			}
		}
	}
}
