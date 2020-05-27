package automc.containers;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Represents the interface for how our bot "remembers" a container.
 * This should be serializable when you implement it.
 *
 */

public interface ICachedContainer {

	public int itemCount(String Item);

	public long getTimeLastOpenedMillis();
	public long getTimeSinceLastOpenedMillis();

	public Iterable<CachedItems> getSlots();

	public BlockPos getPosition();
	public double getScore(Vec3d playerPos, Item item, int maxNeeded);

	public void resetCache();
	public void addItem(ItemStack stack);

}
