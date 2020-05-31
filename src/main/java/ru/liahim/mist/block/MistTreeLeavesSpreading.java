package ru.liahim.mist.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.util.WorldUtil;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**@author Liahim*/
public class MistTreeLeavesSpreading extends MistTreeLeaves {

	private final boolean blooming;

	public MistTreeLeavesSpreading(int baseColor, int bloomMonth, int spoilMonth, boolean blooming) {
		super(baseColor, bloomMonth, spoilMonth);
		this.blooming = blooming;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		IBlockState checkState;
		if (state.getValue(DIR) != EnumFacing.DOWN && state.getValue(DIR) != EnumFacing.UP) {
			for (EnumFacing face : EnumFacing.HORIZONTALS) {
				checkState = world.getBlockState(pos.offset(face));
				if (checkState.getBlock() == this && checkState.getValue(DIR) == EnumFacing.DOWN) {
					world.setBlockToAir(pos.offset(face));
				}
			}
		}
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		EnumFacing dir = state.getValue(DIR);
		EnumAge age = state.getValue(AGE);
		if (age == EnumAge.EMPTY || dir != EnumFacing.DOWN) return dir.getIndex();
		if (age == EnumAge.POTENTIAL) return 6;
		if (age == EnumAge.BLOOMY) return 7;
		return 8;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta < 6) return this.getDefaultState().withProperty(DIR, EnumFacing.getFront(meta)).withProperty(AGE, EnumAge.EMPTY);
		if (meta == 6) return this.getDefaultState().withProperty(DIR, EnumFacing.DOWN).withProperty(AGE, EnumAge.POTENTIAL);
		if (meta == 7) return this.getDefaultState().withProperty(DIR, EnumFacing.DOWN).withProperty(AGE, EnumAge.BLOOMY);
		return this.getDefaultState().withProperty(DIR, EnumFacing.DOWN).withProperty(AGE, EnumAge.FRUIT);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if (!world.isRemote) {
			EnumFacing dir = state.getValue(DIR);
			if (dir != EnumFacing.DOWN) {
				EnumFacing baseDir = dir.getOpposite();
				BlockPos basePos = pos.offset(baseDir);
				IBlockState baseState = world.getBlockState(basePos);
				if (baseState.getBlock() != this.trunkBlock) world.setBlockToAir(pos);
				else if (dir != EnumFacing.UP && rand.nextInt(Math.max(1, this.trunkBlock.getGrowthSpeed() / 2)) == 0) {
					dir = EnumFacing.HORIZONTALS[rand.nextInt(4)];
					if (dir != baseDir && canPlaceBlockAt(world, pos.offset(dir)) && this.trunkBlock.checkEnvironment(world, pos.offset(dir))) {
						for (;;) {
							basePos = basePos.offset(baseState.getValue(MistTreeTrunk.DIR).getOpposite());
							baseState = world.getBlockState(basePos);
							if (baseState.getBlock() != this.trunkBlock) break;
						}
						if (baseState.getBlock() instanceof MistSoil && SoilHelper.getHumus(baseState) > 0)
							world.setBlockState(pos.offset(dir), this.getDefaultState().withProperty(AGE, EnumAge.EMPTY));
					}
				}
			} else {
				IBlockState leaves = getSeasonState(world, pos, state, MistTime.getTickOfMonth(world));
				if (leaves != null) world.setBlockState(pos, leaves);
			}
		}
	}

	@Override
	public void updateLeaves(World world, BlockPos pos, IBlockState state, BlockPos rootPos, IBlockState rootState,
			BlockPos soilPos, IBlockState soil, Random rand) {
		if (state.getValue(DIR) != EnumFacing.UP && rand.nextInt(MistTime.getDayInMonth()) == 0) {
			EnumFacing lDir = EnumFacing.HORIZONTALS[rand.nextInt(4)];
			if (lDir != state.getValue(DIR).getOpposite()) {
				BlockPos lPos = pos.offset(lDir);
				IBlockState leaves = world.getBlockState(lPos);
				if (leaves.getBlock() == this) {
					if (leaves.getValue(DIR) == EnumFacing.DOWN && leaves.getValue(AGE) == EnumAge.EMPTY) {
						int bloom = this.blooming ? (this.bloomMonth + MistTime.monthCount - 1) % MistTime.monthCount : this.bloomMonth;
						if (bloom < this.spoilMonth ? MistTime.getMonth() < bloom || MistTime.getMonth() > this.spoilMonth :
								MistTime.getMonth() > bloom && MistTime.getMonth() < this.spoilMonth) {
							if (rootState.getValue(MistTreeTrunk.SIZE) > 2 && (rootState.getValue(MistTreeTrunk.DIR) == EnumFacing.EAST ||
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
	public IBlockState getSeasonState(World world, BlockPos pos, IBlockState state, long monthTick) {
		if (state.getValue(DIR) == EnumFacing.DOWN && state.getValue(AGE) != EnumAge.EMPTY) {
			if (this.blooming && MistTime.getMonth() == (this.bloomMonth + MistTime.monthCount - 1) % MistTime.monthCount) {
				if (state.getValue(AGE) == EnumAge.POTENTIAL) {
					long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
					if (r < MistTime.getTickInMonth()) {
						if (r < monthTick) return state.withProperty(AGE, EnumAge.BLOOMY);
					}
				}
			} else if (this.bloomMonth < this.spoilMonth ? MistTime.getMonth() >= this.bloomMonth && MistTime.getMonth() < this.spoilMonth :
					MistTime.getMonth() >= this.bloomMonth || MistTime.getMonth() < this.spoilMonth) {
				if (this.blooming) {
					if (state.getValue(AGE) == EnumAge.BLOOMY) {
						if (MistTime.getMonth() == this.bloomMonth) {
							long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
							if (r < MistTime.getTickInMonth()) {
								if (r < monthTick) return state.withProperty(AGE, EnumAge.FRUIT);
							}
						} else return state.withProperty(AGE, EnumAge.FRUIT);
					} else if (state.getValue(AGE) == EnumAge.POTENTIAL) {
						if (MistTime.getMonth() == this.bloomMonth) {
							long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
							if (r < MistTime.getTickInMonth()) {
								return state.withProperty(AGE, EnumAge.BLOOMY);
							}
						}
					}
				} else {
					if (state.getValue(AGE) == EnumAge.POTENTIAL) {
						long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
						if (r < MistTime.getTickInMonth()) {
							if (MistTime.getMonth() == this.bloomMonth) {
								if (r < monthTick) return state.withProperty(AGE, EnumAge.FRUIT);
							} else return state.withProperty(AGE, EnumAge.FRUIT);
						}
					}
				}
			} else {
				if (state.getValue(AGE) == EnumAge.FRUIT) {
					if (MistTime.getMonth() == this.spoilMonth) {
						long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
						if (r < MistTime.getTickInMonth()) {
							if (r < monthTick) return state.withProperty(AGE, EnumAge.POTENTIAL);
						}
					} else return state.withProperty(AGE, EnumAge.POTENTIAL);
				} else if (state.getValue(AGE) == EnumAge.POTENTIAL) {
					if (MistTime.getMonth() == this.spoilMonth) {
						long r = MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() * 3);
						if (r < MistTime.getTickInMonth()) {
							if (r >= monthTick) return state.withProperty(AGE, EnumAge.FRUIT);
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		IBlockState state = world.getBlockState(pos);
		if (state.getValue(DIR) == EnumFacing.DOWN) {
			for (EnumFacing face : EnumFacing.HORIZONTALS) {
				state = world.getBlockState(pos.offset(face));
				if (state.getBlock() == this && state.getValue(DIR) != EnumFacing.DOWN) return ret;
			}
		}
		ret.add(new ItemStack(this));
		return ret;
	}
}