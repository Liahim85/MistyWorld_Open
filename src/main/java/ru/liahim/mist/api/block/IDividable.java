package ru.liahim.mist.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.block.MistBlockSlab;
import ru.liahim.mist.block.MistBlockSlabWood;
import ru.liahim.mist.block.MistBlockStairs;
import ru.liahim.mist.block.MistBlockStep;
import ru.liahim.mist.block.MistBlockWall;
import ru.liahim.mist.block.MistWoodBlock;

public interface IDividable {

	public Block getFullBlock();

	public default IBlockState getFullState(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getFullState(state);
		return getFullBlock().getDefaultState();
	}

	public default Block getStepBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getStepBlock(state);
		return null;
	}

	public default Block getWallBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getWallBlock(state);
		return null;
	}

	public default IBlockState getSlabBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getSlabBlock(state);
		return null;
	}

	public default Block getStairsBlock(IBlockState state) {
		if (getFullBlock() instanceof IDividable) return ((IDividable)getFullBlock()).getStairsBlock(state);
		return null;
	}

	public default EnumActionResult chiselBlock(EntityPlayer player, ItemStack stack, World world, BlockPos pos, IBlockState state, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Block block = state.getBlock();
		Block step = this.getStepBlock(state);
		ItemStack drop = new ItemStack(step.getItemDropped(state, world.rand, 0));
		if (block instanceof MistBlockWall) {
			if (facing == EnumFacing.UP || hitY >= 0.5F) {
				if (!world.isRemote) {
					world.setBlockState(pos, step.getDefaultState().withProperty(BlockStairs.HALF, MistBlockStep.EnumHalf.TOP).withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING)), 11);
					this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
				}
			} else if (facing == EnumFacing.DOWN || hitY < 0.5F) {
				if (!world.isRemote) {
					world.setBlockState(pos, step.getDefaultState().withProperty(BlockStairs.HALF, MistBlockStep.EnumHalf.BOTTOM).withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING)), 11);
					this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
				}
			}
			return EnumActionResult.SUCCESS;
		} else if (block instanceof MistBlockStep) {
			if (!world.isRemote) {
				world.setBlockToAir(pos);
				this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
			}
			return EnumActionResult.SUCCESS;
		} else if (block instanceof MistBlockSlab) {
			EnumFacing face;
			if (block instanceof MistBlockSlabWood) {
				face = state.getValue(MistBlockSlabWood.ISROT) ? (hitX > 0.5F ? EnumFacing.WEST : EnumFacing.EAST) : (hitZ > 0.5F ? EnumFacing.NORTH : EnumFacing.SOUTH);
			} else {
				if (facing.getAxis() == Axis.Y) {
					float i = 1 - hitZ;
					face = hitX < hitZ ? (hitX < i ? EnumFacing.EAST : EnumFacing.NORTH) : (hitX < i ? EnumFacing.SOUTH : EnumFacing.WEST);
				} else if (facing.getAxis() == Axis.X) {
					face = hitZ < 0.25D ? EnumFacing.SOUTH : hitZ < 0.75D ? facing.getOpposite() : EnumFacing.NORTH;
				} else {
					face = hitX < 0.25D ? EnumFacing.EAST : hitX < 0.75D ? facing.getOpposite() : EnumFacing.WEST;
				}
			}
			world.setBlockState(pos, step.getDefaultState().withProperty(BlockStairs.HALF, state.getValue(BlockSlab.HALF) == MistBlockSlab.EnumBlockHalf.BOTTOM ? MistBlockStep.EnumHalf.TOP : MistBlockStep.EnumHalf.BOTTOM).withProperty(BlockStairs.FACING, face), 11);
			this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
			return EnumActionResult.SUCCESS;
		} else if (block instanceof MistBlockStairs) {
			boolean top = state.getValue(BlockStairs.HALF) == MistBlockStairs.EnumHalf.TOP;
			if (top && hitY < 0.5F) {
				world.setBlockState(pos, this.getSlabBlock(state).withProperty(BlockSlab.HALF, MistBlockSlab.EnumBlockHalf.TOP), 11);
				this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
				return EnumActionResult.SUCCESS;
			} else if (!top && hitY > 0.5F) {
				world.setBlockState(pos, this.getSlabBlock(state).withProperty(BlockSlab.HALF, MistBlockSlab.EnumBlockHalf.BOTTOM), 11);
				this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
				return EnumActionResult.SUCCESS;
			} else {
				EnumFacing face = state.getValue(BlockStairs.FACING);
				if ((face == EnumFacing.EAST && hitX < 0.5F) || (face == EnumFacing.WEST && hitX > 0.5F) ||
						(face == EnumFacing.NORTH && hitZ > 0.5F) || (face == EnumFacing.SOUTH && hitZ < 0.5F)) {
					world.setBlockState(pos, this.getWallBlock(state).getDefaultState().withProperty(BlockStairs.HALF, top ? MistBlockStairs.EnumHalf.TOP : MistBlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.FACING, face), 11);
					this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
					return EnumActionResult.SUCCESS;
				}
			}
		} else {
			EnumFacing face;
			if (block instanceof MistWoodBlock) {
				if (state.getValue(MistWoodBlock.TYPE) != MistWoodBlock.EnumType.PLANK || state.getValue(MistWoodBlock.AXIS) == MistWoodBlock.EnumAxis.Y || state.getValue(MistWoodBlock.AXIS) == MistWoodBlock.EnumAxis.NONE) return EnumActionResult.PASS;
				face = state.getValue(MistWoodBlock.AXIS) == MistWoodBlock.EnumAxis.Z ? (hitX > 0.5F ? EnumFacing.WEST : EnumFacing.EAST) : (hitZ > 0.5F ? EnumFacing.NORTH : EnumFacing.SOUTH);
			} else {
				if (facing.getAxis() == Axis.Y) {
					float i = 1 - hitZ;
					face = hitX < hitZ ? (hitX < i ? EnumFacing.EAST : EnumFacing.NORTH) : (hitX < i ? EnumFacing.SOUTH : EnumFacing.WEST);
				} else if (facing.getAxis() == Axis.X) {
					face = hitZ < 0.25D ? EnumFacing.SOUTH : hitZ < 0.75D ? facing.getOpposite() : EnumFacing.NORTH;
				} else {
					face = hitX < 0.25D ? EnumFacing.EAST : hitX < 0.75D ? facing.getOpposite() : EnumFacing.WEST;
				}
			}
			world.setBlockState(pos, this.getStairsBlock(state).getDefaultState().withProperty(BlockStairs.HALF, hitY < 0.5F ? MistBlockStairs.EnumHalf.TOP : MistBlockStairs.EnumHalf.BOTTOM).withProperty(BlockStairs.FACING, face), 11);
			this.dropItem(player, stack, drop, world, pos, state, facing, hitX, hitY, hitZ);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	public default void dropItem(EntityPlayer player, ItemStack stack, ItemStack drop, World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isAirBlock(pos.offset(facing))) pos = pos.offset(facing);
		Block.spawnAsEntity(world, pos, drop);
		SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
		world.playSound(null, pos, soundtype.getBreakSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
		stack.damageItem(1, player);
	}
}