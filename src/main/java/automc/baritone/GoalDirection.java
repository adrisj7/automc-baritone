package automc.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.api.utils.SettingsUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class GoalDirection implements Goal {

	public final int x;
    public final int y;
	public final int z;
	public final int dx;
	public final int dz;

	private boolean penalizeDeviation;
	
    public GoalDirection(BlockPos origin, EnumFacing direction, boolean penalizeDeviation) {
        x = origin.getX();
        y = origin.getY();
        z = origin.getZ();
        dx = direction.getXOffset();
        dz = direction.getZOffset();
        this.penalizeDeviation = penalizeDeviation;
        if (dx == 0 && dz == 0) {
            throw new IllegalArgumentException(direction + "");
        }
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        return false;
    }

    @Override
    public double heuristic(int x, int y, int z) {
        int distanceFromStartInDesiredDirection = (x - this.x) * dx + (z - this.z) * dz;
        int distanceFromStartInIncorrectDirection = Math.abs((x - this.x) * dz) + Math.abs((z - this.z) * dx);

        // we want heuristic to decrease as desiredDirection increases
        double heuristic = -distanceFromStartInDesiredDirection * 1000;

        if (penalizeDeviation) {
        	heuristic += distanceFromStartInIncorrectDirection * 100;
        }
        //heuristic += verticalDistanceFromStart * 1000;
        return heuristic;
    }

    @Override
    public String toString() {
        return String.format(
                "GoalDirection{x=%s, y=%s, z=%s, dx=%s, dz=%s}",
                SettingsUtil.maybeCensor(x),
                SettingsUtil.maybeCensor(y),
                SettingsUtil.maybeCensor(z),
                SettingsUtil.maybeCensor(dx),
                SettingsUtil.maybeCensor(dz)
        );
    }

}
