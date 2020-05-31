package ru.liahim.mist.item;

import ru.liahim.mist.block.MistAcidDirt;
import ru.liahim.mist.common.Mist;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemMistSeeds extends ItemMist implements IPlantable {

	private final Block crops;

	public ItemMistSeeds(Block crops) {
		this.crops = crops;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos,
		EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		IBlockState state = world.getBlockState(pos);
		if (facing == EnumFacing.UP && player.canPlayerEdit(pos.offset(facing), facing, stack) && state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP, this) && world.isAirBlock(pos.up())) {
			if (this.crops instanceof IPlantable ? (((IPlantable)this.crops).getPlantType(world, pos) != Mist.MIST_DOWN_PLANT || state.getBlock() instanceof MistAcidDirt) : true) {
				world.setBlockState(pos.up(), this.crops.getDefaultState());
				world.playSound(null, pos, SoundEvents.BLOCK_GRASS_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
				stack.setCount(stack.getCount() - 1);
				if (player instanceof EntityPlayerMP) {
					CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
				}
				return EnumActionResult.SUCCESS;
			} else return EnumActionResult.FAIL;
		} else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return this.crops instanceof IPlantable ? ((IPlantable)this.crops).getPlantType(world, pos) : EnumPlantType.Crop;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return this.crops.getDefaultState();
	}
}