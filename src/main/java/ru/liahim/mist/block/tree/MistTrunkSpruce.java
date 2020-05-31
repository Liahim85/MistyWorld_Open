package ru.liahim.mist.block.tree;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistBlockBranch;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeLeaves;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.MistWoodBlock;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MistTrunkSpruce extends MistTreeTrunk {

	public MistTrunkSpruce() {
		super(4.0F, 0, 0, true, true, true, (MistTreeLeaves)MistBlocks.SPRUSE_LEAVES, 1, 0, 5, new int[] {2, 2, 2, 3});
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 25;
    }

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 7;
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
		return minTrunckLength + 8 + soilDepth*2 + (int)(posRand % 6) % 3;
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
	protected int canGrowth(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, int totalLength,
		int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength, int maxTrunckLength, int maxTreeHeight,
		ArrayList<Integer> segments, ArrayList<BlockPos> nodes, @Nullable BlockPos fixPos, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		if (MistWorld.isPosInFog(world, rootPos)) return -1;
		if (!(soil.getBlock() instanceof MistSoil)) return -1;
		int humus = SoilHelper.getHumus(soil);
		if (humus == 3) return -1;
		/**Soil factor*/
		if (rand.nextInt(1000) == 0 && ((MistSoil)soil.getBlock()).getWaterPerm(soil) < 2) {
			SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			return -1;
		}
		float temp = world.getBiome(rootPos).getTemperature(rootPos);
		float humi = MistWorld.getHumi(world, rootPos, 0);
		if (soil.getValue(IWettable.WET) && humus > 0) {
			/**Growth speed*/
			if (isBud && branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.15)) != 0) return 1;
			if (temp < -1.0 || temp > 1.8 || humi < 5) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) {
				int trunckSize = world.getBlockState(fixPos).getActualState(world, fixPos).getValue(SIZE);
				if (branchLength < (trunckSize > 2 ? 2 : 1)) return 2;
				else return isBud ? 1 : 0;
			} else if (firstSizeChangeDistance < 5) {
				/**Tree height*/
				if (totalLength < maxTreeHeight || (totalLength - minTrunckLength) % 2 == 0) {
					if (isBud) {
						if (checkEnvironment(world, pos.up(3))) {
							IBlockState upState = world.getBlockState(pos.up(2));
							if (upState.getBlock() == this.leaves && upState.getValue(LDIR) == EnumFacing.DOWN) {
								world.setBlockState(pos.up(3), this.leaves.getDefaultState().withProperty(LDIR, EnumFacing.DOWN).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
								return 2;
							} else if (checkEnvironment(world, pos.up(2))) {
								world.setBlockState(pos.up(2), this.leaves.getDefaultState().withProperty(LDIR, EnumFacing.DOWN).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
								if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
							}
						} else if (checkEnvironment(world, pos.up(2))) {
							world.setBlockState(pos.up(2), this.leaves.getDefaultState().withProperty(LDIR, EnumFacing.DOWN).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
							if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
						}
						return totalLength == 1 ? 0 : 1;
					} else return size > 0 ? 2 : totalLength == 1 ? 0 : 1;
				} else if (isBud) {
					if (checkEnvironment(world, pos.up(2))) {
						world.setBlockState(pos.up(2), this.leaves.getDefaultState().withProperty(LDIR, EnumFacing.DOWN).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
					}
					else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
				} return 0;
			} else return isBud ? 1 : 0;
		} else if (temp > -0.5 && temp < 1.5 && humi > 20) {
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
			} else if ((trunckLength > minTrunckLength && dir == EnumFacing.UP && (trunckLength - minTrunckLength) % 2 == 1 && !isBud) ||
					branchLength == 1) growthDir.add(face);
		}
		return growthDir;
	}

	@Override
	protected boolean isLeavesRemoved(World world, int totalLength, int branchLength, int minTrunckLength, int trunckLength, int firstBranchDistance, BlockPos pos, BlockPos newPos, EnumFacing dir, Random rand) {
		return branchLength > 0 || trunckLength <= minTrunckLength;
	}

	@Override
	protected boolean growth(World world, BlockPos oldPos, BlockPos newPos, int size, EnumFacing baseDir, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection,
			int totalLength, int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength,
			int minTrunckLength, int maxTreeHeight, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		boolean growth = false;
		boolean check = false;
		BlockPos targetPos = newPos.offset(dir);
		boolean CW = (trunckLength/2) % 2 == 0;
		if (checkEnvironment(world, targetPos) || canReplace(world, dir, targetPos, world.getBlockState(targetPos), CW, true)) {
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
					if (checkEnvironment(world, targetPos) || (check && targetState.getBlock() == this.leaves && targetState.getValue(LDIR) == face) ||
							canReplace(world, face, targetPos, targetState, CW, true)) {
						if (availableGrowthDirection.contains(face)) {
							if (world.getBlockState(oldPos.offset(face)).getBlock() == this.leaves &&
									isLeavesRemoved(world, totalLength, branchLength, minTrunckLength, trunckLength, firstBranchDistance, oldPos.offset(face), targetPos, face, rand))
								world.setBlockToAir(oldPos.offset(face));
							world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						} else if (face == EnumFacing.UP || rand.nextInt(3) > 0) {
							world.setBlockState(targetPos, this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
						}
					}
				}
			}
			if (removeHumus(world, soilPos, soil)) makeSoil(world, soilPos, soil, rand);
		}
		return growth;
	}

	private boolean canReplace(World world, EnumFacing dir, BlockPos targetPos, IBlockState targetState, boolean CW, boolean checkLight) {
		if (checkLight && world.getLightBrightness(targetPos) <= 0.45) return false;
		if (targetState.getBlock() == this.leaves) {
			EnumFacing targetDir = targetState.getValue(LDIR);
			if (dir == EnumFacing.UP || targetDir == (CW ? dir.rotateY() : dir.rotateYCCW())) {
				IBlockState branchState = world.getBlockState(targetPos.offset(targetDir.getOpposite()));
				if (branchState.getBlock() == this) {
					if (getDir(branchState) != targetDir) return true;
				} else return true;
			}
		}
		return false;
	}

	@Override
	protected int newNodeDistance(World world, BlockPos rootPos, IBlockState rootState, int totalLength, int branchLength,
		int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength,
		int maxTreeHeight, ArrayList<Integer> segments, Random rand) {
		int rootSize = rootState.getValue(SIZE);
		if (branchLength == 0 && rootSize < 4) {
			int lastSegment = segments.isEmpty() ? totalLength : segments.get(segments.size() - 1);
			if (lastSegment > Math.min(6, 4 + rootSize)) return Math.min(trunckLength, lastSegment - this.nodeDistance[rootSize]);
		}
		return 0;
	}

	@Override
	public void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof MistSoil && ((MistSoil)downState.getBlock()).getWaterPerm(downState) > 1 &&
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
					if (counter >= i + 1) {
						++size;
						++nodeNumber;
					}
					else {
						counter += this.nodeDistance[i];
						break;
					}
				}
				boolean potential = size == 4;
				if (size == 4) SoilHelper.setSoil(world, pos.down(), ((MistSoil)downState.getBlock()).getSoilBlock().getDefaultState(), 2, true, Mist.FLAG);
				boolean node;
				boolean CW = false;
				boolean upper = false;
				EnumFacing cwDir;
				trunkPos = pos;
				BlockPos checkPos;
				ArrayList<BlockPos> branches = new ArrayList<BlockPos>();
				for (int i = 0; i < maxTreeHight; ++i) {
					node = false;
					if (counter == 0 && nodeNumber >= 0) {
						node = true;
						counter = this.nodeDistance[nodeNumber];
						--nodeNumber;
						--size;
					}
					if (i >= minTrunckLength && i < maxTreeHight - 1 && (i - minTrunckLength) % 2 == 0) {
						CW = !CW;
						if (size > 2) {
							for (EnumFacing dir : EnumFacing.HORIZONTALS) {
								checkPos = trunkPos.offset(dir);
								if ((upper || rand.nextInt(3) > 0) && checkEnvironment(world, checkPos, false) && checkEnvironment(world, checkPos.offset(dir), false)) {
									world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir), 2);
									if (checkEnvironment(world, checkPos.offset(dir, 2), false)) {
										world.setBlockState(checkPos.offset(dir), this.getDefaultState().withProperty(DIR, dir), 2);
										branches.add(checkPos.offset(dir));
										if (CW) cwDir = dir.rotateYCCW();
										else cwDir = dir.rotateY();
										if (checkEnvironment(world, checkPos.offset(cwDir), false)) {
											world.setBlockState(checkPos.offset(cwDir), this.getDefaultState().withProperty(DIR, cwDir), 2);
											branches.add(checkPos.offset(cwDir));
										}
									} else branches.add(checkPos);
								}
							}
							for (EnumFacing dir : EnumFacing.HORIZONTALS) {
								checkPos = trunkPos.offset(dir);
								if (world.getBlockState(checkPos).getBlock() == this) {
									if (CW) cwDir = dir.rotateY();
									else cwDir = dir.rotateYCCW();
									if (checkEnvironment(world, checkPos.offset(cwDir), false) && createBud1(world, checkPos.offset(cwDir), cwDir.getOpposite(), CW, potential))
										world.setBlockState(checkPos.offset(cwDir), this.getDefaultState().withProperty(DIR, cwDir), 2);
								}
							}
							for (BlockPos pos1 : branches) {
								downState = world.getBlockState(pos1);
								if (downState.getBlock() == this) {
									if (!createBud1(world, pos1, downState.getValue(DIR).getOpposite(), CW, potential)) {
										if (potential) world.setBlockState(pos1, this.leaves.getDefaultState().withProperty(LDIR, downState.getValue(DIR)), 2);
										else world.setBlockState(pos1, this.leaves.getDefaultState().withProperty(LDIR, downState.getValue(DIR)).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
									}
								}
							}
							branches.clear();
						} else if (size > 0) {
							for (EnumFacing dir : EnumFacing.HORIZONTALS) {
								checkPos = trunkPos.offset(dir);
								if (checkEnvironment(world, checkPos, false)) {
									if (createBud1(world, checkPos, dir.getOpposite(), CW, potential)) {
										world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir), 2);
									} else {
										if (potential) world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, dir), 2);
										else world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, dir).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
									}
								}
							}
						}
						upper = true;
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
				if (potential) world.setBlockState(trunkPos.up(2), this.leaves.getDefaultState().withProperty(LDIR, EnumFacing.DOWN), 2);
				else world.setBlockState(trunkPos.up(2), this.leaves.getDefaultState().withProperty(LDIR, EnumFacing.DOWN).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
				drainZone(world, pos.down(), 2, rand);
				if (checkSnow) checkSnow(world, pos, 3);
			}
		}
	}

	protected boolean createBud1(World world, BlockPos pos, EnumFacing branchDir, boolean CW, boolean potential) {
		boolean check = false;
		BlockPos checkPos = pos.offset(branchDir.getOpposite());
		if (checkEnvironment(world, checkPos, false) || canReplace(world, branchDir.getOpposite(), checkPos, world.getBlockState(checkPos), CW, false)) {
			for (EnumFacing dir : DIR.getAllowedValues()) {
				if (dir != branchDir) {
					checkPos = pos.offset(dir);
					if (checkEnvironment(world, checkPos, false) || canReplace(world, dir, checkPos, world.getBlockState(checkPos), CW, false)) {
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
						world.setBlockState(checkPos, MistBlocks.SPRUCE_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
						if ((i & 1) == 1) {
							for (EnumFacing branchFace : FacingHelper.NOTDOWN) {
								if (branchFace.getAxis() != face.getAxis() && rand.nextBoolean()) {
									branchPos = checkPos.offset(branchFace);
									if (world.getBlockState(branchPos).getMaterial().isReplaceable()) {
										if (world.getBlockState(branchPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(branchPos.up());
										world.setBlockState(branchPos, MistBlocks.SPRUCE_BRANCH.getDefaultState().withProperty(MistBlockBranch.SIZE, 0).withProperty(MistBlockBranch.AXIS, branchFace.getAxis()), 2);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected ItemStack getBranch() {
		return new ItemStack(MistBlocks.SPRUCE_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.SPRUCE_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.SPRUCE_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.SPRUCE_BLOCK, 1, 7);
	}
}