package automc.tasksystem.tasks;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import automc.AutoMC;
import automc.Logger;
import automc.containers.ICachedContainer;
import automc.player.EntityScanner;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ItemUtil;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.process.PathingCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A task to collect an item.
 */
public abstract class ResourceTask extends TaskGoal {

	protected String[] targetItems;
	protected int[] requiredAmounts;

	private String pickingUp = null; // The item we're trying to pick up.
	private int pickingUpTarget = 0;
	private int pickingUpCycle = 0;

	private Set<BlockPos> blacklistedPickupTargets;
	private int blackListTimer = 0;
	private static final int BLACKLIST_ADD_TIMER = 500;

	public ResourceTask(Item[] targetItems, int[] requiredAmounts) {
		// Assert equal size
		if (targetItems.length != requiredAmounts.length) {
			Logger.logError("For any ResourceTask you MUST give the same number of items as requirements. This will lead to an ArrayIndexOutOfBoundsException.");
		}

		this.targetItems = new String[targetItems.length];
		for(int i = 0; i < targetItems.length; ++i) {
			this.targetItems[i] = ItemUtil.getItemId(targetItems[i]);
		}
;
		this.requiredAmounts = requiredAmounts;
		blacklistedPickupTargets = new HashSet<BlockPos>();
	}
	public ResourceTask(String[] targetItems, int[] requiredAmounts) {
		// Assert equal size
		if (targetItems.length != requiredAmounts.length) {
			Logger.logError("For any ResourceTask you MUST give the same number of items as requirements. This will lead to an ArrayIndexOutOfBoundsException.");
		}
		this.targetItems = targetItems;
		this.requiredAmounts = requiredAmounts;
		blacklistedPickupTargets = new HashSet<BlockPos>();
	}
	public ResourceTask(Item item, int requiredAmount) {
		this(new Item[] {item}, new int[] {requiredAmount});
	}
	public ResourceTask(String item, int requiredAmount) {
		this(new String[] {item}, new int[] {requiredAmount});
	}

	@Override
	protected Task getSubTask() {
		
		// TODO: Standardize to all task goals unless we give some kind of override.
		if (!AutoMC.getAutoMC().isInGame()) return null;

		if (pickingUp == null) {
			pickingUp = targetItems[pickingUpCycle];
		}

		// Try to pick up item first.
		EntityScanner entities = AutoMC.getAutoMC().entityScanner;
		if (entities.itemExists(pickingUp)) {
			EntityItem closest = entities.getClosestDroppedItem(pickingUp, blacklistedPickupTargets);
			if (closest != null) {
				if (AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().isActive()) {
					// We're still trying to pick stuff up. Make sure it's worth it.
					if (!isItemEntityCloseEnough(closest)) {
						// Too expensive to get item.
						pickingUp = null;
						blacklistedPickupTargets.add(closest.getPosition());
						++pickingUpCycle;
						if (pickingUpCycle >= targetItems.length) {
							pickingUpCycle = 0;
						}
					} else {
						// We are close enough to the item to collect it.
						AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoal(new GoalBlock(closest.getPosition()));
						onResourceFoundAnotherWay();

						++blackListTimer;
						if (blackListTimer > BLACKLIST_ADD_TIMER) {
							Logger.log(this, "Failed to get resource for some reason, blacklisting.");
							blacklistedPickupTargets.add(closest.getPosition());
							blackListTimer = 0;
						}

						return null;
					}
				} else {
					// We need to start picking up the item.
					AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(closest.getPosition()));
					log("PICKING UP CLOSEST VALID ITEM");
					onResourceFoundAnotherWay();
					return null;
				}
			}
		} else {
			pickingUp = null;
		}


		// First try to grab the item from a chest.
		for (int i = 0; i < targetItems.length; ++i) {
			ICachedContainer c = AutoMC.getAutoMC().containerHandler.getBestContainerWith(targetItems[i], requiredAmounts[i]);
			Vec3d playerPos = Minecraft.getMinecraft().player.getPositionVector();
			if (c != null && c.getScore(playerPos, targetItems[i], requiredAmounts[i]) > 0) {
				//Logger.log("VALID CHEST: " + c.getPosition());
				onResourceFoundAnotherWay();
				return new GrabItemsFromChestCommand(targetItems[i], requiredAmounts[i]);
			}
		}

		return getResourceSubTask();
	}

	// We need to override this, since sometimes we will gather resources in another way.
	protected abstract Task getResourceSubTask();
	protected abstract void onResourceGoalInit();
	protected abstract void onResourceGoalFinish();

	@Override
	protected void onGoalInit() {
		pickingUp = null;
		pickingUpTarget = 0;
		pickingUpCycle = 0;
		blacklistedPickupTargets.clear();
		onResourceGoalInit();
	}

	@Override
	public boolean isDone() {
		for (int i = 0; i < targetItems.length; ++i) {
			if (AutoMC.getAutoMC().player.inventory.getItemCount(targetItems[i]) >= requiredAmounts[i]) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onGoalFinish() {
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().onLostControl();
		onResourceGoalFinish();
	}

	@Override
	protected abstract boolean areConditionsMet();

	@Override
	public boolean areEqual(Task t) {
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
	
	private boolean isItemEntityCloseEnough(EntityItem item) {
		ItemStack stack = item.getItem();
		int count = stack.getCount();
		if (count > pickingUpTarget) count = pickingUpTarget;
		double maxCost = 1.1 * AutoMC.getAutoMC().itemWorkDictionary.getWorkTicks(stack.getItem()) * count;

		// Get cost of recent command
		Optional<PathingCommand> op = AutoMC.getAutoMC().getBaritone().getPathingControlManager().mostRecentCommand();
		if (op.isPresent() && op.get() != null) {
			PathingCommand c = op.get();
			Goal g = c.goal;
			double cost = g.heuristic(item.getPosition());
			if (cost > maxCost) {
				// Too expensive.
				return false;
			}
		}
		return true;
	}

	// When we don't revert to our "sub" task and grab item from a chest or the ground, call this.
	protected void onResourceFoundAnotherWay() {
		
	}
}
