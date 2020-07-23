package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.tasksystem.tasks.GetMiningRequirementTask;
import automc.utility.ItemUtil;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.BlockOptionalMetaLookup;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class MineBlockTask extends TaskGoal {

	private static final int EXPLORE_TIME = 300;

	BlockOptionalMetaLookup lookup;
	MiningRequirement requirement;
	int[] requiredAmounts;

	boolean allowExplore = true;

	boolean tryingToMine = false;
	private int exploreTimer = 0;

	public MineBlockTask(BlockOptionalMetaLookup lookup, int[] requiredAmounts, MiningRequirement req) {
		if (lookup.blocks().size() != requiredAmounts.length) {
			Logger.logError("For mineblocktask you MUST give the same number of blocks as requirements. This will lead to an ArrayIndexOutOfBoundsException.");
		}
		this.lookup = lookup;
		this.requiredAmounts = requiredAmounts;
		this.requirement = req;
	}
	public MineBlockTask(String[] blocks, int[] requiredAmounts, Integer[] metas, MiningRequirement req) {
		BlockOptionalMeta[] boms = new BlockOptionalMeta[blocks.length];
		for (int i = 0; i < boms.length; ++i) {
			Block block = ItemUtil.getBlock(blocks[i]);
			if (block == null) Logger.logError("Null block for string: " + blocks[i]);
			if (metas != null) {
				boms[i] = new BlockOptionalMeta(block, metas[i]);
			} else {
				boms[i] = new BlockOptionalMeta(block);
			}
		}
		this.lookup = new BlockOptionalMetaLookup(boms);
		this.requiredAmounts = requiredAmounts;
		this.requirement = req;

		if (lookup.blocks().size() != requiredAmounts.length) {
			Logger.logError("For mineblocktask you MUST give the same number of blocks as requirements. This will lead to an ArrayIndexOutOfBoundsException.");
		}
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
	protected void onGoalInit() {
		// Disable the mine process if we're starting with another mine process going on.
		if (AutoMC.getAutoMC().getBaritone().getMineProcess().isActive()) {
			AutoMC.getAutoMC().getBaritone().getMineProcess().onLostControl();
		}
		if (AutoMC.getAutoMC().getBaritone().getExploreProcess().isActive()) {
			AutoMC.getAutoMC().getBaritone().getExploreProcess().onLostControl();
		}
		tryingToMine = false;
		exploreTimer = 0;
		retry();
	}

	@Override
	public boolean isDone() {
		for(int i = 0; i < lookup.blocks().size(); ++i) {
			if (requiredAmounts[i] < 0) continue; // -1 means infinity.
			Item item = Item.getItemFromBlock(lookup.blocks().get(i).getBlock());
			if (AutoMC.getAutoMC().player.inventory.getItemCount(item) >= requiredAmounts[i]) {
				//Logger.log(this, "TEMP satisfied: " + ItemUtil.getItemId(item) + " has " + requiredAmounts[i]);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onGoalFinish() {
		Logger.log("MINE FINISHED");
		tryingToMine = false;
		exploreTimer = 0;
		AutoMC.getAutoMC().getBaritone().getMineProcess().onLostControl();
		AutoMC.getAutoMC().getBaritone().getExploreProcess().onLostControl();
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
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
	protected Task getSubTask() {

		// We don't have the mining requirement, get it.
		if (!AutoMC.getAutoMC().player.inventory.miningRequirementMet(requirement)) {
			log("Mining req: " + requirement);
			tryingToMine = false;
			return new GetMiningRequirementTask(requirement);
		}

		// We're exploring.
		if (AutoMC.getAutoMC().getBaritone().getExploreProcess().isActive()) {
			Logger.log("Exploring???");
			if (++exploreTimer > EXPLORE_TIME) {
				log("Checking for blocks again.");
				retry();
			}
			return null;
		}

		// We have the mining requirement and we're not mining.
		if (!AutoMC.getAutoMC().getBaritone().getMineProcess().isActive()) {
			if (tryingToMine) {
				log("Unable to find block. Exploring.");
				// We're trying to mine. Explore.
				BlockPos pos = Minecraft.getMinecraft().player.getPosition();
				AutoMC.getAutoMC().getBaritone().getMineProcess().onLostControl();
				AutoMC.getAutoMC().getBaritone().getExploreProcess().explore(pos.getX(), pos.getZ());
				exploreTimer = 0;
			} else {
				// We're not trying to mine. Give it a shot.
				retry();
			}
			//log("mine");
		}

		return null;
	}

	private void retry() {
		log("RETRY");
		AutoMC.getAutoMC().getBaritone().getExploreProcess().onLostControl();
		AutoMC.getAutoMC().getBaritone().getMineProcess().mine(lookup);
		tryingToMine = true;
		exploreTimer = 0;
	}

}
