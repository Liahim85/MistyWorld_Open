package ru.liahim.mist.world.layer;

import ru.liahim.mist.init.ModBiomesIds;
import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerDownSwampBorder extends GenLayerBorders {

	public GenLayerDownSwampBorder(long seed, GenLayer genlayer) {
		super(seed, genlayer);
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		if (center == ModBiomesIds.BORDER_DOWN || up == ModBiomesIds.BORDER_DOWN ||
				upLeft == ModBiomesIds.BORDER_DOWN || left == ModBiomesIds.BORDER_DOWN) {
			return ModBiomesIds.DOWN_CENTER;
		}
		return ModBiomesIds.DOWN_SWAMP;
	}

	@Override
	protected boolean getBool(int biome) {
		return biome == ModBiomesIds.DOWN_CENTER;
	}
}