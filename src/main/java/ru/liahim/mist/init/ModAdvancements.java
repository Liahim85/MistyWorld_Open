package ru.liahim.mist.init;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ru.liahim.mist.api.advancement.criterion.*;
import ru.liahim.mist.common.Mist;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class ModAdvancements {

	public static final OpenPortalTrigger OPEN_PORTAL = registerCriteriaTrigger(new OpenPortalTrigger());
	public static final FogDamageTrigger FOG_DAMAGE = registerCriteriaTrigger(new FogDamageTrigger());
	public static final TwoFloatTrigger FOG_EFFECT = registerCriteriaTrigger(new TwoFloatTrigger(new ResourceLocation(Mist.MODID, "fog_effect"), "toxic", "pollution"));
	public static final ItemStackTrigger ITEM_CRAFTED = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "item_crafted")));
	public static final ItemStackTrigger ITEM_SMELTED = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "item_smelted")));
	public static final ItemStackTrigger ITEM_SMELTED_MIST = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "item_smelted_mist")));
	public static final ItemStackTrigger ITEM_PICKUP = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "item_pickup")));
	public static final ItemStackTrigger PUT_FILTER = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "put_filter")));
	public static final CarvingTrigger CARVING = registerCriteriaTrigger(new CarvingTrigger());
	public static final IBlockStateTrigger MULCH_PLACED = registerCriteriaTrigger(new IBlockStateTrigger(new ResourceLocation(Mist.MODID, "mulch_placed")));
	public static final ItemStackTrigger COMPOST = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "compost")));
	public static final ItemStackTrigger LATEX = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "latex")));
	public static final ItemStackTrigger CLEAN_UP = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "clean_up")));
	public static final IBlockStateTrigger FERTILE = registerCriteriaTrigger(new IBlockStateTrigger(new ResourceLocation(Mist.MODID, "fertile")));
	public static final IBlockStateTrigger REMAINS = registerCriteriaTrigger(new IBlockStateTrigger(new ResourceLocation(Mist.MODID, "remains")));
	public static final ItemStackAndFloatTrigger CONSUME_TOXIC = registerCriteriaTrigger(new ItemStackAndFloatTrigger(new ResourceLocation(Mist.MODID, "consume_toxic")));
	public static final ItemStackAndFloatTrigger GLASS_CONTAINER = registerCriteriaTrigger(new ItemStackAndFloatTrigger(new ResourceLocation(Mist.MODID, "glass_container")));
	public static final AnimalTrigger TAME_ANIMAL = registerCriteriaTrigger(new AnimalTrigger(new ResourceLocation(Mist.MODID, "tame_animal")));
	public static final AnimalTrigger RIDING_ANIMAL = registerCriteriaTrigger(new AnimalTrigger(new ResourceLocation(Mist.MODID, "riding")));
	public static final ItemStackTrigger STONE_MINED = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "stone_mined")));
	public static final ItemStackTrigger BRICK = registerCriteriaTrigger(new ItemStackTrigger(new ResourceLocation(Mist.MODID, "brick")));

	public static void load() {}

	/**
	 * Call the private static `register` from @link{CriteriaTriggers}
	 * @param criterion The criterion.
	 * @param <T> The criterion type.
	 * @return The registered instance.
	 */
	private static <T extends ICriterionTrigger> T registerCriteriaTrigger(T criterion) {
		Method method = ReflectionHelper.findMethod(CriteriaTriggers.class,
				"register", "func_192118_a", ICriterionTrigger.class);
		try {
			return (T) method.invoke(null, criterion);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}