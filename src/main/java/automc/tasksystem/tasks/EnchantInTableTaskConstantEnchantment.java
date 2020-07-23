package automc.tasksystem.tasks;

public class EnchantInTableTaskConstantEnchantment extends EnchantInTableTask {

	int enchantmentPicked;

	public EnchantInTableTaskConstantEnchantment(String item, int enchantmentPicked) {
		super(item);
		this.enchantmentPicked = enchantmentPicked;
	}

	@Override
	protected int pickEnchantment() {
		return enchantmentPicked;
	}

}
