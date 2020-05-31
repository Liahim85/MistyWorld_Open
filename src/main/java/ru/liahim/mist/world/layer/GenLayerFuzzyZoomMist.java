package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;

public class GenLayerFuzzyZoomMist extends GenLayerZoomMist {

	public GenLayerFuzzyZoomMist(long seed, GenLayer genLayer) {
		super(seed, genLayer, false);
	}

	@Override
	protected int selectModeOrRandom(int int_1, int int_2, int int_3, int int_4) {
		return this.selectRandom(new int[] { int_1, int_2, int_3, int_4 });
	}
}