package automc.tasksystem.tasks;

import java.util.List;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.ContainerType;
import automc.definitions.LoopState;
import automc.player.Inventory;
import automc.tasksystem.Task;
import automc.utility.ItemUtil;
import automc.utility.SingleBlockSchematic;
import baritone.api.schematic.ISchematic;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlaceBlockNearbyTask extends Task {

	// Move this many blocks after being stuck to "unstuck" yourself.
	private static final double STUCK_DISTANCE_THRESHOLD = 3;
	// How long we'd have to wait before we're "stuck"
	private static final int STUCK_TIME = 20;

	Block toPlace;

	ISchematic schematic;

	int stuckTimer = 0;
	Vec3d stuckStartPos;

	public PlaceBlockNearbyTask(Item item) {
		toPlace = Block.getBlockFromItem(item);
		schematic = new SingleBlockSchematic(toPlace);
	}
	public PlaceBlockNearbyTask(String item) {
		this(ItemUtil.getItem(item));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PlaceBlockNearbyTask) {
			return ((PlaceBlockNearbyTask)obj).toPlace.equals(toPlace);
		}
		return super.equals(obj);
	}
	
	@Override
	protected void onInit() {
		initTask();
		AutoMC.getAutoMC().player.pathDisabler.enableBlock(toPlace);
	}

	void initTask() {
		AutoMC.getAutoMC().getBaritone().getExploreProcess().onLostControl();

		AutoMC.getAutoMC().player.inventory.equipItem(Item.getItemFromBlock(toPlace));

		// Place block where the player was standing.
		BlockPos origin = Minecraft.getMinecraft().player.getPosition();
		AutoMC.getAutoMC().getBaritone().getBuilderProcess().build("Single Block: " + toPlace.getLocalizedName(), schematic, origin);
		stuckTimer = 0;
	}

	@Override
	protected void onTick(LoopState state) {

		// To determine if we're stuck, we need to make sure we're not moving for a certain period of time.
		if (!AutoMC.getAutoMC().getBaritone().getPathingBehavior().isPathing()) {
			++stuckTimer;
		}
		//Minecraft.getMinecraft().player.moveForward

		// Handle being stuck
		if (isStuck()) {
			if (AutoMC.getAutoMC().getBaritone().getBuilderProcess().isActive()) {
				log("We got stuck. Trying to move around and get ourselves unstuck.");
				// Cancel the build process and start the explore process.
				AutoMC.getAutoMC().getBaritone().getBuilderProcess().onLostControl();
				BlockPos p = Minecraft.getMinecraft().player.getPosition();
				AutoMC.getAutoMC().getBaritone().getExploreProcess().explore(p.getX(), p.getZ());
				stuckStartPos = Minecraft.getMinecraft().player.getPositionVector();
			} else {
				// Check for whether we're far enough
				double distSqr = stuckStartPos.squareDistanceTo(Minecraft.getMinecraft().player.getPositionVector());
				if (distSqr > STUCK_DISTANCE_THRESHOLD*STUCK_DISTANCE_THRESHOLD) {
					log("Unstuck. Trying to place block again.");
					// If we are far enough, try again.
					initTask();
				}
			}
		}
	}

	@Override
	public boolean isDone() {
		// We're done when we stop building.
		return !isStuck() && !AutoMC.getAutoMC().getBaritone().getBuilderProcess().isActive();
	}

	@Override
	protected void onFinish() {
		AutoMC.getAutoMC().getBaritone().getBuilderProcess().onLostControl();
		AutoMC.getAutoMC().player.pathDisabler.enableBlock(toPlace);
	}

	@Override
	protected boolean areConditionsMet() {
		// We must have the block to place it.
		return AutoMC.getAutoMC().player.inventory.hasItem(Item.getItemFromBlock(toPlace));
	}

	@Override
	protected void onInterrupt() {
		stop();
	}
	
	@Override
	public boolean areEqual(Task t) {
		if (!(t instanceof PlaceBlockNearbyTask)) return false;
		return toPlace.equals(((PlaceBlockNearbyTask) t).toPlace);
	}
	
	private boolean isStuck() {
		return stuckTimer > STUCK_TIME;
	}

}
