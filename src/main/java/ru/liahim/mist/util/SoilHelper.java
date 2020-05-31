package ru.liahim.mist.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.IMistSoil;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistGrass;
import ru.liahim.mist.block.MistHumus_Dirt;

/**@author Liahim*/
public class SoilHelper {

	public static int getHumus(IBlockState state) {
		if (state.getBlock() instanceof MistHumus_Dirt) return 4;
		else if (state.getBlock() instanceof MistDirt) return state.getValue(MistDirt.HUMUS);
		return 0;
	}

	public static boolean setSoil(World world, BlockPos pos, IBlockState state, int hum, boolean wet, int flag) {
		Block block = state.getBlock();
		if (block instanceof IMistSoil) {
			if (block instanceof IWettable) {
				if (hum > 3) {
					if (block == MistBlocks.HUMUS_DIRT || block == MistBlocks.DIRT_F) {
						return world.setBlockState(pos, MistBlocks.HUMUS_DIRT.getDefaultState().withProperty(IWettable.WET, wet), flag);
					} else if (block == MistBlocks.HUMUS_GRASS || block == MistBlocks.GRASS_F) {
						return world.setBlockState(pos, MistBlocks.HUMUS_GRASS.getDefaultState().withProperty(IWettable.WET, wet), flag);
					}
				} else if (hum >= 0) {
					if (block == MistBlocks.DIRT_F || block == MistBlocks.HUMUS_DIRT) {
						return world.setBlockState(pos, MistBlocks.DIRT_F.getDefaultState().
								withProperty(MistDirt.HUMUS, hum).withProperty(IWettable.WET, wet), flag);
					} else if (block == MistBlocks.GRASS_F) {
						return world.setBlockState(pos, state.withProperty(MistDirt.HUMUS, hum).withProperty(IWettable.WET, wet), flag);
					} else if (block == MistBlocks.HUMUS_GRASS) {
						return world.setBlockState(pos, MistBlocks.GRASS_F.getDefaultState().
								withProperty(MistDirt.HUMUS, hum).withProperty(IWettable.WET, wet).withProperty(MistGrass.GROWTH, false), flag);
					} else if (block instanceof MistDirt) {
						return world.setBlockState(pos, state.withProperty(MistDirt.HUMUS, hum).withProperty(IWettable.WET, wet), flag);
					} else return world.setBlockState(pos, state.withProperty(IWettable.WET, wet), flag);
				}
			} else return world.setBlockState(pos, state, flag);
		} else if (block instanceof IWettable) {
			return world.setBlockState(pos, state.withProperty(IWettable.WET, wet), flag);
		}
		return false;
	}

	public static boolean setSoil(World world, BlockPos pos, IBlockState state, int hum, int flag) {
		if (state.getBlock() instanceof IMistSoil) {
			if (state.getBlock() instanceof IWettable) {
				return setSoil(world, pos, state, hum, state.getValue(IWettable.WET), flag);
			}
			return setSoil(world, pos, state, hum, false, flag);
		}
		return false;
	}

	public static boolean setDirt(World world, BlockPos pos, IBlockState state, int hum, boolean wet, int flag) {
		return setSoil(world, pos, getSoilState(state), hum, wet, flag);
	}

	public static boolean setDirt(World world, BlockPos pos, IBlockState state, boolean wet, int flag) {
		if (state.getBlock() instanceof IMistSoil) {
			if (state.getBlock() instanceof MistDirt)
				return setDirt(world, pos, state, state.getValue(MistDirt.HUMUS), wet, flag);
			else if (state.getBlock() instanceof MistHumus_Dirt)
				return setDirt(world, pos, state, 4, wet, flag);
			return setDirt(world, pos, state, 0, wet, flag);
		}
		return false;
	}

	public static boolean setDirt(World world, BlockPos pos, IBlockState state, int flag) {
		if (state.getBlock() instanceof IMistSoil) {
			if (state.getBlock() instanceof IWettable)
				return setDirt(world, pos, state, state.getValue(IWettable.WET), flag);
			return setDirt(world, pos, state, false, flag);
		}
		return false;
	}

	public static boolean setGrass(World world, BlockPos pos, IBlockState state, int hum, boolean wet, boolean isGrowth, int flag) {
		return setSoil(world, pos, getGrassState(state, isGrowth), hum, wet, flag);
	}

	public static boolean setGrass(World world, BlockPos pos, IBlockState state, boolean wet, boolean isGrowth, int flag) {
		if (state.getBlock() instanceof IMistSoil) {
			if (state.getBlock() instanceof MistDirt)
				return setGrass(world, pos, state, state.getValue(MistDirt.HUMUS), wet, isGrowth, flag);
			else if (state.getBlock() instanceof MistHumus_Dirt)
				return setGrass(world, pos, state, 4, wet, isGrowth, flag);
			return setGrass(world, pos, state, 0, wet, isGrowth, flag);
		}
		return false;
	}

	/**Default set full grow.*/
	public static boolean setGrass(World world, BlockPos pos, IBlockState state, boolean wet, int flag) {
		if (state.getBlock() instanceof IMistSoil) return setGrass(world, pos, state, wet, true, flag);
		return false;
	}

	/**Default set full grow.*/
	public static boolean setGrass(World world, BlockPos pos, IBlockState state, int flag) {
		if (state.getBlock() instanceof IMistSoil) {
			if (state.getBlock() instanceof IWettable)
				return setGrass(world, pos, state, state.getValue(IWettable.WET), true, flag);
			return setGrass(world, pos, state, false, true, flag);
		}
		return false;
	}

	public static IBlockState getSoilState(IBlockState state) {
		Block block = state.getBlock();
		if (block instanceof IMistSoil) {
			if (block instanceof MistDirt)
				return ((IMistSoil)block).getSoilBlock().getDefaultState().withProperty(MistDirt.HUMUS,
						state.getValue(MistDirt.HUMUS)).withProperty(IWettable.WET, state.getValue(IWettable.WET));
			else if (block instanceof IWettable) return ((IMistSoil)block).getSoilBlock().getDefaultState().
					withProperty(IWettable.WET, state.getValue(IWettable.WET));
			return ((IMistSoil)block).getSoilBlock().getDefaultState();
		}
		return state;
	}

	public static IBlockState getGrassState(IBlockState state, boolean isGrowth) {
		Block block = state.getBlock();
		if (block instanceof IMistSoil) {
			if (block instanceof MistDirt)
				return ((IMistSoil)block).getGrassBlock().getDefaultState().withProperty(MistDirt.HUMUS,
						state.getValue(MistDirt.HUMUS)).withProperty(IWettable.WET, state.getValue(IWettable.WET)).
						withProperty(MistGrass.GROWTH, isGrowth);
			else if (block instanceof IWettable) return ((IMistSoil)block).getGrassBlock().getDefaultState().
					withProperty(IWettable.WET, state.getValue(IWettable.WET));
			return ((IMistSoil)block).getGrassBlock().getDefaultState();
		}
		return state;
	}

	/**Default full grow stage.*/
	public static IBlockState getGrassState(IBlockState state) {
		return getGrassState(state, true);
	}
}