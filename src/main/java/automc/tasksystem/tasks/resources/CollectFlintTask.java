package automc.tasksystem.tasks.resources;

import java.util.Optional;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.PlaceBlockNearbyTask;
import automc.tasksystem.tasks.ResourceTask;
import baritone.api.pathing.goals.GoalRunAway;
import baritone.api.process.PathingCommand;
import net.minecraft.client.Minecraft;

public class CollectFlintTask extends ResourceTask {

	private Task miningTask;
	private Task placingTask;

	public CollectFlintTask(int requiredAmount) {
		super("flint", requiredAmount);
	}

	@Override
	protected Task getResourceSubTask() {
		// TODO: If we have gravel in inventory, Get closest gravel. If it's far, break.

		// If we're mining, keep mining unless we have gravel and can place it closer.
		if (miningTask != null && miningTask.isActive()) {
			if (AutoMC.getAutoMC().player.inventory.hasItem("gravel")) {
				Optional<PathingCommand> op = AutoMC.getAutoMC().getBaritone().getPathingControlManager().mostRecentCommand();
				if (op.isPresent()) {
					PathingCommand c = op.get();
					double cost = (c != null && c.goal != null)? c.goal.heuristic(Minecraft.getMinecraft().player.getPosition()) : Double.POSITIVE_INFINITY;
					//Logger.log("COST: " + cost);
					// If we're exploring, we didn't find anything.
					// TODO: Figure this out for real though.
					boolean isExploring = c.goal instanceof GoalRunAway;
					if (isExploring || cost > 30) {
						placingTask = new PlaceBlockNearbyTask("gravel");
						return placingTask;
					}
				}
			}
			return miningTask;
		}
		// If we're placing, wait to place.
		if (placingTask != null && placingTask.isActive()) {
			return placingTask;
		}

		// Now we just mine gravel.
		miningTask = new CollectMineOutputTask("gravel", "flint", requiredAmounts[0], MiningRequirement.HAND);
		return miningTask;
	}

	@Override
	protected void onResourceGoalInit() {
		miningTask = null;
	}

	@Override
	protected void onResourceGoalFinish() {
		
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

}
