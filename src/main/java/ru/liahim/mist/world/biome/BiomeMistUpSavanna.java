package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.upperplant.MistMushroom;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.entity.EntityCaravan;
import ru.liahim.mist.entity.EntityCyclops;
import ru.liahim.mist.entity.EntityBarvog;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.generators.DesertCottonGenerator;

public class BiomeMistUpSavanna extends BiomeMistUp {

	private final IBlockState sand = MistBlocks.SAND.getDefaultState();

	public BiomeMistUpSavanna(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistBlocks.GRASS_S.getDefaultState().withProperty(MistDirt.HUMUS, 2).withProperty(IWettable.WET, false);
		this.secondTopBlock = MistBlocks.DIRT_S.getDefaultState().withProperty(MistDirt.HUMUS, 1).withProperty(IWettable.WET, false);
		this.fillerBlock = MistBlocks.DIRT_S.getDefaultState().withProperty(IWettable.WET, false);
		getMistBiomeDecorator().grassPerChunk = 5;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityCaravan.class, 5, 1, 2));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBarvog.class, 20, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityCyclops.class, 20, 2, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Desert;
	}

	@Override
	public float getSpawningChance() {
		return 0.01F;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getGrassColorAtPos(BlockPos pos) {
		double grassNoise = Biome.GRASS_COLOR_NOISE.getValue(pos.getX() * 0.02D, pos.getZ() * 0.05D);
		grassNoise = grassNoise + Biome.GRASS_COLOR_NOISE.getValue(pos.getX() * 0.2D, pos.getZ() * 0.5D);
		return grassNoise > 0.6F ? -5679071 : -5850290;
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
		if (rand.nextFloat() < 0.03F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			((MistTreeTrunk) MistBlocks.ACACIA_TRUNK).generateTrunk(world, randPos, rand);
		}
		if (rand.nextFloat() < 0.02F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			((MistTreeTrunk) MistBlocks.A_TREE_TRUNK).generateTrunk(world, randPos, rand);
		}
		super.decorate(world, rand, pos);
		if (rand.nextFloat() < 0.7F) {
			int rx = rand.nextInt(16) + 8;
			int rz = rand.nextInt(16) + 8;
			BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
			((MistTreeTrunk) MistBlocks.ACACIA_TRUNK).generateTree(world, randPos, rand);
		}
		if (rand.nextFloat() < 0.2F) {
			int k = rand.nextInt(6);
			for (int i = 0; i < k; ++i) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTreeTrunk) MistBlocks.A_TREE_TRUNK).generateTree(world, randPos, rand);
			}
		}
		if (rand.nextInt(16) == 0) {
			MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_1.getDefaultState().withProperty(MistMushroom.TYPE_1, MistMushroom.MushroomType_1.TAN), rand);
		}
		if (rand.nextInt(24) == 0) {
			MistMycelium.generateMycelium(world, pos, MistBlocks.MUSHROOMS_0.getDefaultState().withProperty(MistMushroom.TYPE_0, MistMushroom.MushroomType_0.SAND), rand);
		}
	}

	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double sandNoise = SAND_NOISE.getValue(x * 0.02D, z * 0.02D);
		noises.add(sandNoise + SAND_NOISE.getValue(x * 0.1D, z * 0.1D) * 0.2D);
		noises.add(SAND_NOISE.getValue(x * 0.2D, z * 0.2D) + sandNoise * 0.4D);
		return noises;
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.5D ? MistBlocks.GRASS_S.getDefaultState().withProperty(IWettable.WET, false).withProperty(MistDirt.HUMUS, 1) : this.topBlock;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		return sandNoise > 0.5D ? this.sand : sandNoise > 0.4D ? MistBlocks.DIRT_S.getDefaultState().withProperty(IWettable.WET, false) : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		return sandNoise > 0.4D ? this.sand : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.5D || noises.get(1) > 0.4D ? this.sand.withProperty(IWettable.WET, true) : MistBlocks.DIRT_S.getDefaultState();
	}

	@Override
	protected IBlockState getSecondBottom(Random rand, ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		return sandNoise > 0.4D ? this.sand : sandNoise > 0.3D ? MistBlocks.DIRT_S.getDefaultState().withProperty(IWettable.WET, true) : this.secondTopBlock.withProperty(IWettable.WET, true);
	}

	@Override
	protected IBlockState getZeroBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) < -0.5D ? MistWorld.gravelBlock : null;
	}

	public double getClayNoise(BlockPos pos) {
		double clayNoise = CLAY_NOISE.getValue(pos.getX() * 0.02D, pos.getZ() * 0.02D);
		clayNoise = clayNoise + CLAY_NOISE.getValue(pos.getX() * 0.1D, pos.getZ() * 0.1D) * 0.1D;
		return clayNoise;
	}

	@Override
	public WorldGenerator getRandomWorldGenForGrass(Random rand) {
		return rand.nextInt(16) == 0 ? new DesertCottonGenerator() : super.getRandomWorldGenForGrass(rand);
	}
}