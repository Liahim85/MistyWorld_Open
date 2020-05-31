package ru.liahim.mist.world.layer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.GenLayerVoronoiZoom;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModBiomesIds;
import ru.liahim.mist.world.biome.BiomeMist;

public class GenLayerMist {

	private static boolean shouldDraw = false;

	public static GenLayer[] initializeAllBiomeGenerators(long seed, WorldType worldType, String options) {

		ModBiomesIds.assignBiomeIds();

		int deltaX = (int)(seed >> 8 & 15L);
		int deltaZ = (int)(seed >> 16 & 15L);
		
		GenLayer biomes = new GenLayerMistBiomes(1L, deltaX, deltaZ);
		biomes = new GenLayerZoomMist(1000L, biomes, true);
		drawImage(512, biomes, "0zoom");
		biomes = new GenLayerSmoothMist(700L, biomes);
		biomes = new GenLayerSmoothMist(701L, biomes);
		biomes = new GenLayerSmoothMist(702L, biomes);
		drawImage(512, biomes, "1smooth");
		biomes = new GenLayerDiversify(1000L, biomes, true, deltaX, deltaZ);
		drawImage(512, biomes, "2diverse");
		biomes = new GenLayerZoomMist(1001L, biomes, true);
		biomes = new GenLayerSmoothMist(703L, biomes);
		drawImage(512, biomes, "3zoom_smooth");
		biomes = new GenLayerZoomMist(1002L, biomes, false);
		biomes = new GenLayerSmoothMist(704L, biomes);
		drawImage(512, biomes, "4zoom_smooth");
		biomes = new GenLayerBordersCenter(1000L, biomes);
		drawImage(512, biomes, "5down_center");
		biomes = new GenLayerDiversify(1001L, biomes);
		drawImage(512, biomes, "6diverse");
		biomes = new GenLayerFuzzyZoomMist(1000L, biomes);
		drawImage(512, biomes, "7fuzzy_zoom");
		biomes = new GenLayerDoCanyons(1000L, biomes);
		drawImage(512, biomes, "8canyons");
		biomes = new GenLayerSmoothMist(705L, biomes);
		drawImage(512, biomes, "9smooth");
		biomes = new GenLayerBordersUp(1000L, biomes);
		biomes = new GenLayerDiversifyMin(1000L, biomes);
		drawImage(512, biomes, "10border_diverse");
		biomes = new GenLayerDownSwampBorder(1001L, biomes);
		drawImage(512, biomes, "11border_swamp");
		biomes = new GenLayerFuzzyZoomMist(1001L, biomes);
		biomes = new GenLayerSmoothMist(706L, biomes);
		drawImage(512, biomes, "12zoom_smooth");
		biomes = new GenLayerBordersFinal(1000L, biomes, true);
		//
		biomes = new GenLayerDunesBorders(1005L, biomes);
		biomes = new GenLayerUpSwampBorders(1006L, biomes);
		//
		drawImage(512, biomes, "13borderUp");
		biomes = new GenLayerFuzzyZoomMist(1002L, biomes);
		biomes = new GenLayerBordersFinal(1001L, biomes, false);
		biomes = new GenLayerZoomMist(1003L, biomes, false);
		biomes = new GenLayerZoomMist(1004L, biomes, false);
		biomes = new GenLayerSeparator(1004L, biomes);
		drawImage(2048, biomes, "14zoom");
		// do "voronoi" zoom
		GenLayer genlayervoronoizoom = new GenLayerVoronoiZoom(10L, biomes);
		//drawImage(2048, genlayervoronoizoom, "9voronoi");
		biomes.initWorldGenSeed(seed);
		genlayervoronoizoom.initWorldGenSeed(seed);
		return (new GenLayer[] { biomes, genlayervoronoizoom });	
	}

	public static void drawImage(int size, GenLayer biomes, String name) {
		if (!shouldDraw)
			return;
		try {
			File outFile = new File(name + ".bmp");
			if (outFile.exists())
				return;
			int[] ints = biomes.getInts(0, 0, size, size);
			BufferedImage outBitmap = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = (Graphics2D)outBitmap.getGraphics();
			graphics.clearRect(0, 0, size, size);
			Mist.logger.info(name + ".bmp start draw");
			for (int x = 0; x < size; x++) {
				for (int z = 0; z < size; z++) {
					if (ints[x * size + z] != -1 && Biome.getBiome(ints[x * size + z]) instanceof BiomeMist) {
						graphics.setColor(Color.getColor("", ((BiomeMist)Biome.getBiome(ints[x * size + z])).getBiomeColor()));
						graphics.drawRect(x, z, 1, 1);
					}
				}
			}
			Mist.logger.info(name + ".bmp save");
			ImageIO.write(outBitmap, "BMP", outFile);
		}
		catch (Exception e) {
			Mist.logger.catching(e);
		}
	}
}