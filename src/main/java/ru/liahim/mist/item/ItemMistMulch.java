package ru.liahim.mist.item;

import ru.liahim.mist.api.block.IFarmland;
import ru.liahim.mist.init.ModAdvancements;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemMistMulch extends ItemMist {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(pos.offset(side), side, stack)) return EnumActionResult.FAIL;
		else {
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof IFarmland) {
				if (side == EnumFacing.UP && IFarmland.setMulched(world, pos, state)) {
					if (!player.isCreative()) stack.setCount(stack.getCount() - 1);
					if (player instanceof EntityPlayerMP) ModAdvancements.MULCH_PLACED.trigger((EntityPlayerMP)player, world, pos, state);
					return EnumActionResult.SUCCESS;
				}
			} else if (state.getBlock() instanceof IPlantable && ((IPlantable)state.getBlock()).getPlantType(world, pos) == EnumPlantType.Crop ||
					state.getBlock() == Blocks.BEETROOTS) { //TODO Убрать свёклу, как только она появится в EnumPlantType.Crop
				state = world.getBlockState(pos.down());
				if (state.getBlock() instanceof IFarmland && IFarmland.setMulched(world, pos.down(), state)) {
					if (!player.isCreative()) stack.setCount(stack.getCount() - 1);
					if (player instanceof EntityPlayerMP) ModAdvancements.MULCH_PLACED.trigger((EntityPlayerMP)player, world, pos.down(), state);
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.FAIL;
	}
}