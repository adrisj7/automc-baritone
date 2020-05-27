package automc.tasksystem;

import automc.Logger;
import automc.Runner;
import automc.definitions.LoopState;

public class TaskRunner extends Runner {

	private Task currentTask = null;

	/**
	 * Tell the runner to run this task.
	 * Tasks currently running will be interrupted.
	 * 
	 * This should only really be called to run the BIG main task.
	 * Sub task running will be handled within themselves, recursively and not up here. 
	 * @param task
	 */
	public void runTask(Task task) {
		if (!isRunning()) {
			start();
		}
		if (currentTask != null) {
			currentTask.interrupt();
		}
		currentTask = task;
		currentTask.reset();
		Logger.debug(this, "STARTING TASK: " + currentTask.toString());
	}

	@Override
	protected void onStart() {
		currentTask = null;
	}

	@Override
	protected void onTick(LoopState loopState) {
		if (currentTask != null) {

			// If true, the task completed itself.
			if (currentTask.runUpdate(loopState)) {
				Logger.debug(this, "TASK FINISHED: " + currentTask.toString());
				currentTask = null;
			} else {

				// If the conditions of this task aren't met, just stop it.
				if (!currentTask.areConditionsMet()) {
					currentTask.stop();
					Logger.debug(this, "CONDITION NOT MET for MAIN task: " + currentTask.toString() + ". Stopping that task.");
				}
			}

		}
	}

	@Override
	protected void onStop() {
		Logger.debug(this, "FINISHED!");
		if (currentTask != null) {
			Logger.debug(this, "onStop Task Interrupted: " + currentTask.toString());
			currentTask.interrupt();
			currentTask = null;
		}
	}

	@Override
	public String toString() {
		return "Task Runner";
	}
}
