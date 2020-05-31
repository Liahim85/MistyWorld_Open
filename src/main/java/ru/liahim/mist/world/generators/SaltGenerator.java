package ru.liahim.mist.world.generators;

import java.util.Random;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSaltpeterOre;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMistBorder;
import net.minecraft.block.state.pattern.BlockMatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenerator;

public class SaltGenerator extends WorldGenerator {
	
	private final WorldGenerator saltGen = new WorldGenMinable(MistBlocks.SALTPETER_ORE.getDefaultState().withProperty(MistSaltpeterOre.TYPE, MistSaltpeterOre.SaltType.SALT), 4, BlockMatcher.forBlock(MistBlocks.STONE));
	private int height = MistWorld.fogMaxHight_S + 4;
	private int spread = 50;

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		if (world.provider.getDimension() == Mist.getID()) {
			int c = world.getBiome(pos.add(8, 0, 8)) instanceof BiomeMistBorder ? 2 : 4;
			BlockPos checkPos;
			for (int i = 0; i < c; ++i) {
				checkPos = pos.add(rand.nextInt(16), rand.nextInt(spread) + rand.nextInt(spread) + height - spread, rand.nextInt(16));
				saltGen.generate(world, rand, checkPos);
			}
			return true;
		}
		return false;
	}
}