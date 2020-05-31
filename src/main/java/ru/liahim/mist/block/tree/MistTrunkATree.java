package ru.liahim.mist.block.tree;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistBlockBranch;
import ru.liahim.mist.block.MistGrass;
import ru.liahim.mist.block.MistSand;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeLeaves;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.MistWoodBlock;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;

public class MistTrunkATree extends MistTreeTrunk {

	public MistTrunkATree() {
		super(3.0F, 0, 0, true, true, true, (MistTreeLeaves)MistBlocks.A_TREE_LEAVES, 1, 1, 3, new int[] {2, 2, 2, 3});
	}

	@Override
	protected int getSoilDepth(World world, BlockPos rootPos) {
		int s = -1;
		for (BlockPos pos = rootPos.down(); s < 4 && world.getBlockState(pos).getBlock() instanceof MistSoil; pos = pos.down()) {
			++s;
		}
		return s;
	}

	@Override
	protected int getMinTrunckLength(World world, BlockPos rootPos, long posRand, int soilDepth) {		
		return 1 + (int)(posRand % 5) % 2;
	}

	@Override
	protected int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth) {		
		return minTrunckLength + 8 + soilDepth * 2 + (int)(posRand % 6) % 3;
	}

	@Override
	protected int getGrowingThickness(EnumFacing face, Random rand) {
		return 2;
	}

	@Override
	protected boolean canCheckBranch(World world, BlockPos pos, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, int size, Random rand) {
		return (isBud || !availableGrowthDirection.isEmpty()) && size < 3;
	}

	@Override
	public boolean isDesertTree() {
		return true;
	}

	@Override
	protected int canGrowth(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, int totalLength,
		int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength, int maxTrunckLength, int maxTreeHeight,
		ArrayList<Integer> segments, ArrayList<BlockPos> nodes, @Nullable BlockPos fixPos, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		if (MistWorld.isPosInFog(world, rootPos)) return -1;
		if (!(soil.getBlock() instanceof MistSoil)) return -1;
		int humus = SoilHelper.getHumus(soil);
		if (humus == 3) return -1;
		/**Soil factor*/
		if (rand.nextInt(500) == 0 && ((MistSoil)soil.getBlock()).getWaterPerm(soil) < 2) {
			SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			return -1;
		}
		float temp = world.getBiome(rootPos).getTemperature(rootPos);
		float humi = MistWorld.getHumi(world, rootPos, 0);
		/**For desert trees*/
		if (!soil.getValue(IWettable.WET) && humi >= 100) world.setBlockState(soilPos, soil.withProperty(IWettable.WET, true));
		if ((soil.getValue(IWettable.WET) || (rand.nextInt(40) < humi - 10)) && humus > 0) {
			/**Growth speed*/
			if (branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.15)) != 0) return isBud ? 1 : 0;
			if (temp < 0.0 || temp > 2.5 || humi < 5) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) return isBud ? 1 : 0;
			else if (firstSizeChangeDistance < 5) {
				/**Tree height*/
				if (totalLength < maxTreeHeight || firstBranchDistance == 1) return 2;				
				else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
			} else return isBud ? 1 : 0;
		} else if (temp > 0.5 && temp < 2.0 && humi > 5) {
			makeSoil(world, soilPos, soil, rand);
			return 0;
		}
		else return -1;
	}

	@Override
	protected ArrayList<EnumFacing> chooseGrowthDir(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud,
			int totalLength, int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength, int maxTreeHeight,
			ArrayList<Integer> segments, ArrayList<BlockPos> nodes, @Nullable BlockPos fixPos, BlockPos rootPos, IBlockState rootState, Random rand) {
		ArrayList<EnumFacing> growthDir = new ArrayList<EnumFacing>();
		for (EnumFacing face : availableGrowthDirection) {
			if (face == EnumFacing.UP) {
				if (branchLength == 0) growthDir.add(face);
			} else if (trunckLength > minTrunckLength && dir == EnumFacing.UP && firstBranchDistance == 0 && !isBud) growthDir.add(face);
		}
		return growthDir;
	}

	@Override
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
		}
		if (growth) {
			IBlockState targetState;
			for (EnumFacing face : DIR.getAllowedValues()) {
				if (face != dir && face != dir.getOpposite() && face != EnumFacing.UP) {
					targetPos = newPos.offset(face);
					targetState = world.getBlockState(targetPos);
					if (checkEnvironment(world, targetPos) || (check && targetState.getBlock() == this.leaves && targetState.getValue(LDIR) == face)) {
						if (availableGrowthDirection.contains(face) && world.getBlockState(oldPos.offset(face)).getBlock() == this.leaves) {
							if (isLeavesRemoved(world, totalLength, branchLength, minTrunckLength, trunckLength, firstBranchDistance, oldPos.offset(face), targetPos, face, rand))
								world.setBlockToAir(oldPos.offset(face));
							world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						} else if (totalLength == 1 || rand.nextInt(dir == EnumFacing.UP ? 2 : 3) > 0) {
							world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						}
					}
				}
			}
			if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
		}
		return growth;
	}

	@Override
	protected boolean isLeavesRemoved(World world, int totalLength, int branchLength, int minTrunckLength, int trunckLength, int firstBranchDistance, BlockPos pos, BlockPos newPos, EnumFacing dir, Random rand) {
		return firstBranchDistance == 1 || trunckLength <= minTrunckLength;
	}

	@Override
	protected int newNodeDistance(World world, BlockPos rootPos, IBlockState rootState, int totalLength, int branchLength,
		int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength,
		int maxTreeHeight, ArrayList<Integer> segments, Random rand) {
		int rootSize = rootState.getValue(SIZE);
		if (branchLength == 0 && rootSize < 4) {
			int lastSegment = segments.isEmpty() ? totalLength : segments.get(segments.size() - 1);
			if (lastSegment > Math.min(6, 4 + rootSize)) return Math.min(trunckLength, 3 + rootSize);
		}
		return 0;
	}

	@Override
	protected void setNewLeaves(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection,
		boolean isBud, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		if (isBud && this.newGrowthChance > 0 && rand.nextInt(this.newGrowthChance) == 0 &&
				availableGrowthDirection.size() < (dir == EnumFacing.UP ? 5 : 4)) {
			EnumFacing face;
			if (!availableGrowthDirection.contains(dir)) face = dir;
			else face = (EnumFacing)DIR.getAllowedValues().toArray()[rand.nextInt(DIR.getAllowedValues().size())];
			if (face != dir.getOpposite() && !availableGrowthDirection.contains(face) && (dir == EnumFacing.UP || face != EnumFacing.UP)) {
				BlockPos checkPos = pos.offset(face);
				if (checkEnvironment(world, checkPos)) {
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

	@Override
	public void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof MistGrass && ((MistGrass)downState.getBlock()).getWaterPerm(downState) > 1 &&
						checkEnvironment(world, pos, false) && world.canBlockSeeSky(pos)) {
				long posRand = MistWorld.getPosRandom(world, pos, 0);
				int soilDepth = getSoilDepth(world, pos);
				int minTrunckLength = getMinTrunckLength(world, pos, posRand, soilDepth);
				int maxTreeHight = getMaxTreeHeight(world, pos, minTrunckLength, posRand, soilDepth);
				if ((maxTreeHight - minTrunckLength) % 2 == 0) ++maxTreeHight;
				BlockPos trunkPos = pos;
				int counter = 0;
				for (int i = 0; i < maxTreeHight; ++i) {
					if (checkEnvironment(world, trunkPos.up(), false)) {
						++counter;
						world.setBlockState(trunkPos, this.getDefaultState(), 2);
						trunkPos = trunkPos.up();
					} else break;
				}
				maxTreeHight = counter;
				int size = 0;
				int nodeNumber = -1;
				for (int i = 0; i < 4; ++i) {
					counter -= this.nodeDistance[i];
					if (counter >= i + 3) {
						++size;
						++nodeNumber;
					}
					else {
						counter += this.nodeDistance[i];
						break;
					}
				}
				boolean potential = size == 4;
				if (size == 4) SoilHelper.setSoil(world, pos.down(), ((MistGrass)downState.getBlock()).getSoilBlock().getDefaultState(), 2, false, Mist.FLAG);
				boolean node;
				trunkPos = pos;
				BlockPos checkPos;
				for (int i = 0; i < maxTreeHight; ++i) {
					node = false;
					if (counter == 0 && nodeNumber >= 0) {
						node = true;
						counter = this.nodeDistance[nodeNumber];
						--nodeNumber;
						--size;
					}
					if (i >= minTrunckLength && i < maxTreeHight - 1 && (i - minTrunckLength) % 2 == 0) {
						for (EnumFacing dir : EnumFacing.HORIZONTALS) {
							checkPos = trunkPos.offset(dir);
							if (checkEnvironment(world, checkPos, false) && checkEnvironment(world, checkPos.offset(dir), false)) {
								world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir), 2);
								createBud(world, checkPos, dir.getOpposite(), EnumFacing.UP, potential);
							}
						}
					}
					/**Age*/
					if (i == 0 && size == 4) {
						int j = rand.nextInt(11);
						if (j == 0) world.setBlockState(trunkPos, this.getDefaultState().withProperty(SIZE, 4).withProperty(DIR, EnumFacing.WEST).withProperty(NODE, node), 2);
						else if (j < 6) world.setBlockState(trunkPos, this.getDefaultState().withProperty(SIZE, 4).withProperty(DIR, EnumFacing.EAST).withProperty(NODE, node), 2);
						else world.setBlockState(trunkPos, this.getDefaultState().withProperty(SIZE, 4).withProperty(NODE, node), 2);
					} else world.setBlockState(trunkPos, this.getDefaultState().withProperty(SIZE, size).withProperty(NODE, node), 2);
					trunkPos = trunkPos.up();
					--counter;
				}
				trunkPos = trunkPos.down();
				createBud(world, trunkPos, EnumFacing.DOWN, potential);
				drainZone(world, pos.down(), 2, rand);
				if (checkSnow) checkSnow(world, pos, 3);
			}
		}
	}

	private boolean createBud(World world, BlockPos pos, EnumFacing branchDir, EnumFacing mask, boolean potential) {
		boolean check = false;
		if (checkEnvironment(world, pos.offset(branchDir.getOpposite()), false)) {
			BlockPos checkPos;
			for (EnumFacing dir : DIR.getAllowedValues()) {
				if (dir != branchDir && dir != mask) {
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

	@Override
	public void generateTrunk(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			if (world.getBlockState(pos.down()).getBlock() instanceof MistSoil) {
				EnumFacing face = EnumFacing.getHorizontal(rand.nextInt(4));
				int j = rand.nextInt(3) + 4;
				BlockPos checkPos;
				boolean check = true;
				for (int i = 0; i <= j; ++i) {
					checkPos = pos.offset(face, i);
					if (!world.getBlockState(checkPos).getMaterial().isReplaceable() || !world.isSideSolid(checkPos.down(), EnumFacing.UP)) {
						check = false;
						break;
					}
				}
				if (check) {
					BlockPos branchPos;
					this.generateSoilLump(world, pos, face.getOpposite(), rand, checkSnow);
					for (int i = 0; i < j; ++i) {
						checkPos = pos.offset(face, i);
						if (world.getBlockState(checkPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(checkPos.up());
						world.setBlockState(checkPos, MistBlocks.A_TREE_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
						if ((i & 1) == 1) {
							for (EnumFacing branchFace : FacingHelper.NOTDOWN) {
								if (branchFace.getAxis() != face.getAxis() && rand.nextBoolean()) {
									branchPos = checkPos.offset(branchFace);
									if (world.getBlockState(branchPos).getMaterial().isReplaceable()) {
										if (world.getBlockState(branchPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(branchPos.up());
										world.setBlockState(branchPos, MistBlocks.A_TREE_BRANCH.getDefaultState().withProperty(MistBlockBranch.SIZE, 0).withProperty(MistBlockBranch.AXIS, branchFace.getAxis()), 2);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public void generateDunesTrunk(World world, BlockPos pos, Random rand) {
		if (!world.isRemote) {
			boolean check = false;
			BlockPos checkPos = pos.down();
			IBlockState checkState = world.getBlockState(checkPos);
			if (checkState.getBlock() instanceof MistSoil) check = true;
			else if (checkState.getBlock() instanceof MistSand) {
				for (;;) {
					checkPos = checkPos.down();
					if (checkPos.getY() < MistWorld.seaLevelUp - 5) break;
					checkState = world.getBlockState(checkPos.down());
					if (checkState.getBlock() instanceof MistSoil) {
						check = true;
						pos = checkPos;
						break;
					} else if (!(checkState.getBlock() instanceof MistSand)) break;
				}
			}
			if (check) {
				EnumFacing face = EnumFacing.getHorizontal(rand.nextInt(4));
				int j = rand.nextInt(4) + 3;
				for (int i = 0; i <= j; ++i) {
					checkPos = pos.offset(face, i);
					checkState = world.getBlockState(checkPos);
					if ((!checkState.getMaterial().isReplaceable() && !(checkState.getBlock() instanceof MistSand)) ||
							!world.isSideSolid(checkPos.down(), EnumFacing.UP)) {
						check = false;
						break;
					}
				}
				if (check) {
					for (int i = 0; i < j; ++i) {
						checkPos = pos.offset(face, i);
						if (world.getBlockState(checkPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(checkPos.up());
						world.setBlockState(checkPos, MistBlocks.A_TREE_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())).withProperty(MistWoodBlock.TYPE, MistWoodBlock.EnumType.DEBARKING), 2);
					}
				}
			}
		}
	}

	@Override
	protected ItemStack getBranch() {
		return new ItemStack(MistBlocks.A_TREE_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.A_TREE_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.A_TREE_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.A_TREE_BLOCK, 1, 7);
	}
}