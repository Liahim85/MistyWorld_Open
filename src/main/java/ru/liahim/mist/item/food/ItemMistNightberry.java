package ru.liahim.mist.item.food;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.MistBlocks;

public class ItemMistNightberry extends ItemToxicFood {

	public ItemMistNightberry() {
		super(1, 0.3F, false, -100);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.capabilities.isCreativeMode || player.isSneaking()) {
			if (facing == EnumFacing.UP) {
				IBlockState state = world.getBlockState(pos);
				if (!state.getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
				ItemStack stack = player.getHeldItem(hand);
				if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) && world.getBlockState(pos.offset(facing.getOpposite())).getBlock() == MistBlocks.FLOATING_MAT) {
					state = MistBlocks.NIGHTBERRY.getDefaultState();
					world.setBlockState(pos, state);
					SoundType soundtype = MistBlocks.NIGHTBERRY.getSoundType(state, world, pos, player);
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
		return new PotionEffect[] { new PotionEffect(MobEffects.BLINDNESS, 200, 1, false, false) };
	}

	@Override
	public float getProbability(ItemStack stack) {
		return 0.5F;
	}
}