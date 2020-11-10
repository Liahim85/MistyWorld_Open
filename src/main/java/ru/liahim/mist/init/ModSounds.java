package ru.liahim.mist.init;

import static ru.liahim.mist.api.sound.MistSounds.*;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.liahim.mist.common.Mist;

public class ModSounds {

	public static void registerSounds() {

		PLAYER_GAS_ANALYZER = registerSoundEvent("mist_gas_analyzer");

		BLOCK_URN_OPEN = registerSoundEvent("mist_urn_open");
		BLOCK_URN_CLOSE = registerSoundEvent("mist_urn_close");
		BLOCK_URN_BREAK = registerSoundEvent("mist_urn_break");
		BLOCK_STONE_BREAK = registerSoundEvent("mist_stone_break");
		BLOCK_WOOD_CREAK = registerSoundEvent("mist_wood_creak");

		ENTITY_CARAVAN_AMBIENT = registerSoundEvent("mist_caravan_ambient");
		ENTITY_CARAVAN_HURT = registerSoundEvent("mist_caravan_hurt");
		ENTITY_CARAVAN_DEATH = registerSoundEvent("mist_caravan_death");

		ENTITY_MOSSLING_AMBIENT = registerSoundEvent("mist_mossling_ambient");
		ENTITY_MOSSLING_HURT = registerSoundEvent("mist_mossling_hurt");
		ENTITY_MOSSLING_DEATH = registerSoundEvent("mist_mossling_death");

		ENTITY_BARVOG_AMBIENT = registerSoundEvent("mist_barvog_ambient");
		ENTITY_BARVOG_HURT = registerSoundEvent("mist_barvog_hurt");
		ENTITY_BARVOG_DEATH = registerSoundEvent("mist_barvog_death");

		ENTITY_MOMO_AMBIENT = registerSoundEvent("mist_momo_ambient");
		ENTITY_MOMO_HURT = registerSoundEvent("mist_momo_hurt");
		ENTITY_MOMO_DEATH = registerSoundEvent("mist_momo_death");

		ENTITY_MONK_AMBIENT = registerSoundEvent("mist_monk_ambient");
		ENTITY_MONK_HURT = registerSoundEvent("mist_monk_hurt");
		ENTITY_MONK_DEATH = registerSoundEvent("mist_monk_death");
		ENTITY_MONK_WARNING = registerSoundEvent("mist_monk_warning");

		ENTITY_HULTER_AMBIENT = registerSoundEvent("mist_hulter_ambient");
		ENTITY_HULTER_HURT = registerSoundEvent("mist_hulter_hurt");
		ENTITY_HULTER_DEATH = registerSoundEvent("mist_hulter_death");
		ENTITY_HULTER_WARNING = registerSoundEvent("mist_hulter_warning");

		ENTITY_FOREST_RUNNER_AMBIENT = registerSoundEvent("mist_forest_runner_ambient");
		ENTITY_FOREST_RUNNER_HURT = registerSoundEvent("mist_forest_runner_hurt");
		ENTITY_FOREST_RUNNER_DEATH = registerSoundEvent("mist_forest_runner_death");

		ENTITY_WULDER_AMBIENT = registerSoundEvent("mist_wulder_ambient");
		ENTITY_WULDER_HURT = registerSoundEvent("mist_wulder_hurt");
		ENTITY_WULDER_DEATH = registerSoundEvent("mist_wulder_death");

		ENTITY_PRICKLER_AMBIENT = registerSoundEvent("mist_prickler_ambient");
		ENTITY_PRICKLER_HURT = registerSoundEvent("mist_prickler_hurt");
		ENTITY_PRICKLER_DEATH = registerSoundEvent("mist_prickler_death");

		ENTITY_HORB_AMBIENT = registerSoundEvent("mist_horb_ambient");
		ENTITY_HORB_HURT = registerSoundEvent("mist_horb_hurt");
		ENTITY_HORB_DEATH = registerSoundEvent("mist_horb_death");

		ENTITY_GALAGA_AMBIENT = registerSoundEvent("mist_galaga_ambient");
		ENTITY_GALAGA_HURT = registerSoundEvent("mist_galaga_hurt");
		ENTITY_GALAGA_DEATH = registerSoundEvent("mist_galaga_death");

		ENTITY_SLOTH_AMBIENT = registerSoundEvent("mist_sloth_ambient");
		ENTITY_SLOTH_AMBIENT_CHILD = registerSoundEvent("mist_sloth_ambient_child");
		ENTITY_SLOTH_HURT = registerSoundEvent("mist_sloth_hurt");
		ENTITY_SLOTH_DEATH = registerSoundEvent("mist_sloth_death");

		ENTITY_BRACHIODON_AMBIENT = registerSoundEvent("mist_brachiodon_ambient");
		ENTITY_BRACHIODON_HURT = registerSoundEvent("mist_brachiodon_hurt");
		ENTITY_BRACHIODON_DEATH = registerSoundEvent("mist_brachiodon_death");

		ENTITY_SNIFF_AMBIENT = registerSoundEvent("mist_sniff_ambient");
		ENTITY_SNIFF_HURT = registerSoundEvent("mist_sniff_hurt");
		ENTITY_SNIFF_DEATH = registerSoundEvent("mist_sniff_death");

		ENTITY_SWAMP_CRAB_AMBIENT = registerSoundEvent("mist_swamp_crab_ambient");
		ENTITY_SWAMP_CRAB_HURT = registerSoundEvent("mist_swamp_crab_hurt");
		ENTITY_SWAMP_CRAB_DEATH = registerSoundEvent("mist_swamp_crab_death");
		ENTITY_SWAMP_CRAB_STEP = registerSoundEvent("mist_swamp_crab_step");

		ENTITY_GRAVE_BUG_AMBIENT = registerSoundEvent("mist_grave_bug_ambient");
		ENTITY_GRAVE_BUG_HURT = registerSoundEvent("mist_grave_bug_hurt");
		ENTITY_GRAVE_BUG_DEATH = registerSoundEvent("mist_grave_bug_death");
		ENTITY_GRAVE_BUG_STEP = registerSoundEvent("mist_grave_bug_step");

		ENTITY_FOREST_SPIDER_AMBIENT = registerSoundEvent("mist_forest_spider_ambient");
		ENTITY_FOREST_SPIDER_HURT = registerSoundEvent("mist_forest_spider_hurt");
		ENTITY_FOREST_SPIDER_DEATH = registerSoundEvent("mist_forest_spider_death");
		ENTITY_FOREST_SPIDER_STEP = registerSoundEvent("mist_forest_spider_step");

		ENTITY_WOODLOUSE_AMBIENT = registerSoundEvent("mist_woodlouse_ambient");
		ENTITY_WOODLOUSE_HURT = registerSoundEvent("mist_woodlouse_hurt");
		ENTITY_WOODLOUSE_DEATH = registerSoundEvent("mist_woodlouse_death");

		ENTITY_SNOW_FLEA_AMBIENT = registerSoundEvent("mist_snow_flea_ambient");
		ENTITY_SNOW_FLEA_HURT = registerSoundEvent("mist_snow_flea_hurt");
		ENTITY_SNOW_FLEA_DEATH = registerSoundEvent("mist_snow_flea_death");
		ENTITY_SNOW_FLEA_STEP = registerSoundEvent("mist_snow_flea_step");
		ENTITY_SNOW_FLEA_FLAY = registerSoundEvent("mist_snow_flea_flay");

		ENTITY_CYCLOPS_AMBIENT = registerSoundEvent("mist_cyclops_ambient");
		ENTITY_CYCLOPS_HURT = registerSoundEvent("mist_cyclops_hurt");
		ENTITY_CYCLOPS_DEATH = registerSoundEvent("mist_cyclops_death");
		ENTITY_CYCLOPS_STEP = registerSoundEvent("mist_cyclops_step");

		ENTITY_DESERT_FISH_AMBIENT = registerSoundEvent("mist_desert_fish_ambient");
		ENTITY_DESERT_FISH_HURT = registerSoundEvent("mist_desert_fish_hurt");
		ENTITY_DESERT_FISH_DEATH = registerSoundEvent("mist_desert_fish_death");
		ENTITY_DESERT_FISH_STEP = registerSoundEvent("mist_desert_fish_step");
	
		SKY_SOUND = registerSoundEvent("mist_sky_sound");
		SKY_BOOM = registerSoundEvent("mist_sky_boom");

		//UP_MUSIC = registerSoundEvent("mist_up_music");
		//DOWN_MUSIC = registerSoundEvent("mist_down_music");
	}

	public static SoundEvent registerSoundEvent(String name) {
		ResourceLocation RL = new ResourceLocation(Mist.MODID, name);
		ForgeRegistries.SOUND_EVENTS.register(new SoundEvent(RL).setRegistryName(RL));
		return ForgeRegistries.SOUND_EVENTS.getValue(RL);
	}
}