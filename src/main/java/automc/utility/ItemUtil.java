package automc.utility;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public interface ItemUtil {

	// TODO TODO TODO: Access BLOCK STATES
	/*
	public static <VariantType, EnumType> Item getItemBlock(String name, Property<VariantType> property, EnumType value) {
		Item item = getItem(name);
		Block b = Block.getBlockFromItem(item);
		b.getDefaultState().withProperty(property, value);
	}
	*/

	public static Item getItem(String name) {
		if (name == null) return null;
		name = parseAliases(name);
		if (!name.contains(":")) {
			name = "minecraft:" + name;
		}
		ResourceLocation loc = new ResourceLocation(name);
		if (!Item.REGISTRY.containsKey(loc)) return null;
		return Item.REGISTRY.getObject(loc);
	}
	public static String getItemId(Item item) {

		if (item == null) return "null";
		String key = Item.REGISTRY.getNameForObject(item).toString();
		if (key.startsWith("item.")) {
			key = key.substring("item.".length());
		} else if (key.startsWith("tile.")) {
			key = key.substring("tile.".length());
		} else if (key.startsWith("minecraft:")) {
			key = key.substring("minecraft:".length());
		}
//		Logger.log("boof1: " + key);
		key = parseAliases(key);
//		Logger.log("boof2: " + key);
		return key;
	}

	// Returns the SIMPLEST version of the id that is CONSISTENT and can be used for item hash maps.
	public static String findId(String complexId) {
		return getItemId(getItem(complexId));
	}

	public static Block getBlock(String name) {
		return Block.getBlockFromItem(getItem(name));
	}

	public static IRecipe createPureShapedRecipe(int width, int height, String ...items) {
		NonNullList<Ingredient> ingredients = new NonNullListPublic<Ingredient>();
		for(String item : items) {
			ingredients.add(Ingredient.fromItem(getItem(item)));
		}

		return new ShapedRecipes("minecraft", width, height, ingredients, null);
	}
	public static IRecipe createPureShapelessRecipe(String ...items) {
		NonNullList<Ingredient> ingredients = new NonNullListPublic<Ingredient>();
		for(String item : items) {
			ingredients.add(Ingredient.fromItem(getItem(item)));
		}

		return new ShapelessRecipes("minecraft", null, ingredients);
	}

	// If the name has an alias (for some reason), return the value we more commonly use.
	static String parseAliases(String name) {
		switch (name) {
		case "wood":
			return "planks";
		case "workbench":
			return "crafting_table";
		case "pickaxeWood":
			return "wooden_pickaxe";
		default:
			return name;
		}
	}

	public static boolean recipesEqual(IRecipe r1, IRecipe r2) {
		if (r1.getIngredients().size() != r2.getIngredients().size()) return false;
		for (int i = 0; i < r1.getIngredients().size(); ++i) {
			Ingredient i1 = r1.getIngredients().get(i),
					   i2 = r2.getIngredients().get(i);
			if (i1.getMatchingStacks().length != i2.getMatchingStacks().length) return false;
			for (int j = 0; j < i1.getMatchingStacks().length; j++) {
				ItemStack is1 = i1.getMatchingStacks()[j],
						  is2 = i2.getMatchingStacks()[j];
				if (is1.getCount() != is2.getCount()) return false;
				if (!ItemUtil.getItemId(is1.getItem()).equals(ItemUtil.getItemId(is2.getItem())) ) return false;
			}
		}
		return true;
	}
	
	public static boolean itemsEqual(Item i1, Item i2) {
		return getItemId(i1).equals(getItemId(i2));
	}
}
