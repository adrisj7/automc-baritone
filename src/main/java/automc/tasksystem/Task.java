package automc.tasksystem;

import automc.Logger;
import automc.definitions.LoopState;

/**
 * 	This stores a single task. Only one of these will be running at a time.
 *
 */
public abstract class Task {

	private boolean hasInitedYet;
	private boolean quitFlag = false;

	// How far we are in the "command" chain.
	// Useful for visualizing the command "tree" (it's not really a tree) during debugging.
	// By default we're at depth zero. If we were called from a parent task our depth will be adjusted.
	protected int depth = 0;


	public Task() {
		reset();
	}

	public void reset() {
		hasInitedYet = false;
		quitFlag = false;
	}

	// Only run this if you're sure it's supposed to run.
	// If not, you will keep re-initializing this task.
	public boolean runUpdate(LoopState state) {
		if (!hasInitedYet) {
			hasInitedYet = true;
			onInit();
		}
		if (isDone() || quitFlag) {
			onFinish();
			reset();
			return true;
		}
		onTick(state);
		return false;
	}

	// Interrupts the task
	public void interrupt() {
		onInterrupt();
	}

	protected abstract void onInit();
	protected abstract void onTick(LoopState state);
	protected abstract boolean isDone();
	protected abstract void onFinish();

	/**
	 * Called when another task stops this one.
	 */
	protected abstract void onInterrupt();

	// Are conditions met to be DOING this task?
	// If false, we need to reevaluate what we CAN do and walk back up the task tree/queue/list.
	protected abstract boolean areConditionsMet();

	// This MUST be defined so that the goal system can properly function.
	// Goals create new task objects repeatedly, and it has to KNOW if the new object has the same task.
	protected abstract boolean areEqual(Task t);

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Task)) return false;
		return areEqual((Task)obj);
	}

	public void stop() {
		quitFlag = true;
	}

	protected void log(Object message) {
		String taskName = "[" + toString() + "]: ";
		String padding = "";
		for(int i = 0; i < depth; ++i) {
			padding += "  ";
		}
		Logger.debug(padding + taskName + message);
	}
	
	@Override
	public String toString() {
		String str = this.getClass().getSimpleName();
		if (str != "") {
			return str;
		}
		return super.toString();
	}

}
