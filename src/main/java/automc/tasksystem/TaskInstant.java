package automc.tasksystem;

import automc.definitions.LoopState;

public abstract class TaskInstant extends Task {

	@Override
	protected abstract void onInit();

	@Override
	protected void onTick(LoopState state) {
		// Do nothing
	}

	@Override
	protected boolean isDone() {
		return true;
	}

	@Override
	protected void onFinish() {
		// Do nothing
	}

	@Override
	protected void onInterrupt() {
		// Do nothing
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}
	
}
