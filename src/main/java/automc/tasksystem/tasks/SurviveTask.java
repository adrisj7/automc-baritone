package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.tasksystem.tasks.resources.CollectFoodTask;

public class SurviveTask extends TaskGoal {

	private Task mainTask;
	private CollectFoodTask foodTask;

	private Task exploreTask;
	private int exploreTaskTimer = 0;
	protected int exploreTaskCheckTimeout = 500;

	private int exploringTimer = 0;
	private int exploringTimeout = 200;

	public SurviveTask(Task mainTask) {
		this.mainTask = mainTask;
		foodTask = new CollectFoodTask(20);
	}

	@Override
	protected Task getSubTask() {

		if (AutoMC.getAutoMC().getBaritone().getExploreProcess().isActive()) {
			++exploringTimer;
		}
		
		// If we're exploring, check for another task
		if (exploreTask != null && exploreTask.isActive()) {
			if (exploreTaskTimer > exploreTaskCheckTimeout) {
				exploreTaskTimer = 0;
				exploringTimer = 0;
				exploreTask.stop();
			} else {
				++exploreTaskTimer;
				return exploreTask;
			}
		} else if (exploringTimer > exploringTimeout) {
			exploreTask = toDoInsteadOfExploring();
			if (exploreTask != null) {
				return exploreTask;
			}
		}

		if (canSearchForFood()) {
			// Check if the food task is exploring. If we're exploring and we don't need food, stop it. 
			if (foodTask.needsFood() || foodTask.isActive()) {
				if (!foodTask.needsFood() && foodTask.isActive() && foodTask.isExploring()) {
					// If we don't really need the food and we're searching for it, stop.
					foodTask.stop();
				} else {
					//Logger.log("??? " + AutoMC.getAutoMC().player.inventory.getTotalFoodHungerHealAmount());
					return foodTask;
				}
			}
		}

		return mainTask;
	}

	@Override
	protected void onGoalInit() {
		exploreTask = null;
		exploreTaskTimer = 0;
		exploringTimer = 0;
	}

	@Override
	protected void onGoalFinish() {
		// We've finished our main task.
	}

	@Override
	public boolean isDone() {
		// We stop when our main task stops.
		return mainTask.isDone();
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof SurviveTask) {
			SurviveTask st = (SurviveTask)t;
			return (st.mainTask.equals(mainTask));
		}
		return false;
	}

	/// OVERRIDE THE FOLLOWING METHODS FOR ANARCHY

	// At our current stage, can we search for food?
	protected boolean canSearchForFood() {
		return AutoMC.getAutoMC().player.isInOverworld();
	}
	protected Task toDoInsteadOfExploring() {
		return null;
	}

}
