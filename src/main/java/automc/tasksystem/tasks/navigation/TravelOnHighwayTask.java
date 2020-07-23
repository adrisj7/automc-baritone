package automc.tasksystem.tasks.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import baritone.api.utils.BlockOptionalMeta;
import baritone.api.utils.BlockOptionalMetaLookup;
import baritone.pathing.movement.CalculationContext;
import baritone.process.MineProcess;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TravelOnHighwayTask extends TaskGoal {

	enum Axis {
		X,
		Z
	}

	enum TravelState {
		GetToHighway,		// Get to the highway first
		Highway,			// Travel along the highway
		SearchForExit,		// Searching for a portal to leave with
		LeavingHighway,		// We're trying to get out of the highway
		TravelOverworld		// You left the highway and are now going on foot.
	}

	TravelState state = TravelState.GetToHighway;
	int targetX;
	int targetZ;

	// Important positions on the highway
	int highwayStartX;
	int highwayStartZ;
	int highwayTargetX;
	int highwayTargetZ;

	// If we switch axis, we will go to 0,0 first in the nether. You should hope that doesn't result in yo death.
	boolean axisSwitched = false;

	// We'll search for portals and find the closest one.
	BlockPos closestNetherPortal;

	List<BlockPos> portalLocations = new ArrayList<BlockPos>();
	List<BlockPos> blacklist = new ArrayList<BlockPos>(); // Unused. Might use later?

	int checkPortalInterval = 20;
	int checkTimer = 0;

	BlockOptionalMetaLookup portalSearch;

	CalculationContext baritoneCalculationContext;

	// Whether we're standing in the portal
	boolean waitingInPortal = false;
	
	Task walkToHighwayTask = null;

	public TravelOnHighwayTask(int targetX, int targetZ) {
		this.targetX = targetX;
		this.targetZ = targetZ;

		baritoneCalculationContext = new CalculationContext(AutoMC.getAutoMC().getBaritone(), true);
		portalSearch = new BlockOptionalMetaLookup(new BlockOptionalMeta("portal"));
	}

	@Override
	protected Task getSubTask() {

		BlockPos p = Minecraft.getMinecraft().player.getPosition();

		int netherTargetX = highwayTargetX / 8;
		int netherTargetZ = highwayTargetZ / 8;

		switch (state) {
			case GetToHighway:
				if (isInNether()) {
					// When we're in the nether, start traveling along the highway.
					Logger.log(this,"Got to the nether, will now travel along the highway.");
					state = TravelState.Highway;
					closestNetherPortal = null;
					portalLocations.clear();
					waitingInPortal = false;
					return null;
				}

				// If we found a portal, go to it.
				if (closestNetherPortal != null) {
					return new GetToBlockTask(new BlockOptionalMeta("portal"));
				}

				// Look for portals occasionally
				if (checkTimer++ % checkPortalInterval == 0) {
					Logger.debug(this, "Portal Check...");
					checkForPortals();
				}

				// Walk to the highway
				int nearestHighwayX = 0,
					nearestHighwayZ = 0;
				switch (getClosestAxis(p.getX(), p.getZ())) {
				case X:
					nearestHighwayX = p.getX();
					nearestHighwayZ = 0;
					break;
				case Z:
					nearestHighwayX = 0;
					nearestHighwayZ = p.getZ();
					break;
				}

				double highwayDistSq = p.distanceSq(nearestHighwayX, p.getY(), nearestHighwayZ);
				if (walkToHighwayTask != null || highwayDistSq > 20 * 20) {
					
					if (highwayDistSq < 3*3) {
						Logger.debug(this, "close enough.");
						// If we're really close, move on to our next task.
						walkToHighwayTask = null;
						return null;
					} else {
						// If we're somewhat far away from the highway, go to it.
						if (walkToHighwayTask == null) {
							walkToHighwayTask = new TravelToXZTask(nearestHighwayX, nearestHighwayZ);
						}
						return walkToHighwayTask;
					}
				} else {
					EnumFacing facing;
					int deltaX = targetX - p.getX(),
						deltaZ = targetZ - p.getZ();
					switch (getClosestAxis(p.getX(), p.getZ())) {
					case X:
						facing = (deltaX > 0)? EnumFacing.EAST : EnumFacing.WEST;
						break;
					case Z:
						facing = (deltaZ > 0)? EnumFacing.SOUTH : EnumFacing.NORTH;
						break;
					default:
						Logger.logError("You shouldn't be here!");
						facing = EnumFacing.NORTH;
					}
					//Logger.log("aight: " + nearestHighwayX + ", " + nearestHighwayZ);
					// We're on the highway. Walk along it.
					return new WalkInDirectionTask(facing, new BlockPos(nearestHighwayX, p.getY(), nearestHighwayZ), true);
				}
				//break;
			case Highway:
				if (!isInNether()) {
					// If we're in the overworld again, we walk the rest on foot.
					Logger.log(this,"Left highway! Will walk the rest of the way.");
					state = TravelState.TravelOverworld;
					return null;
				}

				if (axisSwitched) {
					// We're trying to switch axis, we first go to zero.
					if (p.distanceSq(0, p.getY(), 0) < 100f * 100f) {
						// If we're close to zero zero, we no longer have to worry about having our axis switched.
						Logger.log(this, "At zero zero, will travel along our final axis.");
						axisSwitched = false;
					} else {
						return new TravelToXZTask(0, 0);
					}
				}

				// TODO: First travel a little further.
				if (p.distanceSq(netherTargetX, p.getY(), netherTargetZ) < 100f*100f) {

					// If we're close enough to our target, start searching for a portal.
					if (closestNetherPortal != null) {
						Logger.log(this,"Got close enough to target, searching for portal to leave with: " + closestNetherPortal.getX() + ", " + closestNetherPortal.getZ() + ": " + p.getX() + ", " + p.getZ());
					} else {
						Logger.log(this,"Got close enough to target, searching for portal to leave with: (player pos, did not find a closest portal yet)" + p.getX() + ", " + p.getZ());
					}
					state = TravelState.SearchForExit;
					return null;
				}

				// Search for portals
				if (checkTimer++ % checkPortalInterval == 0) {
					checkForPortals();
				}

				return new TravelToXZTask(netherTargetX, netherTargetZ);

			case SearchForExit:
				if (closestNetherPortal != null) {
					// If we will NOT find a closer portal by going further, call it quits.
					double nearestPortalToTargetDistSq = closestNetherPortal.distanceSq(netherTargetX, p.getY(), netherTargetZ);
					double distSqFromTarget = p.distanceSq(netherTargetX, p.getY(), netherTargetZ);
					//Logger.log("o boi: " + distSqFromTarget + " < " + nearestPortalToTargetDistSq);
					if (distSqFromTarget > nearestPortalToTargetDistSq) {
						// Call it quits.
						Logger.log(this, "Found the closest portal, now going to that portal.");
						state = TravelState.LeavingHighway;
						return null;
					}
				} else {
					// Search for portals if we don't have one.
					if (checkTimer++ % checkPortalInterval == 0) {
						checkForPortals();
					}
				}
				// Keep going

				// TODO: Make this part more robust. Pick a direction (as a vector) and travel in that direction.
				// TODO: Make a new baritone goal and use that in a new task to follow.
				int newX = netherTargetX * 3,
					newY = netherTargetZ * 3;
				return new TravelToXZTask(newX, newY); 
			case LeavingHighway:
				if (!isInNether()) {
					Logger.log(this,"Left highway!");
					// If we're in the overworld again, we walk the rest on foot.
					state = TravelState.TravelOverworld;
					return null;
				}

				if (closestNetherPortal == null) {
					// We're shit out of luck but will keep moving forward and try to get there anyways.
					// Keep going

					// Search for portals
					if (checkTimer++ % checkPortalInterval == 0) {
						checkForPortals();
					}

					int newX1 = netherTargetX * 3,
						newY1 = netherTargetZ * 3;
					return new TravelToXZTask(newX1, newY1); 
					//return new GetToBlockTask(portalSearch.blocks().get(0));
				}
				if (p.distanceSq(closestNetherPortal) < 5*5) {
					// We're pretty dang close.
					return new GetToBlockTask(portalSearch.blocks().get(0));
				}
				// We're getting to the closest portal.
				return new TravelToPositionTask(closestNetherPortal);

			case TravelOverworld:
				return new TravelToXZTask(targetX, targetZ);
		}

		return null;
	}

	@Override
	protected void onGoalInit() {
		state = TravelState.GetToHighway;

		BlockPos p = Minecraft.getMinecraft().player.getPosition();

		portalLocations = new ArrayList<BlockPos>();
		blacklist = new ArrayList<BlockPos>(); // Might want to remove this
		checkTimer = 0;
		closestNetherPortal = null;
		waitingInPortal = false;

		Axis startAxis = getClosestAxis(p.getX(), p.getZ());
		Axis endAxis = getClosestAxis(targetX, targetZ);
		switch (startAxis) {
		case X:
			// Go to x highway
			highwayStartX = p.getX();
			highwayStartZ = 0;
			break;
		case Z:
			// Go to the Z highway
			highwayStartX = 0;
			highwayStartZ = p.getZ();
		}
		switch (endAxis) {
		case X:
			// Finish on X highway
			highwayTargetX = targetX;
			highwayTargetZ = 0;
			break;
		case Z:
			// Finish on Z highway
			highwayTargetX = 0;
			highwayTargetZ = targetZ;
		}

		// If we switch axis, we have to go to zero zero first.
		axisSwitched = (startAxis != endAxis);
	}

	@Override
	protected void onGoalFinish() {
		// That's it.
	}

	@Override
	public boolean isDone() {
		if (isInNether()) return false;
		BlockPos p = Minecraft.getMinecraft().player.getPosition();
		return p.distanceSq(targetX, p.getY(), targetZ) < 10*10;
	}

	@Override
	protected boolean areConditionsMet() {
		// We can always travel.
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof TravelOnHighwayTask) {
			TravelOnHighwayTask toht = (TravelOnHighwayTask) t;
			return toht.targetX == targetX && toht.targetZ == targetZ;
		}
		return false;
	}

	private boolean isInNether() {
		return Minecraft.getMinecraft().player.dimension == -1;
	}

	public Axis getClosestAxis(int x, int z) {
		if (Math.abs(z) < Math.abs(x)) {
			// We're closer to X
			return Axis.X;
		} else {
			// We're closer to Z
			return Axis.Z;
		}
	}

	public void checkForPortals() {
		CalculationContext context = new CalculationContext(AutoMC.getAutoMC().getBaritone());
		portalLocations.clear();
		portalLocations = MineProcess.searchWorld(context, portalSearch, 64, Collections.emptyList(), blacklist, Collections.emptyList());
		// TODO: The following check is kinda useless but oh whell
		if (portalLocations == null) portalLocations = new ArrayList<BlockPos>(); 

		int netherTargetX = highwayTargetX / 8;
		int netherTargetZ = highwayTargetZ / 8;
		double targetScore = (closestNetherPortal != null)? closestNetherPortal.distanceSq(netherTargetX, closestNetherPortal.getY(), netherTargetZ) : Double.POSITIVE_INFINITY;

		Logger.log(this, "ok");
		for (BlockPos pos : portalLocations) {
			Logger.log(this, "Found portal: " + pos);
			double score = pos.distanceSq(netherTargetX, pos.getY(), netherTargetZ);
			if (score < targetScore) {
				targetScore = score;
				closestNetherPortal = pos;
				Logger.log(this, "Found new closest portal: " + pos.getX() + ", " + pos.getZ() + " going to " + netherTargetX + ", " + netherTargetZ);
			}
		}
	}

	public void resetClosestPortal() {
		closestNetherPortal = null;
	}
	public BlockPos getClosestPortal() {
		return closestNetherPortal;
	}
	public void leaveNether(boolean resetSearch) {
		if (resetSearch) {
			// Reset our closest nether portal and find a new one.
			closestNetherPortal = null;
		}
		state = TravelState.LeavingHighway;
	}
	public void reset() {
		onGoalInit();
	}
}
