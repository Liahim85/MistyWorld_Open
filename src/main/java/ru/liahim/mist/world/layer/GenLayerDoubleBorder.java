package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public abstract class GenLayerDoubleBorder extends GenLayer {

	public GenLayerDoubleBorder(long seed, GenLayer genlayer) {
		super(seed);
		parent = genlayer;
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
			for (int xOut = 0; xOut < width + 1; xOut++) {
				int center = input[xOut + 1 + (zOut + 1) * widthOut];
				int left = input[xOut + 1 + (zOut + 0) * widthOut];
				int border = getBorder(center, left, upLeft, up);
				if (getBool(center, up, upLeft, left)) {
					if (xOut < width && zOut < height)
						output[xOut + zOut * width] = border;
					if (zOut > 0 && xOut < height)
						output[(xOut + 0) + (zOut - 1) * width] = border;
					if (xOut > 0 && zOut < width)
						output[(xOut - 1) + (zOut + 0) * width] = border;
					if (xOut > 0 && xOut < width && zOut > 0 && zOut < height)
						output[(xOut - 1) + (zOut - 1) * width] = border;
				} else output[xOut + zOut * width] = center;
				up = center;
				upLeft = left;
			}
		}
		return output;
	}

	protected abstract int getBorder(int center, int up, int upLeft, int left);

	protected abstract boolean getBool(int center, int up, int upLeft, int left);
}