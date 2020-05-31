package ru.liahim.mist.init;

import static ru.liahim.mist.api.biome.MistBiomes.*;
import net.minecraft.world.biome.Biome;

public class ModBiomesIds {

	//Forest
	public static int UP_MEADOW;
	public static int UP_FOREST;
	public static int UP_DENSE_FOREST;
	//Desert
	public static int UP_SAVANNA;
	public static int UP_DESERT;
	public static int UP_DUNES;
	//Cold
	public static int UP_SNOWFIELDS;
	public static int UP_TAIGA;
	public static int UP_HILLY_TAIGA;
	//Swamp
	public static int UP_SWAMP;
	public static int UP_SWAMPY_MEADOW;
	public static int UP_SWAMPY_FOREST;
	//Jungle
	public static int UP_JUNGLE;
	public static int UP_JUNGLE_EDGE;
	public static int UP_JUNGLE_HILLS;
	//Technical
	public static int UP_LAKE;
	public static int UP_LOWLAND;
	public static int UP_COLD_LOWLAND;
	public static int UP_MARSH;
	//Rare
	//public static int UP_OASIS;
	//public static int UP_HOT_SPRINGS;
	//public static int UP_ROCKY_DESERT;
	//public static int UP_GLACIER;
	//public static int UP_MUSHROOMS;
	//Borders
	public static int BORDER_UP_PLAINS;
	public static int BORDER_UP_DESERT;
	public static int BORDER_UP_COLD;
	public static int BORDER_UP_SWAMP;
	public static int BORDER_UP_JUNGLE;
	public static int BORDER_DOWN;
	public static int SEPARATOR;
	//Down
	public static int DOWN;
	public static int DOWN_FOREST;
	public static int DOWN_SWAMP;
	public static int DOWN_CENTER;

	public static void assignBiomeIds() {
		//Forest
		UP_MEADOW = Biome.getIdForBiome(upMeadow);					//1 - (1.2, 0.4) (32.5, 118.861) луг (вкрапления опушки, вкрапления каменистой пустыни, вкрапления озёр с границей из заболоченного луга или пляжа)
		UP_FOREST = Biome.getIdForBiome(upForest);					//2 - (1.1, 0.5) (40.844, 128.695) лес (вкрапления опушки, вкрапления луга с границей из опушки)
		UP_DENSE_FOREST = Biome.getIdForBiome(upDenseForest);		//3 - (1.0, 0.6) (49.918, 132.758) густой лес (вкрапления леса, вкрапления заболоченного леса, вкрапления озёр с переменной границей из заболоченного луга)
		//Desert
		UP_SAVANNA = Biome.getIdForBiome(upSavanna);				//4 - (1.5, 0.2) (11.853, 103.677) саванна (вкрапления каменистой пустыни, вкрапления пустыни с границей из каменистой пустыни)
		UP_DESERT = Biome.getIdForBiome(upDesert);					//5 - (2.0, 0.0) (-14.933, 85.917) пустыня (вкрапления каменистой пустыни)
		UP_DUNES = Biome.getIdForBiome(upDunes);					//6 - (1.8, 0.1) (-2.929, 94.236) дюны (вкрапления оазиса)
		//Cold
		UP_SNOWFIELDS = Biome.getIdForBiome(upSnowfields);			//7 - (0.0, 0.0) (49.294, 112.631) заснеженная равнина (вкрапления тайги, вкрапления ледника)
		UP_TAIGA = Biome.getIdForBiome(upTaiga);					//8 - (0.0, 0.2) (58.437, 123.10) тайга (вкрапления заснеженной равнины)
		UP_HILLY_TAIGA = Biome.getIdForBiome(upHillyTaiga);			//9 - (0.0, 0.3) (64.436, 129.456) заснеженные холмы с растительностью (вкрапления ледника, вкрапления горячих источников)
		//Swamp
		UP_SWAMP = Biome.getIdForBiome(upSwamp);					//10 - (0.8, 0.9) (72.407, 151.92) болото (вкрапления заболоченного луга, вкрапления грибного биома, вкрапления озёр с переменной границей из заболоченного луга)
		UP_SWAMPY_MEADOW = Biome.getIdForBiome(upSwampyMeadow);		//11 - (1.0, 0.7) (54.99, 138.201) заболоченный луг (вкрапления болота, вкрапления глиняной поляны, вкрапления озёр)
		UP_SWAMPY_FOREST = Biome.getIdForBiome(upSwampyForest);		//12 - (0.6, 0.9) (78.597, 154.63) заболоченный лес (вкрапления заболоченного луга, вкрапления густого леса, вкрапления грибного биома, вкрапления озёр)
		//Jungle
		UP_JUNGLE = Biome.getIdForBiome(upJungle);					//13 - (1.4, 0.9) (53.818, 144.177) джунгли (вкрапления озёр)
		UP_JUNGLE_EDGE = Biome.getIdForBiome(upJungleEdge);			//14 - (1.5, 0.8) (45.285, 137.02) окраина джунглей (вкрапления луга, вкрапления озёр)
		UP_JUNGLE_HILLS = Biome.getIdForBiome(upJungleHills);		//15 - (1.4, 0.9) (54.915, 144.424) холмистые джунгли (вкрапления озёр)
		//Technical
		UP_LAKE = Biome.getIdForBiome(upLake);						//20 - (1.2, 0.4) (32.5, 118.861) озеро !!! (технический)
		UP_LOWLAND = Biome.getIdForBiome(upLowland);				//22 - (0.6, 0.9) (78.609, 154.668) заболоченная низина !!! (технический)
		UP_COLD_LOWLAND = Biome.getIdForBiome(upColdLowland);		//23 - (-0.4, 0.5) (87.311, 145.609) заснеженная низина !!! (технический)
		UP_MARSH = Biome.getIdForBiome(upMarsh);					//22 - (0.6, 0.9) (78.609, 154.668) низина !!! (технический)
		//Rare
		// UP_OASIS = Biome.getIdForBiome(upOasis);					//16 - (1.6, 0.2) оазис !!! (редкий)
		// UP_HOT_SPRINGS = Biome.getIdForBiome(upHotSprings);		//17? - (0.4, 0.5) горячие источники !!! (редкий)
		// UP_ROCKY_DESERT = Biome.getIdForBiome(upRockyDesert);	//18? - (1.75, 0.2) каменистая пустыня !!! (редкий)
		// UP_GLACIER = Biome.getIdForBiome(upGlacier);				//19 - (-0.5, 0.0) ледник !!! (редкий)
		// UP_MUSHROOMS = Biome.getIdForBiome(upMushrooms);			//21? - (0.8, 0.8) грибной биом !!! (редкий)
		//Borders
		BORDER_UP_PLAINS = Biome.getIdForBiome(borderUpPlains);		//23 - (1.2, 0.4) (32.431, 118.915) граница умеренных областуй !!! (повтор границы у поляны и леса)
		BORDER_UP_DESERT = Biome.getIdForBiome(borderUpDesert);		//24 - (1.5, 0.1) (11.815, 103.73) граница жаркой области
		BORDER_UP_COLD = Biome.getIdForBiome(borderUpCold);			//25 - (0.0, 0.0) (49.294, 112.631) граница холодной области
		BORDER_UP_SWAMP = Biome.getIdForBiome(borderUpSwamp);		//26 - (1.0, 0.5) (44.22, 126.99) граница влажной области
		BORDER_UP_JUNGLE = Biome.getIdForBiome(borderUpJungle);		//27 - (1.5, 0.7) (39.567, 131.544) граница тропической области
		BORDER_DOWN = Biome.getIdForBiome(borderDown);				//28 - (1.5, 0.5) нижняя граница
		SEPARATOR = Biome.getIdForBiome(separator);					//33 - (1.0, 0.5) разделитель
		//Down
		DOWN = Biome.getIdForBiome(down);							//29 - (1.5, 0.5) нижняя равнина
		DOWN_FOREST = Biome.getIdForBiome(downForest);				//30 - (1.3, 0.7) нижний лес
		DOWN_SWAMP = Biome.getIdForBiome(downSwamp);				//31 - (1.7, 0.9) нижнее болото
		DOWN_CENTER = Biome.getIdForBiome(downCenter);				//32 - (1.9, 0.7) центр
	}
}