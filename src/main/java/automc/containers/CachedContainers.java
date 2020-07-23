package automc.containers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import automc.Logger;
import automc.utility.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

// Serializable.
public class CachedContainers {

	// TODO: Figure out how to have a MAX container/slot limit so it doesn't create problems down the line.

	private Set<ICachedContainer> containers;

	private transient Map<BlockPos, ICachedContainer> containerPositionMap;
	private transient Map<String, Set<ICachedContainer>> containerItemMap;

	public CachedContainers() {
		containers = new HashSet<>();
		containerPositionMap = new HashMap<BlockPos, ICachedContainer>();
		containerItemMap = new HashMap<String, Set<ICachedContainer>>();
	}

	public ICachedContainer getBestContainerWith(String item, int maxNeeded) {
		if (!containerItemMap.containsKey(item)) return null;

		Vec3d playerPos = Minecraft.getMinecraft().player.getPositionVector();

		List<BlockPos> toRemove = new LinkedList<>();

		// Maximize:
		//		(number of item * item work) - distance
		double maxScore = -Double.POSITIVE_INFINITY;
		ICachedContainer bestContainer = null;
		for (ICachedContainer c : containerItemMap.get(item)) {
			double score = c.getScore(playerPos, item, maxNeeded);

			//Logger.debug(this, "Found chest with item at " + c.getPosition() + " with score: " + score);

			if (score > maxScore) {
				if (!containerExistsAt(c.getPosition())) {
					toRemove.add(c.getPosition());
					continue;
				}
				maxScore = score;
				bestContainer = c;
			}
		}
		for(BlockPos pos : toRemove) {
			removeContainer(pos);
		}
		return bestContainer;
	}

	// TODO: Same thing for furnaces? blehhhh
	public void onChestModified(Container container, BlockPos pos) {
		if (!containerPositionMap.containsKey(pos)) {
			Logger.debug(this, "Opened new chest at " + pos);
			// New container there
			containerPositionMap.put(pos, new CachedChest(pos));
		}
		ICachedContainer c = containerPositionMap.get(pos);
		// Compile all items
		Set<String> hadItems = new HashSet<String>();
		for (CachedItems slot : c.getSlots()) {
			hadItems.add(slot.getItemName());
		}
		// Clear and add new items
		c.resetCache();

		boolean bigChest = (container.inventorySlots.size() == 90);
		int startSlot = 0,
			endSlot = bigChest? 53 : 26;

		for (int slot = startSlot; slot <= endSlot; ++slot) {
			ItemStack stack = container.getSlot(slot).getStack();
			if (stack.getCount() == 0) {
				// Ignore air/empty.
				continue;
			}
			//Logger.log("     STACK: " + ItemUtil.getItemId(stack.getItem()) + ", " + stack.getCount());
			c.addItem(stack);
			String id = ItemUtil.getItemId(stack.getItem());
			if (!containerItemMap.containsKey(id)) {
				Logger.debug(this, "New item observed, adding chest to item map.");
				containerItemMap.put(id, new HashSet<ICachedContainer>());
				containerItemMap.get(id).add(c);
			} else {
				if (!containerItemMap.get(id).contains(c)) {
					Logger.debug(this, "New chest observed for item, adding chest to item map.");
					containerItemMap.get(id).add(c);
				}
			}
		}
		// Find what was removed and update
		for (String name : hadItems) {
			if (c.itemCount(name) <= 0) {
				// This item was removed.
				Logger.log(this, "Item removed detected: " + name);
				if (containerItemMap.containsKey(name)) {
					containerItemMap.get(name).remove(c);
				}
			}
		}
	}

	public void cleanup() {
		// Check all container positions. If the chunk is loaded and the block is not a chest, unload it.
		Set<BlockPos> cset = containerPositionMap.keySet();
		HashSet<BlockPos> copyset = new HashSet<BlockPos>(cset);
		for (BlockPos pos : copyset) {
			if (!containerExistsAt(pos)) {
				removeContainer(pos);
			}
		}
	}

	public void postSerialize() {
		// Encode our hash maps and stuff for ease of use
		containerPositionMap.clear();
		containerItemMap.clear();
		for (ICachedContainer container : containers) {
			containerPositionMap.put(container.getPosition(), container);
			Set<String> included = new HashSet<>();
			for (CachedItems slot : container.getSlots()) {
				String name = slot.getItemName();
				if (included.contains(name)) continue;
				if (!containerItemMap.containsKey(name)) {
					containerItemMap.put(name, new HashSet<ICachedContainer>());
				}
				containerItemMap.get(name).add(container);
			}
		}
	}
	
	private void removeContainer(BlockPos pos) {
		if (containerPositionMap.containsKey(pos)) {
			ICachedContainer c = containerPositionMap.get(pos);
			containerPositionMap.remove(pos);

			for (CachedItems slot : c.getSlots()) {
				if (containerItemMap.containsKey(slot.getItemName())) {
					containerItemMap.get(slot.getItemName()).remove(c);
				}
			}
			containers.remove(c);
		}
	}

	private boolean containerExistsAt(BlockPos pos) {
		try {
			Chunk chunk = Minecraft.getMinecraft().world.getChunk(pos); 
			if (chunk == null || !chunk.isLoaded()) {
				// It's not in the chunk, we assume it's true.
				return containerPositionMap.containsKey(pos);
			}
			IBlockState bs = Minecraft.getMinecraft().world.getBlockState(pos);
			// TODO: Also check for Furnace...
			return Block.getIdFromBlock(bs.getBlock()) == Block.getIdFromBlock(ItemUtil.getBlock("chest"));
		} catch (NullPointerException e) {
			return containerPositionMap.containsKey(pos);
		}
	}

}
