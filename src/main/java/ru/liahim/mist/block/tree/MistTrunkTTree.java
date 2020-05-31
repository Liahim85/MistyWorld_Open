package ru.liahim.mist.block.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MistTrunkTTree extends MistTreeTrunk {

	public MistTrunkTTree() {
		super(6.0F, 3, 4, true, true, false, (MistTreeLeaves)MistBlocks.T_TREE_LEAVES, 1, 0, 5, new int[] {1, 1, 2, 2});
	}

	@Override
	protected int getSoilDepth(World world, BlockPos rootPos) {
		int s = -1;
		for (BlockPos pos = rootPos.down(); s < 3 && world.getBlockState(pos).getBlock() instanceof MistSoil; pos = pos.down()) {
			++s;
		}
		return s;
	}

	@Override
	protected int getMinTrunckLength(World world, BlockPos rootPos, long posRand, int soilDepth) {		
		return 2 + (int)(posRand % 6) % 3;
	}

	@Override
	protected int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth) {		
		return minTrunckLength + 6 + soilDepth * 2 + (int)(posRand % 8) % 5;
	}

	@Override
	protected int getGrowingThickness(EnumFacing face, Random rand) {
		return rand.nextInt(2);
	}

	@Override
	protected int canGrowth(World world, BlockPos pos, IBlockState state, int size, EnumFacing dir, ArrayList<EnumFacing> availableGrowthDirection, boolean isBud, int totalLength,
		int branchLength, int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength, int maxTrunckLength, int maxTreeHeight,
		ArrayList<Integer> segments, ArrayList<BlockPos> nodes, @Nullable BlockPos fixPos, BlockPos rootPos, IBlockState rootState, BlockPos soilPos, IBlockState soil, Random rand) {
		if (MistWorld.isPosInFog(world, rootPos)) return -1;
		if (!(soil.getBlock() instanceof MistSoil)) return -1;
		int humus = SoilHelper.getHumus(soil);
		if (humus == 3) return -1;
		float temp = world.getBiome(rootPos).getTemperature(rootPos);
		float humi = MistWorld.getHumi(world, rootPos, 0);
		if (soil.getValue(IWettable.WET) && humus > 0) {
			/**Growth speed*/
			if (branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.15)) != 0) return isBud ? 1 : 0;
			if (temp < 0.0 || temp > 2.5 || humi < 10) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) {
				int trunckSize = world.getBlockState(fixPos).getActualState(world, fixPos).getValue(SIZE);
				if (pos.getY() == fixPos.getY()) {
					if (branchLength <= Math.min(3, trunckSize) && fixPos.distanceSq(pos) < 16) return 2;
					else return isBud ? 1 : 0;
				} else {
					BlockPos fixPos2 = pos.offset(dir.getOpposite(), firstBendDistance);
					trunckSize = world.getBlockState(fixPos2).getActualState(world, fixPos2).getValue(SIZE);
					if (branchLength <= Math.min(3, trunckSize)) return 2;
					else return isBud ? 1 : 0;
				}
			} else if (firstSizeChangeDistance < 4) {
				/**Tree height*/
				if (totalLength < maxTreeHeight || dir != EnumFacing.UP) return 2;
				else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
			} else return isBud ? 1 : 0;
		} else if (temp > 0.5 && temp < 2.0 && humi > 20) {
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
		EnumFacing direct = null;
		int i = rand.nextInt(3) + 2;
		if (branchLength == 0 && firstBendDistance >= i && !availableGrowthDirection.isEmpty() &&
				!(availableGrowthDirection.size() == 1 && availableGrowthDirection.contains(EnumFacing.UP))) {
			for (;;) {
				direct = availableGrowthDirection.get(rand.nextInt(availableGrowthDirection.size()));
				if (direct != EnumFacing.UP) break;
			}
		}
		for (EnumFacing face : availableGrowthDirection) {
			if (face == EnumFacing.UP) {
				if (branchLength == 0 && (totalLength <= minTrunckLength || (firstBendDistance > 0 && firstBendDistance < i)
						|| dir != EnumFacing.UP || rand.nextInt(4) == 0))
					growthDir.add(face);
				if (branchLength > 0 && rand.nextInt(4) > 0)
					growthDir.add(face);
			} else if (trunckLength == totalLength - branchLength || branchLength > 0 || dir == EnumFacing.UP || !availableGrowthDirection.contains(EnumFacing.UP)) {
				if (branchLength == 0) {
					if ((totalLength > minTrunckLength || !availableGrowthDirection.contains(EnumFacing.UP)) && (rand.nextInt(firstBendDistance >= i ? 6 : 3) == 0 || face == direct)) {
						IBlockState downBranch = world.getBlockState((pos.offset(face)).down());
						if (downBranch.getBlock() == this ? getDir(downBranch) != face : true)
						growthDir.add(face);
					}
				} else if (face == dir || rand.nextInt(3) > 0) growthDir.add(face);
			}
		}
		return growthDir;
	}

	@Override
	protected boolean isLeavesRemoved(World world, int totalLength, int branchLength, int minTrunckLength, int trunckLength, int firstBranchDistance, BlockPos pos, BlockPos newPos, EnumFacing dir, Random rand) {
		if (dir != EnumFacing.UP) {
			IBlockState checkState;
			boolean cross = false;
			for (EnumFacing face : EnumFacing.HORIZONTALS) {
				checkState = world.getBlockState(pos.offset(face));
				if (checkState.getBlock() == this.leaves && checkState.getValue(LDIR) == EnumFacing.DOWN) {
					if (newPos.offset(face) == pos) cross = true;
					else if (checkEnvironment(world, newPos.offset(face), false))
						world.setBlockState(newPos.offset(face), checkState);
					world.setBlockToAir(pos.offset(face));
				} else if (rand.nextInt(3) == 0) {
					if (checkEnvironment(world, newPos.offset(face), false))
						world.setBlockState(newPos.offset(face), this.leaves.getDefaultState().withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
				}
			}
			if (cross) world.setBlockState(pos, this.leaves.getDefaultState().withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
			else world.setBlockToAir(pos);
		} else return true;
		return false;
	}

	@Override
	protected int newNodeDistance(World world, BlockPos rootPos, IBlockState rootState, int totalLength, int branchLength,
		int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength,
		int maxTreeHeight, ArrayList<Integer> segments, Random rand) {
		int rootSize = rootState.getValue(SIZE);
		if (branchLength == 0 && rootSize < 4) {
			int lastSegment = segments.isEmpty() ? totalLength : segments.get(segments.size() - 1);
			if (lastSegment > rootSize + 5) return Math.min(trunckLength, lastSegment - this.nodeDistance[rootSize]);
		}
		return 0;
	}

	@Override
	protected boolean checkEnvironment(World world, BlockPos pos, boolean checkLight) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == this.leaves && state.getValue(LDIR) == EnumFacing.DOWN) return true;
		return canPlaceBlockAt(world, pos) && (!checkLight || checkLight(world, pos) > 0.45) && !(state.getBlock() instanceof BlockLiquid);
	}

	@Override
	public void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			IBlockState checkState = world.getBlockState(pos.down());
			if (checkState.getBlock() instanceof MistSoil && checkEnvironment(world, pos, false) && world.canBlockSeeSky(pos)) {
				long posRand = MistWorld.getPosRandom(world, pos, 0);
				int soilDepth = getSoilDepth(world, pos);
				int minTrunckLength = getMinTrunckLength(world, pos, posRand, soilDepth);
				int maxTreeHight = getMaxTreeHeight(world, pos, minTrunckLength, posRand, soilDepth);
				BlockPos checkPos = pos;
				int counter = 0;
				for (int i = 0; i <= minTrunckLength; ++i) {
					if (checkEnvironment(world, checkPos.up(), false)) {
						++counter;
						/**Age*/
						if (i == 0) {
							int j = rand.nextInt(11);
							if (j == 0) world.setBlockState(checkPos, this.getDefaultState().withProperty(SIZE, 4).withProperty(DIR, EnumFacing.WEST), 2);
							else if (j < 6) world.setBlockState(checkPos, this.getDefaultState().withProperty(SIZE, 4).withProperty(DIR, EnumFacing.EAST), 2);
							else world.setBlockState(checkPos, this.getDefaultState().withProperty(SIZE, 4), 2);
						} else world.setBlockState(checkPos, this.getDefaultState().withProperty(SIZE, 4), 2);
						checkPos = checkPos.up();
					} else break;
				}
				SoilHelper.setSoil(world, pos.down(), ((MistSoil)checkState.getBlock()).getSoilBlock().getDefaultState(), 2, true, Mist.FLAG);
				int size;
				Map<BlockPos, Integer> cornerPoses = new HashMap<BlockPos, Integer>();
				ArrayList<BlockPos> branches = new ArrayList<BlockPos>();
				cornerPoses = chooseCornerPoses(world, checkPos.down(), counter, rand, cornerPoses);
				if (!cornerPoses.isEmpty()) {
					for (BlockPos cornerPos; !cornerPoses.isEmpty();) {
						cornerPos = (BlockPos)cornerPoses.keySet().toArray()[cornerPoses.size() - 1];
						counter = cornerPoses.get(cornerPos);
						int segment = Math.max(1, Math.min(rand.nextInt(3) + 2, maxTreeHight - counter));
						checkState = world.getBlockState(cornerPos);
						if (checkState.getBlock() == this) {
							size = checkState.getActualState(world, cornerPos).getValue(SIZE);
							checkPos = cornerPos.up();
							boolean check = true;
							for (int i = 1; i <= (size == 0 ? 1 : segment); ++i) {
								if (checkEnvironment(world, checkPos, false) || world.getBlockState(checkPos).getBlock() == this.leaves) {
									++counter;
									world.setBlockState(checkPos, this.getDefaultState().withProperty(SIZE, size), 2);
									/**Branches*/
									if (i != segment && counter > minTrunckLength) {
										for (EnumFacing face : EnumFacing.HORIZONTALS) {
											if (rand.nextInt(3) == 0 && checkEnvironment(world, checkPos.offset(face), false)) {
												checkState = world.getBlockState(checkPos.offset(face).down());
												if (checkState.getBlock() == this ? getDir(checkState) != face : true) {
													world.setBlockState(checkPos.offset(face), this.getDefaultState().withProperty(DIR, face), 2);
													branches.add(checkPos.offset(face));
												}
											}
										}
									}
									checkPos = checkPos.up();
								} else {
									if (i == 1) {
										check = false;
										checkPos = cornerPos.offset(getDir(checkState).getOpposite());
										world.setBlockToAir(cornerPos);
										for (EnumFacing face : EnumFacing.HORIZONTALS) {
											checkState = world.getBlockState(checkPos.offset(face));
											if (checkState.getBlock() == this && getDir(checkState) == face)
												check = true;
										}
										if (!check) {
											for (EnumFacing face : EnumFacing.HORIZONTALS) {
												if (checkPos.offset(face) != cornerPos && !cornerPoses.containsKey(checkPos.offset(face)) &&
														checkEnvironment(world, checkPos.offset(face), false) && checkEnvironment(world, checkPos.offset(face).up(), false)) {
													world.setBlockState(checkPos.offset(face), this.getDefaultState().withProperty(DIR, face), 2);
													cornerPoses.put(checkPos.offset(face), cornerPoses.get(cornerPos));
													break;
												}
											}
										} else check = false;
									}
									break;
								}
							}
							cornerPoses.remove(cornerPos);
							if (check) {
								checkPos = checkPos.down();
								checkState = world.getBlockState(checkPos);
								if (checkState.getBlock() == this) {
									if (counter < maxTreeHight) cornerPoses = chooseCornerPoses(world, checkPos, counter, rand, cornerPoses);
									else {
										if (world.getBlockState(checkPos.up()).getBlock() == this.leaves) world.setBlockToAir(checkPos.up());
										createBud(world, checkPos, EnumFacing.DOWN, true);
										if (size > 0) {
											world.setBlockState(checkPos, checkState.withProperty(NODE, true), 2);
											if (size > 1) {
												for (int i = 1; i < size; ++i) {
													checkPos = checkPos.offset(checkState.getValue(DIR).getOpposite());
													checkState = world.getBlockState(checkPos);
													if (checkState.getBlock() == this)
														world.setBlockState(checkPos, checkState.withProperty(NODE, true), 2);
												}
											}
										}
									}
								}
							}
						} else cornerPoses.remove(cornerPos);
					}
				}
				EnumFacing dir;
				if (!branches.isEmpty()) {
					ArrayList<BlockPos> branches2 = new ArrayList<BlockPos>();
					for (BlockPos brPos : branches) {
						checkState = world.getBlockState(brPos);
						if (checkState.getBlock() == this) {
							dir = checkState.getValue(DIR);
							checkState = world.getBlockState(brPos.offset(dir.getOpposite()));
							checkPos = brPos.offset(dir);
							if (!checkEnvironment(world, checkPos, false) || checkState.getBlock() != this) {
								world.setBlockToAir(brPos);
							} else {
								size = Math.min(3, checkState.getActualState(world, brPos).getValue(SIZE));
								if (size > 1) {
									for (int i = 0; i < size - 1; ++i) {
										if (checkEnvironment(world, checkPos.offset(dir), false)) {
											world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, (size - i) % 2 == 1), 2);
											if (i == size - 2) {
												branches2.add(checkPos);
												if (size == 3) {
													for (EnumFacing face : EnumFacing.HORIZONTALS) {
														if (face != dir && face != dir.getOpposite() && rand.nextInt(4) > 0) {
															checkPos = brPos.offset(dir).offset(face);
															if (checkEnvironment(world, checkPos, false)) {
																world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, face), 2);
																branches2.add(checkPos);
															}
														}
													}
												}
											}
											else checkPos = checkPos.offset(dir);
										} else {
											if (i == 0) {
												if (size < 3) {
													world.setBlockState(brPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, false), 2);
													createBud(world, brPos, dir.getOpposite(), true);
												} else world.setBlockToAir(brPos);
											} else {
												branches2.add(checkPos);
											}
											break;
										}
									}
								} else branches2.add(brPos);
							}
						}
					}
					if (!branches.isEmpty()) {
						for (BlockPos brPos : branches2) {
							createBranch(world, brPos, rand, true);
						}
					}
				}
				for (int x = -6; x < 7; ++x) {
					for (int y = minTrunckLength; y <= maxTreeHight; ++y) {
						for (int z = -6; z < 7; ++z) {
							checkPos = pos.add(x, y, z);
							checkState = world.getBlockState(checkPos);
							if (checkState.getBlock() == this.leaves) {
								dir = checkState.getValue(LDIR);
								if (dir != EnumFacing.UP && dir != EnumFacing.DOWN) {
									for (EnumFacing face : EnumFacing.HORIZONTALS) {
										if (this.checkEnvironment(world, checkPos.offset(face), false)) {
											world.setBlockState(checkPos.offset(face), this.leaves.getDefaultState(), 2);
										}
									}
								}
							}
						}
					}
				}
				drainZone(world, pos.down(), 3, rand);
				if (checkSnow) checkSnow(world, pos, 4);
			}
		}
	}

	private Map<BlockPos, Integer> chooseCornerPoses(World world, BlockPos pos, int counter, Random rand, Map<BlockPos, Integer> cornerPoses) {
		EnumFacing trunkDir;
		BlockPos trunckPos;
		counter = counter + 1;
		for (int i = 0; i < 5; ++i) {
			trunkDir = EnumFacing.HORIZONTALS[rand.nextInt(4)];
			trunckPos = pos.offset(trunkDir);
			if (!cornerPoses.containsKey(trunckPos) && checkEnvironment(world, trunckPos, false)) {
				world.setBlockState(trunckPos, this.getDefaultState().withProperty(DIR, trunkDir), 2);
				cornerPoses.put(trunckPos, counter);
				break;
			} else if (i == 4) {
				world.setBlockState(pos.up(), this.getDefaultState().withProperty(NODE, true), 2);
				cornerPoses.remove(pos.up());
				cornerPoses.put(pos.up(), counter);
			}
		}
		for (EnumFacing face : EnumFacing.HORIZONTALS) {
			trunckPos = pos.offset(face);
			if (rand.nextInt(6) == 0 && !cornerPoses.containsKey(trunckPos) && checkEnvironment(world, trunckPos, false)) {
				world.setBlockState(trunckPos, this.getDefaultState().withProperty(DIR, face), 2);
				cornerPoses.put(trunckPos, counter);
				if (rand.nextBoolean()) break;
			}
		}
		return cornerPoses;
	}

	@Override
	public void generateTrunk(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			if (world.getBlockState(pos.down()).getBlock() instanceof MistSoil) {
				EnumFacing face = EnumFacing.getHorizontal(rand.nextInt(4));
				int j = rand.nextInt(3) + 3;
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
					EnumFacing branchFace;
					for (int i = 0; i < j; ++i) {
						checkPos = pos.offset(face, i);
						if (world.getBlockState(checkPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(checkPos.up());
						world.setBlockState(checkPos, MistBlocks.T_TREE_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
						if (i > 1 && i >= j - 2) {
							branchFace = FacingHelper.NOTDOWN[rand.nextInt(5)];
							if (branchFace.getAxis() != face.getAxis() && rand.nextBoolean()) {
								checkPos = checkPos.offset(branchFace);
								if (world.getBlockState(checkPos).getMaterial().isReplaceable()) {
									if (world.getBlockState(checkPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(checkPos.up());
									world.setBlockState(checkPos, MistBlocks.T_TREE_BRANCH.getDefaultState().withProperty(MistBlockBranch.SIZE, 1).withProperty(MistBlockBranch.AXIS, branchFace.getAxis()), 2);
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
		return new ItemStack(MistBlocks.T_TREE_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.T_TREE_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.T_TREE_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.T_TREE_BLOCK, 1, 7);
	}
}