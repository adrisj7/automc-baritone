package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ItemUtil;
import baritone.api.utils.BlockOptionalMeta;

public class ExampleChestTask extends TaskGoal {

	boolean grabbed = false;
	
	private String item = "";

	public ExampleChestTask(String item) {
		super();
		this.item = item;
	}

	@Override
	protected Task getSubTask() {
		if (AutoMC.getAutoMC().player.inventory.isChestOpened()) {
			Logger.log("BOOF");
			if (grabbed) return null;
			log("There!");
			AutoMC.getAutoMC().player.inventory.grabItemFromOpenChest(item, 1);
			grabbed = true;
		} else {
			if (!AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().isActive()) {
				log("Going");
				AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().getToBlock(new BlockOptionalMeta(ItemUtil.getBlock("chest")));
			}
		}
		return null;
	}

	@Override
	protected void onGoalInit() {
		grabbed = false;
	}

	@Override
	protected boolean isDone() {
		return grabbed;
	}

	@Override
	protected void onFinish() {
		AutoMC.getAutoMC().getBaritone().getGetToBlockProcess().onLostControl();
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	protected boolean areEqual(Task t) {
		// TODO Auto-generated method stub
		return false;
	}

}
