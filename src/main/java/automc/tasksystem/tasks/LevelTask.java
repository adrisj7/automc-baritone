package automc.tasksystem.tasks;

import automc.tasksystem.TaskGoal;
import net.minecraft.client.Minecraft;

public abstract class LevelTask extends TaskGoal {

	protected int targetLevel;

	public LevelTask(int targetLevel) {
		this.targetLevel = targetLevel;
	}

	@Override
	public boolean isDone() {
		return Minecraft.getMinecraft().player.experienceLevel >= targetLevel;
	}
}
