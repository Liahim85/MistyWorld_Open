package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import ru.liahim.mist.init.ModBiomesIds;

public class GenLayerDiversifyMin extends GenLayer {

	private final int meadowBiomes[] = new int[] {
			ModBiomesIds.UP_FOREST, ModBiomesIds.UP_FOREST, ModBiomesIds.UP_FOREST,
			ModBiomesIds.UP_LAKE //, ModBiomesIds.UP_MARSH // TODO replace to Shields
	};
	private final int forestBiomes[] = new int[] {
			ModBiomesIds.UP_MEADOW, ModBiomesIds.UP_MEADOW, ModBiomesIds.UP_MEADOW,
			ModBiomesIds.UP_LAKE //, ModBiomesIds.UP_MARSH // TODO replace to Shields
	};
	private final int denseForestBiomes[] = new int[] {
			ModBiomesIds.UP_FOREST, ModBiomesIds.UP_LAKE
	};
	private final int savannaBiomes[] = new int[] {
			ModBiomesIds.UP_DESERT, ModBiomesIds.UP_SAVANNA, ModBiomesIds.UP_SAVANNA //, ModBiomesIds.idBiomeUpRockyDesert
	};
	/*private final int desertBiomes[] = new int[] {
			ModBiomesIds.idBiomeUpDesert, ModBiomesIds.idBiomeUpRockyDesert
	};
	private final int dunesBiomes[] = new int[] {
			ModBiomesIds.idBiomeUpDesert, ModBiomesIds.idBiomeUpDunes, ModBiomesIds.idBiomeUpOasis
	};*/
	private final int snowfieldsBiomes[] = new int[] {
			ModBiomesIds.UP_SNOWFIELDS, ModBiomesIds.UP_COLD_LOWLAND //, ModBiomesIds.UP_TAIGA, ModBiomesIds.idBiomeUpGlacier
	};
	private final int taigaBiomes[] = new int[] {
			ModBiomesIds.UP_SNOWFIELDS, ModBiomesIds.UP_COLD_LOWLAND
	};
	private final int hillyTaigaBiomes[] = new int[] {
			ModBiomesIds.UP_HILLY_TAIGA, ModBiomesIds.UP_TAIGA //, ModBiomesIds.idBiomeUpGlacier, ModBiomesIds.idBiomeUpHotSprings
	};
	private final int swampBiomes[] = new int[] {
			ModBiomesIds.UP_SWAMP, ModBiomesIds.UP_SWAMPY_FOREST,
			ModBiomesIds.UP_SWAMPY_MEADOW, ModBiomesIds.UP_SWAMPY_MEADOW //, ModBiomesIds.idBiomeUpMushrooms
	};
	private final int swampyMeadowBiomes[] = new int[] {
			ModBiomesIds.UP_SWAMPY_FOREST, ModBiomesIds.UP_SWAMPY_MEADOW
	};
	private final int swampyForestBiomes[] = new int[] {
			ModBiomesIds.UP_SWAMPY_MEADOW //, ModBiomesIds.UP_DENSE_FOREST // TODO replace
	};
	private final int jungleBiomes[] = new int[] {
			ModBiomesIds.UP_JUNGLE_HILLS, ModBiomesIds.UP_JUNGLE, ModBiomesIds.UP_LOWLAND
	};
	private final int jungleEdgeBiomes[] = new int[] {
			ModBiomesIds.UP_JUNGLE, ModBiomesIds.UP_LOWLAND
	};
	private final int jungleHillsBiomes[] = new int[] {
			ModBiomesIds.UP_LOWLAND
	};
	private final int downBiomes[] = new int[] {
			ModBiomesIds.DOWN, ModBiomesIds.DOWN,
			ModBiomesIds.DOWN_FOREST, ModBiomesIds.DOWN_FOREST, ModBiomesIds.DOWN_SWAMP
	};

	public GenLayerDiversifyMin(long seed, GenLayer genlayer) {
		super(seed);
		this.parent = genlayer;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int input[] = parent.getInts(x, z, width, height);
		int output[] = IntCache.getIntCache(width * height);
		int biome;
		for (int zOut = 0; zOut < height; zOut++) {
			for (int xOut = 0; xOut < width; xOut++) {
				int i = xOut + zOut * width;
				biome = input[i];
				initChunkSeed(xOut + x, zOut + z);
				if (biome == ModBiomesIds.DOWN && biome != ModBiomesIds.DOWN_CENTER)
					output[i] = downBiomes[nextInt(downBiomes.length)];
				else if (nextInt(5) == 0) {
					if (biome == ModBiomesIds.UP_MEADOW)
						output[i] = meadowBiomes[nextInt(meadowBiomes.length)];
					else if (biome == ModBiomesIds.UP_SWAMPY_MEADOW)
						output[i] = swampyMeadowBiomes[nextInt(swampyMeadowBiomes.length)];
					else if (biome == ModBiomesIds.UP_FOREST)
						output[i] = forestBiomes[nextInt(forestBiomes.length)];
					else if (biome == ModBiomesIds.UP_DENSE_FOREST)
						output[i] = denseForestBiomes[nextInt(denseForestBiomes.length)];
					else if (biome == ModBiomesIds.UP_SAVANNA)
						output[i] = savannaBiomes[nextInt(savannaBiomes.length)];
					//else if (biome == ModBiomesIds.idBiomeUpDesert)
						//output[i] = desertBiomes[nextInt(desertBiomes.length)];
					//else if (biome == ModBiomesIds.idBiomeUpDunes)
						//output[i] = dunesBiomes[nextInt(dunesBiomes.length)];
					else if (biome == ModBiomesIds.UP_SNOWFIELDS)
						output[i] = snowfieldsBiomes[nextInt(snowfieldsBiomes.length)];
					else if (biome == ModBiomesIds.UP_TAIGA)
						output[i] = taigaBiomes[nextInt(taigaBiomes.length)];
					else if (biome == ModBiomesIds.UP_HILLY_TAIGA)
						output[i] = hillyTaigaBiomes[nextInt(hillyTaigaBiomes.length)];
					else if (biome == ModBiomesIds.UP_SWAMP)
						output[i] = swampBiomes[nextInt(swampBiomes.length)];
					else if (biome == ModBiomesIds.UP_SWAMPY_FOREST)
						output[i] = swampyForestBiomes[nextInt(swampyForestBiomes.length)];
					else if (biome == ModBiomesIds.UP_JUNGLE)
						output[i] = jungleBiomes[nextInt(jungleBiomes.length)];
					else if (biome == ModBiomesIds.UP_JUNGLE_EDGE)
							output[i] = jungleEdgeBiomes[nextInt(jungleEdgeBiomes.length)];
					else if (biome == ModBiomesIds.UP_JUNGLE_HILLS)
							output[i] = jungleHillsBiomes[nextInt(jungleHillsBiomes.length)];
					else output[i] = input[i];
				} else output[i] = input[i];
			}
		}
		return output;
	}
}