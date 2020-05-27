package automc.utility;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
		if (!name.contains(":")) {
			name = "minecraft:" + name;
		}
		ResourceLocation loc = new ResourceLocation(name);
		if (!Item.REGISTRY.containsKey(loc)) return null;
		return Item.REGISTRY.getObject(loc);
	}
	public static String getItemId(Item item) {
		if (item == null) return "null";
		String key = item.getTranslationKey();
		if (key.startsWith("item.")) {
			key = key.substring("item.".length());
		} else if (key.startsWith("tile.")) {
			key = key.substring("tile.".length());
		}
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
}
