package ru.liahim.mist.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.biome.MistBiomes;
import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerDiversify extends GenLayer {

	private final int deltaX;
	private final int deltaZ;
	private final boolean needCenters;
	private final int point = ModBiomesIds.BORDER_DOWN;
	private final int border = ModBiomesIds.DOWN_CENTER;
	private final int forestBiomes[] = new int[] {
			ModBiomesIds.UP_FOREST, ModBiomesIds.UP_FOREST,
			ModBiomesIds.UP_DENSE_FOREST, ModBiomesIds.UP_DENSE_FOREST, ModBiomesIds.UP_MEADOW
	};
	private final int desertBiomes[] = new int[] {
			ModBiomesIds.UP_SAVANNA, ModBiomesIds.UP_SAVANNA, ModBiomesIds.UP_SAVANNA,
			ModBiomesIds.UP_DESERT, ModBiomesIds.UP_DESERT, ModBiomesIds.UP_DUNES
	};
	private final int coldBiomes[] = new int[] {
			ModBiomesIds.UP_SNOWFIELDS, ModBiomesIds.UP_TAIGA, ModBiomesIds.UP_HILLY_TAIGA
	};
	private final int swampBiomes[] = new int[] {
			ModBiomesIds.UP_SWAMP, ModBiomesIds.UP_SWAMP,
			ModBiomesIds.UP_SWAMPY_FOREST, ModBiomesIds.UP_SWAMPY_FOREST, ModBiomesIds.UP_SWAMPY_MEADOW
	};
	private final int jungleBiomes[] = new int[] {
			ModBiomesIds.UP_JUNGLE, ModBiomesIds.UP_JUNGLE,
			ModBiomesIds.UP_JUNGLE_HILLS, ModBiomesIds.UP_JUNGLE_HILLS, ModBiomesIds.UP_JUNGLE_EDGE
	};
	private final int downBiomes[] = new int[] {
			ModBiomesIds.BORDER_DOWN
	};

	public GenLayerDiversify(long seed, GenLayer genlayer, boolean needCenters, int deltaX, int deltaZ) {
		super(seed);
		this.parent = genlayer;
		this.needCenters = needCenters;
		this.deltaX = (deltaX * 2 + 4) & 7;
		this.deltaZ = (deltaZ * 2 + 4) & 7;
	}

	public GenLayerDiversify(long seed, GenLayer genlayer) {
		this(seed, genlayer, false, 0, 0);
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		if (this.needCenters)
			return diversifyMax(x, z, width, height);
		else return diversifyMed(x, z, width, height);
	}

	private int[] diversifyMed(int x, int z, int width, int height) {
		int input[] = parent.getInts(x, z, width, height);
		int output[] = IntCache.getIntCache(width * height);
		EnumBiomeType type;
		for (int zOut = 0; zOut < height; zOut++) {
			for (int xOut = 0; xOut < width; xOut++) {
				int i = xOut + zOut * width;
				int biome = input[i];
				initChunkSeed(xOut + x, zOut + z);
				type = ((BiomeMist)Biome.getBiome(biome)).getBiomeType();
				if (nextInt(5) == 0) {
					if (type == EnumBiomeType.Forest)
						output[i] = forestBiomes[nextInt(forestBiomes.length)];
					else if (type == EnumBiomeType.Desert)
						output[i] = desertBiomes[nextInt(desertBiomes.length)];
					else if (type == EnumBiomeType.Cold)
						output[i] = coldBiomes[nextInt(coldBiomes.length)];
					else if (type == EnumBiomeType.Swamp)
						output[i] = swampBiomes[nextInt(swampBiomes.length)];
					else if (type == EnumBiomeType.Jungle)
						output[i] = jungleBiomes[nextInt(jungleBiomes.length)];
					else output[i] = biome;
				} else output[i] = biome;
			}
		}
		return output;
	}

	private int[] diversifyMax(int x, int z, int width, int height) {
		int x0 = x - 1;
		int z0 = z - 1;
		int widthOut = width + 2;
		int heightOut = height + 2;
		int input[] = parent.getInts(x0, z0, widthOut, heightOut);
		int output[] = IntCache.getIntCache(width * height);
		EnumBiomeType type;
		for (int zOut = 0; zOut < height; zOut++) {
			for (int xOut = 0; xOut < width; xOut++) {
				int right = input[xOut + 0 + (zOut + 1) * widthOut];
				int left = input[xOut + 2 + (zOut + 1) * widthOut];
				int up = input[xOut + 1 + (zOut + 0) * widthOut];
				int down = input[xOut + 1 + (zOut + 2) * widthOut];
				int center = input[xOut + 1 + (zOut + 1) * widthOut];
				int ur = input[xOut + 0 + (zOut + 0) * widthOut];
				int ul = input[xOut + 2 + (zOut + 0) * widthOut];
				int dr = input[xOut + 0 + (zOut + 2) * widthOut];
				int dl = input[xOut + 2 + (zOut + 2) * widthOut];
				initChunkSeed(xOut + x, zOut + z);
				if (Biome.getBiome(center) != null && Biome.getBiome(center) instanceof BiomeMist) {
					type = ((BiomeMist)Biome.getBiome(center)).getBiomeType();
					if (type == EnumBiomeType.Forest)
						output[xOut + zOut * width] = forestBiomes[nextInt(forestBiomes.length)];
					else if (type == EnumBiomeType.Desert)
						output[xOut + zOut * width] = desertBiomes[nextInt(desertBiomes.length)];
					else if (type == EnumBiomeType.Cold)
						output[xOut + zOut * width] = coldBiomes[nextInt(coldBiomes.length)];
					else if (type == EnumBiomeType.Swamp)
						output[xOut + zOut * width] = swampBiomes[nextInt(swampBiomes.length)];
					else if (type == EnumBiomeType.Jungle)
						output[xOut + zOut * width] = jungleBiomes[nextInt(jungleBiomes.length)];
					else if (type == EnumBiomeType.Down || type == EnumBiomeType.Border)
						output[xOut + zOut * width] = center;
				} else output[xOut + zOut * width] = Biome.getIdForBiome(MistBiomes.upMeadow);
				if (((xOut + x) >> 3 << 3 | deltaX) == xOut + x && ((zOut + z) >> 3 << 3 | deltaZ) == zOut + z) {
					output[xOut + zOut * width] = input[xOut + 1 + (zOut + 1) * widthOut] = downBiomes[nextInt(downBiomes.length)];
				}
			}
		}
		return output;
	}

	private boolean onBorder(int center, int right, int left, int up, int down) {
		if (((BiomeMist)Biome.getBiome(right)).getBiomeType() != ((BiomeMist)Biome.getBiome(center)).getBiomeType())
			return true;
		if (((BiomeMist)Biome.getBiome(left)).getBiomeType() != ((BiomeMist)Biome.getBiome(center)).getBiomeType())
			return true;
		if (((BiomeMist)Biome.getBiome(up)).getBiomeType() != ((BiomeMist)Biome.getBiome(center)).getBiomeType())
			return true;
		if (((BiomeMist)Biome.getBiome(down)).getBiomeType() != ((BiomeMist)Biome.getBiome(center)).getBiomeType())
			return true;
		return false;
	}

	private boolean notClose(int right, int left, int up, int down) {
		if (right == point || right == border)
			return false;
		if (left == point || left == border)
			return false;
		if (up == point || up == border)
			return false;
		if (down == point || down == border)
			return false;
		return true;
	}
}