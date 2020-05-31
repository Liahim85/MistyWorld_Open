package ru.liahim.mist.api.block;

import javax.annotation.Nonnull;

import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IShiftPlaceable {

	/** Fired on both sides whenever the player Shift + right clicks while targeting on a top side of block 
	 * @param state */
	public boolean onShiftPlacing(World world, BlockPos pos, @Nonnull ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs);
}