package ru.liahim.mist.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import ru.liahim.mist.api.MistTags;

public interface IMistFood {

	public static boolean showSaltyFood = false;

	public default boolean isFood(ItemStack stack) { return true; }
	public default boolean isEdible(ItemStack stack) { return true; }
	public PotionEffect[] getPotions(ItemStack stack);
	public float getProbability(ItemStack stack);
	public float getToxic(ItemStack stack);

	public static boolean hasSalt(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey(MistTags.saltTag);
	}
}