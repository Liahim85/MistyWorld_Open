package ru.liahim.mist.world.biome;

import ru.liahim.mist.api.block.MistBlocks;

public class BiomeMistDownSwamp extends BiomeMistDown {

	public BiomeMistDownSwamp(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistBlocks.ACID_GRASS_0.getDefaultState();
		this.fillerBlock = MistBlocks.ACID_DIRT_0.getDefaultState();
	}

	@Override
	public int getDownGrassColor() {
		return 0xC8D2E6;
	}
}