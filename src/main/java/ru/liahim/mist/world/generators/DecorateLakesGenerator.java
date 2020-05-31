package ru.liahim.mist.world.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.upperplant.MistNightberry;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.biome.BiomeMistUp;

public class DecorateLakesGenerator implements IWorldGenerator {

	private final int H = MistWorld.fogMaxHight_S + 4;

	@Override
	public void generate(Random rand, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
		IChunkProvider chunkProvider) {
		if (world.provider.getDimension() == Mist.getID())
			findLakes(world, chunkX * 16, chunkZ * 16, rand);
	}

	public void findLakes(World world, int chunkX, int chunkZ, Random rand) {
		if (!world.isRemote) {
			BlockPos pos;
			List<BlockPos> emptyPoses = new ArrayList<BlockPos>();
			for (int x = 0; x < 16; ++x) {
				for (int z = 0; z < 16; ++z) {
					pos = new BlockPos(chunkX + x, MistWorld.seaLevelUp, chunkZ + z);
					if (world.getBlockState(pos).getBlock() == Blocks.GLASS || world.getBlockState(pos).getBlock() == Blocks.STAINED_GLASS) {
						emptyPoses = setWater(world, pos, rand, emptyPoses);
					}
					if (!emptyPoses.isEmpty()) removeWater(world, rand, emptyPoses);
				}
			}
		}
	}

	private List<BlockPos> setWater(World world, BlockPos pos, Random rand, List<BlockPos> emptyPoses) {
		BlockPos facePos;
		boolean check = true;
		List<BlockPos> poses = new ArrayList<BlockPos>();
		for (;;) {
			if (check) {
				if (world.getBlockState(pos).getBlock() == Blocks.GLASS) {
					if (world.getBiome(pos).getTemperature(pos) < 0.25F) {
						world.setBlockState(pos, Blocks.ICE.getDefaultState(), Mist.FLAG);
						if (world.getBlockState(pos.up()).getBlock() == Blocks.SNOW_LAYER)
							world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), Mist.FLAG);
					} else world.setBlockState(pos, MistWorld.waterBlockUpper, Mist.FLAG);
				} else if (world.getBlockState(pos).getBlock() == Blocks.STAINED_GLASS) {
					world.setBlockState(pos, MistBlocks.FLOATING_MAT.getDefaultState(), Mist.FLAG);
					placePlantOnFloatingMat(world, pos.up(), rand);
				}
				poses.add(pos);
			}
			check = false;
			for (EnumFacing face: EnumFacing.HORIZONTALS) {
				facePos = pos.offset(face);
				if (world.getBlockState(facePos).getBlock() == Blocks.GLASS || world.getBlockState(facePos).getBlock() == Blocks.STAINED_GLASS) {
					pos = facePos;
					check = true;
					break;
				} else if (isNotFull(world, facePos) && !emptyPoses.contains(facePos)) emptyPoses.add(facePos);
			}
			if (!check) {
				poses.remove(poses.size() - 1);
				if (poses.isEmpty()) break;
				else pos = poses.get(poses.size() - 1);
			}
		}
		return emptyPoses;
	}

	private void removeWater(World world, Random rand, List<BlockPos> emptyPoses) {
		BlockPos pos;
		BlockPos facePos;
		BlockPos hightPos;
		boolean check;
		List<BlockPos> poses = new ArrayList<BlockPos>();
		for (;;) {
			for (hightPos = emptyPoses.get(emptyPoses.size() - 1); hightPos.getY() > this.H && isNotFull(world, hightPos); hightPos = hightPos.down()) {
				pos = hightPos;
				check = true;
				for (;;) {
					if (check) {
						if (!world.isAirBlock(pos.up()) && isNotFull(world, pos.up())) {
							if (world.getBlockState(pos.up(2)).getBlock() instanceof BlockDoublePlant) {
								world.setBlockState(pos.up(2), Blocks.AIR.getDefaultState(), 2);
							}
							world.setBlockState(pos.up(), Blocks.AIR.getDefaultState(), Mist.FLAG);
						}
						if ((isWater(world, pos) || world.getBlockState(pos).getBlock() == MistBlocks.FLOATING_MAT)) {
							world.setBlockState(pos, Blocks.AIR.getDefaultState(), Mist.FLAG);
						}
						setTopBlock(world, pos.down(), rand);
						poses.add(pos);
					}
					check = false;
					for (EnumFacing face : EnumFacing.HORIZONTALS) {
						facePos = pos.offset(face);
						if (isWater(world, facePos) || world.getBlockState(facePos).getBlock() == MistBlocks.FLOATING_MAT) {
							pos = facePos;
							check = true;
							break;
						}
					}
					if (!check) {
						poses.remove(poses.size() - 1);
						if (poses.isEmpty()) break;
						else pos = poses.get(poses.size() - 1);
					}
				}
			}
			emptyPoses.remove(emptyPoses.size() - 1);
			if (emptyPoses.isEmpty()) break;
		}
	}

	private void setTopBlock(World world, BlockPos pos, Random rand) {
		Biome biome = world.getBiome(pos);
		if (isWater(world, pos)) {
			if (biome.getTemperature(pos) < 0.25F) world.setBlockState(pos, Blocks.ICE.getDefaultState(), Mist.FLAG);
			else {
				boolean swamp = biome == MistBiomes.upSwamp;
				if (swamp || biome == MistBiomes.upMarsh) {
					if (!isWater(world, pos.down())) {
						if (world.getBlockState(pos.down()).getBlock() != MistBlocks.CLAY) {
							world.setBlockState(pos, MistBlocks.FLOATING_MAT.getDefaultState(), Mist.FLAG);
							if (swamp) world.setBlockState(pos.down(), MistBlocks.PEAT.getDefaultState(), Mist.FLAG);
							placePlantOnFloatingMat(world, pos.up(), rand);
						}
					} else if (!isWater(world, pos.down(2))) {
						if (world.getBlockState(pos.down(2)).getBlock() != MistBlocks.CLAY && ((BiomeMist)biome).getNoises(pos.getX(), pos.getZ()).get(3) > 0) {
							world.setBlockState(pos, MistBlocks.FLOATING_MAT.getDefaultState(), Mist.FLAG);
							if (swamp) world.setBlockState(pos.down(2), MistBlocks.PEAT.getDefaultState(), Mist.FLAG);
							placePlantOnFloatingMat(world, pos.up(), rand);
						}
					}
				}
			}
		} else if (!isNotFull(world, pos) && biome instanceof BiomeMistUp && world.getBlockState(pos).getBlock() != MistBlocks.FLOATING_MAT) {
			if (world.getBlockState(pos) != MistWorld.stoneBlockUpper)
				world.setBlockState(pos, ((BiomeMist)biome).getTopBlock(rand, ((BiomeMist)biome).getNoises(pos.getX(), pos.getZ())), Mist.FLAG);
			((BiomeMist)biome).placePlant(world, pos.up(), rand);
		}
	}

	private boolean isNotFull(World world, BlockPos pos) {
		return !world.getBlockState(pos).isFullCube() && !(world.getBlockState(pos).getBlock() instanceof BlockLiquid);
	}

	private boolean isWater(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() instanceof BlockLiquid || world.getBlockState(pos).getBlock() == Blocks.ICE;
	}

	private final IBlockState grass = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS);
	private final IBlockState fern = Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN);
	private final IBlockState grassDown = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS);
	private final IBlockState grassUp = Blocks.DOUBLE_PLANT.getDefaultState().withProperty(BlockDoublePlant.VARIANT, BlockDoublePlant.EnumPlantType.GRASS).withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER);
	private final IBlockState nightberry = MistBlocks.NIGHTBERRY.getDefaultState().withProperty(MistNightberry.AGE, MistNightberry.EnumAge.POTENTIAL);

	/** Use only in Swamp decoration.*/
	private boolean placePlantOnFloatingMat(World world, BlockPos pos, Random rand) {
		if (world.isAirBlock(pos)) {
			int i = rand.nextInt(16);
			if (i < 4) {
				return world.setBlockState(pos, grass, Mist.FLAG);
			} else if (i == 4) {
				return world.setBlockState(pos, fern, Mist.FLAG);
			} else if (i == 5 && world.isAirBlock(pos.up())) {
				return world.setBlockState(pos, grassDown, 2) && world.setBlockState(pos.up(), grassUp, 2);
			} else if (rand.nextInt(32) == 0 && isWater(world, pos.down(2))) {
				return world.setBlockState(pos, nightberry, 2);
			}
		}
		return false;
	}
}