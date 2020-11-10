package ru.liahim.mist.item;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.block.MistBlockStairs;
import ru.liahim.mist.block.MistBlockStep;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.BlockStairs.EnumHalf;
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

public class ItemMistWall extends ItemBlock {

	private final Block fullBlock;

	public ItemMistWall(Block block) {
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
			//Step
			if (state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState) && state.getBlock() instanceof MistBlockStep) {
				EnumFacing side = state.getValue(BlockStairs.FACING);
				if (facing == side.getOpposite()) {
					IBlockState stairsState = ((IDividable)this.fullBlock).getStairsBlock(itemState).getDefaultState();
					if (stairsState.getBlock() instanceof BlockStairs) {
						stairsState = stairsState.withProperty(BlockStairs.HALF, state.getValue(BlockStairs.HALF) == EnumHalf.TOP ? EnumHalf.BOTTOM : EnumHalf.TOP).withProperty(BlockStairs.FACING, facing);
						AxisAlignedBB aabb = stairsState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, stairsState)) {
							SoundType soundtype = this.fullBlock.getSoundType(stairsState, world, pos, player);
							world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
							stack.shrink(1);
						}
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
						}
						return EnumActionResult.SUCCESS;
					}
				}
			}
			//Wall
			else if (state.getBlock() == this.block) {
				EnumFacing side = state.getValue(BlockStairs.FACING);
				if (facing == side.getOpposite()) {
					IBlockState fullState = ((IDividable)this.fullBlock).getFullState(state);
					AxisAlignedBB aabb = fullState.getCollisionBoundingBox(world, pos);
					if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, fullState)) {
						SoundType soundtype = this.fullBlock.getSoundType(fullState, world, pos, player);
						world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
						stack.shrink(1);
					}
					if (player instanceof EntityPlayerMP) {
						CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
					}
					return EnumActionResult.SUCCESS;
				}
			}
			return this.tryPlace(player, stack, world, pos.offset(facing), facing, hitX, hitY, hitZ) ? EnumActionResult.SUCCESS
					: super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
		} else return EnumActionResult.FAIL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing facing, EntityPlayer player, ItemStack stack) {
		BlockPos blockpos = pos;
		IBlockState state = world.getBlockState(pos);
		IBlockState itemState = this.block.getStateFromMeta(stack.getMetadata());
		if (state.getBlock() == this.block || state.getBlock() instanceof MistBlockStep) {
			if (state.getValue(BlockStairs.FACING) == facing.getOpposite()) return true;
		}
		pos = pos.offset(facing);
		state = world.getBlockState(pos);
		return state.getBlock() == this.block || state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState) ? true :
			super.canPlaceBlockOnSide(world, blockpos, facing, player, stack);
	}

	private boolean tryPlace(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (this.fullBlock != null) {
			IBlockState state = world.getBlockState(pos);
			IBlockState itemState = this.block.getStateFromMeta(stack.getMetadata());
			if (state.getBlock() == ((IDividable)this.fullBlock).getStepBlock(itemState) && state.getBlock() instanceof MistBlockStep) {
				EnumFacing side = state.getValue(BlockStairs.FACING);
				if (facing == side || (facing != side.getOpposite() && (hitX < 0.5F ? side == EnumFacing.EAST : side == EnumFacing.WEST) || (hitZ < 0.5F ? side == EnumFacing.SOUTH : side == EnumFacing.NORTH))) {
					IBlockState stairsState = ((IDividable)this.fullBlock).getStairsBlock(itemState).getDefaultState();
					if (stairsState.getBlock() instanceof MistBlockStairs) {
						stairsState = stairsState.withProperty(BlockStairs.HALF, state.getValue(BlockStairs.HALF) == EnumHalf.TOP ? EnumHalf.BOTTOM : EnumHalf.TOP).withProperty(BlockStairs.FACING, side.getOpposite());
						AxisAlignedBB aabb = stairsState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, stairsState)) {
							SoundType soundtype = this.fullBlock.getSoundType(stairsState, world, pos, player);
							world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
							stack.shrink(1);
						}
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
						}
						return true;
					}
				}
			} else if (state.getBlock() == this.block) {
				IBlockState fullState = ((IDividable)this.fullBlock).getFullState(state);
				AxisAlignedBB aabb = fullState.getCollisionBoundingBox(world, pos);
				if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, fullState)) {
					SoundType soundtype = this.fullBlock.getSoundType(fullState, world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					stack.shrink(1);
				}
				if (player instanceof EntityPlayerMP) {
					CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
				}
				return true;
			}
		}
		return false;
	}
}