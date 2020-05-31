package ru.liahim.mist.item.food;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.upperplant.MistTinderFungus;

public class ItemMistTinderFungus extends ItemToxicFood {

	public ItemMistTinderFungus() {
		super(1, 0.3F, false, -100);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.capabilities.isCreativeMode || player.isSneaking()) {
			if (facing.getAxis() != Axis.Y) {
				IBlockState state = world.getBlockState(pos);
				if (!state.getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
				ItemStack stack = player.getHeldItem(hand);
				if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) && world.isSideSolid(pos.offset(facing.getOpposite()), facing) && world.mayPlace(MistBlocks.TINDER_FUNGUS, pos, false, facing, (Entity) null)) {
					state = MistBlocks.TINDER_FUNGUS.getDefaultState().withProperty(MistTinderFungus.FACING, facing);
					world.setBlockState(pos, state);
					SoundType soundtype = MistBlocks.TINDER_FUNGUS.getSoundType(state, world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					if (!player.capabilities.isCreativeMode) stack.shrink(1);
					return EnumActionResult.SUCCESS;
				}
			}
			return EnumActionResult.FAIL;
		}
		return EnumActionResult.PASS;
	}

	@Override
	public PotionEffect[] getPotions(ItemStack stack) {
		return new PotionEffect[] { new PotionEffect(MobEffects.SLOWNESS, 200, 2, false, false) };
	}

	@Override
	public float getProbability(ItemStack stack) {
		return 0.5F;
	}
}