package ru.liahim.mist.world.generators;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.world.MistWorld;

public class DesertCottonGenerator extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		IBlockState state;
		for (state = world.getBlockState(pos); (state.getBlock().isAir(state, world, pos) ||
				state.getBlock().isLeaves(state, world, pos)) && pos.getY() > MistWorld.getFogMaxHight() + 1; state = world.getBlockState(pos)) {
			pos = pos.down();
		}
		long tick = MistTime.getTickOfMonth(world);
		for (int i = 0; i < 64; ++i) {
			pos = pos.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));
			if (world.isAirBlock(pos) && MistBlocks.DESERT_COTTON.canBlockStay(world, pos, MistBlocks.DESERT_COTTON.getDefaultState())) {
				state = MistBlocks.DESERT_COTTON.getSeasonState(world, pos, MistBlocks.DESERT_COTTON.getDefaultState(), tick);
				world.setBlockState(pos, state != null ? state : MistBlocks.DESERT_COTTON.getDefaultState(), 2);
			}
		}
		return true;
	}
}