package automc.command;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.tasks.resources.CollectSticksTask;
import net.minecraft.client.Minecraft;

public class CommandParser {

	// Prefix for the command in chat
	private static final String PREFIX = "$";
	// If true, it will override ALL commands with the prefix, NOT JUST known ones.
	private static final boolean CANCEL_UNKNOWN = true;

	// Da big ol test method that's definitely temporary
	private void test() {
//		AutoMC.getAutoMC().taskRunner.runTask(new ExampleChestTask("diamond"));
		//AutoMC.getAutoMC().taskRunner.runTask(new GrabItemsFromChestCommand("diamond", 2));

		AutoMC.getAutoMC().taskRunner.runTask(new CollectSticksTask(9));
		//AutoMC.getAutoMC().taskRunner.runTask(new MineBlockTask("stone", 5, 1 ,MiningRequirement.HAND));
		//IRecipe recipe = ItemUtil.createPureShapedRecipe(2,2,"planks", "planks", "planks", "planks");
		//AutoMC.getAutoMC().taskRunner.runTask(new CraftRecipeInHandTask(recipe));
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
		case "test":
			test();
			break;
		case "work":
			if (kwords.length == 2) {
				double work = AutoMC.getAutoMC().itemWorkDictionary.getWork(kwords[1]);
				Logger.debug("Work: " + work);
			}
			break;
		case "pos":
			Logger.log("Player position: " + Minecraft.getMinecraft().player.getPositionVector());
			break;
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
