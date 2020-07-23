package automc.tasksystem.tasks;

import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;

@Deprecated
public class PickupNearestItem extends TaskGoal {

	String item;

	public PickupNearestItem(String item) {
		this.item = item;
	}

	@Override
	protected Task getSubTask() {
		// TODO Auto-generated method stub
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
