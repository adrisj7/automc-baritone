package automc.containers;

import automc.AutoMC;
import automc.Logger;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public class ContainerHandler {

	private CachedContainers containers;

	private BlockPos lastOpenContainerPos;
	private Container lastOpenContainer;
	boolean chestOpen = false;

	private int checkInterval = 1000;
	private int tickTimer = 0;

	public ContainerHandler() {
		containers = new CachedContainers();
		lastOpenContainerPos = null;
		lastOpenContainer = null;

		chestOpen = false;
	}

	public void onBlockOpen(BlockPos pos) {
		lastOpenContainerPos = pos;
	}

	public void onContainerOpen(Container container) {

		container = Minecraft.getMinecraft().player.openContainer;
		//containers.onChestModified(container, pos);
		lastOpenContainer = container;

		/*
		if (chestInventory instanceof InventoryBasic) {
			Logger.debug(this, "Opened chest.");
			InventoryBasic inv = (InventoryBasic) chestInventory;
			lastOpenContainer = inv;
			BlockPos pos = lastOpenContainerPos;
			containers.onChestModified(inv, pos);
		} else {
			Logger.logError("onChestGui resulted in inventory that wasn't chest... This shouldn't happen.");
		}
		*/
	}

	public void onGuiClose() {
		if (lastOpenContainerPos != null && lastOpenContainer != null) {
			Logger.debug(this, "Closed chest.");
			containers.onChestModified(lastOpenContainer, lastOpenContainerPos);
			lastOpenContainer = null;
			lastOpenContainerPos = null;
			chestOpen = false;
		}
	}

	public void onTick() {
		if (tickTimer % checkInterval == 1) {
			containers.cleanup();
		}
		++tickTimer;
		
		if (!chestOpen && lastOpenContainer != null) {
			Logger.debug(this, "Opened chest.");
			containers.onChestModified(lastOpenContainer, lastOpenContainerPos);
			chestOpen = true;
			lastOpenContainer = null; // This stops it from looping for some reason...
		}
		
		if (!AutoMC.getAutoMC().isInGame() || !(Minecraft.getMinecraft().player.openContainer instanceof ContainerChest)) {
			chestOpen = false;
		}
	}

	public ICachedContainer getBestContainerWith(String item, int maxNeeded) {
		return containers.getBestContainerWith(item, maxNeeded);
	}

	public ICachedContainer getBestContainerWith(Item item, int maxNeeded) {
		return containers.getBestContainerWith(ItemUtil.getItemId(item), maxNeeded);
	}

	public void load() {
		// TODO: Json load
	}
	public void save() {
		// TODO: Json save
	}
}
