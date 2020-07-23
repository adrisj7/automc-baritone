package automc.items;

import automc.Logger;
import automc.definitions.MiningRequirement;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.misc.GetStackedTask;
import automc.tasksystem.tasks.resources.CollectFlintTask;
import automc.tasksystem.tasks.resources.CollectMineOutputTask;
import automc.tasksystem.tasks.resources.CollectOreAndSmeltTask;
import automc.tasksystem.tasks.resources.CollectPlanksTask;
import automc.tasksystem.tasks.resources.CollectRecipeAndCraftTask;
import automc.tasksystem.tasks.resources.KillAndGrabLootTask;
import automc.tasksystem.tasks.resources.MineBlockTask;
import automc.utility.ItemUtil;
import net.minecraft.entity.passive.EntitySheep;
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

//	Map<Item, String> itemIDNames;
	public ItemTaskCatalogue() {
		/*this.itemIDNames = new HashMap<>();
		includeNames(
			"planks",
			"stick",
			"log",
			"dirt",
			"wooden_pickaxe",
		);
		*/
	}

	public Task getItemTask(Item item, int amount) {
		return getItemTask(ItemUtil.getItemId(item), amount);
	}

	public Task getItemTask(String item, int amount) {
		String name = item;//ItemUtil.findId(item);//itemIDNames.get(item);
		if (name == null) {
			Logger.logError("[ItemTaskCatalogue] Item could not be parsed for a name: " + item + ". Did you forget to include it in the constructor?");
			return null;
		}
		switch (name) {
			/// SIMPLE RAW RESOURCES
			case "log":
			case "dirt":
				return new MineBlockTask(name, amount);
			case "cobblestone":
				return new CollectMineOutputTask(new String[] {"cobblestone", "stone"}, new Integer[] {null, 0}, "cobblestone", amount, MiningRequirement.WOOD);
			case "coal":
				return new CollectMineOutputTask("coal_ore", "coal", amount, MiningRequirement.WOOD);
			case "iron_ore":
				return new MineBlockTask(name, amount, MiningRequirement.STONE);
			case "gold_ore":
				return new MineBlockTask(name, amount, MiningRequirement.IRON);
			// SPECIAL RESOURCES
			case "flint":
				return new CollectFlintTask(amount);
			/// SIMPLE CRAFTING RESOURCES
			case "planks":
				return new CollectPlanksTask(amount);
				//return new CollectCobblestoneTask(amount);//new MineBlockTask(name, amount, MiningRequirement.WOOD);
			case "stick":
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(1, 2, "planks", "planks"), "stick", amount);//new CollectSticksTask(amount);
			case "iron_ingot":
				return new CollectOreAndSmeltTask("iron_ore", name, amount, MiningRequirement.STONE);
			case "gold_ingot":
				return new CollectOreAndSmeltTask("gold_ore", name, amount, MiningRequirement.IRON);
			case "diamond":
				return new CollectMineOutputTask("diamond_ore", name, amount, MiningRequirement.IRON);
			case "obsidian": // TODO: Its own process. If it doesn't see obsidian nearby it searches for lava pools and obsidianifies them.
				return new MineBlockTask(name, amount, MiningRequirement.DIAMOND);
			case "dye#4":
			case "lapis_lazuli":
				return new CollectMineOutputTask("lapis_ore", "dye#4", amount, MiningRequirement.STONE);
			case "wool":
				// TODO: Use shears maybe? It's a lil selfish to do it this way.
				return new KillAndGrabLootTask(EntitySheep.class, "wool", amount);
			case "bed":
			{
				String p = "planks",
					   w = "wool";
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 2, w,w,w, p,p,p), "bed", amount);
			}
			/// CONTAINERS AND TOOLS
			case "crafting_table":
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(2, 2, "planks", "planks", "planks", "planks"), "crafting_table", amount);
			case "chest":
			{
				String p = "planks",
				       o = null;
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, p, p, p, p, o, p, p, p, p), "chest", amount);
			}
			case "furnace":
			{
				String c = "cobblestone",
					   o = null;
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, c, c, c, c, o, c, c, c, c), "furnace", amount);
			}
			// Pickaxes
			case "wooden_pickaxe":
				return getPickaxeTask("planks", name, amount);
			case "stone_pickaxe":
				return getPickaxeTask("cobblestone", name, amount);
			case "iron_pickaxe":
				return getPickaxeTask("iron_ingot", name, amount);
			case "gold_pickaxe":
				return getPickaxeTask("gold_ingot", name, amount);
			case "diamond_pickaxe":
				return getPickaxeTask("diamond", name, amount);
			// Swords
			case "wooden_sword":
				return getSwordTask("planks", name, amount);
			case "stone_sword":
				return getSwordTask("cobblestone", name, amount);
			case "iron_sword":
				return getSwordTask("iron_ingot", name, amount);
			case "gold_sword":
				return getSwordTask("gold_ingot", name, amount);
			case "diamond_sword":
				return getSwordTask("diamond", name, amount);
			// Armor
			case "iron_chestplate":							// Chestplate
				return getChestplateTask("iron_ingot", name, amount);
			case "gold_chestplate":
				return getChestplateTask("gold_ingot", name, amount);
			case "diamond_chestplate":
				return getChestplateTask("diamond", name, amount);
			case "iron_leggings":							// Leggings
				return getLeggingsTask("iron_ingot", name, amount);
			case "gold_leggings":
				return getLeggingsTask("gold_ingot", name, amount);
			case "diamond_leggings":
				return getLeggingsTask("diamond", name, amount);
			case "iron_helmet":							// Helmet
				return getHelmetTask("iron_ingot", name, amount);
			case "gold_helmet":
				return getHelmetTask("gold_ingot", name, amount);
			case "diamond_helmet":
				return getHelmetTask("diamond", name, amount);
			case "iron_boots":							// Boots
				return getBootsTask("iron_ingot", name, amount);
			case "gold_boots":
				return getBootsTask("gold_ingot", name, amount);
			case "diamond_boots":
				return getBootsTask("diamond", name, amount);
			// Misc
			case "shield":
			{
				String w = "planks",
					   o = null,
					   i = "iron_ingot";
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, w,i,w, w,w,w, o,w,o), "shield", amount); // oh no
			}
			case "flint_and_steel":
				return new CollectRecipeAndCraftTask(ItemUtil.createPureShapelessRecipe("flint", "iron_ingot"), name, amount);
			// Custom/Special cases
			case "stacked":
				return new GetStackedTask();
			default:
				Logger.logError("[ItemTaskCatalogue] Couldn't find item with name " + name + " (item " + item + "). Please either check spelling or add the item to the catalogue.");
				return null;
		}
	}
	public boolean taskExists(String item) {
		return getItemTask(item, 1) != null;
	}

	/// UTILITY CONSTRUCTORS
	private Task getPickaxeTask(String material, String name, int amount) {
		return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, material, material, material, null, "stick", null, null, "stick", null), name, amount);
	}
	private Task getSwordTask(String material, String name, int amount) {
		return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, null, material, null, null, material, null, null, "stick", null), name, amount);
	}
	private Task getChestplateTask(String material, String name, int amount) {
		String m = material,
			   o = null;
		return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, m,o,m, m,m,m, m,m,m), name, amount);
	}
	private Task getLeggingsTask(String material, String name, int amount) {
		String m = material,
			   o = null;
		return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, m,m,m, m,o,m, m,o,m), name, amount);
	}
	private Task getBootsTask(String material, String name, int amount) {
		String m = material,
			   o = null;
		return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, o,o,o, m,o,m, m,o,m), name, amount);
	}
	private Task getHelmetTask(String material, String name, int amount) {
		String m = material,
			   o = null;
		return new CollectRecipeAndCraftTask(ItemUtil.createPureShapedRecipe(3, 3, m,m,m, m,o,m, o,o,o), name, amount);
	}

	/*
	private void includeNames(String... values) {
		for(String name : values) {
			itemIDNames.put(ItemUtil.getItem(name), name);
		}
	}
	*/
}
