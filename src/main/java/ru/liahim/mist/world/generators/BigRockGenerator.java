package ru.liahim.mist.world.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.common.Mist;
import net.minecraft.block.BlockBush;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BigRockGenerator extends WorldGenerator {

	private final int size;

	/**Size: 0 - min rock, 1 - med rock, 2 - big rock */
	public BigRockGenerator(int size) {
		super();
		this.size = MathHelper.clamp(size, 0, 2);
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		if (!world.isRemote) {
			boolean snow = world.getBiome(pos).isSnowyBiome();
			if (this.size == 2) return bigRockGen(world, rand, pos, snow);
			else if (this.size == 1) return medRockGen(world, rand, pos, snow);
			else return minRockGen(world, rand, pos, snow);
		}
		return false;
	}

	public boolean bigRockGen(World world, Random rand, BlockPos pos, boolean snow) {
		if (world.provider.getDimension() == Mist.getID() && pos.getY() < 135 && pos.getY() > 115 && rand.nextInt(4) == 0) {
			pos = pos.down(rand.nextInt(3));
			if (world.getBlockState(pos).getBlock() instanceof MistSoil) {
				int radius = rand.nextInt(2) + 2;
				List<BlockPos> poses = new ArrayList<BlockPos>();
				poses = checkBigArea(world, pos, radius, poses);
				if (poses != null) {
					int dx = rand.nextInt(radius * 2 - 1) - radius + 1;
					int dz = rand.nextInt(radius * 2 - 1) - radius + 1;
					if (dx != 0 || dz != 0) {
						int oldRadius = radius;
						radius = Math.min(radius, rand.nextInt(2) + 2);
						if (radius == 3) {
							if (dx * dz == 4) {
								dx /= 2;
								dz /= 2;
							}
						} else if (oldRadius == 3) {
							if (dx * dx < 4 && dz * dz < 4) {
								dx = dx > 0 ? 2 : -2;
								dz = dz > 0 ? 2 : -2;
							}
						}
						pos = pos.add(dx, radius == oldRadius ? - 1 : rand.nextInt(2) - 1, dz);
						poses = checkBigArea(world, pos, radius, poses);
						if (poses != null) {
							for (BlockPos checkPos : poses) {
								if (world.getBlockState(checkPos.up()).getBlock() instanceof BlockBush) {
									world.setBlockToAir(checkPos.up());
								}
								world.setBlockState(checkPos, MistBlocks.STONE.getDefaultState(), Mist.FLAG);
								if (snow && world.canSeeSky(checkPos.up())) {
									world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState(), Mist.FLAG);
								}
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean medRockGen(World world, Random rand, BlockPos pos, boolean snow) {
		if (world.provider.getDimension() == Mist.getID() && pos.getY() < 135 && pos.getY() > 115 && rand.nextInt(4) == 0) {
			pos = pos.down(rand.nextInt(2 + 1));
			if (world.getBlockState(pos).getBlock() instanceof MistSoil) {
				List<BlockPos> poses = new ArrayList<BlockPos>();
				poses = checkMedArea(world, pos, poses);
				if (poses != null) {
					int dx = rand.nextInt(3) - 1;
					int dz = rand.nextInt(3) - 1;
					if (dx != 0 || dz != 0) {
						pos = pos.add(dx, - 1, dz);
						poses = checkMedArea(world, pos, poses);
						if (poses != null) {
							for (BlockPos checkPos : poses) {
								if (world.getBlockState(checkPos.up()).getBlock() instanceof BlockBush) {
									world.setBlockToAir(checkPos.up());
								}
								world.setBlockState(checkPos, MistBlocks.STONE.getDefaultState(), Mist.FLAG);
								if (snow && world.canSeeSky(checkPos.up())) {
									world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState(), Mist.FLAG);
								}
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public boolean minRockGen(World world, Random rand, BlockPos pos, boolean snow) {
		if (world.provider.getDimension() == Mist.getID() && pos.getY() < 135 && pos.getY() > 115 && rand.nextInt(4) == 0) {
			pos = pos.down(rand.nextInt(rand.nextInt(2) + 1));
			if (world.getBlockState(pos).getBlock() instanceof MistSoil) {
				List<BlockPos> poses = new ArrayList<BlockPos>();
				poses = checkMinArea(world, pos, poses, rand);
				if (poses != null) {
					for (BlockPos checkPos : poses) {
						if (world.getBlockState(checkPos.up()).getBlock() instanceof BlockBush) {
							world.setBlockToAir(checkPos.up());
						}
						world.setBlockState(checkPos, MistBlocks.STONE.getDefaultState(), Mist.FLAG);
						if (snow && world.canSeeSky(checkPos.up())) {
							world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState(), Mist.FLAG);
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	
	private List<BlockPos> checkBigArea(World world, BlockPos pos, int radius, List<BlockPos> poses) {
		BlockPos checkPos;
		for (int x = -radius; x <= radius; ++x) {
			for (int y = -radius; y <= radius; ++y) {
				for (int z = -radius; z <= radius; ++z) {
					checkPos = pos.add(x, y, z);
					if (checkPos.distanceSq(pos) <= radius * (radius + 1)) {
						if (world.getBlockState(checkPos).getBlock() instanceof MistTreeTrunk) return null;
						else if (!poses.contains(checkPos)) poses.add(checkPos);
					}
				}
			}
		}
		return poses;
	}

	private List<BlockPos> checkMedArea(World world, BlockPos pos, List<BlockPos> poses) {
		BlockPos checkPos;
		for (int x = -1; x <= 2; ++x) {
			for (int y = -1; y <= 2; ++y) {
				for (int z = -1; z <= 2; ++z) {
					if ((x - 0.5D) * (x - 0.5D) + (y - 0.5D) * (y - 0.5D) + (z - 0.5D) * (z - 0.5D) < 4) {
						checkPos = pos.add(x, y, z);
						if (world.getBlockState(checkPos).getBlock() instanceof MistTreeTrunk) return null;
						else if (!poses.contains(checkPos)) poses.add(checkPos);
					}
				}
			}
		}
		return poses;
	}

	private List<BlockPos> checkMinArea(World world, BlockPos pos, List<BlockPos> poses, Random rand) {
		BlockPos checkPos;
		int i = rand.nextInt(4);
		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					if ((x << 1 | z) != (y == 0 ? i : 3 - i)) {
						checkPos = pos.add(x, y, z);
						if (world.getBlockState(checkPos).getBlock() instanceof MistTreeTrunk) return null;
						else if (!poses.contains(checkPos)) poses.add(checkPos);
					}
				}
			}
		}
		return poses;
	}
}