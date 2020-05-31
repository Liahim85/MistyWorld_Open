package ru.liahim.mist.block.tree;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
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

public class MistTrunkWillow extends MistTreeTrunk {

	public MistTrunkWillow() {
		super(4.0F, 2, 2, true, true, true, (MistTreeLeaves)MistBlocks.WILLOW_LEAVES, 1, 0, 5, new int[] {1, 1, 1, 2});
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 10;
    }

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 2;
    }

	@Override
	protected int getMinTrunckLength(World world, BlockPos rootPos, long posRand, int soilDepth) {		
		return Math.min(3 + (int)(posRand % 6) % 3 + (int)(posRand % 5) % 2, 5);
	}

	@Override
	protected int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth) {		
		return minTrunckLength + 7 + (int)(posRand % 6) % 3;
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
		if (rand.nextInt(1000) == 0 && ((MistSoil)soil.getBlock()).getWaterPerm(soil) > 2) {
			SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			return -1;
		}
		float temp = world.getBiome(rootPos).getTemperature(rootPos);
		float humi = MistWorld.getHumi(world, rootPos, 0);
		if (soil.getValue(IWettable.WET) && humus > 0) {
			/**Growth speed*/
			if (branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.2)) != 0) return isBud ? 1 : 0;
			if (temp < -0.5 || temp > 1.8 || humi < 10) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) {
				int trunckSize = world.getBlockState(fixPos).getActualState(world, fixPos).getValue(SIZE);
				if (branchLength <= trunckSize && firstSizeChangeDistance == 1 &&
						totalLength - branchLength < maxTreeHeight - 1 && fixPos.distanceSq(pos) < 9) return 2;
				else return isBud ? 1 : 0;
			} else if (firstSizeChangeDistance < 4) {
				/**Tree height*/
				if (totalLength < maxTreeHeight) return 2;
				else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
			} else return isBud ? 1 : 0;
		} else if (temp > 0 && temp < 1.5 && humi > 30) {
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
				if ((branchLength == 0 && /**Top*/ totalLength != maxTreeHeight - 2) || (dir != EnumFacing.UP && rand.nextInt(2) == 0))
					growthDir.add(face);
			} else if (trunckLength == totalLength - branchLength || branchLength > 0 || dir == EnumFacing.UP ||
					!availableGrowthDirection.contains(EnumFacing.UP) || totalLength - trunckLength == 1) {
				if (branchLength == 0) {
					if (((totalLength > minTrunckLength || !availableGrowthDirection.contains(EnumFacing.UP) ||
							/**Top*/ totalLength == maxTreeHeight - 2) && rand.nextInt(3) > 0) || totalLength == maxTreeHeight - 1) {
						IBlockState downBranch = world.getBlockState((pos.offset(face)).down());
						if (downBranch.getBlock() == this ? getDir(downBranch) != face : true)
						growthDir.add(face);
					}
				} else if (face == dir || rand.nextBoolean()) growthDir.add(face);
			}
		}
		return growthDir;
	}

	@Override
	protected boolean isLeavesRemoved(World world, int totalLength, int branchLength, int minTrunckLength, int trunckLength, int firstBranchDistance, BlockPos pos, BlockPos newPos, EnumFacing dir, Random rand) {
		if (dir != EnumFacing.UP) {
			int count = 0;
			IBlockState checkState;
			for (int i = 1; i < 5; ++i) {
				checkState = world.getBlockState(pos.down(i));
				if (checkState.getBlock() == this.leaves && checkState.getValue(LDIR) == EnumFacing.DOWN)
					++count;
				else break;
			}
			world.setBlockToAir(pos);
			if (count == 0) count = rand.nextInt(2);
			if (count > 0) {
				for (int i = 1; i <= count; ++i) {
					if (checkEnvironment(world, newPos.down(i), false)) {
						world.setBlockState(newPos.down(i), this.leaves.getDefaultState().withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY));
					} else break;
				}
			}
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
			if (lastSegment > 3 + rootSize) return Math.min(trunckLength, lastSegment - 1);
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
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof MistSoil && ((MistSoil)downState.getBlock()).getWaterPerm(downState) < 3 &&
					checkEnvironment(world, pos, false) && world.canBlockSeeSky(pos)) {
				long posRand = MistWorld.getPosRandom(world, pos, 0);
				int minTrunckLength = getMinTrunckLength(world, pos, posRand, 0);
				int maxTreeHight = getMaxTreeHeight(world, pos, minTrunckLength, posRand, 0);
				BlockPos trunkPos = pos;
				int counter = 0;
				for (int i = 0; i < maxTreeHight - 2; ++i) {
					if (checkEnvironment(world, trunkPos.up(), false)) {
						++counter;
						world.setBlockState(trunkPos, this.getDefaultState(), 2);
						trunkPos = trunkPos.up();
					} else break;
				}
				maxTreeHight = counter;
				counter += 2;
				int size = 0;
				int nodeNumber = -1;
				for (int i = 0; i < 4; ++i) {
					counter -= this.nodeDistance[i];
					if (counter >= i + 2) {
						++size;
						++nodeNumber;
					} else {
						counter += this.nodeDistance[i];
						break;
					}
				}
				boolean potential = size == 4;
				if (size == 4) SoilHelper.setSoil(world, pos.down(), ((MistSoil)downState.getBlock()).getSoilBlock().getDefaultState(), 2, true, Mist.FLAG);
				boolean node;
				trunkPos = pos;
				BlockPos checkPos;
				ArrayList<BlockPos> branches = new ArrayList<BlockPos>();
				ArrayList<BlockPos> branches2 = new ArrayList<BlockPos>();
				for (int i = 0; i < maxTreeHight; ++i) {
					node = false;
					if (counter == 0 && nodeNumber >= 0) {
						node = true;
						counter = this.nodeDistance[nodeNumber];
						--nodeNumber;
						--size;
					}
					if (i >= minTrunckLength && i < maxTreeHight - 1) {
						for (EnumFacing dir : EnumFacing.HORIZONTALS) {
							if (rand.nextInt(3) > 0) {
								checkPos = trunkPos.offset(dir);
								if (checkEnvironment(world, checkPos, false)) {
									downState = world.getBlockState(checkPos.down());
									if (downState.getBlock() == this ? getDir(downState) != dir : true) {
										boolean join = false;
										int bud = 0;
										for (int j = 0; j < 3 && j < size + 1; ++j) {
											if (checkEnvironment(world, checkPos.offset(dir), false)) {
												world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, join), 2);
												checkPos = checkPos.offset(dir);
												join = true;
												++bud;
											} else {
												if (j == 1 && size > 2) {
													world.setBlockToAir(checkPos.offset(dir.getOpposite()));
													bud = 0;
												}
												else if (j > 0) world.setBlockState(trunkPos.offset(dir), this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, true), 2);
												break;
											}
										}
										if (bud > 0) createBud(world, trunkPos.offset(dir, bud), dir.getOpposite(), potential);
										if (bud > 1) {
											checkPos = trunkPos.offset(dir);
											for (EnumFacing dir2 : EnumFacing.HORIZONTALS) {
												if (dir2 != dir && dir2 != dir.getOpposite() && rand.nextBoolean()) {
													if (checkEnvironment(world, checkPos.offset(dir2), false)) {
														world.setBlockState(checkPos.offset(dir2), this.getDefaultState().withProperty(DIR, dir2), 2);
														branches.add(checkPos.offset(dir2));
													}
												}
											}
										}
										if (bud > 2) {
											checkPos = trunkPos.offset(dir, 2);
											for (EnumFacing dir2 : DIR.getAllowedValues()) {
												if (dir2 != dir && dir2 != dir.getOpposite() && rand.nextBoolean()) {
													if (checkEnvironment(world, checkPos.offset(dir2), false)) {
														world.setBlockState(checkPos.offset(dir2), this.getDefaultState().withProperty(DIR, dir2), 2);
														branches.add(checkPos.offset(dir2));
													}
												}
											}
											checkPos = trunkPos.offset(dir);
											for (EnumFacing dir2 : EnumFacing.HORIZONTALS) {
												if (dir2 != dir && dir2 != dir.getOpposite() && rand.nextBoolean()) {
													if (checkEnvironment(world, checkPos.offset(dir2), false)) {
														world.setBlockState(checkPos.offset(dir2), this.getDefaultState().withProperty(DIR, dir2), 2);
														branches2.add(checkPos.offset(dir2));
													}
												}
											}
										}
									}
								}
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
				counter = 0;
				for (EnumFacing dir1 : EnumFacing.HORIZONTALS) {
					checkPos = trunkPos.offset(dir1);
					if ((rand.nextBoolean() || (counter == 0 && dir1 == EnumFacing.EAST)) &&
							checkEnvironment(world, checkPos, false)) {
						world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir1), 2);
						if (createBranch(world, checkPos, rand, potential)) ++counter;
						else world.setBlockToAir(checkPos);
					}
				}
				if (counter == 0) createBud(world, trunkPos, EnumFacing.DOWN, potential);
				if (!branches.isEmpty()) {
					for (BlockPos pos1 : branches) {
						downState = world.getBlockState(pos1);
						boolean up = downState.getValue(DIR) == EnumFacing.UP;
						if (!createBranch(world, downState, pos1, pos.up(pos1.getY() - pos.getY() - (up ? 1 : 0)), rand, (up ? EnumFacing.UP : EnumFacing.DOWN), potential)) {
							world.setBlockToAir(pos1);
						}
					}
				}
				if (!branches2.isEmpty()) {
					for (BlockPos pos1 : branches2) {
						boolean check = false;
						for (EnumFacing dir1 : DIR.getAllowedValues()) {
							if (rand.nextBoolean()) {
								checkPos = pos1.offset(dir1);
								if (checkEnvironment(world, checkPos, false)) {
									boolean up = dir1 == EnumFacing.UP;
									downState = this.getDefaultState().withProperty(DIR, dir1);
									world.setBlockState(checkPos, downState, 2);
									if (!createBranch(world, downState, checkPos, pos.up(checkPos.getY() - pos.getY() - (up ? 1 : 0)), rand, (up ? EnumFacing.UP : EnumFacing.DOWN), potential)) {
										world.setBlockToAir(checkPos);
									} else check = true;
								}
							}
						}
						if (!check) {
							downState = world.getBlockState(pos1);
							if (createBud(world, pos1, downState.getValue(DIR).getOpposite(), potential)) {
								world.setBlockState(pos1, downState.withProperty(NODE, true), 2);
							} else world.setBlockToAir(pos1);
						}
					}
				}
				EnumFacing dir;
				for (int x = -4; x < 5; ++x) {
					for (int y = minTrunckLength; y <= maxTreeHight; ++y) {
						for (int z = -4; z < 5; ++z) {
							checkPos = pos.add(x, y, z);
							downState = world.getBlockState(checkPos);
							if (downState.getBlock() == this.leaves) {
								dir = downState.getValue(LDIR);
								if (dir != EnumFacing.UP && dir != EnumFacing.DOWN) {
									int count = (int)MistWorld.getPosRandom(world, checkPos, 4) + 1;
									for (int i = 1; i <= count; ++i) {
										if (this.checkEnvironment(world, checkPos.down(i), false)) {
											if (potential) world.setBlockState(checkPos.down(i), this.leaves.getDefaultState(), 2);
											else world.setBlockState(checkPos.down(i), this.leaves.getDefaultState().withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
										} else break;
									}
								}
							}
						}
					}
				}
				drainZone(world, pos.down(), 2, rand);
				if (checkSnow) checkSnow(world, pos, 3);
			}
		}
	}

	@Override
	protected boolean checkDistanse(BlockPos pos, BlockPos oldPos, BlockPos rootPos) {
		return rootPos.distanceSq(pos) < 9;		
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
					BlockPos branchPos;
					for (int i = 0; i < j; ++i) {
						checkPos = pos.offset(face, i);
						if (world.getBlockState(checkPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(checkPos.up());
						world.setBlockState(checkPos, MistBlocks.WILLOW_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
						if (i > 1) {
							for (EnumFacing branchFace : FacingHelper.NOTDOWN) {
								if (branchFace.getAxis() != face.getAxis() && rand.nextInt(3) == 0) {
									branchPos = checkPos.offset(branchFace);
									if (world.getBlockState(branchPos.offset(face.getOpposite())).getBlock() != MistBlocks.WILLOW_BRANCH) {
										if (world.getBlockState(branchPos).getMaterial().isReplaceable()) {
											if (world.getBlockState(branchPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(branchPos.up());
											world.setBlockState(branchPos, MistBlocks.WILLOW_BRANCH.getDefaultState().withProperty(MistBlockBranch.SIZE, 1).withProperty(MistBlockBranch.AXIS, branchFace.getAxis()), 2);
										}
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
		return new ItemStack(MistBlocks.WILLOW_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.WILLOW_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.WILLOW_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.WILLOW_BLOCK, 1, 7);
	}
}