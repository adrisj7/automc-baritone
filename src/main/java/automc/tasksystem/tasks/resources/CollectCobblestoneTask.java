package automc.tasksystem.tasks.resources;

import automc.definitions.MiningRequirement;

@Deprecated
public class CollectCobblestoneTask extends MineBlockTask {

	public CollectCobblestoneTask(int requiredAmount) {//String[] blocks, int[] requiredAmounts, Integer[] metas, MiningRequirement req) {
		super(new String[] {"stone", "cobblestone"}, new int[] {requiredAmount, requiredAmount}, new Integer[] {0, null}, MiningRequirement.WOOD);
	}

}
