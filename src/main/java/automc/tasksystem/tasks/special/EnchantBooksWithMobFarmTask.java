package automc.tasksystem.tasks.special;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.tasksystem.tasks.EnchantInTableTaskConstantEnchantment;
import automc.tasksystem.tasks.navigation.TravelToPositionTask;
import automc.tasksystem.tasks.resources.MoveItemsToAnyChestTask;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class EnchantBooksWithMobFarmTask extends TaskGoal {

	private BlockPos farmingPos = null;
	private boolean storeBooksInChest = true;

	private int preferredLapis;

	private Task getLapisTask = null;
	
	public EnchantBooksWithMobFarmTask(boolean storeBooksInChest, int preferredLapis) {
		this.storeBooksInChest = storeBooksInChest;
		this.preferredLapis = preferredLapis;
	}

	@Override
	protected Task getSubTask() {

		// TODO: GET BOOKS

		// Empty out loot
		if (AutoMC.getAutoMC().player.inventory.getEmptySlots() < 2) {
			// TODO: This will also empty your bows, even good ones.
			return new MoveItemsToAnyChestTask(new String[] {
				"bone", "rotten_flesh", "cobblestone", "arrow", "string", "bow"
			});
		}

		// If has enchanting books, move to chest.
		if (storeBooksInChest && AutoMC.getAutoMC().player.inventory.hasItem("enchanted_book")) {
			return new MoveItemsToAnyChestTask("enchanted_book");
		}
		// If collecting lapis, keep doing so until we've collected our preferred amount.
		if (getLapisTask != null && getLapisTask.isActive()) {
			return getLapisTask;
		}
		// If we don't have enough lapis, get more.
		if (AutoMC.getAutoMC().player.inventory.getItemCountWithMeta("dye", 4) < 3) {
			getLapisTask = AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("lapis_lazuli", preferredLapis);
			return getLapisTask;
		}

		// If we have less than 30 XP, go to the experience spawner point and stand there.
		if (Minecraft.getMinecraft().player.experienceLevel < 30) {
			double distSq = Minecraft.getMinecraft().player.getPosition().distanceSq(farmingPos);
			if (distSq > 1.4f) {
				return new TravelToPositionTask(farmingPos);
			}
			// Wait for us to go to the spawner.
			return null;
		}

		// At this point we have all of our conditions.
		// Run a "enchant in table" task that just picks the max enchantment. In it, put in a book and the required lapis.
		return new EnchantInTableTaskConstantEnchantment("book", 3);
	}

	@Override
	protected void onGoalInit() {
		// We start at the block pos.
		Logger.log(this, "Farming from current position. Will return here to farm.");
		farmingPos = Minecraft.getMinecraft().player.getPosition();
	}

	@Override
	protected void onGoalFinish() {
		// Do nothing.
	}

	@Override
	public boolean isDone() {
		// Run forever
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		// Do we have books?
		return AutoMC.getAutoMC().player.inventory.hasItem("book");
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof EnchantBooksWithMobFarmTask) {
			return true;
		}
		return false;
	}

}
