package ru.liahim.mist.api.loottable;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import ru.liahim.mist.common.Mist;

public class LootTables {

	// Urn
	public static final ResourceLocation URN_ALTARS_DESERT_LOOT = register("urn_altars_desert_loot");
	public static final ResourceLocation URN_FUNERARY_LOOT = register("urn_funerary_loot");
	public static final ResourceLocation URN_MIXED_LOOT = register("urn_mixed_loot");
	public static final ResourceLocation URN_RICHES_LOOT = register("urn_riches_loot");
	public static final ResourceLocation URN_WELLS_LOOT = register("urn_wells_loot");
	public static final ResourceLocation URN_BASEMENTS_SWAMP_LOOT = register("urn_basements_swamp_loot");
	public static final ResourceLocation URN_BASEMENTS_JUNGLE_LOOT = register("urn_basements_jungle_loot");
	public static final ResourceLocation URN_BASEMENTS_FOREST_LOOT = register("urn_basements_forest_loot");
	public static final ResourceLocation URN_BASEMENTS_DESERT_LOOT = register("urn_basements_desert_loot");
	public static final ResourceLocation URN_BASEMENTS_COLD_LOOT = register("urn_basements_cold_loot");
	// Remains
	public static final ResourceLocation REMAINS_LOOT = register("remains_loot");
	// Furnace
	public static final ResourceLocation FURNACE_INPUT_LOOT = register("furnace_input_loot");
	public static final ResourceLocation FURNACE_OUTPUT_LOOT = register("furnace_output_loot");
	// Chest
	public static final ResourceLocation CHEST_BASEMENTS_SWAMP_LOOT = register("chest_basements_swamp_loot");
	public static final ResourceLocation CHEST_BASEMENTS_JUNGLE_LOOT = register("chest_basements_jungle_loot");
	public static final ResourceLocation CHEST_BASEMENTS_FOREST_LOOT = register("chest_basements_forest_loot");
	public static final ResourceLocation CHEST_BASEMENTS_DESERT_LOOT = register("chest_basements_desert_loot");
	public static final ResourceLocation CHEST_BASEMENTS_COLD_LOOT = register("chest_basements_cold_loot");
	// Entities
	public static final ResourceLocation MOSSLING_LOOT = register("entities/mossling");
	public static final ResourceLocation FOREST_RUNNER_LOOT = register("entities/forest_runner");
	public static final ResourceLocation MOMO_LOOT = register("entities/momo");
	public static final ResourceLocation LAGUH_LOOT = register("entities/laguh");
	public static final ResourceLocation PRICKLER_LOOT = register("entities/prickler");
	public static final ResourceLocation CARAVAN_LOOT = register("entities/caravan");
	public static final ResourceLocation WULDER_LOOT = register("entities/wulder");
	public static final ResourceLocation HORB_LOOT = register("entities/horb");
	public static final ResourceLocation SNIFF_LOOT = register("entities/sniff");
	public static final ResourceLocation SLOTH_LOOT = register("entities/sloth");
	public static final ResourceLocation MONK_LOOT = register("entities/monk");
	public static final ResourceLocation SALAM_LOOT = register("entities/salam");
	public static final ResourceLocation HULTER_LOOT = register("entities/hulter");
	public static final ResourceLocation STEGO_LOOT = register("entities/stego");

	public static final ResourceLocation GRAVE_BUG_LOOT = register("entities/grave_bug");
	public static final ResourceLocation FOREST_SPIDER_LOOT = register("entities/forest_spider");
	public static final ResourceLocation SWAMP_CRAB_LOOT = register("entities/swamp_crab");
	public static final ResourceLocation SNOW_FLEA_LOOT = register("entities/snow_flea");
	public static final ResourceLocation WOODLOUSE_LOOT = register("entities/woodlouse");
	public static final ResourceLocation CYCLOPS_LOOT = register("entities/cyclops");
	public static final ResourceLocation DESERT_FISH_LOOT = register("entities/desert_fish");

    private static ResourceLocation register(String id) {
        return LootTableList.register(new ResourceLocation(Mist.MODID, id));
    }

    public static void Init() {
    	LootFunctionManager.registerFunction(new EnchantWithChance.Serializer());
    	LootFunctionManager.registerFunction(new SetMetadataWithArray.Serializer());
    	LootConditionManager.registerCondition(new MistSkillCutting.Serializer());
    }
}