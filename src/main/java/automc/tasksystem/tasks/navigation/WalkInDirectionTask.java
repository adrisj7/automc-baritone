package automc.tasksystem.tasks.navigation;

import automc.AutoMC;
import automc.Logger;
import automc.baritone.GoalDirection;
import automc.tasksystem.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class WalkInDirectionTask extends CustomGoalTask {

	private EnumFacing facing;

	private BlockPos origin = null;
	
	private boolean penalizeDeviance;

	public WalkInDirectionTask(EnumFacing facing, BlockPos origin, boolean penalizeDeviance) {
		this.facing = facing;
		this.origin = origin;
		this.penalizeDeviance = penalizeDeviance;
	}

	public WalkInDirectionTask(EnumFacing facing) {
		this(facing, null, false);
	}
	public WalkInDirectionTask(EnumFacing facing, boolean penalizeDeviance) {
		this(facing, null, penalizeDeviance);
	}

	@Override
	protected void onInit() {
		BlockPos startOrigin = origin;
		if (startOrigin == null) {
			startOrigin = Minecraft.getMinecraft().player.getPosition();
		}
		AutoMC.getAutoMC().getBaritone().getCustomGoalProcess().setGoalAndPath(new GoalDirection(startOrigin, facing, penalizeDeviance));
		
		Logger.log("Direction Walk Init");
	}


	@Override
	public boolean areEqual(Task t) {
		if (t instanceof WalkInDirectionTask) {
			WalkInDirectionTask widt = (WalkInDirectionTask) t;
			return widt.facing == facing;
		}
		return false;
	}	
}
