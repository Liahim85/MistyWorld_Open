package ru.liahim.mist.world.layer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerBordersFinal extends GenLayerBorders {

	private boolean isUp;

	public GenLayerBordersFinal(long seed, GenLayer genlayer, boolean isUp) {
		super(seed, genlayer);
		this.isUp = isUp;
	}

	@Override
	protected int getBorder(int center, int up, int upLeft, int left) {
		return isUp ? getUpBorder(center, up, upLeft, left) : ModBiomesIds.BORDER_DOWN;
	}

	@Override
	protected boolean getBool(int center) {
		return isUpBiome(center) == isUp;
	}

	private int getUpBorder(int center, int up, int upLeft, int left) {
		if (Biome.getBiome(center) instanceof BiomeMist) {
			EnumBiomeType type = ((BiomeMist)Biome.getBiome(center)).getBiomeType();
			if (type == EnumBiomeType.Cold)
				return ModBiomesIds.BORDER_UP_COLD;
			else if (type == EnumBiomeType.Desert)
				return ModBiomesIds.BORDER_UP_DESERT;
			else if (type == EnumBiomeType.Swamp)
				return ModBiomesIds.BORDER_UP_SWAMP;
			else if (type == EnumBiomeType.Jungle)
				return ModBiomesIds.BORDER_UP_JUNGLE;
			else if (type == EnumBiomeType.Down) {
				List<Integer> biomes = new ArrayList<Integer>();
				if (isUpBiome(up)) biomes.add(up);
				if (isUpBiome(upLeft)) biomes.add(upLeft);
				if (isUpBiome(left)) biomes.add(left);
				if (biomes.size() > 0)
					return getUpBorder(biomes.get(nextInt(biomes.size())), up, upLeft, left);
				else return ModBiomesIds.BORDER_UP_PLAINS;
			}
		}
		return ModBiomesIds.BORDER_UP_PLAINS;
	}
}