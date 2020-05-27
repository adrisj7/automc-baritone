package automc.player;

import java.util.HashMap;
import java.util.List;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

public class Crafter {

	private PlayerController getPlayer() {
		return AutoMC.getAutoMC().player;
	}
	
	// Returns whether it succeeded
	public boolean craft(ContainerType type, IRecipe recipe) {

		if (!canCraft(recipe)) {
			Logger.debug(this, "...can't craft recipe...");
			return false;
		}

		int recipeWidth = getRecipeMaxWidth(recipe, type == ContainerType.PLAYER);

		List<Ingredient> ingredientList = getRecipeIngredients(recipe); 

		// If our recipe is too big for a 2x2 or 3x3 space.
		boolean tooBig = false;
		switch (type) {
		case PLAYER:
			tooBig = ingredientList.size() > 4 || (recipeWidth == 1 && ingredientList.size() > 2);
			break;
		case CRAFTING:
			tooBig = ingredientList.size() > 9 || (recipeWidth == 1 && ingredientList.size() > 3 || recipeWidth == 2 && ingredientList.size() > 6);
			break;
		default:
			Logger.logError("Tried calling the \"craft\" method from a non-crafting container...");
			return false;
		}
		if (tooBig) {
			Logger.logError("Tried to craft " + recipe.toString() + " but the recipe is too big to fit in the crafting space:" + recipeWidth + ", size: " + ingredientList.size() );
			return false;
		}

		Inventory inventory = getPlayer().inventory;

		int craftIndex = 0;
		String debugCompile = "recipe width: " + recipeWidth + ": slots: ";
		for (Ingredient ingredient : ingredientList) {

			//Logger.log(this, "Ingredient: " + ingredient + ", " + ingredient.getMatchingStacks().length);
			
			//System.out.println("INGREDIENT: " + ingredient + ", " + ingredient.getMatchingStacks().length);

			for(ItemStack stack : ingredient.getMatchingStacks()) {
				List<Integer> indices = inventory.getInvSlotsForItem(stack.getItem());
				if (indices.size() > 0) {
					// We have an item, so move it.
					int windowIndex = Inventory.inventorySlotToWindowSlot(type, indices.get(0));
					debugCompile += craftIndex + " ";
					inventory.moveItems(type, windowIndex, craftIndex + 1, 1);
					break;
				}
			}

			// If our recipe is SMALLER than the max width, we will skip a few indices depending on the size of our grid.
			if (recipeWidth == 1) {
				craftIndex += (type == ContainerType.PLAYER)? 1 : 2;
			} else if (recipeWidth == 2 && type == ContainerType.CRAFTING) {
				if (craftIndex % 3 == 1) {
					craftIndex += 1;
				}
			}
			++craftIndex;
		}
		Logger.debug(this, debugCompile);

		Logger.debug(this, "Getting table output...");
		return getPlayer().inventory.receiveCraftingOutput(type);
	}

	// Get the maximum width of a recipe. If it's shapeless, fill the whole grid.
	int getRecipeMaxWidth(IRecipe recipe, boolean expectSmall) {
		int width = expectSmall? 2 : 3;
		if (recipe instanceof ShapedRecipes) {
			ShapedRecipes srecipe = (ShapedRecipes)recipe;
			width = srecipe.getWidth();
		}
		return width;
	}

	// Get a list of ingredients from a recipe.
	public List<Ingredient> getRecipeIngredients(IRecipe recipe) {
		if (recipe instanceof ShapedRecipes) {
			ShapedRecipes srecipe = (ShapedRecipes)recipe;
			return srecipe.getIngredients();
		} else if (recipe instanceof ShapelessRecipes) {
			ShapelessRecipes srecipe = (ShapelessRecipes)recipe;
			return srecipe.getIngredients();
		} else {
			Logger.debug(this, "Recipe is neither shaped nor shapeless, something messed up: " + recipe.toString());
			return null;
		}
	}

	public boolean canCraft(IRecipe recipe) {
		// Try using up items. If we successfully fill in the requirements, we're good!
		HashMap<Item, Integer> itemsUsed = new HashMap<Item, Integer>();
		for(Ingredient ingredient : getRecipeIngredients(recipe)) {

			// Might be "air"
			if (ingredient.getMatchingStacks().length == 0) continue;
			
			boolean foundValid = false;
			for(ItemStack stack : ingredient.getMatchingStacks()) {
				Item item = stack.getItem();
				if (getPlayer().inventory.hasItem(item)) {
					if (!itemsUsed.containsKey(item)) {
						itemsUsed.put(item, 0);
					}
					// If we have the space to use up one more item, use it.
					if (itemsUsed.get(item) < getPlayer().inventory.getItemCount(item)) {
						itemsUsed.put(item, itemsUsed.get(item) + 1);
						foundValid = true;
						break;
					}
				}
			}
			// We must find a valid item for every ingredient.
			if (!foundValid) {
				return false;
			}
		}
		return true;
	}
}
