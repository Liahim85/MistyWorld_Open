package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.entity.EntityMossling;
import ru.liahim.mist.entity.EntityGalaga;
import ru.liahim.mist.entity.EntitySwampCrab;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistUpSwampBase extends BiomeMistUp {

	protected final int mistPoplarPerChunk;
	protected final boolean genWillowTree;

	public BiomeMistUpSwampBase(BiomeProperties properties, int mistPoplarPerChunk, boolean genWillowTree) {
		super(properties);
		this.topBlock = MistBlocks.GRASS_C.getDefaultState().withProperty(MistDirt.HUMUS, 2);
		this.secondTopBlock = MistBlocks.DIRT_C.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		this.fillerBlock = MistBlocks.DIRT_C.getDefaultState();
		this.mistPoplarPerChunk = mistPoplarPerChunk;
		this.genWillowTree = genWillowTree;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityMossling.class, 15, 2, 4));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityGalaga.class, 5, 1, 1));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySwampCrab.class, 20, 4, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getGrassColorAtPos(BlockPos pos) {
		double d0 = GRASS_COLOR_NOISE.getValue(pos.getX() * 0.1D, pos.getZ() * 0.1D);
		return d0 < -0.1D ? 0x88bc67 : 0x7fb45e;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getFoliageColorAtPos(BlockPos pos) {
		return 0x6e973d;// 0x5d802a
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Swamp;
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
		if (mistPoplarPerChunk > 0 && rand.nextFloat() < 0.04F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			if (rand.nextBoolean()) ((MistTreeTrunk) MistBlocks.ASPEN_TRUNK).generateTrunk(world, randPos, rand);
			else ((MistTreeTrunk) MistBlocks.POPLAR_TRUNK).generateTrunk(world, randPos, rand);
		}
		if (this.genWillowTree && rand.nextFloat() < 0.01F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			((MistTreeTrunk) MistBlocks.WILLOW_TRUNK).generateTrunk(world, randPos, rand);
		}
		super.decorate(world, rand, pos);
		int j = this.mistPoplarPerChunk;
		if (rand.nextFloat() < this.getMistBiomeDecorator().extraTreeChance)
			++j;
		for (int i = 0; i < j; ++i) {
			if (rand.nextInt(3) > 0) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTreeTrunk) MistBlocks.ASPEN_TRUNK).generateTree(world, randPos, rand);
			} else {
				int k = rand.nextInt(rand.nextInt(3) + 1) + 1;
				for (int i1 = 0; i1 < k; ++i1) {
					int rx1 = rand.nextInt(16) + 8;
					int rz1 = rand.nextInt(16) + 8;
					BlockPos randPos = world.getHeight(pos.add(rx1, 0, rz1));
					((MistTreeTrunk) MistBlocks.POPLAR_TRUNK).generateTree(world, randPos, rand);
				}
			}
		}
		if (this.genWillowTree && rand.nextFloat() < 0.7F) {
			int k = rand.nextInt(rand.nextInt(3) + 1) + 1;
			for (int i = 0; i < k; ++i) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTreeTrunk) MistBlocks.WILLOW_TRUNK).generateTree(world, randPos, rand);
			}
		}
		if (this.genWillowTree) {
			if (rand.nextInt(16) == 0) {
				if (rand.nextInt(3) == 0) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.WHITE), rand);
				else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.GREEN), rand);
			}
			if (rand.nextInt(16) == 0) {
				int i = rand.nextInt(6);
				if (i < 3) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.MARSH), rand);
				else if (i < 5) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PINK), rand);
				else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PUFF), rand);
			}
		} else if (this.mistPoplarPerChunk > 0) {
			double mushroomNoise = TREE_NOISE.getValue(pos.getX() * 0.003D, pos.getZ() * 0.003D);
			if (rand.nextInt(16) == 0) {
				int i = rand.nextInt(3);
				if (i == 0) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.WHITE), rand);
				else if (i == 1) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.GREEN), rand);
				else if (mushroomNoise > 0.2) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.VIOLET), rand);
			}
			if (rand.nextInt(24) == 0) {
				if (mushroomNoise > 0.2) {
					if (rand.nextBoolean()) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PUFF), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.GRAY), rand);
				} else {
					int i = rand.nextInt(5);
					if (i < 2) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PUFF), rand);
					else if (i < 4) MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.PINK), rand);
					else MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.YELLOW), rand);
				
				}
			}
		}
		if (this != MistBiomes.upSwampyForest) {
			DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.GRASS);
			for (int k = 0; k < 20; ++k) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				int ry = rand.nextInt(world.getHeight(pos.add(rx, 0, rz)).getY() + 32);
				if (DOUBLE_PLANT_GENERATOR.generate(world, rand, new BlockPos(pos.getX() + rx, ry, pos.getZ() + rz))) {
					break;
				}
			}
		}
	}

	/** 0 - Clay, 1 - Gravel, 2 - Sapropel */
	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double clayNoise = CLAY_NOISE.getValue(x * 0.01D, z * 0.01D);
		noises.add(clayNoise + CLAY_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.02D); // 0
		double gravelNoise = GRAVEL_NOISE.getValue(x * 0.05D, z * 0.05D);
		gravelNoise = gravelNoise + GRAVEL_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.3D;
		noises.add(gravelNoise); // 1
		double sapropelNoise = SAPROPEL_NOISE.getValue(x * 0.02D, z * 0.02D);
		sapropelNoise = sapropelNoise + SAPROPEL_NOISE.getValue(x * 0.1D, z * 0.1D) * 0.3D;
		noises.add(sapropelNoise); // 2
		return noises;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.7D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.6D ? MistBlocks.CLAY.getDefaultState() : this.fillerBlock;
	}

	@Override
	protected IBlockState getSecondBottom(Random rand, ArrayList<Double> noises) {
		return noises.get(0) > 0.65D ? MistBlocks.CLAY.getDefaultState() : this.secondTopBlock;
	}

	@Override
	protected void placeTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (y < MistWorld.seaLevelUp - 1 && noises.get(2) > 1.0D - (MistWorld.seaLevelUp - y) / 10.0D && state.getBlock() != MistBlocks.CLAY) {
			chunkPrimer.setBlockState(x, y, z, MistBlocks.SAPROPEL.getDefaultState());
		} else chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	protected void placeSecondTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (y < MistWorld.seaLevelUp - 2 && state.getBlock() == MistBlocks.DIRT_C) {
			if (chunkPrimer.getBlockState(x, y + 1, z).getBlock() == MistBlocks.SAPROPEL) {
				chunkPrimer.setBlockState(x, y, z, state.withProperty(MistDirt.HUMUS, 2));
			} else chunkPrimer.setBlockState(x, y, z, state);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	public WorldGenerator getRandomWorldGenForGrass(Random rand) {
		return new WorldGenTallGrass(rand.nextInt(4) == 0 ? BlockTallGrass.EnumType.FERN : BlockTallGrass.EnumType.GRASS);
	}

	@Override
	public void addDefaultFlowers() {
		addFlower(Blocks.TALLGRASS.getDefaultState().withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.FERN), 80);
	}
}