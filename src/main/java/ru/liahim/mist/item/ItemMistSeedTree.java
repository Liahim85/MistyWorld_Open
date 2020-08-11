package ru.liahim.mist.item;

import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.block.MistTreeSapling.EnumType;
import ru.liahim.mist.capability.handler.IFoodHandler;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.init.ModAdvancements;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistSeedTree extends ItemFood implements IMistFood {

	public ItemMistSeedTree() {
		super(0, 0.0F, false);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String name = EnumType.byMeta(stack.getItemDamage()).getName();
		return "item.mist." + name + "_seed";
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
	public int getHealAmount(ItemStack stack) {
		EnumType type = EnumType.byMeta(stack.getItemDamage());
		if (type == EnumType.OAK) return 1;
		else if (type == EnumType.STREE) return 1;
		else if (type == EnumType.TTREE) return 1;
		else if (type == EnumType.RTREE) return 3;
		else return 0;
	}

	@Override
	public float getSaturationModifier(ItemStack stack) {
		EnumType type = EnumType.byMeta(stack.getItemDamage());
		if (type == EnumType.OAK) return 0.3F;
		else if (type == EnumType.STREE) return 0.3F;
		else if (type == EnumType.TTREE) return 0.3F;
		else if (type == EnumType.RTREE) return 0.6F;
		else return 0;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
		if (!this.isFood(stack)) return stack;
		return super.onItemUseFinish(stack, world, entity);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote && world.rand.nextFloat() < this.getProbability(stack)) {
			PotionEffect[] potions = getPotions(stack);
			if (potions != null) {
				for (PotionEffect po : potions) player.addPotionEffect(po);
			}
		}

		int toxic = (int) this.getToxic(stack);
		if (toxic != 0) {
			IMistCapaHandler.getHandler(player).addToxic(toxic);
			if (player instanceof EntityPlayerMP) ModAdvancements.CONSUME_TOXIC.trigger((EntityPlayerMP) player, stack, Float.valueOf(toxic));
		}
		IFoodHandler.getHandler(player).setFoodStudy(stack);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return this.isFood(stack) ? 32 : 0;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return this.isFood(stack) ? EnumAction.EAT : EnumAction.NONE;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (!this.isFood(player.getHeldItem(hand))) return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, EnumType.POPLAR.getMeta()));
			for (EnumType type : EnumType.values()) {
				if (type != EnumType.ASPEN && type != EnumType.BIRCH && type != EnumType.POPLAR && type != EnumType.SPRUCE && type != EnumType.WILLOW) {
					items.add(new ItemStack(this, 1, type.getMeta()));
					if (type == EnumType.PINE) items.add(new ItemStack(this, 1, EnumType.SPRUCE.getMeta()));
				}
			}
			items.add(new ItemStack(this, 1, EnumType.WILLOW.getMeta()));
			items.add(new ItemStack(this, 1, EnumType.BIRCH.getMeta()));
			items.add(new ItemStack(this, 1, EnumType.ASPEN.getMeta()));
		}
	}

	@Override
	public boolean isEdible(ItemStack stack) {
		EnumType type = EnumType.byMeta(stack.getItemDamage());
		return type != EnumType.TTREE;
	}

	@Override
	public PotionEffect[] getPotions(ItemStack stack) {
		return EnumType.byMeta(stack.getItemDamage()) == EnumType.TTREE ? new PotionEffect[] {new PotionEffect(MobEffects.POISON, 50, 1, false, false)} : null;
	}

	@Override
	public float getProbability(ItemStack stack) {
		return EnumType.byMeta(stack.getItemDamage()) == EnumType.TTREE ? 0.6F : 0;
	}

	@Override
	public float getToxic(ItemStack stack) {
		return EnumType.byMeta(stack.getItemDamage()) == EnumType.STREE ? -150 : 0;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		EnumType type = EnumType.byMeta(stack.getItemDamage());
		return type == EnumType.OAK || type == EnumType.RTREE || type == EnumType.STREE || type == EnumType.TTREE;
	}
}