package automc.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.ContainerType;
import automc.definitions.MiningRequirement;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

/**
 * An interface for our inventory that lets us view inventory stats.
 */
public class Inventory {

	static boolean SHOW_INVENTORY = false;

	HashMap<Item, Integer> itemCounts;
	HashMap<Item, List<Integer>> itemSlots;

	Set<Integer> foodSlots;
	int totalFoodHunger;
	
	int emptySlots = 0;
	

	public Inventory() {
		itemCounts = new HashMap<Item, Integer>();
		itemSlots = new HashMap<Item, List<Integer>>();

		foodSlots = new HashSet<Integer>();
		totalFoodHunger = 0;
	}

	public int getItemCount(Item item) {
		return itemCounts.containsKey(item)? itemCounts.get(item) : 0;
	}
	public int getItemCountWithMeta(String item, int meta) {
		Item baseItemType = ItemUtil.getItem(item);
		if (itemSlots.containsKey(baseItemType)) {
			int count = 0;
			for(int invSlot : itemSlots.get(baseItemType)) {
				ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(invSlot);
				if (stack.getMetadata() == meta) {
					count += stack.getCount();
				}
			}
			return count;
		}

		return 0;
	}
	public int getItemCount(String item) {
		int metaSplit = item.indexOf("#");
		if (metaSplit != -1 ) {
			// Metas, will do some extra searching for those.
			try {
				String name = item.substring(0, metaSplit);
				String metaString = item.substring(metaSplit + 1);
				int meta = Integer.parseInt(metaString);
				return getItemCountWithMeta(name, meta);
			} catch (NumberFormatException e) {
				Logger.logError("[Inventory] Could not decipher meta for item: " + item);
			}
		}
		// No metas, straightforward.
		return getItemCount(ItemUtil.getItem(item));
	}
	public boolean hasItem(Item item) {
		return getItemCount(item) > 0;
	}
	public boolean hasItem(String item) {
		return getItemCount(item) > 0;
	}

	public int getArmorSlot(Item item) {
		List<ItemStack> armor = Minecraft.getMinecraft().player.inventory.armorInventory;
		for (int i = 0; i < 4; ++i) {
			if (armor.get(i) != null && armor.get(i).getItem().equals(item)) {
				return i;
			} else {
				String n = armor.get(i) != null? ItemUtil.getItemId(armor.get(i).getItem()) : "null";
			}
		}
		return -1;
	}
	public int getArmorSlot(String item) {
		return getArmorSlot(ItemUtil.getItem(item));
	}
	public ItemStack getArmorStack(int armorSlot) {
		if (armorSlot < 0 || armorSlot >= 4) return null;
		return Minecraft.getMinecraft().player.inventory.armorInventory.get(armorSlot);
	}
	public boolean isArmorEquipped(Item armor) {
		return getArmorSlot(armor) != -1;
	}
	public boolean isArmorEquipped(String armor) {
		return isArmorEquipped(ItemUtil.getItem(armor));
	}

	public ItemStack getShieldStack() {
		return Minecraft.getMinecraft().player.getHeldItemOffhand();
	}

	public double getValidFuelAmount() {
		return getItemCount("coal")*8 + getItemCount("planks")*1;
	}

	// Do we have the tools to meet this mining requirement?
	public boolean miningRequirementMet(MiningRequirement req) {
		switch (req) {
			case HAND:
				return true;
			case DIAMOND:
				return hasItem(ItemUtil.getItem("diamond_pickaxe"));
			case IRON:
				return hasItem(ItemUtil.getItem("iron_pickaxe"))
					|| hasItem(ItemUtil.getItem("gold_pickaxe"))
					|| miningRequirementMet(MiningRequirement.DIAMOND);
			case STONE:
				return hasItem(ItemUtil.getItem("stone_pickaxe"))
					|| miningRequirementMet(MiningRequirement.IRON);
			case WOOD:
				return hasItem(ItemUtil.getItem("wooden_pickaxe"))
					|| miningRequirementMet(MiningRequirement.STONE);
		}
		return false;
	}

	public List<Integer> getInvSlotsForItem(Item item) {
		return itemSlots.containsKey(item)? itemSlots.get(item) : new ArrayList<Integer>();
	}
	public List<Integer> getInvSlotsForItemWithMeta(String item, int meta) {
		Item baseItem = ItemUtil.getItem(item);
		List<Integer> result = new ArrayList<Integer>(itemSlots.containsKey(baseItem)? itemSlots.get(baseItem).size() : 0);
		if (itemSlots.containsKey(baseItem)) {
			for(int invSlot : itemSlots.get(baseItem)) {
				ItemStack stack = Minecraft.getMinecraft().player.inventory.getStackInSlot(invSlot);
				if (stack.getMetadata() == meta) {
					result.add(invSlot);
				}
			}
		}
		return result;
	}
	public List<Integer> getInvSlotsForItem(String item) {
		int metaSplit = item.indexOf("#");
		if (metaSplit != -1 ) {
			// Metas, will do some extra searching for those.
			try {
				String name = item.substring(0, metaSplit);
				String metaString = item.substring(metaSplit + 1);
				int meta = Integer.parseInt(metaString);
				return getInvSlotsForItemWithMeta(name, meta);
			} catch (NumberFormatException e) {
				Logger.logError("[Inventory] Could not decipher meta for item: " + item);
			}
		}
		// No metas, straightforward.
		return getInvSlotsForItem(ItemUtil.getItem(item));
	}

	public void swapItems(ContainerType type, int windowSlotFrom, int windowSlotTo) {

		int invSlotTo = windowSlotToInventorySlot(type, windowSlotTo);
		boolean locationIsFull = getInventory().getStackInSlot(invSlotTo).getCount() > 0;

		int playerInventory = Minecraft.getMinecraft().player.inventoryContainer.windowId;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

		if (SHOW_INVENTORY) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Minecraft.getMinecraft().player));
		}

		controller.windowClick(playerInventory, windowSlotFrom, 0, ClickType.PICKUP, player);
		controller.windowClick(playerInventory, windowSlotTo,   0, ClickType.PICKUP, player);

		// We had an item at our swap spot, move it to the original location.
		if (locationIsFull) {
			controller.windowClick(playerInventory, windowSlotFrom, 0, ClickType.PICKUP, player);			
		}

		// Update cached values
		updateInventoryData();
	}

	/**
	 * 
	 * @param type
	 * @param windowSlotFrom
	 * @param windowSlotTo
	 * @param amount
	 * @return: The number of items successfully transported.
	 */
	public int moveItems(ContainerType type, int windowSlotFrom, int windowSlotTo, int amount) {

		if (SHOW_INVENTORY && type == ContainerType.PLAYER) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Minecraft.getMinecraft().player));
		}


		boolean locationIsFull = getItemStackInWindowSlot(type, windowSlotTo).getCount() > 0;
		if (locationIsFull) {
			Logger.logError("Tried to move items from " + windowSlotFrom + " to " + windowSlotTo + " but you have an item in the target position. Expect unexpected behaviour!");
		}

		Container container = getContainer(type);
		if (container == null) {
			return 0;
		}
		
		int containerID = container.windowId;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

		int pickedUpAmount = controller.windowClick(containerID, windowSlotFrom, 0, ClickType.PICKUP, player).getCount();

		int toMove = Math.min(amount, pickedUpAmount);
		
		for(int i = 0; i < Math.min(amount, pickedUpAmount); ++i) {
			controller.windowClick(containerID, windowSlotTo, 1, ClickType.PICKUP, player);
		}

		// If we picked up more items than we placed down, return the items back to their original place.
		if (pickedUpAmount > amount) {
			controller.windowClick(containerID, windowSlotFrom, 0, ClickType.PICKUP, player);			
		}

		// Update cached values
		updateInventoryData();

		return toMove;

	}

	public boolean receiveCraftingOutput(ContainerType type) {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

		boolean isStupidCrafting = (type == ContainerType.PLAYER || type == ContainerType.CRAFTING);

		Container container = getContainer(type);
		if (container == null) return false;
		int containerId = container.windowId;

		ArrayList<Integer> filledSlots = new ArrayList<Integer>();
		for(int check : getInputWindowSlots(type)) {
			if (container.getSlot(check).getHasStack()) {
				filledSlots.add(check);
			}
		}

		if (isStupidCrafting && filledSlots.size() == 0) {
			Logger.log("Something fucked up here... After crafting there is nothing in our slots.");
			return false;
//			filledSlots.add(getInputSlots(type)[0]);
		}

		// Convoluted but functional method
		// First: Swap from the crafting output
//		controller.windowClick(playerInventory, 0, 1, ClickType.SWAP, player);

		/*
		if (getItemStackInWindowSlot(type, getOutputSlot(type)).isEmpty()) {
			// Return everything.
			String debug = "";
			for (int slot : filledSlots) {
				debug += "[" + slot + ", " + ItemUtil.getItemId(container.getSlot(slot).getStack().getItem()) + "] ";
				controller.windowClick(containerId, slot, 0, ClickType.QUICK_MOVE, player);
			}
			Logger.logError("Can't grab output from crafting table. HAD: " + debug);
			return false;
		}*/

		controller.windowClick(containerId, getOutputSlot(type), 0, ClickType.QUICK_MOVE, player);
		controller.windowClick(containerId, getOutputSlot(type), 0, ClickType.SWAP, player);

		if (isStupidCrafting) {
			//controller.windowClick(playerInventory, 0, 1, ClickType.PICKUP, player);
			// 2nd: Pickup ALL items left over.
			for(int slot : filledSlots) {
				System.out.println("SLOT: " + slot);
				controller.windowClick(containerId, slot, 0, ClickType.PICKUP, player);
				controller.windowClick(containerId, slot, 0, ClickType.QUICK_MOVE, player);
	//			controller.windowClick(playerInventory, slot, 0, ClickType.PICKUP, player);
			}
		}

		// Update cached values
		updateInventoryData();

		return true;
	}

	public int quickMoveFromSlot(ContainerType type, int windowSlot) {
		Container container = getContainer(type);
		if (container == null) return 0;
		int containerId = container.windowId;

		EntityPlayerSP player = Minecraft.getMinecraft().player;
		PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

		ItemStack result = getItemStackInWindowSlot(type, windowSlot);
		int slot = getStackableInventorySlot(result.getItem());

		// TODO: Confirm that we could receive everything. If not, discard some items here or elsewhere.

		controller.windowClick(containerId, windowSlot, 0, ClickType.QUICK_MOVE, player);
		
		updateInventoryData();
		return result.getCount();
	}

	public void equipArmor(Item armor, int armorSlot) {

		ItemStack currentArmor = getArmorStack(armorSlot);
		if (currentArmor != null) {
			// Is our armor already equipped?
			if (currentArmor.getItem().equals(armor)) {
				return;
			}
			// We have different armor in that slot. De-equip it.
			if (!currentArmor.isEmpty()) {
				int correspondingSlot = armorSlotToWindowSlot(armorSlot);
				quickMoveFromSlot(ContainerType.PLAYER, correspondingSlot);
				// Now there is NO armor in this slot. Confirmed.
			}
		}

		List<Integer> slots = getInvSlotsForItem(armor); 
		if (slots.size() != 0) {
			int slot = slots.get(0);

			Container container = getContainer(ContainerType.PLAYER);
			if (container == null) return;

			EntityPlayerSP player = Minecraft.getMinecraft().player;
			PlayerControllerMP controller = Minecraft.getMinecraft().playerController;

			int windowSlot = inventorySlotToWindowSlot(ContainerType.PLAYER, slot);

			controller.windowClick(container.windowId, windowSlot, 0, ClickType.QUICK_MOVE, player);			
		}
	}

	public void equipAsShield(Item item) {
		ItemStack current = getShieldStack();
		if (current != null) {
			// Already equipped.
			if (current.getItem().equals(item)) {
				return;
			}
		}
		List<Integer> slots = getInvSlotsForItem(item); 
		if (slots.size() != 0) {
			int slot = slots.get(0);
			swapItems(ContainerType.PLAYER, inventorySlotToWindowSlot(ContainerType.PLAYER, slot) , shieldWindowSlot());
		}
		
	}
	public void equipAsShield(String item) {
		equipAsShield(ItemUtil.getItem(item));
	}

	public void equipArmor(String armor, int armorSlot) {
		equipArmor(ItemUtil.getItem(armor), armorSlot);
	}

	public void equipBestWeapon() {
		String[] order = new String[] {
			"diamond_sword",
			"diamond_axe",
			"iron_sword",
			"iron_axe",
			"cobblestone_sword",
			"cobblestone_axe"
		};
		for (String weapon : order) {
			List<Integer> slots = getInvSlotsForItem(weapon);
			if (slots.size() != 0) {
				int slot = slots.get(0);
				equipItem(slot);
			}
		}
	}

	// TODO: Should this be done in Inventory?
	public void equipItem(Item item) {
		if (!AutoMC.getAutoMC().isInGame()) return;

		// Move item to first slot
		List<Integer> validItemSlots = getInvSlotsForItem(item);
		if (validItemSlots.size() > 0) {
			equipItem(validItemSlots.get(0));
		} else {
			Logger.logError("Tried to equip the following item but item was not in inventory: " + ItemUtil.getItemId(item));
		}
	}

	public void equipItem(int inventorySlot) {
		// Make sure we're able to swap in our inventory.
		AutoMC.getAutoMC().player.closeContainer();
		if (inventorySlot != 0) {
			int slot = Inventory.inventorySlotToWindowSlot(ContainerType.PLAYER, inventorySlot);
			AutoMC.getAutoMC().player.inventory.swapItems(ContainerType.PLAYER, slot, 36);
		}
		Minecraft.getMinecraft().player.inventory.currentItem = 0;
		
	}

	public ItemStack getItemStackInWindowSlot(ContainerType type, int windowSlot) {
		Container c = getContainer(type);
		if (windowSlot >= c.inventorySlots.size()) return null;
		return c.getSlot(windowSlot).getStack();
	}

	public boolean isChestOpened() {
		return (Minecraft.getMinecraft().player.openContainer instanceof ContainerChest);
	}
	public boolean isCraftingTableOpened() {
		return (Minecraft.getMinecraft().player.openContainer instanceof ContainerWorkbench);
	}
	public boolean isFurnaceOpened() {
		return (Minecraft.getMinecraft().player.openContainer instanceof ContainerFurnace);
	}
	public boolean isEnchantmentTableOpened() {
		return (Minecraft.getMinecraft().player.openContainer instanceof ContainerEnchantment);
	}

	/**
	 * 	Tries to grab "amount" items from a chest slot.
	 * @param slot:		The window slot to grab from.
	 * @param amount:	How many items to grab from that slot.
	 * @return:			The number of items successfully grabbed.
	 */
	public int grabItemFromOpenChest(int slot, int amount) {

		// Determine whether it's a large or small chest, grab container accordingly.
		ContainerType type = ContainerType.CHEST;
		Container container = getContainer(type);
		if (container == null) return 0;

		ItemStack toMoveStack = getItemStackInWindowSlot(type, slot);
		if (toMoveStack.isEmpty()) return 0;
		
		// TODO: Check for full inventory. Have some kind if "assert not full" that CREATES an empty slot if need be.
		int emptySlot = getStackableInventorySlot(toMoveStack.getItem());
		int emptyWindowSlot = inventorySlotToWindowSlot(type, emptySlot);
		Logger.log("Found empty slot: " + emptySlot + ", Moving from " + slot + " to " + emptyWindowSlot);
		return this.moveItems(type, slot, emptyWindowSlot, amount);
		/*
		int containerId = container.windowId;

		// Pickup all items in the chest slot
		controller.windowClick(containerId, slot, 0, ClickType.PICKUP, player);
		// Put "amount" items in our empty slot (or as many as we can)
		int slotAmount = getInventory().getStackInSlot(emptySlot).getCount();
		int toMove = (slotAmount < amount)? slotAmount : amount; 
		for (int i = 0; i < toMove; ++i) {
			controller.windowClick(containerId, emptyWindowSlot, 1, ClickType.PICKUP, player);
			Logger.log("Plop: " + emptyWindowSlot);
		}
		// Put our remaining items back in the chest.
		if (slotAmount > amount) {
			controller.windowClick(containerId, slot, 0, ClickType.PICKUP, player);
		}
		return toMove;
		*/
	}

	public boolean grabItemFromOpenChest(Item item, int amount) {
		int grabbed = 0;
		List<Integer> slots = getChestSlotsWithItem(item);
		for (int slot : slots) {
			if (grabbed >= amount) return true;
			ItemStack stack = getItemStackInWindowSlot(ContainerType.CHEST, slot);
			if (stack == null) return false;
			int grabAmount = (stack.getCount() > amount)? amount : stack.getCount();
			grabbed += grabItemFromOpenChest(slot, grabAmount);
		}
		return false;
	}
	
	public boolean grabItemFromOpenChest(String item, int amount) {
		return grabItemFromOpenChest(ItemUtil.getItem(item), amount);
	}

	public List<Integer> getChestSlotsWithItem(Item item) {
		List<Integer> result = new ArrayList<Integer>();
		Container open = Minecraft.getMinecraft().player.openContainer;
		if (open == null) return result; // empty
		int end = (open.inventorySlots.size() == 90)? 53 : 26;
		for (int slot = 0; slot < end; ++slot) {
			ItemStack st = getItemStackInWindowSlot(ContainerType.CHEST, slot);
			if (st.getItem().equals(item)) {
				result.add(slot);
			}
		}
		return result;
	}

	public void putItemInEnchantingTable(int inventorySlot) {
		int windowSlot = inventorySlotToWindowSlot(ContainerType.ENCHANTING_TABLE, inventorySlot);
		// Take out of table first, if any is in there.
		quickMoveFromSlot(ContainerType.ENCHANTING_TABLE, 0);
		quickMoveFromSlot(ContainerType.ENCHANTING_TABLE, windowSlot);
	}
	public void putItemInEnchantingTable(String item) {
		if (hasItem(item)) {
			putItemInEnchantingTable(getInvSlotsForItem(item).get(0));
		} else {
			Logger.logError("Tried to enchant item " + item + ", but you don't have it! Expect bad things.");
		}
	}
	public void putLapisInEnchantingTable(int targetAmount) {
		if (hasItem("dye#4")) {
			ItemStack lapisTarget = getItemStackInWindowSlot(ContainerType.ENCHANTING_TABLE, 1);
			int requiredLeft = targetAmount - lapisTarget.getCount();
			if (requiredLeft > 0) {
				for(int invSlot : getInvSlotsForItem("dye#4")) {
					if (requiredLeft <= 0) break;
					int winSlot = inventorySlotToWindowSlot(ContainerType.ENCHANTING_TABLE, invSlot);
					// Just move all of it, it will auto move the lapis back into your inventory when you're done.
					requiredLeft -= quickMoveFromSlot(ContainerType.ENCHANTING_TABLE, winSlot);
				}
				if (requiredLeft > 0) {
					Logger.logError("Failed to put " + targetAmount + " pieces of lapis in the enchantment table. Still needed " + requiredLeft + " left.");
				}
			}
		} else {
			Logger.logError("Tried to put dye in enchanting table, but you don't have it! Expect bad things.");
		}
	}
	public void getEnchantingTableOutput() {
		quickMoveFromSlot(ContainerType.ENCHANTING_TABLE, 0);		
		quickMoveFromSlot(ContainerType.ENCHANTING_TABLE, 1);
	}

	public void onTick() {
		updateInventoryData();
	}

	private InventoryPlayer getInventory() {
		 return Minecraft.getMinecraft().player.inventory;
	}

	/**
	 * 		Find an inventory slot that can hold ONE of this item.
	 * @param item
	 * @return
	 */
	private int getStackableInventorySlot(Item item) {
		InventoryPlayer inv = getInventory();
		// First try to stack on top of items currently in our inventory.
		List<Integer> slotsWithItem = getInvSlotsForItem(item);
		for (int invSlot : slotsWithItem) {
			ItemStack st = inv.getStackInSlot(invSlot);
			if (st.getCount() < st.getMaxStackSize()) {
				return invSlot;
			}
		}
		// Now, find empty slots.
		for (int invSlot = 0; invSlot < inv.getSizeInventory(); ++invSlot) {
			ItemStack st = inv.getStackInSlot(invSlot);
			if (st.isEmpty()) return invSlot;
		}
		return -1;
	}
	
	private int windowSlotToInventorySlot(ContainerType type, int windowSlot) {
		switch (type) {
		case PLAYER:
			if (windowSlot >= 36) {
				return windowSlot - 36;
			}
			return windowSlot;
		case CRAFTING:
			if (windowSlot >= 37) {
				return windowSlot - 37;
			}
			return windowSlot - 1;
		case FURNACE:
			if (windowSlot >= 30) {
				return windowSlot - 30;
			}
			return windowSlot + 6;
		case CHEST:
			Container open = Minecraft.getMinecraft().player.openContainer;
			int start = (open.inventorySlots.size() == 90)? 54 : 27;
			if (windowSlot >= start + 27) {
				return windowSlot - start - 27;
			}
			return windowSlot - start + 9;
		case ENCHANTING_TABLE:
			if (windowSlot >= 29) {
				return windowSlot - 29;
			}
			return windowSlot + 7;
		default:
			return windowSlot;
		}
		/*
		 * This one was for player inventory, but I doubt these will ever be used.
		switch (windowSlot) {
			case 5:
				return 103;
			case 6:
				return 102;
			case 7:
				return 101;
			case 8:
				return 100;
			case 45:
				return -106;
		}
		*/
	}
	
	private int[] getInputWindowSlots(ContainerType type) {
		switch (type) {
		case PLAYER:
			return new int[] {1, 2, 3, 4};
		case CRAFTING:
			return new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
		case FURNACE:
			return new int[] {0, 1};
		case ENCHANTING_TABLE:
			return new int[] {0, 1};
		default:
			return new int[] {};
		}
	}
	private int getOutputSlot(ContainerType type) {
		switch (type) {
		case PLAYER:
			return 0;
		case CRAFTING:
			return 0;
		case FURNACE:
			return 2;
		case ENCHANTING_TABLE:
			return 0; // Same as input.
		default:
			return 0;
		}
	}

	private Container getContainer(ContainerType type) {
		Container c = Minecraft.getMinecraft().player.openContainer;
		switch (type) {
		case PLAYER:
			return Minecraft.getMinecraft().player.inventoryContainer;
		case CRAFTING:
			if (c instanceof ContainerWorkbench) {
				return c;
			}
			break;
		case FURNACE:
			if (c instanceof ContainerFurnace) {
				return c;
			}
			break;
		case CHEST:
			
			if (c instanceof ContainerChest) {
				return c;
			}
			break;
		case ENCHANTING_TABLE:
			if (c instanceof ContainerEnchantment) {
				return c;
			}
			break;
		}
		return null;
	}
	
	public static int inventorySlotToWindowSlot(ContainerType type, int invSlot) {
		// https://wiki.vg/Inventory
		// https://minecraft.gamepedia.com/Inventory
		
		switch (type) {
		case PLAYER:
			if (invSlot < 9) {
				return invSlot + 36;
			}
			return invSlot;
		case CRAFTING:
			if (invSlot < 9) {
				return invSlot + 37;
			}
			return invSlot + 1;
		case FURNACE:
			if (invSlot < 9) {
				return invSlot + 30;
			}
			return invSlot - 6;
		case CHEST:
			Container open = Minecraft.getMinecraft().player.openContainer;
			int start = (open.inventorySlots.size() == 90)? 54 : 27;
			if (invSlot < 9) {
				return invSlot + (start + 27);
			}
			return (invSlot - 9) + start;
		case ENCHANTING_TABLE:
			if (invSlot < 9) {
				return invSlot + 29;
			}
			return invSlot - 7;
		}
		/*
		switch (invSlot) {
			case 103:
				return 5;
			case 102:
				return 6;
			case 101:
				return 7;
			case 100:
				return 8;
			case -106:
				return 45;
		}
		*/
		return invSlot;
	}

	private int armorSlotToWindowSlot(int armorSlot) {
		return armorSlot + 5;
	}
	private int shieldWindowSlot() {
		return 45; // a very old 45 indeed
	}

	// TODO: Grab item burn data, wasn't able to figure it out quickly.
	public static double getFuelAmount(ItemStack stack) {
		return getFuelAmount(stack.getItem()) * stack.getCount();
	}
	public static double getFuelAmount(Item item) {
		if (item.equals(ItemUtil.getItem("coal"))) {
			return 8;
		} else if (item.equals(ItemUtil.getItem("planks"))) {
			return 1;
		}
		return 0;
	}
	public static double getFuelAmount(String item) {
		return getFuelAmount(ItemUtil.getItem(item));
	}

	// TODO: Include chest/furnace slots.
	public List<Integer> getWindowSlotsWithFuel(ContainerType type) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		// oh btw this array is ALSO a PRIORITY LIST (left to right, important to least important)
		String[] fuelNames = new String[] {"coal", "planks"};
		for(String name : fuelNames) {
			for(int invSlot : getInvSlotsForItem(ItemUtil.getItem(name))) {
				result.add(inventorySlotToWindowSlot(type, invSlot));
			}
		}
		return result;
	}

	public Set<Integer> getInventorySlotsWithFood() {
		return foodSlots;
	}
	public int getTotalFoodHungerHealAmount() {
		return totalFoodHunger;
	}
	
	public ItemStack getStack(int inventorySlot) {
		return Minecraft.getMinecraft().player.inventory.getStackInSlot(inventorySlot);
	}
	
	public boolean isFull() {
		return emptySlots <= 0;
	}
	public int getEmptySlots() {
		return emptySlots;
	}

	// Sadly as of now this will go through the ENTIRE inventory each frame. It's not too bad though since there are ~40 slots.
	void updateInventoryData() {
		itemCounts.clear();
		itemSlots.clear();
		foodSlots.clear();
		totalFoodHunger = 0;
		InventoryPlayer inventory = getInventory();
		emptySlots = 0;
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			Item item = stack.getItem();

			if (stack.isEmpty()) {
				++emptySlots;
				continue;
			}

			// Item counts
			if (!itemCounts.containsKey(item)) itemCounts.put(item, 0);
			itemCounts.put(item, itemCounts.get(item) + stack.getCount());

			// Item slots
			if (!itemSlots.containsKey(item)) itemSlots.put(item, new ArrayList<Integer>());
			itemSlots.get(item).add(i);

			// Food slots
			if (item instanceof ItemFood) {
				ItemFood food = (ItemFood) item;
				foodSlots.add(i);
				totalFoodHunger += food.getHealAmount(null) * stack.getCount();
			}
		}
	}
}
