package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.player.Inventory;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;

public class CollectFuelTask extends TaskGoal {

	// The order which we get our fuel. If we don't have coal, get planks.
	private static final String[] ORDER = new String[] {"coal", "planks"};

	private double requiredFuel;
	private int orderClimb = 0;
	
	Task collectionTask;

	public CollectFuelTask(double requiredFuel) {
		this.requiredFuel = requiredFuel;
	}

	@Override
	protected Task getSubTask() {
		if (orderClimb >= ORDER.length) {
			return null;
		}

		// If our previous task failed, try the next fuel.
		if (collectionTask != null && !collectionTask.isActive() || AutoMC.getAutoMC().getBaritone().getExploreProcess().isActive()) {
			collectionTask = null;
			log("Failed to collect fuel " + ORDER[orderClimb] + ". Will now try the next option.");
			tryNextFuel();
		}

		String targetFuel = ORDER[orderClimb];
		double fuelPower = Inventory.getFuelAmount(targetFuel);
		int fuelNeeded = (int)Math.ceil(requiredFuel / fuelPower);
		collectionTask = AutoMC.getAutoMC().itemTaskCatalogue.getItemTask(targetFuel, fuelNeeded);
		//log("test: Getting it right?: " + targetFuel + ", " + fuelNeeded + ", " + collectionTask);
		return collectionTask;
	}

	@Override
	protected void onGoalInit() {
		orderClimb = 0;
		collectionTask = null;
	}

	@Override
	public boolean isDone() {
		return AutoMC.getAutoMC().player.inventory.getValidFuelAmount() >= requiredFuel;
	}

	@Override
	protected void onGoalFinish() {		
	}

	@Override
	protected boolean areConditionsMet() {
		return orderClimb < ORDER.length;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof CollectFuelTask) {
			CollectFuelTask cft = (CollectFuelTask) t;
			if (Math.abs(cft.requiredFuel - requiredFuel) < 0.1) {
				return true;
			}
		}
		return false;
	}

	private void tryNextFuel() {
		++orderClimb;
		if (orderClimb >= ORDER.length) {
			log("FAILED to get fuel. Could not find ANY instance of valid fuel anywhere.");
		}
	}
}