package automc.tasksystem.tasks;

import java.util.Optional;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ItemUtil;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalRunAway;
import baritone.api.process.IGetToBlockProcess;
import baritone.api.process.PathingCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public abstract class DoStuffInContainerTask extends TaskGoal {
	private static final int REFRESH_SEARCH_INTERVAL = 100;
	private int tickCounter = 0;

	boolean triedToSearch;
	boolean checkedHeuristicFlag; // TODO: Bad idea.
	boolean forceSearchOverrideOverrideFlag; // TODO: VERY BAD IDEA

	private Task ranPlaceTask;
	private Task collectingTask;

	private String containerBlock;

	private BlockPos cachedContainerPos = null;

	public DoStuffInContainerTask(String block) {
		this.containerBlock = block;
	}

	@Override
	protected Task getSubTask() {

		// Some containers require some prep work. For instance, furnaces require the gathering of fuel.
		Task beforeTask = getPrerequisiteTask();
		if (beforeTask != null) {
			//AutoMC.getAutoMC().player.closeContainer();
			return beforeTask;
		}

		// If table UI is opened, craft and finish.
		if (isUIOpened()) {
			if (cachedContainerPos == null) {
				RayTraceResult rtr = Minecraft.getMinecraft().player.rayTrace(5, 1.0F);
				cachedContainerPos = rtr.getBlockPos();
			}
			if (cachedContainerPos != null) {
				triedToSearch = false;
				forceSearchOverrideOverrideFlag = false;
				return getUISubTask(cachedContainerPos);
			} else {
				Logger.logError("Failed to raytrace to container, something went wrong. Program a more robust solution if this isn't exceedingly rare.");
			}
		}
		
		// We're collecting the container
		if (collectingTask != null && collectingTask.isActive()) {
			return collectingTask;
		}
		// We're placing the container
		if (ranPlaceTask != null && ranPlaceTask.isActive()) {
			return ranPlaceTask;
		}

		// If we're going to a block manually, cut it out when we're close enough.
		if (getPositionOverride() != null && AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().isActive()) {
			BlockPos override = getPositionOverride();
			double sqrDist = Minecraft.getMinecraft().player.getPositionVector().squareDistanceTo(override.getX(), override.getY(), override.getZ());
			double thresh = 2;
			if (sqrDist < thresh*thresh) {
				// If we're still trying to break the block
				log("We're close enough, now switching tasks to get to nearest of our container");
				AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().onLostControl();
				goToBlock();
				checkedHeuristicFlag = false;
				tickCounter = 0;
				triedToSearch = false;
				forceSearchOverrideOverrideFlag = true;
			}
		}

//		AutoMC.getAutoMC().player.closeContainer();

		// Get the nearest table if we haven't tried that already.
		if (!triedToSearch) {
			cachedContainerPos = null;
			collectingTask = null;
			log("(DoStuffInContainer) SEARCHING");
			BlockPos override = getPositionOverride();
			if (override == null || forceSearchOverrideOverrideFlag) {
				goToBlock();
				checkedHeuristicFlag = false;
				tickCounter = 0;
				triedToSearch = true;
				return null;
			} else {
				// We're overriden, go to the block.
				getBlockProcess().onLostControl();
				AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalBlock(override));
				tickCounter = 0;
				return null;
			}
		}

		// If we're searching for the block, stop if we start exploring.
		if (getBlockProcess().isActive()) {
			//Logger.debug(this, "going...");
			Optional<PathingCommand> op = AutoMC.getAutoMC().getBaritone().getPathingControlManager().mostRecentCommand();
			if (op.isPresent()) {
				PathingCommand c = op.get();
				// If we're exploring, we didn't find anything.
				// TODO: Figure this out for real though.
				boolean isExploring = c.goal instanceof GoalRunAway;
				if (isExploring) {
					log("Cancelled going to target since we didn't find anything.");
					getBlockProcess().onLostControl();
					ranPlaceTask = null;
					return null;
				}
				if (checkedHeuristicFlag) {
					return null;
				}
				checkedHeuristicFlag = true;
				// Now check for how far the table is.
				// Add some artificial extra scaling so we avoid recrafting stuff over and over again.
				double maxTravelCost = AutoMC.getAutoMC().itemWorkDictionary.getWorkTicks(containerBlock) * 1.5 * 10;
				if (c != null && c.goal != null) {
					BlockPos p = Minecraft.getMinecraft().player.getPosition();
					double heuristic = c.goal.heuristic(p);
					log("Heur: " + heuristic + ", " + maxTravelCost);
					// If it's too far, cancel.
					if (heuristic > maxTravelCost) {
						log("Cancelled going to target since it's too far away.");
						getBlockProcess().onLostControl();
						ranPlaceTask = null;
						return null;
					}
				}
			}
			return null;
		}

		// If we tried to place something and finished, search again.
		if (ranPlaceTask != null && !ranPlaceTask.isActive() ) {
			triedToSearch = false;
			return null;
		}

		// At this point, we tried searching and have failed. Get da blocks.

		// Refresh the search occasionally
		if (++tickCounter % REFRESH_SEARCH_INTERVAL == 0) {
			log("Refreshing search.");
			triedToSearch = false;
		}

		// If we have a table, place it down.
		if (AutoMC.getAutoMC().player.inventory.hasItem(containerBlock)) {
			ranPlaceTask = new PlaceBlockNearbyTask(containerBlock);
			return ranPlaceTask;
		}

		collectingTask = AutoMC.getAutoMC().itemTaskCatalogue.getItemTask(containerBlock, 1); //new CollectCraftingTableTask(1);
		return collectingTask;
	}

	@Override
	protected void onGoalInit() {
		log("Going to Container: INIT");
		triedToSearch = false;
		checkedHeuristicFlag = false;
		forceSearchOverrideOverrideFlag = false;
		ranPlaceTask = null;
		tickCounter = 0;
		cachedContainerPos = null;
		collectingTask = null;

		AutoMC.getAutoMC().player.closeContainer();
	}

	@Override
	protected void onGoalFinish() {
		AutoMC.getAutoMC().player.closeContainer();
		getBlockProcess().onLostControl();
	}

	private IGetToBlockProcess getBlockProcess() {
		return AutoMC.getAutoMC().getBaritone().getGetToBlockProcess();
	}
	
	private void goToBlock() {
		getBlockProcess().getToBlock(ItemUtil.getBlock(containerBlock));
	}
	
	protected abstract boolean isUIOpened();
	protected abstract Task getUISubTask(BlockPos containerPos);
	protected abstract Task getPrerequisiteTask();
	protected BlockPos getPositionOverride() {
		return null;
	}
}
