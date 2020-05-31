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
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeLeaves;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.MistWoodBlock;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;

public class MistTrunkOak extends MistTreeTrunk {

	public MistTrunkOak() {
		super(5.0F, 2, 2, true, true, true, (MistTreeLeaves)MistBlocks.OAK_LEAVES, 1, 0, 5, new int[] {1, 1, 1, 2});
	}

	@Override
	protected int getMinTrunckLength(World world, BlockPos rootPos, long posRand, int soilDepth) {		
		return 2 + (int)(posRand % 5) % 2;
	}

	@Override
	protected int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth) {		
		return minTrunckLength + 8 + (int)(posRand % 7) % 4;
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
			if (branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.2)) != 0) return isBud ? 1 : 0;
			if (temp < -0.5 || temp > 2 || humi < 5) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) {
				int trunckSize = world.getBlockState(fixPos).getActualState(world, fixPos).getValue(SIZE);
				if (branchLength < Math.min(trunckSize, trunckLength <= minTrunckLength + 1 ? 2 : 3)) return 2;
				else return isBud ? 1 : 0;
			} else if (firstSizeChangeDistance < 4) {
				/**Tree height*/
				if (totalLength < maxTreeHeight) return 2;
				/**Old form*/
				else if ((rand.nextInt(8) == 0 && totalLength == maxTreeHeight &&
						getShiftedNodes(world, size, totalLength, branchLength, firstSizeChangeDistance, firstBendDistance, firstBranchDistance, segments, nodes, rand).isEmpty()) ||
						totalLength == maxTreeHeight + 1) return 2;
				else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
			} else return isBud ? 1 : 0;
		} else if (temp > 0 && temp < 1.5 && humi > 20) {
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
				if ((branchLength == 0 && /**Old form*/ totalLength != maxTreeHeight) || (dir != EnumFacing.UP && rand.nextInt(3) == 0))
					growthDir.add(face);
			} else if (trunckLength == totalLength - branchLength || branchLength > 0 || dir == EnumFacing.UP || !availableGrowthDirection.contains(EnumFacing.UP)) {
				if (branchLength == 0) {
					if ((totalLength > minTrunckLength || !availableGrowthDirection.contains(EnumFacing.UP) ||
							/**Old form*/ totalLength == maxTreeHeight) && rand.nextInt(3) > 0) {
						IBlockState downBranch = world.getBlockState((pos.offset(face)).down());
						if (downBranch.getBlock() == this ? getDir(downBranch) != face : true)
						growthDir.add(face);
					}
				} else growthDir.add(face);
			}
		}
		return growthDir;
	}

	@Override
	protected int newNodeDistance(World world, BlockPos rootPos, IBlockState rootState, int totalLength, int branchLength,
		int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength,
		int maxTreeHeight, ArrayList<Integer> segments, Random rand) {
		int rootSize = rootState.getValue(SIZE);
		if (branchLength == 0 && rootSize < 4) {
			int lastSegment = segments.isEmpty() ? totalLength : segments.get(segments.size() - 1);
			if (lastSegment > 3 + rootSize) return Math.min(trunckLength, 2 + rootSize);
		}
		return 0;
	}

	@Override
	public void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof MistSoil && checkEnvironment(world, pos, false) && world.canBlockSeeSky(pos)) {
				long posRand = MistWorld.getPosRandom(world, pos, 0);
				int minTrunckLength = getMinTrunckLength(world, pos, posRand, 0);
				int maxTreeHight = getMaxTreeHeight(world, pos, minTrunckLength, posRand, 0);
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
					} else {
						counter += this.nodeDistance[i];
						break;
					}
				}
				boolean potential = size == 4;
				if (size == 4) SoilHelper.setSoil(world, pos.down(), ((MistSoil)downState.getBlock()).getSoilBlock().getDefaultState(), 2, true, Mist.FLAG);
				boolean node;
				trunkPos = pos;
				counter += 2;
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
					if (i >= minTrunckLength && i < maxTreeHight - 1) {
						for (EnumFacing dir : EnumFacing.HORIZONTALS) {
							if (rand.nextInt(3) > 0) {
								checkPos = trunkPos.offset(dir);
								if (checkEnvironment(world, checkPos, false)) {
									downState = world.getBlockState(checkPos.down());
									if (downState.getBlock() == this ? getDir(downState) != dir : true) {
										boolean join = i == minTrunckLength;
										int bud = 0;
										for (int j = 0; j < (i == minTrunckLength ? 2 : 3) && j < size; ++j) {
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
										if (bud > 1) branches.add(trunkPos.offset(dir, bud - 1));
										if (bud > 2) {
											checkPos = trunkPos.offset(dir);
											for (EnumFacing dir2 : EnumFacing.HORIZONTALS) {
												if (dir2 != dir && dir2 != dir.getOpposite() && rand.nextInt(2) == 0) {
													if (checkEnvironment(world, checkPos.offset(dir2), false)) {
														world.setBlockState(checkPos.offset(dir2), this.getDefaultState().withProperty(DIR, dir2), 2);
														branches.add(checkPos.offset(dir2));
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
					if ((rand.nextInt(2) == 0 || (counter == 0 && dir1 == EnumFacing.EAST)) && checkEnvironment(world, checkPos.up(2), false)) {
						world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir1), 2);
						world.setBlockState(checkPos.up(), this.getDefaultState(), 2);
						createBud(world, checkPos.up(), EnumFacing.DOWN, potential);
						++counter;
					}
				}
				if (counter == 0) createBud(world, trunkPos, EnumFacing.DOWN, potential);
				if (!branches.isEmpty()) {
					for (BlockPos pos1 : branches) {
						createBranch(world, pos1, rand, potential);
					}
				}
				drainZone(world, pos.down(), 3, rand);
				if (checkSnow) checkSnow(world, pos, 4);
			}
		}
	}

	@Override
	public void generateTrunk(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			if (world.getBlockState(pos.down()).getBlock() instanceof MistSoil) {
				EnumFacing face = EnumFacing.getHorizontal(rand.nextInt(4));
				int j = rand.nextInt(4) + 3;
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
						world.setBlockState(checkPos, MistBlocks.OAK_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
						if (i > 1) {
							for (EnumFacing branchFace : FacingHelper.NOTDOWN) {
								if (branchFace.getAxis() != face.getAxis() && rand.nextInt(3) == 0) {
									branchPos = checkPos.offset(branchFace);
									if (world.getBlockState(branchPos.offset(face.getOpposite())).getBlock() != MistBlocks.OAK_BRANCH) {
										if (world.getBlockState(branchPos).getMaterial().isReplaceable()) {
											if (world.getBlockState(branchPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(branchPos.up());
											world.setBlockState(branchPos, MistBlocks.OAK_BRANCH.getDefaultState().withProperty(MistBlockBranch.SIZE, 1).withProperty(MistBlockBranch.AXIS, branchFace.getAxis()), 2);
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
		return new ItemStack(MistBlocks.OAK_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.OAK_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.OAK_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.OAK_BLOCK, 1, 7);
	}
}