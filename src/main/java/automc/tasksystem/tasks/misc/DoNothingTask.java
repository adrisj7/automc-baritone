package automc.tasksystem.tasks.misc;

import automc.definitions.LoopState;
import automc.tasksystem.Task;

/**
 * Just does nothing.
 * 
 * Used to test background systems while giving the player control.
 *
 */
public class DoNothingTask extends Task {

	@Override
	protected void onInit() {
	}

	@Override
	protected void onTick(LoopState state) {
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	protected void onFinish() {
	}

	@Override
	protected void onInterrupt() {		
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		return t instanceof DoNothingTask;
	}

}
