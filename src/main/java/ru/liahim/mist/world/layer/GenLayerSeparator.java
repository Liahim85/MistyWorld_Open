package ru.liahim.mist.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerSeparator extends GenLayerBorders {

	public GenLayerSeparator(long seed, GenLayer genlayer) {
		super(seed, genlayer);
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		return ModBiomesIds.SEPARATOR;
	}

	@Override
	protected boolean getBool(int biome) {
		return !((BiomeMist)Biome.getBiome(biome)).isUpBiome();
	}
}