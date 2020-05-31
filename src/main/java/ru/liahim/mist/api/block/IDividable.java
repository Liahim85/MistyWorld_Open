package ru.liahim.mist.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public interface IDividable {

	public Block getFullBlock();

	public default IBlockState getFullState(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getFullState(state);
		return getFullBlock().getDefaultState();
	}

	public default Block getStepBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getStepBlock(state);
		return null;
	}

	public default IBlockState getSlabBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getSlabBlock(state);
		return null;
	}

	public default Block getStairsBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getStairsBlock(state);
		return null;
	}
}