package automc.baritone;

import java.util.Optional;

import automc.Logger;
import baritone.Baritone;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalGetToBlock;
import baritone.api.pathing.goals.GoalTwoBlocks;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import baritone.utils.BaritoneProcessHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.util.math.BlockPos;

/**
 * A baritone process that interacts with the target block.
 * It's copied mostly from "GetToBlockProcess" with some help from "CustomGoalProcess"
 */
public class InteractWithGoalBlockProcess extends BaritoneProcessHelper {

    private BlockPos gettingTo;
    private BlockPos start;

    /**
     * The current goal
     */
    private Goal goal;
    /**
     * The current process state.
     *
     * @see State
     */
    private State state;

    private int tickCount = 0;
    private int arrivalTickCount = 0;

	public InteractWithGoalBlockProcess(Baritone baritone) {
		super(baritone);
	}

	public void interactWith(BlockPos pos) {
		onLostControl();
		this.gettingTo = pos;
		this.goal = new GoalGetToBlock(pos);
		this.state = State.PATH_REQUESTED;
		arrivalTickCount = 0;
	}
	
    @Override
    public boolean isActive() {
        return gettingTo != null;
    }

    @Override
    public synchronized PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel) {

    	switch (this.state) {
	        case GOAL_SET:
	            return new PathingCommand(this.goal, PathingCommandType.CANCEL_AND_SET_GOAL);
	        case PATH_REQUESTED:
	            // return FORCE_REVALIDATE_GOAL_AND_PATH just once
	            PathingCommand ret = new PathingCommand(this.goal, PathingCommandType.FORCE_REVALIDATE_GOAL_AND_PATH);
	            this.state = State.EXECUTING;
	            return ret;
	        case EXECUTING:
	            if (calcFailed) {
	                onLostControl();
	                return new PathingCommand(this.goal, PathingCommandType.CANCEL_AND_SET_GOAL);
	            }
	            if ((goal.isInGoal(ctx.playerFeet()) && goal.isInGoal(baritone.getPathingBehavior().pathStart()) && isSafeToCancel)) {
	            	Logger.log("oof 1");
	                // we're there xd
	                if (rightClick()) {
		            	Logger.log("oof 2");
	                    onLostControl();
	                    return new PathingCommand(null, PathingCommandType.CANCEL_AND_SET_GOAL);
	                }
	            }
	            /*
	            if (this.goal == null || (this.goal.isInGoal(ctx.playerFeet()) && this.goal.isInGoal(baritone.getPathingBehavior().pathStart()))) {
	                onLostControl(); // we're there xd
	                if (Baritone.settings().disconnectOnArrival.value) {
	                    ctx.world().sendQuittingDisconnectingPacket();
	                }
	                return new PathingCommand(this.goal, PathingCommandType.CANCEL_AND_SET_GOAL);
	            }
	            */
	            return new PathingCommand(goal, PathingCommandType.REVALIDATE_GOAL_AND_PATH);
	            //return new PathingCommand(this.goal, PathingCommandType.SET_GOAL_AND_PATH);
	        default:
	            throw new IllegalStateException();
	    }
        //Goal goal = new GoalComposite(knownLocations.stream().map(this::createGoal).toArray(Goal[]::new));
        //int mineGoalUpdateInterval = Baritone.settings().mineGoalUpdateInterval.value;
        /*
        if (mineGoalUpdateInterval != 0 && tickCount++ % mineGoalUpdateInterval == 0) { // big brain
            List<BlockPos> current = new ArrayList<>(knownLocations);
            CalculationContext context = new CalculationContext(baritone, true);
            Baritone.getExecutor().execute(() -> rescan(current, context));
        }
        */
    }


    /*
    // blacklist the closest block and its adjacent blocks
    public synchronized boolean blacklistClosest() {
        List<BlockPos> newBlacklist = new ArrayList<>();
        knownLocations.stream().min(Comparator.comparingDouble(ctx.player()::getDistanceSq)).ifPresent(newBlacklist::add);
        outer:
        while (true) {
            for (BlockPos known : knownLocations) {
                for (BlockPos blacklist : newBlacklist) {
                    if (areAdjacent(known, blacklist)) { // directly adjacent
                        newBlacklist.add(known);
                        knownLocations.remove(known);
                        continue outer;
                    }
                }
            }
            // i can't do break; (codacy gets mad), and i can't do if(true){break}; (codacy gets mad)
            // so i will do this
            switch (newBlacklist.size()) {
                default:
                    break outer;
            }
        }
        logDebug("Blacklisting unreachable locations " + newBlacklist);
        blacklist.addAll(newBlacklist);
        return !newBlacklist.isEmpty();
    }
    */

    // safer than direct double comparison from distanceSq
    private boolean areAdjacent(BlockPos posA, BlockPos posB) {
        int diffX = Math.abs(posA.getX() - posB.getX());
        int diffY = Math.abs(posA.getY() - posB.getY());
        int diffZ = Math.abs(posA.getZ() - posB.getZ());
        return (diffX + diffY + diffZ) == 1;
    }

    @Override
    public synchronized void onLostControl() {
        gettingTo = null;
        start = null;
        this.state = State.NONE;
        this.goal = null;
        baritone.getInputOverrideHandler().clearAllKeys();
    }

    @Override
    public String displayName0() {
        return "Interacting with " + gettingTo + ".";
    }


    private Goal createGoal(BlockPos pos) {
        if (walkIntoInsteadOfAdjacent(gettingTo)) {
            return new GoalTwoBlocks(pos);
        }
        if (blockOnTopMustBeRemoved(gettingTo) && baritone.bsi.get0(pos.up()).isBlockNormalCube()) {
            return new GoalBlock(pos.up());
        }
        return new GoalGetToBlock(pos);
    }


	///// COPIED FROM GET TO BLOCK PROCESS....... ////
    private boolean rightClick() {
        Optional<Rotation> reachable = RotationUtils.reachable(ctx.player(), gettingTo, ctx.playerController().getBlockReachDistance());
        if (reachable.isPresent()) {
            baritone.getLookBehavior().updateTarget(reachable.get(), true);
            if (this.gettingTo.equals(ctx.getSelectedBlock().orElse(null))) {
                baritone.getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, true); // TODO find some way to right click even if we're in an ESC menu
                System.out.println(ctx.player().openContainer);
                if (!(ctx.player().openContainer instanceof ContainerPlayer)) {
                    return true;
                }
            }
            if (arrivalTickCount++ > 20) {
                logDirect("Right click timed out");
                return true;
            }
            return false; // trying to right click, will do it next tick or so
        }
        logDirect("Arrived but failed to interact with target block.");
        return true;
    }

    private boolean walkIntoInsteadOfAdjacent(BlockPos pos) {
        if (!Baritone.settings().enterPortal.value) {
            return false;
        }
        Block block = getBlockAt(pos);
        return block == Blocks.PORTAL;
    }


    private boolean blockOnTopMustBeRemoved(BlockPos pos) {
        Block block = getBlockAt(pos);
        // only these chests; you can open a crafting table or furnace even with a block on top
        return block == Blocks.ENDER_CHEST || block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST;
    }
    
    
    private Block getBlockAt(BlockPos pos) {
    	return Minecraft.getMinecraft().world.getBlockState(pos).getBlock();
    }

    // Copied from custom goal process...
    protected enum State {
        NONE,
        GOAL_SET,
        PATH_REQUESTED,
        EXECUTING
    }
}
