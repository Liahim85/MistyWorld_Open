package ru.liahim.mist.world.layer;

import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMistBorderUp;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerUpSwampBorders extends GenLayerBorders {

	public GenLayerUpSwampBorders(long seed, GenLayer genlayer) {
		super(seed, genlayer);
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		if (Biome.getBiomeForId(center) instanceof BiomeMistBorderUp || Biome.getBiomeForId(up) instanceof BiomeMistBorderUp ||
				Biome.getBiomeForId(upLeft) instanceof BiomeMistBorderUp ||Biome.getBiomeForId(left) instanceof BiomeMistBorderUp) {
			return ModBiomesIds.UP_SWAMPY_MEADOW;
		}
		return ModBiomesIds.UP_SWAMP;
	}

	@Override
	protected boolean getBool(int biome) {
		return biome == ModBiomesIds.UP_SWAMP;
	}
}