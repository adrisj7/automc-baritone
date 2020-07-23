package automc.tasksystem.tasks.building;

import automc.AutoMC;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ProceduralStaticSchematic;
import baritone.Baritone;
import net.minecraft.init.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class BuildNetherPortalTask extends TaskGoal {

	private static ProceduralStaticSchematic NETHER_SCHEMATIC;
	static {
		String o = "obsidian",
			   a = "air";
		NETHER_SCHEMATIC = new ProceduralStaticSchematic(4,5,1);
		// Literally hard code in build the nether portal. I'm not kidding.
		// Frame
		NETHER_SCHEMATIC.setDirect(1, 0, 0, o);
		NETHER_SCHEMATIC.setDirect(2, 0, 0, o);
		NETHER_SCHEMATIC.setDirect(0, 1, 0, o);
		NETHER_SCHEMATIC.setDirect(0, 2, 0, o);
		NETHER_SCHEMATIC.setDirect(0, 3, 0, o);
		NETHER_SCHEMATIC.setDirect(1, 4, 0, o);
		NETHER_SCHEMATIC.setDirect(2, 4, 0, o);
		NETHER_SCHEMATIC.setDirect(3, 1, 0, o);
		NETHER_SCHEMATIC.setDirect(3, 2, 0, o);
		NETHER_SCHEMATIC.setDirect(3, 3, 0, o);
		// Air in the middle
		NETHER_SCHEMATIC.setDirect(1, 1, 0, a);
		NETHER_SCHEMATIC.setDirect(1, 2, 0, a);
		NETHER_SCHEMATIC.setDirect(1, 3, 0, a);
		NETHER_SCHEMATIC.setDirect(2, 1, 0, a);
		NETHER_SCHEMATIC.setDirect(2, 2, 0, a);
		NETHER_SCHEMATIC.setDirect(2, 3, 0, a);
		// Fire
		//NETHER_SCHEMATIC.setDirect(1, 1, 0, Blocks.FIRE.getDefaultState());
	}

	private boolean built = false;
	BuildStaticSchematicTask buildTask;

	@Override
	protected Task getSubTask() {
		// TODO: Get flint&steel.

		if (!AutoMC.getAutoMC().player.inventory.hasItem("flint_and_steel")) {
			return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("flint_and_steel", 1);
		}

		// If we tried building.
		if (!built) {
			if (buildTask != null) {
				// Keep building.
				if (buildTask.isActive()) {
					log("BUILD");
					return buildTask;				
				} else {
					// We finished building.
					log("FINISHED BUILDING");
					built = true;
				}
			}
		}

		if (built && !buildTask.buildFailed()) {
			// No breaking now.
			Baritone.settings().allowBreak.value = false;
			Baritone.settings().maxFallHeightNoWater.value = 4; // Allow some heavier falling, just this once.
			//PathingBehavior p = (PathingBehavior) AutoMC.getAutoMC().getBaritone().getPathingBehavior();
			//p.secretInternalGetCalculationContext().allowBreak = false;

			// Fire!
			if (AutoMC.getAutoMC().customBaritone.getInteractWithGoalBlockProcess().isActive()) {
				log("flameing");
				AutoMC.getAutoMC().player.inventory.equipItem(Items.FLINT_AND_STEEL);
				return null;
			} else {
				log("flame it");
				// Flame it.
				Vec3i target = buildTask.getBuildOrigin();
				target = new Vec3i(target.getX() + 1, target.getY(), target.getZ());
				AutoMC.getAutoMC().customBaritone.getInteractWithGoalBlockProcess().interactWith(new BlockPos(target));
			}
		} else {
			// We BUILD!
			buildTask = new BuildStaticSchematicTask("nether_portal", NETHER_SCHEMATIC);
			return buildTask;
		}
		return null;
	}

	@Override
	protected void onGoalInit() {
		built = false;
		buildTask = null;
	}

	@Override
	protected void onGoalFinish() {
		Baritone.settings().allowBreak.value = true;
		Baritone.settings().maxFallHeightNoWater.value = 3;
		// Stop interact with block process.
		AutoMC.getAutoMC().customBaritone.getInteractWithGoalBlockProcess().onLostControl();
	}

	@Override
	public boolean isDone() {
		// TODO: Search for portal frame.
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		// Assume they can always be met.
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		return t instanceof BuildNetherPortalTask;
	}

}
