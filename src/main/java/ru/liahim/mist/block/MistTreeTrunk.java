package ru.liahim.mist.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IMistSoil;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;

import com.google.common.base.Predicate;

/**@author Liahim*/
public abstract class MistTreeTrunk extends MistBlock implements IPlantable {
	
	public static final PropertyInteger SIZE = PropertyInteger.create("size", 0, 4);
	public static final PropertyBool NODE = PropertyBool.create("node");
	public static final PropertyDirection DIR = PropertyDirection.create("dir", new Predicate<EnumFacing>() {
		@Override
		public boolean apply(@Nullable EnumFacing face) {
			return face != EnumFacing.DOWN;
		}
	});
	/**Reducing the thickness of the branches when changing direction from vertical to horizontal*/
	protected final boolean VGShift;
	/**Reducing the thickness of the branches when changing direction from horizontal to horizontal*/
	protected final boolean GGShift;
	/**Reducing the thickness of the branches when changing direction from horizontal to vertical*/
	protected final boolean GVShift;
	/**Max branch width from 0 to 4*/
	protected final int maxBranchWidth;
	/**The maximum width of the branches to be closed bark*/
	protected final int maxCapWidth;
	protected final MistTreeLeaves leaves;
	protected static final PropertyDirection LDIR = MistTreeLeaves.DIR;
	/**Growth parameters*/
	protected final int newGrowthChance;
	protected final int minBranchDistance;
	private final int growthSpeed;
	protected final int[] nodeDistance;

	public MistTreeTrunk(float hardness, int maxBranchWidth, int maxCapWidth, boolean VGShift, boolean GGShift, boolean GVShift,
			MistTreeLeaves leaves, int newGrowthChance, int minBranchDistance, int growthSpeed, int[] nodeDistance) {
		super(Material.WOOD, Material.WOOD.getMaterialMapColor());
		this.setSoundType(SoundType.WOOD);
        this.setHardness(hardness);
		this.setDefaultState(this.blockState.getBaseState().withProperty(SIZE, 0).withProperty(DIR, EnumFacing.UP).withProperty(NODE, false));
		this.VGShift = VGShift;
		this.GGShift = GGShift;
		this.GVShift = GVShift;
		this.maxBranchWidth = MathHelper.clamp(maxBranchWidth, 0, 3);
		this.maxCapWidth = maxCapWidth;
		this.leaves = leaves;
		this.newGrowthChance = newGrowthChance;
		this.minBranchDistance = minBranchDistance;
		this.growthSpeed = Math.max(1, growthSpeed);
		this.nodeDistance = nodeDistance;
		this.setTickRandomly(true);
	}

	public int getGrowthSpeed() {
		return growthSpeed;
	}

	public MistTreeLeaves getLeaves() {
		return this.leaves;
	}

	public boolean isDesertTree() {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		EnumFacing dir = getDir(state);
		int size = state.getActualState(source, pos).getValue(SIZE);
		if (size == 4)
			return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
		else {
			double i = 0.0625D * size;
			if (dir == EnumFacing.EAST) {
				if (size < 3)
					return new AxisAlignedBB(-0.375D + i, 0.375D - i, 0.375D - i, 0.625D + i, 0.625D + i, 0.625D + i);
				else return new AxisAlignedBB(-0.125D, 0.125D, 0.125D, 0.875D, 0.875D, 0.875D);
			} else if (dir == EnumFacing.WEST) {
				if (size < 3)
					return new AxisAlignedBB(0.375D - i, 0.375D - i, 0.375D - i, 1.375D - i, 0.625D + i, 0.625D + i);
				else return new AxisAlignedBB(0.125D, 0.125D, 0.125D, 1.125D, 0.875D, 0.875D);
			} else if (dir == EnumFacing.SOUTH) {
				if (size < 3)
					return new AxisAlignedBB(0.375D - i, 0.375D - i, -0.375D + i, 0.625D + i, 0.625D + i, 0.625D + i);
				else return new AxisAlignedBB(0.125D, 0.125D, -0.125D, 0.875D, 0.875D, 0.875D);
			} else if (dir == EnumFacing.NORTH) {
				if (size < 3)
					return new AxisAlignedBB(0.375D - i, 0.375D - i, 0.375D - i, 0.625D + i, 0.625D + i, 1.375D - i);
				else return new AxisAlignedBB(0.125D, 0.125D, 0.125D, 0.875D, 0.875D, 1.125D);
			} else {
				if (size < 3)
					return new AxisAlignedBB(0.375D - i, -0.375D + i, 0.375D - i, 0.625D + i, 0.625D + i, 0.625D + i);
				else return new AxisAlignedBB(0.125D, -0.125D, 0.125D, 0.875D, 0.875D, 0.875D);
			}
		}
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return getBoundingBox(state, worldIn, pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag advanced) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		if (player != null && player.isCreative()) tooltip.add(I18n.format("tile.mist.live_tree.tooltip"));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		ItemStack heldItem = player.getHeldItem(hand);
		if (player.isCreative() && heldItem != null && heldItem.getItem() instanceof ItemAxe) {
			if (!world.isRemote) {
				EnumFacing face = getDir(state);
				if (world.getBlockState(pos.offset(face.getOpposite())).getBlock() == this) {
					world.setBlockState(pos, state.withProperty(NODE, !state.getValue(NODE)));
				} else {
					int size = state.getValue(SIZE);
					if (size < 4) {
						world.setBlockState(pos, state.withProperty(SIZE, size + 1));
					} else {
						world.setBlockState(pos, state.withProperty(SIZE, 0));
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		if (!world.isRemote && world.getChunkFromBlockCoords(pos).isPopulated()) {
			EnumFacing dir = getDir(state);
			BlockPos checkPos = pos.offset(dir.getOpposite());
			IBlockState checkState = world.getBlockState(checkPos);
			if (checkState.getBlock() != this || getDir(checkState) == dir.getOpposite()) {
				if (!world.isSideSolid(checkPos, dir)) world.destroyBlock(pos, false);
			} else {
				if (dir == EnumFacing.UP) world.setBlockState(pos, getUpState(world, state, checkState, checkPos));
				else {
					checkPos = pos.up();
					checkState = world.getBlockState(checkPos);
					if (checkState.getBlock() == this && getDir(checkState) == EnumFacing.UP)
						world.setBlockState(checkPos, getUpState(world, checkState, state, pos));
				}
			}
		}
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
		BlockPos basePos = pos.offset(side.getOpposite());
		IBlockState soil = world.getBlockState(basePos);
		return this.canPlaceBlockAt(world, pos) && (soil.getBlock() == this ||
				(side == EnumFacing.UP && soil.getBlock().canSustainPlant(soil, world, basePos, side, this)));		
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
		float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
			BlockPos downPos = pos.down();
			IBlockState downState = world.getBlockState(downPos);
			if (downState.getBlock() == this)
				return getUpState(world, this.getDefaultState(), downState, downPos);
			else return this.getDefaultState();
		}
		else return this.getDefaultState().withProperty(DIR, facing);
	}

	private IBlockState getUpState(World world, IBlockState state, IBlockState baseState, BlockPos basePos) {
		EnumFacing dir = getDir(baseState);
		if (dir == EnumFacing.UP && !baseState.getValue(NODE))
			return state.withProperty(SIZE, baseState.getValue(SIZE));
		return state.withProperty(SIZE, Math.max(0, baseState.getActualState(world, basePos).getValue(SIZE) - (this.GVShift && dir != EnumFacing.UP ? 1 : 0)));
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isFullCube(state);
    }

	@Override
	public boolean isFullCube(IBlockState state) {
		return !state.getValue(NODE) && state.getValue(SIZE) == 4;
    }

	@Override
	public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {
		return isFullCube(state);
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return isFullCube(state);
    }

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return state.getActualState(world, pos).getValue(SIZE) == 4 ? BlockFaceShape.SOLID : BlockFaceShape.UNDEFINED;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		destroyBranch(world, pos, state, true);
	}

	public void destroyBranch(World world, BlockPos pos, IBlockState state, boolean drop) {
		BlockPos posUp;
		IBlockState stateUp;
		for (EnumFacing face : DIR.getAllowedValues()) {
			if (face != getDir(state).getOpposite()) {
				posUp = pos.offset(face);
				stateUp = world.getBlockState(posUp);
				if (stateUp.getBlock() == this && getDir(stateUp) == face) {
					world.setBlockState(posUp, getActualState(world, stateUp, state, posUp, getDir(stateUp)));
					world.destroyBlock(posUp, drop);
				} else if (stateUp.getBlock() instanceof MistTreeLeaves && stateUp.getValue(LDIR) == face)
					world.destroyBlock(posUp, drop);
			}
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		EnumFacing face = getDir(state);
		if (face == EnumFacing.UP && !state.getValue(NODE))
			return state.withProperty(NODE, checkCornerCap(world, pos, face, state.getValue(SIZE))).withProperty(DIR, face);
		BlockPos basePos = pos.offset(face.getOpposite());
		IBlockState base = world.getBlockState(basePos);
		if (base.getBlock() == this && getDir(base) != face.getOpposite()) {
			return getActualState(world, state, base.getActualState(world, basePos), pos, face);
		}
		return state.withProperty(NODE, checkCornerCap(world, pos, face, state.getValue(SIZE)));
	}

	private IBlockState getActualState(IBlockAccess world, IBlockState state, IBlockState base, BlockPos pos, EnumFacing face) {
		EnumFacing faceB = getDir(base);
		int sizeB = base.getValue(SIZE);
		boolean node = state.getValue(NODE);
		boolean join = face != faceB;
		boolean canShift = canShift(face, faceB);
		int size = Math.max(0, Math.min(sizeB, join ? this.maxBranchWidth + (canShift ? 1 : 0) : 4) - (node ? 1 : 0) - (join && canShift ? 1 : 0));
		boolean corner = checkCornerCap(world, pos, face, size);
		return state.withProperty(SIZE, size).withProperty(NODE, corner);
	}

	/**Necessary the "join" checking!!!*/
	protected boolean canShift(EnumFacing face, EnumFacing faceB) {
		if (face == EnumFacing.UP) return this.GVShift;
		else if (faceB == EnumFacing.UP) return this.VGShift;
		return this.GGShift;
	}

	private boolean checkCornerCap(IBlockAccess world, BlockPos pos, EnumFacing face, int size) {
		IBlockState checkState;
		if (face == EnumFacing.UP && size > maxCapWidth) {
			checkState = world.getBlockState(pos.up());
			return checkState.getBlock() == this && getDir(checkState) == EnumFacing.UP;
		} else {
			for (EnumFacing check : DIR.getAllowedValues()) {
				if (check != face.getOpposite()) {
					checkState = world.getBlockState(pos.offset(check));
					if (checkState.getBlock() == this && getDir(checkState) == check)
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumFacing fase = state.getValue(DIR);
		boolean node = state.getValue(NODE);
		if (fase == EnumFacing.EAST)
			return state.getValue(SIZE) == 4 ? 14 : 6 + (node ? 1 : 0);
		else if (fase == EnumFacing.WEST)
			return state.getValue(SIZE) == 4 ? 15 : 8 + (node ? 1 : 0);
		else if (fase == EnumFacing.NORTH)
			return 10 + (node ? 1 : 0);
		else if (fase == EnumFacing.SOUTH)
			return 12 + (node ? 1 : 0);
		else return node ? 5 : state.getValue(SIZE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta < 5) return this.getDefaultState().withProperty(SIZE, meta);
		else if (meta == 5) return this.getDefaultState().withProperty(NODE, true);
		else if (meta < 8) return this.getDefaultState().withProperty(DIR, EnumFacing.EAST).withProperty(NODE, meta == 7);
		else if (meta < 10) return this.getDefaultState().withProperty(DIR, EnumFacing.WEST).withProperty(NODE, meta == 9);
		else if (meta < 12) return this.getDefaultState().withProperty(DIR, EnumFacing.NORTH).withProperty(NODE, meta == 11);
		else if (meta < 14) return this.getDefaultState().withProperty(DIR, EnumFacing.SOUTH).withProperty(NODE, meta == 13);
		else return this.getDefaultState().withProperty(DIR, meta == 14 ? EnumFacing.EAST : EnumFacing.WEST).withProperty(SIZE, 4);
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.getValue(SIZE) == 4 ? state : state.withProperty(DIR, rot.rotate(state.getValue(DIR)));
    }

	@Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.getValue(SIZE) == 4 ? state : state.withRotation(mirrorIn.toRotation(state.getValue(DIR)));
    }

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { DIR, NODE, SIZE });
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		return EnumPushReaction.DESTROY;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos,
		EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Plains;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return this.getDefaultState();
	}
	
	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		return getDir(state) != EnumFacing.UP;
	}

	@Override
    public boolean isWood(IBlockAccess world, BlockPos pos) {
        return true;
    }

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 20;
    }

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }

	/**Work only with tree trunk. Needed block check!!!*/
	public EnumFacing getDir(IBlockState state) {
		return state.getValue(SIZE) == 4 ? EnumFacing.UP : state.getValue(DIR);
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int size = state.getActualState(world, pos).getValue(SIZE);
		if (size == 0) {
			if (rand.nextBoolean()) ret.add(getBranch());
			else ret.add(new ItemStack(Items.STICK));
		} else if (size == 1) {
			ret.add(getBranch());
		} else if (size == 2) {
			ret.add(getTrunk());
		} else if (size == 3) {
			if (rand.nextBoolean()) ret.add(getTrunk());
			else ret.add(getBlock());
		} else {
			if (getNode() != null) {
				if (fortune > 3) fortune = 3;
				if (rand.nextInt(512) < 1 + fortune * 32) ret.add(getNode());
				else ret.add(getBlock());
			} else ret.add(getBlock());
		}
    }

	protected abstract ItemStack getBranch();
	protected abstract ItemStack getTrunk();
	protected abstract ItemStack getBlock();
	protected abstract ItemStack getNode();

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	/**Fired from ServerEventHandler if Player != null.*/
	public boolean dropSapling(World world, BlockPos pos) {
		boolean sapling = false;
		IBlockState state = world.getBlockState(pos);
		if (state.getActualState(world, pos).getValue(SIZE) == 0) {
			EnumFacing dir = state.getValue(DIR);
			IBlockState upState = world.getBlockState(pos.offset(dir));
			int count = 0;
			if (upState.getBlock() == this.leaves && upState.getValue(LDIR) == dir) {
				for (EnumFacing face : DIR.getAllowedValues()) {
					if (face != dir && face != dir.getOpposite()) {
						upState = world.getBlockState(pos.offset(face));
						if (upState.getBlock() == this.leaves && upState.getValue(LDIR) == face) ++count;
					}
				}
				sapling = world.rand.nextInt((5 - count) + 2) == 0;
			}
		}
		if (sapling) {
			world.destroyBlock(pos, false);
			spawnAsEntity(world, pos, getSapling());
		}
		return sapling;
	}

	protected ItemStack getSapling() {
		return new ItemStack(MistBlocks.TREE_SAPLING, 1, MistTreeSapling.EnumType.getMetaByTree(this));
	}

	///////////////
	////GROWING////
	///////////////

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			int size = state.getActualState(world, pos).getValue(SIZE);
			EnumFacing dir = getDir(state);
			if (size <= getGrowingThickness(dir, rand)) {
				if (rand.nextInt(growthSpeed) == 0) {
					EnumFacing opposite = dir.getOpposite();
					ArrayList<EnumFacing> availableGrowthDirection = new ArrayList<EnumFacing>();
					boolean isBud = true;
					IBlockState checkState;
					for (EnumFacing checkDir : DIR.getAllowedValues()) {					
						if (checkDir != opposite) {
							checkState = world.getBlockState(pos.offset(checkDir));
							if (checkState.getBlock() == this.leaves && checkState.getValue(LDIR) == checkDir) {
								availableGrowthDirection.add(checkDir);
							} else if (isBud && checkState.getBlock() == this && getDir(checkState) == checkDir) isBud = false;
						}
					}
					if (canCheckBranch(world, pos, availableGrowthDirection, isBud, size, rand)) {
						/**Checking*/
						int counter = 1;
						int totalLength = 1;
						int branchLength = 0;
						int firstSizeChangeDistance = 0;
						int firstBendDistance = 0;
						int firstBranchDistance = 0;
						int trunckLength = 1;
						BlockPos fixPos = null;
						ArrayList<Integer> segments = new ArrayList<Integer>();
						ArrayList<BlockPos> nodes = new ArrayList<BlockPos>();
						if (state.getValue(NODE)) {
							segments.add(1);
							nodes.add(pos);
							counter = 0;
						}
						BlockPos rootPos = pos;
						BlockPos downPos = pos.offset(opposite);
						IBlockState rootState = state;
						IBlockState downState = null;
						int previousSize = size;
						EnumFacing currentDir = dir;
						EnumFacing previousDir = dir;
						for (;;) {
							downState = world.getBlockState(downPos);
							if (downState.getBlock() == this) {
								++counter;
								currentDir = getDir(downState);
								if (currentDir == previousDir.getOpposite()) {
									world.destroyBlock(downPos, false);
									return;
								}
								if (firstBranchDistance == 0 && totalLength <= this.minBranchDistance) {
									for (EnumFacing branchDir : DIR.getAllowedValues()) {
										if (firstBranchDistance == 0 && branchDir != previousDir && branchDir != currentDir.getOpposite()) {
											checkState = world.getBlockState(downPos.offset(branchDir));
											if ((checkState.getBlock() == this && getDir(checkState) == branchDir) ||
													(checkState.getBlock() == this.leaves && checkState.getValue(LDIR) == branchDir))
												firstBranchDistance = totalLength;
										}
									}
								}
								if (previousSize == 0 && (counter == 1 || currentDir != previousDir)) {
									int curretnSize = downState.getActualState(world, downPos).getValue(SIZE);
									if (curretnSize > 0) {
										firstSizeChangeDistance = totalLength;
										previousSize = curretnSize;
									}
									if (counter == 1) {
										boolean check = false;
										if (curretnSize == 0) check = true;
										else if (curretnSize == 1 && currentDir != previousDir && canShift(previousDir, currentDir)) check = true;
										if (check) {
											world.setBlockState(rootPos, this.getDefaultState().withProperty(DIR, previousDir));
											counter += segments.get(segments.size() - 1);
											segments.remove(segments.size() - 1);
											nodes.remove(nodes.size() - 1);
										}
									}
								}
								if (currentDir != previousDir) {
									if (firstBendDistance == 0) firstBendDistance = totalLength;
									checkState = world.getBlockState(downPos.offset(currentDir));
									if (checkState.getBlock() == this && getDir(checkState) == currentDir) {
										branchLength = totalLength;
										fixPos = downPos;
									}
									previousDir = currentDir;
									trunckLength = 0;
								}
								if (downState.getValue(NODE)) {
									segments.add(counter);
									nodes.add(downPos);
									counter = 0;
								}
								++trunckLength;
								++totalLength;
								rootState = downState;
								rootPos = downPos;
								downPos = downPos.offset(currentDir.getOpposite());
							} else {
								segments.add(counter);
								break;
							}
						}
						long posRand = MistWorld.getPosRandom(world, rootPos, 0);
						int soilDepth = getSoilDepth(world, rootPos);
						int minTruckLength = getMinTrunckLength(world, rootPos, posRand, soilDepth);
						int maxTreeHeight = getMaxTreeHeight(world, rootPos, minTruckLength, posRand, soilDepth);
						int maxTruckLength = getMaxTrunckLength(world, rootPos, maxTreeHeight, minTruckLength, posRand, soilDepth);
						/**Can growth*/
						int canGrowth = canGrowth(world, pos, state, size, dir, availableGrowthDirection, isBud, totalLength, branchLength,
								firstSizeChangeDistance, firstBendDistance, firstBranchDistance, trunckLength, minTruckLength, maxTruckLength,
								maxTreeHeight, segments, nodes, fixPos, rootPos, rootState, downPos, downState, rand);
						/**Growth*/
						if (canGrowth == 2) {
							ArrayList<EnumFacing> growthDirs = chooseGrowthDir(world, pos, state, size, dir, availableGrowthDirection, isBud,
									totalLength, branchLength, firstSizeChangeDistance, firstBendDistance, firstBranchDistance, trunckLength,
									minTruckLength, maxTreeHeight, segments, nodes, fixPos, rootPos, rootState, rand);
							if (!growthDirs.isEmpty()) {
								boolean growth = false;
								for (EnumFacing face : growthDirs) {
									if (growth(world, pos, pos.offset(face), size, dir, face, availableGrowthDirection, totalLength, branchLength, firstSizeChangeDistance,
											firstBendDistance, firstBranchDistance, trunckLength, minTruckLength, maxTreeHeight, rootPos, rootState, downPos, downState, rand)
									&& face == dir && !segments.isEmpty()) growth = true;
								}
								if (growth) {
									segments.set(0, segments.get(0) + 1);
									if (branchLength > 0) ++branchLength;
									if (firstSizeChangeDistance > 0) ++firstSizeChangeDistance;
									if (firstBendDistance > 0) ++firstBendDistance;
									if (firstBranchDistance > 0) ++firstBranchDistance;
									if (trunckLength == totalLength) ++trunckLength;
									++totalLength;
								}
							}
						}
						/**New leaves*/
						else if (canGrowth == 1) {
							setNewLeaves(world, pos, state, size, dir, availableGrowthDirection, isBud, rootPos, rootState, downPos, downState, rand);
						}
						/**Move nodes*/
						if (canGrowth > 0) {
							if (!availableGrowthDirection.isEmpty()) {
								ArrayList<BlockPos> shiftedNodes = getShiftedNodes(world, size, totalLength, branchLength,
										firstSizeChangeDistance, firstBendDistance, firstBranchDistance, segments, nodes, rand);
								boolean lastNodeMoved = false;
								if (!shiftedNodes.isEmpty()) {
									lastNodeMoved = true;
									for (BlockPos shiftPos : shiftedNodes) {
										if (lastNodeMoved)
											lastNodeMoved = shiftNode(world, shiftPos);
									}
								}
								if (lastNodeMoved) segments.set(segments.size() - 1, segments.get(segments.size() - 1) + 1);
								int newNodeDistance = newNodeDistance(world, rootPos, rootState, totalLength, branchLength, firstSizeChangeDistance,
										firstBendDistance, firstBranchDistance, trunckLength, minTruckLength, maxTreeHeight, segments, rand);
								if (newNodeDistance > 0) {
									createNewNode(world, rootPos, rootState, newNodeDistance);
								}
							}
						}
						/**Set dead*/
						else if (canGrowth < 0) setDead(world, pos, state, size, dir, availableGrowthDirection, isBud, rand);
					}
				}
			} else {
				IBlockState checkState;
				EnumFacing opposite = dir.getOpposite();
				if (rand.nextInt(getDestroyChance(size)) == 0) {
					ArrayList<EnumFacing> availableGrowthDirection = new ArrayList<EnumFacing>();
					boolean isBud = true;
					for (EnumFacing checkDir : DIR.getAllowedValues()) {					
						if (checkDir != opposite) {
							checkState = world.getBlockState(pos.offset(checkDir));
							if (checkState.getBlock() == this.leaves && checkState.getValue(LDIR) == checkDir) {
								availableGrowthDirection.add(checkDir);
							} else if (isBud && checkState.getBlock() == this && getDir(checkState) == checkDir) isBud = false;
						}
					}
					setDead(world, pos, state, size, dir, availableGrowthDirection, isBud, rand);
				} else {
					dir = (EnumFacing)DIR.getAllowedValues().toArray()[rand.nextInt(DIR.getAllowedValues().size())];
					BlockPos checkPos = pos.offset(dir);
					checkState = world.getBlockState(checkPos);
					if (checkState.getBlock() == this.leaves && checkState.getValue(LDIR) == dir) {
						world.setBlockToAir(checkPos);
					}
				}
				/**Make older*/
				if (size == 4 && rand.nextInt(growthSpeed) == 0) {
					checkState = world.getBlockState(pos.offset(opposite));
					if (checkState.getBlock() != this && SoilHelper.getHumus(checkState) != 3) {
						makeOlder(world, pos, state, pos.offset(opposite), checkState, rand, false);
					}
				}
			}
		}
	}

	protected int getSoilDepth(World world, BlockPos rootPos) {
		return 0;
	}

	protected int trySetDead(World world, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, boolean isBud, Random rand) {
		if (rootState.getValue(SIZE) < 4 && rand.nextInt(growthSpeed * 1000) == 0) {
			SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			return -1;
		} else return isBud ? 1 : 0;
	}

	protected abstract int getMinTrunckLength(World world, BlockPos rootPos, long posRand, int soilDepth);

	protected abstract int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth);

	protected int getMaxTrunckLength(World world, BlockPos rootPos, int maxTreeHeight, int minTrunckLength, long posRand, int soilDepth) {
		return minTrunckLength;
	}

	/**Returns the maximum thickness of the branches, capable of forming a buds.*/
	protected int getGrowingThickness(EnumFacing face, Random rand) {
		return 0;
		//if (face == EnumFacing.UP) return this.VGShift ? 2 : 1;
		//return this.GVShift || this.GGShift ? 2 : 1;
	}

	protected boolean canCheckBranch(World world, BlockPos pos, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, int size, Random rand) {
		return isBud || !availableGrowthDirection.isEmpty();
	}

	/**Can the branch continue to growth? Check branch and soil configuration.
	 * Returns index: (2) growth & shift nodes; (1) set new leaves & shift nodes; (0) nothing; (-1) set dead.*/
	protected abstract int canGrowth(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, int totalLength,
		int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength, int maxTrunckLength, int maxTreeHeight,
		ArrayList<Integer> segments, ArrayList<BlockPos> nodes, @Nullable BlockPos fixPos, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand);

	/**Choosing direction of growth.*/
	protected abstract ArrayList<EnumFacing> chooseGrowthDir(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud,
			int totalLength, int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength, int maxTreeHeight,
			ArrayList<Integer> segments, ArrayList<BlockPos> nodes, @Nullable BlockPos fixPos, BlockPos rootPos, IBlockState rootState, Random rand);

	/**Increases tree age. Starts the stage of dying.*/
	public void makeOlder(World world, BlockPos rootPos, IBlockState root, BlockPos soilPos, IBlockState soil, Random rand, boolean now) {
		if (root.getValue(SIZE) == 4) {
			EnumFacing dir = root.getValue(DIR);
			if (dir == EnumFacing.UP) {
				/**Young --> Adult*/
				if (now || rand.nextInt(2000) == 0) world.setBlockState(rootPos, root.withProperty(DIR, EnumFacing.EAST));
			} else if (dir == EnumFacing.EAST) {
				/**Adult --> Old*/
				if (now || rand.nextInt(2000) == 0) world.setBlockState(rootPos, root.withProperty(DIR, EnumFacing.WEST));
			} else if (dir == EnumFacing.WEST) {
				if (!now) dissemination(world, rootPos, soilPos, soil, rand);
				/**Old --> Dying*/
				if (now || rand.nextInt(500) == 0) {
					SoilHelper.setSoil(world, soilPos, soil, 3, 2);
				}
			}
		}
	}

	/**Placement of the world's young shoots.*/
	protected void dissemination(World world, BlockPos pos, BlockPos soilPos, IBlockState soil, Random rand) {
		if (rand.nextInt(10) == 0) {
			int randX = rand.nextInt(16) - 8;
			int randZ = rand.nextInt(16) - 8;
			BlockPos target = pos.add(randX, 8, randZ);
			while (target.getY() > MistWorld.getFogMaxHight() && !(world.getBlockState(target).getBlock() instanceof MistSoil)) target = target.down();
			if (world.getBlockState(target).getBlock() instanceof MistSoil && world.canSeeSky(target.up()) &&
					world.getBlockState(target.up()).getMaterial().isReplaceable() && world.getLightBrightness(target.up()) > 0.45) {
				/** Place sapling!!! */
				world.setBlockState(target.up(), MistBlocks.TREE_SAPLING.getDefaultState().withProperty(MistTreeSapling.TYPE, MistTreeSapling.EnumType.getTypeByTree(this)));
				/**Set dying*/
				if (rand.nextBoolean()) SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			}
		}
	}

	protected boolean growth(World world, BlockPos oldPos, BlockPos newPos, int size, EnumFacing baseDir, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection,
			int totalLength, int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength,
			int minTrunckLength, int maxTreeHeight, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		boolean growth = false;
		boolean check = false;
		BlockPos targetPos = newPos.offset(dir);
		if (checkEnvironment(world, targetPos)) {
			world.setBlockState(newPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, size > 0));
			world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, dir).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
			growth = true;
		} else if (branchLength == 0 && dir == baseDir) {
			for (EnumFacing face : DIR.getAllowedValues()) {
				if (face != dir && face != dir.getOpposite()) {
					targetPos = newPos.offset(face);
					if ((face == EnumFacing.UP || rand.nextInt(3) == 0) && checkEnvironment(world, targetPos)) {
						world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						check = true;
					}
				}
			}
			if (check) {
				world.setBlockState(newPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, size > 0));
				growth = true;
			}
		}
		if (growth) {
			IBlockState targetState;
			for (EnumFacing face : DIR.getAllowedValues()) {
				if (face != dir && face != dir.getOpposite()) {
					targetPos = newPos.offset(face);
					targetState = world.getBlockState(targetPos);
					if (checkEnvironment(world, targetPos, false) || (check && targetState.getBlock() == this.leaves && targetState.getValue(LDIR) == face)) {
						if (availableGrowthDirection.contains(face)) {
							if (world.getBlockState(oldPos.offset(face)).getBlock() == this.leaves &&
									isLeavesRemoved(world, totalLength, branchLength, minTrunckLength, trunckLength, firstBranchDistance, oldPos.offset(face), targetPos, face, rand))
								world.setBlockToAir(oldPos.offset(face));
							world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						} else if (face == EnumFacing.UP || rand.nextInt(dir == EnumFacing.UP ? 2 : 3) > 0) {
							world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						}
					}
				}
			}
			if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
		}
		return growth;
	}

	protected boolean isLeavesRemoved(World world, int totalLength, int branchLength, int minTrunckLength, int trunckLength, int firstBranchDistance, BlockPos pos, BlockPos newPos, EnumFacing dir, Random rand) {
		return true;
	}

	protected boolean removeHumus(World world, BlockPos soilPos, IBlockState soilState) {
		if (soilState.getBlock() instanceof MistSoil) {
			int humus = SoilHelper.getHumus(soilState);
			if (humus > 0 && SoilHelper.setSoil(world, soilPos, soilState, humus - 1, 2)) return true;
		}
		return false;
	}

	protected boolean checkEnvironment(World world, BlockPos pos) {
		return checkEnvironment(world, pos, true);
	}

	protected boolean checkEnvironment(World world, BlockPos pos, boolean checkLight) {
		return canPlaceBlockAt(world, pos) && (!checkLight || checkLight(world, pos) > 0.45) && !(world.getBlockState(pos).getBlock() instanceof BlockLiquid);
	}

	protected float checkLight(World world, BlockPos pos) {
		int sky = world.getLightFor(EnumSkyBlock.SKY, pos);
		int block = world.getLightFor(EnumSkyBlock.BLOCK, pos);
		if (sky < block) {
			return world.provider.getLightBrightnessTable()[block];
		}
        return world.provider.getLightBrightnessTable()[Math.max(sky - world.getSkylightSubtracted(), 0)];
	}

	protected void setNewLeaves(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection,
		boolean isBud, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		if (isBud && this.newGrowthChance > 0 && rand.nextInt(this.newGrowthChance) == 0 &&
				availableGrowthDirection.size() < (dir == EnumFacing.UP ? 5 : 4)) {
			EnumFacing face;
			if (!availableGrowthDirection.contains(dir)) face = dir;
			else if (!availableGrowthDirection.contains(EnumFacing.UP)) face = EnumFacing.UP;
			else face = (EnumFacing)DIR.getAllowedValues().toArray()[rand.nextInt(DIR.getAllowedValues().size())];
			if (face != dir.getOpposite()) {
				BlockPos checkPos = pos.offset(face);
				if (!availableGrowthDirection.contains(face) && checkEnvironment(world, checkPos)) {
					world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
					if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
				} else {
					IBlockState leaves = world.getBlockState(checkPos);
					if (leaves.getBlock() == this.leaves && leaves.getValue(LDIR) == face) {
						this.leaves.updateLeaves(world, checkPos, leaves, rootPos, rootState, soilPos, soil, rand);
					}
				}
			}
		}
	}

	protected void setDead(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir,
		ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, Random rand) {
		if (!availableGrowthDirection.isEmpty()) {
			pos = pos.offset(availableGrowthDirection.get(rand.nextInt(availableGrowthDirection.size())));
			world.setBlockToAir(pos);
			fertilizeSoil(world, pos, rand);
		} else if (isBud) {
			world.setBlockToAir(pos);
			fertilizeSoil(world, pos, rand);
		}
	}

	protected void fertilizeSoil(World world, BlockPos pos, Random rand) {
		pos = pos.down();
		for (;;) {
			if (pos.getY() <= MistWorld.fogMaxHight_S + 4) break;
			if (!world.isSideSolid(pos, EnumFacing.UP)) {
				if (!world.isSideSolid(pos, EnumFacing.DOWN)) pos = pos.down();
				else break;
			} else {
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof MistSoil) {
					int hum = SoilHelper.getHumus(state);
					if (hum < 2) SoilHelper.setSoil(world, pos, state, hum + 1, 2);
				}
				break;
			}
		}
	}

	/**Returns an array of nodes to be shifted in order from the branch to the root.*/
	protected ArrayList<BlockPos> getShiftedNodes(World world, int size, int totalLength, int branchLength, int firstSizeChangeDistance,
			int firstBendDistance, int firstBranchDistance, ArrayList<Integer> segments, ArrayList<BlockPos> nodes, Random rand) {
		ArrayList<BlockPos> shiftedNodes = new ArrayList<BlockPos>();
		boolean moveAll = false;
		int segment;
		int total = 0;
		int i = 0;
		for (BlockPos pos : nodes) {
			segment = segments.get(i);
			total += segment;
			if (branchLength == 0 || total <= branchLength) {
				if (segment + (moveAll ? 1 : 0) > this.nodeDistance[i] + (i == 0 && firstSizeChangeDistance == this.nodeDistance[0] ? 1 : 0)) {
					moveAll = true;
					shiftedNodes.add(pos);
				}
				++i;
			}
		}
		return shiftedNodes;
	}

	/**Shifts nodes in the direction of growth.*/
	protected boolean shiftNode(World world, BlockPos pos) {
		IBlockState oldNodeState = world.getBlockState(pos);
		EnumFacing oldNodeDir = getDir(oldNodeState);
		ArrayList<BlockPos> newNodes = new ArrayList<BlockPos>();
		boolean move = true;
		BlockPos checkPos;
		IBlockState checkState;
		for (EnumFacing checkDir : DIR.getAllowedValues()) {
			if (move && checkDir != oldNodeDir.getOpposite()) {
				checkPos = pos.offset(checkDir);
				checkState = world.getBlockState(checkPos);
				if (checkState.getBlock() == this && getDir(checkState) == checkDir) {
					int size = checkState.getActualState(world, checkPos).getValue(SIZE);
					if (!checkState.getValue(NODE)) {
						if (size < this.maxBranchWidth || checkDir == oldNodeDir) newNodes.add(checkPos);
					} else if (checkDir == oldNodeDir || size < this.maxBranchWidth - 1) {
						move = false;
						newNodes.clear();
					} else newNodes.add(checkPos);
				} else if (checkDir == oldNodeDir && oldNodeState.getActualState(world, pos).getValue(SIZE) >= this.maxCapWidth) {
					move = false;
					newNodes.clear();
				}
			}
		}
		if (move) {
			world.setBlockState(pos, oldNodeState.withProperty(NODE, false));
			if (!newNodes.isEmpty()) {
				for (BlockPos newNodePos : newNodes) {
					world.setBlockState(newNodePos, world.getBlockState(newNodePos).withProperty(NODE, true));
				}
			}
		}
		return move;
	}

	/**Calculates the distance to the new node from the root. Return 0 if the new node is not needed.*/
	protected abstract int newNodeDistance(World world, BlockPos rootPos, IBlockState rootState, int totalLength, int branchLength,
		int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength,
		int maxTreeHeight, ArrayList<Integer> segments, Random rand);

	protected void createNewNode(World world, BlockPos rootPos, IBlockState rootState, int newNodeDistance) {
		int rootSize = rootState.getValue(SIZE);
		if (rootSize < 4) {
			IBlockState state;
			int i = 0;
			boolean checkNode = false;
			BlockPos checkPos;
			IBlockState checkState;
			for (BlockPos pos = rootPos; i <= newNodeDistance; pos = pos.up(), ++i) {
				state = world.getBlockState(pos);
				if (state.getBlock() == this && getDir(state) == EnumFacing.UP) {
					if (i < newNodeDistance) {
						for (EnumFacing face : DIR.getAllowedValues()) {
							if (!checkNode && face != EnumFacing.DOWN) {
								checkPos = pos.offset(face);
								checkState = world.getBlockState(checkPos);
								if (checkState.getBlock() == this && getDir(checkState) == face && checkState.getValue(NODE)) {
									if (checkState.getActualState(world, checkPos).getValue(SIZE) < this.maxBranchWidth - 1)
										checkNode = true;
								}
							}
						}
					}
					if (checkNode || i == newNodeDistance) {
						world.setBlockState(pos, state.withProperty(NODE, true));
						world.setBlockState(rootPos, rootState.withProperty(SIZE, rootSize + 1));
						break;
					} else {
						for (EnumFacing face : DIR.getAllowedValues()) {
							if (face != EnumFacing.UP && face != EnumFacing.DOWN) {
								checkPos = pos.offset(face);
								checkState = world.getBlockState(checkPos);
								if (checkState.getBlock() == this && getDir(checkState) == face &&
										checkState.getActualState(world, checkPos).getValue(SIZE) < this.maxBranchWidth)
									world.setBlockState(checkPos, checkState.withProperty(NODE, true));
							}
						}
					}
				} else if (rootSize < this.maxCapWidth) world.setBlockState(rootPos, rootState.withProperty(SIZE, rootSize + 1));
			}
		}
	}

	protected int getDestroyChance(int size) {
		return 8; //(size + 1) * 8;
	}

	protected void makeSoil(World world, BlockPos soilPos, IBlockState soilState, Random rand) {
		if (soilState.getBlock() instanceof MistSoil && (isDesertTree() || soilState.getValue(IWettable.WET)) && SoilHelper.getHumus(soilState) < 2) {
			ArrayList<BlockPos> checkPoses = new ArrayList<BlockPos>();
			ArrayList<BlockPos> wetSoil = new ArrayList<BlockPos>();
			ArrayList<BlockPos> idealSoil = new ArrayList<BlockPos>();
			BlockPos donorPos = null;
			BlockPos checkPos;
			IBlockState checkState;
			wetSoil.add(soilPos);
			for (int i = 0; i < 3; ++i) {
				checkPoses.clear();
				checkPoses.addAll(wetSoil);
				wetSoil.clear();
				for (BlockPos pos : checkPoses) {
					for (EnumFacing face : EnumFacing.VALUES) {
						checkPos = pos.offset(face);
						if ((i == 0 && face != EnumFacing.UP) || (i > 0 && checkPos != soilPos && !checkPoses.contains(checkPos))) {
							checkState = world.getBlockState(checkPos);
							if (checkState.getBlock() instanceof MistSoil && (isDesertTree() || checkState.getValue(IWettable.WET))) {
								wetSoil.add(checkPos);
								if (SoilHelper.getHumus(checkState) > 0) {
									checkState = world.getBlockState(checkPos.up());
									if (checkState == this ? !(getDir(checkState) == EnumFacing.UP) : true)
										idealSoil.add(checkPos);
								}
							}
						}
					}
				}
				if (!idealSoil.isEmpty()) {
					donorPos = idealSoil.get(rand.nextInt(idealSoil.size()));
					break;
				}
			}
			if (donorPos != null) {
				checkState = world.getBlockState(donorPos);
				SoilHelper.setSoil(world, donorPos, checkState, SoilHelper.getHumus(checkState) - 1, 2);
				SoilHelper.setSoil(world, soilPos, soilState, SoilHelper.getHumus(soilState) + 1, 2);
			}
		}
	}
	
	// Generation

	public abstract void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow);

	public void generateTree(World world, BlockPos pos, Random rand) {
		generateTree(world, pos, rand, false);
	}

	public abstract void generateTrunk(World world, BlockPos pos, Random rand, boolean checkSnow);

	public void generateTrunk(World world, BlockPos pos, Random rand) {
		generateTrunk(world, pos, rand, false);
	}

	protected boolean createBud(World world, BlockPos pos, EnumFacing branchDir, boolean potential) {
		boolean check = false;
		if (checkEnvironment(world, pos.offset(branchDir.getOpposite()), false)) {
			BlockPos checkPos;
			for (EnumFacing dir : DIR.getAllowedValues()) {
				if (dir != branchDir) {
					checkPos = pos.offset(dir);
					if (checkEnvironment(world, checkPos, false)) {
						check = true;
						if (potential) world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, dir), 2);
						else world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, dir).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
					}
				}
			}
		}
		return check;
	}

	protected boolean createBranch(World world, BlockPos pos, Random rand, boolean potential) {
		return createBranch(world, null, pos, null, rand, EnumFacing.DOWN, potential);
	}

	protected boolean createBranch(World world, IBlockState state, BlockPos pos, Random rand, boolean potential) {
		return createBranch(world, state, pos, null, rand, EnumFacing.DOWN, potential);
	}

	protected boolean createBranch(World world, BlockPos pos, BlockPos rootPos, Random rand, boolean potential) {
		return createBranch(world, null, pos, rootPos, rand, EnumFacing.DOWN, potential);
	}

	protected boolean createBranch(World world, @Nullable IBlockState state, BlockPos pos, @Nullable BlockPos rootPos, Random rand, EnumFacing maskDir, boolean potential) {
		boolean check = false;
		if (state == null) state = world.getBlockState(pos);
		if (state.getBlock() == this) {
			EnumFacing branchDir = getDir(state);
			EnumFacing opposite = branchDir.getOpposite();
			BlockPos checkPos;
			for (EnumFacing dir : DIR.getAllowedValues()) {
				if (dir != opposite && dir != maskDir && (rand.nextBoolean() || dir == EnumFacing.UP || dir == branchDir)) {
					checkPos = pos.offset(dir);
					if ((rootPos == null || checkDistanse(checkPos, pos, rootPos)) &&
							checkEnvironment(world, checkPos, false) && createBud(world, checkPos, dir.getOpposite(), potential)) {
						check = true;
						world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, true), 2);
					}
				}
			}
			if (!check) {
				state = world.getBlockState(pos.offset(branchDir));
				if (state.getBlock() == this && getDir(state) == branchDir) {
					check = true;
				} else if (createBud(world, pos, opposite, potential)) {
					world.setBlockState(pos, this.getDefaultState().withProperty(DIR, branchDir).withProperty(NODE, true), 2);
					check = true;
				} else world.setBlockToAir(pos);
			}
		}
		return check;
	}
	
	protected boolean checkDistanse(BlockPos pos, BlockPos oldPos, BlockPos rootPos) {
		return rootPos.distanceSq(oldPos) < rootPos.distanceSq(pos);		
	}

	protected void drainZone(World world, BlockPos pos, int dist, Random rand) {
		BlockPos checkPos;
		IBlockState checkState;
		for (int x = -dist; x <= dist; ++x) {
			for (int y = -dist; y <= dist; ++y) {
				for (int z = -dist; z <= dist; ++z) {
					int summ = Math.abs(x) + Math.abs(y) + Math.abs(z);
					if (summ > 0 && summ <= dist) {
						checkPos = pos.add(x, y, z);
						checkState = world.getBlockState(checkPos);
						if (checkState.getBlock() instanceof MistSoil) {
							SoilHelper.setSoil(world, checkPos, checkState, Math.min(1, rand.nextInt(summ)), Mist.FLAG);
						}
					}
				}
			}
		}
	}

	protected void checkSnow(World world, BlockPos pos, int radius) {
		if (world.getBiome(pos).isSnowyBiome()) {
			BlockPos checkPos;
			IBlockState checkState;
			for (int x = -radius; x <= radius; ++x) {
				for (int z = -radius; z <= radius; ++z) {
					checkPos = world.getPrecipitationHeight(pos.add(x, 0, z));
					if (world.canSnowAt(checkPos, true))
						world.setBlockState(checkPos, Blocks.SNOW_LAYER.getDefaultState(), 2);
					checkState = world.getBlockState(checkPos.down());
					int h = checkPos.getY() - 2;
					if (checkState.getBlock() instanceof MistTreeLeaves || checkState.getBlock() == this) {
						for (int y = h; y > MistWorld.fogMaxHight_S + 4; --y) {
							checkPos = pos.add(x, y - h, z);
							checkState = world.getBlockState(checkPos);
							if (checkState.getBlock() == Blocks.SNOW_LAYER) {
								world.setBlockToAir(checkPos);
								break;
							} else if (checkState.isFullCube()) {
								break;
							}
						}
					}
				}
			}
		}
	}

	protected void generateSoilLump(World world, BlockPos pos, EnumFacing face, Random rand, boolean checkSnow) {
		BlockPos inPos = pos.offset(face, 1);
		pos = pos.offset(face, 2).down();
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof IMistSoil && !(world.getBlockState(pos.down()).getBlock() instanceof IMistSoil) &&
				world.getBlockState(inPos).getBlock().isReplaceable(world, inPos)) {
			if (world.getBlockState(pos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(pos.up());
			if (checkSnow && world.canSnowAt(pos, false)) world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
			else world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
			world.setBlockState(inPos, state, 2);
			SoilHelper.setDirt(world, inPos.down(), state, 2);
			if (checkSnow && world.canSnowAt(inPos.up(), false)) world.setBlockState(inPos.up(), Blocks.SNOW_LAYER.getDefaultState());
			for (EnumFacing side : EnumFacing.HORIZONTALS) {
				if (side != face.getOpposite() && rand.nextInt(8) != 0) {
					int i = side == face ? 2 : 1;
					BlockPos outPos = pos.offset(side);
					inPos = outPos.up(i).offset(face.getOpposite(), i);
					state = world.getBlockState(outPos);
					if (state.getBlock() instanceof IMistSoil && !(world.getBlockState(outPos.down()).getBlock() instanceof IMistSoil) &&
							world.getBlockState(inPos).getBlock().isReplaceable(world, inPos)) {
						if (world.getBlockState(outPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(outPos.up());
						if (checkSnow && world.canSnowAt(outPos, false)) world.setBlockState(outPos, Blocks.SNOW_LAYER.getDefaultState());
						else world.setBlockState(outPos, Blocks.AIR.getDefaultState(), 2);
						world.setBlockState(inPos, state, 2);
						SoilHelper.setDirt(world, inPos.down(), state, 2);
						if (checkSnow && world.canSnowAt(inPos.up(), false)) world.setBlockState(inPos.up(), Blocks.SNOW_LAYER.getDefaultState());
					}
				}
			}
		}
	}
}