package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.containers.ICachedContainer;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;

/**
 * A task to collect an item.
 */
public abstract class ResourceTask extends TaskGoal {

	protected Item[] targetItems;
	protected int[] requiredAmounts;

	public ResourceTask(Item[] targetItems, int[] requiredAmounts) {
		// TODO: Assert equal size
		this.targetItems = targetItems;
		this.requiredAmounts = requiredAmounts;
	}
	public ResourceTask(String[] targetItems, int[] requiredAmounts) {
		// TODO: call original constructor
		this.targetItems = new Item[targetItems.length];
		for(int i = 0; i < targetItems.length; ++i) {
			this.targetItems[i] = ItemUtil.getItem(targetItems[i]);
		}
		this.requiredAmounts = requiredAmounts;
	}
	public ResourceTask(Item item, int requiredAmount) {
		this(new Item[] {item}, new int[] {requiredAmount});
	}
	public ResourceTask(String item, int requiredAmount) {
		this(new String[] {item}, new int[] {requiredAmount});
	}

	@Override
	protected Task getSubTask() {

		// First try to grab the item from a chest.
		for (int i = 0; i < targetItems.length; ++i) {
			ICachedContainer c = AutoMC.getAutoMC().containerHandler.getBestContainerWith(targetItems[i], requiredAmounts[i]);
			Vec3d playerPos = Minecraft.getMinecraft().player.getPositionVector();
			if (c != null && c.getScore(playerPos, targetItems[i], requiredAmounts[i]) > 0) {
				//Logger.log("VALID CHEST: " + c.getPosition());
				return new GrabItemsFromChestCommand(targetItems[i], requiredAmounts[i]);
			}
		}

		return getResourceSubTask();
	}

	// We need to override this, since sometimes we will gather resources in another way.
	protected abstract Task getResourceSubTask();

	@Override
	protected abstract void onGoalInit();

	@Override
	protected boolean isDone() {
		for (int i = 0; i < targetItems.length; ++i) {
			if (AutoMC.getAutoMC().player.inventory.getItemCount(targetItems[i]) >= requiredAmounts[i]) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected abstract void onFinish();

	@Override
	protected abstract boolean areConditionsMet();

	@Override
	protected boolean areEqual(Task t) {
		if (t instanceof ResourceTask) {
			ResourceTask rt = (ResourceTask)t;
			if (targetItems.length != rt.targetItems.length) {
				return false;
			}
			for (int i = 0; i < targetItems.length; ++i) {
				if (requiredAmounts[i] != rt.requiredAmounts[i]) {
					return false;
				}
				if (!targetItems[i].equals(rt.targetItems[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return super.toString(); // TODO: Item name?? 
	}
}
