package newrealm;

import java.awt.Color;
import java.io.IOException;
import java.lang.Math;
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
	private boolean hostile = true; //If hostile entities will act hostile or not
	private float ghoulHostileDistance = 10.0f; //How close for Ghoul to start going after player

	public EntityManager(VariableFrameRateGame vfrg, ProtocolClient pc){	
		this.game = (MyGame)vfrg;
		this.pc = pc;
	}

	public Entity createEntity(int id, ObjShape s, TextureImage t, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, type);
		newEntity.initFiringCooldown();
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);

		return newEntity;
	}

	/** Used to add a new entity to the entities list 
	 * that will be iterated through on every update call 
	 */
	 public Entity createEntity(int id, AnimatedShape s, TextureImage t, Vector3f position, boolean renderHidden, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		newEntity.initFiringCooldown();
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);

		return newEntity;
	}

	/** Allows for custom scaling of object rather than the default of 1.0f */
	public Entity createEntity(int id, AnimatedShape s, TextureImage t, Vector3f position, boolean renderHidden, String type, float scaleAmount) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(scaleAmount);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);

		return newEntity;
	}

	public Entity createEntity(UUID id, AnimatedShape s, TextureImage t, Vector3f position, boolean renderHidden, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);

		return newEntity;
	}

		/** Allows for custom scaling of object rather than the default of 1.0f */
	public Entity createEntity(int id, ObjShape s, TextureImage t, Vector3f position, boolean renderHidden, String type, float scaleAmount) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(scaleAmount);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);

		return newEntity;
	}
	
	public Entity createEntity(UUID id, ObjShape s, TextureImage t, Vector3f position, boolean renderHidden, String type) throws IOException {	
		System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position, renderHidden, type);
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);

		return newEntity;
	}
	
	public void removeEntity(int id)
	{	Entity entity = findEntity(id);
		if(entity != null)
		{	game.getEngine().getSceneGraph().removeGameObject(entity);
			entities.remove(entity);
		}
		else
		{	System.out.println("tried to remove, but unable to find entity in list");
		}
	}

	/* 
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
	*/
	
	private Entity findEntity(int id)
	{	Entity entity;
		Iterator<Entity> it = entities.iterator();
		while(it.hasNext())
		{	entity = it.next();
			if(entity.getID() == id)
			{	return entity;
			}
		}		
		return null;
	}

	/* 
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
	}*/
	
	public void updateEntityPosition(int id, Vector3f position)
	{	Entity entity = findEntity(id);
		if (entity != null)
		{	entity.setPosition(position);
		}
		else
		{	System.out.println("tried to update entity position, but unable to find entity in list");
		}
	}

	public void updateEntityRotation(int id, Matrix4f rotation)
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
				if (temp.getHP() > 0){
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
			/*else if (type == "Door"){
				if (distance(avatarPos, temp.getWorldLocation()) <= 3f){
					temp.getLocalTranslation().setRowColumn(2, 4, 3);
				}
				else{ //Raised, player out of range, so lower it now
					temp.getLocalTranslation().setRowColumn(4, 4, 0);
				}
			}*/
			else if (type == "NextLevelPad"){
				if (distance(avatarPos, temp.getWorldLocation()) <= 1.5f){
					game.Win();
				}
			}
		}
	}

	//Helper function for determining distance between 2 Vector3f Objects
	public float distance(Vector3f v1, Vector3f v2){
		//sqrt((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)
		return (float) Math.sqrt(Math.pow((double)(v2.x() - v1.x()), 2) + Math.pow((double)(v2.y() - v1.y()), 2) + Math.pow((double)(v2.z() - v1.z()), 2));
	}
}
