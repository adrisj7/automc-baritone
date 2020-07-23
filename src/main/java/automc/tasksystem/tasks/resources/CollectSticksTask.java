package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.CraftRecipeInHandTask;
import automc.tasksystem.tasks.ResourceTask;
import automc.utility.ItemUtil;
import net.minecraft.item.crafting.IRecipe;

// This is already handled by CollectRecipeAndCraftTask.
@Deprecated()
public class CollectSticksTask extends ResourceTask {

	static IRecipe stickRecipe = ItemUtil.createPureShapedRecipe(1, 2, "planks", "planks");

	public CollectSticksTask(int requiredAmount) {
		super("stick", requiredAmount);
	}

	@Override
	protected Task getResourceSubTask() {
		// We can craft some sticks
		if (AutoMC.getAutoMC().player.crafter.canCraft(stickRecipe)) {
			return new CraftRecipeInHandTask(stickRecipe);
		}

		int neededPlanks = requiredAmounts[0] / 2;
		if (neededPlanks % 2 != 0) neededPlanks += 1;
		return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("planks", neededPlanks);//new CollectPlanksTask(neededPlanks);
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
