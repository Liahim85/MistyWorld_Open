package ru.liahim.mist.item;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.item.EntityMistPainting;
import ru.liahim.mist.entity.item.EntityMistPainting.EnumArt;

public class ItemMistPainting extends ItemMist {

	static final String name = "item.mist.painting.tooltip.";

	public ItemMistPainting() {
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		EnumArt art = EntityMistPainting.EnumArt.values()[stack.getMetadata()];
		tooltip.add(I18n.format(name + "name") + ": " + I18n.format(name + art.title));
		tooltip.add(I18n.format(name + "format") + ": " + art.sizeX/16 + "x" + art.sizeY/16);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack itemstack = player.getHeldItem(hand);
		BlockPos blockpos = pos.offset(facing);
		if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && player.canPlayerEdit(blockpos, facing, itemstack)) {
			EntityHanging entityhanging = this.createEntity(world, blockpos, facing, itemstack.getMetadata());
			if (entityhanging != null && entityhanging.onValidSurface()) {
				if (!world.isRemote) {
					entityhanging.playPlaceSound();
					world.spawnEntity(entityhanging);
				}
				itemstack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		} else return EnumActionResult.FAIL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (EnumArt art : EntityMistPainting.EnumArt.values()) {
				items.add(new ItemStack(this, 1, art.ordinal()));
			}
		}
	}

	@Nullable
	private EntityHanging createEntity(World world, BlockPos pos, EnumFacing clickedSide, int meta) {
		return new EntityMistPainting(world, pos, clickedSide, meta);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.UNCOMMON;
	}
}