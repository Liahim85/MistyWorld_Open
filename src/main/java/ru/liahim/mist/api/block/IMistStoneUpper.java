package ru.liahim.mist.api.block;

import net.minecraft.block.state.IBlockState;

public interface IMistStoneUpper extends IMistStone {	

	public default boolean isUpperStone(IBlockState state) {
		return true;
	}
}