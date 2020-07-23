package automc.utility;

import java.util.List;

import baritone.utils.schematic.StaticSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class ProceduralStaticSchematic extends StaticSchematic {

	public ProceduralStaticSchematic(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        states = new IBlockState[x][z][y];
	}

    @Override
    public IBlockState desiredState(int x, int y, int z, IBlockState current, List<IBlockState> approxPlaceable) {
    	// Null means "any block goes here"
    	if (this.states[x][z][y] == null) return current;
        return this.states[x][z][y];
    }

	// MODIFYING THE THING
	public void setDirect(int x, int y, int z, IBlockState state) {
		/*
		System.out.println("== AW SHEET");
		System.out.println(states.length + ", " + states[0].length + ", " + states[0][0].length);
		System.out.println(x + ", " + z + ", " + y);
		System.out.println("^^ AW SHEET");
		*/
		states[x][z][y] = state;
	}
	public void setDirect(int x, int y, int z, Block block) {
		setDirect(x,y,z,block.getDefaultState());
	}
	@SuppressWarnings("deprecation") // I don't know how else to do it :/
	public void setDirect(int x, int y, int z, Block block, int meta) {
		setDirect(x,y,z, block.getStateFromMeta(meta));
	}
	public void setDirect(int x, int y, int z, String block) {
		setDirect(x,y,z, ItemUtil.getBlock(block));
	}
	public void setDirect(int x, int y, int z, String block, int meta) {
		setDirect(x,y,z, ItemUtil.getBlock(block), meta);
	}

	public void setAny(int x, int y, int z) {
		states[x][z][y] = null;
	}
}
