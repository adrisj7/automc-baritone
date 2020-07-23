package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.ResourceTask;
import automc.tasksystem.tasks.SmeltInFurnaceTask;
import automc.utility.ItemUtil;
import net.minecraft.item.Item;

public class CollectOreAndSmeltTask extends ResourceTask {
	String ore;
	MiningRequirement req;
	
	Task smeltTask;

	public CollectOreAndSmeltTask(Item ore, Item ingot, int requiredAmount, MiningRequirement req) {
		super(ingot, requiredAmount);
		this.ore = ItemUtil.getItemId(ore);
		this.req = req;
	}

	public CollectOreAndSmeltTask(String ore, String ingot, int requiredAmount, MiningRequirement req) {
		this(ItemUtil.getItem(ore), ItemUtil.getItem(ingot), requiredAmount, req);
	}

	@Override
	protected Task getResourceSubTask() {

		// If we're smelting, keep it going.
		if (smeltTask != null && smeltTask.isActive()) {
			log("CollectOre&Smelt: smelting...");
			return smeltTask;
		}

		// If we have enough ore and aren't smelting
		int requiredRemaining = requiredAmounts[0] - AutoMC.getAutoMC().player.inventory.getItemCount(targetItems[0]);
		if (AutoMC.getAutoMC().player.inventory.getItemCount(ore) >= requiredRemaining) {
			log("CollectOre&Smelt: DO DA SMELT: " + requiredAmounts[0]);
			// Go ahead and smelt
			smeltTask = new SmeltInFurnaceTask(ore, targetItems[0], requiredAmounts[0]);
			return smeltTask;
		}

		// Otherwise, we need to mine.
		return new MineBlockTask(ore, 999, req);
	}

	@Override
	protected void onResourceGoalInit() {
		smeltTask = null;
	}

	@Override
	protected void onResourceGoalFinish() {
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}
}
