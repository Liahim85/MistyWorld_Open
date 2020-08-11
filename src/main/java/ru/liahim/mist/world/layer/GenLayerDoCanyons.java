package ru.liahim.mist.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.api.biome.EnumBiomeType;
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
			if (getBiomeType(center) != getBiomeType(up)) return true;
			if (getBiomeType(center) != getBiomeType(upLeft)) return true;
			if (getBiomeType(center) != getBiomeType(left)) return true;
			if (getBiomeType(up) != getBiomeType(left)) return true;
		}
		return false;
	}

	protected EnumBiomeType getBiomeType(int center) {
		return Biome.getBiome(center) instanceof BiomeMist ? ((BiomeMist)Biome.getBiome(center)).getBiomeType() : EnumBiomeType.Down;
	}
}