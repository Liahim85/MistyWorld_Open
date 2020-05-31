package ru.liahim.mist.world.generators;

import java.util.Random;

import com.google.common.base.Predicate;

import ru.liahim.mist.common.Mist;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class SingleBlockGenerator extends WorldGenerator {

	private final IBlockState state;
	private final Predicate<IBlockState> predicate;

	public SingleBlockGenerator(IBlockState state, Predicate<IBlockState> predicate) {
		this.state = state;
		this.predicate = predicate;
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		if (world.provider.getDimension() == Mist.getID()) {
			pos = pos.add(8, 0, 8);
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().isReplaceableOreGen(state, world, pos, this.predicate)) {
				world.setBlockState(pos, this.state, Mist.FLAG);
				return true;
			}
		}
		return false;
	}
}