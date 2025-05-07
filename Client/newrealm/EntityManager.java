package newrealm;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import org.joml.*;

import tage.*;

public class EntityManager
{
	private MyGame game;
	private Vector<Entity> entities = new Vector<Entity>();

	public EntityManager(VariableFrameRateGame vfrg, ProtocolClient pc){	
		game = (MyGame)vfrg;
	}

	/** Used to add a new entity to the entities list 
	 * that will be iterated through on every update call 
	 */
	public void add(GameObject entity){

	}
	
	public void createEntity(UUID id, ObjShape s, TextureImage t, Vector3f position) throws IOException
	{	System.out.println("adding entity with ID --> " + id);
		Entity newEntity = new Entity(id, s, t, position);
		Matrix4f initialScale = (new Matrix4f()).scaling(1f);
		newEntity.setLocalScale(initialScale);
		entities.add(newEntity);
	}
	
	public void removeEntity(UUID id)
	{	Entity entity = findAvatar(id);
		if(entity != null)
		{	game.getEngine().getSceneGraph().removeGameObject(entity);
			entities.remove(entity);
		}
		else
		{	System.out.println("tried to remove, but unable to find entity in list");
		}
	}

	private Entity findAvatar(UUID id)
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
	{	Entity entity = findAvatar(id);
		if (entity != null)
		{	entity.setPosition(position);
		}
		else
		{	System.out.println("tried to update entity position, but unable to find entity in list");
		}
	}

	public void updateEntityRotation(UUID id, Matrix4f rotation)
	{	Entity entity = findAvatar(id);
		if (entity != null)
		{	entity.setRotation(rotation);
		}
		else
		{	System.out.println("tried to update entity rotation, but unable to find entity in list");
		}
	}
}
