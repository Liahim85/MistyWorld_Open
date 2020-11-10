package ru.liahim.mist.world.generators;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLootType;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.biome.BiomeMistBorder;
import ru.liahim.mist.world.biome.BiomeMistUpDesert;
import ru.liahim.mist.world.generators.TombGenBase.Type;

public class WellGen extends WorldGenerator {

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		return generate(world, rand, pos, false);
	}

	public boolean generate(World world, Random rand, BlockPos pos, boolean manual) {
		if (!manual && rand.nextFloat() >= ModConfig.generation.wellsGenerationChance) return false;
		int rx = rand.nextInt(16) + 8;
		int rz = rand.nextInt(16) + 8;
		BlockPos center = world.getHeight(pos.add(rx, 0, rz));
		if (world.getBlockState(center.down()).getMaterial().isReplaceable()) center = center.down();
		if (center.getY() <= MistWorld.fogMaxHight_S || center.getY() > MistWorld.seaLevelUp + 5) return false;
		Biome biome = world.getBiome(center);
		if (!(biome instanceof BiomeMist) || biome instanceof BiomeMistBorder || biome instanceof BiomeMistUpDesert ||
				!((BiomeMist)biome).isUpBiome() || !(world.getBlockState(center.down()).getBlock() instanceof MistSoil)) return false;
		boolean check = true;
		lab:
		for (int x = -1; x < 2; ++x) {
			for (int z = -1; z < 2; ++z) {
				if (!world.getBlockState(center.add(x, 0, z)).getMaterial().isReplaceable() || !world.isBlockNormalCube(center.add(x, -1, z), false)) {
					check = false;
					break lab;
				}
			}
		}
		if (check) {
			boolean desert = ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Desert;
			boolean jungle = ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Jungle;
			boolean cold = ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Cold;
			boolean forest = ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Forest;
			Type[] bType = new Type[] { Type.COBBLE };
			if (cold) {
				if (rand.nextInt(4) == 0) bType = new Type[] { Type.BRICK, Type.BRICK, Type.COBBLE };
				else bType = new Type[] { Type.BRICK };
			} else if (jungle) bType = new Type[] { Type.BRICK };
			else if (forest && rand.nextInt(4) > 0) {
				if (rand.nextBoolean()) bType = new Type[] { Type.BRICK, Type.COBBLE, Type.COBBLE };
				else bType = new Type[] { Type.BRICK, Type.BRICK, Type.COBBLE };
			}
			BlockPos wall;
			int i = 1;
			int type = rand.nextInt(4);
			int deep = rand.nextInt(3) + 2;
			boolean water = rand.nextInt(3) > 0;
			for (int x = -1; x < 2; ++x) {
				for (int z = -1; z < 2; ++z) {
					wall = center.add(x, 0, z);
					if (x != 0 || z != 0) {
						if (jungle) {
							if (type > 1) world.setBlockState(wall, TombGenBase.getSlab(bType[rand.nextInt(bType.length)], false, rand, 1), Mist.FLAG);
							else world.setBlockState(wall, TombGenBase.getStairs(bType[rand.nextInt(bType.length)], x < 0 ? EnumFacing.EAST : x > 0 ? EnumFacing.WEST : z < 0 ? EnumFacing.SOUTH : EnumFacing.NORTH, false, rand, 1), Mist.FLAG);
						} else if (desert) {
							if (type > 1) world.setBlockState(wall, TombGenBase.getStairs(bType[rand.nextInt(bType.length)], x < 0 ? EnumFacing.WEST : x > 0 ? EnumFacing.EAST : z < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH, false, rand, -1), Mist.FLAG);
							else world.setBlockState(wall, TombGenBase.getStep(bType[rand.nextInt(bType.length)], x < 0 ? EnumFacing.WEST : x > 0 ? EnumFacing.EAST : z < 0 ? EnumFacing.NORTH : EnumFacing.SOUTH, false, rand, -1), Mist.FLAG);
						} else if (cold && type != 0) {
							world.setBlockState(wall, TombGenBase.getBlock(bType[rand.nextInt(bType.length)], rand, 1), Mist.FLAG);
							if (world.canSeeSky(wall.up())) world.setBlockState(wall.up(), Blocks.SNOW_LAYER.getDefaultState());
						} else {
							i = rand.nextInt(10);
							if (i == 0) world.setBlockState(wall, TombGenBase.getStairs(bType[rand.nextInt(bType.length)], EnumFacing.HORIZONTALS[rand.nextInt(4)], false, rand, 1), Mist.FLAG);
							else if (i == 1) world.setBlockState(wall, TombGenBase.getSlab(bType[rand.nextInt(bType.length)], false, rand, 1), Mist.FLAG);
							else world.setBlockState(wall, TombGenBase.getBlock(bType[rand.nextInt(bType.length)], rand, 1), Mist.FLAG);
						}
					}
					for (i = 0, wall = wall.down(); wall.getY() > MistWorld.fogMaxHight_S && (wall.getY() > MistWorld.seaLevelUp - 3 || world.getBlockState(wall).getBlock() != MistBlocks.STONE); ++i, wall = wall.down()) {
						if (x != 0 || z != 0) {
							world.setBlockState(wall, TombGenBase.getBlock(bType[rand.nextInt(bType.length)], rand, desert ? -1 : 1), Mist.FLAG);
						} else if (water) {
							if (wall.getY() <= MistWorld.seaLevelUp) world.setBlockState(wall, Blocks.WATER.getDefaultState(), Mist.FLAG);
							else world.setBlockToAir(wall);
						} else if (i >= deep) {
							if (i == deep) {
								world.setBlockState(wall, biome.topBlock, Mist.FLAG);
							} else {
								world.setBlockState(wall, biome.fillerBlock, Mist.FLAG);
							}
						} else world.setBlockToAir(wall);
					}
				}
			}
			world.setBlockState(center.down(i), MistBlocks.GRAVEL.getDefaultState(), Mist.FLAG);
			if (rand.nextInt(3) == 0) {
				wall = center.down(i).offset(EnumFacing.HORIZONTALS[rand.nextInt(4)]);
				world.setBlockState(wall, MistBlocks.URN.getDefaultState(), Mist.FLAG);
				TileEntity te = world.getTileEntity(wall);
				UrnLootType.initializeType(te, ((BiomeMist)biome).getBiomeType(), UrnLocation.WELLS, rand);
			}
			BiomeMist.looseRockGen.generate(world, rand, center);
			return true;
		}
		return false;
	}
}