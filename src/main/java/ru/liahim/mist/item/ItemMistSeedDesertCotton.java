package ru.liahim.mist.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.upperplant.MistDesertCotton;

public class ItemMistSeedDesertCotton extends ItemMist {

	public ItemMistSeedDesertCotton() {
		this.setHasSubtypes(true);
	    this.setMaxDamage(0);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = stack.getMetadata();
		String name = "";
		if (i == 0) name = "desert_cotton_f";
		else if (i == 1) name = "desert_cotton_s";
		return "item.mist." + name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			items.add(new ItemStack(this, 1, 0));
			items.add(new ItemStack(this, 1, 1));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		int meta = stack.getItemDamage();
		if (meta == 1) {
			if (stack.getCount() != 0 && player.canPlayerEdit(pos.offset(side), side, stack)) {
				if (!world.getBlockState(pos).getMaterial().isReplaceable()) pos = pos.offset(side);
				if (world.getBlockState(pos.down()).getBlock() != MistBlocks.DESERT_COTTON && MistBlocks.DESERT_COTTON.canPlaceBlockOnSide(world, pos, side)) {
					world.setBlockState(pos, MistBlocks.DESERT_COTTON.getDefaultState().withProperty(MistDesertCotton.AGE, 8));
					SoundType soundtype = MistBlocks.DESERT_COTTON.getSoundType(MistBlocks.DESERT_COTTON.getDefaultState(), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					stack.shrink(1);
					if (player instanceof EntityPlayerMP) {
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
					}
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.FAIL;
	}
}