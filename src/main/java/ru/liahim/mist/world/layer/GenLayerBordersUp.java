package ru.liahim.mist.world.layer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerBordersUp extends GenLayerBorders {

	public GenLayerBordersUp(long seed, GenLayer genlayer) {
		super(seed, genlayer);
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		return getUpBorder(center, up, upLeft, left);
	}

	@Override
	protected boolean getBool(int biome) {
		return ((BiomeMist)Biome.getBiome(biome)).isUpBiome();
	}

	private int getUpBorder(int center, int up, int upLeft, int left) {
		EnumBiomeType type = ((BiomeMist)Biome.getBiome(center)).getBiomeType();
		if (type == EnumBiomeType.Forest)
			return ModBiomesIds.UP_MEADOW;
		else if (type == EnumBiomeType.Cold)
			return ModBiomesIds.UP_SNOWFIELDS;
		else if (type == EnumBiomeType.Desert)
			return ModBiomesIds.UP_SAVANNA;
		else if (type == EnumBiomeType.Swamp)
			return ModBiomesIds.UP_SWAMPY_MEADOW;
		else if (type == EnumBiomeType.Jungle)
			return ModBiomesIds.UP_JUNGLE_EDGE;
		else if (type == EnumBiomeType.Down) {
			List<Integer> biomes = new ArrayList<Integer>();
			if (((BiomeMist)Biome.getBiome(up)).isUpBiome())
				biomes.add(up);
			if (((BiomeMist)Biome.getBiome(upLeft)).isUpBiome())
				biomes.add(upLeft);
			if (((BiomeMist)Biome.getBiome(left)).isUpBiome())
				biomes.add(left);
			if (biomes.size() > 0)
				return getUpBorder(biomes.get(nextInt(biomes.size())), up, upLeft, left);
			else return ModBiomesIds.BORDER_UP_PLAINS;
		}
		return ModBiomesIds.UP_MEADOW;
	}
}