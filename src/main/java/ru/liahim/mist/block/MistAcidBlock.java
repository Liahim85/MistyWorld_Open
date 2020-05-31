package ru.liahim.mist.block;

import java.util.Random;

import javax.annotation.Nonnull;

import ru.liahim.mist.api.advancement.FogDamagePredicate.FogDamageType;
import ru.liahim.mist.api.block.IDownPlant;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.util.FacingHelper;
import ru.liahim.mist.world.FogDamage;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**@author Liahim*/
public class MistAcidBlock extends BlockFluidClassic { //TODO �������� ������� ����� (�����)

	public MistAcidBlock(Fluid fluid) {
		super(fluid, Material.WATER);
		this.setLightOpacity(3);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (entity.isDead) return;
		else if (entity instanceof EntityLivingBase) {
			if (entity instanceof EntityBlaze) return;
			if (entity instanceof EntityMagmaCube) return;
			if (entity instanceof EntityPlayer && (((EntityPlayer)entity).isCreative() || ((EntityPlayer)entity).getHealth() == 0)) return;
			if (!world.isRemote && entity.ticksExisted % 10 == 0) {
				float concentration = MistWorld.isPosInFog(world, pos.getY() + 2) ? FogDamage.getConcentration(world, pos) : 0;
				concentration = 0.5F + concentration / 2;
				float damage = FogDamage.getAcidDamage(concentration);
				float pollution = FogDamage.getAcidPollution(concentration);
				if (entity instanceof EntityPlayerMP) {
					float[] suitFactor = FogDamage.getPollutionProtection((EntityPlayer)entity);
					float pollutionFactor = 1;
					if (suitFactor[0] > 0) {
						pollutionFactor -= Math.pow(FogDamage.getFinalEfficiency(suitFactor[1], concentration), 2);
					}
					damage *= pollutionFactor;
					pollution *= pollutionFactor;
					
					if (damage > 0.2F) {
						entity.attackEntityFrom(MistWorld.DISSOLUTION, damage);
						IMistCapaHandler.getHandler((EntityPlayer)entity).addPollution((int) Math.ceil(pollution));
						ModAdvancements.FOG_DAMAGE.trigger((EntityPlayerMP)entity, entity.world, entity.getPosition(), FogDamageType.BY_ACID, -1.0F, null, suitFactor[0] == 4, null);
					}
				} else entity.attackEntityFrom(MistWorld.DISSOLUTION, damage);
			}
		} else if (entity instanceof EntityBoat && !entity.isDead) {
			if ((entity.ticksExisted & 1) == 0) world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + world.rand.nextFloat(), pos.getY() + 1.0D, pos.getZ() + world.rand.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);
			if (!world.isRemote && entity.ticksExisted > 200) {
				entity.setDead();
				if (world.getGameRules().getBoolean("doEntityDrops")) {
					for (int i = 0; i < 3; ++i) {
						entity.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.PLANKS), 1, ((EntityBoat)entity).getBoatType().getMetadata()), 0.0F);
					}
					for (int j = 0; j < 2; ++j) {
						entity.dropItemWithOffset(Items.STICK, 1, 0.0F);
					}
				}
			}
		} else if (entity instanceof EntityFishHook && !entity.isDead && entity.ticksExisted > 30) {
			if (!world.isRemote) {
				entity.setDead();
			}
		} else if (entity instanceof EntityItem && !entity.isDead && entity.ticksExisted > 60) {
			if (!world.isRemote) entity.setDead();
		}
	}

	/*@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (!world.isRemote && state.getValue(LEVEL) == 0 && !MistWorld.isPosInFog(world, pos.up(4))) {
			IBlockState neighbor = world.getBlockState(fromPos);
			if (neighbor.getBlock() == this && neighbor.getValue(LEVEL) == 1) {
				world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, 1));
			}
		}
		super.neighborChanged(state, world, pos, block, fromPos);
	}*/

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		super.updateTick(world, pos, state, rand);
		if (!world.isRemote) {
			if (world.getBlockState(pos.up()).getBlock() instanceof BlockBush && !(world.getBlockState(pos.up()).getBlock() instanceof IDownPlant)) {
				world.destroyBlock(pos.up(), true);
			}
			if (rand.nextBoolean()) {
				BlockPos pos1 = pos.offset(FacingHelper.NOTUP[rand.nextInt(5)]);
				if (isSoluble(world.getBlockState(pos1).getMaterial())) {
					world.destroyBlock(pos1, false);
				} else if (pos1 == pos.down()) {
					if (world.getBlockState(pos.down()).getBlock() == Blocks.GRASS || world.getBlockState(pos.down()).getBlock() == Blocks.MYCELIUM) {
						world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState());
					}
				}
			}
			boolean isDown = MistWorld.isPosInFog(world, pos.up(4));
			int level = state.getValue(LEVEL);
			IBlockState stateW;
			for (EnumFacing face : EnumFacing.VALUES) {
				stateW = world.getBlockState(pos.offset(face));
				if (stateW.getBlock() == Blocks.WATER || stateW.getBlock() == Blocks.FLOWING_WATER) {
					int levelW = stateW.getValue(BlockLiquid.LEVEL);
					if (isDown) {
						if (face != EnumFacing.UP && (face == EnumFacing.DOWN || levelW > level || (levelW == 0 && level < 2))) {
							world.setBlockState(pos.offset(face), this.getDefaultState().withProperty(LEVEL, levelW));
						}
					} else {
						if (face != EnumFacing.DOWN && (face == EnumFacing.UP || levelW < level || (level == 0 && levelW < 2))) {
							world.setBlockState(pos, Blocks.WATER.getDefaultState().withProperty(BlockLiquid.LEVEL, level));
							break;
						}
					}
				}
			}
			/*if (!isDown && level == 0) {
				world.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, 1));
			}*/
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		int i = rand.nextInt(5);
		if (i == 0) {
			if (isSoluble(world.getBlockState(pos.offset(EnumFacing.EAST)).getMaterial()))
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 1.0F, pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);
		} else if (i == 1) {
			if (isSoluble(world.getBlockState(pos.offset(EnumFacing.WEST)).getMaterial()))
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);
		} else if (i == 2) {
			if (isSoluble(world.getBlockState(pos.offset(EnumFacing.NORTH)).getMaterial()))
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ(), 0.0D, 0.0D, 0.0D, new int[0]);
		} else if (i == 3) {
			if (isSoluble(world.getBlockState(pos.offset(EnumFacing.SOUTH)).getMaterial()))
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + 1.0F, 0.0D, 0.0D, 0.0D, new int[0]);
		} else if (i == 4) {
			if (isSoluble(world.getBlockState(pos.offset(EnumFacing.SOUTH)).getMaterial()))
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D, new int[0]);
		}
	}

	private boolean isSoluble(Material mat) {
		if (mat == Material.WATER) return false;
		if (mat == Material.AIR) return false;
		if (mat == Material.GROUND) return false;
		if (mat == Material.GRASS) return false;
		if (mat == Material.ROCK) return false;
		if (mat == Material.SAND) return false;
		if (mat == Material.CLAY) return false;
		if (mat == Material.IRON) return false;
		if (mat == Material.GLASS) return false;
		if (mat == Material.ANVIL) return false;
		if (mat == Material.BARRIER) return false;
		return true;
	}

	@Override
	public float getFluidHeightForRender(IBlockAccess world, BlockPos pos, @Nonnull IBlockState up) {
		IBlockState here = world.getBlockState(pos);
		if (here.getBlock() == this) {
			if (up.getMaterial().isLiquid()) return 1;
			if (getMetaFromState(here) == getMaxRenderHeightMeta()) return 8F/9;
		}
		if (here.getBlock() instanceof BlockLiquid) {
			if (here.getValue(BlockLiquid.LEVEL) == 8) return 1;
			else if (up.getMaterial().isLiquid()) return 1;
			return Math.min(1 - BlockLiquid.getLiquidHeightPercent(here.getValue(BlockLiquid.LEVEL)), 8F/9);
		}
		return !here.getMaterial().isSolid() && up.getMaterial().isLiquid() ? 1 : here.getBlock() == Blocks.AIR ? 0.0F : this.getQuantaPercentage(world, pos) * 8F/9;
	}

	@Override
	public float getFluidHeightAverage(float... flow) {
		float total = 0;
		int count = 0;
		float end = 0;
		for (int i = 0; i < flow.length; i++) {
			if (flow[i] == 1) return 1;
			if (flow[i] >= 8F/9) {
				total += flow[i] * 10;
				count += 10;
			}
			if (flow[i] >= 0) {
				total += flow[i];
				count++;
			}
		}
		if (end == 0) end = total / count;
		return end - 0.001F;
	}

	@Override
	public boolean displaceIfPossible(World world, BlockPos pos) {
		if (world.isAirBlock(pos)) return true;
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block == this || block instanceof BlockLiquid) return false;
		if (displacements.containsKey(block)) {
			if (displacements.get(block)) {
				if (state.getBlock() != Blocks.SNOW_LAYER)
					block.dropBlockAsItem(world, pos, state, 0);
				return true;
			}
			return false;
		}
		Material material = state.getMaterial();
		if (material.blocksMovement() || material == Material.PORTAL) {
			return false;
		}
		int density = getDensity(world, pos);
		if (density == Integer.MAX_VALUE) {
			block.dropBlockAsItem(world, pos, state, 0);
			return true;
		}
		return this.density > density;
	}

	@Override
	public int getQuantaValue(IBlockAccess world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() == Blocks.AIR) return 0;
		if (state.getBlock() instanceof BlockLiquid) return quantaPerBlock - state.getValue(BlockLiquid.LEVEL);
		if (state.getBlock() != this) return -1;
		return quantaPerBlock - state.getValue(LEVEL);
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return MapColor.LIME_STAINED_HARDENED_CLAY;
	}
}