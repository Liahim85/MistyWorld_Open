package ru.liahim.mist.api.block;

import ru.liahim.mist.api.item.MistItems;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFarmland {

	public static final PropertyBool MULCH = PropertyBool.create("mulch");

	public static boolean setMulched(World world, BlockPos pos, IBlockState state) {
		if (state.getBlock() instanceof IFarmland && !state.getValue(MULCH)) {
			world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_HIT, SoundCategory.BLOCKS, 0.5F, 0.7F);
			return world.setBlockState(pos, state.withProperty(MULCH, true));
		}
		return false;
	}
	
	public static void extractMulch(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && state.getValue(MULCH)) {
			world.setBlockState(pos, state.withProperty(MULCH, false));
			Block.spawnAsEntity(world, pos.up(), new ItemStack(MistItems.MULCH));
		}
	}
}