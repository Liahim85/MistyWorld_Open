package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockSand;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.biome.Biome;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistClay;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.entity.EntityMomo;
import ru.liahim.mist.entity.EntityHulter;
import ru.liahim.mist.entity.EntityWoodlouse;

public class BiomeMistUpJungleEdge extends BiomeMistUpJungle {
	
	private IBlockState redSand = MistBlocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND).withProperty(IWettable.WET, true);

	public BiomeMistUpJungleEdge(BiomeProperties properties, int treePerChunk) {
		super(properties, treePerChunk);
		this.spawnableCreatureList.clear();
		this.spawnableMonsterList.clear();
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityMomo.class, 20, 2, 5));
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityHulter.class, 5, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityWoodlouse.class, 20, 2, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.6D) return this.topBlock;
		double sandNoise = noises.get(0);
		if (sandNoise > 0.3D) return this.redSand.withProperty(IWettable.WET, false);
		else if (sandNoise > 0.2D) return MistBlocks.GRASS_T.getDefaultState().withProperty(MistDirt.HUMUS, 0);
		else if (sandNoise > 0.1D) return MistBlocks.GRASS_T.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		return this.topBlock;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.6D) return this.secondTopBlock;
		double sandNoise = noises.get(0);
		return sandNoise > 0.2D ? this.redSand : sandNoise > 0.1D ? MistBlocks.DIRT_T.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.6D) return MistBlocks.CLAY.getDefaultState().withProperty(MistClay.VARIANT, MistClay.EnumClayType.RED_CLAY);
		return noises.get(0) > 0.1D ? this.redSand : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(1) > 0.7D) return MistBlocks.CLAY.getDefaultState().withProperty(MistClay.VARIANT, MistClay.EnumClayType.RED_CLAY);
		double sandNoise = noises.get(0);
		return sandNoise > 0.2D ? this.redSand : sandNoise > 0.1 ? MistBlocks.DIRT_T.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.secondTopBlock;
	}
}