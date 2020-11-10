package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistBranchBlock extends ItemBlock {

	public ItemMistBranchBlock(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String name = "";
		String blockName = this.block.getUnlocalizedName();
		switch (stack.getItemDamage()) {
		case 0:
			name = "_4";
			break;
		case 3:
			name = "_4_d";
			break;
		case 6:
			name = "_8";
			break;
		case 9:
			name = "_8_d";
			break;
		}
		return blockName + name;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		IBlockState state = world.getBlockState(pos);
		if (!state.getBlock().isReplaceable(world, pos)) pos = pos.offset(facing);
		ItemStack stack = player.getHeldItem(hand);
		IBlockState newState = this.block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, stack.getMetadata(), player, hand);
		if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) && world.checkNoEntityCollision(newState.getCollisionBoundingBox(world, pos).offset(pos), player)) {
			if (placeBlockAt(stack, player, world, pos, facing, hitX, hitY, hitZ, newState)) {
				newState = world.getBlockState(pos);
				SoundType soundtype = newState.getBlock().getSoundType(newState, world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		} else return EnumActionResult.FAIL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
		IBlockState state = world.getBlockState(pos);
		if (!state.getBlock().isReplaceable(world, pos)) pos = pos.offset(side);
		IBlockState newState = this.block.getStateForPlacement(world, pos, side, 0.5F, 0.5F, 0.5F, stack.getMetadata(), player, EnumHand.MAIN_HAND);
		return world.checkNoEntityCollision(newState.getCollisionBoundingBox(world, pos).offset(pos));
	}
}