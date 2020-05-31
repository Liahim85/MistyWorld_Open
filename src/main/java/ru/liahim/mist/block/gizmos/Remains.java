package ru.liahim.mist.block.gizmos;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IShiftPlaceable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.entity.EntityGraveBug;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.tileentity.TileEntityRemains;

public class Remains extends MistBlockContainer implements IShiftPlaceable {

	public static final PropertyInteger LAYERS = PropertyInteger.create("layers", 0, 7);
	public static final PropertyBool OLD = PropertyBool.create("old");

	public Remains() {
		super(Material.GROUND);
		this.setSoundType(SoundType.GROUND);
		this.setDefaultState(this.blockState.getBaseState().withProperty(LAYERS, 0).withProperty(OLD, false));
		this.setHardness(1.0F);
		this.setHarvestLevel("shovel", 0);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, (state.getValue(LAYERS) + 1) * 0.125D, 1.0D);
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, state.getValue(LAYERS) * 0.125D + 0.0625D, 1.0D);
	}

	@Override
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		return (world.getBlockState(pos).getValue(LAYERS)) < 4;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return (state.getValue(LAYERS)) == 7;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == EnumFacing.DOWN || state.getValue(LAYERS) == 7 ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (Block.getBlockFromItem(heldItem.getItem()) == this) return false;
		if (!heldItem.isEmpty()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof TileEntityRemains) {
				TileEntityRemains remains = (TileEntityRemains) te;
				if (heldItem.getItem() == MistItems.REMAINS) {
					if (side != EnumFacing.UP) return false;
					if (state.getValue(LAYERS) < 7) {
						NBTTagCompound tag = remains.writeItems(new NBTTagCompound());
						if (world.setBlockState(pos, state.withProperty(LAYERS, state.getValue(LAYERS) + 1))) {
							((TileEntityRemains) world.getTileEntity(pos)).readItems(tag);
							world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
							if (!player.capabilities.isCreativeMode) heldItem.shrink(1);
							return true;
						}
					} else if (world.getBlockState(pos.up()).getMaterial().isReplaceable()) {
						world.setBlockState(pos.up(), this.getDefaultState());
						world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
						if (!player.capabilities.isCreativeMode) heldItem.setCount(heldItem.getCount() - 1);
						return true;
					}
				} else if (remains.putStack(heldItem)) {
					world.playSound(null, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.8F, 1.0F);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isFullCube(state);
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return state.getValue(LAYERS) == 7;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if (state.getValue(OLD) && player instanceof EntityPlayerMP) {
			ModAdvancements.REMAINS.trigger((EntityPlayerMP)player, world, pos, state);
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote && !state.getValue(OLD) && !world.isSideSolid(pos.down(), EnumFacing.UP))
			world.destroyBlock(pos, true);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityRemains te = new TileEntityRemains();
		te.init(((meta & 7) + 1) * 3);
		return te;
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
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 7));
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IInventory) {
			dropInventoryItems(world, pos, (IInventory)te);
			world.updateComparatorOutputLevel(pos, this);
		}
		if (state.getValue(OLD)) {
			for (EnumFacing face : EnumFacing.HORIZONTALS) {
				IBlockState neighbor = world.getBlockState(pos.offset(face));
				if (neighbor.getBlock() == this && neighbor.getValue(OLD)) world.destroyBlock(pos.offset(face), true);
			}
			EntityGraveBug.spawnBug(world, pos, world.rand);
		}
		super.breakBlock(world, pos, state);
	}

	private static void dropInventoryItems(World world, BlockPos pos, IInventory inventory) {
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty()) spawnItemStack(world, pos, stack, world.rand);
		}
	}

	private static void spawnItemStack(World world, BlockPos pos, ItemStack stack, Random rand) {
		float f = rand.nextFloat() * 0.8F + 0.1F;
		float f1 = rand.nextFloat() * 0.8F + 0.1F;
		float f2 = rand.nextFloat() * 0.8F + 0.1F;
		while (!stack.isEmpty()) {
			EntityItem entity = new EntityItem(world, pos.getX() + (double) f, pos.getY() + (double) f1, pos.getZ() + (double) f2, stack.splitStack(rand.nextInt(21) + 10));
			entity.motionX = rand.nextGaussian() * 0.05D;
			entity.motionY = rand.nextGaussian() * 0.05D + 0.2D;
			entity.motionZ = rand.nextGaussian() * 0.05D;
			entity.setPickupDelay(20);
			world.spawnEntity(entity);
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.REMAINS;
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random rand) {
		return state.getValue(OLD) ? rand.nextInt(rand.nextInt(state.getValue(LAYERS) + 1) + 1) : state.getValue(LAYERS) + 1;
	}

	@Nullable
	public ILockableContainer getLockableContainer(World world, BlockPos pos) {
		return this.getContainer(world, pos, false);
	}

	@Nullable
	public ILockableContainer getContainer(World world, BlockPos pos, boolean allowBlocking) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (!(tileentity instanceof TileEntityRemains)) return null;
		if (!allowBlocking) return null;
		return (TileEntityRemains)tileentity;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
		return Container.calcRedstoneFromInventory(this.getLockableContainer(world, pos));
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		int layer = state.getValue(LAYERS);
		if (entity.posY < pos.getY() + (layer + 1) * 0.125D) {
			if (!world.isRemote && !state.getValue(OLD) && entity != null && entity instanceof EntityItem) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof TileEntityRemains) {
					TileEntityRemains remains = (TileEntityRemains) te;
					if (remains.cooldown < 0) {
						ItemStack stack = ((EntityItem)entity).getItem();
						if (stack.getItem() == MistItems.REMAINS) {
							if (layer < 7) {
								NBTTagCompound tag = remains.writeItems(new NBTTagCompound());
								if (world.setBlockState(pos, state.withProperty(LAYERS, state.getValue(LAYERS) + 1))) {
									((TileEntityRemains) world.getTileEntity(pos)).readItems(tag);
									((EntityItem)entity).getItem().shrink(1);
									entity.setPosition(entity.posX, entity.posY + 0.125D, entity.posZ);
								}
							}
						} else if (Block.getBlockFromItem(stack.getItem()) != this) {
							remains.pullStack((EntityItem)entity);
						}
						remains.cooldown = 8;
					} else --remains.cooldown;
				}
			}
			entity.motionX *= 0.4D;
			entity.motionZ *= 0.4D;
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return ((state.getValue(OLD) ? 1 : 0) << 3) | state.getValue(LAYERS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(LAYERS, meta & 7).withProperty(OLD, meta > 7);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { LAYERS, OLD });
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (side == EnumFacing.UP) return true;
		IBlockState checkState = world.getBlockState(pos.offset(side));
		return checkState.getBlock() == this && checkState.getValue(LAYERS) >= state.getValue(LAYERS) ? false : super.shouldSideBeRendered(state, world, pos, side);
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return state.getValue(LAYERS) == 7 ? new ItemStack(this, 1, 7) : new ItemStack(MistItems.REMAINS, 1);
	}

	@Override
	public boolean onShiftPlacing(World world, BlockPos pos, ItemStack stack, EntityPlayer player, float hitX, float hitY, float hitZ, BlockFaceShape bfs) {
		if (bfs != BlockFaceShape.SOLID) return false;
		if (stack.getItem() == MistItems.REMAINS && world.setBlockState(pos, this.getDefaultState())) {
			world.playSound(null, pos, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			//if (player instanceof EntityPlayerMP) ModAdvancements.COMPOST.trigger((EntityPlayerMP)player, stack);
			if (!player.capabilities.isCreativeMode) stack.setCount(stack.getCount() - 1);
			return true;
		}
		return false;
	}
}