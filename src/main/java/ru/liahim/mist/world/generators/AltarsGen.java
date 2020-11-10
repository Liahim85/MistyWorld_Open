package ru.liahim.mist.world.generators;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLootType;
import ru.liahim.mist.util.GenUtil;
import ru.liahim.mist.util.GenUtil.GenSet;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.biome.BiomeMistBorder;
import ru.liahim.mist.world.biome.BiomeMistUpDesert;
import ru.liahim.mist.world.biome.BiomeMistUpMarsh;

public class AltarsGen extends TombGenBase {

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		if (rand.nextFloat() >= ModConfig.generation.altarGenerationChance) return false;
		BlockPos center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
		if (world.getBlockState(center.down()).getMaterial().isReplaceable()) center = center.down();
		if (center.getY() <= MistWorld.seaLevelUp) return false;
		Biome biome = world.getBiome(center);
		if (!(biome instanceof BiomeMist) || !((BiomeMist)biome).isUpBiome() || biome instanceof BiomeMistBorder || biome instanceof BiomeMistUpMarsh || biome instanceof BiomeMistUpDesert ||
				!(world.getBlockState(center.down()).getBlock() instanceof MistSoil)) return false;
		EnumBiomeType type = ((BiomeMist)biome).getBiomeType();
		BlockPos checkPos;
		if (type == EnumBiomeType.Forest) {
			Type bType = Type.BRICK;
			if (roomCheck(world, center, -3, 3, -1, -1, -3, 3, true)) {
				for (int x = -2; x <= 2; ++x) {
					for (int z = -2; z <= 2; ++z) {
						checkPos = center.add(x, -1, z);
						if (x == -2 || x == 2 || z == -2 || z == 2) {
							if (Math.abs(x * z) == 4 || rand.nextInt(32) != 0) world.setBlockState(checkPos, getBlock(Type.COBBLE, rand, 1), Mist.FLAG);
						} else if (x == 0 && z == 0) world.setBlockState(checkPos, getSlab(bType, false, rand, 1), Mist.FLAG);
						else {
							if (rand.nextInt(32) == 0) world.setBlockState(checkPos, getSlab(bType, false, rand, 1), Mist.FLAG);
							else if (x == -1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.WEST, false, rand, 1), Mist.FLAG);
							else if (x == 1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.EAST, false, rand, 1), Mist.FLAG);
							else if (z == -1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.NORTH, false, rand, 1), Mist.FLAG);
							else world.setBlockState(checkPos, getStairs(bType, EnumFacing.SOUTH, false, rand, 1), Mist.FLAG);
						}
					}
				}
				world.setBlockState(center.add(-2, 0, -2), getSlab(bType, false, rand, 1), Mist.FLAG);
				world.setBlockState(center.add(-2, 0, 2), getSlab(bType, false, rand, 1), Mist.FLAG);
				world.setBlockState(center.add(2, 0, -2), getSlab(bType, false, rand, 1), Mist.FLAG);
				world.setBlockState(center.add(2, 0, 2), getSlab(bType, false, rand, 1), Mist.FLAG);
				BiomeMist.looseRockGen.generate(world, rand, center);
				return true;
			}
		} else if (type == EnumBiomeType.Swamp) {
			Type bType = Type.COBBLE;
			for (int i = 0; i < 5; ++i) {
				if (roomCheck(world, center, -6, 6, -1, -1, -6, 6, true)) {
					int wet = 1;
					int innerHight = 4;
					int outerHight = 2;
					generateColumn(world, center.add(-1, -1, -3), rand, innerHight, wet, bType);
					generateColumn(world, center.add(1, -1, -3),  rand, innerHight, wet, bType);
					generateColumn(world, center.add(-3, -1, -1), rand, innerHight, wet, bType);
					generateColumn(world, center.add(-3, -1, 1),  rand, innerHight, wet, bType);
					generateColumn(world, center.add(-1, -1, 3),  rand, innerHight, wet, bType);
					generateColumn(world, center.add(1, -1, 3),   rand, innerHight, wet, bType);
					generateColumn(world, center.add(3, -1, -1),  rand, innerHight, wet, bType);
					generateColumn(world, center.add(3, -1, 1),   rand, innerHight, wet, bType);
	
					generateColumn(world, center.add(0, -1, 6),   rand, outerHight, wet, bType);
					generateColumn(world, center.add(3, -1, 5),   rand, outerHight, wet, bType);
					generateColumn(world, center.add(5, -1, 3),   rand, outerHight, wet, bType);
					generateColumn(world, center.add(6, -1, 0),   rand, outerHight, wet, bType);
					generateColumn(world, center.add(5, -1, -3),  rand, outerHight, wet, bType);
					generateColumn(world, center.add(3, -1, -5),  rand, outerHight, wet, bType);
					generateColumn(world, center.add(0, -1, -6),  rand, outerHight, wet, bType);
					generateColumn(world, center.add(-3, -1, -5), rand, outerHight, wet, bType);
					generateColumn(world, center.add(-5, -1, -3), rand, outerHight, wet, bType);
					generateColumn(world, center.add(-6, -1, 0),  rand, outerHight, wet, bType);
					generateColumn(world, center.add(-5, -1, 3),  rand, outerHight, wet, bType);
					generateColumn(world, center.add(-3, -1, 5),  rand, outerHight, wet, bType);
					
					BiomeMist.looseRockGen.generate(world, rand, center);
					return true;
				} else center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
			}
		} else if (type == EnumBiomeType.Cold) {
			if (biome != MistBiomes.upSnowfields || rand.nextBoolean()) {
				Type bType = Type.COBBLE;
				for (int i = 0; i < 1; ++i) {
					if (roomCheck(world, center, -5, 5, -1, -1, -5, 5, true)) {
						GenUtil gen;
						int wet = 1;
						for (EnumFacing face : EnumFacing.HORIZONTALS) {
							gen = new GenUtil(world, new GenSet(center, face, Mirror.NONE));
							if (rand.nextInt(8) == 0) {
								generateColumn(world, gen.getPos(center.add(-1, -1, 5)), rand, rand.nextInt(3) + 1, wet, bType);
								generateColumn(world, gen.getPos(center.add(1, -1, 5)), rand, rand.nextInt(3) + 1, wet, bType);
							} else {
								for (int j = -1; j < 3; ++j) {
									gen.setBlockState(center.add(-1, j, 5), getBlock(bType, rand, wet));
									gen.setBlockState(center.add(1, j, 5), getBlock(bType, rand, wet));
								}
								gen.setBlockState(center.add(-1, 3, 5), getStairs(bType, EnumFacing.EAST, false, rand, wet));
								gen.setBlockState(center.add(1, 3, 5), getStairs(bType, EnumFacing.WEST, false, rand, wet));
								gen.setBlockState(center.add(0, 3, 5), getBlock(bType, rand, wet));
								if (world.canSeeSky(gen.getPos(center.add(0, 4, 5)))) gen.setBlockState(center.add(0, 4, 5), Blocks.SNOW_LAYER.getDefaultState());
								if (gen.getBlockState(center.add(0, 0, 5)).getBlock() == Blocks.SNOW_LAYER) gen.setBlockState(center.add(0, 0, 5), Blocks.AIR.getDefaultState());
							}
							BiomeMist.looseRockGen.generate(world, rand, center.up(3));
						}
						return true;
					} else center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
				}
			}
		} else if (type == EnumBiomeType.Desert) {
			if (roomCheck(world, center, -1, 1, -1, -1, -1, 1, true)) {
				Type bType = Type.COBBLE;
				int wet = -1;
				int h = rand.nextInt(4) + 1;
				for (int x = -1; x <= 1; ++x) {
					for (int z = -1; z <= 1; ++z) {
						for (int y = -1; y < h; ++y) {
							checkPos = center.add(x, y, z);
							if (x != 0 && z != 0) {
								if (y == -1) world.setBlockState(checkPos, getBlock(bType, rand, wet));
								else if (x == -1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.EAST, false, rand, wet));
								else world.setBlockState(checkPos, getStairs(bType, EnumFacing.WEST, false, rand, wet));
							} else if (x != 0 || z != 0) {
								if (y == -1) {
									if (x == -1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.WEST, false, rand, wet));
									else if (x == 1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.EAST, false, rand, wet));
									else if (z == -1) world.setBlockState(checkPos, getStairs(bType, EnumFacing.NORTH, false, rand, wet));
									else world.setBlockState(checkPos, getStairs(bType, EnumFacing.SOUTH, false, rand, wet));
								} else {
									if (x == -1) world.setBlockState(checkPos, getStep(bType, EnumFacing.EAST, true, rand, wet));
									else if (x == 1) world.setBlockState(checkPos, getStep(bType, EnumFacing.WEST, true, rand, wet));
									else if (z == -1) world.setBlockState(checkPos, getStep(bType, EnumFacing.SOUTH, true, rand, wet));
									else world.setBlockState(checkPos, getStep(bType, EnumFacing.NORTH, true, rand, wet));
								}
							} else world.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						}
					}
				}
				world.setBlockState(center.up(h), getSlab(bType, false, rand, wet));
				world.setBlockState(center.down(2), getBlock(bType, rand, wet));
				if (rand.nextBoolean()) {
					world.setBlockState(center.down(), MistBlocks.URN.getDefaultState());
					UrnLootType.initializeType(world.getTileEntity(center.down()), EnumBiomeType.Desert, UrnLocation.ALTARS, rand);
				}
				BiomeMist.looseRockGen.generate(world, rand, center.up(3));
				// Tables
				/*
				checkPos = center;
				for (int i = 0; i < 3; ++i) {
					center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
					if (center.distanceSq(checkPos) >= 16 && roomCheck(world, center, -1, 2, -1, -1, -1, 1, true)) {
						for (int x = -1; x <= 2; ++x) {
							for (int z = -1; z <= 1; ++z) {
								checkPos = center.add(x, 0, z);
								if (z == 0) {
									if (x == -1) world.setBlockState(checkPos, getStep(EnumFacing.EAST, false, rand, wet));
									else if (x == 0) world.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet));
									else if (x == 1) world.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet));
									else world.setBlockState(checkPos, getStep(EnumFacing.WEST, false, rand, wet));
								} else {
									if (x == 0) world.setBlockState(checkPos, getStep(EnumFacing.WEST, false, rand, wet));
									else if (x == 1) world.setBlockState(checkPos, getStep(EnumFacing.EAST, false, rand, wet));
									else if (z == -1) world.setBlockState(checkPos, getStep(EnumFacing.SOUTH, false, rand, wet));
									else world.setBlockState(checkPos, getStep(EnumFacing.NORTH, false, rand, wet));	
								}
							}
						}
						break;
					}
				}*/
				return true;
			}
		} else if (type == EnumBiomeType.Jungle) {
			if (roomCheck(world, center, -2, 2, -1, -1, -2, 2, true) && world.canSeeSky(center.south()) && world.canSeeSky(center.north())) {
				generatePillar(world, center.north(), EnumFacing.NORTH, rand, 6, 0, Type.COBBLE);
				generatePillar(world, center.south(), EnumFacing.SOUTH, rand, 8, 0, Type.COBBLE);
				BiomeMist.looseRockGen.generate(world, rand, center.up(3));
				return true;
			}
		}
		return false;
	}

	public static int generateColumn(World world, BlockPos pos, Random rand, int hight, int wet, Type... type) {
		hight = hight - rand.nextInt(rand.nextInt(rand.nextInt(hight) + 1) + 1);
		for (int i = 0; i < hight; ++i) {
			world.setBlockState(pos.up(i), getBlock(type[rand.nextInt(type.length)], rand, wet), Mist.FLAG);
		}
		int j = rand.nextInt(5);
		if (j == 0) world.setBlockState(pos.up(hight), getSlab(type[rand.nextInt(type.length)], false, rand, wet), Mist.FLAG);
		else if (j == 1) world.setBlockState(pos.up(hight), getStep(type[rand.nextInt(type.length)], EnumFacing.getHorizontal(rand.nextInt(4)), false, rand, wet), Mist.FLAG);
		else if (j == 2) world.setBlockState(pos.up(hight), getStairs(type[rand.nextInt(type.length)], EnumFacing.getHorizontal(rand.nextInt(4)), false, rand, wet), Mist.FLAG);
		else {
			world.setBlockState(pos.up(hight), getBlock(type[rand.nextInt(type.length)], rand, wet), Mist.FLAG);
			pos = pos.up(hight + 1);
			if (world.getBiome(pos).isSnowyBiome() && world.canSeeSky(pos)) world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState(), Mist.FLAG);
		}
		return hight;
	}

	private void generatePillar(World world, BlockPos pos, EnumFacing side, Random rand, int hight, int wet, Type type) {
		if (rand.nextInt(6) > 0) {
			for (int i = -1; i <= hight; ++i) {
				world.setBlockState(pos.up(i), getBlock(i < hight && rand.nextInt(4) == 0 ? Type.BRICK : Type.COBBLE, rand, i < 1 ? 1 : wet));
			}
			world.setBlockState(pos.up(hight + 1), getStep(type, side.getOpposite(), false, rand, wet));
			world.setBlockState(pos.offset(side.getOpposite()).up(hight + 1), getStep(type, side, false, rand, wet));
			world.setBlockState(pos.offset(side.getOpposite()).up(hight), getStep(type, side, true, rand, wet));
		} else {
			int h = hight + 2 - generateColumn(world, pos.down(), rand, hight - 3, wet, Type.COBBLE, Type.COBBLE, Type.COBBLE, Type.BRICK);
			for (EnumFacing face : EnumFacing.HORIZONTALS) {
				if (face != side.getOpposite() && rand.nextBoolean()) {
					for (int i = 2; i <= h; ++i) {
						if (i > 2 && i < h && world.isSideSolid(pos.offset(face, i).down(), EnumFacing.UP)) {
							world.setBlockState(pos.offset(face, i), getBlock(rand.nextInt(4) == 0 ? Type.BRICK : Type.COBBLE, rand, wet));
						} else {
							int f = rand.nextInt(6);
							if (i == 2) {
								if (f < 5) world.setBlockState(pos.offset(face, i), getStairs(rand.nextInt(4) == 0 ? Type.BRICK : Type.COBBLE, face, f < 3, rand, wet));
								else world.setBlockState(pos.offset(face, i), getBlock(rand.nextInt(4) == 0 ? Type.BRICK : Type.COBBLE, rand, wet));
							} else if (i == h) {
								if (f < 5) world.setBlockState(pos.offset(face, i), getStairs(rand.nextInt(4) == 0 ? Type.BRICK : Type.COBBLE, face.getOpposite(), f < 2, rand, wet));
								else world.setBlockState(pos.offset(face, i), getBlock(rand.nextInt(4) == 0 ? Type.BRICK : Type.COBBLE, rand, wet));
							}
						}
					}
					break;
				}
			}
		}
	}
}