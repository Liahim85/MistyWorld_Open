package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import ru.liahim.mist.init.ModBiomesIds;

public class GenLayerSmoothMist extends GenLayer {

	public GenLayerSmoothMist(long seed, GenLayer genLayer) {
		super(seed);
		super.parent = genLayer;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int widthOut = width + 2;
		int heightOut = height + 2;
		int[] input = this.parent.getInts(x - 1, z - 1, widthOut, heightOut);
		int[] output = IntCache.getIntCache(width * height);
		int point = ModBiomesIds.BORDER_DOWN;
		for (int zOut = 0; zOut < height; ++zOut) {
			for (int xOut = 0; xOut < width; ++xOut) {
				int right = input[xOut + 0 + (zOut + 1) * widthOut];
				int left = input[xOut + 2 + (zOut + 1) * widthOut];
				int up = input[xOut + 1 + (zOut + 0) * widthOut];
				int down = input[xOut + 1 + (zOut + 2) * widthOut];
				int center = input[xOut + 1 + (zOut + 1) * widthOut];
				if (center != point) {
					if (right == left && up == down) {
						this.initChunkSeed(xOut + x, zOut + z);
						if (this.nextInt(2) == 0)
							center = right;
						else center = up;
					} else {
						if (right == left)
							center = right;
						if (up == down)
							center = up;
					}
				}
				output[xOut + zOut * width] = center;
			}
		}
		return output;
	}
}