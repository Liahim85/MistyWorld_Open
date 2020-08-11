package ru.liahim.mist.block.upperplant;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistBlock;

public class MistTinderFungus extends MistBlock {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	private static final double d = 0.25D;
	private static final double u = 0.5625D;
	public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.25D, d, 0.625D, 0.75D, u, 1.0D);
	public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.25D, d, 0.0D, 0.75D, u, 0.375D);
	public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, d, 0.25D, 0.375D, u, 0.75D);
	public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.625D, d, 0.25D, 1.0D, u, 0.75D);

	public MistTinderFungus() {
		super(Material.WOOD);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		this.setHardness(2);
	}

	@Override
	public String getUnlocalizedName() {
		return "item.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing face = state.getValue(FACING);
		if (face == EnumFacing.NORTH) return NORTH_AABB;
		if (face == EnumFacing.SOUTH) return SOUTH_AABB;
		if (face == EnumFacing.EAST) return EAST_AABB;
		else return WEST_AABB;
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
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.TINDER_FUNGUS;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		EnumFacing face = state.getValue(FACING);
		if (!world.isSideSolid(pos.offset(face.getOpposite()), face)) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(MistItems.TINDER_FUNGUS);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}
}