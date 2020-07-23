package automc.tasksystem.tasks.misc;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.TaskGoal;
import automc.utility.ItemUtil;

// Gets full diamond armor, a sword & shield and equips them.
public class GetStackedTask extends TaskGoal {

	private boolean wentToDaMines = false;

	@Override
	protected Task getSubTask() {
		//DebugBreaker.breakNow();

		//Logger.log("?????");
		
		/*
		if (!wentToDaMines) {
			if (!has("crafting_table")) {
				return get("crafting_table");
			}
		}
		*/

		if (!has("shield")) {
			return get("shield");
		}
		if (!wearingShield("shield")) {
			equipShield("shield");
		}

		// Bring a crafting table along with you.

		if (!has("diamond_pickaxe")) {
			wentToDaMines = true;
			return get("diamond_pickaxe");
		}

		int diamondsNeeded = getDiamondsNeeded(8 + 7 + 5 + 4 + 2);
		if (!has("diamond", diamondsNeeded)) {
			return get("diamond", diamondsNeeded);
		}

		if (!has("diamond_chestplate")) {
			return get("diamond_chestplate");
		}

		if (!wearing("diamond_chestplate")) {
			equip("diamond_chestplate", 1);
			return null;
		}

		if (!has("diamond_leggings")) {
			return get("diamond_leggings");
		}
		
		if (!wearing("diamond_leggings")) {
			equip("diamond_leggings", 1);
			return null;
		}

		if (!has("diamond_helmet")) {
			return get("diamond_helmet");
		}
		
		if (!wearing("diamond_helmet")) {
			equip("diamond_helmet", 0);
			return null;
		}

		if (!has("diamond_boots")) {
			return get("diamond_boots");
		}
		
		if (!wearing("diamond_boots")) {
			equip("diamond_boots", 3);
			return null;
		}

		if (!has("diamond_sword")) {
			return get("diamond_sword");
		}

		stop();
		return null;
	}

	@Override
	protected void onGoalInit() {
		// TODO Auto-generated method stub
		wentToDaMines = false;
	}

	@Override
	protected void onGoalFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		// We can always do it.
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof GetStackedTask) {
			return true;
		}
		return false;
	}

	private boolean wearing(String item) {
		return AutoMC.getAutoMC().player.inventory.isArmorEquipped(item);
	}

	private boolean has(String item) {
		return AutoMC.getAutoMC().player.inventory.hasItem(item) || wearing(item);
	}
	private boolean has(String item, int count) {
		return AutoMC.getAutoMC().player.inventory.getItemCount(item) >= count;
	}
	private Task get(String item, int count) {
		return AutoMC.getAutoMC().itemTaskCatalogue.getItemTask(item, count);
	}
	private Task get(String item) {
		return get(item, 1);
	}

	private boolean wearingShield(String shield) {
		return AutoMC.getAutoMC().player.inventory.getShieldStack().getItem().equals(ItemUtil.getItem(shield));
	}
	private void equipShield(String shield) {
		AutoMC.getAutoMC().player.inventory.equipAsShield(ItemUtil.getItem(shield));
	}

	private void equip(String item, int armorSlot) {
		AutoMC.getAutoMC().player.inventory.equipArmor(item, armorSlot);
	}

	private int getDiamondsNeeded(int total) {
		return total - (has("diamond_chestplate")? 8 : 0) - (has("diamond_leggings")? 7 : 0) - (has("diamond_helmet")? 5 : 0) - (has("diamond_boots")? 4 : 0) - (has("diamond_sword")? 2 : 0);
	}

	public boolean isStacked() {
		return has("diamond_helmet") && has("diamond_chestplate") && has("diamond_leggings") && has("diamond_boots") && has("shield") && has("diamond_sword");
	}
}
