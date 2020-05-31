package ru.liahim.mist.world.biome;

import ru.liahim.mist.api.block.MistBlocks;

public class BiomeMistDownNormal extends BiomeMistDown {

	public BiomeMistDownNormal(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistBlocks.ACID_GRASS_2.getDefaultState();
		this.fillerBlock = MistBlocks.ACID_DIRT_2.getDefaultState();
	}

	@Override
	public int getDownGrassColor() {
		return 0xCDC8AF;
	}
}