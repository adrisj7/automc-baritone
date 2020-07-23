package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.ContainerType;
import automc.tasksystem.Task;
import automc.utility.ItemUtil;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;

/**
 * @author adris
 *
 */
public class CraftRecipeInTableTask extends DoStuffInContainerTask {

	IRecipe recipe;

	public CraftRecipeInTableTask(IRecipe recipe) {
		super("crafting_table");
		this.recipe = recipe;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof CraftRecipeInTableTask) {
			CraftRecipeInTableTask crt = (CraftRecipeInTableTask) t;
			return ItemUtil.recipesEqual(recipe, crt.recipe);
		}
		return false;
	}

	@Override
	protected boolean isUIOpened() {
		return AutoMC.getAutoMC().player.inventory.isCraftingTableOpened();
	}

	@Override
	protected Task getUISubTask(BlockPos containerPos) {
		if (AutoMC.getAutoMC().player.crafter.craft(ContainerType.CRAFTING, recipe)) {
			Logger.log("CRAFTED Recipe");
		}
		return null;
	}

	@Override
	protected boolean areConditionsMet() {
		return AutoMC.getAutoMC().player.crafter.canCraft(recipe);
	}

	@Override
	public boolean isDone() {
		// We quit manually
		return false;
	}

	@Override
	protected Task getPrerequisiteTask() {
		// No prereqs. If we have materials and get to our crafting table, we're good to go.
		return null;
	}
	
	@Override
	protected void onFinish() {
		super.onFinish();
		AutoMC.getAutoMC().player.closeContainer();
	}

}
