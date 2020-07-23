package automc.containers;

import java.util.HashMap;
import java.util.Map;

import automc.AutoMC;
import automc.Logger;
import automc.utility.ItemUtil;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

// Serializable.
public class CachedChest implements ICachedContainer {

	private BlockPos pos;
	private long openedTime;
	private Map<String, CachedItems> nameSlotMap;

	public CachedChest(BlockPos pos) {
		this.pos = pos;
		nameSlotMap = new HashMap<>();
	}

	public void open(ContainerChest container) {
		nameSlotMap.clear();
		for(Slot slot : container.inventorySlots) {
			ItemStack stack = slot.getStack();
			String name = ItemUtil.getItemId(stack.getItem());
			nameSlotMap.put(name, new CachedItems(stack));
		}
	}

	@Override
	public int itemCount(String item) {
		item = ItemUtil.findId(item);
		if (nameSlotMap.containsKey(item)) {
			return nameSlotMap.get(item).getCount();
		}
		return 0;
	}

	@Override
	public long getTimeLastOpenedMillis() {
		return openedTime;
	}

	@Override
	public long getTimeSinceLastOpenedMillis() {
		return System.currentTimeMillis() - openedTime;
	}

	@Override
	public void resetCache() {
		openedTime = System.currentTimeMillis();
		nameSlotMap.clear();
	}

	@Override
	public BlockPos getPosition() {
		return pos;
	}
	
	@Override
	public Iterable<CachedItems> getSlots() {
		return nameSlotMap.values();
	}

	@Override
	public void addItem(ItemStack stack) {
		String id = ItemUtil.getItemId(stack.getItem());
		if (!nameSlotMap.containsKey(id)) {
			nameSlotMap.put(id, new CachedItems(stack));			
		} else {
			nameSlotMap.get(id).addItems(stack.getCount());
		}
		/*
		if (nameSlotMap.containsKey(id)) {
			Logger.debug(this, "Add items: " + id + " now has " + nameSlotMap.get(id).getCount());
		}
		*/
	}

	@Override
	public int hashCode() {
		if (pos == null) {
			return 0;
		}
		// No need to try items, usually the position and open time is unique enough.
		return pos.hashCode();// * 6132924 + (int)openedTime;
	}

	// To check if it's the same chest, just use position.
	@Override
	public boolean equals(Object o) {
		if (o instanceof CachedChest) {
			CachedChest cc = (CachedChest)o;
			if (!pos.equals(cc.pos)) return false;
			return true;
			/*
			if (openedTime != cc.openedTime) return false;
			for (String key : nameSlotMap.keySet()) {
				if (!cc.nameSlotMap.containsKey(key)) return false;
				if (!nameSlotMap.get(key).equals(cc.nameSlotMap.get(key))) return false;
			}
			return true;
			*/
		}
		return false;
	}

	@Override
	public double getScore(Vec3d playerPos, String item, int maxNeeded) {
		double distanceSqr = playerPos.squareDistanceTo(getPosition().getX(), getPosition().getY(), getPosition().getZ());
		int weHave = itemCount(item);
		if (weHave > maxNeeded) weHave = maxNeeded;
		double specificValue = weHave * AutoMC.getAutoMC().itemWorkDictionary.getWork(item);
		//Logger.log("Has: " + weHave + ", dist Sqr: " + distanceSqr + ", sp: " + specificValue + ", score: " + (specificValue*specificValue - distanceSqr) );
		return specificValue*specificValue - distanceSqr;
	}

}
