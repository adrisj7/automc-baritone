package automc.tasksystem.tasks.building;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ItemUtil;
import baritone.api.process.PathingCommand;
import baritone.process.BuilderProcess;
import baritone.utils.schematic.StaticSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BuildStaticSchematicTask extends TaskGoal {

	// Move this many blocks after being stuck to "unstuck" yourself.
	private static final double STUCK_DISTANCE_THRESHOLD = 3;
	// How long we'd have to wait before we're "stuck"
	private static final int STUCK_TIME = 20;

	private String unique_id;
	private StaticSchematic schematic;
	private Vec3i buildOrigin;

	int stuckTimer = 0;
	Vec3d stuckStartPos;

	boolean buildInPlace = false;

	Map<SmallBlockState, Integer> neededBlocks;

	boolean triedToBuild = false;

	private static final int PAUSE_TIMER_THRESHOLD = 30;
	int pauseTimer = 0;

	private boolean failed = false;

	public BuildStaticSchematicTask(String unique_id, StaticSchematic schematic, Vec3i buildOrigin) {
		this.unique_id = unique_id;
		this.schematic = schematic;
		this.buildOrigin = buildOrigin;
		
		this.buildInPlace = (buildOrigin == null);

		neededBlocks = new HashMap<>();
	}
	public BuildStaticSchematicTask(String unique_id, StaticSchematic schematic) {
		this(unique_id, schematic, null);
	}

	@Override
	protected Task getSubTask() {

		// TODO: Smarter system. This stops checking for resources once we start building.
		if (!triedToBuild) {
			// Get all resources.
			for (SmallBlockState sbs : neededBlocks.keySet()) {
				int count = neededBlocks.get(sbs);
				if (AutoMC.getAutoMC().player.inventory.getItemCount(sbs.id) < count) {
					triedToBuild = false;
					// Get the missing item.
					Task t = AutoMC.getAutoMC().itemTaskCatalogue.getItemTask(sbs.id, count);
					return t;
				}
			}
		}

		if (AutoMC.getAutoMC().getBaritone().getBuilderProcess().isPaused() ) {
			++pauseTimer;
			if (pauseTimer > PAUSE_TIMER_THRESHOLD) {
				log("Pause timeout reached.");
				failed = false;
				AutoMC.getAutoMC().getBaritone().getBuilderProcess().onLostControl();
			}
		} else {
			pauseTimer = 0;
		}

		// We have all of the resources. Build it.
		BuilderProcess bp = (BuilderProcess) AutoMC.getAutoMC().getBaritone().getBuilderProcess();
		if (!bp.isActive()) {
			if (triedToBuild) {
				log("Build task COMPLETE, or failed.");
				stop();
				return null;
			}
			// If we have no build origin, set it to the player position.
			if (buildInPlace) {
				buildOrigin = Minecraft.getMinecraft().player.getPosition();
			}
			AutoMC.getAutoMC().getBaritone().getBuilderProcess().build(unique_id, schematic, buildOrigin);
			triedToBuild = true;
			log("Trying to build the schematic.");
		} else {
			Optional<PathingCommand> op = AutoMC.getAutoMC().getBaritone().getPathingControlManager().mostRecentCommand();
			if (op.isPresent()) {
				PathingCommand c = op.get();
				Logger.log("GOAL: " + c.goal);
			}
			Logger.log("BOOF ");
			for(IBlockState b : bp.getApproxPlaceable()) {
				if (b.getBlock() != Blocks.AIR) {
					Logger.log("		" + b.getBlock().getLocalizedName());
				}
			}
		}

		
		return null;
	}

	@Override
	protected void onGoalInit() {

		failed = false;

		// Calculate what blocks we'll use.
		for (int x = 0; x < schematic.widthX(); ++x) {
			for (int y = 0; y < schematic.heightY(); ++y) {
				for(int z = 0; z < schematic.lengthZ(); ++z) {
					IBlockState s = schematic.getDirect(x, y, z);
					if (s == null) continue; // null is akin to "I don't care"
					if (checkSpecial(s)) continue; // Stuff that isn't necessarily a resource.
					Block b = s.getBlock();
					int meta = b.getMetaFromState(s);
					String id = ItemUtil.getItemId(Item.getItemFromBlock(b));

					SmallBlockState sbs = new SmallBlockState(id, meta);

					if (!neededBlocks.containsKey(sbs)) {
						neededBlocks.put(sbs, 0);
					}
					neededBlocks.put(sbs, neededBlocks.get(sbs) + 1);
				}
			}
		}
		
		enableDisableBlocks(true);

		triedToBuild = false;
	}

	@Override
	public boolean isDone() {
		return false;//!triedToBuild || AutoMC.getAutoMC().getBaritone().getBuilderProcess().isActive();
	}

	@Override
	protected void onGoalFinish() {
		enableDisableBlocks(false);
	}

	@Override
	protected boolean areConditionsMet() {
		// Assume that we can always get the blocks we need.
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof BuildStaticSchematicTask) {
			BuildStaticSchematicTask bst = (BuildStaticSchematicTask)t;
			return unique_id.equals(bst.unique_id);
		}
		return false;
	}

	private void enableDisableBlocks(boolean enable) {
		Set<String> idAlreadySeen = new HashSet<String>();
		for (SmallBlockState sbs : neededBlocks.keySet()) {
			String id = sbs.id;
			if (idAlreadySeen.contains(id)) continue;
			idAlreadySeen.add(id);
			if (enable) {
				AutoMC.getAutoMC().player.pathDisabler.enableBlock(ItemUtil.getBlock(id));				
			} else {
				AutoMC.getAutoMC().player.pathDisabler.disableBlock(ItemUtil.getBlock(id));
			}
		}
	}

	/**
	 * Checks and validates "special" blocks (like fire)
	 * @param state
	 * @return
	 */
	private boolean checkSpecial(IBlockState state) {
		Block b = state.getBlock();
		if (b.equals(Blocks.FIRE)) return true;
		if (b.equals(Blocks.AIR)) return true;
		return false;
	}
	
	public Vec3i getBuildOrigin() {
		return buildOrigin;
	}
	
	public boolean buildFailed() {
		return failed;
	}

	class SmallBlockState {
		public String id;
		public int meta;

		public SmallBlockState(String id, int meta) {
			this.id = id;
			this.meta = meta;
		}
		
		@Override
		public int hashCode() {
			return id.hashCode() + meta*999999;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof SmallBlockState) {
				SmallBlockState sbs = (SmallBlockState)o;
				return id.equals(sbs.id) && meta == sbs.meta;
			}
			return false;
		}
	}
}
