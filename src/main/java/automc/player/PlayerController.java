package automc.player;

import automc.AutoMC;
import automc.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;

public class PlayerController {
	public Inventory inventory;
	public Crafter crafter;


	public PlayerController() {
		inventory = new Inventory();
		crafter = new Crafter();
	}
	
	public void onTick() {
		inventory.onTick();
	}

	public void closeContainer() {
		Minecraft.getMinecraft().player.closeScreen();
		Container c = Minecraft.getMinecraft().player.openContainer;
		if (c != null) {
			c.onContainerClosed(Minecraft.getMinecraft().player);
		}
		AutoMC.getAutoMC().getBaritone().getInputOverrideHandler().clearAllKeys();
		Logger.debug(this, "Closed container.");
	}
}
