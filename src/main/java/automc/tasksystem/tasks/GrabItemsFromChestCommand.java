package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.Logger;
import automc.containers.ICachedContainer;
import automc.tasksystem.Task;
import automc.utility.ItemUtil;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.BlockOptionalMeta;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class GrabItemsFromChestCommand extends ResourceTask {

	ICachedContainer targetContainer = null;
	
	public GrabItemsFromChestCommand(Item item, int requiredAmount) {
		super(item, requiredAmount);
		Logger.log("Grabbing: " + ItemUtil.getItemId(item) + ", " + requiredAmount);
	}
	public GrabItemsFromChestCommand(String item, int requiredAmount) {
		super(item, requiredAmount);
	}

	@Override
	protected Task getSubTask() {
		if (!AutoMC.getAutoMC().isInGame()) return null;
		if (targetContainer == null) return null;
		Vec3d pos = Minecraft.getMinecraft().player.getPositionVector();
		Vec3i cpos = targetContainer.getPosition();
		double distanceSqr = pos.squareDistanceTo(cpos.getX(), cpos.getY(), cpos.getZ());
		double thresh = 2;
		if (distanceSqr < thresh*thresh) {
			// If we're still trying to break the block
			if (AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().isActive()) {
				Logger.debug(this, "We're close enough, now switching tasks to get to nearest chest.");
				AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().onLostControl();
				AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().getToBlock(new BlockOptionalMeta(ItemUtil.getBlock("chest")));
			}

			// If chest is open
			if (AutoMC.getAutoMC().player.inventory.isChestOpened()) {
				int requiredLeft = requiredAmounts[0] - AutoMC.getAutoMC().player.inventory.getItemCount(targetItems[0]);
				AutoMC.getAutoMC().player.inventory.grabItemFromOpenChest(targetItems[0], requiredLeft);
				this.stop();
			}
		}
		return null;
	}

	// We want to override default behavior: If we don't it will try to run a chest command. It will infinitely recurse.
	@Override
	protected Task getResourceSubTask() {
		return null;
	}

	@Override
	protected void onResourceGoalInit() {
		// Maximize based on how many items we NEED LEFT, not based on how many we NEED TOTAL.
		int remaining = requiredAmounts[0] - AutoMC.getAutoMC().player.inventory.getItemCount(targetItems[0]); 
		targetContainer = AutoMC.getAutoMC().containerHandler.getBestContainerWith(targetItems[0], remaining);
		if (targetContainer != null) {
			if (targetContainer.getScore(Minecraft.getMinecraft().player.getPositionVector(), targetItems[0], requiredAmounts[0]) < 0) {
				targetContainer = null;
			} else {
				log("Found chest at " + targetContainer.getPosition() + ", approaching.");
				AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoal(new GoalBlock(targetContainer.getPosition()));
				AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().path();
			}
		}
		
		if (targetContainer == null) {
			log("No chest found. Will cancel go to chest command.");
		}
	}

	@Override
	protected void onResourceGoalFinish() {
		// TODO: AutoMC.getAutoMC().player.closeContainer();
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().onLostControl();
		AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().onLostControl();
		AutoMC.getAutoMC().player.closeContainer();
		Logger.log("porque?");
	}

	@Override
	protected boolean areConditionsMet() {
		// TODO: If chest doesnt have da items it no good
		return AutoMC.getAutoMC().isInGame() && targetContainer != null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GrabItemsFromChestCommand) {
			return super.equals(obj);
		}
		return false;
	}

}
