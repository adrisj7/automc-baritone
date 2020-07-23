package automc.tasksystem.tasks.misc;

import automc.tasksystem.Task;
import automc.tasksystem.tasks.SurviveTask;
import automc.tasksystem.tasks.navigation.TravelToPositionTask;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3i;

/**
 * Let's see how this bot fairs
 * 
 * on the oldest anarchy server in blocc game
 */
public class SurviveAnarchyTask extends SurviveTask {

	// These determine the spawn and where we chose to go to LEAVE spawn.
	private int spawnRadius = 0;
	private Vec2f awayFromSpawnTarget;

	public SurviveAnarchyTask(int spawnRadius, double travelAngle, Task mainTask) {
		super(mainTask);
		this.spawnRadius = spawnRadius;

		double radAngle = Math.toDegrees(travelAngle);

		awayFromSpawnTarget = new Vec2f((float)(spawnRadius * Math.cos(radAngle)), (float)(spawnRadius * Math.sin(radAngle)));
	}

	protected boolean canSearchForFood() {
		return super.canSearchForFood() && isFarFromOrigin(3000);
	}
	protected Task toDoInsteadOfExploring() {
		if (!isAwayFromSpawn()) {
			return new TravelToPositionTask(new BlockPos(awayFromSpawnTarget.x, 64, awayFromSpawnTarget.y));
		}
		return null;
	}

	private boolean isAwayFromSpawn() {
		return isFarFromOrigin(spawnRadius);
	}

	private boolean isFarFromOrigin(double distance) {
		Vec3i delta = Minecraft.getMinecraft().player.getPosition();
		double magSqr = delta.getX()*delta.getX() + delta.getZ()*delta.getZ();
		return magSqr > distance*distance;
	}

}
