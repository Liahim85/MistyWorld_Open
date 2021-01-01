package ru.liahim.mist.block.tree;

import java.util.ArrayList;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.block.MistBlockBranch;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.block.MistTreeLeaves;
import ru.liahim.mist.block.MistTreeTrunk;
import ru.liahim.mist.block.MistWoodBlock;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;
import ru.liahim.mist.world.biome.BiomeMist;

public class MistTrunkPoplar extends MistTreeTrunk {

	public MistTrunkPoplar() {
		super(3.0F, 1, 2, false, false, false, (MistTreeLeaves)MistBlocks.POPLAR_LEAVES, 1, 0, 4, new int[] {3, 3, 3, 4});
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
		return 2 + (int)(posRand % 5) % 2;
	}

	@Override
	protected int getMaxTreeHeight(World world, BlockPos rootPos, int minTrunckLength, long posRand, int soilDepth) {		
		return 17 + soilDepth*2 + (int)(posRand % 6) % 3;
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
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			if (state.getActualState(world, pos).getValue(SIZE) == 4) {
				if (rand.nextInt(200) == 0) {
					world.playSound(null, pos, MistSounds.BLOCK_WOOD_CREAK, SoundCategory.BLOCKS, 0.5F, world.rand.nextFloat() * 0.4F + 0.8F);
				}
			} else if (pos.getY() > 130 && MistWorld.canPlayAmbiendSounds(world, pos)) {
				Biome biome = world.getBiome(pos);
				if (biome instanceof BiomeMist && ((BiomeMist)biome).getBiomeType() == EnumBiomeType.Swamp) {
					long tick = world.getWorldTime() % 24000;
					if (tick > 12000 && rand.nextInt(20 + (int) Math.abs(tick - 18000)/200) == 0) {
						world.playSound(null, pos, MistSounds.BLOCK_SWAMP_BIRD, SoundCategory.AMBIENT, 1.5F, world.rand.nextFloat() * 0.2F + 0.9F);
					} else if (rand.nextInt(500) == 0) {
						world.playSound(null, pos, MistSounds.BLOCK_SWAMP_BIRD, SoundCategory.AMBIENT, 1.5F, world.rand.nextFloat() * 0.2F + 0.9F);
					}
				}
			}
		}
		super.updateTick(world, pos, state, rand);
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
		if (rand.nextInt(500) == 0 && ((MistSoil)soil.getBlock()).getWaterPerm(soil) > 2) {
			SoilHelper.setSoil(world, soilPos, soil, 3, 2);
			return -1;
		}
		float temp = world.getBiome(rootPos).getTemperature(rootPos);
		float humi = MistWorld.getHumi(world, rootPos, 0);
		if (soil.getValue(IWettable.WET) && humus > 0) {
			/**Growth speed*/
			if (branchLength == 0 && rand.nextInt((int)Math.ceil(trunckLength * 0.1)) != 0) return isBud ? 1 : 0;
			if (temp < -0.5 || temp > 2 || humi < 10) return 0;
			if (availableGrowthDirection.isEmpty()) return isBud ? 1 : 0;
			/**Is branch*/
			if (branchLength > 0) {
				if (branchLength < 4) return availableGrowthDirection.contains(EnumFacing.UP) ? 2 : isBud ? 1 : 0;
				else return isBud ? 1 : 0;
			} else if (firstSizeChangeDistance < 4) {
				/**Tree height*/
				if (totalLength < maxTreeHeight && totalLength - trunckLength < 8) return 2;
				else return trySetDead(world, rootPos, rootState, soilPos, soil, isBud, rand);
			} else return isBud ? 1 : 0;
		} else if (temp > 0 && temp < 1.5 && humi > 40) {
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
			if (face == EnumFacing.UP) growthDir.add(face);
			else if ((branchLength == 0 || totalLength - trunckLength == 0) && (size > 0 || (isBud && !availableGrowthDirection.contains(EnumFacing.UP))) && rand.nextBoolean()) {
				growthDir.add(face);
			}
		}
		return growthDir;
	}

	@Override
	protected boolean isLeavesRemoved(World world, int totalLength, int branchLength, int minTrunckLength, int trunckLength, int firstBranchDistance, BlockPos pos, BlockPos newPos, EnumFacing dir, Random rand) {
		if (branchLength > 1) return rand.nextInt(4) == 0;
		else if (branchLength == 1 || totalLength <= minTrunckLength || totalLength - trunckLength == 1 || rand.nextInt(3) > 0) return true;
		else {
			boolean delete = false;
			IBlockState downPos;
			for (int i = 1; i <= 2; ++i) {
				downPos = world.getBlockState(pos.down(i));
				if ((downPos.getBlock() == this.leaves && downPos.getValue(LDIR) == dir) ||
						(downPos.getBlock() == this && getDir(downPos) == dir)) delete = true;
			}
			return delete;
		}
	}

	@Override
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

	@Override
	protected int newNodeDistance(World world, BlockPos rootPos, IBlockState rootState, int totalLength, int branchLength,
		int firstSizeChangeDistance, int firstBendDistance, int firstBranchDistance, int trunckLength, int minTrunckLength,
		int maxTreeHeight, ArrayList<Integer> segments, Random rand) {
		int rootSize = rootState.getValue(SIZE);
		if (branchLength == 0 && rootSize < 4) {
			int lastSegment = segments.isEmpty() ? totalLength : segments.get(segments.size() - 1);
			if (lastSegment > 5 + rootSize) return Math.min(trunckLength, 3 + rootSize);
		}
		return 0;
	}

	@Override
	public void generateTree(World world, BlockPos pos, Random rand, boolean checkSnow) {
		if (!world.isRemote) {
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() instanceof MistSoil && ((MistSoil)downState.getBlock()).getWaterPerm(downState) < 3 &&
					checkEnvironment(world, pos, false) && world.canBlockSeeSky(pos)) {
				long posRand = MistWorld.getPosRandom(world, pos, 0);
				int soilDepth = getSoilDepth(world, pos);
				int minTrunckLength = getMinTrunckLength(world, pos, posRand, soilDepth);
				int maxTreeHight = getMaxTreeHeight(world, pos, minTrunckLength, posRand, soilDepth);
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
							if (rand.nextInt(3) == 0) {
								checkPos = trunkPos.offset(dir);
								if (checkEnvironment(world, checkPos, false)) {
									downState = world.getBlockState(checkPos.down());
									if (downState.getBlock() == this ? getDir(downState) != dir : true) {
										downState = world.getBlockState(checkPos.down(2));
										if (downState.getBlock() == this ? getDir(downState) != dir : true) {
											if (size == 0) {
												if (potential) world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, dir), 2);
												else world.setBlockState(checkPos, this.leaves.getDefaultState().withProperty(LDIR, dir).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
											} else if (checkEnvironment(world, checkPos.offset(dir), false)) {
												world.setBlockState(checkPos, this.getDefaultState().withProperty(DIR, dir).withProperty(NODE, true), 2);
												branches.add(checkPos);
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
				createBud(world, trunkPos, EnumFacing.DOWN, potential);
				if (!branches.isEmpty()) {
					for (BlockPos pos1 : branches) {
						if (checkEnvironment(world, pos1.up(), false)) {
							for (int i = 1; i < 4; ++i) {
								checkPos = pos1.up(i);
								if (checkEnvironment(world, checkPos.up(), false)) {
									world.setBlockState(checkPos, this.getDefaultState().withProperty(NODE, i == 1), 2);
									if (i == 3) {
										createBud(world, checkPos, EnumFacing.DOWN, potential);
										world.setBlockState(pos1, world.getBlockState(pos1).withProperty(NODE, false), 2);
									}
									else {
										for (EnumFacing face : EnumFacing.HORIZONTALS) {
											if (rand.nextInt(4) > 0 && checkEnvironment(world, checkPos.offset(face), false)) {
												if (potential) world.setBlockState(checkPos.offset(face), this.leaves.getDefaultState().withProperty(LDIR, face), 2);
												else world.setBlockState(checkPos.offset(face), this.leaves.getDefaultState().withProperty(LDIR, face).withProperty(MistTreeLeaves.AGE, MistTreeLeaves.EnumAge.EMPTY), 2);
											}
										}
									}
								} else {
									createBud(world, checkPos.down(), i == 1 ? world.getBlockState(checkPos.down()).getValue(DIR).getOpposite() : EnumFacing.DOWN, potential);
									break;
								}
							}
						}						
						else createBud(world, pos1, world.getBlockState(pos1).getValue(DIR).getOpposite(), potential);
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
				int j = rand.nextInt(5) + 4;
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
						world.setBlockState(checkPos, MistBlocks.POPLAR_BLOCK.getDefaultState().withProperty(MistWoodBlock.AXIS, MistWoodBlock.EnumAxis.fromFacingAxis(face.getAxis())), 2);
						if (checkSnow && world.canSnowAt(checkPos.up(), false)) world.setBlockState(checkPos.up(), Blocks.SNOW_LAYER.getDefaultState());
						if (i > 1) {
							for (EnumFacing branchFace : FacingHelper.NOTDOWN) {
								if (branchFace.getAxis() != face.getAxis() && rand.nextInt(4) == 0) {
									branchPos = checkPos.offset(branchFace);
									if (world.getBlockState(branchPos.offset(face.getOpposite())).getBlock() != MistBlocks.POPLAR_BRANCH &&
											world.getBlockState(branchPos.offset(face.getOpposite(), 2)).getBlock() != MistBlocks.POPLAR_BRANCH &&
											world.getBlockState(branchPos.offset(face.getOpposite(), 3)).getBlock() != MistBlocks.POPLAR_BRANCH) {
										if (world.getBlockState(branchPos).getMaterial().isReplaceable()) {
											if (world.getBlockState(branchPos.up()).getBlock() == Blocks.DOUBLE_PLANT) world.setBlockToAir(branchPos.up());
											world.setBlockState(branchPos, MistBlocks.POPLAR_BRANCH.getDefaultState().withProperty(MistBlockBranch.SIZE, 0).withProperty(MistBlockBranch.AXIS, branchFace.getAxis()), 2);
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
		return new ItemStack(MistBlocks.POPLAR_BRANCH);
	}

	@Override
	protected ItemStack getTrunk() {
		return new ItemStack(MistBlocks.POPLAR_BRANCH, 1, 6);
	}

	@Override
	protected ItemStack getBlock() {
		return new ItemStack(MistBlocks.POPLAR_BLOCK);
	}

	@Override
	protected ItemStack getNode() {
		return new ItemStack(MistBlocks.POPLAR_BLOCK, 1, 7);
	}
}