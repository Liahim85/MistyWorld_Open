package ru.liahim.mist.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMossable {

	public boolean setMossy(IBlockState state, World world, BlockPos pos);
}