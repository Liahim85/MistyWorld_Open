package ru.liahim.mist.world.layer;

import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;
import ru.liahim.mist.init.ModBiomesIds;

public class GenLayerMistBiomes extends GenLayer {

	private final int deltaX;
	private final int deltaZ;
	private final int upBiomes[] = new int[] {
			ModBiomesIds.UP_FOREST, ModBiomesIds.UP_SWAMPY_FOREST, ModBiomesIds.UP_JUNGLE,
			ModBiomesIds.UP_DESERT, ModBiomesIds.UP_TAIGA
	};
	private final int downBiomes[] = new int[] {
			ModBiomesIds.BORDER_DOWN
	};

	public GenLayerMistBiomes(long seed, int deltaX, int deltaZ) {
		super(seed);
		this.deltaX = deltaX;
		this.deltaZ = deltaZ;
	}

	@Override
	public int[] getInts(int x, int z, int width, int height) {
		int dest[] = IntCache.getIntCache(width * height);
		for (int dz = 0; dz < height; dz++) {
			for (int dx = 0; dx < width; dx++) {
				initChunkSeed(dx + x, dz + z);
				if (((dx + x) >> 4 << 4 | deltaX) == dx + x && ((dz + z) >> 4 << 4 | deltaZ) == dz + z)
					dest[dx + dz * width] = downBiomes[nextInt(downBiomes.length)];
				else dest[dx + dz * width] = upBiomes[nextInt(upBiomes.length)];
			}
		}
		return dest;
	}
}