package ru.liahim.mist.block;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.init.ModAdvancements;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MistBlockBranch extends MistBlock {

	public static final PropertyEnum<Axis> AXIS = PropertyEnum.<Axis>create("axis", Axis.class);
	public static final PropertyInteger SIZE = PropertyInteger.create("size", 0, 1);
	public static final PropertyBool DEBARKING = PropertyBool.create("deb");
	public static final PropertyBool POSITIVE_CONNECTION = PropertyBool.create("positive");
	public static final PropertyBool NEGATIVE_CONNECTION = PropertyBool.create("negative");
	private final int flammability;
	private final int fireSpeed;

	protected static final AxisAlignedBB X0 = new AxisAlignedBB(0.0D, 0.375D, 0.375D, 1.0D, 0.625D, 0.625D);
	protected static final AxisAlignedBB Y0 = new AxisAlignedBB(0.375D, 0.0D, 0.375D, 0.625D, 1.0D, 0.625D);
	protected static final AxisAlignedBB Z0 = new AxisAlignedBB(0.375D, 0.375D, 0.0D, 0.625D, 0.625D, 1.0D);
	protected static final AxisAlignedBB X1 = new AxisAlignedBB(0.0D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D);
	protected static final AxisAlignedBB Y1 = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);
	protected static final AxisAlignedBB Z1 = new AxisAlignedBB(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 1.0D);

	public MistBlockBranch(float hardness, float resistance, int flammability, int fireSpeed, MapColor color) {
		super(Material.WOOD, color);
		this.setHardness(hardness);
		if (resistance > 0) this.setResistance(resistance);
		this.setSoundType(SoundType.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(SIZE, 0).withProperty(AXIS, Axis.Y).withProperty(DEBARKING, false));
		this.flammability = flammability;
		this.fireSpeed = fireSpeed;
	}

	public MistBlockBranch(float hardness, float resistance, int flammability, int fireSpeed) {
		this(hardness, resistance, flammability, fireSpeed, Material.WOOD.getMaterialMapColor());
	}

	public MistBlockBranch(float hardness, int flammability, int fireSpeed, MapColor color) {
		this(hardness, -1, flammability, fireSpeed, color);
	}

	public MistBlockBranch(float hardness, int flammability, int fireSpeed) {
		this(hardness, -1, flammability, fireSpeed, Material.WOOD.getMaterialMapColor());
	}

	public MistBlockBranch(float hardness, MapColor color) {
		this(hardness, -1, 20, 5, color);
	}

	public MistBlockBranch(float hardness) {
		this(hardness, -1, 20, 5, Material.WOOD.getMaterialMapColor());
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		Axis axis = state.getValue(AXIS);
		int size = state.getValue(SIZE);
		if (axis == Axis.X) return size == 0 ? X0 : X1;
		else if (axis == Axis.Y) return size == 0 ? Y0 : Y1;
		else return size == 0 ? Z0 : Z1;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return getBoundingBox(state, world, pos);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = this.getStateFromMeta(meta);
		switch (facing.getAxis()) {
		case Z:
			return state.withProperty(AXIS, Axis.Z);
		case X:
			return state.withProperty(AXIS, Axis.X);
		case Y:
			return state.withProperty(AXIS, Axis.Y);
		default:
			return state;
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (!state.getValue(DEBARKING) && heldItem != null && heldItem.getItem() instanceof ItemAxe) {
			if (side.getAxis() != state.getValue(AXIS)) {
				if (!world.isRemote) {
					if (player instanceof EntityPlayerMP) ModAdvancements.CARVING.trigger((EntityPlayerMP) player, world, pos, false);
					world.setBlockState(pos, state.withProperty(DEBARKING, true));
					heldItem.damageItem(1, player);
					ItemStack stack = new ItemStack(MistItems.MULCH);
					pos = pos.offset(side);
					EntityItem entity_item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, stack);
					entity_item.setDefaultPickupDelay();
					world.spawnEntity(entity_item);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(state.withProperty(AXIS, Axis.Y));
	}

	@Override
	protected ItemStack getSilkTouchDrop(IBlockState state) {
		if (state.getValue(AXIS) == Axis.Y) return super.getSilkTouchDrop(state);
		else return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state.withProperty(AXIS, Axis.Y)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(SIZE, 1).withProperty(DEBARKING, false))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(SIZE, 1).withProperty(DEBARKING, true))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(SIZE, 0).withProperty(DEBARKING, false))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(SIZE, 0).withProperty(DEBARKING, true))));
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fireSpeed;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isFullCube(state);
    }

	@Override
	public boolean isFullCube(IBlockState state) { return false; }

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return state.getValue(AXIS) == side.getAxis() ? state.getValue(SIZE) == 0 ? BlockFaceShape.CENTER : BlockFaceShape.CENTER_BIG : BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		IBlockState state = world.getBlockState(pos);
		for (IProperty<?> prop : state.getProperties().keySet()) {
			if (prop.getName().equals("axis")) {
				world.setBlockState(pos, state.cycleProperty(prop));
				return true;
			}
		}
		return false;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		switch (rot) {
		case COUNTERCLOCKWISE_90:
		case CLOCKWISE_90:
			switch (state.getValue(AXIS)) {
			case X:
				return state.withProperty(AXIS, Axis.Z);
			case Z:
				return state.withProperty(AXIS, Axis.X);
			default:
				return state;
			}
		default:
			return state;
		}
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return state.getValue(AXIS) != Axis.Y;
	}

	@Override public boolean isWood(IBlockAccess world, BlockPos pos) { return true; }

	@Override
	public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos) {
		return state.getValue(AXIS) == Axis.Y;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		Axis axis = state.getValue(AXIS);
		int size = state.getValue(SIZE);
		IBlockState posState = world.getBlockState(pos.offset(EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis)));
		IBlockState negState = world.getBlockState(pos.offset(EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis)));
		boolean positive = (posState.getBlock() instanceof MistBlockBranch && posState.getValue(AXIS) != axis && posState.getValue(SIZE) >= size) ||
				(posState.getBlock() instanceof BlockFence && axis != Axis.Y && size == 0);
		boolean negative = (negState.getBlock() instanceof MistBlockBranch && negState.getValue(AXIS) != axis && negState.getValue(SIZE) >= size) ||
				(negState.getBlock() instanceof BlockFence && axis != Axis.Y && size == 0);
		return state.withProperty(POSITIVE_CONNECTION, positive).withProperty(NEGATIVE_CONNECTION, negative);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		int i = meta % 3;
		return this.getDefaultState().withProperty(AXIS, i == 0 ? Axis.Y : (i == 1 ? Axis.X : Axis.Z)).withProperty(DEBARKING, meta % 6 > 2).withProperty(SIZE, meta / 6);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		Axis a = state.getValue(AXIS);
		return (a == Axis.Y ? 0 : (a == Axis.X ? 1 : 2)) + (state.getValue(DEBARKING) ? 3 : 0) + state.getValue(SIZE) * 6;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { AXIS, SIZE, DEBARKING, POSITIVE_CONNECTION, NEGATIVE_CONNECTION });
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(state.withProperty(AXIS, Axis.Y)));
	}
}