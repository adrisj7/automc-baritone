package automc.tasksystem.tasks.navigation;

import automc.AutoMC;
import automc.definitions.LoopState;
import automc.tasksystem.Task;

public abstract class CustomGoalTask extends Task {

	@Override
	protected abstract void onInit();

	@Override
	public abstract boolean areEqual(Task t);

	@Override
	protected void onTick(LoopState state) {
		if (!AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().isActive()) {
			stop();
		}
	}

	@Override
	public boolean isDone() {
		return false; // Manual
	}

	@Override
	protected void onFinish() {
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().onLostControl();
	}

	@Override
	protected void onInterrupt() {
		onFinish();
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

}
