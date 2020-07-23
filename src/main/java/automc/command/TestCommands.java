package automc.command;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.ContainerType;
import automc.player.PlayerController;

/**
 * This contains some really simple test commands to check my sanity from time to time when things aren't working.
 */
public class TestCommands {

	// Runs the current test code
	public static void fillCraftingGridTest() {
		Logger.debug("Placing first 4 items in inventory crafting grid");
		PlayerController player = AutoMC.getAutoMC().player;
		player.inventory.moveItems(ContainerType.PLAYER, 36, 1, 1);
		player.inventory.moveItems(ContainerType.PLAYER, 37, 2, 1);
		player.inventory.moveItems(ContainerType.PLAYER, 38, 3, 1);
		player.inventory.moveItems(ContainerType.PLAYER, 39, 4, 1);
	}

}
