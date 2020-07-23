package automc.command;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.MiningRequirement;
import automc.hacks.TimerHack;
import automc.items.ItemTaskCatalogue;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.leveling.LevelMiningTask;
import automc.tasksystem.tasks.misc.GetStackedTask;
import automc.tasksystem.tasks.misc.SurviveAnarchyTask;
import automc.tasksystem.tasks.navigation.TravelOnHighwayTask;
import automc.tasksystem.tasks.navigation.TravelOnHighwayWithBedBackupTask;
import automc.tasksystem.tasks.resources.MineBlockTask;
import automc.tasksystem.tasks.special.EnchantBooksWithMobFarmTask;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CommandParser {

	// Prefix for the command in chat
	private static final String PREFIX = "$";
	// If true, it will override ALL commands with the prefix, NOT JUST known ones.
	private static final boolean CANCEL_UNKNOWN = true;

	// Da big ol test method that's definitely temporary
	private void get(String item, int amount) {
		ItemTaskCatalogue tc = AutoMC.getAutoMC().itemTaskCatalogue;
		Logger.log(this, "Trying to grab 1 item from generic task: " + item);
		Task t = tc.getItemTask(item, amount);
		if (t != null) {
			AutoMC.getAutoMC().taskRunner.runTask(t);
		} else {
			Logger.logError("Invalid item, could not find task: " + item);
		}
	}

	// TODO: These are kinda trash, remove 'em
	private void getLevelFromDiamonds(int level) {
		Task t = new LevelMiningTask(level, new MineBlockTask("diamond_ore", -1, MiningRequirement.DIAMOND));
		AutoMC.getAutoMC().taskRunner.runTask(t);
	}
	private void getLevelFromMobSpawner(int level) {
		Task t = new LevelMiningTask(level, new MineBlockTask("mob_spawner", -1, MiningRequirement.DIAMOND));
		AutoMC.getAutoMC().taskRunner.runTask(t);
	}

	private void travelWithHighway(int targetX, int targetZ) {
		AutoMC.getAutoMC().taskRunner.runTask(new TravelOnHighwayTask(targetX, targetZ));
	}

	private void enchantBooksWithMobFarm(boolean useChest, int count) {
		AutoMC.getAutoMC().taskRunner.runTask(new EnchantBooksWithMobFarmTask(useChest, count));		
	}

	private void test() {
		AutoMC.getAutoMC().taskRunner.runTask(new TravelOnHighwayWithBedBackupTask(8000, 0, true, true));
		//AutoMC.getAutoMC().taskRunner.runTask(new MoveItemsToAnyChestTask(ItemUtil.getItem("diamond"), 2));
		//AutoMC.getAutoMC().taskRunner.runTask(new BuildNetherPortalTask());
		//AutoMC.getAutoMC().taskRunner.runTask(new CollectFoodTask(20));
		//AutoMC.getAutoMC().taskRunner.runTask(new SurviveTask(new TravelToPositionTask(new BlockPos(0, 64, 1000000))));
		//AutoMC.getAutoMC().taskRunner.runTask(new PlayMinecraftTask());
		//AutoMC.getAutoMC().taskRunner.runTask(new SmeltInFurnaceTask("iron_ore", "iron_ingot", 3));
		//AutoMC.getAutoMC().getBaritone().getInputOverrideHandler().setInputForceState(Input.CLICK_RIGHT, false);
		//AutoMC.getAutoMC().player.inventory.equipArmor("diamond_chestplate", 1);
		//AutoMC.getAutoMC().player.inventory.equipArmor("diamond_leggings", 2);
		//Logger.log(AutoMC.getAutoMC().player.inventory.isArmorEquipped("diamond_chestplate"));
		//AutoMC.getAutoMC().taskRunner.runTask(AutoMC.getAutoMC().itemTaskCatalogue.getItemTask("diamond_chestplate", 1));
		//AutoMC.getAutoMC().combatRunner.killer.killNearest(EntityZombie.class);
		//AutoMC.getAutoMC().taskRunner.runTask(new CollectFlintTask(5));
		//AutoMC.getAutoMC().taskRunner.runTask(new GetStackedTask());
		//AutoMC.getAutoMC().player.inventory.equipArmor(Items.DIAMOND_CHESTPLATE);

		//AutoMC.getAutoMC().taskRunner.runTask(new BuildNetherPortalTask()); // holy shit
		//AutoMC.getAutoMC().taskRunner.runTask(new CollectOreAndSmeltTask("iron_ore", "iron_ingot", 3, MiningRequirement.STONE));
		//AutoMC.getAutoMC().taskRunner.runTask(new SmeltInFurnaceTask("iron_ore", "iron_ingot", 3));
	}

	private void surviveAnarchy(int radius, String axis) {
		double angle = 0;
		axis = axis.toLowerCase();
		switch (axis) {
		case "+x":
		case "x":
			angle = 0;
			break;
		case "-x":
			angle = 180;
		case "+z":
		case "z":
			angle = 90;
			break;
		case "-z":
			angle = -90;
			break;
		default:
			Logger.logError("Invalid axis. Must be one of the following: +x, -x, +z, -z.");
			return;
		}
		AutoMC.getAutoMC().taskRunner.runTask(new SurviveAnarchyTask(radius, angle, new GetStackedTask()));
	}

	private void open(BlockPos pos) {
		AutoMC.getAutoMC().customBaritone.getInteractWithGoalBlockProcess().interactWith(pos);
	}

	/**
	 * Tries to run a chat command.
	 * @param chat: The FULL chat message
	 * @return: whether the message is a command or not.
	 */
	public boolean tryParseCommand(String chat) {
		if (chat.length() == 0) return false;
		if (chat.startsWith(PREFIX)) {
			String noPrefix = chat.substring(PREFIX.length());
			String[] split = noPrefix.split(" ");
			return tryCommand(split[0], split);
		}
		return false;
	}

	private boolean tryCommand(String command, String[] kwords) {
		// TODO: Smarter command parser
		switch (command) {
		case "get":
			if (kwords.length == 2) {
				get(kwords[1], 1);
			} else if (kwords.length == 3) {
				try {
					get(kwords[1], Integer.parseInt(kwords[2]));
				} catch (NumberFormatException e) {
					Logger.log("Invalid number of items to get: " + kwords[2]);
				}
			} else {
				Logger.log("Usage: get <item> <number_of_items = 1>");
			}
			break;
		case "level":
			boolean bad = false;
			try {				
				if (kwords.length == 2) {
					getLevelFromDiamonds(Integer.parseInt(kwords[1]));
				} else if (kwords.length == 3) {
					int level = Integer.parseInt(kwords[2]);
					switch (kwords[1]) {
						case "diamond":
							getLevelFromDiamonds(level);
							break;
						case "mob_spawner":
							getLevelFromMobSpawner(level);
							break;
						default:
							bad = true;
							break;
					}
				} else {
					bad = true;
				}
				if (bad) {
					Logger.log("Usage: level [type = diamond] <target level>. Types include: diamond, mob_spawner");
				}

			} catch (NumberFormatException e) {
				Logger.log("Invalid target level (must be an integer).");
			}
			break;
		case "test":
			//AutoMC.getAutoMC().player.equipItem(Items.FLINT_AND_STEEL);
			test();
			break;
		case "open":
			if (kwords.length != 4) {
				Logger.log("Usage: open <x> <y> <z>");
			} else {
				try {
					int x = Integer.parseInt(kwords[1]),
						y = Integer.parseInt(kwords[2]),
						z = Integer.parseInt(kwords[3]);
					open(new BlockPos(x,y,z));
				} catch (NumberFormatException e) {
					Logger.log("Could not read arguments as integers. Try formatting them properly.");
				}
			}
			break;
		case "work":
			if (kwords.length == 2) {
				double work = AutoMC.getAutoMC().itemWorkDictionary.getWork(kwords[1]);
				Logger.debug("Work: " + work);
			}
			break;
		case "id":
			ItemStack st = Minecraft.getMinecraft().player.getHeldItemMainhand();
			if (st.isEmpty()) {
				Logger.log(this, "(No items)");
			} else {
				Item item = st.getItem();
				String id = ItemUtil.getItemId(item);
				Logger.log("Held Item ID: " + id);
			}
			break;
		case "pos":
			Logger.log("Player position: " + Minecraft.getMinecraft().player.getPositionVector());
			break;
		case "highway":
			if (kwords.length != 3) {
				Logger.log("Usage: highway [target_x] [target_z]");
			} else {
				try {
					int tx = Integer.parseInt(kwords[1]),
					    tz = Integer.parseInt(kwords[2]);
					travelWithHighway(tx, tz);
				} catch (NumberFormatException e) {
					Logger.log("Invalid target coordinates. Must be integers.");
				}
			}
			break;
		case "get_books":
			Logger.log("Enchanting books and putting 'em in a nearby chest. I assume you have an enchanting table and mob farm set up!!!");
			enchantBooksWithMobFarm(true, 9999);
			break;
		case "anarchy":
			String axis = "+x";
			int radius = 20000;
			if (kwords.length > 1) {
				axis = kwords[1];
			}
			if (kwords.length > 2) {
				try {
					int r = Integer.parseInt(kwords[2]);
					radius = r;
				} catch (NumberFormatException e) {
					Logger.log("Invalid radius. Must be an integer.");
				}
			}
			surviveAnarchy(radius, axis);
			break;
		case "timer":
			try {
				float scale = 1f;
				if (kwords.length > 2) {
					Logger.log("Usage: timer [timescale=1]. Sets timer time scale.");
				} else if (kwords.length == 2) {
					scale = Float.parseFloat(kwords[1]);
				}
				TimerHack.setTime(scale);
			} catch (NumberFormatException e) {
				Logger.log("Must give a valid number.");
			}
		case "stop":
		case "quit":
		case "cancel":
			AutoMC.getAutoMC().onCancel();
			break;
		default:
			if (CANCEL_UNKNOWN) {
				Logger.debug("\\u00A7cUnknown command: " + command);
			}
			return CANCEL_UNKNOWN;
		}

		return true;
	}

}
