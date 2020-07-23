package automc.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("rawtypes")
public class EntityScanner {

	private Map<Class, List<Entity>> cachedEntities;
	private Map<String, List<EntityItem>> cachedItemEntities;
	private List<EntityAnimal> cachedAnimals;

	public EntityScanner() {
		cachedEntities = new HashMap<>();
		cachedItemEntities = new HashMap<>();
		cachedAnimals = new LinkedList<EntityAnimal>();
	}

	public void onTick() {
		updateCachedEntities();
	}

	public List<Entity> getEntitiesOfClass(Class c) {
		if (!cachedEntities.containsKey(c)) return new ArrayList<Entity>(); // empty.
		return cachedEntities.get(c);
	}
	public List<EntityItem> getDroppedItems(String name) {
		if (!cachedItemEntities.containsKey(name)) return new ArrayList<EntityItem>(); // empty.
		return cachedItemEntities.get(name);
	}
	public List<EntityItem> getDroppedItems(Item item) {
		return getDroppedItems(ItemUtil.getItemId(item));
	}
	public boolean itemExists(String item) {
		return getDroppedItems(item).size() != 0;
	}
	public boolean itemExists(Item item) {
		return itemExists(ItemUtil.getItemId(item));
	}

	public EntityItem getClosestDroppedItem(String item, Set<BlockPos> blacklist) {
		Entity player = Minecraft.getMinecraft().player;
		double minSqrDist = Double.POSITIVE_INFINITY;
		EntityItem best = null;
		for(EntityItem e : getDroppedItems(item)) {
			if (blacklist != null && blacklist.contains(e.getPosition())) continue;
			double ds = player.getDistanceSq(e);
			if (ds < minSqrDist) {
				minSqrDist = ds;
				best = e;
			}
		}
		return best;
	}
	public EntityItem getClosestDroppedItem(Item item, Set<BlockPos> blacklist) {
		return getClosestDroppedItem(ItemUtil.getItemId(item), blacklist);
	}
	
	public List<EntityAnimal> getAnimals() {
		return cachedAnimals;
	}

	private void updateCachedEntities() {
		cachedEntities.clear();
		cachedItemEntities.clear();
		cachedAnimals.clear();
		for (Entity e : Minecraft.getMinecraft().world.loadedEntityList) {
			Class c = e.getClass();

			if (!cachedEntities.containsKey(c)) {
				cachedEntities.put(c, new ArrayList<Entity>());
			}
			cachedEntities.get(c).add(e);

			if (e instanceof EntityItem) {
				EntityItem item = (EntityItem) e;
				String id = ItemUtil.getItemId(item.getItem().getItem());
				if (!cachedItemEntities.containsKey(id)) {
					cachedItemEntities.put(id, new ArrayList<EntityItem>());
				}
				cachedItemEntities.get(id).add(item);
			}
			
			if (e instanceof EntityAnimal) {
				cachedAnimals.add((EntityAnimal) e);
			}
		}
	}
}
