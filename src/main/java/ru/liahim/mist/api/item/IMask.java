package ru.liahim.mist.api.item;

import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.util.ItemStackMapKey;

import java.util.HashMap;
import javax.annotation.Nonnull;

import com.google.common.collect.Maps;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec2f;

public interface IMask {

	public static final HashMap<ItemStackMapKey, Vec2f> respirators = Maps.<ItemStackMapKey, Vec2f>newHashMap(/*new ItemStackMapKey(new ItemStack(Items.IRON_HELMET))*/);
	public static final HashMap<ItemStackMapKey, Integer> filters = Maps.<ItemStackMapKey, Integer>newHashMap();

	/**This method is called once per tick if the mask is being worn by a player.*/
	public default void onWornTick(ItemStack stack, EntityLivingBase player) {}

	/**This method is called when the mask is equipped by a player.*/
	public default void onEquippedMask(ItemStack stack, EntityLivingBase player) {}
	
	public static void onEquipped(ItemStack stack, EntityLivingBase player) {
		if (!stack.isEmpty() && stack.getItem() instanceof IMask) ((IMask)stack.getItem()).onEquippedMask(stack, player);
	}

	/**This method is called when the mask is unequipped by a player.*/
	public default void onUnequippedMask(ItemStack stack, EntityLivingBase player) {}

	public static void onUnequipped(ItemStack stack, EntityLivingBase player) {
		if (!stack.isEmpty() && stack.getItem() instanceof IMask) ((IMask)stack.getItem()).onUnequippedMask(stack, player);
	}

	/**Can this mask be placed in a mask slot.*/
	public default boolean canEquipMask(@Nonnull ItemStack stack, EntityLivingBase player) {
		return !isMaskInHelmetSlot(player);
	}

	public static boolean canEquip(ItemStack stack, EntityLivingBase player) {
		if (isMask(stack)) {
			if (stack.getItem() instanceof IMask) return ((IMask)stack.getItem()).canEquipMask(stack, player);
			else return !isMaskInHelmetSlot(player);
		} else return false;
	}

	/**Can this mask be removed from a mask slot.*/
	public default boolean canUnequipMask(ItemStack stack, EntityLivingBase player) {
		return true;
	}

	public static boolean canUnequip(ItemStack stack, EntityLivingBase player) {
		if (!stack.isEmpty() && stack.getItem() instanceof IMask) return ((IMask)stack.getItem()).canUnequipMask(stack, player);
		return true;
	}

	/**
	 * Will mask automatically sync to client if a change is detected in its NBT or damage values?
	 * Default is off, so override and set to true if you want to auto sync.
	 * This sync is not instant, but occurs every 10 ticks (0.5 seconds).
	 */
	public default boolean willAutoSync(ItemStack stack, EntityLivingBase player) {
		return false;
	}

	public default boolean isRespirator() {
		return false;
	}

	public static boolean isRespirator(ItemStack stack) {
		return stack.isEmpty() ? false : stack.getItem() instanceof IMask ? ((IMask)stack.getItem()).isRespirator() : respirators.containsKey(new ItemStackMapKey(stack));
	}

	public default boolean canEat() {
		return false;
	}

	public static boolean canEat(ItemStack stack) {
		if (stack.isEmpty()) return true;
		else if (stack.getItem() instanceof IMask) return ((IMask)stack.getItem()).canEat();
		else {
			ItemStackMapKey key = new ItemStackMapKey(stack);
			if (!respirators.containsKey(key)) return true;
			else return respirators.get(key).y == 1;
		}
	}

	/**Returns the respirator impermeability in percent.*/
	public default float getImpermeability() {
		return 90; //85 for open
	}

	/**Returns the respirator impermeability in percent.*/
	public static float getImpermeability(ItemStack mask) {
		float i;
		if (mask.getItem() instanceof IMask) i = ((IMask)mask.getItem()).getImpermeability();
		else {
			ItemStackMapKey key = new ItemStackMapKey(mask);
			if (!respirators.containsKey(key)) i = 0;
			else i = respirators.get(key).x;
		}
		int level = EnchantmentHelper.getEnchantmentLevel(Enchantments.RESPIRATION, mask);
		i += (100 - i) * level * 0.1F;
		return i;
	}

	public static boolean isMaskInSlot(Entity player) {
		return player instanceof EntityPlayer && isMask(IMistCapaHandler.getHandler((EntityPlayer)player).getMask());
	}

	public static boolean isMaskInHelmetSlot(Entity player) {
		return player instanceof EntityPlayer && isMask(((EntityPlayer)player).getItemStackFromSlot(EntityEquipmentSlot.HEAD));
	}

	public static boolean isMask(ItemStack stack) {
		return !stack.isEmpty() && (stack.getItem() instanceof IMask || stack.getSubCompound(MistTags.nbtFilterTag) != null);
	}

	/**Returns the filter in the respirator.*/
	public static ItemStack getFilter(ItemStack stack) {
		NBTTagCompound tag = stack.getSubCompound(MistTags.nbtFilterTag);
		return tag != null ? new ItemStack(tag) : ItemStack.EMPTY;
	}

	/**Returns the filtering depth of the current respirator in percent.*/
	public static float damageFilter(ItemStack mask, int damage, IMistCapaHandler mistCapa) {
		ItemStack filter = getFilter(mask);
		if (!filter.isEmpty()) {
			int durability = getFilterDurability(filter);
			float filteringDepth = getFilteringDepth(filter);
			filteringDepth *= getImpermeability(mask)/100;
			if (durability == filter.getMaxDamage()) {
				damage += filter.getItemDamage();
				if (damage < durability) {
					filter.setItemDamage(damage);
					mask.getTagCompound().setTag(MistTags.nbtFilterTag, filter.serializeNBT());
					mask.getTagCompound().setInteger(MistTags.nbtFilterDurabilityTag, damage);
				} else {
					mask.getTagCompound().removeTag(MistTags.nbtFilterTag);
					mask.getTagCompound().removeTag(MistTags.nbtFilterDurabilityTag);
				}
			} else {
				damage += mask.getTagCompound().getInteger(MistTags.nbtFilterDurabilityTag);
				if (damage <= durability) mask.getTagCompound().setInteger(MistTags.nbtFilterDurabilityTag, damage);
				else {
					mask.getTagCompound().removeTag(MistTags.nbtFilterTag);
					mask.getTagCompound().removeTag(MistTags.nbtFilterDurabilityTag);
				}
			}
			mistCapa.setMaskChanged(true, false);
			return filteringDepth;
		}
		return 0;
	}

	/**Returns the filter durability.*/
	public static int getFilterDurability(ItemStack filter) {
		if (filter.isItemStackDamageable()) return filter.getMaxDamage();
		else {
			ItemStackMapKey key = new ItemStackMapKey(filter);
			return !filters.isEmpty() && filters.containsKey(key) ? filters.get(key) : 1000;
		}
	}

	/**Returns the filtering depth of current filter.*/
	public static float getFilteringDepth(ItemStack filter) {
		if (filter.getItem() instanceof IFilter) return ((IFilter)filter.getItem()).getFilteringDepth();
		else return filter.hasTagCompound() ? filter.getTagCompound().getFloat(MistTags.nbtFilteringDepthTag) : 0;
	}
}