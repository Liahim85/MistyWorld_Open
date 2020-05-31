package ru.liahim.mist.item.food;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.capability.handler.IFoodHandler;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.init.ModAdvancements;

public class ItemToxicFood extends ItemFood implements IMistFood {

	private final int toxic;

	public ItemToxicFood(int amount, float saturation, boolean isWolfFood, int toxic) {
		super(amount, saturation, isWolfFood);
		this.toxic = toxic;
	}

	@Override
	public String getUnlocalizedName() {
		return "item.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null) {
			IFoodHandler mCapa = IFoodHandler.getHandler(player);
			if (this.isFood(stack) && mCapa.isFoodStudy(stack)) {

				float toxic = this.getToxic(stack);
				if (toxic > 0) tooltip.add(TextFormatting.DARK_RED + I18n.format("item.mist.food_toxic.tooltip"));
				else if (toxic < 0) tooltip.add(TextFormatting.DARK_GREEN + I18n.format("item.mist.food_antitoxic.tooltip"));

				if (!this.isEdible(stack)) tooltip.add(TextFormatting.DARK_RED + I18n.format("item.mist.food_inedible.tooltip"));
				else tooltip.add(TextFormatting.DARK_GREEN + I18n.format("item.mist.food_edible.tooltip"));
			}
		}
	}

	@Override
	public void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if (stack.getItem() == this) {
			if (this.getToxic(stack) != 0) {
				IMistCapaHandler.getHandler(player).addToxic(this.toxic);
				if (player instanceof EntityPlayerMP) ModAdvancements.CONSUME_TOXIC.trigger((EntityPlayerMP) player, stack, Float.valueOf(this.toxic));
			}
			IFoodHandler.getHandler(player).setFoodStudy(stack);
		}
		super.onFoodEaten(stack, world, player);
	}

	@Override
	public float getToxic(ItemStack stack) {
		return this.toxic;
	}

	@Override
	public PotionEffect[] getPotions(ItemStack stack) {
		return null;
	}

	@Override
	public float getProbability(ItemStack stack) {
		return 0;
	}
}