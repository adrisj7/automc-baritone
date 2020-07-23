package automc.tasksystem.tasks.resources;

import java.util.HashMap;
import java.util.Map;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.CraftRecipeInHandTask;
import automc.tasksystem.tasks.CraftRecipeInTableTask;
import automc.tasksystem.tasks.ResourceTask;
import automc.utility.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

/**
 * This is basically a "generic" option for creating an item.
 *
 * It will collect each item in the recipe and craft it either in a table or in your inventory.
 *
 */
public class CollectRecipeAndCraftTask extends ResourceTask {

	IRecipe recipe;
	boolean isSmall;

	Task craftTask;

	public CollectRecipeAndCraftTask(IRecipe recipe, Item targetItem, int requiredAmount) {
		super(targetItem, requiredAmount);
		this.recipe = recipe;
		isSmall = recipe.canFit(2, 2);
	}
	public CollectRecipeAndCraftTask(IRecipe recipe, String targetItem, int requiredAmount) {
		super(targetItem, requiredAmount);
		this.recipe = recipe;
		isSmall = recipe.canFit(2, 2);
	}

	@Override
	protected Task getResourceSubTask() {
		// Get a map of all required items.
		// Note that this does NOT work well with super flexible recipes.
		Map<String, Integer> required = new HashMap<>();
		// For each item, if we don't have it, find it.
		for (Ingredient ing : recipe.getIngredients()) {
			// TODO: Smarter way.
			// FOR NOW WE WILL JUST PICK THE FIRST ITEM AND GO WITH IT.
			boolean foundOption = false;
			String debugPrint = "[ ";
			if (ing.getMatchingStacks().length == 0) continue;
			for (ItemStack stack : ing.getMatchingStacks()) {
				Item item = stack.getItem();
				int count = stack.getCount();
				String id = ItemUtil.getItemId(item);
				debugPrint += id + " ";
				if (!AutoMC.getAutoMC().itemTaskCatalogue.taskExists(id)) {
					continue;
				}
				if (foundOption) continue;
				foundOption = true;
				if (!required.containsKey(id)) {
					required.put(id, 0);
				}
				required.put(id, required.get(id) + count);
			}
			debugPrint += "]";
			if (!foundOption) {
				Logger.logError("(For item " + targetItems[0] + ") Could not find valid resource option for the ingredient with matching items: " + debugPrint + ". Stopping resource collection task.");
				stop();
			} else {
				//Logger.log("ye " + ItemUtil.getItemId(targetItems[0]) + ", " + debugPrint);
			}
		}

		// For all required items, make sure we have them.
		for (String id : required.keySet()) {
			// If we have less items than required, get the items.
			int requiredAmount = required.get(id) * requiredAmounts[0];
			if (AutoMC.getAutoMC().player.inventory.getItemCount(id) < requiredAmount) {
				Task toGet = AutoMC.getAutoMC().itemTaskCatalogue.getItemTask(id, requiredAmount);
				log("Getting: " + toGet + ", " + requiredAmount + "(item: " + id + " " + AutoMC.getAutoMC().player.inventory.getItemCount(id) + ")");
				return toGet;
			} else {
				Logger.log("We have: " + requiredAmount + " of " + id);
			}
		}

		// We now have all of the items. Craft it.
		if (isSmall) {
			return new CraftRecipeInHandTask(recipe);
		} else {
			return new CraftRecipeInTableTask(recipe);
		}
	}

	@Override
	protected void onResourceGoalInit() {
		Logger.log(this, "TEST: " + toString());
		craftTask = null;
		AutoMC.getAutoMC().player.pathDisabler.disableRecipe(recipe);
	}

	@Override
	protected void onResourceGoalFinish() {
		AutoMC.getAutoMC().player.pathDisabler.enableRecipe(recipe);
		AutoMC.getAutoMC().player.closeContainer();
	}

	@Override
	protected boolean areConditionsMet() {
		// We will always assume we can do it.
		return true;//AutoMC.getAutoMC().player.crafter.canCraft(recipe);
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof CollectRecipeAndCraftTask) {
			CollectRecipeAndCraftTask crt = (CollectRecipeAndCraftTask) t;

			if (requiredAmounts[0] != crt.requiredAmounts[0]) return false;
			// Compare the recipes.
			return ItemUtil.recipesEqual(recipe, crt.recipe);

		}
		return false;
	}
	
	@Override
	public String toString() {
		return super.toString() + ": " + targetItems[0] + ", " + requiredAmounts[0];
	}

}
