package automc.player;

import java.util.HashMap;
import java.util.List;

import baritone.api.BaritoneAPI;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

/**
 * 	Marks blocks as disabled and re-enabled for baritone to path.
 *	We're doing this so that baritone doesn't end up using blocks we consider "valuable" at the moment.
 *
 *	TODO: Do this more intelligently. Instead of disabling an item completely, disable a QUANTITY of that item ensuring we have a minimum amount of said item.
 *
 */

public class PathDisabler {
	// Contains how many other processes are "disabling" this particular block.
	HashMap<Item, Integer> disableStack;

	public PathDisabler() {
		disableStack = new HashMap<Item, Integer>();
	}

	public void disableBlock(Item block) {
		List<Item> val = BaritoneAPI.getSettings().acceptableThrowawayItems.value;
		// Already disabled.
		if (!val.contains(block)) {
			return;
		}
		if (!disableStack.containsKey(block)) {
			disableStack.put(block, 0);
			val.remove(block);
		}
		disableStack.put(block, disableStack.get(block) + 1);
	}

	public void enableBlock(Item block) {
		// Already enabled
		if (!disableStack.containsKey(block)) {
			return;
		}
		disableStack.put(block, disableStack.get(block) - 1);
		// Add our block back.
		if (disableStack.get(block) <= 0) {
			disableStack.remove(block);
			BaritoneAPI.getSettings().acceptableThrowawayItems.value.add(block);
		}
	}
	public void enableBlock(Block block) {
		enableBlock(Item.getItemFromBlock(block));
	}
	public void disableBlock(Block block) {
		disableBlock(Item.getItemFromBlock(block));
	}

	// Just to be safe, disable every possible stack in the reicpe. FOR NOW.
	public void disableRecipe(IRecipe recipe) {
		for(Ingredient ing : recipe.getIngredients()) {
			for(ItemStack st : ing.getMatchingStacks()) {
				disableBlock(st.getItem());
			}
		}
	}
	public void enableRecipe(IRecipe recipe) {
		for(Ingredient ing : recipe.getIngredients()) {
			for(ItemStack st : ing.getMatchingStacks()) {
				enableBlock(st.getItem());
			}
		}
	}

	// Add everything back.
	public void reset() {
		for(Item block : disableStack.keySet()) {
			disableStack.remove(block);
			BaritoneAPI.getSettings().acceptableThrowawayItems.value.add(block);
		}
	}

}
