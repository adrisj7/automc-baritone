package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.Logger;
import automc.combat.Killer;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.ResourceTask;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;

@SuppressWarnings("rawtypes")
public class KillAndGrabLootTask extends ResourceTask {

	private EntityLivingBase target = null;
	private Class targetClass = null;

	// Good lord these constructors are way too verbose
	public KillAndGrabLootTask(EntityLivingBase target, Item[] targetItems, int[] requiredAmounts) {
		super(targetItems, requiredAmounts);
		this.target = target;
	}
	public KillAndGrabLootTask(EntityLivingBase target, String targetItem, int requiredAmount) {
		super(targetItem, requiredAmount);
		this.target = target;
	}
	public KillAndGrabLootTask(EntityLivingBase target, String[] targetItems, int[] requiredAmounts) {
		super(targetItems, requiredAmounts);
		this.target = target;
	}

	public KillAndGrabLootTask(Class targetClass, Item[] targetItems, int[] requiredAmounts) {
		super(targetItems, requiredAmounts);
		this.targetClass = targetClass;
	}
	public KillAndGrabLootTask(Class targetClass, String targetItem, int requiredAmount) {
		super(targetItem, requiredAmount);
		this.targetClass = targetClass;
	}
	public KillAndGrabLootTask(Class targetClass, String[] targetItems, int[] requiredAmounts) {
		super(targetItems, requiredAmounts);
		this.targetClass = targetClass;
	}


	// When we're not grabbing loot from the floor or chests, kill the target.
	@Override
	protected Task getResourceSubTask() {
		Killer killer = AutoMC.getAutoMC().combatRunner.killer;

		//Logger.log("killin & chillin");

		if (target != null) {
			killer.kill(target);
		} else if (targetClass != null) {
			killer.killNearest(targetClass);
		}

		return null;
	}

	@Override
	protected void onResourceGoalInit() {
		
	}

	@Override
	protected void onResourceGoalFinish() {
		AutoMC.getAutoMC().combatRunner.killer.stop();
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	protected void onResourceFoundAnotherWay() {
		Logger.log("pickin up");
		// If we're getting our resources some other way, stop killing.
		Killer killer = AutoMC.getAutoMC().combatRunner.killer;
		if (killer.isAttacking()) {
			killer.stop();
		}
	}
}
