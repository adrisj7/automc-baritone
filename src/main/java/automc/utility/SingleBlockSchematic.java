package automc.utility;

import baritone.api.schematic.FillSchematic;
import baritone.api.utils.BlockOptionalMeta;
import net.minecraft.block.Block;

/**
 * A basic setup, it's just so that we can tell the schematic system to place one block.
 * Really.
 *
 */
public class SingleBlockSchematic extends FillSchematic {
	public SingleBlockSchematic(Block block) {
		super(1, 1, 1, new BlockOptionalMeta(block));
	}
}
