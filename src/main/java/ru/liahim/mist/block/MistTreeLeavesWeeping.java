package ru.liahim.mist.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.util.WorldUtil;
import ru.liahim.mist.world.MistWorld;

/**@author Liahim*/
public class MistTreeLeavesWeeping extends MistTreeLeaves {

	private final int leavesCount;

	public MistTreeLeavesWeeping(int baseColor, int leavesCount, int bloomMonth, int spoilMonth) {
		super(baseColor, bloomMonth, spoilMonth);
		this.leavesCount = leavesCount;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (this.leavesCount > 0) {
			EnumFacing dir = state.getValue(DIR);
			if (dir == EnumFacing.DOWN) {
				if (this.leavesCount > 1) {
					int count = 0;
					IBlockState checkState;
					EnumFacing checkDir;
					for (int i = 1; i <= this.leavesCount; ++i) {
						checkState = world.getBlockState(pos.up(i));
						if (checkState.getBlock() == this) {
							checkDir = checkState.getValue(DIR);
							if (checkDir == EnumFacing.UP) i = 5;
							else if (checkDir != EnumFacing.DOWN) {
								count = i;
								break;
							}
						} else break;
					}
					if (count > 0) count = this.leavesCount - count;
					if (count > 0) {
						for (int i = 1; i <= count; ++i) {
							checkState = world.getBlockState(pos.down(i));
							if (checkState.getBlock() == this && checkState.getValue(DIR) == EnumFacing.DOWN)
								world.setBlockToAir(pos.down(i));
							else break;
						}
					}
				}
			} else if (dir != EnumFacing.UP) {
				IBlockState checkState;
				for (int i = 1; i <= this.leavesCount; ++i) {
					checkState = world.getBlockState(pos.down(i));
					if (checkState.getBlock() == this && checkState.getValue(DIR) == EnumFacing.DOWN)
						world.setBlockToAir(pos.down(i));
					else break;
				}
			}
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumFacing dir = state.getValue(DIR);
		EnumAge age = state.getValue(AGE);
		if (age == EnumAge.EMPTY || dir == EnumFacing.UP) return dir.getIndex();
		if (dir == EnumFacing.DOWN) {
			if (age == EnumAge.POTENTIAL) return 6;
			return 11;
		}
		if (age == EnumAge.POTENTIAL) return dir.getIndex() + 5;
		return dir.getIndex() + 10;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta < 6) return this.getDefaultState().withProperty(DIR, EnumFacing.getFront(meta)).withProperty(AGE, EnumAge.EMPTY);
		if (meta < 11) return this.getDefaultState().withProperty(DIR, meta == 6 ? EnumFacing.DOWN : EnumFacing.getFront(meta - 5)).withProperty(AGE, EnumAge.POTENTIAL);
		return this.getDefaultState().withProperty(DIR, meta == 11 ? EnumFacing.DOWN : EnumFacing.getFront(meta - 10)).withProperty(AGE, EnumAge.FRUIT);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			EnumFacing dir = state.getValue(DIR);
			if (dir != EnumFacing.DOWN) {
				BlockPos basePos = pos.offset(dir.getOpposite());
				IBlockState baseState = world.getBlockState(basePos);
				if (baseState.getBlock() != this.trunkBlock) {
					world.setBlockToAir(pos);
					return;
				} else if (rand.nextInt(this.trunkBlock.getGrowthSpeed()) == 0 && dir != EnumFacing.UP) {
					BlockPos checkPos;
					IBlockState checkState;
					int count = (int)MistWorld.getPosRandom(world, pos, this.leavesCount + (this.leavesCount > 1 ? 0 : 1)) + (this.leavesCount > 1 ? 1 : 0);
					for (int i = 1; i <= count; ++i) {
						checkPos = pos.down(i);
						checkState = world.getBlockState(checkPos);
						if (checkState.getBlock() == this) {
							if (checkState.getValue(DIR) != EnumFacing.DOWN) i = this.leavesCount + 1;
							else if (i == count) {
								checkPos = checkPos.down();
								checkState = world.getBlockState(checkPos);
								if (checkState.getBlock() == this && checkState.getValue(DIR) == EnumFacing.DOWN)
									world.setBlockToAir(checkPos);
							}
						} else if (this.trunkBlock.checkEnvironment(world, checkPos)) {
							for (;;) {
								basePos = basePos.offset(baseState.getValue(MistTreeTrunk.DIR).getOpposite());
								baseState = world.getBlockState(basePos);
								if (baseState.getBlock() != this.trunkBlock) break;
							}
							if (baseState.getBlock() instanceof MistSoil && SoilHelper.getHumus(baseState) > 0) {
								world.setBlockState(checkPos, this.getDefaultState().withProperty(AGE, EnumAge.EMPTY));
								return;
							}
							break;
						} else break;
					}
				}
			}
			if (dir != EnumFacing.UP) {
				IBlockState leaves = getSeasonState(world, pos, state, MistTime.getTickOfMonth(world));
				if (leaves != null) world.setBlockState(pos, leaves);
			}
		}
	}

	@Override
	public void updateLeaves(World world, BlockPos pos, IBlockState state, BlockPos rootPos, IBlockState rootState,
			BlockPos soilPos, IBlockState soil, Random rand) {
		if (state.getValue(DIR) != EnumFacing.UP && rand.nextInt(MistTime.getDayInMonth()) == 0) {
			int count = (int)MistWorld.getPosRandom(world, pos, this.leavesCount + (this.leavesCount > 1 ? 0 : 1)) + (this.leavesCount > 1 ? 1 : 0);
			if (count > 0) {
				count = rand.nextInt(count) + 1;
				BlockPos lPos = pos;
				IBlockState leaves;
				for (int i = 0; i < count; ++i) {
					leaves = world.getBlockState(lPos.down());
					if (leaves.getBlock() == this && leaves.getValue(DIR) == EnumFacing.DOWN) {
						lPos = lPos.down();
					} else break;
				}
				leaves = world.getBlockState(lPos);
				if (leaves.getBlock() == this) {
					if (leaves.getValue(AGE) == EnumAge.EMPTY) {
						if (this.bloomMonth < this.spoilMonth ? MistTime.getMonth() < this.bloomMonth || MistTime.getMonth() > this.spoilMonth :
							MistTime.getMonth() > this.bloomMonth && MistTime.getMonth() < this.spoilMonth) {
							if (rootState.getValue(MistTreeTrunk.SIZE) == 4 && (rootState.getValue(MistTreeTrunk.DIR) == EnumFacing.EAST ||
									rootState.getValue(MistTreeTrunk.DIR) == EnumFacing.WEST) && world.getLightBrightness(lPos) > 0.45) {
								WorldUtil.simpleSetBlock(world, lPos, leaves.withProperty(AGE, EnumAge.POTENTIAL));
							}
						}
					}
				}
			}
		}
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		IBlockState state = world.getBlockState(pos);
		if (state.getValue(DIR) == EnumFacing.DOWN) {
			for (int i = 1; i <= this.leavesCount; ++i) {
				state = world.getBlockState(pos.up(i));
				if (state.getBlock() == this && state.getValue(DIR) != EnumFacing.DOWN) return ret;
			}
		}
		ret.add(new ItemStack(this));
		return ret;
	}
}