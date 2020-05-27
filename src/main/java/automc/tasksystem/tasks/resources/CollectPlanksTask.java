package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.CraftRecipeInHandTask;
import automc.tasksystem.tasks.MineBlockTask;
import automc.tasksystem.tasks.ResourceTask;
import automc.utility.ItemUtil;
import net.minecraft.item.crafting.IRecipe;

public class CollectPlanksTask extends ResourceTask {

	static IRecipe logRecipe = ItemUtil.createPureShapelessRecipe("log");

	public CollectPlanksTask(int requiredAmount) {
		super("planks", requiredAmount);
	}

	@Override
	protected Task getResourceSubTask() {
		// We can craft planks
		if (AutoMC.getAutoMC().player.crafter.canCraft(logRecipe)) {
			return new CraftRecipeInHandTask(logRecipe);
		}
		// We need to punch trees or wood
		return new MineBlockTask(new String[] {"log", "planks"}, new int[] {1, 4}, MiningRequirement.HAND);
	}

	@Override
	protected void onGoalInit() {
		// Nothing.
	}

	@Override
	protected void onFinish() {
		// Nothing.
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

}
