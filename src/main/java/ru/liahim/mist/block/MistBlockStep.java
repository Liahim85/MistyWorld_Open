package ru.liahim.mist.block;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IDividable;
import ru.liahim.mist.api.block.IMossable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.util.FacingHelper;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistBlockStep extends BlockStairs implements IDividable, IMossable {

	private final Block fullBlock;
	protected final boolean tick;

	public MistBlockStep(IBlockState modelState, boolean tick) {
		super(modelState);
		this.useNeighborBrightness = true;
        this.setLightOpacity(0);
        this.fullBlock = modelState.getBlock();
		this.tick = tick;
		this.setTickRandomly(tick);
	}

	public MistBlockStep(IBlockState modelState) {
		this(modelState, false);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos) {
		return this.getActualState(state, worldIn, pos).getBoundingBox(worldIn, pos).offset(pos);
	}

	/*@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return ADD_AABB;
	}*/

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		BlockStairs.EnumShape shape = this.getActualState(state, source, pos).getValue(SHAPE);
		if (shape == BlockStairs.EnumShape.STRAIGHT) {
			return getCollQuarterBlock(state);
		} else if (shape == BlockStairs.EnumShape.OUTER_LEFT || shape == BlockStairs.EnumShape.OUTER_RIGHT) {
			return getCollEighthBlock(state);
		} else return state.getValue(HALF) == BlockStairs.EnumHalf.TOP ? AABB_SLAB_BOTTOM : AABB_SLAB_TOP;
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
		boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;

		switch (bstate.getValue(FACING)) {
		case NORTH:
		default:
			return flag ? AABB_QTR_BOT_NORTH : AABB_QTR_TOP_NORTH;
		case SOUTH:
			return flag ? AABB_QTR_BOT_SOUTH : AABB_QTR_TOP_SOUTH;
		case WEST:
			return flag ? AABB_QTR_BOT_WEST : AABB_QTR_TOP_WEST;
		case EAST:
			return flag ? AABB_QTR_BOT_EAST : AABB_QTR_TOP_EAST;
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

		boolean flag = bstate.getValue(HALF) == BlockStairs.EnumHalf.TOP;

		switch (enumfacing1) {
		case NORTH:
		default:
			return flag ? AABB_OCT_BOT_NW : AABB_OCT_TOP_NW;
		case SOUTH:
			return flag ? AABB_OCT_BOT_SE : AABB_OCT_TOP_SE;
		case WEST:
			return flag ? AABB_OCT_BOT_SW : AABB_OCT_TOP_SW;
		case EAST:
			return flag ? AABB_OCT_BOT_NE : AABB_OCT_TOP_NE;
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		IBlockState state = super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(SHAPE, BlockStairs.EnumShape.STRAIGHT);
		if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
			float i = 1 - hitZ;
			state = state.withProperty(FACING, hitX < hitZ ? (hitX < i ? EnumFacing.WEST : EnumFacing.SOUTH) : (hitX < i ? EnumFacing.NORTH : EnumFacing.EAST));
		} else if (facing == EnumFacing.EAST || facing == EnumFacing.WEST) {
			state = state.withProperty(FACING, hitZ < 0.33D ? EnumFacing.NORTH : hitZ < 0.66D ? facing.getOpposite() : EnumFacing.SOUTH);
		} else {
			state = state.withProperty(FACING, hitX < 0.33D ? EnumFacing.WEST : hitX < 0.66D ? facing.getOpposite() : EnumFacing.EAST);
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
				boolean up = state.getValue(HALF) == EnumHalf.BOTTOM;
				EnumFacing[] faces = up ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS;
				EnumFacing face = state.getValue(FACING);
				for (EnumFacing side : faces) {
					if (side != face.getOpposite() && world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
						return;
					}
				}
				boolean check = !up && world.getBlockState(pos.down()).getBlock() instanceof MistGrass;
				if (!check && !world.isSideSolid(pos.up(), EnumFacing.DOWN)) {
					for (EnumFacing side : EnumFacing.HORIZONTALS) {
						if (world.getBlockState(pos.offset(side)).getBlock() instanceof MistGrass) {
							check = true;
							break;
						}
					}
				}
				if (!up && !check) {
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
			EnumFacing[] faces = state.getValue(HALF) == EnumHalf.BOTTOM ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS;
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
			EnumFacing[] faces = state.getValue(HALF) == EnumHalf.BOTTOM ? FacingHelper.NOTDOWN : EnumFacing.HORIZONTALS;
			EnumFacing face = state.getValue(FACING);
			for (EnumFacing side : faces) {
				if (side != face.getOpposite() && world.getBlockState(pos.offset(side)).getBlock() == MistBlocks.ACID_BLOCK) {
					world.setBlockState(pos, this.getNormalBlock().getDefaultState().withProperty(FACING, face).withProperty(HALF, state.getValue(HALF)).withProperty(SHAPE, state.getValue(SHAPE)));
					break;
				}
			}
		}
	}

	protected Block getNormalBlock() {
		if (this == MistBlocks.COBBLESTONE_MOSS_STEP) return MistBlocks.COBBLESTONE_STEP;
		else if (this == MistBlocks.STONE_BRICK_MOSS_STEP) return MistBlocks.STONE_BRICK_STEP;
		else return this;
	}

	protected Block getMossBlock() {
		if (this == MistBlocks.COBBLESTONE_STEP) return MistBlocks.COBBLESTONE_MOSS_STEP;
		else if (this == MistBlocks.STONE_BRICK_STEP) return MistBlocks.STONE_BRICK_MOSS_STEP;
		else return this;
	}

	protected boolean isNormalBlock() {
		return this == MistBlocks.COBBLESTONE_STEP || this == MistBlocks.STONE_BRICK_STEP;
	}

	protected boolean isMossBlock() {
		return this == MistBlocks.COBBLESTONE_MOSS_STEP || this == MistBlocks.STONE_BRICK_MOSS_STEP;
	}

	@Override
	public boolean setMossy(IBlockState state, World world, BlockPos pos) {
		if (state.getBlock() == MistBlocks.COBBLESTONE_STEP) return world.setBlockState(pos, MistBlocks.COBBLESTONE_MOSS_STEP.getDefaultState().withProperty(HALF, state.getValue(HALF)).withProperty(FACING, state.getValue(FACING)));
		else if (state.getBlock() == MistBlocks.STONE_BRICK_STEP) return world.setBlockState(pos, MistBlocks.STONE_BRICK_MOSS_STEP.getDefaultState().withProperty(HALF, state.getValue(HALF)).withProperty(FACING, state.getValue(FACING)));
		return false;
	}

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return false;
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return true;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	@Nullable
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if (this == MistBlocks.COBBLESTONE_STEP || this == MistBlocks.COBBLESTONE_MOSS_STEP) return MistItems.ROCKS;
		else if (this == MistBlocks.STONE_BRICK_STEP || this == MistBlocks.STONE_BRICK_MOSS_STEP) return MistItems.BRICK;
		return super.getItemDropped(state, rand, fortune);
	}

	@Override
	public Block getFullBlock() {
		return this.fullBlock;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fullBlock.getFlammability(world, pos, face);
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return this.fullBlock.getFireSpreadSpeed(world, pos, face);
	}
}