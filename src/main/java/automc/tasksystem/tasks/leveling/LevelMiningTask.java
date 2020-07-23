package automc.tasksystem.tasks.leveling;

import automc.tasksystem.Task;
import automc.tasksystem.tasks.LevelTask;
import automc.tasksystem.tasks.resources.MineBlockTask;

public class LevelMiningTask extends LevelTask {
	
	private MineBlockTask mineTask;
	
	public LevelMiningTask(int targetLevel, MineBlockTask mineTask) {
		super(targetLevel);
		this.mineTask = mineTask;
	}

	@Override
	protected Task getSubTask() {
		return mineTask;
	}

	@Override
	protected void onGoalInit() {
		// Do nothing
	}

	@Override
	protected void onGoalFinish() {
		// Do nothing
	}

	@Override
	protected boolean areConditionsMet() {
		return true; // mine task will gather.
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof LevelMiningTask) {
			return ((LevelMiningTask) t).mineTask.areEqual(mineTask);
		}
		return false;
	}

}
