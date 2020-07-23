package automc.tasksystem.tasks.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Keyboard;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.tasksystem.tasks.PlaceBlockNearbyTask;
import automc.tasksystem.tasks.misc.GetStackedTask;
import automc.tasksystem.tasks.resources.CollectFoodTask;
import automc.utility.ItemUtil;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.pathing.movement.CalculationContext;
import baritone.process.MineProcess;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * 	This is for those long haul runs. Really long haul runs.
 *
 */

public class TravelOnHighwayWithBedBackupTask extends TaskGoal {

	enum State {
		Traveling,
		Food,
		FindingBed
	}
	private State state = State.Traveling;

	// If true, will also grab food in the overworld.
	boolean collectFoodInOverworld = false;
	// If true, will keep our gear up if it breaks when we're in the overworld.
	boolean stayStacked = false;

	// When we go back 
	double minDistanceToPlaceBed = 500;

	// Sub tasks
	private TravelOnHighwayTask travelTask;
	private CollectFoodTask foodTask;
	private GetStackedTask stackedTask;

	private BlockPos portalExitPos = null;

	private boolean checkForBed = false;
	private Task goToBedTask = null;
	private Task placeBedTask = null;

	public TravelOnHighwayWithBedBackupTask(int targetX, int targetZ, boolean collectFoodInOverword, boolean stayStacked) {
		this.collectFoodInOverworld = collectFoodInOverword;
		this.stayStacked = stayStacked;

		travelTask = new TravelOnHighwayTask(targetX, targetZ);
		foodTask = new CollectFoodTask(20);
		stackedTask = new GetStackedTask();
		// TODO: Check for orientation/metas.
		goToBedTask = new GetToBlockTask(ItemUtil.getBlock("bed"));
		placeBedTask = new PlaceBlockNearbyTask("bed");
	}

	@Override
	protected Task getSubTask() {

		boolean inOverworld = AutoMC.getAutoMC().player.isInOverworld();

		switch (state) {
		case Traveling:
			if (collectFoodInOverworld) {
				if (foodTask.needsFood()) {
					Logger.debug(this, "GETTING FOOD. Will leave the nether and get some.");
					travelTask.leaveNether(true);
					state = State.Food;
					return null;
				}
			}

			
			// TODO: Check for night time and distance to set bed time.
			if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
				Logger.debug(this, "GOING TO SLEEP. Will leave the nether, sleep, then come back.");
				travelTask.leaveNether(true);
				state = State.FindingBed;
				return null;
			}
			
			// If we need to stack, go ahead and stack. Will only do this in the overworld.
			if (shouldStack()) {
				return stackedTask;
			}

			return travelTask;
		case FindingBed:
			if (!inOverworld) {
				portalExitPos = null;
				// We're in the nether, we should be leaving in out travel task.
				// TODO: Make a LeaveNether task and use that instead.
				travelTask.leaveNether(false);
				return travelTask;
			}
			if (portalExitPos == null) {
				portalExitPos = AutoMC.getAutoMC().player.getOverworldPosition();
			}
			// We're in the overworld, do the bed thing.
			return findBedSubTask(portalExitPos, minDistanceToPlaceBed);
		case Food:
			if (foodTask.needsFood()) {
				if (!inOverworld) {
					// We're in the nether, we should be leaving in out travel task.
					// TODO: Make a LeaveNether task and use that instead.
					travelTask.leaveNether(false);
					return travelTask;
				}
				return foodTask;
			} else {
				// We're done with food. Keep moving.
				state = State.Traveling;
				return null;
			}
		}
		return null;
	}

	@Override
	protected void onGoalInit() {
		checkForBed = true;
	}

	@Override
	protected void onGoalFinish() {
		
	}

	@Override
	public boolean isDone() {
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof TravelOnHighwayWithBedBackupTask) {
			// TODO: Maybe add other conditions?
			return true;
		}
		return false;
	}

	private boolean shouldStack() {
		return stayStacked && AutoMC.getAutoMC().player.isInOverworld() && !stackedTask.isStacked();
	}

	private Task findBedSubTask(BlockPos startPos, double distanceFromHighway) {

		if (placeBedTask.isActive()) {
			return placeBedTask;
		}
		
		// We're going to the bed.
		if (goToBedTask.isActive()) {
			return goToBedTask;
		}

		// We're sleeping (great!) so just wait it out.
		if (AutoMC.getAutoMC().player.isSleeping()) {
			return null;
		}

		// If we want to check for a bed, check for it.
		if (checkForBed) {
			BlockPos nearestBed = getNearestBed();
			if (nearestBed == null) {
				checkForBed = false;
			} else {
				// We found a bed! Get to it.
				return goToBedTask;
			}
		}

		if (!AutoMC.getAutoMC().player.inventory.hasItem("bed")) {
			// We don't have a bed. Get the bed
			return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("bed", 1);
		} else {
			// We have a bed and can place it, but first go out far enough to place it.

			BlockPos p = AutoMC.getAutoMC().player.getOverworldPosition();
			if (axisDistance() < this.minDistanceToPlaceBed) {
				// We're not far enough, go out first.
				EnumFacing facing;
				switch (travelTask.getClosestAxis(p.getX(), p.getZ())) {
				case X:
					facing = EnumFacing.SOUTH;
					break;
				case Z:
					facing = EnumFacing.EAST;
					break;
				default:
					facing = EnumFacing.EAST; // This shouldn't ever happen.
				}
				return new WalkInDirectionTask(facing);
			} else {
				// We're far enough. Place the bed.
				checkForBed = true; // Will only check after placing bed is complete (which is what we want)

				return placeBedTask;
			}
		}
	}

	private double axisDistance() {
		BlockPos p = AutoMC.getAutoMC().player.getOverworldPosition();
		switch (travelTask.getClosestAxis(p.getX(), p.getZ())) {
		case X:
			return p.getZ();
		case Z:
			return p.getX();
		default:
			// Should never happen.
			Logger.logError("You forgot to add a case here.");
			return p.getDistance(0, 0, 0);
		}
	}

	private BlockPos getNearestBed() {
		CalculationContext context = new CalculationContext(AutoMC.getAutoMC().getBaritone());
		BlockOptionalMetaLookup bedSearch = new BlockOptionalMetaLookup(new BlockOptionalMeta("bed"));
		List<BlockPos> positions = new ArrayList<BlockPos>(1);
		positions = MineProcess.searchWorld(context, bedSearch, 1, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		if (positions.size() != 0) {
			return positions.get(0);
		}
		return null;
	}

}
