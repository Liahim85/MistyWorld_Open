package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.tree.MistTrunkATree;
import ru.liahim.mist.block.tree.MistTrunkAcacia;
import ru.liahim.mist.entity.EntityCaravan;
import ru.liahim.mist.entity.EntityBarvog;
import ru.liahim.mist.entity.EntityCyclops;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistUpDesert extends BiomeMistUp {

	private final boolean grow;
	private final IBlockState dirt0 = MistBlocks.DIRT_S.getDefaultState().withProperty(IWettable.WET, false);
	private final IBlockState dirt1 = dirt0.withProperty(MistDirt.HUMUS, 1);
	private final IBlockState dirt2 = dirt0.withProperty(MistDirt.HUMUS, 2);

	public BiomeMistUpDesert(BiomeProperties properties, boolean grow) {
		super(properties);
		this.topBlock = MistBlocks.SAND.getDefaultState();
		this.fillerBlock = MistBlocks.SAND.getDefaultState();
		this.grow = grow;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityCaravan.class, 20, 1, 4));
		if (grow) this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityBarvog.class, 10, 1, 4));
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
		return 0xbf7839;
		//double grassNoise = Biome.GRASS_COLOR_NOISE.getValue(pos.getX() * 0.05D, pos.getZ() * 0.02D);
		//grassNoise = grassNoise + Biome.GRASS_COLOR_NOISE.getValue(pos.getX() * 0.5D, pos.getZ() * 0.2D);
		//return grassNoise > 0.6F ? -5679071 : -5850290;
	}

	@Override
	public void decorate(World world, Random rand, BlockPos pos) {
		if (this.grow) {
			if (rand.nextFloat() < 0.05F) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTrunkAcacia) MistBlocks.ACACIA_TRUNK).generateDunesTrunk(world, randPos, rand);
			}
			if (rand.nextFloat() < 0.05F) {
				int rx = rand.nextInt(16) + 8;
				int rz = rand.nextInt(16) + 8;
				BlockPos randPos = world.getHeight(pos.add(rx, 0, rz));
				((MistTrunkATree) MistBlocks.A_TREE_TRUNK).generateDunesTrunk(world, randPos, rand);
			}
		}
		super.decorate(world, rand, pos);
	}

	/** 0 - Dirt */
	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double grassNoise = -SAND_NOISE.getValue(x * 0.01D, z * 0.01D);
		double minorGrassNoise = -SAND_NOISE.getValue(x * 0.3D, z * 0.3D);
		noises.add(grassNoise + minorGrassNoise * 0.3D); //0
		return noises;
	}

	@Override
	protected void placeTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (this.grow && y < MistWorld.seaLevelUp + 5) {
			double i = noises.get(0) + (MistWorld.seaLevelUp + 5 - y) * 0.3D - 1.0D;
			if (i > 0.5D) chunkPrimer.setBlockState(x, y, z, this.dirt2);
			else if (i > 0.1D) chunkPrimer.setBlockState(x, y, z, this.dirt1);
			else if (i > -0.4D) chunkPrimer.setBlockState(x, y, z, this.dirt0);
			else chunkPrimer.setBlockState(x, y, z, state);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	protected void placeSecondTopBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (this.grow && y < MistWorld.seaLevelUp + 5) {
			double i = noises.get(0) + (MistWorld.seaLevelUp + 5 - y) * 0.3D - 1.0D;
			if (i > -0.0D) chunkPrimer.setBlockState(x, y, z, this.dirt1);
			else if (i > -0.5D) chunkPrimer.setBlockState(x, y, z, this.dirt0);
			else chunkPrimer.setBlockState(x, y, z, state);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}

	@Override
	protected void placeFillerBlock(ChunkPrimer chunkPrimer, int x, int y, int z, IBlockState state, @Nullable ArrayList<Double> noises) {
		if (this.grow && y < MistWorld.seaLevelUp + 5 &&
				noises.get(0) + (MistWorld.seaLevelUp + 5 - y) * 0.3D - 1.0D > -0.6D) {
			chunkPrimer.setBlockState(x, y, z, this.dirt0);
		} else chunkPrimer.setBlockState(x, y, z, state);
	}
}