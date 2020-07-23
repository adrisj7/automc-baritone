package automc.tasksystem.tasks.misc;

import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;

public class SleepInBedTask extends TaskGoal {

	@Override
	protected Task getSubTask() {
		return null;
	}

	@Override
	protected void onGoalInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onGoalFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean areEqual(Task t) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
