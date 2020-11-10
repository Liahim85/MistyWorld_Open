package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.world.MistWorld;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistBlockWall extends MistBlockStep {

	/**
     * B: .. T: x.
     * B: .. T: x.
     */
    protected static final AxisAlignedBB AABB_QTR_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 1.0D, 1.0D);
    /**
     * B: .. T: .x
     * B: .. T: .x
     */
    protected static final AxisAlignedBB AABB_QTR_EAST = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    /**
     * B: .. T: xx
     * B: .. T: ..
     */
    protected static final AxisAlignedBB AABB_QTR_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5D);
    /**
     * B: .. T: ..
     * B: .. T: xx
     */
    protected static final AxisAlignedBB AABB_QTR_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 1.0D, 1.0D, 1.0D);
    /**
     * B: .. T: x.
     * B: .. T: ..
     */
    protected static final AxisAlignedBB AABB_OCT_NW = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.5D, 1.0D, 0.5D);
    /**
     * B: .. T: .x
     * B: .. T: ..
     */
    protected static final AxisAlignedBB AABB_OCT_NE = new AxisAlignedBB(0.5D, 0.0D, 0.0D, 1.0D, 1.0D, 0.5D);
    /**
     * B: .. T: ..
     * B: .. T: x.
     */
    protected static final AxisAlignedBB AABB_OCT_SW = new AxisAlignedBB(0.0D, 0.0D, 0.5D, 0.5D, 1.0D, 1.0D);
    /**
     * B: .. T: ..
     * B: .. T: .x
     */
    protected static final AxisAlignedBB AABB_OCT_SE = new AxisAlignedBB(0.5D, 0.0D, 0.5D, 1.0D, 1.0D, 1.0D);

	public MistBlockWall(IBlockState modelState, boolean tick) {
		super(modelState, tick);
	}

	public MistBlockWall(IBlockState modelState) {
		this(modelState, false);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		BlockStairs.EnumShape shape = this.getActualState(state, source, pos).getValue(SHAPE);
		if (shape == BlockStairs.EnumShape.STRAIGHT) {
			return getCollQuarterBlock(state);
		} else if (shape == BlockStairs.EnumShape.OUTER_LEFT || shape == BlockStairs.EnumShape.OUTER_RIGHT) {
			return getCollEighthBlock(state);
		} else return FULL_BLOCK_AABB;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox,
			List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean par7) {
		state = this.getActualState(state, worldIn, pos);
		for (AxisAlignedBB axisalignedbb : getCollisionBoxList(state)) {
			addCollisionBoxToList(pos, entityBox, collidingBoxes, axisalignedbb);
		}
	}

	private static List<AxisAlignedBB> getCollisionBoxList(IBlockState bstate) {
		List<AxisAlignedBB> list = Lists.<AxisAlignedBB> newArrayList();
		BlockStairs.EnumShape shape = bstate.getValue(SHAPE);
		if (shape == BlockStairs.EnumShape.STRAIGHT || shape == BlockStairs.EnumShape.INNER_LEFT || shape == BlockStairs.EnumShape.INNER_RIGHT) {
			list.add(getCollQuarterBlock(bstate));
		}
		if (shape != BlockStairs.EnumShape.STRAIGHT) {
			list.add(getCollEighthBlock(bstate));
		}
		return list;
	}

	private static AxisAlignedBB getCollQuarterBlock(IBlockState bstate) {
		switch (bstate.getValue(FACING)) {
			case NORTH:
			default: return AABB_QTR_NORTH;
			case SOUTH: return AABB_QTR_SOUTH;
			case WEST: return AABB_QTR_WEST;
			case EAST: return AABB_QTR_EAST;
		}
	}

	private static AxisAlignedBB getCollEighthBlock(IBlockState bstate) {
		EnumFacing enumfacing = bstate.getValue(FACING);
		EnumFacing enumfacing1;

		switch (bstate.getValue(SHAPE)) {
		case OUTER_LEFT:
		default:
			enumfacing1 = enumfacing;
			break;
		case OUTER_RIGHT:
			enumfacing1 = enumfacing.rotateY();
			break;
		case INNER_RIGHT:
			enumfacing1 = enumfacing.getOpposite();
			break;
		case INNER_LEFT:
			enumfacing1 = enumfacing.rotateYCCW();
		}

		switch (enumfacing1) {
			case NORTH:
			default: return AABB_OCT_NW;
			case SOUTH: return AABB_OCT_SE;
			case WEST: return AABB_OCT_SW;
			case EAST: return AABB_OCT_NE;
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
		if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
			float i = 1 - hitZ;
			state = state.withProperty(FACING, hitX < hitZ ? (hitX < i ? EnumFacing.WEST : EnumFacing.SOUTH) : (hitX < i ? EnumFacing.NORTH : EnumFacing.EAST));
		} else if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
			state = state.withProperty(FACING, hitZ < 0.25D ? EnumFacing.NORTH : hitZ < 0.75D ? facing.getOpposite() : EnumFacing.SOUTH);
		} else {
			state = state.withProperty(FACING, hitX < 0.25D ? EnumFacing.WEST : hitX < 0.75D ? facing.getOpposite() : EnumFacing.EAST);
		}
		return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || hitY <= 0.5D) ? state.withProperty(HALF, BlockStairs.EnumHalf.TOP) : state.withProperty(HALF, BlockStairs.EnumHalf.BOTTOM);
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState blockState, World worldIn, BlockPos pos, Vec3d start, Vec3d end) {
		List<RayTraceResult> list = Lists.<RayTraceResult> newArrayList();
		for (AxisAlignedBB axisalignedbb : getCollisionBoxList(this.getActualState(blockState, worldIn, pos))) {
			list.add(this.rayTrace(pos, start, end, axisalignedbb));
		}
		RayTraceResult raytraceresult1 = null;
		double d1 = 0.0D;
		for (RayTraceResult raytraceresult : list) {
			if (raytraceresult != null) {
				double d0 = raytraceresult.hitVec.squareDistanceTo(end);
				if (d0 > d1) {
					raytraceresult1 = raytraceresult;
					d1 = d0;
				}
			}
		}
		return raytraceresult1;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (this.tick && !world.isRemote) {
			if (this.isMossBlock()) {
				if (rand.nextInt(4) == 0 && MistWorld.isPosInFog(world, pos.getY())) {
					world.setBlockState(pos, this.getDefaultState());
				}
			} else if (this.isNormalBlock() && rand.nextInt(500) == 0 &&
				!MistWorld.isPosInFog(world, pos.getY()) && world.getBiome(pos).getRainfall() >= 0.3F) {
				EnumFacing[] faces = EnumFacing.HORIZONTALS;
				EnumFacing face = state.getValue(FACING);
				for (EnumFacing side : faces) {
					if (side != face.getOpposite() && world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
						return;
					}
				}
				boolean check = world.getBlockState(pos.down()).getBlock() instanceof MistGrass;
				if (!check && !world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (world.getBlockState(pos.offset(side)).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (!check) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (!world.isSideSolid(pos.offset(side), side.getOpposite()) && world.getBlockState(pos.offset(side).down()).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (check) world.setBlockState(pos, this.getMossBlock().getDefaultState().withProperty(FACING, face).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (this.tick && !world.isRemote && this.isMossBlock()) {
			EnumFacing[] faces = EnumFacing.HORIZONTALS;
			EnumFacing face = state.getValue(FACING);
			for (EnumFacing side : faces) {
				if (side != face.getOpposite() && world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, this.getNormalBlock().getDefaultState().withProperty(FACING, face).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
					break;
				}
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (this.tick && !world.isRemote && this.isMossBlock()) {
			EnumFacing[] faces = EnumFacing.HORIZONTALS;
			EnumFacing face = state.getValue(FACING);
			for (EnumFacing side : faces) {
				if (side != face.getOpposite() && world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, this.getNormalBlock().getDefaultState().withProperty(FACING, face).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
					break;
				}
			}
		}
	}

	@Override
	protected Block getNormalBlock() {
		if (this == MistBlocks.COBBLESTONE_MOSS_WALL) return MistBlocks.COBBLESTONE_WALL;
		else if (this == MistBlocks.STONE_BRICK_MOSS_WALL) return MistBlocks.STONE_BRICK_WALL;
		else return this;
	}

	@Override
	protected Block getMossBlock() {
		if (this == MistBlocks.COBBLESTONE_WALL) return MistBlocks.COBBLESTONE_MOSS_WALL;
		else if (this == MistBlocks.STONE_BRICK_WALL) return MistBlocks.STONE_BRICK_MOSS_WALL;
		else return this;
	}

	@Override
	protected boolean isNormalBlock() {
		return this == MistBlocks.COBBLESTONE_WALL || this == MistBlocks.STONE_BRICK_WALL;
	}

	@Override
	protected boolean isMossBlock() {
		return this == MistBlocks.COBBLESTONE_MOSS_WALL || this == MistBlocks.STONE_BRICK_MOSS_WALL;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		return face == state.getValue(FACING) ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return this.getStepBlock(state).getItemDropped(state, rand, fortune);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}
}