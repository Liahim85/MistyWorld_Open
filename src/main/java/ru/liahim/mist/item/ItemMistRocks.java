package ru.liahim.mist.item;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.block.MistBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemMistRocks extends ItemMist {

	private final Block step;
	private final Block mossStep;

	public ItemMistRocks(Block step, Block mossStep) {
		this.step = step;
		this.mossStep = mossStep;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != MistBlocks.CAMPFIRE) {
			if (state.getBlock() instanceof IDividable && ((IDividable)state.getBlock()).getStepBlock(state) == this.mossStep) {
				return Item.getItemFromBlock(this.mossStep).onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
			}
			return Item.getItemFromBlock(this.step).onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
		}
		return EnumActionResult.FAIL;
	}
}