package ru.liahim.mist.block.tree;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeLeaves;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.MistWoodBlock;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;

public class MistTrunkSTree extends MistTreeTrunk {

	public MistTrunkSTree() {
		super(10.0F, 1, 2, false, true, true, (MistTreeLeaves)MistBlocks.S_TREE_LEAVES, 1, 0, 8, new int[] {1, 1, 1, 1});
		this.setResistance(15);
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 5;
    }

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        return 1;
    }

	@Override
	protected int getMinTrunckLength(World world, BlockPos rootPos, long posRand, int soilDepth) {		
		return 1 + (int)(posRand % 6) % 3;
	}

	@Override
	protected int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth) {		
		return minTrunckLength + 6 + (int)(posRand % 5) % 2;
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
		if (rand.nextInt(100) == 0 && ((MistSoil)soil.getBlock()).getWaterPerm(soil) < 3) {
			SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			return -1;
		}
		float temp = world.getBiome(rootPos).getTemperature(rootPos);
		float humi = MistWorld.getHumi(world, rootPos, 0);
		if (soil.getValue(IWettable.WET) && humus > 0) {
			/**Growth speed*/
			if (branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.2)) != 0) return isBud ? 1 : 0;
			if (temp < -1 || temp >= 1.5 || humi < 5) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) {
				int trunckSize = world.getBlockState(fixPos).getActualState(world, fixPos).getValue(SIZE);
				if (branchLength == Math.min(trunckSize, trunckLength <= minTrunckLength + 1 ? 0 : 1) &&
						(totalLength < maxTreeHeight || trunckLength == totalLength - branchLength)) return 2;
				else return isBud ? 1 : 0;
			} else if ((firstSizeChangeDistance == 0 && totalLength < 3) || firstSizeChangeDistance == 1) {
				/**Tree height*/
				if (totalLength < maxTreeHeight) return 2;
				else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
			} else return isBud ? 1 : 0;
		} else if (temp >= -0.5 && temp <= 0.5 && humi > 20) {
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
				if ((branchLength == 0 && /**Top*/(totalLength != maxTreeHeight - 2 || rand.nextInt(2) == 0)) || (dir != EnumFacing.UP && rand.nextInt(3) > 0))
					growthDir.add(face);
			} else if (totalLength == maxTreeHeight - 1 || trunckLength == totalLength - branchLength || branchLength > 0 || dir == EnumFacing.UP || !availableGrowthDirection.contains(EnumFacing.UP)) {
				if (branchLength == 0) {
					if ((totalLength > minTrunckLength || !availableGrowthDirection.contains(EnumFacing.UP)) && rand.nextInt(3) > 0) {
						if (totalLength >= maxTreeHeight - 2) growthDir.add(face);
						else {
							IBlockState downBranch = world.getBlockState((pos.offset(face)).down());
							if (downBranch.getBlock() == this ? getDir(downBranch) != face : true)
							growthDir.add(face);
						}
					}
				} else if (face == dir ? rand.nextInt(3) > 0 : true) growthDir.add(face);
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
			if (lastSegment > Math.max(2, minTrunckLength - 2 + rootSize) && rand.nextInt(rootSize + 1) == 0) return Math.min(trunckLength, lastSegment - 1);
		}
		return 0;
	}

	@Override
	public void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof MistSoil && ((MistSoil)downState.getBlock()).getWaterPerm(downState) > 2 &&
					checkEnvironment(world, pos, false) && world.canBlockSeeSky(pos)) {
				boolean cropTop = rand.nextBoolean();
				long posRand = MistWorld.getPosRandom(world, pos, 0);
				int minTrunckLength = getMinTrunckLength(world, pos, posRand, 0);
				int maxTreeHight = getMaxTreeHeight(world, pos, minTrunckLength, posRand, 0) - (cropTop ? 2 : 0);
				BlockPos trunkPos = pos;
				int counter = 0;
				for (int i = 0; i < maxTreeHight; ++i) {
					if (checkEnvironment(world, trunkPos.up(), false)) {
						++counter;
						world.setBlockState(trunkPos, this.getDefaultState(), 2);
						trunkPos = trunkPos.up();
					} else break;
				}
				counter += cropTop ? 2 : 0;
				maxTreeHight = counter;				
				int size = 0;
				int nodeNumber = -1;
				for (int i = 0; i < 4; ++i) {
					counter -= this.nodeDistance[i];
					if (counter >= Math.max(2, minTrunckLength - 2 + i)) {
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
				maxTreeHight -= cropTop ? 2 : 0;
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
					if (i >= minTrunckLength && i < maxTreeHight - 1) {
						for (EnumFacing dir : EnumFacing.HORIZONTALS) {
							if (rand.nextInt(3) > 0) {
								checkPos = trunkPos.offset(dir);
								if (checkEnvironment(world, checkPos, false)) {
									downState = world.getBlockState(checkPos.down());
									if ((cropTop && i == maxTreeHight - 1) ||
											downState.getBlock() == this ? getDir(downState) != dir : true) {
										boolean normalBranch = rand.nextInt(3) > 0;
										boolean check = false;
										for (EnumFacing dir1 : DIR.getAllowedValues()) {
											if (dir1 != EnumFacing.UP && dir1 != dir.getOpposite() && (normalBranch || dir1 != dir) &&
													checkEnvironment(world, checkPos.offset(dir1), false)) {
												if (createBud(world, checkPos.offset(dir1), dir1.getOpposite(), potential)) {
													world.setBlockState(checkPos.offset(dir1), this.getDefaultState().withProperty(DIR, dir1).withProperty(NODE, dir1 == dir), 2);
													check = true;
												}
											}
										}
										if (check) {
											world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir), 2);
											branches.add(checkPos.up());
										}
										else if (createBud(world, checkPos, dir.getOpposite(), potential))
											world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, true), 2);
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
				if (cropTop) {
					for (EnumFacing dir1 : EnumFacing.HORIZONTALS) {
						checkPos = trunkPos.offset(dir1);
						if ((rand.nextInt(2) == 0 || (counter == 0 && dir1 == EnumFacing.EAST)) && checkEnvironment(world, checkPos.up(2), false)) {
							world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir1), 2);
							world.setBlockState(checkPos.up(), this.getDefaultState(), 2);
							createBud(world, checkPos.up(), EnumFacing.DOWN, potential);
							++counter;
						}
					}
				}
				if (counter == 0 || !cropTop) createBud(world, trunkPos, EnumFacing.DOWN, potential);
				if (!branches.isEmpty()) {
					for (BlockPos pos1 : branches) {
						if (rand.nextInt(4) == 0 && checkEnvironment(world, pos1, false) && createBud(world, pos1, EnumFacing.DOWN, potential))
							world.setBlockState(pos1, this.getDefaultState().withProperty(DIR, EnumFacing.UP).withProperty(NODE, true), 2);
					}
				}
				drainZone(world, pos.down(), 2, rand);
				if (checkSnow) checkSnow(world, pos, 3);
			}
		}
	}

	@Override
	public void generateTrunk(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			if (world.getBlockState(pos.down()).getBlock() instanceof MistSoil) {
				EnumFacing face = EnumFacing.getHorizontal(rand.nextInt(4));
				int j = rand.nextInt(2) + 2;
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
					for (int i = 0; i < j; ++i) {
						checkPos = pos.offset(face, i);
						if (world.getBlockState(checkPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(checkPos.up());
						world.setBlockState(checkPos, MistBlocks.S_TREE_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
					}
				}
			}
		}
	}

	@Override
	protected ItemStack getBranch() {
		return new ItemStack(MistBlocks.S_TREE_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.S_TREE_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.S_TREE_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.S_TREE_BLOCK, 1, 7);
	}
}