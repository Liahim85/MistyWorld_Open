package ru.liahim.mist.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISeasonalChanges {

	public default IBlockState getSeasonState(World world, BlockPos pos, IBlockState state, long monthTick) {
		return null;
	}
}