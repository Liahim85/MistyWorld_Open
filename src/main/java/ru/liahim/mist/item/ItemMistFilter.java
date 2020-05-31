package ru.liahim.mist.item;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.IFilter;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistFilter extends ItemMist implements IFilter {

	private final float filteringDepth;

	public ItemMistFilter(int maxDamage, float filteringDepth) {
		super();
		this.setMaxDamage(maxDamage);
		this.filteringDepth = filteringDepth;
	}

	@Override
	public float getFilteringDepth() {
		return this.filteringDepth;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
		float d = (float)stack.getItemDamage()/stack.getMaxDamage()*100;
		StringBuilder sb = new StringBuilder();
		sb.append(I18n.format("item.mist.filter_depth.tooltip"));
		sb.append(": ");
		sb.append(TextFormatting.GREEN);
		sb.append(String.format("%.2f", this.filteringDepth));
		sb.append("%");
		tooltip.add(sb.toString());
		sb.delete(0, sb.length());
		sb.append(I18n.format("item.mist.filter_damage.tooltip"));
		sb.append(": ");
		if (d >= 25) sb.append(d < 50 ? TextFormatting.YELLOW : d < 75 ? TextFormatting.GOLD : TextFormatting.RED);
		sb.append(String.format("%.2f", d));
		sb.append("%");
		tooltip.add(sb.toString());
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
        return !stack.isItemDamaged() ? super.maxStackSize : 1;
    }

	@Override
	public void setDamage(ItemStack stack, int damage) {
		stack = ItemMistFilter.setDamageNBT(stack, true);
		super.setDamage(stack, damage);
	}

	/**Used to prevent merging stacks of filters with different damage. Also see removeDamageNBT(ItemStack stack).*/
	public static ItemStack setDamageNBT(ItemStack stack, boolean checkDamage) {
		if (!checkDamage || !stack.isItemDamaged()) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setBoolean("MistDamage", true);
		}
		return stack;
	}

	/**Used to prevent merging stacks of filters with different damage. Also see setDamageNBT(ItemStack stack).*/
	public static ItemStack removeDamageNBT(ItemStack stack) {
		if (stack.getTagCompound() != null) stack.getTagCompound().removeTag("MistDamage");
		return stack;
	}

	@Override
	public boolean isRepairable() {
		return false;
	}
}