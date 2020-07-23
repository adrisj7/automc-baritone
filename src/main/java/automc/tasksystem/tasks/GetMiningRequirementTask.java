package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;

public class GetMiningRequirementTask extends TaskGoal {

	MiningRequirement requirement;

	public GetMiningRequirementTask(MiningRequirement requirement) {
		this.requirement = requirement;
	}

	@Override
	protected Task getSubTask() {
		switch (requirement) {
		case WOOD:
			return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("wooden_pickaxe", 1);
		case STONE:
			return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("stone_pickaxe", 1);
		case IRON:
			return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("iron_pickaxe", 1);
		case DIAMOND:
			return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("diamond_pickaxe", 1);
		case HAND:
			stop();
			return null;
		default:
			break;
		}
		log("Weird requirement: " + requirement);
		return null;
	}

	@Override
	protected void onGoalInit() {
		
	}

	@Override
	public boolean isDone() {
		return AutoMC.getAutoMC().player.inventory.miningRequirementMet(requirement);
	}

	@Override
	protected void onGoalFinish() {
		
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof GetMiningRequirementTask) {
			GetMiningRequirementTask mrt = (GetMiningRequirementTask) t;
			return requirement == mrt.requirement;
		}
		return false;
	}

}
