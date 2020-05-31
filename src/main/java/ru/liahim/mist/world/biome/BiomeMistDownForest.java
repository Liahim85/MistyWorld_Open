package ru.liahim.mist.world.biome;

import ru.liahim.mist.api.block.MistBlocks;

public class BiomeMistDownForest extends BiomeMistDown {

	public BiomeMistDownForest(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistBlocks.ACID_GRASS_1.getDefaultState();
		this.fillerBlock = MistBlocks.ACID_DIRT_1.getDefaultState();
	}

	@Override
	public int getDownGrassColor() {
		return 0xC8C8C8;
	}
}