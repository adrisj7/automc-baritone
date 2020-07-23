package automc.tasksystem.tasks.resources;

import automc.tasksystem.Task;
import automc.tasksystem.tasks.ResourceTask;
import net.minecraft.item.Item;

public class PickItemsOffGroundTask extends ResourceTask {

	boolean failed = false;
	
	public PickItemsOffGroundTask(Item item, int requiredAmount) {
		super(item, requiredAmount);
	}
	public PickItemsOffGroundTask(String item, int requiredAmount) {
		super(item, requiredAmount);
	}

	@Override
	protected Task getResourceSubTask() {
		failed = true;
		return null;
	}

	@Override
	protected void onResourceGoalInit() {
		failed = false;
	}

	@Override
	protected void onResourceGoalFinish() {

	}

	@Override
	protected boolean areConditionsMet() {
		return !failed;
	}

}
