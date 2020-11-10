package ru.liahim.mist.world.generators;

import java.util.Random;

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
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLocation;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnLootType;
import ru.liahim.mist.util.GenUtil;
import ru.liahim.mist.util.GenUtil.GenSet;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.generators.TombGen.ForestTomb;
import ru.liahim.mist.world.generators.TombGen.SwampTomb;

public class CliffTombGen extends TombGenBase {

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) {
		BlockPos center = pos.add(16, MistWorld.fogMaxHight_S + 8 - pos.getY(), 16);
		if (!world.isAirBlock(center) || !world.canSeeSky(center)) return false;
		if (rand.nextFloat() >= ModConfig.generation.cliffTombGenerationChance) return false;
		Biome biome = world.getBiome(center);
		if (!(biome instanceof BiomeMist) || !((BiomeMist)biome).isUpBiome()) return false;
		return generateTomb(world, center, rand);
	}

	private boolean generateTomb(World world, BlockPos pos, Random rand) {
		BlockPos findCenter = null;
		EnumFacing findFace = null;
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			for (int i = 1; i < 8; ++i) {
				if (world.getBlockState(pos.offset(face, i)).isFullCube()) {
					findCenter = pos.offset(face, i);
					findFace = face;
					break;
				}
			}
		}
		if (findCenter != null) {
			Biome biome = world.getBiome(findCenter);
			EnumBiomeType type = ((BiomeMist)biome).getBiomeType();
			if (type == EnumBiomeType.Desert || type == EnumBiomeType.Jungle || biome == MistBiomes.borderUpDesert || biome == MistBiomes.borderUpJungle) {
				int wet = type == EnumBiomeType.Desert || biome == MistBiomes.borderUpDesert ? -1 : 0;
				BlockPos center;
				boolean check = false;
				EnumBiomeType biomeType = type == EnumBiomeType.Border ? biome == MistBiomes.borderUpDesert ? EnumBiomeType.Desert : EnumBiomeType.Jungle : type;
				for (int i = 1; i < 64; ++i) {
					center = findCenter.add(rand.nextInt(8) - 4, rand.nextInt(4) - 2, rand.nextInt(8) - 4);
					if (world.isAirBlock(center) && world.canSeeSky(center)) {
						findFace = null;
						for (EnumFacing face : EnumFacing.HORIZONTALS) {
							if (world.getBlockState(center.offset(face)).getBlock() == MistWorld.stoneBlockUpper.getBlock()) {
								findFace = face;
								break;
							}
						}
						if (findFace != null && world.getBlockState(center.offset(findFace).down(2)).isFullCube() &&
								world.getBlockState(center.offset(findFace, 2).up()).isFullCube()) {
							center = center.offset(findFace, 2);
							world.setBlockState(center.down(), getBlock(Type.COBBLE, rand, wet));
							if (rand.nextInt(3) != 0) {
								world.setBlockState(center, MistBlocks.URN.getDefaultState());
								UrnLootType.initializeType(world.getTileEntity(center), biomeType, UrnLocation.CLIFF, rand);
							} else world.setBlockToAir(center);
							findFace = findFace.getOpposite();
							for (int k = 1; k < 4 && world.getBlockState(center.offset(findFace, k)).isFullCube(); ++k)
								world.setBlockToAir(center.offset(findFace, k));
							for (int k = 1; k < 4 && world.getBlockState(center.offset(findFace, k).down()).isFullCube(); ++k)
								world.setBlockToAir(center.offset(findFace, k).down());
							check = true;
						}
					}
				}
				return check;
			} else if (type == EnumBiomeType.Forest || biome == MistBiomes.borderUpPlains) {
				BlockPos center = findCenter.offset(findFace, 4);
				GenUtil gen = new GenUtil(world, new GenSet(center, findFace == EnumFacing.SOUTH ? Rotation.NONE : findFace == EnumFacing.NORTH ? Rotation.CLOCKWISE_180 : findFace == EnumFacing.WEST ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90, Mirror.NONE));
				if (roomCheck(gen, -4, 4, 0, 4, 0, 0, false) && ForestTomb.generateMiniRoom(gen, (BiomeMist)biome, EnumBiomeType.Forest, UrnLocation.CLIFF, rand, true, true)) {
					BlockPos checkPos;
					for (int x = -1; x <= 1; ++x) {
						for (int z = 1; z >= -5; --z) {
							for (int y = 1; y <= 3; ++y) {
								checkPos = center.add(x, y, z);
								if (x == -1 || x == 1 || y == 3) {
									if ((y != 3 || x == 0) && gen.getBlockState(checkPos).isFullCube()) gen.setBlockState(checkPos, getBlock(Type.BRICK, rand, 0));
								} else if (z == 1 && gen.getBlockState(checkPos).isFullCube()) gen.setBlockState(checkPos, getBlock(Type.COBBLE, rand, 0));
								else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							}
						}
					}
					return true;
				}
			} else if (type == EnumBiomeType.Swamp || biome == MistBiomes.borderUpSwamp) {
				BlockPos center = findCenter.offset(findFace, 2).up(4);
				GenUtil gen = new GenUtil(world, new GenSet(center, findFace == EnumFacing.SOUTH ? Rotation.NONE : findFace == EnumFacing.NORTH ? Rotation.CLOCKWISE_180 : findFace == EnumFacing.WEST ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90, Mirror.NONE));
				if (roomCheck(gen, -3, 3, -7, -1, 0, 7, false)) {
					SwampTomb.generateMiniRoom(gen, (BiomeMist)biome, EnumBiomeType.Swamp, UrnLocation.CLIFF, rand, true, true);
					center = center.down(6);
					BlockPos checkPos;
					for (int x = -1; x <= 1; ++x) {
						for (int z = 0; z >= -5; --z) {
							for (int y = 1; y <= 3; ++y) {
								checkPos = center.add(x, y, z);
								if (x == -1 || x == 1 || y == 3) {
									if ((y != 3 || x == 0) && gen.getBlockState(checkPos).isFullCube()) gen.setBlockState(checkPos, getBlock(Type.COBBLE, rand, 0));
								} else gen.setBlockState(checkPos, Blocks.AIR.getDefaultState());
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}
}