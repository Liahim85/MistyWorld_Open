package ru.liahim.mist.init;

import static ru.liahim.mist.api.biome.MistBiomes.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;
import ru.liahim.mist.world.biome.BiomeMistBorderDown;
import ru.liahim.mist.world.biome.BiomeMistBorderUp;
import ru.liahim.mist.world.biome.BiomeMistDownCenter;
import ru.liahim.mist.world.biome.BiomeMistDownForest;
import ru.liahim.mist.world.biome.BiomeMistDownNormal;
import ru.liahim.mist.world.biome.BiomeMistDownSwamp;
import ru.liahim.mist.world.biome.BiomeMistUpColdBase;
import ru.liahim.mist.world.biome.BiomeMistUpDesert;
import ru.liahim.mist.world.biome.BiomeMistUpSavanna;
import ru.liahim.mist.world.biome.BiomeMistUpForest;
import ru.liahim.mist.world.biome.BiomeMistUpJungle;
import ru.liahim.mist.world.biome.BiomeMistUpJungleEdge;
import ru.liahim.mist.world.biome.BiomeMistUpMarsh;
import ru.liahim.mist.world.biome.BiomeMistUpLowland;
import ru.liahim.mist.world.biome.BiomeMistUpMeadow;
import ru.liahim.mist.world.biome.BiomeMistUpSwamp;
import ru.liahim.mist.world.biome.BiomeMistUpSwampyForest;
import ru.liahim.mist.world.biome.BiomeMistUpSwampyMeadow;

public class ModBiomes {

	private static boolean hasAssignedBiomeID = false;
	private static boolean hasBiomeIdConflicts = false;
	private static float upHeight = MistWorld.baseHeightUp;
	private static float downHeight = MistWorld.baseHeightDown;
	private static final float upTemp = 0.1F;
	private static final int waterColor = 0xE0FFAE;
	private static final int waterColorLowland = 0xD9EC8D;
	private static final int waterColorMarsh = 0xD2D96D;
	private static final int waterColorSwamp = 0xC3B22C;
	private static final int waterColorCold = 0xD2D96D;

	public static void registerBiomes() {
		Mist.logger.info("Start to initialize Biomes");
		upMeadow = registerBiome("up_meadow", new BiomeMistUpMeadow((new BiomeMist.BiomeProperties("Meadow")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.02F).setTemperature(1.2F + upTemp).setRainfall(0.4F).setWaterColor(waterColor)).setBiomeColor(0xbced5d));
		upForest = registerBiome("up_forest", new BiomeMistUpForest((new BiomeMist.BiomeProperties("Forest")).setBaseHeight(upHeight + 0.15F).setHeightVariation(0.035F).setTemperature(1.1F + upTemp).setRainfall(0.5F).setWaterColor(waterColor), 4).setBiomeColor(0x6b9912));
		upDenseForest = registerBiome("up_dense_forest", new BiomeMistUpForest((new BiomeMist.BiomeProperties("Dense Forest")).setBaseHeight(upHeight + 0.15F).setHeightVariation(0.05F).setTemperature(1.0F + upTemp).setRainfall(0.6F).setWaterColor(waterColor), 6).setBiomeColor(0x547512));
		upSavanna = registerBiome("up_savanna", new BiomeMistUpSavanna((new BiomeMist.BiomeProperties("Savanna")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.04F).setTemperature(1.5F + upTemp).setRainfall(0.2F).setRainDisabled().setWaterColor(waterColor)).setBiomeColor(0xd4f664));
		upDesert = registerBiome("up_desert", new BiomeMistUpDesert((new BiomeMist.BiomeProperties("Desert")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.0F).setTemperature(2.0F + upTemp).setRainfall(0.0F).setRainDisabled().setWaterColor(waterColor), false).setBiomeColor(0xecff6c));
		upDunes = registerBiome("up_dunes", new BiomeMistUpDesert((new BiomeMist.BiomeProperties("Dunes")).setBaseHeight(upHeight + 0.2F).setHeightVariation(0.0F).setTemperature(1.8F + upTemp).setRainfall(0.1F).setRainDisabled().setWaterColor(waterColor), true).setBiomeColor(0xe9eb40));
		upSnowfields = registerBiome("up_snowfields", new BiomeMistUpColdBase((new BiomeMist.BiomeProperties("Snowfields")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.02F).setTemperature(0.0F + upTemp).setRainfall(0.0F).setSnowEnabled().setWaterColor(waterColorCold), 0, 0.002F, false).setBiomeColor(0xd0d7d7));
		upTaiga = registerBiome("up_taiga", new BiomeMistUpColdBase((new BiomeMist.BiomeProperties("Taiga")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.03F).setTemperature(0.0F + upTemp).setRainfall(0.2F).setSnowEnabled().setWaterColor(waterColorCold), 2, 0, true).setBiomeColor(0x78b496));
		upHillyTaiga = registerBiome("up_hilly_taiga", new BiomeMistUpColdBase((new BiomeMist.BiomeProperties("Hilly Taiga")).setBaseHeight(upHeight + 0.2F).setHeightVariation(0.07F).setTemperature(0.0F + upTemp).setRainfall(0.3F).setSnowEnabled().setWaterColor(waterColorCold), 3, 0.1F, true).setBiomeColor(0xbed5d5));
		upSwamp = registerBiome("up_swamp", new BiomeMistUpSwamp((new BiomeMist.BiomeProperties("Swampland")).setBaseHeight(upHeight - 0.0F).setHeightVariation(0.0F).setTemperature(0.8F + upTemp).setRainfall(0.9F).setWaterColor(waterColorSwamp)).setBiomeColor(0x518938));
		upSwampyMeadow = registerBiome("up_swampy_meadow", new BiomeMistUpSwampyMeadow((new BiomeMist.BiomeProperties("Swampy Meadow")).setBaseHeight(upHeight + 0.0F).setHeightVariation(0.01F).setTemperature(1.0F + upTemp).setRainfall(0.7F).setWaterColor(waterColorMarsh)).setBiomeColor(0xc9df89));
		upSwampyForest = registerBiome("up_swampy_forest", new BiomeMistUpSwampyForest((new BiomeMist.BiomeProperties("Swampy Forest")).setBaseHeight(upHeight + 0.0F).setHeightVariation(0.03F).setTemperature(0.6F + upTemp).setRainfall(0.9F).setWaterColor(waterColorMarsh)).setBiomeColor(0x3e6f28));
		upJungle = registerBiome("up_jungle", new BiomeMistUpJungle((new BiomeMist.BiomeProperties("Jungle")).setBaseHeight(upHeight + 0.2F).setHeightVariation(0.05F).setTemperature(1.4F + upTemp).setRainfall(0.9F).setWaterColor(waterColor), 3).setBiomeColor(0x11f600));
		upJungleEdge = registerBiome("up_jungle_edge", new BiomeMistUpJungleEdge((new BiomeMist.BiomeProperties("Jungle Edge")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.02F).setTemperature(1.5F + upTemp).setRainfall(0.8F).setWaterColor(waterColor), 3).setBiomeColor(0x95c539));
		upJungleHills = registerBiome("up_jungle_hills", new BiomeMistUpJungle((new BiomeMist.BiomeProperties("Jungle Hills")).setBaseHeight(upHeight + 0.25F).setHeightVariation(0.2F).setTemperature(1.4F + upTemp).setRainfall(0.9F).setWaterColor(waterColor), 3).setBiomeColor(0x889912));
		upLake = registerBiome("up_lake", new BiomeMistUpMeadow((new BiomeMist.BiomeProperties("Lake")).setBaseHeight(upHeight - 0.2F).setHeightVariation(0.0F).setTemperature(1.2F + upTemp).setRainfall(0.6F).setWaterColor(waterColor)).setBiomeColor(0x4084ee));
		upLowland = registerBiome("up_lowland", new BiomeMistUpLowland((new BiomeMist.BiomeProperties("Lowland")).setBaseHeight(upHeight - 0.15F).setHeightVariation(0.01F).setTemperature(0.6F + upTemp).setRainfall(0.9F).setWaterColor(waterColor)).setBiomeColor(0x4084bb));
		upColdLowland = registerBiome("up_cold_lowland", new BiomeMistUpColdBase((new BiomeMist.BiomeProperties("Cold Lowland")).setBaseHeight(upHeight - 0.15F).setHeightVariation(0.01F).setTemperature(-0.4F + upTemp).setRainfall(0.5F).setSnowEnabled().setWaterColor(waterColorCold), -999, 0, false).setBiomeColor(0x0064ff));
		upMarsh = registerBiome("up_marsh", new BiomeMistUpMarsh((new BiomeMist.BiomeProperties("Marsh")).setBaseHeight(upHeight - 0.15F).setHeightVariation(0.01F).setTemperature(0.6F + upTemp).setRainfall(0.9F).setWaterColor(waterColorLowland), false).setBiomeColor(0x4084eb));
		//upOasis = registerBiome("up_oasis", new BiomeMistUpPlainsCenter((new BiomeMist.BiomeProperties("Oasis")).setBaseHeight(upHeight + 0.0F).setHeightVariation(0.01F).setTemperature(1.6F + upTemp).setRainfall(0.2F).setRainDisabled().setWaterColor(waterColorSwamp)).setBiomeColor(0x7cff08));
		//upHotSprings = registerBiome("up_hot_springs", new BiomeMistUpPlainsCenter((new BiomeMist.BiomeProperties("Hot Springs")).setBaseHeight(upHeight + 0.0F).setHeightVariation(0.01F).setTemperature(0.4F + upTemp).setRainfall(0.5F).setSnowEnabled().setWaterColor(waterColorSwamp)).setBiomeColor(0xb9b48b));
		//upRockyDesert = registerBiome("up_rocky_desert", new BiomeMistUpRockyDesert((new BiomeMist.BiomeProperties("Rocky_Desert")).setBaseHeight(upHeight + 0.1F).setHeightVariation(0.01F).setTemperature(1.75F + upTemp).setRainfall(0.2F).setRainDisabled().setWaterColor(waterColorSwamp)).setBiomeColor(0xadaeac));
		//upGlacier = registerBiome("up_glacier", new BiomeMistUpColdCenter((new BiomeMist.BiomeProperties("Glacier")).setBaseHeight(upHeight + 0.2F).setHeightVariation(0.03F).setTemperature(-0.5F + upTemp).setRainfall(0.0F).setSnowEnabled().setWaterColor(waterColorSwamp), -999, 0, false).setBiomeColor(0x7dd6de));
		//upMushrooms = registerBiome("up_mushrooms", new BiomeMistUpSwampCenter((new BiomeMist.BiomeProperties("Mushrooms")).setBaseHeight(upHeight + 0.0F).setHeightVariation(0.03F).setTemperature(0.8F + upTemp).setRainfall(0.8F).setWaterColor(waterColorSwamp), -999, false).setBiomeColor(0x93709a));
		borderUpPlains = registerBiome("border_plains", new BiomeMistBorderUp((new BiomeMist.BiomeProperties("Plains Border")).setBaseHeight(upHeight).setHeightVariation(0.0F).setTemperature(1.2F + upTemp).setRainfall(0.4F).setWaterColor(waterColor), upMeadow).setBiomeColor(0xc5d8a0));
		borderUpDesert = registerBiome("border_desert", new BiomeMistBorderUp((new BiomeMist.BiomeProperties("Desert Border")).setBaseHeight(upHeight).setHeightVariation(0.0F).setTemperature(1.5F + upTemp).setRainfall(0.2F).setRainDisabled().setWaterColor(waterColor), upSavanna).setBiomeColor(0xdde6a4));
		borderUpCold = registerBiome("border_cold", new BiomeMistBorderUp((new BiomeMist.BiomeProperties("Cold Border")).setBaseHeight(upHeight).setHeightVariation(0.0F).setTemperature(0.0F + upTemp).setRainfall(0.0F).setSnowEnabled().setWaterColor(waterColor), upSnowfields).setBiomeColor(0xc8e4e7));
		borderUpSwamp = registerBiome("border_swamp", new BiomeMistBorderUp((new BiomeMist.BiomeProperties("Swampland Border")).setBaseHeight(upHeight).setHeightVariation(0.0F).setTemperature(1.0F + upTemp).setRainfall(0.5F).setWaterColor(waterColor), upSwampyMeadow).setBiomeColor(0xa9bd9f));
		borderUpJungle = registerBiome("border_jungle", new BiomeMistBorderUp((new BiomeMist.BiomeProperties("Jungle Border")).setBaseHeight(upHeight + 0.0F).setHeightVariation(0.0F).setTemperature(1.5F + upTemp).setRainfall(0.7F).setWaterColor(waterColor), upJungleEdge).setBiomeColor(0x6d7e66));
		borderDown = registerBiome("border_down", new BiomeMistBorderDown((new BiomeMist.BiomeProperties("Down Border")).setBaseHeight(downHeight + 0.3F).setHeightVariation(0.05F).setTemperature(1.5F).setRainfall(0.5F).setWaterColor(waterColor)).setBiomeColor(0xcab58d));
		separator = registerBiome("separator", new BiomeMistBorderDown((new BiomeMist.BiomeProperties("Border")).setBaseHeight(downHeight + 2.0F).setHeightVariation(0.0F).setTemperature(1.0F).setRainfall(0.5F).setWaterColor(waterColor)).setBiomeColor(0xffffff));
		down = registerBiome("down", new BiomeMistDownNormal((new BiomeMist.BiomeProperties("Down")).setBaseHeight(downHeight).setHeightVariation(0.05F).setTemperature(1.5F).setRainfall(0.5F)).setBiomeColor(0xc9a947));
		downForest = registerBiome("down_forest", new BiomeMistDownForest((new BiomeMist.BiomeProperties("Down Forest")).setBaseHeight(downHeight + 0.2F).setHeightVariation(0.05F).setTemperature(1.3F).setRainfall(0.7F)).setBiomeColor(0xac8232));
		downSwamp = registerBiome("down_swamp", new BiomeMistDownSwamp((new BiomeMist.BiomeProperties("Down Swamp")).setBaseHeight(downHeight - 0.0F).setHeightVariation(0.01F).setTemperature(1.7F).setRainfall(0.9F)).setBiomeColor(0xc3953d));
		downCenter = registerBiome("down_center", new BiomeMistDownCenter((new BiomeMist.BiomeProperties("Down Center")).setBaseHeight(downHeight + 0.1F).setHeightVariation(0.05F).setTemperature(1.9F).setRainfall(0.1F)).setBiomeColor(0x9b6b0f));
		registerWithBiomeDictionary();
		Mist.logger.info("Finished initializing Biomes");
	}

	private static Biome registerBiome(String name, Biome biome) {
		ResourceLocation RL = new ResourceLocation(Mist.MODID, name);
		biome.setRegistryName(RL);
		ForgeRegistries.BIOMES.register(biome);
		return ForgeRegistries.BIOMES.getValue(RL);
	}

	public static void registerWithBiomeDictionary() {
		BiomeDictionary.addTypes(upMeadow, Type.PLAINS);
		BiomeDictionary.addTypes(upForest, Type.FOREST);
		BiomeDictionary.addTypes(upDenseForest, Type.FOREST, Type.DENSE, Type.WET);
		BiomeDictionary.addTypes(upSavanna, Type.SAVANNA, Type.HOT, Type.DRY);
		BiomeDictionary.addTypes(upDesert, Type.SANDY, Type.HOT, Type.DRY);
		BiomeDictionary.addTypes(upDunes, Type.SANDY, Type.HOT, Type.DRY, Type.HILLS);
		BiomeDictionary.addTypes(upSnowfields, Type.PLAINS, Type.SNOWY, Type.COLD, Type.CONIFEROUS);
		BiomeDictionary.addTypes(upTaiga, Type.FOREST, Type.SNOWY, Type.COLD, Type.CONIFEROUS);
		BiomeDictionary.addTypes(upHillyTaiga, Type.FOREST, Type.SNOWY, Type.COLD, Type.CONIFEROUS, Type.HILLS);
		BiomeDictionary.addTypes(upSwamp, Type.SWAMP, Type.WET);
		BiomeDictionary.addTypes(upSwampyMeadow, Type.SWAMP, Type.PLAINS, Type.WET);
		BiomeDictionary.addTypes(upSwampyForest, Type.SWAMP, Type.FOREST, Type.WET);
		BiomeDictionary.addTypes(upJungle, Type.FOREST, Type.JUNGLE, Type.WET, Type.LUSH);
		BiomeDictionary.addTypes(upJungleEdge, Type.JUNGLE, Type.WET);
		BiomeDictionary.addTypes(upJungleHills, Type.FOREST, Type.JUNGLE, Type.HILLS, Type.LUSH);
		BiomeDictionary.addTypes(upLake, Type.RIVER, Type.WET);
		BiomeDictionary.addTypes(upLowland, Type.JUNGLE, Type.WET);
		BiomeDictionary.addTypes(upColdLowland, Type.RIVER, Type.WET, Type.COLD, Type.SNOWY);
		BiomeDictionary.addTypes(upMarsh, Type.RIVER, Type.WET);
		//BiomeDictionary.addTypes(upOasis, Type.HOT, Type.LUSH);
		//BiomeDictionary.addTypes(upHotSprings, Type.COLD, Type.SPARSE, Type.WET, Type.SNOWY);
		//BiomeDictionary.addTypes(upRockyDesert, Type.HOT, Type.WASTELAND, Type.DRY, Type.SANDY);
		//BiomeDictionary.addTypes(upGlacier, Type.HILLS, Type.COLD, Type.SNOWY, Type.WASTELAND);
		//BiomeDictionary.addTypes(upMushrooms, Type.MUSHROOM, Type.WET);
		BiomeDictionary.addTypes(borderUpPlains, Type.PLAINS, Type.WASTELAND);
		BiomeDictionary.addTypes(borderUpDesert, Type.SANDY, Type.HOT, Type.DRY, Type.WASTELAND);
		BiomeDictionary.addTypes(borderUpCold, Type.SNOWY, Type.COLD, Type.WASTELAND);
		BiomeDictionary.addTypes(borderUpSwamp, Type.WET, Type.WASTELAND);
		BiomeDictionary.addTypes(borderUpJungle, Type.JUNGLE, Type.WET, Type.WASTELAND);
		BiomeDictionary.addTypes(borderDown, Type.DEAD, Type.SPOOKY);
		BiomeDictionary.addTypes(separator, Type.DEAD, Type.SPOOKY);
		BiomeDictionary.addTypes(down, Type.DEAD, Type.SPOOKY);
		BiomeDictionary.addTypes(downForest, Type.DEAD, Type.SPOOKY);
		BiomeDictionary.addTypes(downSwamp, Type.DEAD, Type.SPOOKY);
		BiomeDictionary.addTypes(downCenter, Type.DEAD, Type.SPOOKY);
	}
}