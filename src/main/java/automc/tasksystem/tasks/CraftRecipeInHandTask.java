package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.definitions.ContainerType;
import automc.tasksystem.Task;
import automc.tasksystem.TaskInstant;
import automc.utility.ItemUtil;
import net.minecraft.item.crafting.IRecipe;

public class CraftRecipeInHandTask extends TaskInstant {

	private IRecipe recipe;

	public CraftRecipeInHandTask(IRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	protected void onInit() {
		AutoMC.getAutoMC().player.closeContainer();

		AutoMC.getAutoMC().player.crafter.craft(ContainerType.PLAYER, recipe);
	}

	@Override
	protected boolean areConditionsMet() {
		return AutoMC.getAutoMC().player.crafter.canCraft(recipe);
	}

	@Override
	public boolean areEqual(Task t) {
		if (!(t instanceof CraftRecipeInHandTask)) return false;
		return ItemUtil.recipesEqual(recipe, ((CraftRecipeInHandTask)t).recipe);
	}
}
