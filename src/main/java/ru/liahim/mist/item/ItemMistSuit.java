package ru.liahim.mist.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.BlockCauldron;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.item.IColoredItem;
import ru.liahim.mist.api.item.ISuit;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.client.model.ModelSuit;
import ru.liahim.mist.init.ItemColoring;
import ru.liahim.mist.init.ModItems;

public class ItemMistSuit extends ItemMistArmor implements ISuit, IColoredItem {

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return ItemColoring.ITEM_ARMOR_COLORING;
	}
	private final String thinTex = "mist:textures/models/armor/rubber_layer_1_a.png";
	private final String thickTex = "mist:textures/models/armor/rubber_layer_1.png";
	private final String legginsTex = "mist:textures/models/armor/rubber_layer_2.png";
	private final String overlay_1 = "mist:textures/models/armor/rubber_layer_1_overlay.png";
	private final String overlay_2 = "mist:textures/models/armor/rubber_layer_2_overlay.png";
	private final float protection;

	public ItemMistSuit(ArmorMaterial material, int renderIndex, EntityEquipmentSlot equipmentSlot, float protection) {
		super(material, renderIndex, equipmentSlot);
		this.protection = protection;
	}

	@Override
	public float getPollutionProtection() {
		return this.protection;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		if (stack.getItem() instanceof ISuit) {
			StringBuilder sb = new StringBuilder();
			sb.append(I18n.format("item.mist.suit_protection.tooltip"));
			sb.append(": ");
			sb.append(TextFormatting.GREEN);
			sb.append(String.format("%.2f", ((ISuit)stack.getItem()).getPollutionProtection()));
			sb.append("%");
			tooltip.add(sb.toString());
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (this.hasColor(stack)) {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockCauldron) {
				int level = state.getValue(BlockCauldron.LEVEL);
				if (level > 0) {
					if (!world.isRemote) {
						this.removeColor(stack);
						Blocks.CAULDRON.setWaterLevel(world, pos, state, level - 1);
						player.addStat(StatList.ARMOR_CLEANED);
					}
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack material) {
		return material.getItem() == MistItems.RUBBER || super.getIsRepairable(toRepair, material);
	}

	@Override
	public boolean hasOverlay(ItemStack stack) {
		return this.getArmorMaterial() == ModItems.RUBBER_MATERIAL || getColor(stack) != 0xFFFFFF;
	}

	@Override
	public boolean hasColor(ItemStack stack) {
		if (this.getArmorMaterial() != ModItems.RUBBER_MATERIAL) return false;
		else {
			NBTTagCompound tag = stack.getTagCompound();
			return tag != null && tag.hasKey("display", 10) ? tag.getCompoundTag("display").hasKey("color", 3) : false;
		}
	}

	@Override
	public int getColor(ItemStack stack) {
		if (this.getArmorMaterial() != ModItems.RUBBER_MATERIAL) return 0xFFFFFF;
		else {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null) {
				NBTTagCompound tag1 = tag.getCompoundTag("display");
				if (tag1 != null && tag1.hasKey("color", 3)) {
					return tag1.getInteger("color");
				}
			}
			return 0x726d61;
		}
	}

	@Override
	public void removeColor(ItemStack stack) {
		if (this.getArmorMaterial() == ModItems.RUBBER_MATERIAL) {
			NBTTagCompound tag = stack.getTagCompound();
			if (tag != null) {
				NBTTagCompound tag1 = tag.getCompoundTag("display");
				if (tag1.hasKey("color")) {
					tag1.removeTag("color");
				}
			}
		}
	}

	@Override
	public void setColor(ItemStack stack, int color) {
		if (this.getArmorMaterial() != ModItems.RUBBER_MATERIAL) {
			throw new UnsupportedOperationException("Can\'t dye non-leather!");
		} else {		
			NBTTagCompound tab = stack.getTagCompound();
			if (tab == null) {
				tab = new NBTTagCompound();
				stack.setTagCompound(tab);
			}
			NBTTagCompound tab1 = tab.getCompoundTag("display");
			if (!tab.hasKey("display", 10)) {
				tab.setTag("display", tab1);
			}
			tab1.setInteger("color", color);
		}
	}

	@Override
	@Nullable
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if (entity instanceof AbstractClientPlayer) {
			boolean legs = slot == EntityEquipmentSlot.LEGS;
			if (type != null) return legs ? overlay_2 : overlay_1;
			boolean alex = ((AbstractClientPlayer)entity).getSkinType().equals("slim");
			return legs ? legginsTex : alex ? thinTex : thickTex;
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slot, ModelBiped model) {
		if (entity instanceof AbstractClientPlayer) {
			boolean alex = ((AbstractClientPlayer)entity).getSkinType().equals("slim");
			boolean legs = slot == EntityEquipmentSlot.LEGS;
			return legs ? ModelSuit.leggins : alex ? ModelSuit.thin : ModelSuit.thick;
		}
		return null;
	}
}