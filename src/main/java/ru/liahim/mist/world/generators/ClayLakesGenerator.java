package ru.liahim.mist.world.generators;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistBlockWettable;
import ru.liahim.mist.block.MistClay;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMistUpSavanna;
import net.minecraft.block.BlockBush;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;

public class ClayLakesGenerator implements IWorldGenerator {

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
		if (!world.isRemote && world.provider.getDimension() == Mist.getID()) {
			BlockPos center = world.getHeight(BlockPos.ORIGIN.add(chunkX * 16 + 8, 0, chunkZ * 16 + 8));
			if (world.getBiome(center) == MistBiomes.upSavanna) {
				ArrayList<BlockPos> clayPoses = checkSpace(world, center);
				if (clayPoses != null && !clayPoses.isEmpty() && clayPoses.size() >= 150 && clayPoses.size() < 1000) {
					for (BlockPos pos : clayPoses) {
						double i = ((BiomeMistUpSavanna)MistBiomes.upSavanna).getClayNoise(pos);
						if (i > 0.45F && world.getLight(pos.up()) > 12) {
							if (world.getBlockState(pos.up()).getBlock() instanceof BlockBush) {
								for(int j = 1;;++j) {
									world.setBlockState(pos.up(j), Blocks.AIR.getDefaultState(), Mist.FLAG);
									if (!(world.getBlockState(pos.up(j + 1)).getBlock() instanceof BlockBush)) break;
								}
							}
							world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), Mist.FLAG);
							if (world.getBlockState(pos).getBlock() instanceof MistBlockWettable) {
								world.setBlockState(pos, Blocks.AIR.getDefaultState(), Mist.FLAG);
								if (world.getBlockState(pos.down()).getBlock() instanceof MistBlockWettable || world.getBlockState(pos.down()).getBlock() == MistBlocks.GRAVEL) {
									world.setBlockState(pos.down(), MistBlocks.CLAY.getDefaultState().withProperty(MistClay.TYPE, MistClay.EnumBlockType.CRACKED).withProperty(IWettable.WET, false), Mist.FLAG);
									for (EnumFacing face: EnumFacing.HORIZONTALS) {
										center = pos.up().offset(face);
										if (world.getBlockState(center).isFullBlock() && !(world.getBlockState(center.up()).getBlock() instanceof MistTreeTrunk)) {
											world.setBlockState(center.up(), Blocks.AIR.getDefaultState(), Mist.FLAG);
											world.setBlockState(center, Blocks.AIR.getDefaultState(), Mist.FLAG);
											if (world.getBlockState(center.down()).getBlock() instanceof MistDirt) {
												world.setBlockState(center.down(), MistBlocks.GRASS_C.getDefaultState().withProperty(IWettable.WET, false), Mist.FLAG);
											}
										}
									}
								}
							}
						} else if (i > 0.425F) {
							if (world.getBlockState(pos).getBlock() instanceof MistDirt) {
								world.setBlockState(pos, MistBlocks.GRASS_C.getDefaultState().withProperty(IWettable.WET, false), Mist.FLAG);
							} if (world.getBlockState(pos.down()).getBlock() instanceof MistBlockWettable) {
								world.setBlockState(pos.down(), MistBlocks.CLAY.getDefaultState().withProperty(IWettable.WET, false), Mist.FLAG);
							}
						} else if (world.getBlockState(pos.down()).getBlock() instanceof MistDirt) {
							world.setBlockState(pos.down(), MistBlocks.DIRT_C.getDefaultState().withProperty(IWettable.WET, false), Mist.FLAG);
						}
						boolean checkFish = false;
						for (BlockPos posDown = pos.down(2); posDown.getY() > MistWorld.fogMaxHight_S; posDown = posDown.down()) {
							if (world.getBlockState(posDown).getBlock() instanceof MistBlockWettable) {
								if (!checkFish && random.nextFloat() < ModConfig.generation.desertFishGenRarity) {
									world.setBlockState(posDown, MistBlocks.CLAY.getDefaultState().withProperty(IWettable.WET, false).withProperty(MistClay.TYPE, MistClay.EnumBlockType.CONTAINER), Mist.FLAG);
								} else world.setBlockState(posDown, MistBlocks.CLAY.getDefaultState().withProperty(IWettable.WET, false), Mist.FLAG);
								checkFish = true;
							} else break;
						}
					}
				}
			}
		}
	}

	private ArrayList<BlockPos> checkSpace(World world, BlockPos center) {
		while (center.getY() > MistWorld.seaLevelUp && world.getBlockState(center) != MistBiomes.upSavanna.topBlock) {
			center = center.down();
		}
		if (center.getY() <= MistWorld.seaLevelUp + 5 && world.getBlockState(center) == MistBiomes.upSavanna.topBlock) {
			if (((BiomeMistUpSavanna)MistBiomes.upSavanna).getClayNoise(center) > 0.4F) {
				IBlockState state;
				IBlockState upState;
				BlockPos facePos;
				LinkedList<BlockPos> poses = new LinkedList<BlockPos>();
				ArrayList<BlockPos> clayPoses = new ArrayList<BlockPos>();
				poses.addLast(center);
				clayPoses.add(center);
				for (BlockPos pos = center; !poses.isEmpty();) {
					for (EnumFacing face: EnumFacing.HORIZONTALS) {
						facePos = pos.offset(face);
						if (!poses.contains(facePos) && !clayPoses.contains(facePos)) {
							state = world.getBlockState(facePos);
							upState = world.getBlockState(facePos.up());
							if (!state.isFullBlock() || world.getBlockState(facePos.up(2)).isFullBlock()) return null;
							if ((state == MistBiomes.upSavanna.topBlock || state == MistBiomes.upDesert.topBlock ||
									upState == MistBiomes.upSavanna.topBlock || upState == MistBiomes.upDesert.topBlock) &&
									((BiomeMistUpSavanna)MistBiomes.upSavanna).getClayNoise(facePos) > 0.4F) {
								poses.addLast(facePos);
								clayPoses.add(facePos);
							}
						}
					}
					poses.removeFirstOccurrence(pos);
					if (!poses.isEmpty()) pos = poses.getFirst();
				}
				return clayPoses;
			}
		}
		return null;
	}
}