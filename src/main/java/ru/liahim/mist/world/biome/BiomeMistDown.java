package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.world.MistWorld;

public abstract class BiomeMistDown extends BiomeMist {

	public BiomeMistDown(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistBlocks.ACID_GRASS_2.getDefaultState();
		this.fillerBlock = MistBlocks.ACID_DIRT_2.getDefaultState();
	}

	@Override
	public boolean isUpBiome() {
		return false;
	}

	@Override
	public int getDownGrassColor() {
		return 0xC8C8C8;
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Down;
	}
	
	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double sandNoise = SAND_NOISE.getValue(x * 0.02D, z * 0.02D);
		sandNoise = sandNoise + SAND_NOISE.getValue(x * 0.2D, z * 0.2D) * 0.2D;
		noises.add(sandNoise); //0
		double gravelNoise = GRAVEL_NOISE.getValue(x * 0.05D, z * 0.05D);
		gravelNoise = gravelNoise + GRAVEL_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.3D;
		noises.add(gravelNoise); //1
		return noises;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.6D ? MistBlocks.ACID_SAND.getDefaultState() : noises.get(1) > 0.6D ? MistWorld.gravelBlock : this.fillerBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.5D ? MistBlocks.ACID_SAND.getDefaultState() : noises.get(1) > 0.5D ? MistWorld.gravelBlock : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.7D ? MistBlocks.ACID_SAND.getDefaultState() : noises.get(1) > 0.7D ? MistWorld.gravelBlock : this.fillerBlock;
	}
}