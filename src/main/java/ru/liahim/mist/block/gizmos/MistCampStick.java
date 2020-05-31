package ru.liahim.mist.block.gizmos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IShiftPlaceable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.tileentity.TileEntityCampStick;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistCampStick extends MistBlockContainer implements IShiftPlaceable {

	public static final PropertyDirection DIR = BlockHorizontal.FACING;

	protected static final AxisAlignedBB EAST = new AxisAlignedBB(0.625D, 0.0D, 0.4375D, 1.0625D, 0.75D, 0.625D);
	protected static final AxisAlignedBB WEST = new AxisAlignedBB(-0.0625D, 0.0D, 0.375D, 0.375D, 0.75D, 0.5625D);
	protected static final AxisAlignedBB SOUTH = new AxisAlignedBB(0.375D, 0.0D, 0.625D, 0.5625D, 0.75D, 1.0625D);
	protected static final AxisAlignedBB NORTH = new AxisAlignedBB(0.4375D, 0.0D, -0.0625D, 0.625D, 0.75D, 0.375D);

	public MistCampStick() {
		super(Material.GROUND, MapColor.AIR);
		this.setSoundType(SoundType.WOOD);
		this.setHardness(0.4F);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(DIR)) {
		case EAST:
			return EAST;
		case WEST:
			return WEST;
		case SOUTH:
			return SOUTH;
		case NORTH:
			return NORTH;
		default:
			return FULL_BLOCK_AABB;
		}
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			if ((fromPos.equals(pos.down()) && !world.isSideSolid(fromPos, EnumFacing.UP)) || world.getBlockState(fromPos).getMaterial().isLiquid() ||
					(fromPos.equals(pos.offset(state.getValue(DIR))) && !world.isAirBlock(fromPos) && world.getBlockState(fromPos).getBlock() != MistBlocks.CAMPFIRE)) {
				world.destroyBlock(pos, true);
			} else {
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof TileEntityCampStick) {
					this.isWork(world, pos.offset(state.getValue(DIR)), (TileEntityCampStick)te);
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (hand == EnumHand.MAIN_HAND) {
			ItemStack heldItem = player.getHeldItem(hand);
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof TileEntityCampStick) {
				ItemStack food = ((TileEntityCampStick)te).getFood();
				if (player.isSneaking()) {
					if (heldItem.isEmpty()) {
						if (!food.isEmpty()) {
							player.setHeldItem(hand, food);
							((TileEntityCampStick)te).clearFood();
							world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
							return true;
						}
					} else if (heldItem.getCount() < heldItem.getMaxStackSize() && heldItem.isItemEqual(food)) {
						heldItem.grow(1);
						((TileEntityCampStick)te).clearFood();
						world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
						return true;
					}
				} else {
					TileEntity camp = world.getTileEntity(pos.offset(state.getValue(DIR)));
					if (camp == null || (camp instanceof TileEntityCampfire && !((TileEntityCampfire)camp).hasCookingTool())) {
						if (((TileEntityCampStick)te).setFood(heldItem)) {
							world.playSound(null, pos, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.BLOCKS, 0.25F, 1.2F - world.rand.nextFloat() * 0.4F);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onShiftPlacing(World world, BlockPos pos, @Nonnull ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs) {
		if (bfs != BlockFaceShape.SOLID) return false;
		if (stack.getItem() == Items.STICK) {
			float i = 1 - hitZ;
			EnumFacing dir = player.getHorizontalFacing();
			EnumFacing dir2 = hitX < hitZ ? (hitX < i ? EnumFacing.WEST : EnumFacing.SOUTH) : (hitX < i ? EnumFacing.NORTH : EnumFacing.EAST);
			if (world.getBlockState(pos.offset(dir2)).getBlock() == MistBlocks.CAMPFIRE) {}
			else if (dir != dir2 && world.getBlockState(pos.offset(dir)).getBlock() == MistBlocks.CAMPFIRE) { dir2 = dir; }
			else {
				if (dir.getAxis() == Axis.X) dir2 = hitZ < 0.5F ? EnumFacing.NORTH : EnumFacing.SOUTH;
				else dir2 = hitX < 0.5F ? EnumFacing.WEST : EnumFacing.EAST;
				if (world.getBlockState(pos.offset(dir2)).getBlock() == MistBlocks.CAMPFIRE) {}
				else {
					dir = dir.getOpposite();
					if (world.getBlockState(pos.offset(dir)).getBlock() == MistBlocks.CAMPFIRE) { dir2 = dir; }
					else {
						dir2 = dir2.getOpposite();
						if (world.getBlockState(pos.offset(dir2)).getBlock() == MistBlocks.CAMPFIRE) {}
						else dir2 = EnumFacing.DOWN;
					}
				}
			}
			if (dir2 != EnumFacing.DOWN) {
				if (world.setBlockState(pos, this.getDefaultState().withProperty(DIR, dir2))) {
					TileEntityCampStick te = ((TileEntityCampStick)world.getTileEntity(pos));
					te.setFacing(dir2);
					this.isWork(world, pos.offset(dir2), te);
					world.playSound(null, pos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 0.8F, world.rand.nextFloat() * 0.4F + 0.8F);
					stack.shrink(1);
					return true;
				}
			}
		}
		return false;
	}

	private void isWork(World world, BlockPos pos, TileEntityCampStick te) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == MistBlocks.CAMPFIRE) {
			int stage = state.getValue(MistCampfire.STAGE);
			if (stage == 8 || stage == 9) te.setWork(true);
			else te.setWork(false);
		} else te.setWork(false);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCampStick();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.STICK));
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof TileEntityCampStick) {
			ItemStack stack = ((TileEntityCampStick)te).getFood();
			if (!stack.isEmpty()) InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
			world.updateComparatorOutputLevel(pos, this);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);
			if (te != null && te instanceof TileEntityCampStick && ((TileEntityCampStick)te).getFood().isEmpty()) {
				world.destroyBlock(pos, true);
			}
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DIR).getHorizontalIndex();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(DIR, EnumFacing.getHorizontal(meta));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { DIR });
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}
}