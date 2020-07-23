package automc.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;

import automc.AutoMC;
import automc.utility.ItemUtil;
import net.minecraft.util.math.BlockPos;

// DO NOT USE I realize this is kinda silly.
@Deprecated
public class LocalWorldScanner {

	/**
	 * TODO:
	 * 		- Make two functions: a "slow"/"recompute" version and a "fast"/"usecached" version.
	 * 		- The "use cached" function caches searched blocks, so if the exact same value is searched for again it won't run the slow search.
	 * 		- Make the cache a stack of sorts, holding a set of items.
	 * 		- Make a "pushCache" function. This pushes a new list of sets onto the stack.  
	 * 		- Make a "popCache" function.  This pops the list and removes all blocks from the stack, while keeping blocks that exist in stacks BELOW it.
	 * 
	 * 		- Make a "on chunk update" function that will re calculate the cache of closest block locations when a chunk is updated.
	 * 
	 * 		- ^^^^ FIGURE OUT IF THIS IS EVEN A GOOD IDEA OR NOT BLAGH
	 * 
	 * 		- ALSO keep track of dropped items and have a way to grab the entity data/position of a dropped item of a type.
	 * 		- Then, make a task that grabs said item.
	 * 		- Then, incorporate that task into the base ResourceTask so it grabs nearby dropped items if they're close enough. 
	 */

	/**
	 * The maximum number of block positions we'll store per block/item type. Any more and we'll truncate the furthest blocks.
	 */
	//private static final int MAX_COORDS_PER_TYPE = 64;

	Stack<Set<String>> blocksCachedStack;

	Map<String, SortedSet<String>> cachedBlocks;

	public LocalWorldScanner() {
		blocksCachedStack = new Stack<>();
		cachedBlocks = new HashMap<>();
	}

	public List<BlockPos> recalculateClosestBlocks(String id, int maximum, int searchX, int searchZ, double maxRegionDistanceSq) {
		// Use baritone's already searched for crap
		// TODO: Figure out whether "WorldScanner" is better for this. Test both of them out.
		return AutoMC.getAutoMC().getBaritone().getWorldProvider().getCurrentWorld().getCachedWorld().getLocationsOf(id, maximum, searchX, searchZ, (int) maxRegionDistanceSq);
	}

	public List<BlockPos> getClosestBlocksFast(String id, int maximum, int searchX, int searchZ) {
		return null;
	}

	public void pushCacheStack(String ...blockIDs) {
		Set<String> pushed = new HashSet<String>(blockIDs.length);
		for (String id : blockIDs) {
			pushed.add(ItemUtil.findId(id));
		}
		blocksCachedStack.add(pushed);
	}

	public void popCacheStack() {
		Set<String> removed = blocksCachedStack.pop();
		// Iterate through removed, check if block still exists, if not then free blocks cached memory.
		for (String mightRemove : removed) {
			if (!shouldBeCached(mightRemove)) {
				if (cachedBlocks.containsKey(mightRemove)) {
					// Free "mightRemove" from memory.
					cachedBlocks.remove(mightRemove);
				}
			}
		}
	}

	private boolean shouldBeCached(String id) {
		id = ItemUtil.findId(id);
		for(Set<String> bset : blocksCachedStack) {
			if (bset.contains(id)) return true;
		}
		return false;
	}

}
