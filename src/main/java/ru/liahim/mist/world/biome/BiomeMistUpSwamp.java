package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeMistUpSwamp extends BiomeMistUpSwampBase {

	public BiomeMistUpSwamp(BiomeProperties properties) {
		super(properties, 1, true);
		getMistBiomeDecorator().grassPerChunk = 5;
		this.getMistBiomeDecorator().waterlilyPerChunk = 5;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getGrassColorAtPos(BlockPos pos) {
		double d0 = GRASS_COLOR_NOISE.getValue(pos.getX() * 0.1D, pos.getZ() * 0.1D);
		return d0 < -0.1D ? 0x76a95c : 0x6da354;
	}

	/** 0 - Clay, 1 - Gravel, 2 - Sapropel, 3 - Floating Mat */
	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = super.getNoises(x, z);
		noises.add(FLOATING_MAT_NOISE.getValue(x * 0.3D, z * 0.3D));
		return noises;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.7D ? MistBlocks.CLAY.getDefaultState() : MistBlocks.SAPROPEL.getDefaultState();
	}

	@Override
	protected void placeTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (y < MistWorld.seaLevelUp && state.getBlock() == MistBlocks.SAPROPEL) {
			if (chunkPrimer.getBlockState(x, y + 2, z).getBlock() == Blocks.AIR) {
				chunkPrimer.setBlockState(x, y, z, MistBlocks.PEAT.getDefaultState());
				chunkPrimer.setBlockState(x, y + 1, z, Blocks.STAINED_GLASS.getDefaultState()); //This for the Lake Decoration
			} else if (noises.get(3) > 0.0D && (chunkPrimer.getBlockState(x, y + 3, z).getBlock() == Blocks.AIR)) {
				chunkPrimer.setBlockState(x, y, z, MistBlocks.PEAT.getDefaultState());
				chunkPrimer.setBlockState(x, y + 2, z, Blocks.STAINED_GLASS.getDefaultState()); //This for the Lake Decoration
			} else chunkPrimer.setBlockState(x, y, z, state);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	protected void placeSecondTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (y < MistWorld.seaLevelUp && state.getBlock() == MistBlocks.DIRT_C) {
			Block blockUp = chunkPrimer.getBlockState(x, y + 1, z).getBlock();
			if (blockUp == MistBlocks.SAPROPEL || blockUp == MistBlocks.PEAT) {
				chunkPrimer.setBlockState(x, y, z, state.withProperty(MistDirt.HUMUS, 2));
			} else if (noises.get(3) > 0.2D) {
				chunkPrimer.setBlockState(x, y, z, MistBlocks.PEAT.getDefaultState());
			} else chunkPrimer.setBlockState(x, y, z, state);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}
}