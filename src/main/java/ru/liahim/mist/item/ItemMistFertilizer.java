package ru.liahim.mist.item;

import ru.liahim.mist.api.block.IMossable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistDirt;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.init.ModAdvancements;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemMistFertilizer extends ItemMist {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(pos.offset(side), side, stack)) return EnumActionResult.FAIL;
		else {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof MistDirt) {
				if ((((MistDirt)state.getBlock()).canFertile(state) && side == EnumFacing.UP) || (player.isCreative() &&
						(side == EnumFacing.UP ? (world.getBlockState(pos.up()).getBlock() instanceof MistTreeTrunk ?
								world.getBlockState(pos.up()).getValue(MistTreeTrunk.DIR) != EnumFacing.UP : true) : true))) {		
					int hum = state.getValue(MistDirt.HUMUS);
					if (hum < 3) {
						if (!player.isCreative()) stack.shrink(1);
						world.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.8F, 1.0F);
						if (!world.isRemote) {
							world.setBlockState(pos, state.withProperty(MistDirt.HUMUS, hum + 1));
							if (player instanceof EntityPlayerMP) ModAdvancements.FERTILE.trigger((EntityPlayerMP) player, world, pos, state);
						}
						return EnumActionResult.SUCCESS;
					}
				}
			} else if (state.getBlock() instanceof IPlantable && ((IPlantable)state.getBlock()).getPlantType(world, pos) == EnumPlantType.Crop ||
					state.getBlock() == Blocks.BEETROOTS) { //TODO Убрать свёклу, как только она появится в EnumPlantType.Crop
				state = world.getBlockState(pos.down());
				if (state.getBlock() instanceof MistDirt && ((MistDirt)state.getBlock()).canFertile(state)) {
					int hum = state.getValue(MistDirt.HUMUS);
					if (hum < 3) {
						if (!player.isCreative()) stack.shrink(1);
						world.playSound(player, pos.down(), SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.8F, 1.0F);
						if (!world.isRemote) {
							world.setBlockState(pos.down(), state.withProperty(MistDirt.HUMUS, hum + 1));
							if (player instanceof EntityPlayerMP) ModAdvancements.FERTILE.trigger((EntityPlayerMP) player, world, pos.down(), state);
						}
						return EnumActionResult.SUCCESS;
					}
				}
			} else if (stack.getItem() == MistItems.HUMUS && state.getBlock() instanceof IMossable && ((IMossable)state.getBlock()).setMossy(state, world, pos)) {
				if (!player.isCreative()) stack.shrink(1);
				world.playSound(player, pos.down(), SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.8F, 1.0F);
				if (!world.isRemote && player instanceof EntityPlayerMP) ModAdvancements.MOSSY.trigger((EntityPlayerMP) player, world, pos, state);
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.FAIL;
	}
}