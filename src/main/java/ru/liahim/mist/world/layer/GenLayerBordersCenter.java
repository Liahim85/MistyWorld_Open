package ru.liahim.mist.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerBordersCenter extends GenLayerBorders {

	public GenLayerBordersCenter(long seed, GenLayer genlayer) {
		super(seed, genlayer);
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		return Biome.getIdForBiome(MistBiomes.down);
	}

	@Override
	protected boolean getBool(int biome) {
		return ((BiomeMist)Biome.getBiome(biome)).isUpBiome();
	}
}