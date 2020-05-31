package ru.liahim.mist.world.generators;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.gizmos.Remains;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.tileentity.TileEntityRemains;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.util.GenUtil;
import ru.liahim.mist.util.GenUtil.GenSet;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.biome.BiomeMistBorder;
import ru.liahim.mist.world.biome.BiomeMistUpDesert;
import ru.liahim.mist.world.biome.BiomeMistUpMarsh;
import ru.liahim.mist.world.biome.BiomeMistUpSavanna;

public class TombGen extends TombGenBase {

	/** Deserm moss */
	static int d = -1;
	private static final UrnLocation loc = UrnLocation.TOMBS;
	private static float genChance;
	static {updateChance();}

	public static void updateChance() {
		genChance = (float) Math.max(ModConfig.generation.forestTombGenerationChance,
							Math.max(ModConfig.generation.swampTombGenerationChance,
							Math.max(ModConfig.generation.desertTombGenerationChance,
							Math.max(ModConfig.generation.snowTombGenerationChance, ModConfig.generation.jungleTombGenerationChance))));
	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		float chance = rand.nextFloat();
		if (chance >= genChance) return false;
		int rx = rand.nextInt(16) + 8;
		int rz = rand.nextInt(16) + 8;
		BlockPos center = world.getHeight(pos.add(rx, 0, rz));
		if (world.getBlockState(center.down()).getMaterial().isReplaceable()) center = center.down();
		if (center.getY() <= MistWorld.seaLevelUp) return false;
		Biome biome = world.getBiome(center);
		if (!(biome instanceof BiomeMist) || !((BiomeMist)biome).isUpBiome() || biome instanceof BiomeMistBorder || biome instanceof BiomeMistUpMarsh || biome instanceof BiomeMistUpDesert ||
				!(world.getBlockState(center.down()).getBlock() instanceof MistSoil)) return false;
		EnumBiomeType type = ((BiomeMist)biome).getBiomeType();
		if (type == EnumBiomeType.Forest) {
			if (chance < ModConfig.generation.forestTombGenerationChance && (biome != MistBiomes.upMeadow || rand.nextBoolean())) return ForestTomb.generate(world, center, (BiomeMist)biome, rand);
		} else if (type == EnumBiomeType.Swamp) {
			if (chance < ModConfig.generation.swampTombGenerationChance) return SwampTomb.generate(world, center, (BiomeMist)biome, rand);
		} else if (type == EnumBiomeType.Cold) {
			if (chance < ModConfig.generation.snowTombGenerationChance && (biome != MistBiomes.upSnowfields || rand.nextBoolean())) return SnowTomb.generate(world, center, (BiomeMist)biome, rand);
		} else if (type == EnumBiomeType.Desert) {
			if (chance < ModConfig.generation.desertTombGenerationChance) return SavannaTomb.generate(world, center, (BiomeMist)biome, rand);
		} else if (type == EnumBiomeType.Jungle) {
			if (chance < ModConfig.generation.jungleTombGenerationChance) return TropicTomb.generate(world, pos, rand);
		}
		return false;
	}

	private static void generateEntrance(GenUtil gen, BiomeMist biome, Random rand, int offset, boolean wet) {
		BlockPos center = gen.set.center;
		BlockPos checkPos;
		boolean cold = biome.isSnowyBiome();
		for (int x = -1; x <= 1; ++x) {
			for (int z = -5 - offset; z <= 1; ++z) {
				for (int y = -1; y >= -6 - offset; --y) {
					checkPos = center.add(x, y, z);
					if (y == -1) {
						if (z < 1 - offset) {
							if (cold && (x != 0 || z == -offset) && gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
							else gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState());
							if (x == -1) {
								if (cold) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : -1));
								else if (rand.nextInt(8) == 0) gen.setBlockState(checkPos, getSlab(false, rand, wet ? 1 : -1));
								else gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand,  wet ? 1 : -1));
							} else if (x == 1) {
								if (cold) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : -1));
								else if (rand.nextInt(8) == 0) gen.setBlockState(checkPos, getSlab(false, rand,  wet ? 1 : -1));
								else gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand,  wet ? 1 : -1));
							} else {
								if (z == -offset) {
									if (cold) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : -1));
									else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand,  wet ? 1 : -1));
								} else if (z == -5 - offset && gen.world.isBlockNormalCube(gen.getPos(checkPos.add(0, 0, -1)), false)) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand,  wet ? 1 : -1));
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							}
						}
					} else if (y <= -1 - z - offset && y >= -7 - z - offset) {
						if (x == 0 && y < -1 - z - offset && y > -7 - z - offset) {
							if (y == -2 - z - offset) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
							else if (y > -6 - offset) {
								if (y > -6 - z - offset) {
									if (y == -2) {
										gen.setBlockState(checkPos, biome.topBlock); //grass
										if (cold && gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
									} else if (y == -3) gen.setBlockState(checkPos, biome.secondTopBlock); //dirt
									else gen.setBlockState(checkPos, biome.fillerBlock); //dirt
								} else gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, wet ? 0 : y == -2 ? -1 : d));
							} else gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						} else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : (y == -2 && z == -4 - offset) ? -1 : d));
					}
				}
			}
		}
		BiomeMist.looseRockGen.generate(gen.world, rand, center);
	}

	public static class TropicTomb {

		private static final EnumBiomeType biomeType = EnumBiomeType.Jungle;

		public static boolean generate(World world, BlockPos pos, Random rand) {
			for (int i = 0; i < 5; ++i) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos center = world.getHeight(pos.add(rx, 0, rz));
				if (world.getBlockState(center.down()).getMaterial().isReplaceable()) center = center.down();
				if (center.getY() > MistWorld.seaLevelUp) {
					Biome biome = world.getBiome(center);
					if (biome instanceof BiomeMist && ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Jungle && world.getBlockState(center.down()).getBlock() instanceof MistSoil) {
						GenUtil gen = new GenUtil(world, new GenSet(center, Rotation.values()[rand.nextInt(4)], Mirror.values()[rand.nextInt(2)]));
						if (roomCheck(gen, -3, 3, -3, -1, -3, 3, true)) {
							generateTropicEntrance(gen, (BiomeMist)biome, rand, false);
							return true;
						}
					}
				}
			}
			return false;
		}

		private static void generateTropicEntrance(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			IBlockState top = null;
			IBlockState fill = null;
			boolean water = false;
			int state = rand.nextInt(6);
			if (state == 0) {
				top = redSandDry;
				fill = redSandWet;
			} else if (state == 1) {
				top = biome.topBlock;
				fill = biome.fillerBlock;
			} else if (state == 2) top = fill = redSandWet;
			else if (state == 3) top = fill = MistBlocks.GRAVEL.getDefaultState();
			else water = true;
			int i;
			int bottonDeep;
			int deep;
			for (int x = -3; x <= 3; ++x) {
				for (int z = -3; z <= 3; ++z) {
					gen.setBlockState(center.add(x, 0, z), Blocks.AIR.getDefaultState());
					i = -1;
					if (x <= -2) {
						if (z <= -2) i = 0;
						else if (z == 0) i = 1;
						else if (z >= 2) i = 2;
					} else if (x == 0) {
						if (z >= 2) i = 3;
						else if (z <= -2) i = 7;
					} else if (x >= 2) {
						if (z >= 2) i = 4;
						else if (z == 0) i = 5;
						else if (z <= -2) i = 6;
					}
					bottonDeep = MistWorld.fogMaxHight_S + 4 - center.getY() + (int)(biome.getGrassNoise().getValue((double)(center.getX() + x) / 20, (double)(center.getZ() + z) / 20) * 2 + (Math.abs(x * z)) * 0.2D);
					deep = MistWorld.fogMaxHight_S + 3 - center.getY() + rand.nextInt(rand.nextInt(3) + 1);
					if (state == 2) top = rand.nextBoolean() ? biome.topBlock.withProperty(MistDirt.HUMUS, rand.nextInt(3)) : redSandDry;
					else if (state == 3) top = rand.nextBoolean() ? biome.topBlock.withProperty(MistDirt.HUMUS, rand.nextInt(2) + 1) : MistBlocks.GRAVEL.getDefaultState();
					for (int y = -1; y > deep; --y) {
						checkPos = center.add(x, y, z);
						if (gen.world.isBlockFullCube(gen.getPos(checkPos))) {
							if (x == -3 || x == 3 || z == -3 || z == 3) {
								if (i == -1) gen.setBlockState(checkPos, getCobble(rand, 1));
								else {
									int w = wet || -y - 1 <= i ? 1 : (-y - 4) % 8 == i ? -1 : 0;
									if (y < -1 && (-y - 2) % 8 == i) gen.setBlockState(checkPos, MistBlocks.STONE.getDefaultState());
									else if (y > MistWorld.fogMaxHight_S + 5 - center.getY()) {
										if (y == -1 || (-y - 1) % 8 == i) {
											if (y > -6 || (x != 0 && z != 0)) {
												if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, w));
												else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, w));
												else if (z == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, w));
												else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, w));
											} else if (y > MistWorld.fogMaxHight_S + 4 - center.getY()) {
												gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
												Rotation rot = x == -3 ? Rotation.CLOCKWISE_90 : x == 3 ? Rotation.COUNTERCLOCKWISE_90 : z == -3 ? Rotation.CLOCKWISE_180 : Rotation.NONE;
												int j = rand.nextInt(8);
												if (y < -7 && j <= 4) generateRoom(gen.add(checkPos.down(), rot, Mirror.NONE), biome, rand, false);
												else if (j <= 6) generateMiniRoomPart(gen.add(checkPos.down(), rot, Mirror.NONE), biome, rand, false);
												else generateEnd(gen.add(checkPos.down(), rot, Mirror.NONE), biome, rand, false);
											}
										} else if (y < -2 && (-y - 3) % 8 == i) {
											if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, -1));
											else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, -1));
											else if (z == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, -1));
											else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, -1));
										} else if ((x == 0 || z == 0) && y > MistWorld.fogMaxHight_S + 6 - center.getY()) {
											if (y <= -5 && -y % 8 == i) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
											else if (y <= -4 && (-y + 1) % 8 == i) {
												if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, w));
												else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, w));
												else if (z == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, w));
												else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, w));
											} else gen.setBlockState(checkPos, getCobble(rand, w));
										} else gen.setBlockState(checkPos, getCobble(rand, w));
									} else gen.setBlockState(checkPos, getCobble(rand, w));
								}
							} else {
								if ((x == -2 || x == 2 || z == -2 || z == 2) && (i != -1 && (y < -1 && (-y - 2) % 8 == i))) gen.setBlockState(checkPos, MistBlocks.STONE.getDefaultState());
								else if (water) {
									if (y < MistWorld.fogMaxHight_S + 6 - center.getY()) gen.setBlockState(checkPos, Blocks.WATER.getDefaultState());
									else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
								} else if (y == bottonDeep + 1) gen.setBlockState(checkPos, top);
								else if (y < bottonDeep + 1) gen.setBlockState(checkPos, fill);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							}
						}
					}
				}
			}
			if (state > 0) {
				checkPos = center.down(MistWorld.fogMaxHight_S + 4 - center.getY());
				BiomeMist.looseRockGen.generate(gen.world, rand, checkPos);
				if (state == 1) biome.getRandomWorldGenForGrass(rand).generate(gen.world, rand, checkPos);
			}
		}

		private static void generateMiniRoomPart(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			if (roomCheck(gen, -3, 3, 0, 5, 1, 7, false)) {
				for (int x = -2; x <= 2; ++x) {
					for (int z = 1; z <= 6; ++z) {
						for (int y = 0; y <= 4; ++y) {
							checkPos = center.add(x, y, z);
							if (x == -2 || x == 2 || z == 1 || z == 6 || y == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
							else if (y == 0) gen.setBlockState(checkPos, redSandDry);
							else if (y == 3) {
								if (x == -1) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 1 : 0));
								else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 1 : 0));
								else if (z == 5) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
								else placeWeb(gen, checkPos, rand, true);
							} else if (y == 2) {
								if ((x == -1 || x == 1) && (z == 2 || z == 6)) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						}
					}
				}
				gen.setBlockState(center.add(0, 0, 1), MistBlocks.STONE.getDefaultState());
				gen.setBlockState(center.add(-3, 2, 3), getCobble(rand, wet ? 1 : 0));
				gen.setBlockState(center.add(3, 2, 3), getCobble(rand, wet ? 1 : 0));
				placeUrn(gen, center.add(-2, 2, 3), biomeType, loc, rand, true);
				placeUrn(gen, center.add(2, 2, 3), biomeType, loc, rand, true);
				int i = rand.nextInt(5);
				if (i == 0) {
					gen.setBlockState(center.add(-1, 2, 7), getCobble(rand, wet ? 1 : 0));
					placeUrn(gen, center.add(-1, 2, 6), biomeType, loc, rand, true);
					generateGrave(gen, center.add(1, 1, 5), Rotation.COUNTERCLOCKWISE_90, rand, wet, false);
				} else if (i == 1) {
					gen.setBlockState(center.add(1, 2, 7), getCobble(rand, wet ? 1 : 0));
					placeUrn(gen, center.add(1, 2, 6), biomeType, loc, rand, true);
					generateGrave(gen, center.add(-1, 1, 5), Rotation.CLOCKWISE_90, rand, wet, true);
				} else {
					gen.setBlockState(center.add(0, 2, 7), getCobble(rand, wet ? 1 : 0));
					placeUrn(gen, center.add(0, 2, 6), biomeType, loc, rand, true);
					generateGrave(gen, center.add(-1, 1, 5), Rotation.NONE, rand, wet, false);
					generateGrave(gen, center.add(1, 1, 5), Rotation.NONE, rand, wet, true);
				}
			} else generateEnd(gen, biome, rand, wet);
		}

		private static void generateRoom(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			if (roomCheck(gen, -5, 5, 0, 5, 1, 8, false)) {
				for (int x = -4; x <= 4; ++x) {
					for (int z = 1; z <= 7; ++z) {
						for (int y = 0; y <= 4; ++y) {
							checkPos = center.add(x, y, z);
							if (x == -4 || x == 4 || z == 1 || z == 7 || y == 4) {
								if (y != 4 || (x >= -2 && x <= 2)) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
							} else if (y == 0) {
								if (z == 4 && (x == 2 || x == -2)) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								else gen.setBlockState(checkPos, redSandDry);
							} else if (y == 3) {
								if (z == 6) {
									if (x >= -1 && x <= 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
									else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								} else {
									if (x == -1) gen.setBlockState(checkPos, getStep(EnumFacing.WEST, true, rand, wet ? 1 : 0));
									else if (x == 1) gen.setBlockState(checkPos, getStep(EnumFacing.EAST, true, rand, wet ? 1 : 0));
									else if (x == 0) placeWeb(gen, checkPos, rand, true);
									else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								}
							} else if (z == 4) {
								if (x == -3 || x == 3) placeWeb(gen, checkPos, rand, true);
								else if (x == -2 || x == 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (y == 2) {
								if ((x == -3 || x == 3) && (z == 2 || z == 7)) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						}
					}
				}
				gen.setBlockState(center.add(0, 0, 1), MistBlocks.STONE.getDefaultState());
				gen.setBlockState(center.add(-5, 2, 3), getCobble(rand, wet ? 1 : 0));
				gen.setBlockState(center.add(5, 2, 3), getCobble(rand, wet ? 1 : 0));
				placeUrn(gen, center.add(-4, 2, 3), biomeType, loc, rand, true);
				placeUrn(gen, center.add(4, 2, 3), biomeType, loc, rand, true);
				gen.setBlockState(center.add(-5, 2, 5), getCobble(rand, wet ? 1 : 0));
				gen.setBlockState(center.add(5, 2, 5), getCobble(rand, wet ? 1 : 0));
				placeUrn(gen, center.add(-4, 2, 5), biomeType, loc, rand, true);
				placeUrn(gen, center.add(4, 2, 5), biomeType, loc, rand, true);
				gen.setBlockState(center.add(-1, 2, 8), getCobble(rand, wet ? 1 : 0));
				gen.setBlockState(center.add(1, 2, 8), getCobble(rand, wet ? 1 : 0));
				placeUrn(gen, center.add(-1, 2, 7), biomeType, loc, rand, true);
				placeUrn(gen, center.add(1, 2, 7), biomeType, loc, rand, true);
				generateGrave(gen, center.add(-3, 1, 2), Rotation.CLOCKWISE_90, rand, wet, false);
				generateGrave(gen, center.add(3, 1, 2), Rotation.COUNTERCLOCKWISE_90, rand, wet, true);
				generateGrave(gen, center.add(-3, 1, 6), Rotation.CLOCKWISE_90, rand, wet, true);
				generateGrave(gen, center.add(3, 1, 6), Rotation.COUNTERCLOCKWISE_90, rand, wet, false);
				generateGrave(gen, center.add(0, 1, 6), Rotation.NONE, rand, wet, rand.nextBoolean());
			} else generateMiniRoomPart(gen, biome, rand, wet);
		}

		private static void generateEnd(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -1; x <= 1; ++x) {
				for (int z = 1; z <= 2; ++z) {
					for (int y = 0; y <= 3; ++y) {
						checkPos = center.add(x, y, z);
						if (x == -1 || x == 1 || z == 2 || y == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
						else if (y == 0) gen.setBlockState(checkPos, MistBlocks.STONE.getDefaultState());
						else if (y == 2) placeUrn(gen, checkPos, biomeType, loc, rand, true, 8);
						else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
					}
				}
			}
		}

		private static void generateGrave(GenUtil gen, BlockPos center, Rotation rotation, Random rand, boolean wet, boolean mirror) {
			GenUtil genRot = gen.add(center, rotation, Mirror.NONE);
			center = genRot.set.center;
			IBlockState state;
			if (rand.nextInt(8) == 0) state = getSlab(false, rand, wet ? 1 : 0);
			else state = getStairs(EnumFacing.SOUTH, false, rand, wet ? 1 : 0);
			genRot.setBlockState(center, state);
			int i = rand.nextInt(12);
			if (i == 0) state = Blocks.AIR.getDefaultState();
			else if (i == 1) state = getStep(EnumFacing.SOUTH, false, rand, wet ? 1 : 0);
			else if (i == 2) state = getStep(mirror ? EnumFacing.EAST : EnumFacing.WEST, false, rand, wet ? 1 : 0);
			else state = getSlab(false, rand, wet ? 1 : 0);
			genRot.setBlockState(center.north(), state);
			int size = rand.nextInt(3) + 4;
			genRot.setBlockState(center.down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, size).withProperty(Remains.OLD, true));
			genRot.setBlockState(center.north().down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, size).withProperty(Remains.OLD, true));
			((TileEntityRemains)genRot.getTileEntity(center.down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
			((TileEntityRemains)genRot.getTileEntity(center.north().down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
		}
	}

	public static class SnowTomb {

		private static final EnumBiomeType biomeType = EnumBiomeType.Cold;
		
		public static boolean generate(World world, BlockPos center, BiomeMist biome, Random rand) {
			GenUtil gen = new GenUtil(world, new GenSet(center, Rotation.values()[rand.nextInt(4)], Mirror.values()[rand.nextInt(2)]));
			if (rand.nextInt(5) != 0 && roomCheck(gen, -3, 3, -6, -1, -3, 3, true)) {
				generateSnowEntrance(gen, biome, rand, true);
				return true;
			} else {
				gen = new GenUtil(world, new GenSet(center.add(0, -3, 0), Rotation.CLOCKWISE_90, Mirror.NONE));
				if (roomCheck(gen, -2, 2, 0, 2, -3, 4, true)) {
					generateMiniRoomPart(gen, biome, rand, true, true);
					return true;
				}
			}
			return false;
		}

		private static void generateSnowEntrance(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -3; x <= 3; ++x) {
				for (int z = -3; z <= 3; ++z) {
					for (int y = -1; y >= -6; --y) {
						checkPos = center.add(x, y, z);
						if (x >= -1 && x <= 1 && z >= -1 && z <= 1) {
							if (y == -1) gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState());
							if (x == 0 || z == 0) {
								if (y == -6) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
								else if (y == -5) {
									if (gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos, Blocks.SNOW_LAYER.getDefaultState());
								} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else {
								int i = -1;
								if (z == 1) i = x == -1 ? -4 : -3;
								else if (x == 1) i = -2;
								if (y < i) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								else if (y > i) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
								else if (gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos, Blocks.SNOW_LAYER.getDefaultState());
							}
						} else {
							if (y == -1) {
								if (x >= -2 && x <= 2 && z >= -2 && z <= 2) {
									if (Math.abs(x * z) == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
									else if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 1 : 0));
									else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 1 : 0));
									else if (z == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 1 : 0));
									else if (z == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
								} else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								if (gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
							} else if (x == -3 || x == 3 || z == -3 || z == 3) {
								if (Math.abs(x * z) != 9) {
									if (x == 0 || z == 0) {
										if (y == -6) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
										else if (y <= -4) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
										else if (y == -3) {
											if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 1 : 0));
											else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 1 : 0));
											else if (z == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 1 : 0));
											else if (z == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
										} else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
									} else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								}
							} else if (Math.abs(x * z) == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
							else if (y == -6) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
							else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
			generateRoom(gen.add(gen.set.center.add(0, -6, 4), Rotation.NONE, Mirror.NONE), biome, rand, false);
			generateRoom(gen.add(gen.set.center.add(0, -6, -4), Rotation.CLOCKWISE_180, Mirror.NONE), biome, rand, false);
			generateRoom(gen.add(gen.set.center.add(-4, -6, 0), Rotation.CLOCKWISE_90, Mirror.NONE), biome, rand, false);
			generateRoom(gen.add(gen.set.center.add(4, -6, 0), Rotation.COUNTERCLOCKWISE_90, Mirror.NONE), biome, rand, false);
		}

		private static void generateRoom(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			if (rand.nextInt(3) == 0 && roomCheck(gen, -3, 3, 0, 5, 0, 6, false)) generateRoomPart(gen, biome, rand, wet);
			else if (rand.nextInt(3) != 0 && roomCheck(gen, -2, 2, 0, 5, 0, 4, false)) generateMiniRoomPart(gen, biome, rand, false, wet);
			else {
				BlockPos center = gen.set.center;
				BlockPos checkPos;
				for (int x = -1; x <= 1; ++x) {
					for (int z = 0; z <= 2; ++z) {
						for (int y = 0; y <= 3; ++y) {
							checkPos = center.add(x, y, z);
							gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
						}
					}
				}
				placeUrn(gen, center.add(0, 2, 1), biomeType, loc, rand, false, 1);
				gen.setBlockState(center.add(0, 1, 1), getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
				gen.setBlockState(center.add(0, 0, 1), MistBlocks.GRAVEL.getDefaultState());
				gen.setBlockState(center.add(0, 0, 0), MistBlocks.GRAVEL.getDefaultState());
			}
		}

		private static void generateRoomPart(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -3; x <= 3; ++x) {
				for (int z = 0; z <= 6; ++z) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, z);
						if (y == 4) {
							if (x <= -2 || x >= 2 || z <= 1 || z >= 5) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
							else if (x == -1) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 1 : 0));
							else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 1 : 0));
							else if (z == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 1 : 0));
							else if (z == 4) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
						} else if (x == -3 || x == 3 || z == 0 || z == 6) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
						else if (y == 0) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						else if (y == 3) {
							if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 1 : 0));
							else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 1 : 0));
							else if (z == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 1 : 0));
							else if (z == 5) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
							else if (x == 0 || z == 3) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							else placeWeb(gen, checkPos, rand, true);
						} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
					}
				}
			}
			gen.setBlockState(center.add(0, 4, 3), getSlab(true, rand, wet ? 1 : 0));
			gen.setBlockState(center.add(0, 0, 0), MistBlocks.GRAVEL.getDefaultState());

			generateGrave(gen, center.add(-1, 1, 6), Rotation.NONE, rand, wet);
			generateGrave(gen, center.add(1, 1, 6), Rotation.NONE, rand, wet);
			generateGrave(gen, center.add(-3, 1, 2), Rotation.CLOCKWISE_90, rand, wet);
			generateGrave(gen, center.add(-3, 1, 4), Rotation.CLOCKWISE_90, rand, wet);
			generateGrave(gen, center.add(3, 1, 2), Rotation.COUNTERCLOCKWISE_90, rand, wet);
			generateGrave(gen, center.add(3, 1, 4), Rotation.COUNTERCLOCKWISE_90, rand, wet);

			placeWeb(gen, center.add(-2, 2, 1), rand, false);
			placeWeb(gen, center.add(-2, 2, 5), rand, false);
			placeWeb(gen, center.add(2, 2, 1), rand, false);
			placeWeb(gen, center.add(2, 2, 5), rand, false);

			placeUrn(gen, center.add(-2, 1, 1), biomeType, loc, rand, false);
			placeUrn(gen, center.add(-2, 1, 3), biomeType, loc, rand, false);
			placeUrn(gen, center.add(-2, 1, 5), biomeType, loc, rand, false);
			placeUrn(gen, center.add(0, 1, 5), biomeType, loc, rand, false);
			placeUrn(gen, center.add(2, 1, 5), biomeType, loc, rand, false);
			placeUrn(gen, center.add(2, 1, 3), biomeType, loc, rand, false);
			placeUrn(gen, center.add(2, 1, 1), biomeType, loc, rand, false);
		}

		private static void generateMiniRoomPart(GenUtil gen, BiomeMist biome, Random rand, boolean separate, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -2; x <= 2; ++x) {
				for (int z = 0; z <= 4; ++z) {
					for (int y = 0; y <= 5; ++y) {
						checkPos = center.add(x, y, z);
						if (y == 5) {
							if (separate) {
								if (x >= -1 && x <= 1 && z >= 1 && z <= 3) {
									if (x == 0 && z == 2) {
										gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
										if (gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
									} else if (x == -1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 1 : 0));
									else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 1 : 0));
									else if (z == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 1 : 0));
									else if (z == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, wet ? 1 : 0));
								}
							}
						} else if (x == -2 || x == 2 || z == 0 || z == 4 || y == 4) {
							if (separate && y == 4) {
								if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 1 : 0));
								else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 1 : 0));
								else if (z == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 1 : 0));
								else if (z == 4) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, wet ? 1 : 0));
								else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
							} else gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
						} else if (y == 0) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						else if (y == 3) {
							if (x == -1) gen.setBlockState(checkPos, getStep(EnumFacing.WEST, true, rand, wet ? 1 : 0));
							else if (x == 1) gen.setBlockState(checkPos, getStep(EnumFacing.EAST, true, rand, wet ? 1 : 0));
							else if (z == 1) gen.setBlockState(checkPos, getStep(EnumFacing.NORTH, true, rand, wet ? 1 : 0));
							else if (z == 3) gen.setBlockState(checkPos, getStep(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
							else placeWeb(gen, checkPos, rand, true);
						} else if (y == 2) {
							if (x == 0 || z == 2) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							else placeWeb(gen, checkPos, rand, true);
						} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
					}
				}
			}
			gen.setBlockState(center.add(0, 4, 2), getSlab(true, rand, wet ? 1 : 0));
			gen.setBlockState(center.add(0, 0, 0), MistBlocks.GRAVEL.getDefaultState());

			generateGrave(gen, center.add(0, 1, 4), Rotation.NONE, rand, wet);
			generateGrave(gen, center.add(-2, 1, 2), Rotation.CLOCKWISE_90, rand, wet);
			generateGrave(gen, center.add(2, 1, 2), Rotation.COUNTERCLOCKWISE_90, rand, wet);

			placeUrn(gen, center.add(-1, 1, 1), biomeType, loc, rand, false);
			placeUrn(gen, center.add(1, 1, 1), biomeType, loc, rand, false);
			placeUrn(gen, center.add(-1, 1, 3), biomeType, loc, rand, false);
			placeUrn(gen, center.add(1, 1, 3), biomeType, loc, rand, false);

			if (separate) {
				for (int x = -1; x <= 1; ++x) {
					for (int z = -3; z <= -1; ++z) {
						for (int y = 0; y <= 2; ++y) {
							checkPos = center.add(x, y, z);
							if (x == -1 || x == 1) {
								gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
								if (y == 2 && gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
							} else if (z <= -2) {
								if (y == -z - 1) {
									gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, wet ? 1 : 0));
									if (y == 2) gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState());
								} else if (y < -z - 1) gen.setBlockState(checkPos, getCobble(rand, wet ? 1 : 0));
							} else if (y == 0) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
							else if (y == 1) gen.setBlockState(checkPos, biome.fillerBlock);
							else if (y == 2) {
								gen.setBlockState(checkPos, biome.topBlock);
								if (gen.world.canBlockSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
							}
						}
					}
				}
				gen.setBlockState(center.add(0, 1, 0), biome.fillerBlock);
				gen.setBlockState(center.add(0, 2, 0), biome.fillerBlock);
			}
		}

		private static void generateGrave(GenUtil gen, BlockPos center, Rotation rotation, Random rand, boolean wet) {
			GenUtil genRot = gen.add(center, rotation, Mirror.NONE);
			center = genRot.set.center;
			genRot.setBlockState(center, getStairs(EnumFacing.SOUTH, false, rand, wet ? 1 : 0));
			genRot.setBlockState(center.up(), getStairs(EnumFacing.SOUTH, true, rand, wet ? 1 : 0));
			int i = rand.nextInt(16);
			IBlockState state;
			if (i == 0) state = Blocks.AIR.getDefaultState();
			else if (i == 1) state = getStep(EnumFacing.EAST, false, rand, wet ? 1 : 0);
			else if (i == 2) state = getStep(EnumFacing.WEST, false, rand, wet ? 1 : 0);
			else if (i < 6) state = getStep(EnumFacing.SOUTH, false, rand, wet ? 1 : 0);
			else state = getSlab(false, rand, wet ? 1 : 0);
			genRot.setBlockState(center.north(), state);
			genRot.setBlockState(center.north().down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, rand.nextInt(3) + 4).withProperty(Remains.OLD, true));
			((TileEntityRemains)genRot.getTileEntity(center.north().down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
		}
	}

	public static class SavannaTomb {

		private static final EnumBiomeType biomeType = EnumBiomeType.Desert;
		
		public static boolean generate(World world, BlockPos center, BiomeMist biome, Random rand) {
			GenUtil gen = new GenUtil(world, new GenSet(center, Rotation.CLOCKWISE_90, Mirror.NONE));
			if (roomCheck(gen, -1, 2, -6, -1, -3, 0, true) && roomCheck(gen, -1, 2, -6, -1, 1, 4, false)) {
				if ((rand.nextInt(3) == 0 && generateCross(gen.add(center.add(0, -6, 5), Rotation.NONE, Mirror.NONE), biome, rand, false, 1)) ||
						generateRoom(gen.add(center.add(0, -6, 5), Rotation.NONE, Mirror.NONE), biome, rand, false, 0)) {
					generateSavannaEntrance(gen, biome, rand, false);
					return true;
				}
			}
			return false;
		}

		private static boolean generateRoom(GenUtil gen, BiomeMist biome, Random rand, boolean wet, int count) {
			if (roomCheck(gen, -2, 3, 0, 5, 0, 3, false)) {
				boolean check = true;
				int j = rand.nextInt(3);
				for (int i = 0; i < j; ++i) {
					if (!roomCheck(gen, -2, 3, 0, 5, 4 + i * 2, 5 + i * 2, false)) {
						if (i == 0) check = false;
						j = i;
						break;
					}
				}
				if (check) {
					++j;
					for (int i = 0; i < j; ++i) {
						generateRoomPart(gen.add(gen.set.center.add(0, 0, i * 2), Rotation.NONE, Mirror.NONE), biome, rand, i == j - 1, wet, count);
					}
					return true;
				}
			}
			return false;
		}

		private static boolean generateCross(GenUtil gen, BiomeMist biome, Random rand, boolean wet, int count) {
			if (roomCheck(gen, -3, 4, 0, 5, 0, 4, false)) {
				boolean check = false;
				GenUtil gen1 = gen.add(gen.set.center.add(0, 0, 3), Rotation.NONE, Mirror.NONE);
				if (generateRoom(gen1, biome, rand, wet, count)) {
					generateCrossWall(gen1, biome, rand, true, wet);
					check = true;
				} else generateCrossWall(gen1, biome, rand, false, wet);

				gen1 = gen.add(gen.set.center.add(-2, 0, 0), Rotation.CLOCKWISE_90, Mirror.NONE);
				if (generateRoom(gen1, biome, rand, wet, count)) {
					generateCrossWall(gen1, biome, rand, true, wet);
					check = true;
				} else generateCrossWall(gen1, biome, rand, false, wet);

				gen1 = gen.add(gen.set.center.add(3, 0, 1), Rotation.COUNTERCLOCKWISE_90, Mirror.NONE);
				if (generateRoom(gen1, biome, rand, wet, count)) {
					generateCrossWall(gen1, biome, rand, true, wet);
					check = true;
				} else generateCrossWall(gen1, biome, rand, false, wet);

				if (check) {
					generateCrossWall(gen, biome, rand, true, wet);
					generateCrossPart(gen, biome, rand, wet);
					return true;
				}
			}
			return false;
		}
		
		private static void generateCrossWall(GenUtil gen, BiomeMist biome, Random rand, boolean medium, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			if (medium || rand.nextBoolean()) {
				if (!medium) {
					for (int x = -1; x <= 2; ++x) {
						for (int y = 0; y <= 4; ++y) {
							checkPos = center.add(x, y, 0);
							gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						}
					}
				}
				for (int x = -1; x <= 2; ++x) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, -1);
						if (x == -1 || x == 2 || y == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						else if (y == 0) gen.setBlockState(checkPos, sand);
						else if (y == 1) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						else if (y == 2) placeWeb(gen, checkPos, rand, true);
						else if (x == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
						else gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
					}
				}
			} else {
				for (int x = -1; x <= 2; ++x) {
					for (int z = -1; z <= 0; ++z) {
						for (int y = 0; y <= 4; ++y) {
							checkPos = center.add(x, y, z);
							if (z == -1) {
								if (x == -1 || x == 2 || y == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								else if (y == 0) gen.setBlockState(checkPos, sand);
								else if (y == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
								else if (y == 2) placeUrn(gen, checkPos, biomeType, loc, rand, true, 2);
								else if (x == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
							} else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						}
					}
				}
			}
		}

		private static void generateCrossPart(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = 0; x <= 1; ++x) {
				for (int z = 0; z <= 1; ++z) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, z);
						if (y == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						else if (y == 0) gen.setBlockState(checkPos, sand);
						else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
					}
				}
			}
		}

		private static void generateSavannaEntrance(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			int grass = rand.nextInt(3);
			boolean mirror = rand.nextBoolean();
			for (int x = -1; x <= 2; ++x) {
				for (int z = -3; z <= 4; ++z) {
					for (int y = -6; y <= -1; ++y) {
						checkPos = center.add(x, y, z);
						if (y == -1) {
							if (z <= 0) {
								if (x == -1) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 1 : -1));
								else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 1 : -1));
								else if (z == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 1 : -1));
								else if (z == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, wet ? 1 : -1));
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							}
						} else {
							if (x == -1 || x == 2 || z == -3 || (y == -2 && z > 0)) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							else if (z < 0) {
								if ((x == 0 && ((z == -2 && y <= (mirror ? -2 : -3)) || (z == -1 && y <= (mirror ? -5 : -4)))) ||
									(x == 1 && ((z == -2 && y <= (mirror ? -3 : -2)) || (z == -1 && y <= (mirror ? -4 : -5))))) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								else if (y == -2) {
									if (grass == 0) gen.setBlockState(checkPos, biome.topBlock);
									else if (grass == 1) gen.setBlockState(checkPos, biome.topBlock.withProperty(MistDirt.HUMUS, 0));
									else gen.setBlockState(checkPos, sand);
								} else gen.setBlockState(checkPos, sand);
							} else if (y == -6) gen.setBlockState(checkPos, sand);
							else if (y == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
							else if (y < -2 - z) gen.setBlockState(checkPos, sand);
							else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
			gen.setBlockState(center.add(0, -3, 4), getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
			gen.setBlockState(center.add(1, -3, 4), getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
			gen.setBlockState(center.add(0, -3, 1), getSlab(true, rand, wet ? 0 : d));
			gen.setBlockState(center.add(1, -3, 1), getSlab(true, rand, wet ? 0 : d));
		}

		private static void generateRoomPart(GenUtil gen, BiomeMist biome, Random rand, boolean end, boolean wet, int count) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -2; x <= 3; ++x) {
				for (int z = 0; z <= 2; ++z) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, z);
						 if (z == 1) {
							if (x == -2 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							else if (y == 4) {
								if (x == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							} else if (y == 0) gen.setBlockState(checkPos, sand);
							else if (x == -1) {
								if (y == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else placeUrn(gen, checkPos, biomeType, loc, rand, true);
							} else if (x == 2) {
								if (y == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else placeUrn(gen, checkPos, biomeType, loc, rand, true);
							} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						} else if (end || z == 0) {
							if (x == -2 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							else if (y == 4) {
								if (x == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							} else if (y == 0) gen.setBlockState(checkPos, sand);
							else if (x == -1) {
								if (y == 2) placeUrn(gen, checkPos, biomeType, loc, rand, true);
								else gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
							} else if (x == 2) {
								if (y == 2) placeUrn(gen, checkPos, biomeType, loc, rand, true);
								else gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
							} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						}
					}
				}
			}
			if (end) {
				if (count > 1 || rand.nextInt(4) != 0 ||
					!generateCross(gen.add(center.add(0, 0, 4), Rotation.NONE, Mirror.NONE), biome, rand, wet, count + 1)) {
					for (int x = -2; x <= 3; ++x) {
						for (int z = 3; z <= 4; ++z) {
							for (int y = 0; y <= 4; ++y) {
								checkPos = center.add(x, y, z);
								if (z == 3) {
									if (x < 0 || x > 1 || y == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
									else if (y == 0) gen.setBlockState(checkPos, sand);
									else if (y == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									else if (y == 2) placeUrn(gen, checkPos, biomeType, loc, rand, true, 2);
									else if (x == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
									else gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								} else if (x != -2 && x != 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							}
						}
					}
					placeWeb(gen, center.add(0, 3, 2), rand, false);
					placeWeb(gen, center.add(1, 3, 2), rand, false);
				}
			}
		}
	}

	public static class ForestTomb {

		private static final EnumBiomeType biomeType = EnumBiomeType.Forest;
		
		public static boolean generate(World world, BlockPos center, BiomeMist biome, Random rand) {
			int offset = rand.nextInt(3);
			GenUtil gen = new GenUtil(world, new GenSet(center, Rotation.CLOCKWISE_90, Mirror.NONE));
			boolean mini = rand.nextInt(3) == 0;
			if (roomCheck(gen, -1, 1, -6 - offset, -1, -5 - offset, 0, true)) {
				boolean check = false;
				if (!mini) {
					if (roomCheck(gen, -4, 4, -6 - offset, -1 + offset, 1, 5 + offset, false)) check = true;
					else mini = true;
				}
				if (mini) check = roomCheck(gen, -3, 3, -6 - offset, -1 + offset, 1, 5 + offset, false);
				if (check) {
					int f = mini ? 3 : 4;
					generateEntrance(gen, biome, rand, offset, true);
					int j = (mini ? 1 : 2) - rand.nextInt(rand.nextInt(mini ? 2 : 3) + 1);
					for (int i = 0; i < j; ++i) {
						if (!roomCheck(gen, -f, f, -6 - offset, -1 - offset, 6 + offset + i * 2, 7 + offset + i * 2, false)) {
							j = i;
							break;
						}
					}
					++j;
					for (int i = 0; i < j; ++i) {
						if (mini) generateMiniRoomPart(gen.add(center.add(0, -6 - offset, 2 + i * 2), Rotation.NONE, Mirror.NONE), biome, biomeType, loc, rand, false, i == 0, i == j - 1, true);
						else generateRoomPart(gen.add(center.add(0, -6 - offset, 2 + i * 2), Rotation.NONE, Mirror.NONE), biome, rand, i == 0, i == j - 1, true);
					}
					return true;
				}
			}
			return false;
		}

		private static void generateRoomPart(GenUtil gen, BiomeMist biome, Random rand, boolean start, boolean end, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -4; x <= 4; ++x) {
				for (int z = 0; z <= 2; ++z) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, z);
						if (z == 1) {
							if (x == -4 || x == 4 || y == 4) {
								if (y == 4) {
									if (x >= -1 && x <= 1) gen.setBlockState(checkPos, getSlab(true, rand, wet ? 0 : d));
									else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								} else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							} else if (y == 3) {
								if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else if (x == -2 || x == 2) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (y == 2) {
								if (x == -3 || x == 3) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (y == 0) {
								if (x == -3 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
							} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						} else if (end || z == 0) {
							if (x == -4 || x == 4 || y == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							else if (y == 3) {
								if (x == 3 || x == -3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								else if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == -1) gen.setBlockState(checkPos, getStep(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else if (x == 1) gen.setBlockState(checkPos, getStep(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (y == 2) {
								if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else if (x == -2 || x == 2) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (y == 1) {
								if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 0 : d));
								else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 0 : d));
								else if (x == -2 || x == 2) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (x == -3 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							else gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						} 
					}
				}
			}
			if (start || rand.nextInt(3) != 0) {
				generateGrave(gen, center.add(-3, 1, 1), Rotation.CLOCKWISE_90, rand, wet);
				generateGrave(gen, center.add(3, 1, 1), Rotation.COUNTERCLOCKWISE_90, rand, wet);
			} else {
				if (rand.nextBoolean() && gen.getBlockState(center.add(-3, 0, -1)).getBlock() == MistBlocks.REMAINS) {
					if (generateMiniRoom(gen.add(center.add(-3, 0, 1), Rotation.CLOCKWISE_90, Mirror.NONE), biome, biomeType, loc, rand, false, wet)) {
						gen.setBlockState(center.add(-3, 0, 1), MistBlocks.GRAVEL.getDefaultState());
						gen.setBlockState(center.add(-4, 0, 1), MistBlocks.GRAVEL.getDefaultState());
						placeWeb(gen, center.add(-4, 1, 1), rand, true);
						placeWeb(gen, center.add(-4, 2, 1), rand, true);
					} else generateGrave(gen, center.add(-3, 1, 1), Rotation.CLOCKWISE_90, rand, wet);
				} else generateGrave(gen, center.add(-3, 1, 1), Rotation.CLOCKWISE_90, rand, wet);
				if (rand.nextBoolean() && gen.getBlockState(center.add(3, 0, -1)).getBlock() == MistBlocks.REMAINS) {
					if (generateMiniRoom(gen.add(center.add(3, 0, 1), Rotation.COUNTERCLOCKWISE_90, Mirror.NONE), biome, biomeType, loc, rand, false, wet)) {
						gen.setBlockState(center.add(3, 0, 1), MistBlocks.GRAVEL.getDefaultState());
						gen.setBlockState(center.add(4, 0, 1), MistBlocks.GRAVEL.getDefaultState());
						placeWeb(gen, center.add(4, 1, 1), rand, true);
						placeWeb(gen, center.add(4, 2, 1), rand, true);
					} else generateGrave(gen, center.add(3, 1, 1), Rotation.COUNTERCLOCKWISE_90, rand, wet);
				} else generateGrave(gen, center.add(3, 1, 1), Rotation.COUNTERCLOCKWISE_90, rand, wet);
			}
			if (start) {
				for (int x = -4; x <= 4; ++x) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, -1);
						if (x < -1 || x > 1) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
					}
				}
			}
			if (end) {
				int i = rand.nextInt(3);
				if (i == 0) {
					for (int x = -4; x <= 4; ++x) {
						for (int y = 0; y <= 4; ++y) {
							checkPos = center.add(x, y, 3);
							if (x < -1 || x > 1 || y != 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						}
					}
					gen.setBlockState(center.add(-1, 2, 4), getCobble(rand, wet ? 0 : d));
					gen.setBlockState(center.add(0, 2, 4), getCobble(rand, wet ? 0 : d));
					gen.setBlockState(center.add(1, 2, 4), getCobble(rand, wet ? 0 : d));
					placeUrn(gen, center.add(-1, 2, 3), biomeType, loc, rand, true, 3);
					placeUrn(gen, center.add(0, 2, 3), biomeType, loc, rand, true, 3);
					placeUrn(gen, center.add(1, 2, 3), biomeType, loc, rand, true, 3);
				} else if (i == 1) {
					for (int x = -4; x <= 4; ++x) {
						for (int z = 3; z <= 5; ++z) {
							for (int y = 0; y <= 4; ++y) {
								checkPos = center.add(x, y, z);
								if (x == -4 || x == 4 || y == 4 || z == 5) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								else if (y == 0) {
									if (x == -3 || x == 3 || z == 4) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
									else gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
								} else if (z == 3) {
									if (y == 3) {
										if (x == -3 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else if (x == -1 || x == 1) gen.setBlockState(checkPos, getSlab(true, rand, wet ? 0 : d));
										else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									} else if (y == 2) {
										if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
										else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
										else if (x == -2 || x == 2) placeWeb(gen, checkPos, rand, true);
										else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
									} else if (y == 1) {
										if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 0 : d));
										else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 0 : d));
										else if (x != -1 && x != 1) placeUrn(gen, checkPos, biomeType, loc, rand, true);
									}
								} else {
									if (y == 3) {
										if (x != -1 && x != 1) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									} else if (y == 2) {
										if (x == -3 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else if (x != -1 && x != 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
										else placeWeb(gen, checkPos, rand, true);
									} else if (y == 1) {
										if (x == -3 || x == 3) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else if (x != -1 && x != 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 0 : d));
									}
								}
							}
						}
					}
					gen.setBlockState(center.add(0, 3, 2), getStep(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
					generateGrave(gen, center.add(-1, 1, 4), Rotation.NONE, rand, wet);
					generateGrave(gen, center.add(1, 1, 4), Rotation.NONE, rand, wet);
				} else {
					for (int x = -4; x <= 4; ++x) {
						for (int z = 3; z <= 5; ++z) {
							for (int y = 0; y <= 4; ++y) {
								checkPos = center.add(x, y, z);
								if (x <= -2 || x >= 2 || y == 4 || z == 5) {
									if (z == 3 || (x >= -2 && x <= 2)) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								} else if (x == 0) {
									if (y > 1) {
										if (z == 3) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
										else if (y == 2) placeWeb(gen, checkPos, rand, true);
										else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									}
								} else {
									if (z == 3) {
										if (y == 0) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
										else if (y == 1) placeUrn(gen, checkPos, biomeType, loc, rand, true);
										else if (y == 2) placeWeb(gen, checkPos, rand, true);
										else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									} else {
										if (y == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 0 : d));
										else if (y == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
										else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
									}
								}
							}
						}
					}
					generateGrave(gen, center.add(0, 1, 4), Rotation.NONE, rand, wet);
					placeUrn(gen, center.add(-2, 1, 2), biomeType, loc, rand, false);
					placeUrn(gen, center.add(2, 1, 2), biomeType, loc, rand, false);
				}
			}
		}

		public static boolean generateMiniRoom(GenUtil gen, BiomeMist biome, EnumBiomeType biomeType, UrnLocation loc, Random rand, boolean close, boolean wet) {
			if (roomCheck(gen, -3, 3, 0, 4, 1, 5, false)) {
				int j = rand.nextInt(2);
				for (int i = 0; i < j; ++i) {
					if (!roomCheck(gen, -3, 3, 0, 4, 6 + i * 2, 7 + i * 2, false)) {
						j = i;
						break;
					}
				}
				++j;
				for (int i = 0; i < j; ++i) {
					generateMiniRoomPart(gen.add(gen.set.center.add(0, 0, 2 + i * 2), Rotation.NONE, Mirror.NONE), biome, biomeType, loc, rand, close, i == 0, i == j - 1, wet);
				}
				return true;
			}
			return false;
		}

		private static void generateMiniRoomPart(GenUtil gen, BiomeMist biome, EnumBiomeType biomeType, UrnLocation loc, Random rand, boolean close, boolean start, boolean end, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -3; x <= 3; ++x) {
				for (int z = 0; z <= 2; ++z) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, z);
						if (z == 1) {
							if (x == -3 || x == 3 || y == 4) {
								if (!close || y > 0) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							} else if (y == 0) {
								if (!close && x == 0) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
							} else if (y == 1) {
								if (x == 0) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else if (y == 2) {
								if (x == -2 || x == 2) placeWeb(gen, checkPos, rand, true);
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							} else {
								if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else if (x == 0) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
								else placeWeb(gen, checkPos, rand, true);
							}
						} else if (end || z == 0) {
							if (x == -3 || x == 3 || y == 4) {
								if (!close || y > 0) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							} else if (y == 0) {
								if (!close) {
									if (x == -2 || x == 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
									else gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
								}
							} else if (y == 1) {
								if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 0 : d));
								else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 0 : d));
								else if (x == 0) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
								else placeWeb(gen, checkPos, rand, true);
							} else if (y == 2) {
								if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else if (x == 0) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
								else placeWeb(gen, checkPos, rand, true);
							} else {
								if (x == 0) placeWeb(gen, checkPos, rand, true);
								else if (x == -1) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
								else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							}
						}
					}
				}
			}
			generateGrave(gen, center.add(-2, 1, 1), Rotation.CLOCKWISE_90, rand, wet);
			generateGrave(gen, center.add(2, 1, 1), Rotation.COUNTERCLOCKWISE_90, rand, wet);
			if (start) {
				for (int x = -3; x <= 3; ++x) {
					for (int y = 0; y <= 4; ++y) {
						checkPos = center.add(x, y, -1);
						if (x < -1 || x > 1) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
					}
				}
			}
			if (end) {
				int i = rand.nextInt(3);
				if (rand.nextBoolean()) {
					for (int x = -3; x <= 3; ++x) {
						for (int y = 0; y <= 4; ++y) {
							checkPos = center.add(x, y, 3);
							if (x != 0 || y != 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						}
					}
					gen.setBlockState(center.add(0, 2, 4), getCobble(rand, wet ? 0 : d));
					placeUrn(gen, center.add(0, 2, 3), biomeType, loc, rand, true, 2);
				} else {
					for (int x = -3; x <= 3; ++x) {
						for (int z = 3; z <= 5; ++z) {
							for (int y = 0; y <= 4; ++y) {
								checkPos = center.add(x, y, z);
								if (x <= -3 || x >= 3 || y == 4 || z == 5) {
									if (z != 5 || (x != -3 && x != 3)) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
								}
								else if (z == 3) {
									if (y == 0) {
										if (!close) {
											if (x == -2 || x == 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
											else if (x != 0) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
										}
									} else if (y == 1) {
										if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, false, rand, wet ? 0 : d));
										else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, false, rand, wet ? 0 : d));
										else if (x != 0) placeUrn(gen, checkPos, biomeType, loc, rand, true);
									} else if (y == 2) {
										if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
										else if (x == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
										else if (x == 0) gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
										else placeWeb(gen, checkPos, rand, true);
									} else {
										if (x == -2 || x == 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else if (x == 0) placeWeb(gen, checkPos, rand, true);
										else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									}
								} else {
									if (y == 0) {
										if (x != 0) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
									} else if (y == 1) {
										if (x == -2 || x == 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else if (x != 0) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 0 : d));
									} else if (y == 2) {
										if (x == -2 || x == 2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
										else if (x == 0) placeWeb(gen, checkPos, rand, true);
										else gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
									} else {
										if (x == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
										else gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
									}
								}
							}
						}
					}
					generateGrave(gen, center.add(0, 1, 4), Rotation.NONE, rand, wet);
				}
			}
		}

		private static void generateGrave(GenUtil gen, BlockPos center, Rotation rotation, Random rand, boolean wet) {
			GenUtil genRot = gen.add(center, rotation, Mirror.NONE);
			center = genRot.set.center;
			genRot.setBlockState(center, getSlab(false, rand, wet ? 0 : d));
			int i = rand.nextInt(16);
			IBlockState state;
			if (i == 0) state = Blocks.AIR.getDefaultState();
			else if (i == 1) state = getStep(EnumFacing.EAST, false, rand, wet ? 0 : d);
			else if (i == 2) state = getStep(EnumFacing.WEST, false, rand, wet ? 0 : d);
			else if (i < 6) state = getStep(EnumFacing.SOUTH, false, rand, wet ? 0 : d);
			else state = getSlab(false, rand, wet ? 0 : d);
			genRot.setBlockState(center.north(), state);
			int size = rand.nextInt(3) + 4;
			genRot.setBlockState(center.down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, size).withProperty(Remains.OLD, true));
			genRot.setBlockState(center.north().down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, size).withProperty(Remains.OLD, true));
			((TileEntityRemains)genRot.getTileEntity(center.down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
			((TileEntityRemains)genRot.getTileEntity(center.north().down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
		}
	}

	public static class SwampTomb {

		private static final EnumBiomeType biomeType = EnumBiomeType.Swamp;
		
		public static boolean generate(World world, BlockPos center, BiomeMist biome, Random rand) {
			GenUtil gen = new GenUtil(world, new GenSet(center, Rotation.CLOCKWISE_90, Mirror.NONE));
			if (roomCheck(gen, -1, 1, -6, -1, -5, 1, true)) { // entrance
				int i = rand.nextInt(3);
				boolean wet = !(biome instanceof BiomeMistUpSavanna);
				/** Room */
				if (i < 2 && roomCheck(gen, -4, 4, -7, -1, 1, 7, false)) { // room
					if (i == 0 && roomCheck(gen, -4, 4, -7, -1, -9, -3, false)) {
						generateEntrance(gen, biome, rand, 0, wet);
						generateRoom(gen, biome, rand, true, false, wet);
						if (rand.nextInt(3) == 0 && roomCheck(gen, -4, 4, -9, -8, -9, -3, false)) {
							generateRoom(gen.add(center.north(2).down(2), Rotation.CLOCKWISE_180, Mirror.NONE), biome, rand, true, true, wet);
							generateStairs(gen, biome, rand, wet);
							generateStairs(gen.add(center, Rotation.NONE, Mirror.FRONT_BACK), biome, rand, wet);
						} else {
							generateRoom(gen.add(center.north(2), Rotation.CLOCKWISE_180, Mirror.NONE), biome, rand, true, true, wet);
							generateCorridor(gen, biome, rand, wet);
							generateCorridor(gen.add(center, Rotation.NONE, Mirror.FRONT_BACK), biome, rand, wet);
						}
					} else {
						generateEntrance(gen, biome, rand, 0, wet);
						generateRoom(gen, biome, rand, false, false, wet);
					}
					return true;
				} else if (roomCheck(gen, -2, 2, -7, -1, 1, 6, false)) { // mini room
					generateEntrance(gen, biome, rand, 0, wet);
					generateMiniRoom(gen, biome, biomeType, loc, rand, false, wet);
					return true;
				}
			}
			return false;
		}

		private static void generateRoom(GenUtil gen, BiomeMist biome, Random rand, boolean mini, boolean back, boolean wet) {
			generateRoom(gen, biome, rand, mini, back, true, wet);
		}

		private static void generateRoom(GenUtil gen, BiomeMist biome, Random rand, boolean mini, boolean back, boolean backUrn, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -4; x <= 4; ++x) {
				for (int z = 1; z <= 7; ++z) {
					for (int y = -6; y <= -2; ++y) {
						checkPos = center.add(x, y, z);
						if (x == -4 || x == 4 || y == -2 || z == 1 || z == 7) {
							if (x < -1 || x > 1 || (back ? (x != 0 || y < -3 || z > 2 || (z == 2 && gen.getBlockState(checkPos).isFullCube())) : z != 1)) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						} else if (y == -6) {
							gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						} else if (y == -3) {
							if (z == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 0 : d));
							else if (z == 6) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
							else if (x == -3) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
							else if (x == 3) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
							else {
								if (x == -1 || x == 1) gen.setBlockState(checkPos, getSlab(true, rand, wet ? 0 : d));
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							}
						} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
					}
				}
			}
			generateGrave(gen, center.add(0, -5, 7), Rotation.NONE, rand, wet);
			if (mini) {
				generateGrave(gen, center.add(-2, -5, 7), Rotation.NONE, rand, wet);
				generateGrave(gen, center.add(2, -5, 7), Rotation.NONE, rand, wet);

				gen.setBlockState(center.add(-5, -4, 5), getCobble(rand, wet ? 0 : d));
				gen.setBlockState(center.add(5, -4, 5), getCobble(rand, wet ? 0 : d));
				gen.setBlockState(center.add(-5, -4, 3), getCobble(rand, wet ? 0 : d));
				gen.setBlockState(center.add(5, -4, 3), getCobble(rand, wet ? 0 : d));
				// Urns
				placeUrn(gen, center.add(-4, -4, 5), biomeType, loc, rand, true);
				placeUrn(gen, center.add(4, -4, 5), biomeType, loc, rand, true);
				placeUrn(gen, center.add(-4, -4, 3), biomeType, loc, rand, true);
				placeUrn(gen, center.add(4, -4, 3), biomeType, loc, rand, true);
				placeUrn(gen, center.add(-1, -5, 6), biomeType, loc, rand, false);
				placeUrn(gen, center.add(1, -5, 6), biomeType, loc, rand, false);
				if (back) {
					placeUrn(gen, center.add(-1, -4, 1), biomeType, loc, rand, true);
					placeUrn(gen, center.add(1, -4, 1), biomeType, loc, rand, true);
				}
			} else {
				generateGrave(gen, center.add(-4, -5, 3), Rotation.CLOCKWISE_90, rand, wet);
				generateGrave(gen, center.add(-4, -5, 5), Rotation.CLOCKWISE_90, rand, wet);
				generateGrave(gen, center.add(4, -5, 3), Rotation.COUNTERCLOCKWISE_90, rand, wet);
				generateGrave(gen, center.add(4, -5, 5), Rotation.COUNTERCLOCKWISE_90, rand, wet);

				gen.setBlockState(center.add(-2, -4, 8), getCobble(rand, wet ? 0 : d));
				gen.setBlockState(center.add(2, -4, 8), getCobble(rand, wet ? 0 : d));
				// Urns
				placeUrn(gen, center.add(-2, -4, 7), biomeType, loc, rand, true);
				placeUrn(gen, center.add(2, -4, 7), biomeType, loc, rand, true);
				placeUrn(gen, center.add(-3, -5, 2), biomeType, loc, rand, false);
				placeUrn(gen, center.add(-3, -5, 4), biomeType, loc, rand, false);
				placeUrn(gen, center.add(3, -5, 2), biomeType, loc, rand, false);
				placeUrn(gen, center.add(3, -5, 4), biomeType, loc, rand, false);

				if (backUrn) {
					gen.setBlockState(center.add(-2, -4, 0), getCobble(rand, wet ? 0 : d));
					gen.setBlockState(center.add(2, -4, 0), getCobble(rand, wet ? 0 : d));
					placeUrn(gen, center.add(-2, -4, 1), biomeType, loc, rand, true);
					placeUrn(gen, center.add(2, -4, 1), biomeType, loc, rand, true);
				}
			}
			placeUrn(gen, center.add(-3, -5, 6), biomeType, loc, rand, false);
			placeUrn(gen, center.add(3, -5, 6), biomeType, loc, rand, false);
			// Web
			placeWeb(gen, center.add(-3, -4, 6), rand, false);
			placeWeb(gen, center.add(3, -4, 6), rand, false);
			placeWeb(gen, center.add(-3, -4, 2), rand, false);
			placeWeb(gen, center.add(3, -4, 2), rand, false);
			placeWeb(gen, center.add(-2, -3, 5), rand, false);
			placeWeb(gen, center.add(2, -3, 5), rand, false);
			placeWeb(gen, center.add(-2, -3, 3), rand, false);
			placeWeb(gen, center.add(2, -3, 3), rand, false);
		}

		public static void generateMiniRoom(GenUtil gen, BiomeMist biome, EnumBiomeType biomeType, UrnLocation loc, Random rand, boolean close, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			for (int x = -2; x <= 2; ++x) {
				for (int z = 1; z <= 6; ++z) {
					for (int y = -6; y <= -2; ++y) {
						checkPos = center.add(x, y, z);
						if (x == -2 || x == 2 || y == -2 || z == 1 || z == 6) {
							if (close ? y != -6 : z != 1) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						} else if (y == -6) {
							if (!close) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						} else if (y == -3) {
							if (x == -1) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
							else if (x == 1) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
							else if (z == 2) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 0 : d));
							else if (z == 5) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
							else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
					}
				}
			}
			if (rand.nextInt(3) == 0) {
				generateGrave(gen, center.add(0, -5, 6), Rotation.NONE, rand, wet);
				placeUrn(gen, center.add(-1, -5, 5), biomeType, loc, rand, false);
				placeUrn(gen, center.add(1, -5, 5), biomeType, loc, rand, false);
				placeWeb(gen, center.add(-1, -4, 5), rand, false);
				placeWeb(gen, center.add(1, -4, 5), rand, false);
			} else {
				generateGrave(gen, center.add(-1, -5, 6), Rotation.NONE, rand, wet);
				generateGrave(gen, center.add(1, -5, 6), Rotation.NONE, rand, wet);
				placeUrn(gen, center.add(0, -5, 5), biomeType, loc, rand, false);
			}
			gen.setBlockState(center.add(-3, -4, 3), getCobble(rand, wet ? 0 : d));
			gen.setBlockState(center.add(3, -4, 3), getCobble(rand, wet ? 0 : d));
			placeUrn(gen, center.add(-2, -4, 3), biomeType, loc, rand, true);
			placeUrn(gen, center.add(2, -4, 3), biomeType, loc, rand, true);
			placeWeb(gen, center.add(-1, -5, 2), rand, false);
			placeWeb(gen, center.add(1, -5, 2), rand, false);
			placeWeb(gen, center.add(0, -3, 4), rand, false);
			placeWeb(gen, center.add(0, -3, 3), rand, false);
		}

		private static void generateCorridor(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			boolean miniRoom = rand.nextBoolean();
			boolean room = rand.nextInt(4) == 0;
			GenUtil gen90 = null;
			if (miniRoom) {
				gen90 = gen.add(center.add(-4, 0, -1), Rotation.CLOCKWISE_90, Mirror.NONE);
				if (room) room = miniRoom = roomCheck(gen90, -4, 4, -7, -1, 2, 7, false);
				if (!miniRoom) miniRoom = roomCheck(gen90, -2, 2, -6, -1, 2, 6, false);
			}
			for (int x = -5; x <= -2; ++x) {
				for (int z = -3; z <= 1; ++z) {
					for (int y = -6; y <= -2; ++y) {
						checkPos = center.add(x, y, z);
						if (x == -5 || y == -2 || z == -3 || z == 1) {
							if (x == -5 || y == -2) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						} else if (y == -6) {
							gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						} else if (y == -3) {
							if (x == -4) gen.setBlockState(checkPos, getStairs(EnumFacing.WEST, true, rand, wet ? 0 : d));
							else if (x == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.EAST, true, rand, wet ? 0 : d));
							else if (z == -2) gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, true, rand, wet ? 0 : d));
							else if (z == 0) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
							else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						} else if (y == -5 && x != -3 && (!miniRoom || x != -4)) {
							if (z == -2) {
								gen.setBlockState(checkPos, getStairs(EnumFacing.NORTH, false, rand, wet ? 0 : d));
								gen.setBlockState(checkPos.down(), getCobble(rand, wet ? 0 : d));
							} else if (z == 0) {
								gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 0 : d));
								gen.setBlockState(checkPos.down(), getCobble(rand, wet ? 0 : d));
							} else {
								gen.setBlockState(checkPos, getSlab(false, rand, wet ? 0 : d));
								gen.setBlockState(checkPos.down(), MistBlocks.FLOATING_MAT.getDefaultState());
								gen.setBlockState(checkPos.down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, rand.nextInt(3) + 4).withProperty(Remains.OLD, true));
								((TileEntityRemains)gen.getTileEntity(checkPos.down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
							}
						} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
					}
				}
			}
			placeWeb(gen, center.add(-3, -4, -3), rand, true);
			placeWeb(gen, center.add(-3, -5, -3), rand, true);
			gen.setBlockState(center.add(-3, -6, -3), MistBlocks.GRAVEL.getDefaultState());
			placeWeb(gen, center.add(-3, -4, 1), rand, true);
			placeWeb(gen, center.add(-3, -5, 1), rand, true);
			gen.setBlockState(center.add(-3, -6, 1), MistBlocks.GRAVEL.getDefaultState());
			placeWeb(gen, center.add(-4, -4, 0), rand, false);
			placeWeb(gen, center.add(-2, -4, 0), rand, false);
			placeWeb(gen, center.add(-4, -4, -2), rand, false);
			placeWeb(gen, center.add(-2, -4, -2), rand, false);
			placeWeb(gen, center.add(-3, -3, -1), rand, false);
			if (miniRoom) {
				if (room) generateRoom(gen90, biome, rand, false, false, wet);
				else generateMiniRoom(gen90, biome, biomeType, loc, rand, false, wet);
				placeWeb(gen, center.add(-5, -4, -1), rand, true);
				placeWeb(gen, center.add(-5, -5, -1), rand, true);
				gen.setBlockState(center.add(-5, -6, -1), MistBlocks.GRAVEL.getDefaultState());
			} else {
				gen.setBlockState(center.add(-6, -4, -1), getCobble(rand, wet ? 0 : d));
				placeUrn(gen, center.add(-5, -4, -1), biomeType, loc, rand, true);
			}
		}

		private static void generateStairs(GenUtil gen, BiomeMist biome, Random rand, boolean wet) {
			BlockPos center = gen.set.center;
			BlockPos checkPos;
			boolean miniRoom = rand.nextBoolean();
			boolean room = rand.nextInt(4) == 0;
			GenUtil gen90 = null;
			if (miniRoom) {
				gen90 = gen.add(center.add(-3, -2, -2), Rotation.CLOCKWISE_90, Mirror.NONE);
				if (room) room = miniRoom = roomCheck(gen90, -4, 4, -7, -1, 2, 7, false);
				if (!miniRoom) miniRoom = roomCheck(gen90, -2, 2, -6, -1, 2, 6, false);
			}
			for (int x = -4; x <= -2; ++x) {
				for (int z = -3; z <= 1; ++z) {
					for (int y = -8; y <= -3; ++y) {
						checkPos = center.add(x, y, z);
						if (x == -4 || x == -2 || y == -3 || z == -3 || z == 1) {
							gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
						} else if (z >= -1) {
							if (y == z - 6) gen.setBlockState(checkPos, getStairs(EnumFacing.SOUTH, false, rand, wet ? 0 : d));
							else if (y < z - 6) gen.setBlockState(checkPos, getCobble(rand, wet ? 0 : d));
							else placeWeb(gen, checkPos, rand, true);
						} else if (y == -8) gen.setBlockState(checkPos, MistBlocks.GRAVEL.getDefaultState());
						else placeWeb(gen, checkPos, rand, true);
					}
				}
			}
			gen.setBlockState(center.add(-3, -5, -3), getStairs(EnumFacing.NORTH, true, rand, wet ? 0 : d));
			gen.setBlockState(center.add(-3, -4, -2), getStairs(EnumFacing.NORTH, true, rand, wet ? 0 : d));
			placeWeb(gen, center.add(-3, -6, -3), rand, true);
			placeWeb(gen, center.add(-3, -7, -3), rand, true);
			gen.setBlockState(center.add(-3, -8, -3), MistBlocks.GRAVEL.getDefaultState());
			placeWeb(gen, center.add(-3, -4, 1), rand, true);
			placeWeb(gen, center.add(-3, -5, 1), rand, true);
			gen.setBlockState(center.add(-3, -6, 1), MistBlocks.GRAVEL.getDefaultState());
			if (miniRoom) {
				if (room) generateRoom(gen90, biome, rand, false, false, false, wet);
				else generateMiniRoom(gen90, biome, biomeType, loc, rand, false, wet);
				placeWeb(gen, center.add(-4, -6, -2), rand, true);
				placeWeb(gen, center.add(-4, -7, -2), rand, true);
				gen.setBlockState(center.add(-4, -8, -2), MistBlocks.GRAVEL.getDefaultState());
			}
		}

		private static void generateGrave(GenUtil gen, BlockPos center, Rotation rotation, Random rand, boolean wet) {
			GenUtil genRot = gen.add(center, rotation, Mirror.NONE);
			center = genRot.set.center;
			genRot.setBlockState(center, getStairs(EnumFacing.SOUTH, false, rand, wet ? 0 : d));
			genRot.setBlockState(center.up(), getStairs(EnumFacing.SOUTH, true, rand, wet ? 0 : d));
			int i = rand.nextInt(32);
			IBlockState state;
			boolean close = false;
			if (i == 0) state = Blocks.AIR.getDefaultState();
			else if (i == 1) state = getStep(EnumFacing.SOUTH, false, rand, wet ? 0 : d);
			else if (i == 2) state = getStep(EnumFacing.EAST, false, rand, wet ? 0 : d);
			else if (i == 3) state = getStep(EnumFacing.WEST, false, rand, wet ? 0 : d);
			else { state = getSlab(false, rand, wet ? 0 : d); close = true; }
			genRot.setBlockState(center.north(), state);
			if (i > 3) {
				i = rand.nextInt(16);
				if (i == 0) state = Blocks.AIR.getDefaultState();
				else if (i == 1) state = getStep(EnumFacing.SOUTH, false, rand, wet ? 0 : d);
				else if (i == 2) state = getStep(EnumFacing.EAST, false, rand, wet ? 0 : d);
				else if (i == 3) state = getStep(EnumFacing.WEST, false, rand, wet ? 0 : d);
				else state = getSlab(false, rand,  wet ? 0 : d);
			} else state = Blocks.AIR.getDefaultState();
			genRot.setBlockState(center.north(2), state);
			if (close && i == 0 && rand.nextInt(4) == 0) {
				if (rand.nextBoolean()) genRot.setBlockState(center.north(2).east(), getStep(EnumFacing.WEST, false, rand, wet ? 0 : d));
				else genRot.setBlockState(center.north(2).west(), getStep(EnumFacing.EAST, false, rand, wet ? 0 : d));
			}
			int size = rand.nextInt(3) + 4;
			genRot.setBlockState(center.north().down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, size).withProperty(Remains.OLD, true));
			genRot.setBlockState(center.north(2).down(), MistBlocks.REMAINS.getDefaultState().withProperty(Remains.LAYERS, size).withProperty(Remains.OLD, true));
			((TileEntityRemains)genRot.getTileEntity(center.north().down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
			((TileEntityRemains)genRot.getTileEntity(center.north(2).down())).setLootTable(LootTables.REMAINS_LOOT, rand.nextLong());
		}
	}
}