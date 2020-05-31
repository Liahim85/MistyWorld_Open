package ru.liahim.mist.item;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.block.MistBlockSlabStone;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStairs.EnumHalf;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistSlab extends ItemBlock {

	protected final Block fullBlock;

	public ItemMistSlab(Block block) {
		super(block);
		if (block instanceof IDividable) this.fullBlock = ((IDividable)block).getFullBlock();
		else this.fullBlock = null;
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getCount() != 0 && player.canPlayerEdit(pos.offset(facing), facing, stack) && this.fullBlock != null) {
			IBlockState state = world.getBlockState(pos);
			IBlockState itemState = this.block.getStateFromMeta(stack.getMetadata());
			if (state.getBlock() == this.block && (!(state.getBlock() instanceof MistBlockSlabStone) || state.getValue(MistBlockSlabStone.VARIANT) == itemState.getValue(MistBlockSlabStone.VARIANT))) {
				BlockSlab.EnumBlockHalf half = state.getValue(BlockSlab.HALF);
				if (half == BlockSlab.EnumBlockHalf.BOTTOM ? facing == EnumFacing.UP : facing == EnumFacing.DOWN) {
					IBlockState fullState = ((IDividable)this.fullBlock).getFullState(state);
					AxisAlignedBB aabb = fullState.getCollisionBoundingBox(world, pos);
					if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, fullState)) {
						SoundType soundtype = this.fullBlock.getSoundType(fullState, world, pos, player);
						world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
						stack.setCount(stack.getCount() - 1);
					}
					if (player instanceof EntityPlayerMP) {
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
					}
					return EnumActionResult.SUCCESS;
				}
			} else if (state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState) && state.getBlock() instanceof BlockStairs) {
				if (state.getValue(BlockStairs.HALF) == EnumHalf.TOP ? facing == EnumFacing.UP : facing == EnumFacing.DOWN) {
					IBlockState stairsState = ((IDividable)this.fullBlock).getStairsBlock(itemState).getDefaultState();
					if (stairsState.getBlock() instanceof BlockStairs) {
						stairsState = stairsState.withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING)).withProperty(BlockStairs.HALF, state.getValue(BlockStairs.HALF));
						AxisAlignedBB aabb = stairsState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, stairsState)) {
							SoundType soundtype = stairsState.getBlock().getSoundType(stairsState, world, pos, player);
							world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
							stack.setCount(stack.getCount() - 1);
						}
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
						}
						return EnumActionResult.SUCCESS;
					}
				}
			}
			return this.tryPlace(player, stack, world, pos.offset(facing), facing, hitY) ? EnumActionResult.SUCCESS
					: super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
		} else {
			return EnumActionResult.FAIL;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing facing, EntityPlayer player, ItemStack stack) {
		BlockPos blockpos = pos;
		IBlockState state = world.getBlockState(pos);
		IBlockState itemState = this.block.getStateFromMeta(stack.getMetadata());
		if (state.getBlock() == this.block && (!(state.getBlock() instanceof MistBlockSlabStone) || state.getValue(MistBlockSlabStone.VARIANT) == itemState.getValue(MistBlockSlabStone.VARIANT))) {
			boolean top = state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
			if (top ? facing == EnumFacing.DOWN : facing == EnumFacing.UP) return true;
		} else if (this.fullBlock != null && state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState)) {
			if (state.getBlock() instanceof BlockStairs) {
				boolean top = state.getValue(BlockStairs.HALF) == EnumHalf.TOP;
				if (top ? facing == EnumFacing.UP : facing == EnumFacing.DOWN) return true;
			}
		}
		pos = pos.offset(facing);
		state = world.getBlockState(pos);
		return state.getBlock() == this.block || (this.fullBlock != null && state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState)) ? true :
			super.canPlaceBlockOnSide(world, blockpos, facing, player, stack);
	}

	private boolean tryPlace(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing facing, float hitY) {
		if (this.fullBlock != null) {
			IBlockState state = world.getBlockState(pos);
			IBlockState itemState = this.block.getStateFromMeta(stack.getMetadata());
			if (state.getBlock() == this.block && (!(state.getBlock() instanceof MistBlockSlabStone) || state.getValue(MistBlockSlabStone.VARIANT) == itemState.getValue(MistBlockSlabStone.VARIANT))) {
				IBlockState fullState = ((IDividable)this.fullBlock).getFullState(state);
				AxisAlignedBB aabb = fullState.getCollisionBoundingBox(world, pos);
				if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, fullState)) {
					SoundType soundtype = this.fullBlock.getSoundType(fullState, world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					stack.setCount(stack.getCount() - 1);
				}
				if (player instanceof EntityPlayerMP) {
					CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
				}
				return true;
			} else if (state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState) && state.getBlock() instanceof BlockStairs) {
				EnumHalf half = state.getValue(BlockStairs.HALF);
				if (half == EnumHalf.TOP ? (facing == EnumFacing.DOWN || hitY > 0.5F) : (facing == EnumFacing.UP || hitY < 0.5F)) {
					IBlockState stairsState = ((IDividable)this.fullBlock).getStairsBlock(itemState).getDefaultState();
					if (stairsState.getBlock() instanceof BlockStairs) {
						stairsState = stairsState.withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING)).withProperty(BlockStairs.HALF, half);
						AxisAlignedBB aabb = stairsState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, stairsState)) {
							SoundType soundtype = stairsState.getBlock().getSoundType(stairsState, world, pos, player);
							world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
							stack.setCount(stack.getCount() - 1);
						}
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}