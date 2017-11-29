package coolsquid.react.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockWrapper {

	public final IBlockState state;
	public final BlockPos pos;

	public BlockWrapper(IBlockState state, BlockPos pos) {
		this.state = state;
		this.pos = pos;
	}
}