package ru.liahim.mist.item;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.block.MistBlockSlab;
import ru.liahim.mist.block.MistBlockSlabWood;
import ru.liahim.mist.block.MistBlockStairs;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
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
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistStep extends ItemBlock {

	private final Block fullBlock;

	public ItemMistStep(Block block) {
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
			if (state.getBlock() == this.block) {
				EnumFacing side = state.getValue(BlockStairs.FACING);
				if (facing == side.getOpposite()) {
					IBlockState slabState = ((IDividable)this.fullBlock).getSlabBlock(itemState);
					if (slabState.getBlock() instanceof MistBlockSlab) {
						boolean top = state.getValue(BlockStairs.HALF) == EnumHalf.BOTTOM;
						slabState = slabState.withProperty(BlockSlab.HALF, top ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
						if (slabState.getBlock() instanceof MistBlockSlabWood) {
							slabState = slabState.withProperty(MistBlockSlabWood.ISROT, side.getAxis() == Axis.X);
						}
						AxisAlignedBB aabb = slabState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, slabState)) {
							SoundType soundtype = this.fullBlock.getSoundType(slabState, world, pos, player);
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
			//Slab
			else if (state.getBlock() instanceof MistBlockSlab && MistBlockSlab.getClearSlabState(state) == ((IDividable)this.fullBlock).getSlabBlock(itemState).withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM)) {
				boolean top = state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP;
				if (top ? facing == EnumFacing.DOWN : facing == EnumFacing.UP) {
					IBlockState stairsState = ((IDividable)this.fullBlock).getStairsBlock(itemState).getDefaultState();
					if (stairsState.getBlock() instanceof BlockStairs) {
						EnumFacing face;
						if (state.getBlock() instanceof MistBlockSlabWood) {
							face = state.getValue(MistBlockSlabWood.ISROT) ? (hitX > 0.5F ? EnumFacing.EAST : EnumFacing.WEST) : (hitZ > 0.5F ? EnumFacing.SOUTH : EnumFacing.NORTH);
						} else {
							float i = 1 - hitZ;
							face = hitX < hitZ ? (hitX < i ? EnumFacing.WEST : EnumFacing.SOUTH) : (hitX < i ? EnumFacing.NORTH : EnumFacing.EAST);
						}
						stairsState = stairsState.withProperty(BlockStairs.FACING, face).withProperty(BlockStairs.HALF, top ? EnumHalf.TOP : EnumHalf.BOTTOM);
						AxisAlignedBB aabb = stairsState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, stairsState)) {
							SoundType soundtype = stairsState.getBlock().getSoundType(stairsState, world, pos, player);
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
			//Stairs
			else if (state.getBlock() == ((IDividable)this.fullBlock).getStairsBlock(itemState) && state.getBlock() instanceof BlockStairs) {
				if (hitX > 0 && hitX < 1 && hitY > 0 && hitY < 1 && hitZ > 0 && hitZ < 1) {
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
		if (state.getBlock() == this.block) {
			if (state.getValue(BlockStairs.FACING) == facing.getOpposite()) return true;
		} else if (this.fullBlock != null) {
			if (state.getBlock() instanceof MistBlockSlab && MistBlockSlab.getClearSlabState(state) == ((IDividable)this.fullBlock).getSlabBlock(itemState).withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM)) {
				boolean top = state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP;
				if (top ? facing == EnumFacing.DOWN : facing == EnumFacing.UP) return true;
			} else if (state.getBlock() instanceof MistBlockStairs &&
					state.getBlock() == ((IDividable)this.fullBlock).getStairsBlock(itemState)) {
				return true;
			}
		}
		pos = pos.offset(facing);
		state = world.getBlockState(pos);
		return state.getBlock() == this.block || (this.fullBlock != null && ((state.getBlock() instanceof MistBlockSlab &&
				MistBlockSlab.getClearSlabState(state) == ((IDividable)this.fullBlock).getSlabBlock(itemState).withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM)) ||
				state.getBlock() == ((IDividable)this.fullBlock).getStairsBlock(itemState))) ? true :
			super.canPlaceBlockOnSide(world, blockpos, facing, player, stack);
	}

	private boolean tryPlace(EntityPlayer player, ItemStack stack, World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (this.fullBlock != null) {
			IBlockState state = world.getBlockState(pos);
			IBlockState itemState = this.block.getStateFromMeta(stack.getMetadata());
			if (state.getBlock() == this.block) {
				boolean top = state.getValue(BlockStairs.HALF) == EnumHalf.BOTTOM;
				if (top ? (facing == EnumFacing.DOWN || hitY > 0.5F) : (facing == EnumFacing.UP || hitY < 0.5F)) {
					IBlockState slabState = ((IDividable)this.fullBlock).getSlabBlock(itemState);
					if (slabState.getBlock() instanceof MistBlockSlab) {
						slabState = slabState.withProperty(BlockSlab.HALF, top ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM);
						if (slabState.getBlock() instanceof MistBlockSlabWood) {
							slabState = slabState.withProperty(MistBlockSlabWood.ISROT, state.getValue(BlockStairs.FACING).getAxis() == Axis.X);
						}
						AxisAlignedBB aabb = slabState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, slabState)) {
							SoundType soundtype = this.fullBlock.getSoundType(slabState, world, pos, player);
							world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
							stack.shrink(1);
						}
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
						}
						return true;
					}
				}
			} else if (state.getBlock() instanceof MistBlockSlab && MistBlockSlab.getClearSlabState(state) == ((IDividable)this.fullBlock).getSlabBlock(itemState).withProperty(BlockSlab.HALF, EnumBlockHalf.BOTTOM)) {
				boolean top = state.getValue(BlockSlab.HALF) == EnumBlockHalf.TOP;
				if (top ? (facing == EnumFacing.UP || hitY < 0.5F) : (facing == EnumFacing.DOWN || hitY > 0.5F)) {
					IBlockState stairsState = ((IDividable)this.fullBlock).getStairsBlock(itemState).getDefaultState();
					if (stairsState.getBlock() instanceof BlockStairs) {
						EnumFacing side;
						if (state.getBlock() instanceof MistBlockSlabWood) {
							side = state.getValue(MistBlockSlabWood.ISROT) ? ((hitX > 0.0F && hitX < 1.0F) ? (hitX > 0.5F ? EnumFacing.EAST : EnumFacing.WEST) : facing.getOpposite()) :
								((hitZ > 0.0F && hitZ < 1.0F) ? (hitZ > 0.5F ? EnumFacing.SOUTH : EnumFacing.NORTH) : facing.getOpposite());
						} else {
							if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
								float i = 1 - hitZ;
								side = hitX < hitZ ? (hitX < i ? EnumFacing.WEST : EnumFacing.SOUTH) : (hitX < i ? EnumFacing.NORTH : EnumFacing.EAST);
							} else side = facing.getOpposite();
						}
						stairsState = stairsState.withProperty(BlockStairs.FACING, side).withProperty(BlockStairs.HALF, top ? EnumHalf.TOP : EnumHalf.BOTTOM);
						AxisAlignedBB aabb = stairsState.getCollisionBoundingBox(world, pos);
						if (aabb != Block.NULL_AABB && world.checkNoEntityCollision(aabb.offset(pos)) && world.setBlockState(pos, stairsState)) {
							SoundType soundtype = stairsState.getBlock().getSoundType(stairsState, world, pos, player);
							world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
							stack.shrink(1);
						}
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP)player, pos, stack);
						}
						return true;
					}
				}
			} else if (state.getBlock() == ((IDividable)this.fullBlock).getStairsBlock(itemState) && state.getBlock() instanceof MistBlockStairs) {
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