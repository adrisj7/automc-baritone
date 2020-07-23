package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.CraftRecipeInHandTask;
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
		int req = requiredAmounts[0];
		return new MineBlockTask(new String[] {"log", "planks"}, new int[] {(int)Math.ceil((double)req/4.0), req}, MiningRequirement.HAND);
	}

	@Override
	protected void onResourceGoalInit() {
		// Nothing.
	}

	@Override
	protected void onResourceGoalFinish() {
		// Nothing.
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

}
