package automc.tasksystem.tasks.navigation;

import automc.AutoMC;
import automc.definitions.LoopState;
import automc.tasksystem.Task;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class RunAwayTask extends CustomGoalTask {

	private float distance;

	private BlockPos startPos;
	private boolean maintainY;

	public RunAwayTask(float distance, boolean maintainY) {
		this.distance = distance;
		this.maintainY = maintainY;
	}

	@Override
	protected void onInit() {
		startPos = Minecraft.getMinecraft().player.getPosition();
		GoalRunAway g;
		if (maintainY) {
			g = new GoalRunAway(distance, startPos.getY(), startPos);
		} else {
			g = new GoalRunAway(distance, startPos);
		}
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoalAndPath(g);
	}

	@Override
	protected void onTick(LoopState state) {
		if (!AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().isActive()) {
			stop();
		}
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof RunAwayTask) {
			RunAwayTask runawaayyy = (RunAwayTask) t;
			return runawaayyy.startPos.equals(startPos) && runawaayyy.maintainY == maintainY && Math.abs(runawaayyy.distance - distance) < 1f;
		}
		return false;
	}
}
