package ru.liahim.mist.world.biome;

import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.world.MistWorld;

public class BiomeMistBorderDown extends BiomeMistBorder {

	public BiomeMistBorderDown(BiomeProperties properties) {
		super(properties);
		this.topBlock = MistWorld.gravelBlock;
		this.fillerBlock = MistWorld.gravelBlock;
	}

	@Override
	public boolean isUpBiome() {
		return false;
	}

	@Override
	public EnumBiomeType getBiomeType() {
		return EnumBiomeType.Border;
	}
}