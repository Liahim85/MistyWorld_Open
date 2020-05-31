package ru.liahim.mist.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IMistAdsorbent {

	/**Damage from fog will not act at a distance of one block from the specified block.*/
	public boolean isMistAdsorbent(World world, BlockPos pos, IBlockState state);
}