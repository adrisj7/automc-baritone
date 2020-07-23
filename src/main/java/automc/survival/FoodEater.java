package automc.survival;

import java.util.HashSet;
import java.util.Set;

import automc.AutoMC;
import automc.Logger;
import automc.control.InputOverrider;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

/**
 * Keeps track of our food, eats, and lets anyone know when we need more food
 */
public class FoodEater {

	// DO NOT eat these automatically unless we're like almost dead.
	private Set<String> autoEatBlacklist;

	// TODO: parametarize?
	private static final int HUNGER_THRESHOLD = 14;
	private static final int HEALTH_THRESHOLD = 12;

	private static final boolean DONT_EAT_RAW_FOODS = true;

	private Set<String> rawFoods;

	public enum EatStrategy {
		THRESHOLD,
		KNAPSACK
	}

	private int eatSlot = -1;

	public FoodEater() {
		autoEatBlacklist = new HashSet<String>();
		rawFoods = new HashSet<String>();
		addToBlacklist(
			"enchanted_golden_apple",
			"chorus_fruit",
			"spider_eye",
			"rotten_flesh"
		);
		addRawFoods(
			"porkchop",
			"fish",
			"beef",
			"chicken",
			"rabbit",
			"mutton",
			"potato"
		);
	}

	private EatStrategy strategy = EatStrategy.THRESHOLD;

	public void onTick() {
		if (getHealth() < HEALTH_THRESHOLD) {
			// If we can't regenerate.
			if (getFoodLevel() < 17) {
				thresholdStrategy(99999); // Eat no matter what. Will fill in the smallest food that will do the job.
			}
		}
		switch (strategy) {
		case THRESHOLD:
			thresholdStrategy(HUNGER_THRESHOLD);
			break;
		case KNAPSACK:
			knapsackStrategy();
		}
	}

	public void onFoodEaten() {
		stop();
	}

	public void stop() {
		if (isEating()) {
			AutoMC.getAutoMC().inputOverride.setInputForce(InputOverrider.RIGHT_CLICK, false);
			eatSlot = -1;
		}
	}

	public boolean isEating() {
		return (eatSlot != -1);
	}

	// Just eat the food item that will utilize 
	private void thresholdStrategy(int threshold) {
		if (getFoodLevel() < threshold) {
			int maxNoWasteHunger = 1 + 20 - getFoodLevel(); // Let us waste one here, just cause.

			boolean hasFood = false;
			int bestSlot = -1;
			int bestHeal = 0;
			int smallestHeal = 9999;
			int smallestSlot = -1;

			boolean hasNonRawFood = false;
			for (int invSlot : AutoMC.getAutoMC().player.inventory.getInventorySlotsWithFood()) {
				ItemStack stack = AutoMC.getAutoMC().player.inventory.getStack(invSlot);
				ItemFood food = (ItemFood)stack.getItem();
				String id = ItemUtil.getItemId(food);
				if (!rawFoods.contains(id)) {
					hasNonRawFood = true;
					break;
				}
			}

			// Get the biggest food that isn't wasteful.
			for (int invSlot : AutoMC.getAutoMC().player.inventory.getInventorySlotsWithFood()) {
				ItemStack stack = AutoMC.getAutoMC().player.inventory.getStack(invSlot);
				ItemFood food = (ItemFood)stack.getItem();
				if (autoEatBlacklist.contains(ItemUtil.getItemId(food))) continue; // Blacklist

				boolean isRaw = rawFoods.contains(ItemUtil.getItemId(food));
				if (isRaw && (DONT_EAT_RAW_FOODS || hasNonRawFood)) continue; // Don't eat raw food if we have non raw food.

				hasFood = true;
				int heal = food.getHealAmount(null); // The "null" argument here is unused.
				if (heal < maxNoWasteHunger && heal > bestHeal) {
					bestSlot = invSlot;
					bestHeal = heal;
				} else if (heal < smallestHeal) {
					smallestHeal = heal;
					smallestSlot = invSlot;
				}
			}
			if (hasFood) {
				if (bestSlot != -1) {
					// Eat from best slot.
					eatFromSlot(bestSlot);
				} else {
					// Eat from smallest slot.
					eatFromSlot(smallestSlot);
				}
			}
		}
	}

	// Eat food if it fits.
	private void knapsackStrategy() {
		Logger.logError("implement me >:(");
	}

	private void eatFromSlot(int invSlot) {
		AutoMC.getAutoMC().inputOverride.setInputForce(InputOverrider.RIGHT_CLICK, true);
		AutoMC.getAutoMC().player.inventory.equipItem(invSlot);
		if (eatSlot == invSlot) return;
		eatSlot = invSlot;
	}

	private void addToBlacklist(String ...blacklist) {
		for (String item : blacklist) {
			autoEatBlacklist.add(item);
		}
	}
	private void addRawFoods(String ...foods) {
		for (String food : foods) {
			rawFoods.add(food);
		}
	}

	private int getFoodLevel() {
		return Minecraft.getMinecraft().player.getFoodStats().getFoodLevel();
	}
	private float getHealth() {
		return Minecraft.getMinecraft().player.getHealth();
	}
}
