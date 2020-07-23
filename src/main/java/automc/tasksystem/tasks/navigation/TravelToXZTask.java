package automc.tasksystem.tasks.navigation;


import automc.AutoMC;
import automc.tasksystem.Task;
import baritone.api.pathing.goals.GoalXZ;

public class TravelToXZTask extends CustomGoalTask {

	private int targetX;
	private int targetZ;

	public TravelToXZTask(int targetX, int targetZ) {
		this.targetX = targetX;
		this.targetZ = targetZ;
	}

	@Override
	protected void onInit() {
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalXZ(targetX, targetZ));
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof TravelToXZTask) {
			TravelToXZTask ttpt = (TravelToXZTask) t;
			return ttpt.targetX == targetX;
		}
		return false;
	}

}
