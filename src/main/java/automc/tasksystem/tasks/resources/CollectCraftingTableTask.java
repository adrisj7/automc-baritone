package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.definitions.ContainerType;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.ResourceTask;
import automc.utility.ItemUtil;
import net.minecraft.item.crafting.IRecipe;

@Deprecated
public class CollectCraftingTableTask extends ResourceTask {

	static IRecipe tableRecipe = ItemUtil.createPureShapedRecipe(2,2,"planks", "planks", "planks", "planks");

	public CollectCraftingTableTask(int requiredAmount) {
		super("crafting_table", requiredAmount);
	}

	@Override
	protected Task getResourceSubTask() {
		// We have materials, craft.
		if (AutoMC.getAutoMC().player.crafter.canCraft(tableRecipe)) {
			AutoMC.getAutoMC().player.crafter.craft(ContainerType.PLAYER, tableRecipe);
			return null;
		}
		// Otherwise, collect 4 planks per table.
		return new CollectPlanksTask(4 * requiredAmounts[0]);
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
