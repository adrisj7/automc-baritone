package automc.items;

import java.util.HashMap;
import java.util.Map;

import automc.Logger;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.MineBlockTask;
import automc.tasksystem.tasks.resources.CollectPlanksTask;
import automc.tasksystem.tasks.resources.CollectSticksTask;
import automc.utility.ItemUtil;
import net.minecraft.item.Item;

/**
 * This holds a mapping of item to task we defined.
 * 
 * This is useful for scenarios where we need to get an ambiguous item.
 * For instance, when we make our auto schematica builder, we will need to find the task
 * associated with grabbing an item.
 * 
 * @author adris
 *
 */
public class ItemTaskCatalogue {
	
	Map<Item, String> itemIDNames;
	public ItemTaskCatalogue() {
		this.itemIDNames = new HashMap<>();
		includeNames(
			"planks",
			"stick",
			"log",
			"dirt"
		);
	}

	public Task getItemTask(String item, int amount) {
		// If you think about it, this is _technically_ very wasteful, but don't worry about it right now.
		// Later the other method will be used.
		return getItemTask(ItemUtil.getItem(item), amount);
	}

	public Task getItemTask(Item item, int amount) {
		String name = itemIDNames.get(item);
		switch (name) {
			// The simple raw resources that you can't craft
			case "log":
			case "dirt":
				return new MineBlockTask(name, amount);
			case "planks":
				return new CollectPlanksTask(amount);
			case "stick":
				return new CollectSticksTask(amount);
			default:
				Logger.logError("[ItemTaskCatalogue] Couldn't find item with name " + item + ". Please either check spelling or add the item to the catalogue.", false);
				return null;
		}
	}

	private void includeNames(String... values) {
		for(String name : values) {
			itemIDNames.put(ItemUtil.getItem(name), name);
		}
	}
}
