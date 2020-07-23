package automc.items;

import static net.minecraft.client.Minecraft.getMinecraft;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import automc.Logger;
import automc.utility.ItemUtil;
import automc.utility.SettingsUtil;
import net.minecraft.item.Item;

/**
 * 		When looking for items in chests & furnaces, we are willing to travel further for some items than others.
 */
 
public class ItemWorkDictionary {

	private static final Path WORK_DICTIONARY_PATH = getMinecraft().gameDir.toPath().resolve("automc").resolve("item_work_dictionary.json");

	// If data is not found at all, just use this.
	private static final double DEFAULT_WORK_VALUE = 10;

	// Used to calculate how many ticks we'd be worth spending to get to an item.
	private static final double DEFAULT_TRAVEL_BLOCKS_PER_SECOND = 4.3 * 1.5; // Add more weight to it.

	ItemWorkData data = null;

	public ItemWorkDictionary() {
		
	}

	public void init() {
		load();
	}

	// TODO: Standardize.
	private void copyOverDefaults(String defaultResourcePath, Path realPath) {
		if (!Files.exists(realPath)) {
			realPath.toFile().getParentFile().mkdirs();
			try(InputStream in = getClass().getResourceAsStream(defaultResourcePath)) {
				if (in == null) throw new IOException();
				Files.copy(in, realPath);
			} catch (IOException e) {
				Logger.logError("Failed to copy over default file to the real minecraft path. From: " + defaultResourcePath + ", to: " + realPath);
				e.printStackTrace();
			}
		}
	}

	private void load() {
		copyOverDefaults("/automc_resources/item_work_dictionary.json", WORK_DICTIONARY_PATH);
		try {
			data = SettingsUtil.readJson(WORK_DICTIONARY_PATH, ItemWorkData.class);
			data.onPostSerialize();
        } catch (IOException e) {
        	Logger.logError("Failed to read work dictionary file at " + WORK_DICTIONARY_PATH.toString(), false);
			e.printStackTrace();
        }
	}

	public double getWork(Item item) {
		if (data == null) return DEFAULT_WORK_VALUE;
		return data.getWork(item);
	}
	public double getWork(String item) {
		return getWork(ItemUtil.getItem(item));
	}

	public double getWorkTicks(Item item) {
		double baseSpeed = DEFAULT_TRAVEL_BLOCKS_PER_SECOND / 20.0; // (blocks / sec) / (ticks / sec) = (blocks / tick)
		return getWork(item) / baseSpeed; // blocks / (blocks / tick) = ticks
	}
	public double getWorkTicks(String item) {
		return getWorkTicks(ItemUtil.getItem(item));
	}

	static class ItemWorkData {
		private double defaultWork = -1;
		private Map<String, Double> workMap;
		
		private transient Map<Item, Double> workRealMap;

		public void onPostSerialize() {
			if (workMap == null) return;
			workRealMap = new HashMap<Item, Double>();
			for(String key : workMap.keySet()) {
				workRealMap.put(ItemUtil.getItem(key), workMap.get(key));
			}
		}

		public double getWork(Item item) {
			if (workRealMap == null) return getDefaultWork();
			if (!workRealMap.containsKey(item)) return getDefaultWork();
			return workRealMap.get(item);
		}

		public double getDefaultWork() {
			if (defaultWork == -1) {
				return DEFAULT_WORK_VALUE;
			}
			return defaultWork;
		}

	}
}
