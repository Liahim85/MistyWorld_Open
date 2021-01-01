package ru.liahim.mist.world;

import java.util.UUID;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.advancement.FogDamagePredicate.FogDamageType;
import ru.liahim.mist.api.block.IMistAdsorbent;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.event.PollutionEvent;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.api.item.ISuit;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModAdvancements;

public class FogDamage {
	private static final UUID healthUUID = UUID.fromString("4e65bedc-27d8-44da-8d8e-4493874517ab");

	private static final boolean ambient = true;
	private static final int pollutiomDamageBorder = (10000 - 2000)/100;

	public static void calculateFogDamage(EntityLivingBase entity) {
		if (!entity.world.isRemote && !entity.isDead) {
			boolean mist = entity.world.provider.getDimension() == Mist.getID();
			if (!isDamageTick(entity.ticksExisted) || entity.ticksExisted == 0) return;
			boolean isPlayer = entity instanceof EntityPlayer;
			EntityPlayer player = isPlayer ? (EntityPlayer)entity : null;
			if (!isPlayer) {
				if (!mist) return;
				if (entity instanceof EntityZombie) return;
				if (entity instanceof EntitySkeleton) return;
				if (entity instanceof EntitySkeletonHorse) return;
				if (entity instanceof EntityZombieHorse) return;
				if (entity instanceof EntityGolem) return;
				if (entity instanceof EntityBlaze) return;
				if (entity instanceof EntityMagmaCube) return;
			} else if ((player.isCreative() || player.isSpectator()) || player.getHealth() == 0) return;
			IMistCapaHandler mistCapa = isPlayer ? IMistCapaHandler.getHandler(player) : null;
			float eyeHeight = entity.getEyeHeight();
			PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
			pos.setPos(Math.floor(entity.posX), Math.floor(entity.posY + eyeHeight), Math.floor(entity.posZ));
			float depth = 0;
			if (entity.posY + eyeHeight <= MistWorld.getFogMaxHight() + 4.0F) {
				if (entity.posY + eyeHeight < MistWorld.getFogMinHight()) depth = 4;
				else depth = (float)Math.min(4, MistWorld.getFogHight(entity.world, 0) + 4.0F - entity.posY - eyeHeight);
			}
			if (mist && depth > 0) {
				float concentration = getConcentration(entity.world, pos);
				boolean rain = depth >= 4 && isRainTick(entity.ticksExisted) && entity.world.isRaining() && entity.world.canBlockSeeSky(pos);
				boolean adsorbent = isAdsorbentNear(entity.world, pos);
				boolean adsorbentTick = adsorbent && isAdsorbentTick(entity.ticksExisted);
				float adsorbentFactor = adsorbent ? 1 - getFinalEfficiency(adsorbentTick ? 90 : 30, concentration) : 1;

				float rainDamage = rain ? getRainDamage(concentration)*depth/4 : 0;
				float fogDamage = getFogDamage(concentration)*depth/4;
				float toxic = getFogToxic(concentration)*depth/4;
				boolean pollutionTick = isPollutionTick(entity.ticksExisted);

				boolean isMask = false;
				boolean pollutionProtection = false;
				float pollutionFactor = 1;
				boolean suit = false;
				if (isPlayer) {
					ItemStack mask = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
					if (!IMask.isMask(mask) && mistCapa != null) mask = mistCapa.getMask();
					if (IMask.isMask(mask) && !IMask.getFilter(mask).isEmpty()) {
						isMask = true;
						float filteringDepth = IMask.damageFilter(mask, Math.round(getFilterDamage(concentration) * adsorbentFactor), mistCapa);
						filteringDepth = 1 - getFinalEfficiency(filteringDepth, concentration);
						fogDamage *= filteringDepth;
						toxic *= filteringDepth;
					}
					if (rainDamage > 0 || pollutionTick) {
						float[] suitFactor = getPollutionProtection(player);
						if (suitFactor[0] > 0) {
							suit = suitFactor[0] == 4;
							pollutionProtection = true;
							pollutionFactor -= getFinalEfficiency(suitFactor[1], concentration);
							if (isPollutionProtectionTick(entity.ticksExisted, (int) suitFactor[0], true)) {
								rainDamage *= pollutionFactor;
							}
							rain = rainDamage >= 0.5F;
							if (!isPollutionProtectionTick(entity.ticksExisted, (int) suitFactor[0], false)) {
								pollutionFactor = 1;
							}
						}
					}
				}

				float pollution = pollutionTick ? getPollution(concentration, rain)*depth/4 : 0;

				fogDamage *= adsorbentFactor;
				toxic *= adsorbentFactor;
				if (!rain) {
					pollution *= adsorbentFactor;
					pollution *= pollutionFactor;
				}

				PollutionEvent event = new PollutionEvent(entity, pollution, toxic, fogDamage, rainDamage);
				MinecraftForge.EVENT_BUS.post(event);
				pollution = event.getPollution();
				toxic = event.getToxic();
				fogDamage = event.getFogDamage();
				rainDamage = event.getRainDamage();

				boolean damage = false;
				if (fogDamage > 0) {
					if (entity instanceof EntityPlayerMP) ModAdvancements.FOG_DAMAGE.trigger((EntityPlayerMP)entity, entity.world, entity.getPosition(), FogDamageType.BY_FOG, concentration, isMask, suit, adsorbent);
					if (!isMask && !adsorbentTick) {
						entity.attackEntityFrom(MistWorld.IN_FOG, fogDamage);
						damage = true;
					} else if (fogDamage >= 0.5) {
						entity.attackEntityFrom(MistWorld.IN_FOG, fogDamage);
						damage = true;
					}
				}
				if (rainDamage > 0) {
					if (entity instanceof EntityPlayerMP) ModAdvancements.FOG_DAMAGE.trigger((EntityPlayerMP)entity, entity.world, entity.getPosition(), FogDamageType.BY_RAIN, concentration, isMask, suit, adsorbent);
					if (!pollutionProtection) {
						entity.attackEntityFrom(MistWorld.DISSOLUTION, rainDamage);
						damage = true;
					} else if (rainDamage >= 0.5) {
						entity.attackEntityFrom(MistWorld.DISSOLUTION, rainDamage);
						damage = true;
					}
				}
				if (damage) entity.setRevengeTarget(entity);
				if (mistCapa != null) {
					if (toxic > 0) mistCapa.addToxic(Math.round(toxic));
					if (pollution > 0) mistCapa.addPollution(Math.round(pollution));
				}
			} else if (mistCapa != null) {
				int clear = !isRainTick(entity.ticksExisted) ? 0 : entity.world.isRainingAt(pos) ? -10 : entity.ticksExisted % 800 == 0 ? -1 : 0;
				if (!(entity.getRidingEntity() instanceof EntityBoat)) {
					pos.setPos(entity.posX, entity.posY, entity.posZ);
					AxisAlignedBB bb = entity.getEntityBoundingBox().grow(-0.1D, -0.4D, -0.1D);
					int minX = MathHelper.floor(bb.minX);
					int maxX = MathHelper.ceil(bb.maxX);
					int minY = MathHelper.floor(bb.minY);
					int maxY = MathHelper.ceil(bb.maxY);
					int minZ = MathHelper.floor(bb.minZ);
					int maxZ = MathHelper.ceil(bb.maxZ);
					lab1:
					for (int x = minX; x < maxX; ++x) {
						for (int y = minY; y < maxY; ++y) {
							for (int z = minZ; z < maxZ; ++z) {
								IBlockState state = entity.world.getBlockState(pos.setPos(x, y, z));
								if (state.getMaterial() == Material.WATER && state.getBlock() != MistBlocks.ACID_BLOCK) {
									clear = -20;
									break lab1;
								}
							}
						}
					}
				}
				mistCapa.addPollution(clear);
				if (entity.ticksExisted % 1000 == 0) mistCapa.addToxic(-1);
			}
			if (isPlayer) makeEffects(player, mistCapa.getToxic(), mistCapa.getPollution());
			pos.release();
		}
	}

	public static void makeEffects(EntityPlayer player, int toxic, int pollution) {
		if (player instanceof EntityPlayerMP) ModAdvancements.FOG_EFFECT.trigger((EntityPlayerMP) player, Float.valueOf(toxic), Float.valueOf(pollution));
		IAttributeInstance boost = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
		int t = toxic - 2000;
		if (t < 0) t = 0;
		t = t/500 - 1;
		int healthDamage = -(t + 1);
		boost.removeModifier(healthUUID);
		boost.applyModifier(new AttributeModifier(healthUUID, "toxicDamage", healthDamage, 0));
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
		if (player.ticksExisted % 80 == 0) {
			if (toxic > 7500) {
				int a = (toxic - 7500)/500;
				player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, a, ambient, false));
				player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, a, ambient, false));
				player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, a, ambient, false));
				if (toxic > 9000) {
					if (player.getRNG().nextInt(10) == 0) player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, player.getRNG().nextInt(200) + 100, 0, ambient, false));
					if (toxic > 9900) player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 100, 0, ambient, false));
				}
			}
		}
		int p = Math.max((10000 - pollution)/100, 1);
		if (p < pollutiomDamageBorder && player.ticksExisted % (p * 20) == 0 && player.getHealth() > 1) {
			player.attackEntityFrom(MistWorld.DISSOLUTION, 1);
		}
	}

	public static float getFogDamage(float concentration) {
		return 1 + 4F * concentration;
	}

	public static float getFogToxic(float concentration) {
		return 40 + 60F * concentration;
	}

	public static float getFilterDamage(float concentration) {
		return 1 + 20F * concentration;
	}

	public static float getRainDamage(float concentration) {
		return 1 + 2F * concentration;
	}

	public static float getPollution(float concentration, boolean rain) {
		return (1 + 20F * concentration) * (rain ? 100 : 1);
	}

	public static float getAcidDamage(float concentration) {
		if (concentration < 0.5F) return 2;
		return 4 * concentration;
	}

	public static float getAcidPollution(float concentration) {
		if (concentration < 0.5F) return 128;
		return 256 * concentration;
	}

	/** Returns the relative fog concentration in the "centers" (0 - far from center, 0.5 - normal center, 1 - main center). */
	public static float getConcentration(World world, BlockPos pos) {
		return getConcentration(world, pos, false);
	}

	/** Returns the relative fog concentration in the "centers" (0 - far from center, 0.5 - normal center, 1 - main center). */
	public static float getConcentration(World world, BlockPos pos, boolean client) {
		double dist = client ? pos.distanceSq(MistWorld.getCenterPos(pos, false))
							 : pos.distanceSq(MistWorld.getCenterPos(world, pos, false));
		boolean main = false;
		if (dist > 160000) {
			dist = client ? pos.distanceSq(MistWorld.getCenterPos(pos, true))
						: pos.distanceSq(MistWorld.getCenterPos(world, pos, true));
			main = true;
		}
		if (dist > 640000) dist = -1;
		if (dist > 0) dist = Math.sqrt(dist);				
		return dist < 0 ? 0 : main ? (float)Math.min(400, 800 - dist)/400 : (float)Math.min(200, 400 - dist)/400;
	}

	/**
	 * Returns the final effectiveness of the respirator/suit/adsorbent (from 0 to 1) depending on the its quality and proximity to the center. 
	 * @param quality - filtering depth/pollution protection/etc. in percent.
	 * @param concentration - relative fog concentration in the "centers" (0 - far from center, 0.5 - normal center, 1 - main center).
	 */
	public static float getFinalEfficiency(float quality, float concentration) {
		concentration = MathHelper.clamp(concentration, 0, 1);
		concentration = (float) Math.pow(concentration, 2) / 2 + 0.03F;
		quality = MathHelper.clamp(quality, 0, 100);
		return (float) Math.pow(Math.pow(MathHelper.cos(concentration), Math.pow(100 - quality, 2)) - 1, 3) + 1;
	}

	/** Returns the pollution protection of current suit [1] and count of suit elements [0]. */
	public static float[] getPollutionProtection(EntityPlayer player) {
		ItemStack armor;
		float protection = 0;
		int count = 0;
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
				armor = player.getItemStackFromSlot(slot);
				if (armor.getItem() instanceof ISuit) {
					protection += ((ISuit)armor.getItem()).getPollutionProtection();
					count++;
				} else {
					NBTTagCompound tag = armor.getSubCompound(MistTags.nbtInnerSuitTag);
					if (tag != null) {
						armor = new ItemStack(tag);
						if (armor.getItem() instanceof ISuit) protection += ((ISuit)armor.getItem()).getPollutionProtection();
						count++;
					}
				}
			}
		}
		if (count != 0) protection /= count;
		return new float[] { count, protection };
	}

	/** Determines if there is an adsorbent block in a cube of 3x3x3 blocks around the a specified position. */
	public static boolean isAdsorbentNear(World world, BlockPos pos) {
		IBlockState state;
		PooledMutableBlockPos pmPos = PooledMutableBlockPos.retain();
		for (int x = -1; x < 2; ++x) {
			for (int y = -1; y < 2; ++y) {
				for (int z = -1; z < 2; ++z) {
					pmPos.setPos(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
					state = world.getBlockState(pmPos);
					if (state.getBlock() instanceof IMistAdsorbent && ((IMistAdsorbent)state.getBlock()).isMistAdsorbent(world, pmPos, state)) {
						return true;
					}
				}
			}
		}
		pmPos.release();
		return false;
	}

	public static boolean isDamageTick(int ticksExisted) {
		return ticksExisted % 20 == 0;
	}

	public static boolean isRainTick(int ticksExisted) {
		return ticksExisted % 80 == 0;
	}

	public static boolean isPollutionTick(int ticksExisted) {
		return ticksExisted % 40 == 0;
	}

	public static boolean isPollutionProtectionTick(int ticksExisted, int count, boolean rain) {
		if (count == 0) return false;
		if (count > 4) count = 4;
		return rain ? ticksExisted % (400 - 80 * count) == 0 : ticksExisted % (200 - 40 * count) == 0;
	}

	public static boolean isAdsorbentTick(int ticksExisted) {
		return (ticksExisted + 40) % 80 != 0;
	}
}