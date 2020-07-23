package automc.tasksystem.tasks.resources;

import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.ResourceTask;
import automc.utility.ItemUtil;
import net.minecraft.item.Item;

/**
 * This is a mining task that expects the mined block to give a DIFFERENT output from just itself.
 *
 */
public class CollectMineOutputTask extends ResourceTask {

	String[] blocks;
	Integer[] metas;
	MiningRequirement req;

	int[] reqAmountDuplicate;

	public CollectMineOutputTask(String[] blocks, Integer[] metas, Item item, int requiredAmount, MiningRequirement req) {
		this(blocks, metas, ItemUtil.getItemId(item), requiredAmount, req);
	}
	
	public CollectMineOutputTask(String[] blocks, Integer[] metas, String item, int requiredAmount, MiningRequirement req) {
		super(item, requiredAmount);
		this.blocks = blocks;
		this.metas = metas;
		this.req = req;
		
		reqAmountDuplicate = new int[blocks.length];
		for (int i = 0; i < blocks.length; ++i) reqAmountDuplicate[i] = 999; // TODO: Have an option for "mine forever"
	}
	public CollectMineOutputTask(String[] blocks, String item, int requiredAmount, MiningRequirement req) {
		this(blocks, null, item, requiredAmount, req);
	}
	public CollectMineOutputTask(String block, String item, int requiredAmount, MiningRequirement req) {
		this(new String[] {block}, null, item, requiredAmount, req);
	}

	@Override
	protected Task getResourceSubTask() {
		return new MineBlockTask(blocks, reqAmountDuplicate, metas, req);
	}

	@Override
	protected void onResourceGoalInit() {
	}

	@Override
	protected void onResourceGoalFinish() {
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}
}
