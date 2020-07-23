package automc.tasksystem.tasks.resources;

import automc.AutoMC;
import automc.definitions.ContainerType;
import automc.player.Inventory;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.DoStuffInContainerTask;
import automc.utility.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

// Places an item (or multiple) from our inventory into a chest.
public class MoveItemsToAnyChestTask extends DoStuffInContainerTask {

	private String[] items;
	private int slotsToMove;

	public MoveItemsToAnyChestTask(String[] items, int slotsToMove) {
		super("chest");
		this.items = items;
		this.slotsToMove = slotsToMove;		
	}

	public MoveItemsToAnyChestTask(Item item, int slotsToMove) {
		this(ItemUtil.getItemId(item), slotsToMove);
	}
	public MoveItemsToAnyChestTask(String item, int slotsToMove) {
		this(new String[] {item}, slotsToMove);
	}

	// This constructor moves ALL items of that type.
	public MoveItemsToAnyChestTask(Item item) {
		this(item, -1);
	}
	public MoveItemsToAnyChestTask(String item) {
		this(item, -1);
	}
	public MoveItemsToAnyChestTask(String[] items) {
		this(items, -1);
	}


	@Override
	protected boolean isUIOpened() {
		return AutoMC.getAutoMC().player.inventory.isChestOpened();
	}

	@Override
	protected Task getUISubTask(BlockPos containerPos) {
		// Move the item(s) into the chest until we've emptied out enough slots.
		int slotsMoved = 0;
		for (String item : items) {
			if (slotsToMove != -1 && slotsMoved >= slotsToMove) {
				break;
			}
			for(int invSlot : AutoMC.getAutoMC().player.inventory.getInvSlotsForItem(item)) {
				if (slotsToMove != -1 && slotsMoved >= slotsToMove) {
					break;
				}
				int winSlot = Inventory.inventorySlotToWindowSlot(ContainerType.CHEST, invSlot);
				//int count = Minecraft.getMinecraft().player.inventory.getStackInSlot(invSlot).getCount();
				AutoMC.getAutoMC().player.inventory.quickMoveFromSlot(ContainerType.CHEST, winSlot);
				++slotsMoved;
			}
		}
		stop();
		return null;
	}

	@Override
	protected Task getPrerequisiteTask() {
		// Nothing is needed. DoStuffInContainerTask automatically crafts a chest if we don't find one.
		return null;
	}

	@Override
	public boolean isDone() {
		// We quit manually.
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		if (!isUIOpened()) {
			// Return whether we have the items to place in the chest.
			//return AutoMC.getAutoMC().player.inventory.getItemCount(item) >= slotsToMove;
		}
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof MoveItemsToAnyChestTask) {
			MoveItemsToAnyChestTask pinct = (MoveItemsToAnyChestTask) t;
			if (pinct.slotsToMove != slotsToMove) {
				return false;
			}
			if (pinct.items.length != items.length) {
				return false;
			}
			for (int i = 0; i < items.length; ++i) {
				if (!pinct.items[i].equals(items[i])) return false;
			}
			return true;
		}
		return false;
	}

}
