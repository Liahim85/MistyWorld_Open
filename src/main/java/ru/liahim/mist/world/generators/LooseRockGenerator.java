package ru.liahim.mist.world.generators;

import java.util.Random;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistLooseRock;
import ru.liahim.mist.common.Mist;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class LooseRockGenerator extends WorldGenerator {

	final IBlockState rock = MistBlocks.LOOSE_ROCK.getDefaultState();
	final IBlockState flint = MistBlocks.LOOSE_ROCK.getDefaultState().withProperty(MistLooseRock.TYPE, 1);
	final Block mask = MistBlocks.SAND;
	int radius = 8;

	public LooseRockGenerator() {
		super();
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		for (IBlockState state = world.getBlockState(pos); (state.getBlock().isAir(state, world, pos) || state.getBlock().isLeaves(state, world, pos))
				&& pos.getY() > 0; state = world.getBlockState(pos)) {
			pos = pos.down();
		}
		for (int i = 0; i < radius * 2; ++i) {
			BlockPos checkpos = pos.add(rand.nextInt(radius) - rand.nextInt(radius), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(radius) - rand.nextInt(radius));
			if (world.isAirBlock(checkpos) && world.isSideSolid(checkpos.down(), EnumFacing.UP) && (this.mask == null || world.getBlockState(checkpos.down()).getBlock() != this.mask)) {
				if (rand.nextInt(24) == 0) world.setBlockState(checkpos, this.flint, Mist.FLAG);
				else world.setBlockState(checkpos, this.rock, Mist.FLAG);
			}
		}
		return true;
	}
}