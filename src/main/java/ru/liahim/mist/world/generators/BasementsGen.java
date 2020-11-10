package ru.liahim.mist.world.generators;

import java.util.Random;

import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.tileentity.TileEntityMistChest;
import ru.liahim.mist.tileentity.TileEntityMistFurnace;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLootType;
import ru.liahim.mist.util.GenUtil;
import ru.liahim.mist.util.GenUtil.GenSet;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.biome.BiomeMistBorder;
import ru.liahim.mist.world.biome.BiomeMistUpDesert;
import ru.liahim.mist.world.biome.BiomeMistUpMarsh;
import ru.liahim.mist.world.biome.MistBiomeDecorator;

public class BasementsGen extends TombGenBase {

	public static final NoiseGeneratorPerlin BASE_NOISE = new NoiseGeneratorPerlin(new Random(54321L), 1);

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		float f = (float) ModConfig.generation.basementGenerationChance;
		double d = BasementsGen.BASE_NOISE.getValue(pos.getX() * 0.01D * f, pos.getZ() * 0.01D * f);
		if (d <= 1.0D - 0.65D * f) return false;
		BlockPos center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
		if (world.getBlockState(center.down()).getMaterial().isReplaceable()) center = center.down();
		if (center.getY() <= MistWorld.seaLevelUp) return false;
		Biome biome = world.getBiome(center);
		if (!(biome instanceof BiomeMist) || !((BiomeMist)biome).isUpBiome() || biome instanceof BiomeMistBorder || biome instanceof BiomeMistUpMarsh || biome instanceof BiomeMistUpDesert ||
				!(world.getBlockState(center.down()).getBlock() instanceof MistSoil)) return false;

		int l = rand.nextInt(3) + 5;
		int b = MathHelper.clamp(l - rand.nextInt(4), 5, 7);
		int h = ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Cold ? rand.nextInt(2) : 0;
		int deep = -3 - rand.nextInt(2);
		boolean corner = rand.nextBoolean();
		boolean columnar = false;

		EnumBiomeType type = ((BiomeMist)biome).getBiomeType();
		Type[] bType = new Type[] { Type.COBBLE };
		if (type == EnumBiomeType.Jungle) {
			l = b;
			corner = false;
			columnar = rand.nextInt(4) == 0;
		} else if (type == EnumBiomeType.Desert) {
			corner = false;
			columnar = rand.nextInt(5) > 1;
		} else {
			columnar = rand.nextInt(4) == 0;
			if (type == EnumBiomeType.Cold) {
				if (rand.nextBoolean()) bType = new Type[] { Type.BRICK };
				else if (rand.nextInt(4) > 0) bType = new Type[] { Type.BRICK, Type.COBBLE, Type.COBBLE };
			} else if (type == EnumBiomeType.Forest && rand.nextBoolean()) bType = new Type[] { Type.BRICK, Type.COBBLE, Type.COBBLE };
		}

		if (columnar) {
			boolean big = rand.nextBoolean();
			if (l % 2 == 1) if (big) ++l; else --l;
			if (b % 2 == 1) if (big) ++b; else --b;
		}

		for(int i = 0; i < 5; ++ i) {
			GenUtil gen = new GenUtil(world, new GenSet(center.add(-b/2, 0, -l/2), Rotation.values()[rand.nextInt(4)], Mirror.values()[rand.nextInt(2)]));
			if (roomCheck(gen, 0, b, deep, -1, 0, l, true)) {
				if (columnar) generateColumnar(gen, (BiomeMist)biome, 0, b, 0, l, rand, 1, (type == EnumBiomeType.Forest || type == EnumBiomeType.Cold) && rand.nextInt(3) > 0 ? Type.BRICK : Type.COBBLE);
				else {
					generateRoom(gen, (BiomeMist)biome, 0, b, deep, h, 0, l, rand, 1, false, bType);
					if (corner && roomCheck(gen, -3, -1, deep, -1, 0, 4, true)) generateRoom(gen, (BiomeMist)biome, -3, 0, deep, h, 0, 4, rand, 1, true, bType);
					BiomeMist.looseRockGen.generate(world, rand, gen.set.center);
					return true;
				}
			} else if (rand.nextInt(8) == 0 && ((WellGen)MistBiomeDecorator.wellGen).generate(world, rand, pos, true)) return true;
			else center = world.getHeight(pos.add(rand.nextInt(16) + 8, 0, rand.nextInt(16) + 8));
		}
		return false;
	}

	private void generateRoom(GenUtil gen, BiomeMist biome, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, Random rand, int wet, boolean neighbor, Type... type) {
		BlockPos center = gen.set.center;
		BlockPos checkPos;
		EnumBiomeType biomeType = biome.getBiomeType();
		boolean snow = biomeType == EnumBiomeType.Cold;
		IBlockState floor, top, second, fill;
		int chance = (maxX - minX + maxZ - minZ) * 2;
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				floor = MistBlocks.GRAVEL.getDefaultState();
				top = biome.topBlock;
				second = biome.secondTopBlock;
				fill = biome.fillerBlock;
				if (biomeType == EnumBiomeType.Desert) { wet = -1; floor = sand; }
				else if (biomeType == EnumBiomeType.Jungle) floor = redSandWet;
				if (((BiomeMist) gen.world.getBiome(gen.getPos(center.add(x, 0, z)))).getBiomeType() == EnumBiomeType.Border) {
					fill = MistBlocks.STONE.getDefaultState();
					floor = fill;
					top = MistBlocks.GRAVEL.getDefaultState();
					second = top;
				}
				for (int y = minY; y <= maxY; ++y) {
					checkPos = center.add(x, y, z);
					if (x == minX || (x == maxX && !neighbor) || z == minZ || z == maxZ) {
						if (y < -1) {
							if (y == minY + 1 && x == maxX && z == (maxZ + minZ) / 2 && rand.nextBoolean()) {
								gen.setBlockState(checkPos, MistBlocks.FURNACE.getDefaultState().withProperty(BlockHorizontal.FACING, EnumFacing.WEST));
								TileEntityMistFurnace.initializeLoot(gen.getTileEntity(checkPos), rand);
							} else gen.setBlockState(checkPos, getBlock(type[rand.nextInt(type.length)], rand, -1));
						} else if (y == -1) {
							if (rand.nextInt(3) != 0) gen.setBlockState(checkPos, getBlock(type[rand.nextInt(type.length)], rand, wet));
							else {
								if (rand.nextInt(3) > 0 || ((x == minX || x == maxX) && (z == minZ || z == maxZ ))) {
									gen.setBlockState(checkPos, getBlock(type[rand.nextInt(type.length)], rand, wet));
									if (snow && gen.world.canSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
								} else {
									if (x == minX) gen.setBlockState(checkPos, getStairs(type[rand.nextInt(type.length)], EnumFacing.WEST, false, rand, wet));
									else if (x == maxX) gen.setBlockState(checkPos, getStairs(type[rand.nextInt(type.length)], EnumFacing.EAST, false, rand, wet));
									else if (z == minZ) gen.setBlockState(checkPos, getStairs(type[rand.nextInt(type.length)], EnumFacing.NORTH, false, rand, wet));
									else if (z == maxZ) gen.setBlockState(checkPos, getStairs(type[rand.nextInt(type.length)], EnumFacing.SOUTH, false, rand, wet));
									gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState());
								}
								break;
							}
						} else {
							if (y != maxY && rand.nextInt(3) == 0) gen.setBlockState(checkPos, getBlock(type[rand.nextInt(type.length)], rand, wet));
							else {
								int i = rand.nextInt(7);
								if (i < 2) {
									gen.setBlockState(checkPos, getBlock(type[rand.nextInt(type.length)], rand, wet));
									if (snow && gen.world.canSeeSky(gen.getPos(checkPos.up()))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
								} else if (i < 5) gen.setBlockState(checkPos, getSlab(type[rand.nextInt(type.length)], false, rand, wet));
								else if (i == 5) gen.setBlockState(checkPos, getStairs(type[rand.nextInt(type.length)], EnumFacing.HORIZONTALS[rand.nextInt(4)], false, rand, wet));
								else if (i == 6) gen.setBlockState(checkPos, getStep(type[rand.nextInt(type.length)], EnumFacing.HORIZONTALS[rand.nextInt(4)], false, rand, wet));
								if (i >= 2) gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState());
								break;
							}
						}
					} else if (y == minY) {
						gen.setBlockState(checkPos, rand.nextBoolean() ? floor : y == -3 ? second : fill);
					} else if (y == -2) {
						gen.setBlockState(checkPos, top);
						if (snow) {
							if (gen.world.canSeeSky(gen.getPos(center.add(x, maxY + 1, z)))) gen.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState(), 2);
							else gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState(), 2);
						}
					} else if (y == -3) {
						if (((x == minX + 1 && neighbor) || (x == maxX - 1 && !neighbor) || z == minZ + 1 || z == maxZ - 1) && rand.nextInt(chance) == 0) {
							int i = rand.nextInt(5);
							if (i == 0) gen.setBlockState(checkPos, Blocks.ANVIL.getDefaultState().withProperty(BlockAnvil.DAMAGE, 2).withProperty(BlockAnvil.FACING, (x == minX + 1 || x == maxX - 1) ? EnumFacing.NORTH : EnumFacing.EAST));
							else if (i == 1) gen.setBlockState(checkPos, Blocks.CAULDRON.getDefaultState()); 
							else {
								if (rand.nextInt(5) > 1) placeUrn(gen, checkPos, biomeType, rand);
								else placeChest(gen, checkPos, x == minX + 1 ? EnumFacing.EAST : x == maxX - 1 ? EnumFacing.WEST : z == minZ + 1 ? EnumFacing.SOUTH : EnumFacing.NORTH, biomeType, rand);
							}
						} else gen.setBlockState(checkPos, second);
					} else if (!snow || y >= 0) {
						gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
						if (gen.getBlockState(checkPos.up()).getBlock() == Blocks.SNOW_LAYER) gen.setBlockState(checkPos.up(), Blocks.AIR.getDefaultState());
					}
				}
			}
		}
	}

	private void generateColumnar(GenUtil gen, BiomeMist biome, int minX, int maxX, int minZ, int maxZ, Random rand, int wet, Type type) {
		BlockPos center = gen.set.center;
		BlockPos checkPos;
		if (biome.getBiomeType() == EnumBiomeType.Desert) wet = -1;
		for (int x = minX; x <= maxX; ++x) {
			for (int z = minZ; z <= maxZ; ++z) {
				if (x == minX || x == maxX || z == minZ || z == maxZ) {
					if ((x - minX) % 2 == 0 && (z - minZ) % 2 == 0) {
						checkPos = center.add(x, -1, z);
						gen.setBlockState(checkPos.down(), getBlock(type, rand, -1));
						AltarsGen.generateColumn(gen.world, gen.getPos(checkPos), rand, 1, wet, type);
					}
				} else if (x == minX + 1 || x == maxX - 1 || z == minZ + 1 || z == maxZ - 1) {
					if (rand.nextInt((maxX - minX + maxZ - minZ) * 2) == 0) {
						checkPos = center.add(x, -2, z);
						if (gen.getBlockState(checkPos).getBlock() instanceof IWettable) {
							if (rand.nextInt(5) > 1) placeUrn(gen, checkPos, biome.getBiomeType(), rand);
							else placeChest(gen, checkPos, x == minX + 1 ? EnumFacing.EAST : x == maxX - 1 ? EnumFacing.WEST : z == minZ + 1 ? EnumFacing.SOUTH : EnumFacing.NORTH, biome.getBiomeType(), rand);
						}
					}
				}
			}
		}
	}

	private void placeChest(GenUtil gen, BlockPos pos, EnumFacing face, EnumBiomeType biomeType, Random rand) {
		gen.setBlockState(pos, MistBlocks.NIOBIUM_CHEST.getDefaultState().withProperty(BlockChest.FACING, face));
		TileEntityMistChest.initializeType(gen.getTileEntity(pos), biomeType, rand);
	}

	private void placeUrn(GenUtil gen, BlockPos pos, EnumBiomeType biomeType, Random rand) {
		gen.setBlockState(pos, MistBlocks.URN.getDefaultState());
		UrnLootType.initializeType(gen.getTileEntity(pos), biomeType, UrnLocation.BASEMENTS, rand);
	}
}