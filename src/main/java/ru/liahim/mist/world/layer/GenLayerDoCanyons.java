package ru.liahim.mist.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerDoCanyons extends GenLayerDoubleBorder {

	public GenLayerDoCanyons(long seed, GenLayer genlayer) {
		super(seed, genlayer);
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		return ModBiomesIds.DOWN;
	}

	@Override
	protected boolean getBool(int center, int up, int upLeft, int left) {
		int point = ModBiomesIds.BORDER_DOWN;
		if (center != point && up != point && upLeft != point && left != point) {
			if (((BiomeMist)Biome.getBiome(center)).getBiomeType() != ((BiomeMist)Biome.getBiome(up)).getBiomeType())
				return true;
			if (((BiomeMist)Biome.getBiome(center)).getBiomeType() != ((BiomeMist)Biome.getBiome(upLeft)).getBiomeType())
				return true;
			if (((BiomeMist)Biome.getBiome(center)).getBiomeType() != ((BiomeMist)Biome.getBiome(left)).getBiomeType())
				return true;
			if (((BiomeMist)Biome.getBiome(up)).getBiomeType() != ((BiomeMist)Biome.getBiome(left)).getBiomeType())
				return true;
		}
		return false;
	}
}