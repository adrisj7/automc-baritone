package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.definitions.LoopState;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.utility.ItemUtil;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.BlockOptionalMetaLookup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

public class MineBlockTask extends Task {

	BlockOptionalMetaLookup lookup;
	MiningRequirement requirement;
	int[] requiredAmounts;

	public MineBlockTask(BlockOptionalMetaLookup lookup, int[] requiredAmounts, MiningRequirement req) {
		this.lookup = lookup;
		this.requiredAmounts = requiredAmounts;
		this.requirement = req;
	}
	public MineBlockTask(String[] blocks, int[] requiredAmounts, Integer[] metas, MiningRequirement req) {
		BlockOptionalMeta[] boms = new BlockOptionalMeta[blocks.length];
		for (int i = 0; i < boms.length; ++i) {
			Block block = ItemUtil.getBlock(blocks[i]);
			if (metas != null) {
				boms[i] = new BlockOptionalMeta(block, metas[i]);
			} else {
				boms[i] = new BlockOptionalMeta(block);
			}
		}
		this.lookup = new BlockOptionalMetaLookup(boms);
		this.requiredAmounts = requiredAmounts;
		this.requirement = req;
	}
	public MineBlockTask(String[] blocks, int[] requiredAmounts, MiningRequirement req) {
		this(blocks, requiredAmounts, null, req);
	}
	public MineBlockTask(String block, int requiredAmount, Integer meta, MiningRequirement req) {
		this(new String[] {block}, new int[] {requiredAmount}, new Integer[] {meta}, req);
	}
	public MineBlockTask(String block, int requiredAmount, MiningRequirement req) {
		this(block, requiredAmount, null, req);
	}
	public MineBlockTask(String block, int requiredAmount) {
		this(block, requiredAmount, null, MiningRequirement.HAND);
	}

	@Override
	protected void onInit() {
		AutoMC.getAutoMC().getBaritone().getMineProcess().mine(lookup);
	}

	@Override
	protected boolean isDone() {
		for(int i = 0; i < lookup.blocks().size(); ++i) {
			Item item = Item.getItemFromBlock(lookup.blocks().get(i).getBlock());
			if (AutoMC.getAutoMC().player.inventory.getItemCount(item) >=  requiredAmounts[i]) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onFinish() {
		AutoMC.getAutoMC().getBaritone().getMineProcess().onLostControl();
	}

	@Override
	protected boolean areConditionsMet() {
		return AutoMC.getAutoMC().player.inventory.miningRequirementMet(requirement);
	}

	@Override
	protected boolean areEqual(Task t) {
		if (t instanceof MineBlockTask) {
			MineBlockTask mbt = (MineBlockTask) t;
			if (mbt.lookup.blocks().size() != lookup.blocks().size()) return false;
			for (int i = 0; i < lookup.blocks().size(); ++i) {
				if (requiredAmounts[i] != mbt.requiredAmounts[i]) return false;
				BlockOptionalMeta bom0 = lookup.blocks().get(i),
								  bom1 = mbt.lookup.blocks().get(i);
				if (bom0.getMeta() != bom1.getMeta()) return false;
				if (!bom0.getBlock().equals(bom1.getBlock())) return false;
			}
			return true;
		}
		return false;
	}
	@Override
	protected void onTick(LoopState state) {
		// Do nothing
	}
	@Override
	protected void onInterrupt() {
		// Cancel mining
		onFinish();
	}

}
