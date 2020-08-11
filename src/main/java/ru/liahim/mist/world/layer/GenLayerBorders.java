package ru.liahim.mist.world.layer;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import ru.liahim.mist.world.biome.BiomeMist;

public abstract class GenLayerBorders extends GenLayer {

	public GenLayerBorders(long seed, GenLayer genlayer) {
		super(seed);
		this.parent = genlayer;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int widthOut = width + 2;
		int heightOut = height + 2;
		int input[] = this.parent.getInts(x - 1, z - 1, widthOut, heightOut);
		int output[] = IntCache.getIntCache(width * height);
		for (int zOut = 0; zOut < height + 1; zOut++) {
			int up = input[(zOut + 1) * widthOut];
			int upLeft = input[(zOut + 0) * widthOut];
			boolean u = getBool(up);
			boolean ul = getBool(upLeft);
			for (int xOut = 0; xOut < width + 1; xOut++) {
				int center = input[xOut + 1 + (zOut + 1) * widthOut];
				int left = input[xOut + 1 + (zOut + 0) * widthOut];
				initChunkSeed(xOut + x, zOut + z);
				int border = getBorder(center, left, upLeft, up);
				boolean c = getBool(center);
				boolean l = getBool(left);
				if (c) {
					if (xOut < width && zOut < height) {
						if (!l || !u || !ul)
							output[xOut + zOut * width] = border;
						else output[xOut + zOut * width] = center;
						if (l != u) {
							if (l && zOut > 0 && xOut < height)
								output[(xOut + 0) + (zOut - 1) * width] = border;
							if (u && xOut > 0 && zOut < width)
								output[(xOut - 1) + (zOut + 0) * width] = border;
						}
					}
				} else {
					if (xOut < width && zOut < height)
						output[xOut + zOut * width] = center;
					if (l && zOut > 0 && xOut < height)
						output[(xOut + 0) + (zOut - 1) * width] = border;
					if (u && xOut > 0 && zOut < width)
						output[(xOut - 1) + (zOut + 0) * width] = border;
					if (ul && xOut > 0 && xOut < width && zOut > 0 && zOut < height)
						output[(xOut - 1) + (zOut - 1) * width] = border;
				}
				up = center;
				upLeft = left;
				u = c;
				ul = l;
			}
		}
		return output;
	}

	protected boolean isUpBiome(int center) {
		return Biome.getBiome(center) instanceof BiomeMist && ((BiomeMist)Biome.getBiome(center)).isUpBiome();
	}

	protected abstract int getBorder(int center, int up, int upLeft, int left);

	protected abstract boolean getBool(int biome);
}