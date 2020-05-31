package ru.liahim.mist.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**@author Liahim*/
public class WorldUtil {

	/** Caution! Applies only to replace the foliage with a similar one. */
	public static void simpleSetBlock(World world, BlockPos pos, IBlockState state, boolean nullCheck) {
		if (nullCheck) {
			ExtendedBlockStorage ebs = world.getChunkFromBlockCoords(pos).getBlockStorageArray()[pos.getY() >> 4];
			if (ebs != Chunk.NULL_BLOCK_STORAGE) ebs.set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
		} else world.getChunkFromBlockCoords(pos).getBlockStorageArray()[pos.getY() >> 4].set(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, state);
	}

	/** Caution! Applies only to replace the foliage with a similar one. */
	public static void simpleSetBlock(World world, BlockPos pos, IBlockState state) {
		simpleSetBlock(world, pos, state, false);
	}
}