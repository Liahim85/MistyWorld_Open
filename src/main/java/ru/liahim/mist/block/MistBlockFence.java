package ru.liahim.mist.block;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemLead;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistBlockFence extends BlockFence {

	public static final PropertyInteger SIZE = MistBlockBranch.SIZE;
	public static final PropertyBool DEBARKING = MistBlockBranch.DEBARKING;
	private final int flammability;
	private final int fireSpeed;
	public static final AxisAlignedBB BIG_PILLAR_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.5D, 0.75D);
	protected static final AxisAlignedBB[] BIG_BOUNDING_BOXES = new AxisAlignedBB[] {new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D), new AxisAlignedBB(0.25D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D), new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)};
	private final Block branch;

	public MistBlockFence(Block branch, float hardness, int flammability, int fireSpeed) {
		super(Material.WOOD, Material.WOOD.getMaterialMapColor());
		this.setHardness(hardness);
		this.setSoundType(SoundType.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(SIZE, 0).withProperty(DEBARKING, false).withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false));
        this.flammability = flammability;
		this.fireSpeed = fireSpeed;
		this.branch = branch;
	}

	public MistBlockFence(Block branch, float hardness) {
		this(branch, hardness, 20, 5);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
		if (!isActualState) state = state.getActualState(world, pos);
		if (state.getValue(SIZE) == 0) addCollisionBoxToList(pos, entityBox, collidingBoxes, PILLAR_AABB);
		else addCollisionBoxToList(pos, entityBox, collidingBoxes, BIG_PILLAR_AABB);
		if (!state.getValue(NORTH) && !state.getValue(EAST) && !state.getValue(SOUTH) && !state.getValue(WEST)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
			addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
		} else {
			if (state.getValue(NORTH)) addCollisionBoxToList(pos, entityBox, collidingBoxes, NORTH_AABB);
			if (state.getValue(EAST)) addCollisionBoxToList(pos, entityBox, collidingBoxes, EAST_AABB);
			if (state.getValue(SOUTH)) addCollisionBoxToList(pos, entityBox, collidingBoxes, SOUTH_AABB);
			if (state.getValue(WEST)) addCollisionBoxToList(pos, entityBox, collidingBoxes, WEST_AABB);
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		state = this.getActualState(state, source, pos);
		return state.getValue(SIZE) == 0 ? BOUNDING_BOXES[getBoundingBoxIdx(state)] : BIG_BOUNDING_BOXES[getBoundingBoxIdx(state)];
	}

	private static int getBoundingBoxIdx(IBlockState state) {
		int i = 0;
		if (state.getValue(NORTH)) i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
		if (state.getValue(EAST)) i |= 1 << EnumFacing.EAST.getHorizontalIndex();
		if (state.getValue(SOUTH)) i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
		if (state.getValue(WEST)) i |= 1 << EnumFacing.WEST.getHorizontalIndex();
		if (i == 0) i = 15;
		return i;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.getHeldItem(hand).getItem() == Items.LEAD && state.getValue(SIZE) == 0) {
			if (!world.isRemote) return ItemLead.attachToFence(player, world, pos);
			else return true;
		}
		return false;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return this.getMetaFromState(state);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		drops.add(new ItemStack(this.branch, 1, this.damageDropped(state) * 3));
		drops.add(new ItemStack(Items.STICK));
		drops.add(new ItemStack(Items.STICK));
	}

	@Override
	public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {
		for (int size : SIZE.getAllowedValues()) {
			items.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(SIZE, size).withProperty(DEBARKING, false))));
			items.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(SIZE, size).withProperty(DEBARKING, true))));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(SIZE, meta >> 1).withProperty(DEBARKING, meta % 2 == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(SIZE) << 1 | (state.getValue(DEBARKING) ? 1 : 0);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { SIZE, DEBARKING, NORTH, EAST, WEST, SOUTH });
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fireSpeed;
	}
}