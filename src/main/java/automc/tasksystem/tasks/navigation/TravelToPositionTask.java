package automc.tasksystem.tasks.navigation;

import automc.AutoMC;
import automc.tasksystem.Task;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.util.math.BlockPos;

public class TravelToPositionTask extends CustomGoalTask {

	private BlockPos target;

	public TravelToPositionTask(BlockPos target) {
		this.target = target;
	}

	@Override
	protected void onInit() {
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(target));
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof TravelToPositionTask) {
			return ((TravelToPositionTask)t).target.equals(target);
		}
		return false;
	}

}
