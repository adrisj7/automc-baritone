package automc.player;

import automc.AutoMC;
import automc.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;

public class PlayerController {
	public Inventory inventory;
	public Crafter crafter;

	public PathDisabler pathDisabler;

	public PlayerController() {
		inventory = new Inventory();
		crafter = new Crafter();
		pathDisabler = new PathDisabler();
	}
	
	public void onTick() {
		inventory.onTick();
	}

	public void closeContainer() {
		if (!AutoMC.getAutoMC().isInGame()) return;
		Minecraft.getMinecraft().player.closeScreen();
		Container c = Minecraft.getMinecraft().player.openContainer;
		if (c != null) {
			c.onContainerClosed(Minecraft.getMinecraft().player);
			Logger.debug(this, "Closed container.");
		}
		AutoMC.getAutoMC().getBaritone().getInputOverrideHandler().clearAllKeys();
	}

	public void reset() {
		pathDisabler.reset();
	}
	
	public boolean isInOverworld() {
		return Minecraft.getMinecraft().player.dimension == 0;
	}
	
	public BlockPos getOverworldPosition() {
		BlockPos p = Minecraft.getMinecraft().player.getPosition();
		if (isInOverworld()) {
			return p;
		}
		return new BlockPos(p.getX() * 8, p.getY(), p.getZ() * 8);
	}
	public BlockPos getNetherPosition() {
		BlockPos p = Minecraft.getMinecraft().player.getPosition();
		if (!isInOverworld()) {
			return p;
		}
		return new BlockPos(p.getX() / 8, p.getY(), p.getZ() / 8);
	}
	
	public boolean isSleeping() {
		return Minecraft.getMinecraft().player.isPlayerSleeping();
	}

}
