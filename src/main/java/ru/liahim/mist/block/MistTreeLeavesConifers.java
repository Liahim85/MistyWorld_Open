package ru.liahim.mist.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistTreeLeavesConifers extends MistTreeLeaves {

	public MistTreeLeavesConifers(int baseColor, boolean mixColor, int bloomMonth, int spoilMonth) {
		super(baseColor, mixColor, bloomMonth, spoilMonth);
	}

	public MistTreeLeavesConifers(int baseColor, int bloomMonth, int spoilMonth) {
		this(baseColor, true, bloomMonth, spoilMonth);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (state.getValue(DIR) == EnumFacing.UP) {
			IBlockState checkState = world.getBlockState(pos.up());
			if (checkState.getBlock() == this && checkState.getValue(DIR) == EnumFacing.DOWN) {
				world.setBlockToAir(pos.up());
			}
		}
	}
}