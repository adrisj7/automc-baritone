package automc.tasksystem.tasks.misc;

import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.tasksystem.tasks.building.BuildNetherPortalTask;

/**
 * This fricking plays minecraft
 * @author adris
 *
 */
public class PlayMinecraftTask extends TaskGoal {

	private GetStackedTask getStackedTask;

	public PlayMinecraftTask() {
		getStackedTask = new GetStackedTask();
	}

	@Override
	protected Task getSubTask() {
		// 1: Get stacked
		if (!getStackedTask.isStacked()) {
			return getStackedTask;
		}
		// 2: Nether portal
		return new BuildNetherPortalTask();
	}

	@Override
	protected void onGoalInit() {
		
	}

	@Override
	protected void onGoalFinish() {
		
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof PlayMinecraftTask) {
			return true;
		}
		return false;
	}
}
