package ru.liahim.mist.world.biome;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistBorderUp extends BiomeMistBorder {

	private final Biome masterBiome;

	public BiomeMistBorderUp(BiomeProperties properties, Biome masterBiome) {
		super(properties);
		this.topBlock = MistWorld.stoneBlockUpper;
		this.fillerBlock = MistWorld.stoneBlockUpper;
        this.spawnableMonsterList.add(new Biome.SpawnListEntry(EntitySkeleton.class, 1, 0, 1));
        this.masterBiome = masterBiome;
	}

	@Override
	public boolean isUpBiome() {
		return true;
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Border;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getGrassColorAtPos(BlockPos pos) {
		return masterBiome.getGrassColorAtPos(pos);
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getFoliageColorAtPos(BlockPos pos) {
		return masterBiome.getFoliageColorAtPos(pos);
	}

	@Override
	@Nullable
	public ArrayList<Double> getNoises(int x, int z) {
		ArrayList<Double> noises = new ArrayList<Double>();
		double gravelNoise = GRAVEL_NOISE.getValue(x * 0.05D, z * 0.05D);
		gravelNoise = gravelNoise + GRAVEL_NOISE.getValue(x * 0.3D, z * 0.3D) * 0.3D;
		noises.add(gravelNoise); //0
		return noises;
	}

	@Override
	public IBlockState getTopBlock(Random rand, @Nullable ArrayList<Double> noises) {
		return noises.get(0) > 0.8 ? MistBlocks.GRAVEL.getDefaultState() : this.topBlock;
	}
}