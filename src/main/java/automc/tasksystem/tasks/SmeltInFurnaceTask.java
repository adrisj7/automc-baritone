package automc.tasksystem.tasks;

import java.util.List;

import automc.AutoMC;
import automc.Logger;
import automc.containers.CachedSmeltingFurnace;
import automc.definitions.ContainerType;
import automc.player.Inventory;
import automc.tasksystem.Task;
import automc.tasksystem.tasks.resources.CollectFuelTask;
import automc.utility.ItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class SmeltInFurnaceTask extends DoStuffInContainerTask {

	private Item material;
	private Item target;
	private int requiredAmount;

	private CachedSmeltingFurnace furnace;

	private BlockPos furnacePos = null;

	public SmeltInFurnaceTask(Item material, Item target, int requiredAmount) {
		super("furnace");
		this.material = material;
		this.target = target;
		this.requiredAmount = requiredAmount;
		furnace = new CachedSmeltingFurnace();
	}
	public SmeltInFurnaceTask(String material, String target, int requiredAmount) {
		this(ItemUtil.getItem(material), ItemUtil.getItem(target), requiredAmount);
	}

	@Override
	protected boolean isUIOpened() {
		return AutoMC.getAutoMC().player.inventory.isFurnaceOpened();
	}

	@Override
	protected Task getUISubTask(BlockPos containerPos) {
		// Here we assume:
		//	- Furnace is opened
		//  - We have the materials to burn inside the furnace & inventory
		//  - We have the fuel to do it, if we don't have the fuel it will force itself out of this function.

		this.furnacePos = containerPos;

		// Update every frame.
		furnace.updateValuesByOpenContainer();

		if (!ItemUtil.getItemId(furnace.getMaterial()).equals(ItemUtil.getItemId(material))) {
			// The current materials in the furnace are invalid, move them to our inventory.
			AutoMC.getAutoMC().player.inventory.quickMoveFromSlot(ContainerType.FURNACE, 0);
			furnace.updateValuesByOpenContainer();
		}

		// Check if we have materials left to fill in the furnace.
		int needToSmelt = requiredAmount - (AutoMC.getAutoMC().player.inventory.getItemCount(target) + furnace.getOutputCount());
		int materialNeeded = needToSmelt - furnace.getMaterialCount();

		if (materialNeeded > 0) {
			List<Integer> invSlots = AutoMC.getAutoMC().player.inventory.getInvSlotsForItem(material);
			for(int invSlot : invSlots) {
				int slot = Inventory.inventorySlotToWindowSlot(ContainerType.FURNACE, invSlot);
				ItemStack stack = AutoMC.getAutoMC().player.inventory.getItemStackInWindowSlot(ContainerType.FURNACE, slot);
				int amount = (stack.getCount() > materialNeeded)? materialNeeded : stack.getCount();
				materialNeeded -= AutoMC.getAutoMC().player.inventory.moveItems(ContainerType.FURNACE, slot, 0, amount);
				furnace.updateValuesByOpenContainer();
				if (materialNeeded <= 0) {
					break;
				}
			}
			if (materialNeeded > 0) {
				Logger.logError("Furnace still needs " + materialNeeded + " raw materials and you were unable to fill the furnace! This will cause problems.");
			}
		}

		// Fuel
		int materialCount = furnace.getMaterialCount();
		double fuel = furnace.getTotalFuel();
		double fuelNeeded = (double)materialCount - fuel;

		if (fuelNeeded > 0) {
			List<Integer> fuelSlots = AutoMC.getAutoMC().player.inventory.getWindowSlotsWithFuel(ContainerType.FURNACE);
			for (int slot : fuelSlots) {
				ItemStack stack = AutoMC.getAutoMC().player.inventory.getItemStackInWindowSlot(ContainerType.FURNACE, slot);
				if (!furnace.hasFuelStored() || ItemUtil.itemsEqual(stack.getItem(), furnace.getFuelType())) {
					int amount = (stack.getCount() > fuelNeeded)? (int)Math.ceil(fuelNeeded) : stack.getCount();
					fuelNeeded -= AutoMC.getAutoMC().player.inventory.moveItems(ContainerType.FURNACE, slot, 1, amount);
					furnace.updateValuesByOpenContainer();
					if (fuelNeeded <= 0) {
						break;
					}					
				}
			}
			// If we don't have enough fuel INSIDE it's ok, we may just need to wait for our furnace to run out of this particular fuel type.
		}

		// Output: Take it all.
		if (furnace.getOutputCount() > 0) {
			log("Taking from furnace output.");
			AutoMC.getAutoMC().player.inventory.quickMoveFromSlot(ContainerType.FURNACE, 2);
			furnace.updateValuesByOpenContainer();
		}

		return null;
	}

	@Override
	protected boolean areConditionsMet() {
		// TODO: combined with cached furnace, we have:
		//	- The materials to burn that will give us the REQUIRED amount
		//  - The fuel to burn the materials we still are REQUIRED to burn
		int totalMaterials = AutoMC.getAutoMC().player.inventory.getItemCount(material) + furnace.getMaterialCount();
		int totalTarget    = AutoMC.getAutoMC().player.inventory.getItemCount(target) + furnace.getOutputCount();
		double totalFuel = AutoMC.getAutoMC().player.inventory.getValidFuelAmount() + furnace.getTotalFuel();
		int remaining = requiredAmount - totalTarget;
		Logger.log("Total: " + totalMaterials + " " + totalTarget + ", " + requiredAmount);
		return (totalMaterials + totalTarget >= requiredAmount);// && (totalFuel >= remaining);
	}

	@Override
	public boolean isDone() {
		// Only when we have enough materials do we stop.
		if (AutoMC.getAutoMC().player.inventory.getItemCount(target) >= requiredAmount) {
			Logger.log("TEST: DONE SMELTING: " + ItemUtil.getItemId(target) + " > " + requiredAmount);
			return true;
		}
		return false;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof SmeltInFurnaceTask) {
			SmeltInFurnaceTask sft = (SmeltInFurnaceTask) t;
			if (requiredAmount != sft.requiredAmount) return false;
			if (!ItemUtil.getItemId(material).equals(ItemUtil.getItemId(sft.material))) return false;
			if (!ItemUtil.getItemId(target).equals(ItemUtil.getItemId(sft.target))) return false;
			return true;
		}
		return false;
	}

	@Override
	protected Task getPrerequisiteTask() {
		// Get fuel.
		furnace.updateValuesByOpenContainer();
		int targetFuel = inventoryFuelTarget();
		Logger.log("FUEL:" + targetFuel);
		if (targetFuel > AutoMC.getAutoMC().player.inventory.getValidFuelAmount()) {
			Logger.log("collect fuel: " + targetFuel);
			return new CollectFuelTask(targetFuel);
		}
		return null;
	}

	private int inventoryFuelTarget() {
		// TODO TODO: Consider the progress of the "arrow" that the fuel is smelting. Consider that portion "smelted".
		int totalTarget    = AutoMC.getAutoMC().player.inventory.getItemCount(target) + furnace.getOutputCount();
		double inventoryFuel = AutoMC.getAutoMC().player.inventory.getValidFuelAmount();
		double totalFuelAvailable = inventoryFuel + furnace.getTotalFuel();
		double remaining = (double)(requiredAmount - totalTarget) - furnace.getBurnProgress(); // How many MORE of the target items must we smelt?
		// We subtract by 1 because WHILE FUEL GETS USED, we ALSO MAKE PROGRESS on the ITEM SMELTED.
		// If we have more materials remaining than fuel avaialable, gather that much fuel EXTRA in our inventory.
//		Logger.log("We need: " + remaining + ", but have " + totalFuelAvailable);
		return (int)Math.ceil(remaining - totalFuelAvailable);
	}

	protected BlockPos getPositionOverride() {
		// If the block no longer holds a furnace, there is no position.
		if (furnacePos == null) return null;
		if (  !ItemUtil.itemsEqual(
				Item.getItemFromBlock(Minecraft.getMinecraft().world.getBlockState(furnacePos).getBlock()),
				ItemUtil.getItem("furnace"))) {
			furnacePos = null;
		}
		return furnacePos;
	}
}
