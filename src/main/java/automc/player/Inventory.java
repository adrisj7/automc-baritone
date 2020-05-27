package automc.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * An interface for our inventory that lets us view inventory stats.
 */
public class Inventory {

	static boolean SHOW_INVENTORY = false;
	
	HashMap<Item, Integer> itemCounts;
	HashMap<Item, List<Integer>> itemSlots;

	public Inventory() {
		itemCounts = new HashMap<Item, Integer>();
		itemSlots = new HashMap<Item, List<Integer>>();
	}

	public int getItemCount(Item item) {
		return itemCounts.containsKey(item)? itemCounts.get(item) : 0;
	}
	public int getItemCount(String item) {
		return getItemCount(ItemUtil.getItem(item));
	}
	public boolean hasItem(Item item) {
		return getItemCount(item) > 0;
	}
	public boolean hasItem(String item) {
		return hasItem(ItemUtil.getItem(item));
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
				return hasItem(ItemUtil.getItem("minecraft:diamond_pickaxe"));
			case IRON:
				return hasItem(ItemUtil.getItem("minecraft:iron_pickaxe"))
					|| hasItem(ItemUtil.getItem("minecraft:gold_pickaxe"))
					|| miningRequirementMet(MiningRequirement.DIAMOND);
			case STONE:
				return hasItem(ItemUtil.getItem("minecraft:stone_pickaxe"))
					|| miningRequirementMet(MiningRequirement.IRON);
			case WOOD:
				return hasItem(ItemUtil.getItem("minecraft:wooden_pickaxe"))
					|| miningRequirementMet(MiningRequirement.STONE);
		}
		return false;
	}

	public List<Integer> getInvSlotsForItem(Item item) {
		return itemSlots.containsKey(item)? itemSlots.get(item) : new ArrayList<Integer>();
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
		for(int check : getInputSlots(type)) {
			System.out.println("slot check: " + check + ", " + type);
			if (container.getSlot(check).getHasStack()) {
				filledSlots.add(check);
			}
		}

		if (isStupidCrafting && filledSlots.size() == 0) {
			System.err.println("Something fucked up here... After crafting there is nothing in our slots.");
			return false;
//			filledSlots.add(getInputSlots(type)[0]);
		}

		// Convoluted but functional method
		// First: Swap from the crafting output
//		controller.windowClick(playerInventory, 0, 1, ClickType.SWAP, player);
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

	public ItemStack getItemStackInWindowSlot(ContainerType type, int windowSlot) {
		Container c = getContainer(type);
		if (windowSlot >= c.inventorySlots.size()) return null;
		return c.getSlot(windowSlot).getStack();
	}

	public boolean isChestOpened() {
		return (Minecraft.getMinecraft().player.openContainer instanceof ContainerChest);
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
	
	private int[] getInputSlots(ContainerType type) {
		switch (type) {
		case PLAYER:
			return new int[] {1, 2, 3, 4};
		case CRAFTING:
			return new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
		case FURNACE:
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
		default:
			return 0;
		}
	}

	private Container getContainer(ContainerType type) {
		Container c;
		switch (type) {
		case PLAYER:
			return Minecraft.getMinecraft().player.inventoryContainer;
		case CRAFTING:
			c = Minecraft.getMinecraft().player.openContainer;
			if (c instanceof ContainerWorkbench) {
				return c;
			}
			break;
		case FURNACE:
			c = Minecraft.getMinecraft().player.openContainer;
			if (c instanceof ContainerFurnace) {
				return c;
			}
			break;
		case CHEST:
			c = Minecraft.getMinecraft().player.openContainer;
			if (c instanceof ContainerChest) {
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

	// Sadly as of now this will go through the ENTIRE inventory each frame. It's not too bad though since there are ~40 slots.
	void updateInventoryData() {
		itemCounts.clear();
		itemSlots.clear();
		InventoryPlayer inventory = getInventory();
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			Item item = stack.getItem();

			// Item counts
			if (!itemCounts.containsKey(item)) itemCounts.put(item, 0);
			itemCounts.put(item, itemCounts.get(item) + stack.getCount());

			// Item slots
			if (!itemSlots.containsKey(item)) itemSlots.put(item, new ArrayList<Integer>());
			itemSlots.get(item).add(i);
		}
	}
}
