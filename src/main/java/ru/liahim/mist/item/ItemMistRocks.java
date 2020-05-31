package ru.liahim.mist.item;

import ru.liahim.mist.api.block.MistBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMistRocks extends ItemMist {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.getBlockState(pos).getBlock() != MistBlocks.CAMPFIRE)
			return Item.getItemFromBlock(MistBlocks.COBBLESTONE_STEP).onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
		return EnumActionResult.FAIL;
	}
}