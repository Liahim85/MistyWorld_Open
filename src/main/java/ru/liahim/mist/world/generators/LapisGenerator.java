package ru.liahim.mist.world.generators;

import java.util.Random;

import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.common.Mist;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

public class LapisGenerator extends WorldGenerator {
	
	private final WorldGenerator lapisGen = new WorldGenMinable(MistBlocks.LAPIS_ORE.getDefaultState(), 5, BlockMatcher.forBlock(MistBlocks.STONE));

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		if (world.provider.getDimension() == Mist.getID()) {
			BlockPos checkPos = pos.add(8, 0, 8);
			int h = world.getHeight(checkPos.getX(), checkPos.getZ());
			Biome biome = world.getBiome(checkPos);
			if ((biome == MistBiomes.upDenseForest && h > 135) ||
				(biome == MistBiomes.upHillyTaiga && h > 132) ||
				(biome == MistBiomes.upJungleHills && h > 135) ||
				(biome == MistBiomes.upDunes && h > 130)) {
				return lapisGen.generate(world, rand, pos);
			}
		}
		return false;
	}
}