package ru.liahim.mist.block.downplant;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import ru.liahim.mist.api.block.IDownPlant;
import ru.liahim.mist.api.block.IMistAdsorbent;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistAcidSoil;
import ru.liahim.mist.block.MistBlock;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.MistWorld;

/**@author Liahim*/
public class MistSponge extends MistBlock implements IPlantable, IMistAdsorbent, IDownPlant {

	public static final PropertyInteger STAGE = PropertyInteger.create("stage", 0, 5);
	public static final PropertyInteger VARIANT = PropertyInteger.create("var", 0, 8);

	public MistSponge() {
		super(Material.GRASS, MapColor.WHITE_STAINED_HARDENED_CLAY);
		this.setSoundType(SoundType.PLANT);
		this.setDefaultState(this.blockState.getBaseState().withProperty(STAGE, 5).withProperty(VARIANT, 8));
		this.setHardness(0.4F);
		this.setTickRandomly(true);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int st = state.getValue(STAGE);
		int var = state.getActualState(source, pos).getValue(VARIANT);
		double n = 0.0D;
		double e = 1.0D;
		double s = 1.0D;
		double w = 0.0D;
		double u = 1.0D;
		if (st == 0) {
			switch (var) {
			case 0: n = 0.3125D; e = 0.6875D; s = 0.6875D; w = 0.3125D; u = 0.375D; break;
			case 1: n = 0.25D;   e = 0.6875D; s = 0.625D;  w = 0.3125D; u = 0.375D; break;
			case 2: n = 0.25D;   e = 0.75D;   s = 0.625D;  w = 0.375D;  u = 0.375D; break;
			case 3: n = 0.3125D; e = 0.75D;   s = 0.6875D; w = 0.375D;  u = 0.375D; break;
			case 4: n = 0.375D;  e = 0.75D;   s = 0.75D;   w = 0.375D;  u = 0.375D; break;
			case 5: n = 0.375D;  e = 0.6875D; s = 0.75D;   w = 0.3125D; u = 0.375D; break;
			case 6: n = 0.375D;  e = 0.625D;  s = 0.75D;   w = 0.25D;   u = 0.375D; break;
			case 7: n = 0.3125D; e = 0.625D;  s = 0.6875D; w = 0.25D;   u = 0.375D; break;
			case 8: n = 0.25D;   e = 0.625D;  s = 0.625D;  w = 0.25D;   u = 0.375D; break;
			}
		} else if (st == 1) {
			switch (var) {
			case 0: n = 0.1875D; e = 0.8125D; s = 0.8125D; w = 0.1875D; u = 0.625D; break;
			case 1: n = 0.125D;  e = 0.8125D; s = 0.75D;   w = 0.1875D; u = 0.625D; break;
			case 2: n = 0.125D;  e = 0.875D;  s = 0.75D;   w = 0.25D;   u = 0.625D; break;
			case 3: n = 0.1875D; e = 0.875D;  s = 0.8125D; w = 0.25D;   u = 0.625D; break;
			case 4: n = 0.25D;   e = 0.875D;  s = 0.875D;  w = 0.25D;   u = 0.625D; break;
			case 5: n = 0.25D;   e = 0.8125D; s = 0.875D;  w = 0.1875D; u = 0.625D; break;
			case 6: n = 0.25D;   e = 0.75D;   s = 0.875D;  w = 0.125D;  u = 0.625D; break;
			case 7: n = 0.1875D; e = 0.75D;   s = 0.8125D; w = 0.125D;  u = 0.625D; break;
			case 8: n = 0.125D;  e = 0.75D;   s = 0.75D;   w = 0.125D;  u = 0.625D; break;
			}
		} else if (st == 2) {
			switch (var) {
			case 0: n = 0.3125D; e = 0.6875D; s = 0.6875D; w = 0.3125D; u = 1.0D; break;
			case 1: n = 0.1875D; e = 0.6875D; s = 0.5625D; w = 0.3125D; u = 1.0D; break;
			case 2: n = 0.1875D; e = 0.8125D; s = 0.5625D; w = 0.4375D; u = 1.0D; break;
			case 3: n = 0.3125D; e = 0.8125D; s = 0.6875D; w = 0.4375D; u = 1.0D; break;
			case 4: n = 0.4375D; e = 0.8125D; s = 0.8125D; w = 0.4375D; u = 1.0D; break;
			case 5: n = 0.4375D; e = 0.6875D; s = 0.8125D; w = 0.3125D; u = 1.0D; break;
			case 6: n = 0.4375D; e = 0.5625D; s = 0.8125D; w = 0.1875D; u = 1.0D; break;
			case 7: n = 0.3125D; e = 0.5625D; s = 0.6875D; w = 0.1875D; u = 1.0D; break;
			case 8: n = 0.1875D; e = 0.5625D; s = 0.5625D; w = 0.1875D; u = 1.0D; break;
			}
		} else if (st == 3) {
			switch (var) {
			case 0: n = 0.1875D; e = 0.8125D; s = 0.8125D; w = 0.1875D; u = 1.0D; break;
			case 1: n = 0.125D;  e = 0.8125D; s = 0.75D;   w = 0.1875D; u = 1.0D; break;
			case 2: n = 0.125D;  e = 0.875D;  s = 0.75D;   w = 0.25D;   u = 1.0D; break;
			case 3: n = 0.1875D; e = 0.875D;  s = 0.8125D; w = 0.25D;   u = 1.0D; break;
			case 4: n = 0.25D;   e = 0.875D;  s = 0.875D;  w = 0.25D;   u = 1.0D; break;
			case 5: n = 0.25D;   e = 0.8125D; s = 0.875D;  w = 0.1875D; u = 1.0D; break;
			case 6: n = 0.25D;   e = 0.75D;   s = 0.875D;  w = 0.125D;  u = 1.0D; break;
			case 7: n = 0.1875D; e = 0.75D;   s = 0.8125D; w = 0.125D;  u = 1.0D; break;
			case 8: n = 0.125D;  e = 0.75D;   s = 0.75D;   w = 0.125D;  u = 1.0D; break;
			}
		} else if (st == 4) {
			if (var < 4) {
				n = 0.0625D; e = 0.9375D; s = 0.9375D; w = 0.0625D; u = 1.0D;
			}
		} else if (st == 5) {
			if (var == 8) {
				n = 0.375D; e = 0.625D; s = 0.625D; w = 0.375D; u = 0.125D;
			} else if (var < 3) {
				n = 0.0625D; e = 0.9375D; s = 0.9375D; w = 0.0625D; u = 0.8125D;
			} else if (var < 6) {
				n = 0.0625D; e = 0.9375D; s = 0.9375D; w = 0.0625D; u = 0.9375D;
			} else {
				n = 0.0D; e = 1.0D; s = 1.0D; w = 0.0D; u = 0.9375D;
			}
		}
		return new AxisAlignedBB(w, 0.0D, n, e, u, s);
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return getBoundingBox(state, world, pos);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote) {
			int st = state.getValue(STAGE);
			int var = state.getActualState(world, pos).getValue(VARIANT);
			if (st == 4 ? var < 6 : true) {
				if (!(world.getBlockState(pos.down()).getBlock() instanceof MistAcidSoil) && (st <= 2 || (st == 5 && var == 8))) {
					if (st == 1 ? world.getBlockState(pos.down()).getBlock() != this : true)
						world.destroyBlock(pos, true);
				}
			} else if (var < 8) {
				int i = checkLiquid(world, pos);
				if (i < 0) world.setBlockState(pos, state.withProperty(VARIANT, 8));
				else if (i > 0 && var == 7 && !MistWorld.isPosInFog(world, pos)) {
					world.setBlockState(pos, state.withProperty(VARIANT, 6));
				}
			}
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote && state.getValue(STAGE) == 4) {
			int var = state.getValue(VARIANT);
			if (var == 6 || var == 7) {
				int i = checkLiquid(world, pos);
				if (i < 0) world.setBlockState(pos, state.withProperty(VARIANT, 8));
				else if (i > 0 && var == 7 && !MistWorld.isPosInFog(world, pos)) {
					world.setBlockState(pos, state.withProperty(VARIANT, 6));
				}
			}
		}
	}

	private int checkLiquid(World world, BlockPos pos) {
		IBlockState checkState;
		int i = 0;
		for (EnumFacing face : FacingHelper.NOTDOWN) {
			checkState = world.getBlockState(pos.offset(face));
			if (checkState.getMaterial() == Material.WATER) {
				if (checkState.getBlock() == MistBlocks.ACID_BLOCK) {
					return -1;
				} else if (checkState.getBlock() == Blocks.WATER || checkState.getBlock() == Blocks.FLOWING_WATER) {
					i = 1;
				}
			}
		}
		return i;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int st = state.getValue(STAGE);
		int var = state.getActualState(world, pos).getValue(VARIANT);
		if (st == 5 && var == 4 && side == EnumFacing.UP) {
			if (!world.isRemote) {
				ItemStack stack = new ItemStack(MistItems.SPONGE_SLIME);
				EntityItem entity_item = new EntityItem(world, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, stack);
				entity_item.setDefaultPickupDelay();
				world.spawnEntity(entity_item);
				world.playSound(null, pos, SoundEvents.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 0.2F, 1.5F); //TODO change pitch
				world.playSound(null, pos, SoundEvents.BLOCK_CLOTH_BREAK, SoundCategory.BLOCKS, 0.5F, 0.8F); //TODO change pitch
				world.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 3));
				if (world.getBlockState(pos.down(2)).getBlock() == this && world.getBlockState(pos.down(2)).getValue(STAGE) == 4 && world.getBlockState(pos.down(2)).getActualState(world, pos.down(2)).getValue(VARIANT) == 2) {
					world.setBlockState(pos.down(2), state.withProperty(STAGE, 4).withProperty(VARIANT, 1));
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		if (!worldIn.isRemote) {
			int mutateChance = 4;
			int aging = 4;
			int st = state.getValue(STAGE);
			int var = state.getActualState(worldIn, pos).getValue(VARIANT);
			boolean fog = MistWorld.isPosInFog(worldIn, pos);
			boolean soil = worldIn.getBlockState(pos.down()).getBlock() instanceof MistAcidSoil;
			int downSt = -1;
			int downVar = -1;
			if (worldIn.getBlockState(pos.down()).getBlock() == this) {
				downSt = worldIn.getBlockState(pos.down()).getValue(STAGE);
				downVar = worldIn.getBlockState(pos.down()).getActualState(worldIn, pos.down()).getValue(VARIANT);
			}
			int upSt = -1;
			int upVar = -1;
			if (worldIn.getBlockState(pos.up()).getBlock() == this) {
				upSt = worldIn.getBlockState(pos.up()).getValue(STAGE);
				upVar = worldIn.getBlockState(pos.up()).getActualState(worldIn, pos.up()).getValue(VARIANT);
			}
			int downSt2 = -1;
			int downVar2 = -1;
			if (worldIn.getBlockState(pos.down(2)).getBlock() == this) {
				downSt2 = worldIn.getBlockState(pos.down(2)).getValue(STAGE);
				downVar2 = worldIn.getBlockState(pos.down(2)).getActualState(worldIn, pos.down(2)).getValue(VARIANT);
			}
			if (rand.nextInt(8) == 0) { //GROWTH RATE
				if (st == 0) {
					worldIn.setBlockState(pos, state.withProperty(STAGE, 1).withProperty(VARIANT, var), 3);
				} else if (st == 1) {
					if (soil) {
						if (worldIn.isAirBlock(pos.up())) {
							worldIn.setBlockState(pos, state.withProperty(STAGE, 2).withProperty(VARIANT, var), 3);
							worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 1).withProperty(VARIANT, var), 3);
						}
					} else if (downSt > -1) {
						if (downSt == 2) {
							if (worldIn.isAirBlock(pos.up()) && fog) {
								worldIn.setBlockState(pos, state.withProperty(STAGE, 3).withProperty(VARIANT, var), 3);
								worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 1).withProperty(VARIANT, var), 3);
							}
						} else if (downSt == 3) {
							if (worldIn.isAirBlock(pos.up()) && fog) {
								if (downSt2 == 3) {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 0), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 0), 3);
								} else if (rand.nextInt(mutateChance) == 0) {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 3).withProperty(VARIANT, var), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 1).withProperty(VARIANT, var), 3);
								} else {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 0), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 0), 3);
								}
							}
						} else worldIn.destroyBlock(pos, false);
					} else worldIn.destroyBlock(pos, false);
				} else if (st == 2) {
					if (!soil || ((upSt < 0 || upSt > 4 || (upSt == 4 && upVar >= 6))))
						worldIn.destroyBlock(pos, false);
				} else if (st == 3) {
					if (downSt < 2 || downSt > 3 || (upSt < 3 && upSt != 1) || (upSt == 4 && upVar >= 6) || upSt == 5)
						worldIn.destroyBlock(pos, false);
				} else if (st == 4) {
					if (fog) {
						if (var == 0) {
							boolean fog2 = pos.up(2).getY() < MistWorld.getFogHight(worldIn, 0) + 4.0F;
							if (downSt < 3 || (downSt == 4 && downVar > 0) || upSt < 4 || (upSt == 4 && upVar >= 6) || (upSt == 5 && upVar != 0))
								worldIn.destroyBlock(pos, false);
							else if (downSt == 3 && upSt == 5) {
								if (rand.nextInt(mutateChance) == 0 || !worldIn.isAirBlock(pos.up(2))) {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 1), 3);
								} else if (worldIn.isAirBlock(pos.up(2)) && fog2) {
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 4).withProperty(VARIANT, 0), 3);
									worldIn.setBlockState(pos.up(2), state.withProperty(STAGE, 5).withProperty(VARIANT, 0), 3);
								}
							} else if (downSt == 4 && upSt == 5) {
								if (downSt2 == 4) {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 1), 3);
								} else if (rand.nextInt(mutateChance) == 0 && worldIn.isAirBlock(pos.up(2)) && fog2) {
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 4).withProperty(VARIANT, 0), 3);
									worldIn.setBlockState(pos.up(2), state.withProperty(STAGE, 5).withProperty(VARIANT, 0), 3);
								} else {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 1), 3);
								}
							}
						} else if (var == 1) {
							if (downSt < 2 || upSt < 4 || (upSt == 4 && upVar >= 6) || (upSt == 5 && upVar == 8))
								worldIn.destroyBlock(pos, false);
							else if (upSt == 5 && upVar == 1) {
								worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 2), 3);
								worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 2), 3);
								if (downSt == 4 && downVar == 0)
									worldIn.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
							} else if (upSt == 5 && upVar == 3) {
								worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 2), 3);
								worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 4), 3);
							}
						} else if (var == 2) {
							if (downSt < 2 || upSt < 4 || (upSt == 4 && upVar >= 6) || (upSt == 5 && upVar == 8))
								worldIn.destroyBlock(pos, false);
							else if (upSt == 5 && upVar == 2) {
								boolean open = false;
								if (downSt == 3)
									open = true;
								else if (downSt == 4) {
									if (downVar == 1) {
										worldIn.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 2), 3);
										if (downSt2 == 4 && downVar2 == 0)
											worldIn.setBlockState(pos.down(2), state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
									} else if (downVar == 2) {
										if (downSt2 == 3)
											open = true;
										else if (downSt2 == 4 && downVar2 == 1)
											worldIn.setBlockState(pos.down(2), state.withProperty(STAGE, 4).withProperty(VARIANT, 2), 3);
										else if (downSt2 == 4 && downVar2 == 2)
											open = true;
									}
								}
								if (open) {
									worldIn.destroyBlock(pos.up(), false);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 4), 3);
								}
							} else if (upSt == 5 && upVar == 4 && rand.nextInt(aging) == 0) {
								worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 4), 3);
								worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 6), 3);
								if (downSt == 3)
									worldIn.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
							}
						} else if (var == 3) {
							if (downSt < 2 || upSt < 5 || (upSt == 5 && upVar == 8))
								worldIn.destroyBlock(pos, false);
							else if (upSt == 5) {
								worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
								worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 3), 3);
								if (downSt == 4 && downVar == 1)
									worldIn.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 2), 3);
							}
						} else if (var == 4) {
							if (downSt < 2 || upSt < 4 || (upSt == 4 && upVar >= 6) || (upSt == 5 && upVar == 8))
								worldIn.destroyBlock(pos, false);
							else if (upSt == 5) {
								if (downSt == 3 || (downSt == 4 && downVar == 1))
									worldIn.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 2), 3);
								else if (downSt == 4 && downVar == 2) {
									worldIn.setBlockState(pos, state.withProperty(STAGE, 4).withProperty(VARIANT, 5), 3);
									worldIn.setBlockState(pos.up(), state.withProperty(STAGE, 5).withProperty(VARIANT, 7), 3);
									worldIn.setBlockState(pos.down(), state.withProperty(STAGE, 4).withProperty(VARIANT, 4), 3);
									if (downSt2 == 3)
										worldIn.setBlockState(pos.down(2), state.withProperty(STAGE, 4).withProperty(VARIANT, 1), 3);
								}
							}
						} else if (var == 5) {
							if (downSt < 4 || upSt < 5 || (upSt == 5 && upVar == 8))
								worldIn.destroyBlock(pos, false);
							else if (upSt == 5) {
								worldIn.destroyBlock(pos.up(), false);
								dissemination(worldIn, pos.up(), rand);
								if (downSt2 == 2)
									worldIn.destroyBlock(pos, false);
								else if (downSt2 == 4 && (downVar2 == 1 || downVar2 == 2))
									worldIn.setBlockState(pos, state.withProperty(STAGE, 5).withProperty(VARIANT, 6), 3);
							}
						}
					} else if (var < 6 && (downSt < 2 || upSt < 4 || (upSt == 4 && upVar >= 6) || (upSt == 5 && upVar == 8)))
						worldIn.destroyBlock(pos, false);
				} else if (st == 5 && var == 8) {
					worldIn.setBlockState(pos, state.withProperty(STAGE, 0).withProperty(VARIANT, Math.abs(pos.getX() * pos.getY() * pos.getZ()) % 9), 3);
				}
			}
		}
	}

	private void dissemination(World worldIn, BlockPos pos, Random rand) {
		for (int i = 0; i < rand.nextInt(3) + 3; i++) {
			BlockPos pos1 = pos.add(rand.nextInt(3) - rand.nextInt(3), 0, rand.nextInt(3) - rand.nextInt(3));
			BlockPos pos2 = pos1.down();
			while (pos2.getY() > pos.getY() - 10 && (worldIn.isAirBlock(pos2) || worldIn.getBlockState(pos2).getMaterial().isReplaceable() &&
					!worldIn.getBlockState(pos1).getMaterial().isLiquid()) && pos2.getY() > 1) {
				pos1 = pos2;
				pos2 = pos1.down();
			}
			if (worldIn.isAirBlock(pos1) || worldIn.getBlockState(pos1).getMaterial().isReplaceable() && !worldIn.getBlockState(pos1).getMaterial().isLiquid()) {
				ItemStack stack = new ItemStack(MistItems.SPONGE_SPORE);
				EntityItem entity_item = new EntityItem(worldIn, pos1.getX() + 0.5D, pos1.getY() + 0.5D, pos1.getZ() + 0.5D, stack);
				entity_item.setDefaultPickupDelay();
				if (worldIn.getBlockState(pos2).getBlock() instanceof MistAcidSoil) {
					int spore = 0;
					BlockPos pos3;
					IBlockState state;
					for (int x = -8; x <= 8; x++) {
						for (int y = -2; y <= 2; y++) {
							for (int z = -8; z <= 8; z++) {
								pos3 = pos1.add(x, y, z);
								state = worldIn.getBlockState(pos3);
								if (state.getBlock() == this && (state.getValue(STAGE) <= 2 || state.getActualState(worldIn, pos3) == this.getDefaultState())) {
									spore++;
								}
							}
						}
					}
					if (spore < 8)
						worldIn.setBlockState(pos1, this.getDefaultState(), 3);
					else if (rand.nextInt(3) == 0)
						worldIn.spawnEntity(entity_item);
				} else if (rand.nextInt(3) == 0)
					worldIn.spawnEntity(entity_item);
			}
		}
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return isFullCube(state);
    }

	@Override
	public boolean isFullCube(IBlockState state) {
		int st = state.getValue(STAGE);
		int var = state.getValue(VARIANT);
		return st == 4 ? var >= 6 : false;
    }

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, this.getMetaFromState(getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 6))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 7))));
		list.add(new ItemStack(this, 1, this.getMetaFromState(getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 8))));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i;
		int st = state.getValue(STAGE);
		int var = state.getValue(VARIANT);
		if (st < 4)
			i = st + 1;
		else if (st == 4) {
			if (var < 6)
				i = var + 5;
			else i = var + 7;
		} else {
			if (var < 3)
				i = 11;
			else if (var < 8)
				i = 12;
			else i = 0;
		}
		return i;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		if (meta == 0) return this.getDefaultState();
		else if (meta < 5) return this.getDefaultState().withProperty(STAGE, meta - 1).withProperty(VARIANT, 0);
		else if (meta < 11) return this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, meta - 5);
		else if (meta == 11) return this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 0);
		else if (meta == 12) return this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 3);
		else return this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, meta - 7);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		int st = state.getValue(STAGE);
		int var = state.getValue(VARIANT);
		int downVar = worldIn.getBlockState(pos.down()).getBlock() == this
			? worldIn.getBlockState(pos.down()).getActualState(worldIn, pos.down()).getValue(VARIANT) : -1;
		return getActualState(state, pos, st, var, downVar);
	}

	private IBlockState getActualState(IBlockState state, BlockPos pos, int st, int var, int downVar) {
		if (st < 4) {
			if (downVar < 0)
				downVar = Math.abs(pos.getX() * pos.getY() * pos.getZ()) % 9;
			var = downVar;
		} else if (st == 5) {
			if (downVar >= 0) {
				if (var < 3 && downVar < 3)
					var = downVar;
				else if (var < 8) {
					if (downVar == 0)
						var = downVar;
					else if (downVar > 0 && downVar <= 5)
						var = downVar + 2;
				}
			}
		}
		return state.withProperty(STAGE, st).withProperty(VARIANT, var);
	}

	@Override
	public void onBlockClicked(World worldIn, BlockPos pos, EntityPlayer player) {
		IBlockState state = worldIn.getBlockState(pos);
		if (!worldIn.isRemote && state.getValue(STAGE) == 5 && state.getValue(VARIANT) == 8)
			worldIn.destroyBlock(pos, true);
	}

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		IBlockState upState = worldIn.getBlockState(pos.up());
		if (!worldIn.isRemote && upState.getBlock() == this) {
			int upSt = upState.getValue(STAGE);
			int upVar = getActualState(upState, pos.up(), upSt, upState.getValue(VARIANT), state.getActualState(worldIn, pos).getValue(VARIANT)).getValue(VARIANT);
			if (upSt == 4 ? upVar < 6 : true) {
				worldIn.setBlockState(pos.up(), state.withProperty(STAGE, upSt).withProperty(VARIANT, upVar), 1);
				worldIn.destroyBlock(pos.up(), true);
			}
		}
	}

	@Override
	public void getDrops(NonNullList ret, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		int st = state.getValue(STAGE);
		int var = state.getActualState(world, pos).getValue(VARIANT);
		int count = quantityDropped(st, var, rand);
		for (int i = 0; i < count; i++) {
			Item item = this.getItemDropped(st, var, rand);
			if (item != null) {
				ret.add(new ItemStack(item, 1, this.damageDropped(st, var, state)));
			}
		}
	}

	private Item getItemDropped(int st, int var, Random random) {
		if (st == 0 || st == 2)
			return null;
		else if (st == 1 || st == 3 || (st >= 4 && var == 0))
			return MistItems.SPONGE_MEAT;
		else if (st == 4) {
			if (var <= 3)
				return MistItems.SPONGE_FIBRE;
			else return new ItemStack(this).getItem();
		} else {
			if (var <= 5)
				return MistItems.SPONGE_FIBRE;
			else if (var == 6)
				return random.nextBoolean() ? MistItems.SPONGE_FIBRE : MistItems.SPONGE_SPORE;
			else if (var == 7)
				return MistItems.SPONGE_SPORE;
			else return MistItems.SPONGE_SPORE;
		}
	}

	private int quantityDropped(int st, int var, Random random) {
		if (st == 0 || st == 2)
			return 0;
		else if (st == 1)
			return random.nextInt(2);
		else if (st == 3)
			return random.nextInt(2) + 1;
		else if (st >= 4 && var == 0)
			return random.nextInt(3) + 1;
		else if (st == 4) {
			if (var <= 3)
				return random.nextInt(2);
			else return 1;
		} else {
			if (var <= 5)
				return random.nextInt(2);
			else if (var == 6)
				return random.nextInt(2);
			else if (var == 7)
				return random.nextInt(3) + 3;
			else return 1;
		}
	}

	private int damageDropped(int st, int var, IBlockState state) {
		if (st == 4) {
			if (var <= 3)
				return 0;
			else if (var <= 5)
				return 14;
			else return getMetaFromState(state);
		} else return 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { STAGE, VARIANT });
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	@Override
	public EnumPushReaction getMobilityFlag(IBlockState state) {
		if (getMetaFromState(state) >= 13) return EnumPushReaction.NORMAL;
		return EnumPushReaction.DESTROY;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return Mist.MIST_DOWN_PLANT;
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
		return this.getDefaultState();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		int i = getMetaFromState(state);
		if (i == 0 || i >= 13)
			return this.getDrops(world, pos, state, 0).get(0);
		else return ItemStack.EMPTY;
	}

	@Override
	public boolean isMistAdsorbent(World world, BlockPos pos, IBlockState state) {
		int st = state.getValue(STAGE);
		int var = state.getActualState(world, pos).getValue(VARIANT);
		return st == 3 || (st > 3 && (var < 2 || var == 3)) || (st == 5 && var == 5);
	}

	public void generateSponge(World world, BlockPos pos, Random rand) {
		if (!world.isRemote) {
			for (int i = 0; i < rand.nextInt(4) + 5; i++) {
				BlockPos pos1 = world.getHeight(pos.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4)));
				while ((world.isAirBlock(pos1) || world.getBlockState(pos1).getMaterial().isReplaceable()) && Math.abs(pos1.getY() - pos.getY()) < 5) {
					pos1 = pos1.down();
				}
				generateSingleSponge(world, pos1, rand);
			}
		}
	}

	public void generateSingleSponge(World world, BlockPos pos, Random rand) {
		if (!world.isRemote) {
			if (world.getBlockState(pos).getBlock() instanceof MistAcidSoil) {
				boolean check = false;
				Material mat;
				for (int i = 1; i < 8; ++i) {
					mat = world.getBlockState(pos.up(i)).getMaterial();
					if (!mat.isReplaceable() || mat.isLiquid()) {
						check = true;
						break;
					}
				}
				if (!check) {
					int age = rand.nextInt(25);
					if (age == 0) world.setBlockState(pos.up(), this.getDefaultState());
					else if (age < 3) world.setBlockState(pos.up(), this.getDefaultState().withProperty(STAGE, age - 1).withProperty(VARIANT, 0), Mist.FLAG);
					else {
						world.setBlockState(pos.up(), this.getDefaultState().withProperty(STAGE, 2).withProperty(VARIANT, 0), Mist.FLAG);
						if (age == 3) world.setBlockState(pos.up(2), this.getDefaultState().withProperty(STAGE, 1).withProperty(VARIANT, 0), Mist.FLAG);
						else if (age < 17) {
							int up = rand.nextInt(3) == 0 ? rand.nextInt(3) : 1;
							int down = rand.nextInt(4) == 0 ? 1 : 0;
							world.setBlockState(pos.up(2), this.getDefaultState().withProperty(STAGE, 3).withProperty(VARIANT, 0), Mist.FLAG);
							if (down > 0) world.setBlockState(pos.up(3), this.getDefaultState().withProperty(STAGE, 3).withProperty(VARIANT, 0), Mist.FLAG);
							if (age < 6) world.setBlockState(pos.up(3 + down), this.getDefaultState().withProperty(STAGE, 1).withProperty(VARIANT, 0), Mist.FLAG);
							else {
								if (age < 9) {
									world.setBlockState(pos.up(3 + down), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 0), Mist.FLAG);
									for (int i = 0; i < up; ++i) world.setBlockState(pos.up(4 + down + i), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 0), Mist.FLAG);
									world.setBlockState(pos.up(4 + down + up), this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 0), Mist.FLAG);
								} else if (age < 13) {
									world.setBlockState(pos.up(3 + down), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, MathHelper.clamp(age - 8 - up, 0, 2)), Mist.FLAG);
									for (int i = 0; i < up; ++i) world.setBlockState(pos.up(4 + down + i), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, MathHelper.clamp(age + i - 7 - up, 0, 2)), Mist.FLAG);
									world.setBlockState(pos.up(4 + down + up), this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 0), Mist.FLAG);
								} else {
									world.setBlockState(pos.up(3 + down), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 2), Mist.FLAG);
									for (int i = 0; i < up; ++i) world.setBlockState(pos.up(4 + down + i), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 2), Mist.FLAG);
									world.setBlockState(pos.up(4 + down + up), this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 4), Mist.FLAG);
								}
							}
						} else if (age < 22) {
							boolean crown = rand.nextBoolean();
							int med = rand.nextInt(3) == 0 ? 1 : 0;
							int down = rand.nextInt(3) == 0 ? 0 : rand.nextInt(4) == 0 && med == 0 ? 2 : 1;
							int full = med == 0 && (down > 0 || !crown) ? (rand.nextInt(8) == 0 && down > 0 && !crown ? 2 : 1) : 0;
							for (int i = 0; i < down; ++i) world.setBlockState(pos.up(2 + i), this.getDefaultState().withProperty(STAGE, 3).withProperty(VARIANT, 0), Mist.FLAG);
							if (med == 0) {
								for (int i = 0; i < full; ++i) world.setBlockState(pos.up(2 + down + i), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 2), Mist.FLAG);
							} else world.setBlockState(pos.up(2 + down), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 1), Mist.FLAG);
							world.setBlockState(pos.up(2 + down + full + med), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 4), Mist.FLAG);
							if (crown) {
								world.setBlockState(pos.up(3 + down + full + med), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 5), Mist.FLAG);
								world.setBlockState(pos.up(4 + down + full + med), this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 7), Mist.FLAG);
							} else {
								world.setBlockState(pos.up(3 + down + full + med), this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 6), Mist.FLAG);
							}
						} else if (age < 24) {
							world.setBlockState(pos.up(2), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 4), Mist.FLAG);
							if (age == 22) {
								world.setBlockState(pos.up(3), this.getDefaultState().withProperty(STAGE, 4).withProperty(VARIANT, 5), Mist.FLAG);
								world.setBlockState(pos.up(4), this.getDefaultState().withProperty(STAGE, 5).withProperty(VARIANT, 7), Mist.FLAG);
							}
						}
					}
				}
			}
		}
	}
}