package automc.tasksystem;

import automc.Logger;
import automc.definitions.LoopState;

/**
 * Now THIS is the grand kahooney
 * the meat and potatoes
 * the big boss
 * the booney clooney
 * 
 * This my friends is how the automation works.
 * Goal tasks are split up into sub goals and sub tasks.
 * 
 * When you run a goal, it will run these sub goals and sub tasks.
 * 
 * This will keep happening until the overarching goal is met.
 * 
 * It is up to YOU to determine what sub-goals and sub tasks get added to a goal, and this magic
 * is handled when you implement the "getSubTask" method.
 * 
 * 
 * The cool thing about this is, while data is not stored recursively,
 * these goals are dynamic and recursive and hella easy to program.
 * In fact, me talking about it is probably distracting from how damn simple
 * this all is.
 */

public abstract class TaskGoal extends Task {

	private Task currentSubTask;

	@Override
	protected void onInit() {
		onGoalInit();
		currentSubTask = null;
	}

	@Override
	protected void onTick(LoopState state) {
		Task sub = getSubTask();

		// We have a new task, our current task is interrupted.
		if (!areTasksEqual(currentSubTask, sub)) {
			if (currentSubTask != null) {
				log("Sub interrupted: " + currentSubTask + " in place of new: " + sub);
				currentSubTask.interrupt();
			} else {
				if (sub != null) {
					log("New sub: " + sub);
				}
			}
			currentSubTask = sub;

			if (currentSubTask != null) {
				// This sub task is one layer deeper than us.
				currentSubTask.depth = depth + 1;
			}
		}

		// If the tasks are deemed the same, they are NOT changed.
		// This makes sure that "init" is __only__ called when there is a NEW task.

		// Update the current task.
		if (currentSubTask != null) {
			if (currentSubTask.runUpdate(state)) {
				log("Sub finished: " + currentSubTask);
				// We updated and FINISHED the current sub task.
				//currentSubTask.onFinish();
				//currentSubTask = null;
			}
	
			// The current sub task failed to meet its conditions.
			if (currentSubTask != null && !currentSubTask.areConditionsMet()) {
				log("Sub finished: (bad condition): " + currentSubTask);
				currentSubTask.interrupt();
				currentSubTask = null;
			}
		}
	}

	@Override
	protected void onInterrupt() {
		// We EXPECT our goal task to be interrupted all the time as other goals interrupt this one (Usually).
		if (currentSubTask != null) {
			log("Sub interrupted/interrupted: " + currentSubTask);
			currentSubTask.interrupt();
			currentSubTask = null;
		}
		stop();
		onFinish();
	}

	@Override
	protected void onFinish() {
		if (currentSubTask != null) {
			currentSubTask.interrupt();
			log("Sub interrupted/finished: " + currentSubTask);
			currentSubTask = null;
		}
		onGoalFinish();
	}

	private boolean areTasksEqual(Task t1, Task t2) {
		if (t1 == null && t2 == null) return true;
		if (t1 == null && t2 != null) return false;
		if (t1 != null && t2 == null) return false;
		return t1.equals(t2);
	}

	/// Up to you to implement the rest, and they are enforced so you're reminded to at least consider them.

	protected abstract Task getSubTask();

	protected abstract void onGoalInit();
	protected abstract void onGoalFinish();
	
	@Override
	public
	abstract boolean isDone();
	// Has the same outcome as onFinish, but it's nice to split up the methods so the implementations are easier to read.
	@Override
	protected abstract boolean areConditionsMet();


	public void printTaskChain() {
		Logger.log(this);
		Task t = currentSubTask;
		String spaces = "";
		while (t != null) {
			spaces += "  ";
			
			Logger.log(spaces + t);
			if (t instanceof TaskGoal) {
				TaskGoal tg = (TaskGoal) t;
				t = tg.currentSubTask;
			} else {
				t = null;
			}
		}
	}
}
