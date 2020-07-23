package automc.tasksystem.tasks.resources;

import java.util.HashMap;
import java.util.Map;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.tasksystem.tasks.CraftRecipeInTableTask;
import automc.tasksystem.tasks.SmeltInFurnaceTask;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.ItemFood;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings("rawtypes")
public class CollectFoodTask extends TaskGoal {

	private int targetHungerFill;

	private Map<String, String> rawFoods;
	private Map<Class, String> foodAnimals;

	private Task killTask = null;

	private Task cookingRawFoodTask = null;

	public CollectFoodTask(int targetHungerFill) {
		this.targetHungerFill = targetHungerFill;
		rawFoods = new HashMap<String, String>();
		foodAnimals = new HashMap<>();
		addRaw(
				"porkchop", "cooked_porkchop",
				"fish", "cooked_fish",
				"beef", "cooked_beef",
				"chicken", "cooked_chicken",
				"rabbit", "cooked_rabbit",
				"mutton", "cooked_mutton",
				"potato", "baked_potato"
		);
		foodAnimals.put(EntityPig.class, "porkchop");
		foodAnimals.put(EntitySheep.class, "mutton");
		foodAnimals.put(EntityCow.class, "beef");
		foodAnimals.put(EntityChicken.class, "chicken");
	}

	@Override
	protected Task getSubTask() {
		// TODO: Try collecting food through a variety of means.

		if (killTask != null && killTask.isActive()) {
			return killTask;
		}

		if (cookingRawFoodTask != null && cookingRawFoodTask.isActive()) {
			return cookingRawFoodTask;
		}

		// If we have wheat, craft bread.
		if (AutoMC.getAutoMC().player.inventory.getItemCount("wheat") >= 3) {
			return new CraftRecipeInTableTask(ItemUtil.createPureShapedRecipe(3, 1, "wheat", "wheat", "wheat"));
		}
		// If we have raw food, cook it.
		for (String raw : rawFoods.keySet()) {
			if (AutoMC.getAutoMC().player.inventory.hasItem(raw)) {
				String cooked = rawFoods.get(raw);
				int count = AutoMC.getAutoMC().player.inventory.getItemCount(raw) + AutoMC.getAutoMC().player.inventory.getItemCount(cooked);
				cookingRawFoodTask = new SmeltInFurnaceTask(raw, cooked, count);
				return cookingRawFoodTask;
			}
		}
		// TODO: If farm is nearby, run farm task until we can't.

		Entity player = Minecraft.getMinecraft().player;

		// -> If animals are nearby, doof em
		EntityAnimal nearestAnimal = null;
		double nearestSqDistance = Double.POSITIVE_INFINITY;
		for(EntityAnimal animal : AutoMC.getAutoMC().entityScanner.getAnimals()) {
			if (foodAnimals.containsKey(animal.getClass())) {
				double sqDist = player.getDistanceSq(animal);
				if (sqDist < nearestSqDistance) {
					nearestSqDistance = sqDist;
					nearestAnimal = animal;
				}
			}
		}
		if (nearestAnimal != null) {
			int currentFill = AutoMC.getAutoMC().player.inventory.getTotalFoodHungerHealAmount();
			int needed = targetHungerFill - currentFill;
			String drop = foodAnimals.get(nearestAnimal.getClass());
			String cooked = rawFoods.containsKey(drop)? rawFoods.get(drop) : drop;
			double healPerCooked = ((ItemFood) ItemUtil.getItem(cooked)).getHealAmount(null);
			killTask = new KillAndGrabLootTask(nearestAnimal, drop, (int) Math.ceil(needed / healPerCooked));
			return killTask;
		}

		if (!isExploring()) {
			Logger.log(this, "Could not find food. Should explore I guess.");
			BlockPos p = Minecraft.getMinecraft().player.getPosition();
			AutoMC.getAutoMC().getBaritone().getExploreProcess().explore(p.getX(), p.getY());
		}
		return null;
	}

	@Override
	protected void onGoalInit() {
		cookingRawFoodTask = null;
		killTask = null;
	}

	@Override
	protected void onGoalFinish() {		
	}

	@Override
	public boolean isDone() {
		return !needsFood();
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof CollectFoodTask) {
			return targetHungerFill == ((CollectFoodTask)t).targetHungerFill;
		}
		return false;
	}

	public boolean needsFood() {
		EntityPlayerSP p = Minecraft.getMinecraft().player;
		int hunger = p.getFoodStats().getFoodLevel();
//		float sat =    p.getFoodStats().getSaturationLevel();

		// If we're full and mostly healthy, we're good for now.
		if (hunger > 16 && p.getHealth() > 17) {
			return false;
		}

		if (hasRawFood()) {
			return true;
		}

		return AutoMC.getAutoMC().player.inventory.getTotalFoodHungerHealAmount() < targetHungerFill;
	}

	private boolean hasRawFood() {
		if (cookingRawFoodTask != null && cookingRawFoodTask.isActive()) return true;
		for (String raw : rawFoods.keySet()) {
			if (AutoMC.getAutoMC().player.inventory.hasItem(raw)) {
				return true;
			}
		}
		return false;
	}
	
	private void addRaw(String ...foods) {
		if (foods.length % 2 != 0) {
			Logger.logError("Tried to intitialize an invalid pairing of raw to cooked foods...");
		}
		for (int i = 0; i < foods.length; i += 2) {
			String raw = foods[i],
				   cooked = foods[i+1];
			rawFoods.put(raw, cooked);
		}
	}

	public boolean isExploring() {
		return AutoMC.getAutoMC().getBaritone().getExploreProcess().isActive();
	}

}
