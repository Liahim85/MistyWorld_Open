package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.biome.Biome;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.entity.EntityForestRunner;
import ru.liahim.mist.entity.EntityForestSpider;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistUpMeadow extends BiomeMistUp {

	private IBlockState sand = MistBlocks.SAND.getDefaultState().withProperty(IWettable.WET, true);

	public BiomeMistUpMeadow(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistBlocks.GRASS_F.getDefaultState().withProperty(MistDirt.HUMUS, 2);
		this.secondTopBlock = MistBlocks.DIRT_F.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		this.fillerBlock = MistBlocks.DIRT_F.getDefaultState();
		getMistBiomeDecorator().grassPerChunk = 10;
		this.spawnableCreatureList.add(new Biome.SpawnListEntry(EntityForestRunner.class, 20, 1, 3));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntityForestSpider.class, 20, 2, 4));
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 5, 0, 1));
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Forest;
	}

	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double sandNoise = SAND_NOISE.getValue(x * 0.02D, z * 0.02D);
		double minorSandNoise = SAND_NOISE.getValue(x * 0.2D, z * 0.2D);
		double clayNoise = CLAY_NOISE.getValue(x * 0.01D, z * 0.01D);
		noises.add(sandNoise + minorSandNoise * 0.2D); //0
		noises.add(sandNoise + minorSandNoise * 0.1D); //1
		noises.add(clayNoise); //2
		double gravelNoise = GRAVEL_NOISE.getValue(x * 0.05D, z * 0.05D);
		gravelNoise = gravelNoise + GRAVEL_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.3D;
		noises.add(gravelNoise); //3
		return noises;
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		if (noises.get(2) > 0.7D) return this.topBlock;
		double sandNoise = noises.get(1);
		if (sandNoise > 0.5D) return this.sand.withProperty(IWettable.WET, false);
		else if (sandNoise > 0.4D) return MistBlocks.GRASS_S.getDefaultState().withProperty(MistDirt.HUMUS, 0);
		else if (sandNoise > 0.3D) return MistBlocks.GRASS_S.getDefaultState().withProperty(MistDirt.HUMUS, 1);
		return this.topBlock;
	}

	@Override
	public IBlockState getSecondTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		double clayNoise = noises.get(2);
		if (noises.get(3) > 0.7D && sandNoise <= 0.4D) return MistWorld.gravelBlock;
		if (clayNoise > 0.7D) return sandNoise < 0.4D ? MistBlocks.DIRT_C.getDefaultState().withProperty(MistDirt.HUMUS, 1) : this.secondTopBlock;
		return sandNoise > 0.4D ? this.sand : sandNoise > 0.3D ? MistBlocks.DIRT_S.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.secondTopBlock;
	}

	@Override
	public IBlockState getFillerBlock(Random rand, @Nullable ArrayList<Double> noises) {
		double sandNoise = noises.get(0);
		double clayNoise = noises.get(2);
		if (noises.get(3) > 0.5D && sandNoise <= 0.3D) return MistWorld.gravelBlock;
		if (clayNoise > 0.7D) return MistBlocks.CLAY.getDefaultState();
		else if (clayNoise > 0.6D) return clayNoise > 0.65D || sandNoise < 0.3D ? MistBlocks.DIRT_C.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.fillerBlock;
		return sandNoise > 0.3D ? this.sand : sandNoise > 0.2D ? MistBlocks.DIRT_S.getDefaultState().withProperty(MistDirt.HUMUS, 0) : this.fillerBlock;
	}

	@Override
	protected IBlockState getBottom(Random rand, @Nullable ArrayList<Double> noises) {
		IBlockState state = super.getBottom(rand, noises);
		return noises.get(3) > 0.5D && state.getBlock() != MistBlocks.SAND ? MistWorld.gravelBlock : state;
	}

	@Override
	protected IBlockState getSecondBottom(Random rand, @Nullable ArrayList<Double> noises) {
		IBlockState state = super.getBottom(rand, noises);
		return noises.get(3) > 0.4D && state.getBlock() != MistBlocks.SAND ? MistWorld.gravelBlock : state;
	}
}