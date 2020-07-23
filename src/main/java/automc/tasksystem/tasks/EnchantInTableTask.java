package automc.tasksystem.tasks;

import automc.AutoMC;
import automc.Logger;
import automc.tasksystem.Task;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.util.math.BlockPos;

public abstract class EnchantInTableTask extends DoStuffInContainerTask {

	String item;

	public EnchantInTableTask(String item) {
		super("enchanting_table");
		this.item = item;
	}

	@Override
	protected boolean isUIOpened() {
		return AutoMC.getAutoMC().player.inventory.isEnchantmentTableOpened();
	}

	@Override
	protected Task getUISubTask(BlockPos containerPos) {
		Logger.log("Enchanting!");
		AutoMC.getAutoMC().player.inventory.putItemInEnchantingTable("book");
		int enchantment = 3;
		AutoMC.getAutoMC().player.inventory.putLapisInEnchantingTable(enchantment);
		ContainerEnchantment c = (ContainerEnchantment) Minecraft.getMinecraft().player.openContainer;
		if (enchantment <= c.enchantLevels.length) {
			//c.enchantItem(Minecraft.getMinecraft().player, enchantment - 1);
			Minecraft.getMinecraft().playerController.sendEnchantPacket(c.windowId, enchantment - 1);
			AutoMC.getAutoMC().player.inventory.getEnchantingTableOutput();
		}
		stop();
		return null;
	}

	@Override
	protected Task getPrerequisiteTask() {
		// TODO: Get lapis maybe? Depends on what we pick for our enchantment.
		return null;
	}

	@Override
	public boolean isDone() {
		// We manually stop.
		return false;
	}

	@Override
	protected boolean areConditionsMet() {
		return true;
	}

	@Override
	public boolean areEqual(Task t) {
		if (t instanceof EnchantInTableTask) {
			return ((EnchantInTableTask)t).item.equals(item);
		}
		return false;
	}

	/**
	 * Return: The index of the enchantment that you pick. Either 1, 2 or 3.
	 */
	protected abstract int pickEnchantment();

}
