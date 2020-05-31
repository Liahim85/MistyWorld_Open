package ru.liahim.mist.block.upperplant;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.block.IRubberBallCollideble;
import ru.liahim.mist.api.block.ISeasonalChanges;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.EntityRubberBall;
import ru.liahim.mist.init.BlockColoring;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.util.WorldUtil;
import ru.liahim.mist.world.MistWorld;

public class MistDesertCotton extends BlockBush implements IColoredBlock, ISeasonalChanges, IRubberBallCollideble {

	@Override
	@SideOnly(Side.CLIENT)
	public IBlockColor getBlockColor() {
		return new IBlockColor() {
			@Override
			public int colorMultiplier(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex) {
				return world != null && pos != null ? getMixColor(world, pos, state, tintIndex) : 0xFFFFFFFF;
			}
		};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColor() {
		return BlockColoring.BLOCK_ITEM_COLORING;
	}

	public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 11);
	public static final PropertyBool ISUP = PropertyBool.create("isup");
	protected static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.4D, 0.0D, 0.4D, 0.6D, 0.6875D, 0.6D);
	protected static final AxisAlignedBB FULL_UP_AABB = new AxisAlignedBB(0.4D, 0.0D, 0.4D, 0.6D, 1.0D, 0.6D);
	protected static final AxisAlignedBB BLOOM_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 0.8125D, 0.7D);
	protected static final AxisAlignedBB FULL_BUSH_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 1.0D, 0.7D);

	public MistDesertCotton() {
		super();
		this.setSoundType(SoundType.PLANT);
		this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0).withProperty(ISUP, false));
		this.setHardness(0.2F);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		int age = state.getActualState(world, pos).getValue(AGE);
		if (state.getValue(ISUP)) return age == 0 ? UP_AABB : age <= 5 ? BLOOM_AABB : FULL_UP_AABB;
		else if (age >= 3 && age <= 6) return FULL_BUSH_AABB;
		return BUSH_AABB;
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		int age = state.getActualState(world, pos).getValue(AGE);
		if (state.getValue(ISUP)) return age <= 5 ? UP_AABB : FULL_UP_AABB;
		else if (age >= 4 && age <= 6) return FULL_UP_AABB;
		return NULL_AABB;
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
		int age = state.getActualState(world, pos).getValue(AGE);
		if (!state.getValue(ISUP) && age >= 3 && age <= 6) {
			List<RayTraceResult> list = Lists.<RayTraceResult>newArrayList();
			list.add(this.rayTrace(pos, start, end, BUSH_AABB));
			list.add(this.rayTrace(pos, start, end, FULL_UP_AABB));
			RayTraceResult raytraceresult1 = null;
			double d1 = 0.0D;
			for (RayTraceResult raytraceresult : list) {
				if (raytraceresult != null) {
					double d0 = raytraceresult.hitVec.squareDistanceTo(end);
					if (d0 > d1) {
						raytraceresult1 = raytraceresult;
						d1 = d0;
					}
				}
			}
			return raytraceresult1;
		} else return super.collisionRayTrace(state, world, pos, start, end);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (hand == EnumHand.MAIN_HAND) {
			//if (player.getHeldItem(hand).isEmpty()) {
				if (state.getValue(ISUP)) {
					int age = state.getValue(AGE);
					if (age >= 1 && age <= 3) {
						Random rand = world instanceof World ? world.rand : RANDOM;
						int i = rand.nextInt(2) + 1;
						int j = rand.nextInt(2) + 1;
						if (age == 1) {
							world.setBlockState(pos, state.withProperty(AGE, 4));
							for (BlockPos downPos = pos.down(); world.getBlockState(downPos).getBlock() == this; downPos = downPos.down()) {
								if (!world.getBlockState(downPos).getValue(ISUP)) {
									world.setBlockState(downPos, state.withProperty(ISUP, false).withProperty(AGE, 10));
								}
							}
							Block.spawnAsEntity(world, pos, new ItemStack(MistItems.DESERT_COTTON_SEED, i + j, 0));
						} else if (age == 2) {
							world.setBlockState(pos, state.withProperty(AGE, 5));
							for (BlockPos downPos = pos.down(); world.getBlockState(downPos).getBlock() == this; downPos = downPos.down()) {
								if (!world.getBlockState(downPos).getValue(ISUP)) {
									world.setBlockState(downPos, state.withProperty(ISUP, false).withProperty(AGE, 10));
								} else {
									world.setBlockState(downPos, state.withProperty(ISUP, true).withProperty(AGE, 7));
								}
							}
							Block.spawnAsEntity(world, pos, new ItemStack(MistItems.DESERT_COTTON_SEED, i, 0));
							Block.spawnAsEntity(world, pos, new ItemStack(MistItems.DESERT_COTTON_SEED, j, 1));
						} else if (age == 3) {
							world.setBlockState(pos, state.withProperty(AGE, 5));
							for (BlockPos downPos = pos.down(); world.getBlockState(downPos).getBlock() == this; downPos = downPos.down()) {
								if (!world.getBlockState(downPos).getValue(ISUP)) {
									int ageDown = world.getBlockState(downPos).getValue(AGE);
									if (ageDown == 2) world.setBlockState(downPos, state.withProperty(ISUP, false).withProperty(AGE, 10));
									if (ageDown == 7) world.setBlockState(downPos, state.withProperty(ISUP, false).withProperty(AGE, 11));
								}
							}
							Block.spawnAsEntity(world, pos, new ItemStack(MistItems.DESERT_COTTON_SEED, i + j, 1));
						}
					}
				}
			//}
		}
		return false;
	}

	/*@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		if (world.getBlockState(pos.down()).getBlock() == this) return this.getDefaultState().withProperty(ISUP, true);
		return this.getDefaultState();
    }*/
	
	//TODO Need light check!

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos, state, rand);
		if (!world.isRemote && !state.getValue(ISUP) && !MistWorld.isPosInFog(world, pos)) {
			Biome biome = world.getBiome(pos);
			IBlockState checkState = world.getBlockState(pos.down());
			if (checkState.getBlock() instanceof MistSoil && SoilHelper.getHumus(checkState) > 0 && ((MistSoil)checkState.getBlock()).getWaterPerm(checkState) > 1 &&
					biome.getTemperature(pos) > 0.5F && biome.getRainfall() > 0 && biome.getRainfall() <= 0.5F) {
				int age = state.getValue(AGE);
				float percent = getBloomPersent(world, pos, MistTime.getTickOfMonth(world));
				if (age < 8) {
					int size = (int) MistWorld.getPosRandom(world, pos, 4);
					if (size == 1 || size == 2) size = 1;
					if (size == 3) size = 2;
					boolean air = true;
					for (int i = 0; i <= size; ++i) {
						checkState = world.getBlockState(pos.up(1 + i));
						if (checkState.getBlock() != Blocks.AIR && checkState.getBlock() != this) {
							air = false;
							break;
						}
					}
					if (age == 0) {
						if (percent >= 0.05) world.setBlockState(pos, state.withProperty(AGE, 1));
					} else if (age == 1) {
						if (percent >= 0.1) world.setBlockState(pos, state.withProperty(AGE, 2));
					} else if (age == 2) {
						if (percent >= 0.5) {
							if (percent < 0.55) {
								if (size > 0 && world.isAirBlock(pos.up(2))) {
									world.setBlockState(pos.up(2), this.getDefaultState().withProperty(ISUP, true));
								}
							} else if (percent < 0.6) {
								if (size > 1 && world.isAirBlock(pos.up(3))) {
									if (world.isAirBlock(pos.up(2))) world.setBlockState(pos.up(2), this.getDefaultState().withProperty(ISUP, true));
									world.setBlockState(pos.up(3), this.getDefaultState().withProperty(ISUP, true));
								}
							} else if (percent < 0.8) {
								BlockPos checkPos = pos.up();
								while (world.getBlockState(checkPos).getBlock() == this) checkPos = checkPos.up();
								checkPos = checkPos.down();
								if (world.getBlockState(checkPos).getValue(ISUP)) {
									int j = 1;
									if (percent >= 0.7) j = 2;
									if (percent >= 0.75) j = 3;
									world.setBlockState(checkPos, this.getDefaultState().withProperty(ISUP, true).withProperty(AGE, j));
								}
							} else {
								world.setBlockState(pos, this.getDefaultState().withProperty(AGE, 7));
							}
						} else if (percent >= 0.4) {
							if (!air) world.setBlockState(pos, state.withProperty(AGE, 10));
							else if (world.isAirBlock(pos.up())) world.setBlockState(pos, state.withProperty(AGE, 3));
						}
					} else if (age == 3) {
						if (percent >= 0.45 && world.isAirBlock(pos.up())) {
							world.setBlockState(pos, state.withProperty(AGE, 4));
							world.setBlockState(pos.up(), this.getDefaultState().withProperty(ISUP, true));
						}
					} else if (age == 7) {
						if (percent >= 0.95) {
							for (int i = 1; i < 4; ++i) {
								if (world.getBlockState(pos.up(i)).getBlock() == this) {
									world.setBlockState(pos.up(i), Blocks.AIR.getDefaultState());
								} else break;
							}
						} else if (percent >= 0.85) {
							BlockPos checkPos = pos.up();
							while (world.getBlockState(checkPos).getBlock() == this) checkPos = checkPos.up();
							checkPos = checkPos.down();
							if (world.getBlockState(checkPos).getValue(ISUP)) {
								world.setBlockState(checkPos, this.getDefaultState().withProperty(ISUP, true).withProperty(AGE, 5));
							}
						} else if (percent < 0.8) {
							dissemination(world, pos, rand);
						}
					}
				} else {
					boolean grow = rand.nextInt(1 + MistTime.getDayInMonth() / ModConfig.time.desertCottonBloomCount) == 0;
					if (age == 8 && percent < 0.05) world.setBlockState(pos, state.withProperty(AGE, 0));
					else if (age == 9 && percent >= 0.05 && percent < 0.1) world.setBlockState(pos, state.withProperty(AGE, 1));
					else if (age == 10) {
						BlockPos checkPos = pos.up();
						while (world.getBlockState(checkPos).getBlock() == this) checkPos = checkPos.up();
						checkPos = checkPos.down();
						if (world.getBlockState(checkPos).getValue(ISUP)) {
							if (percent >= 0.95 || percent < 0.1 || grow) {
								int ageUp = world.getBlockState(checkPos).getValue(AGE);
								if (ageUp == 4 || ageUp == 5) {
									for (int i = 1; i < 4; ++i) {
										if (world.getBlockState(pos.up(i)).getBlock() == this) {
											world.setBlockState(pos.up(i), Blocks.AIR.getDefaultState());
										} else break;
									}
								}
							}
						} else if (percent >= 0.1 && percent < 0.4) world.setBlockState(pos, state.withProperty(AGE, 2));
					} else if (age == 11) {
						BlockPos checkPos = pos.up();
						while (world.getBlockState(checkPos).getBlock() == this) checkPos = checkPos.up();
						checkPos = checkPos.down();
						if (world.getBlockState(checkPos).getValue(ISUP)) {
							if (percent >= 0.95 || percent < 0.1 || grow) {
								int ageUp = world.getBlockState(checkPos).getValue(AGE);
								if (ageUp == 4 || ageUp == 5) {
									for (int i = 1; i < 4; ++i) {
										if (world.getBlockState(pos.up(i)).getBlock() == this) {
											world.setBlockState(pos.up(i), Blocks.AIR.getDefaultState());
										} else break;
									}
								}
							}
						} else if (percent >= 0.95) {
							world.setBlockState(pos, state.withProperty(AGE, 7));
						} else if (percent < 0.05) {
							dissemination(world, pos, rand);
						} else world.setBlockToAir(pos);
					} else if (age < 10 && grow) {
						world.setBlockState(pos, state.withProperty(AGE, age + 1));
					}
				}
			}
		}
	}

	private void dissemination(World world, BlockPos pos, Random rand) {
		BlockPos checkPos;
		int k = 0;
		for (int i = 0; i < 3; ++i) {
			checkPos = world.getHeight(pos.add(rand.nextInt(5) - 2, 0, rand.nextInt(5) - 2)).up();
			if (this.canBlockStay(world, checkPos, this.getDefaultState())) {
				world.setBlockState(checkPos, this.getDefaultState());
				++k;
			}
		}
		if (k > 0) world.setBlockToAir(pos);
		else world.setBlockState(pos, this.getDefaultState());
	}

	@Override
	public IBlockState getSeasonState(World world, BlockPos pos, IBlockState state, long monthTick) {
		if (!state.getValue(ISUP) && !MistWorld.isPosInFog(world, pos)) {
			Biome biome = world.getBiome(pos);
			IBlockState newState = world.getBlockState(pos.down());
			if (newState.getBlock() instanceof MistSoil && SoilHelper.getHumus(newState) > 0 && ((MistSoil)newState.getBlock()).getWaterPerm(newState) > 1 &&
					biome.getTemperature(pos) > 0.5F && biome.getRainfall() > 0 && biome.getRainfall() <= 0.5F) {
				int age = state.getValue(AGE);
				if (age < 8) {
					newState = null;
					float percent = getBloomPersent(world, pos, monthTick);
					int size = (int) MistWorld.getPosRandom(world, pos, 4);
					if (size == 1 || size == 2) size = 1;
					if (size == 3) size = 2;
					IBlockState checkState;
					if (percent < 0.5 || percent >= 0.95) {
						for (int i = 1; i < 4; ++i) {
							checkState = world.getBlockState(pos.up(i));
							if (checkState.getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(i), Blocks.AIR.getDefaultState(), true);
							} else break;
						}
					}
					if (percent < 0.05) newState = state.withProperty(AGE, 0);
					else if (percent < 0.1) newState = state.withProperty(AGE, 1);
					else if (percent < 0.4) newState = state.withProperty(AGE, 2);
					else if (percent < 0.45) newState = state.withProperty(AGE, 3);
					else if (percent < 0.8) {
						newState = state.withProperty(AGE, 2);
						if (percent < 0.5) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 0).withProperty(ISUP, true), true);
							for (int i = 2; i < 4; ++i) {
								checkState = world.getBlockState(pos.up(i));
								if (checkState.getBlock() == this) {
									WorldUtil.simpleSetBlock(world, pos.up(i), Blocks.AIR.getDefaultState(), true);
								} else break;
							}
						} else if (percent < 0.55) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 0).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 0).withProperty(ISUP, true), true);
									if (world.getBlockState(pos.up(3)).getBlock() == this) WorldUtil.simpleSetBlock(world, pos.up(3), Blocks.AIR.getDefaultState(), true);
								}
							}
						} else if (percent < 0.6) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 0).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 0).withProperty(ISUP, true), true);
									if (size > 1 && (world.isAirBlock(pos.up(3)) || world.getBlockState(pos.up(3)).getBlock() == this))
										WorldUtil.simpleSetBlock(world, pos.up(3), this.getDefaultState().withProperty(AGE, 0).withProperty(ISUP, true), true);
								}
							}
						} else if (percent < 0.7) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 1).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 1).withProperty(ISUP, true), true);
									if (size > 1 && (world.isAirBlock(pos.up(3)) || world.getBlockState(pos.up(3)).getBlock() == this))
										WorldUtil.simpleSetBlock(world, pos.up(3), this.getDefaultState().withProperty(AGE, 1).withProperty(ISUP, true), true);
								}
							}
						} else if (percent < 0.75) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 2).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 2).withProperty(ISUP, true), true);
									if (size > 1 && (world.isAirBlock(pos.up(3)) || world.getBlockState(pos.up(3)).getBlock() == this))
										WorldUtil.simpleSetBlock(world, pos.up(3), this.getDefaultState().withProperty(AGE, 2).withProperty(ISUP, true), true);
								}
							}
						} else {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 3).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 3).withProperty(ISUP, true), true);
									if (size > 1 && (world.isAirBlock(pos.up(3)) || world.getBlockState(pos.up(3)).getBlock() == this))
										WorldUtil.simpleSetBlock(world, pos.up(3), this.getDefaultState().withProperty(AGE, 3).withProperty(ISUP, true), true);
								}
							}
						}
					} else {
						newState = state.withProperty(AGE, 7);
						if (percent < 0.85) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 3).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 3).withProperty(ISUP, true), true);
									if (size > 1 && (world.isAirBlock(pos.up(3)) || world.getBlockState(pos.up(3)).getBlock() == this))
										WorldUtil.simpleSetBlock(world, pos.up(3), this.getDefaultState().withProperty(AGE, 3).withProperty(ISUP, true), true);
								}
							}
						} else if (percent < 0.95) {
							if (world.isAirBlock(pos.up()) || world.getBlockState(pos.up()).getBlock() == this) {
								WorldUtil.simpleSetBlock(world, pos.up(), this.getDefaultState().withProperty(AGE, 5).withProperty(ISUP, true), true);
								if (size > 0 && (world.isAirBlock(pos.up(2)) || world.getBlockState(pos.up(2)).getBlock() == this)) {
									WorldUtil.simpleSetBlock(world, pos.up(2), this.getDefaultState().withProperty(AGE, 5).withProperty(ISUP, true), true);
									if (size > 1 && (world.isAirBlock(pos.up(3)) || world.getBlockState(pos.up(3)).getBlock() == this))
										WorldUtil.simpleSetBlock(world, pos.up(3), this.getDefaultState().withProperty(AGE, 5).withProperty(ISUP, true), true);
								}
							}
						} else if (age < 10) {
							for (int i = 1; i < 4; ++i) {
								if (world.getBlockState(pos.up(i)).getBlock() == this) {
									WorldUtil.simpleSetBlock(world, pos.up(i), Blocks.AIR.getDefaultState(), true);
								} else break;
							}
						}
					}
					if (newState == state) return null;
					else return newState;
				}
			}
		}
		return null;
	}

	private float getBloomPersent(World world, BlockPos pos, long monthTick) {
		long bloomRange = MistTime.getTickInYear() / ModConfig.time.desertCottonBloomCount;
		return (float)((MistTime.getMonth() * MistTime.getTickInMonth() + monthTick + MistWorld.getPosRandom(world, pos, 0) % (MistTime.getTickInMonth() / (ModConfig.time.desertCottonBloomCount * 2))) % bloomRange) / bloomRange;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IBlockState stateUp = world.getBlockState(pos.up());
		if (world.getBlockState(pos.up()).getBlock() == this) {
			int age = state.getValue(AGE);
			int ageUp = stateUp.getActualState(world, pos.up()).getValue(AGE);
			if (state.getValue(ISUP)) {
				if (age != 7) {
					if (ageUp < 2 || ageUp == 4) return state.withProperty(AGE, 6);
					else if (ageUp == 2) return state.withProperty(AGE, 7);
					else if (ageUp == 3 || ageUp == 5) return state.withProperty(AGE, 8);
					else return state.withProperty(AGE, ageUp);
				}
			} else {
				if (age == 2 || age == 10) {
					if (ageUp == 2 || ageUp == 3 || ageUp == 5 || ageUp == 8) return state.withProperty(AGE, 5);
					else return state.withProperty(AGE, 4);
				} else if (age == 7 || age == 11) return state.withProperty(AGE, 6);
			}
		}
		return state;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return world.getBlockState(pos.down()).getBlock() == this || super.canPlaceBlockAt(world, pos);
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		if (state.getBlock() != this) return super.canBlockStay(world, pos, state);
		else if (state.getValue(ISUP)) return world.getBlockState(pos.down()).getBlock() == this;
		else return super.canBlockStay(world, pos, state);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		if (state.getValue(ISUP)) {
			IBlockState down = world.getBlockState(pos.down());
			if (down.getBlock() == this) {
				if (down.getValue(ISUP)) {
					world.destroyBlock(pos.down(), true);
				} else {
					int age = down.getValue(AGE);
					if (age == 2) world.setBlockState(pos.down(), down.withProperty(AGE, 10));
					else if (age == 7) world.setBlockState(pos.down(), down.withProperty(AGE, 11));
				}
			}
		}
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int age = state.getActualState(world, pos).getValue(AGE);
		if (state.getValue(ISUP)) {
			ret.add(new ItemStack(Items.STICK));
			int i = rand.nextInt(2) + 1;
			int j = rand.nextInt(2) + 1;
			if (age == 1) {
				ret.add(new ItemStack(MistItems.DESERT_COTTON_SEED, i + j, 0));
			} else if (age == 2) {
				ret.add(new ItemStack(MistItems.DESERT_COTTON_SEED, i, 0));
				ret.add(new ItemStack(MistItems.DESERT_COTTON_SEED, j, 1));
			} else if (age == 3) {
				ret.add(new ItemStack(MistItems.DESERT_COTTON_SEED, i + j, 1));
			}
		} else if (age == 0 || age == 8) {
			ret.add(new ItemStack(MistItems.DESERT_COTTON_SEED, 1, 1));
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Plains;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { AGE, ISUP });
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int age = state.getValue(AGE);
		if (state.getValue(ISUP)) {
			if (age < 6) return age + 9;
			else if (age == 7) return 15;
			else return 9;
		} else {
			if (age < 4) return age;
			else if (age < 6) return 2;
			else if (age < 8) return 4;
			else return age - 3;
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		int age;
		if (meta < 4) age = meta;
		else if (meta == 4) age = 7;
		else if (meta < 9) age = meta + 3;
		else if (meta < 15) age = meta - 9;
		else age = 7;
		return this.getDefaultState().withProperty(ISUP, meta > 8).withProperty(AGE, age);
	}

	private int getMixColor(IBlockAccess world, BlockPos pos, IBlockState state, int tintIndex) {
		int biomeColor = BiomeColorHelper.getGrassColorAtPos(world, pos);
		if (tintIndex == 0) {
			int age = state.getValue(AGE);
			if (state.getValue(ISUP)) {
				if (age == 2 || age == 3 || age == 5 || age == 8) return this.getBrownColor(biomeColor);
				else return this.getGreenColor(biomeColor);
			} else {
				if (age == 6 || age == 7 || age == 11) return this.getBrownColor(biomeColor);
				else return this.getGreenColor(biomeColor);
			}
		} else if (tintIndex == 1) {
			int age = state.getValue(AGE);
			if (state.getValue(ISUP)) {
				if (age == 1 || age == 2) return this.getYellowColor(biomeColor);
				else if (age == 3) return this.getWhiteColor(biomeColor);
				else if (age == 7) return this.getBrownColor(biomeColor);
				else return this.getGreenColor(biomeColor);
			} else {
				if (age == 5) return this.getBrownColor(biomeColor);
				else return this.getGreenColor(biomeColor);
			}
		} else if (tintIndex == 2) return this.getWhiteColor(biomeColor);
		return biomeColor;
	}

	private int getGreenColor(int biomeColor) {
		int r = (((0x226F2D >> 16) & 255) + ((biomeColor >> 16) & 255) * 3)/4;
		int g = (((0x226F2D >> 8) & 255) + ((biomeColor >> 8) & 255) * 3)/4;
		int b = ((0x226F2D & 255) + (biomeColor & 255) * 3)/4;
		return r << 16 | g << 8 | b;
	}

	private int getBrownColor(int biomeColor) {
		int r = (((biomeColor >> 16) & 255) + ((0xC8A064 >> 16) & 255) * 2)/3;
		int g = (((biomeColor >> 8) & 255) + ((0xC8A064 >> 8) & 255) * 2)/3;
		int b = ((biomeColor & 255) + (0xC8A064 & 255) * 2)/3;
		return r << 16 | g << 8 | b;
	}

	private int getYellowColor(int biomeColor) {
		int r = (((biomeColor >> 16) & 255) + ((0xFFDE27 >> 16) & 255) * 2)/3;
		int g = (((biomeColor >> 8) & 255) + ((0xFFDE27 >> 8) & 255) * 2)/3;
		int b = ((biomeColor & 255) + (0xFFDE27 & 255) * 2)/3;
		return r << 16 | g << 8 | b;
	}

	private int getWhiteColor(int biomeColor) {
		int r = (((biomeColor >> 16) & 255) + ((0xFFFFFF >> 16) & 255) * 2)/3;
		int g = (((biomeColor >> 8) & 255) + ((0xFFFFFF >> 8) & 255) * 2)/3;
		int b = ((biomeColor & 255) + (0xFFFFFF & 255) * 2)/3;
		return r << 16 | g << 8 | b;
	}

	@Override
	public boolean isCollide(World world, IBlockState state, EntityRubberBall ball, RayTraceResult result, Random rand) {
		if (!world.isRemote && ball.getMotion().lengthSquared() > 0.5F) {
			world.destroyBlock(result.getBlockPos(), true);
			ball.motionX *= rand.nextDouble() * 0.5 + 0.5;
			ball.motionY *= rand.nextDouble() * 0.5 + 0.5;
			ball.motionZ *= rand.nextDouble() * 0.5 + 0.5;
			return false;
		} else return true;
	}
}