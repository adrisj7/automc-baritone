package automc.tasksystem;

import automc.AutoMC;
import automc.Logger;
import automc.Runner;
import automc.combat.CombatRunner;
import automc.definitions.LoopState;
import automc.survival.SurvivalRunner;

public class TaskRunner extends Runner {

	private Task currentTask = null;

	private boolean paused = false;

	private long commandStartTime = 0;
	
	@Override
	public void start() {
		super.start();
		// TODO: This is bad design, crossed dependencies.
		// 		Thankfully this bad design doesn't interact with anything extra but
		//		FIX THIS if you plan on adding more runners.
		AutoMC.getAutoMC().combatRunner.start();
		AutoMC.getAutoMC().survivalRunner.start();
		paused = false;
	}

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
		Logger.log(this, "STARTING TASK: " + currentTask.toString());
		commandStartTime = System.currentTimeMillis();
	}

	@Override
	protected void onStart() {
		currentTask = null;
		paused = false;
	}

	@Override
	protected void onTick(LoopState loopState) {
		if (currentTask != null) {
			// If we pause, pause and interrupt our task. Otherwise, unpause and resume our task.
			if (shouldPause()) {
				paused = true;
				// TODO: Make sure baritone doesn't override our player look
				if (currentTask.isActive()) {
					currentTask.pause();
				}
				return;
			} else {
				if (paused) {
					if (currentTask != null && !currentTask.isActive()) {
						currentTask.unpause();
					}
					paused = false;
				}
			}


			// If true, the task completed itself.
			if (currentTask.runUpdate(loopState)) {
				Logger.log(this, "TASK FINISHED: " + currentTask.toString());
				long deltaTime = System.currentTimeMillis() - commandStartTime;
				Logger.log(this, "took " + ((float)(deltaTime) / 1000f) + " seconds." );
				
				currentTask = null;
				// TODO: A bit messy but it stops it.
				AutoMC.getAutoMC().onCancel();
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

	public void printTaskChain() {
		Logger.log("=============TASK CHAIN:===============");
		if (currentTask != null) {
			if (currentTask instanceof TaskGoal) {
				((TaskGoal)currentTask).printTaskChain();
			} else {
				Logger.log(currentTask);
			}
		} else {
			Logger.log("(empty)");
		}
		Logger.log("=======================================");
	}
	
	@Override
	public String toString() {
		return "Task Runner";
	}

	private boolean shouldPause() {
		CombatRunner cr = AutoMC.getAutoMC().combatRunner;
		SurvivalRunner sr = AutoMC.getAutoMC().survivalRunner;
		if (cr.aura.isBlocking() || sr.eater.isEating()) {
			return true;
		}
		return false;
	}
}
