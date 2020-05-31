package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import ru.liahim.mist.init.ModBiomesIds;

public class GenLayerZoomMist extends GenLayer {

	boolean needBorder;

	public GenLayerZoomMist(long seed, GenLayer genlayer, boolean needBorder) {
		super(seed);
		super.parent = genlayer;
		this.needBorder = needBorder;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int x0 = x >> 1;
		int z0 = z >> 1;
		int widthIn = (width >> 1) + 2;
		int heightIn = (height >> 1) + 2;
		int point = ModBiomesIds.BORDER_DOWN;
		int border = ModBiomesIds.DOWN_CENTER;
		int[] input = this.parent.getInts(x0, z0, widthIn, heightIn);
		int widthOut = widthIn - 1 << 1;
		int heightOut = heightIn - 1 << 1;
		int[] output = IntCache.getIntCache(widthOut * heightOut);
		for (int zIn = 0; zIn < heightIn - 1; ++zIn) {
			int indexOut = (zIn << 1) * widthOut;
			int biome = input[(zIn + 0) * widthIn];
			int biomeU = input[(zIn + 1) * widthIn];
			for (int i = 0; i < widthIn - 1; ++i) {
				this.initChunkSeed(i + x0 << 1, zIn + z0 << 1);
				int biomeR = input[i + 1 + (zIn + 0) * widthIn];
				int biomeUR = input[i + 1 + (zIn + 1) * widthIn];
				if (biome == point && (needBorder || biomeU == border || biomeR == border || biomeUR == border)) {
					output[indexOut] = point;
					output[indexOut++ + widthOut] = border;
					output[indexOut] = border;
					output[indexOut++ + widthOut] = border;
				} else {
					output[indexOut] = biome;
					output[indexOut++ + widthOut] = ((needBorder || biome == border) && (biome == point || biomeU == point))
						? border : this.selectRandom(new int[] { biome, biomeU });
					output[indexOut] = ((needBorder || biome == border) && (biome == point || biomeR == point))
						? border : this.selectRandom(new int[] { biome, biomeR });
					output[indexOut++ + widthOut] = ((needBorder || biome == border) && (biome == point || biomeU == point || biomeR == point || biomeUR == point))
						? border : this.selectModeOrRandom(biome, biomeR, biomeU, biomeUR);
				}
				biome = biomeR;
				biomeU = biomeUR;
			}
		}
		int[] output2 = IntCache.getIntCache(width * height);
		for (int j = 0; j < height; ++j)
			System.arraycopy(output, (j + (z & 1)) * widthOut + (x & 1), output2, j * width, width);
		return output2;
	}
}