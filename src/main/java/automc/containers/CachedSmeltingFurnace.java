package automc.containers;

import java.lang.reflect.Field;

import automc.AutoMC;
import automc.Logger;
import automc.definitions.ContainerType;
import automc.player.Inventory;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * This caches the data from a single furnace that's relevant to us.
 *
 */
public class CachedSmeltingFurnace {

	private ItemStack material;
	private ItemStack output;

	private ItemStack fuel;

	private double burningFuel;
	private double burnProgress;

	public CachedSmeltingFurnace() {
		// Empty
		material = null;
		output = null;
		fuel = null;
		burningFuel = 0;
	}

	public void updateValuesByOpenContainer() {
		if (AutoMC.getAutoMC().player.inventory.isFurnaceOpened()) {
			material = 	AutoMC.getAutoMC().player.inventory.getItemStackInWindowSlot(ContainerType.FURNACE, 0);
			fuel = 		AutoMC.getAutoMC().player.inventory.getItemStackInWindowSlot(ContainerType.FURNACE, 1);
		    output = 	AutoMC.getAutoMC().player.inventory.getItemStackInWindowSlot(ContainerType.FURNACE, 2);
		    burningFuel = furnaceFuelLeft();
		    burnProgress = furnaceBurnProgress();
		    Logger.log("TEMP burn progress: " + burnProgress);
		} else {
			/*
			// Container is not furnace, so reset.
			material = null;
			output = null;
			fuel = null;
			burningFuel = 0;
			*/
		}
	}

	//// ACCESSORS

	public Item getMaterial() {
		if (material == null) return null;
		return material.getItem();
	}
	public Item getOutput() {
		if (output == null) return null;
		return output.getItem();
	}
	public Item getFuelType() {
		if (fuel == null) return null;
		return fuel.getItem();
	}

	public int getMaterialCount() {
		if (material == null) return 0;
		return material.getCount();
	}
	public int getOutputCount() {
		if (output == null) return 0;
		return output.getCount();
	}
	public double getTotalFuel() {
		if (fuel == null) return 0;
		return Inventory.getFuelAmount(fuel) + burningFuel;
	}
	public double getBurnProgress() {
		return burnProgress;
	}
	
	public boolean hasFuelStored() {
		if (fuel == null) return false;
		return !fuel.isEmpty();
	}

	public int fuelStillNeeded() {
		return (int)Math.ceil(getMaterialCount() - getTotalFuel()); 
	}

	//// ACCESSORS (end)

	private double furnaceFuelLeft() {
		if (AutoMC.getAutoMC().player.inventory.isFurnaceOpened()) {
			ContainerFurnace cfurnace = (ContainerFurnace) Minecraft.getMinecraft().player.openContainer;
			try {
				Field m = ContainerFurnace.class.getDeclaredField("tileFurnace");
				m.setAccessible(true);
				IInventory inv = (IInventory)m.get(cfurnace);
				return (double)inv.getField(0) / 200;
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				System.out.println("test invalid");
			}
		}
		System.out.println("Furnace was empty, zero fuel left.");
		return 0;
	}
	
	private double furnaceBurnProgress() {
		if (AutoMC.getAutoMC().player.inventory.isFurnaceOpened()) {
			ContainerFurnace cfurnace = (ContainerFurnace) Minecraft.getMinecraft().player.openContainer;
			try {
				Field m = ContainerFurnace.class.getDeclaredField("tileFurnace");
				m.setAccessible(true);
				IInventory inv = (IInventory)m.get(cfurnace);
				return (double)inv.getField(2) / 200;
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				System.out.println("test invalid");
			}
		}
		System.out.println("Furnace was empty, zero fuel left.");
		return 0;
	}
}
