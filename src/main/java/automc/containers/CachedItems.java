package automc.containers;

import automc.utility.ItemUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

//Serializable.
public class CachedItems {
	private String item;
	private int count;

	public CachedItems(String item, int count) {
		this.item = item;
		this.count = count;
	}
	public CachedItems(ItemStack stack) {
		this(ItemUtil.getItemId(stack.getItem()), stack.getCount());
	}
	
	public String getItemName() {
		return item;
	}
	public int getCount() {
		return count;
	}
	public Item getItem() {
		return ItemUtil.getItem(item);
	}
	
	public void addItems(int add) {
		count += add;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof CachedItems) {
			CachedItems cs = (CachedItems)o;
			return item.equals(cs.item) && count == cs.count;
		}
		return false;
	}
}
