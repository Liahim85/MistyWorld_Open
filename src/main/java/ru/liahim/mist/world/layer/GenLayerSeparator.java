package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.init.ModBiomesIds;

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
		return !isUpBiome(biome);
	}
}