package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BiomeMistUpMarsh extends BiomeMistUpSwampBase {

	private final boolean floatingMat;

	public BiomeMistUpMarsh(BiomeProperties properties, boolean floatingMat) {
		super(properties, -999, false);
		getMistBiomeDecorator().grassPerChunk = 10;
		this.floatingMat = floatingMat;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getGrassColorAtPos(BlockPos pos) {
		return 0x7ab048;
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
	protected void placeTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (this.floatingMat && y < MistWorld.seaLevelUp && state.getBlock() != MistBlocks.CLAY) {
			if (chunkPrimer.getBlockState(x, y + 2, z).getBlock() == Blocks.AIR) {
				chunkPrimer.setBlockState(x, y + 1, z, Blocks.STAINED_GLASS.getDefaultState()); //This for the Lake Decoration
			} else if (noises.get(3) > 0.0D && (chunkPrimer.getBlockState(x, y + 3, z).getBlock() == Blocks.AIR)) {
				chunkPrimer.setBlockState(x, y + 2, z, Blocks.STAINED_GLASS.getDefaultState()); //This for the Lake Decoration
			}
			if (y < MistWorld.seaLevelUp - 1 && noises.get(2) > 1.0D - (MistWorld.seaLevelUp - y) / 10.0D) {
				chunkPrimer.setBlockState(x, y, z, MistBlocks.SAPROPEL.getDefaultState());
			} else chunkPrimer.setBlockState(x, y, z, state);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	public boolean placePlant(World world, BlockPos pos, Random rand) {
		if (world.isAirBlock(pos)) {
			int i = rand.nextInt(4);
			if (i == 0) {
				if (Blocks.TALLGRASS.canBlockStay(world, pos, grass)) return world.setBlockState(pos, grass, 2);
			} else if (i == 1) {
				if (Blocks.TALLGRASS.canBlockStay(world, pos, grass)) return world.setBlockState(pos, grassDown, 2) && world.setBlockState(pos.up(), grassUp, 2);
			}
		}
		return false;
	}
}