package ru.liahim.mist.world.biome;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BiomeMistUpLowland extends BiomeMistUpJungleEdge {

	public BiomeMistUpLowland(BiomeProperties properties) {
		super(properties, -999);
		getMistBiomeDecorator().grassPerChunk = 10;
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