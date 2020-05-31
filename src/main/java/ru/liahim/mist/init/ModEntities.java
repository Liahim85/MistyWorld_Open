package ru.liahim.mist.init;

import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.*;
import ru.liahim.mist.entity.item.EntityMistPainting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;

public class ModEntities {

	public static void registerEntities() {
		int id = 0;
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "mossling"), EntityMossling.class, "mist.mossling", id++, Mist.instance, 80, 3, true, 0x595e30, 0xa99d79);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "forest_runner"), EntityForestRunner.class, "mist.forest_runner", id++, Mist.instance, 80, 3, true, 0xa18754, 0x806736);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "momo"), EntityMomo.class, "mist.momo", id++, Mist.instance, 80, 3, true, 0x7d9999, 0x829e9e);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "barvog"), EntityBarvog.class, "mist.barvog", id++, Mist.instance, 80, 3, true, 0xab633f, 0x9b4c2b);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "prickler"), EntityPrickler.class, "mist.prickler", id++, Mist.instance, 80, 3, true, 0x515754, 0xa1ada9);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "caravan"), EntityCaravan.class, "mist.caravan", id++, Mist.instance, 80, 3, true, 0xceb890, 0xebc898);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "wulder"), EntityWulder.class, "mist.wulder", id++, Mist.instance, 80, 3, true, 0x77534a, 0xba8977);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "horb"), EntityHorb.class, "mist.horb", id++, Mist.instance, 80, 3, true, 0x7d3e4a, 0xf4d3d8);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "sniff"), EntitySniff.class, "mist.sniff", id++, Mist.instance, 80, 3, true, 0x9984a8, 0x9984a8);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "sloth"), EntitySloth.class, "mist.sloth", id++, Mist.instance, 80, 3, true, 0x3c392d, 0x967846);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "monk"), EntityMonk.class, "mist.monk", id++, Mist.instance, 80, 3, true, 0x6e4d3c, 0xccb183);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "galaga"), EntityGalaga.class, "mist.galaga", id++, Mist.instance, 80, 3, true, 0x94693e, 0x7978ab);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "hulter"), EntityHulter.class, "mist.hulter", id++, Mist.instance, 80, 3, true, 0x385754, 0x304d48);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "brachiodon"), EntityBrachiodon.class, "mist.brachiodon", id++, Mist.instance, 80, 3, true, 0xbea584, 0xc48e62);
		// Hostile
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "grave_bug"), EntityGraveBug.class, "mist.grave_bug", id++, Mist.instance, 80, 3, true, 0x3a5352, 0x88aea2);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "forest_spider"), EntityForestSpider.class, "mist.forest_spider", id++, Mist.instance, 80, 3, true, 0xb6917a, 0x8f5b4c);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "swamp_crab"), EntitySwampCrab.class, "mist.swamp_crab", id++, Mist.instance, 80, 3, true, 0x566b5b, 0xafb87b);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "snow_flea"), EntitySnowFlea.class, "mist.snow_flea", id++, Mist.instance, 80, 3, true, 0xe2e2e2, 0xf88f8f);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "woodlouse"), EntityWoodlouse.class, "mist.woodlouse", id++, Mist.instance, 80, 3, true, 0x55575e, 0xa4a7bb);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "cyclops"), EntityCyclops.class, "mist.cyclops", id++, Mist.instance, 80, 3, true, 0xc78350, 0xde6f3c);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "desert_fish"), EntityDesertFish.class, "mist.desert_fish", id++, Mist.instance, 80, 3, true, 0x9aa1a4, 0x949b9d);
		// EntytiItem
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "painting"), EntityMistPainting.class, "mist.painting", id++, Mist.instance, 70, Integer.MAX_VALUE, false);
		EntityRegistry.registerModEntity(new ResourceLocation(Mist.MODID, "rubber_ball"), EntityRubberBall.class, "mist.rubber_ball", id++, Mist.instance, 80, 1, true);
	}
}