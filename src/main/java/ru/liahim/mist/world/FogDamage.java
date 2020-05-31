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
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.advancement.FogDamagePredicate.FogDamageType;
import ru.liahim.mist.api.block.IMistAdsorbent;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.api.item.ISuit;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModAdvancements;

public class FogDamage {

	private static final UUID[] healthUUIDs = new UUID[] {
		UUID.fromString("4e65bedc-27d8-44da-8d8e-4493874517ab"),
		UUID.fromString("0051b697-c495-4c85-bac0-2089cfd967fd"),
		UUID.fromString("55801a45-15e9-4445-9658-47f42b4e30b0"),
		UUID.fromString("9e36fc8c-27ec-40cd-a8d4-5f227c9f2082"),
		UUID.fromString("9640f779-eec2-4b8d-9bb5-8ef5690c8cf8"),
		UUID.fromString("2328268e-dbb9-4a52-bfbc-975f81cd32b2"),
		UUID.fromString("b2545134-6801-4e90-81c6-c52e8d231655"),
		UUID.fromString("731e3c7e-c729-465b-b6a9-dd1f27746bc3"),
		UUID.fromString("149ecd6a-974a-44e5-812f-b6f981371d66"),
		UUID.fromString("65c9a779-fddc-4c55-91ba-6fba62768268"),
		UUID.fromString("f30c6bc4-9617-42f5-bdf9-ade683591126"),
		UUID.fromString("2a1367b9-b9be-464f-8382-62e14cb3f147"),
		UUID.fromString("bf90bfae-4619-44b9-a440-dbfeec5bc754"),
		UUID.fromString("59d63b61-c9bc-4a7d-8e53-591d5da1e6f6"),
		UUID.fromString("2b43567d-4f84-4774-aef8-f69457f43ab1"),
		UUID.fromString("6616b020-606e-46ba-817e-a32a5e33ef50")
	};
	private static final AttributeModifier[] toxicModifiers = new AttributeModifier[] {
		new AttributeModifier(healthUUIDs[0], "toxicDamage", -1, 0),
		new AttributeModifier(healthUUIDs[1], "toxicDamage", -2, 0),
		new AttributeModifier(healthUUIDs[2], "toxicDamage", -3, 0),
		new AttributeModifier(healthUUIDs[3], "toxicDamage", -4, 0),
		new AttributeModifier(healthUUIDs[4], "toxicDamage", -5, 0),
		new AttributeModifier(healthUUIDs[5], "toxicDamage", -6, 0),
		new AttributeModifier(healthUUIDs[6], "toxicDamage", -7, 0),
		new AttributeModifier(healthUUIDs[7], "toxicDamage", -8, 0),
		new AttributeModifier(healthUUIDs[8], "toxicDamage", -9, 0),
		new AttributeModifier(healthUUIDs[9], "toxicDamage", -10, 0),
		new AttributeModifier(healthUUIDs[10], "toxicDamage", -11, 0),
		new AttributeModifier(healthUUIDs[11], "toxicDamage", -12, 0),
		new AttributeModifier(healthUUIDs[12], "toxicDamage", -13, 0),
		new AttributeModifier(healthUUIDs[13], "toxicDamage", -14, 0),
		new AttributeModifier(healthUUIDs[14], "toxicDamage", -15, 0),
		new AttributeModifier(healthUUIDs[15], "toxicDamage", -16, 0)
	};
	private static final boolean ambient = true;
	private static final int pollutiomDamageBorder = (10000 - 2000)/100;

	public static void calculateFogDamage(EntityLivingBase entity) {
		if (!entity.world.isRemote && !entity.isDead && entity.world.provider.getDimension() == Mist.getID()) {
			if (!isDamageTick(entity.ticksExisted) || entity.ticksExisted == 0) return;
			boolean isPlayer = entity instanceof EntityPlayer;
			EntityPlayer player = isPlayer ? (EntityPlayer)entity : null;
			if (!isPlayer) {
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
			if (depth > 0) {
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
		for (int i = 0; i < toxicModifiers.length; ++i) {
			if (i == t) {
				if(!boost.hasModifier(toxicModifiers[i])) boost.applyModifier(toxicModifiers[i]);
			} else if (boost.hasModifier(toxicModifiers[i])) boost.removeModifier(toxicModifiers[i]);
		}
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
		double dist = pos.distanceSq(MistWorld.getCenterPos(world, pos, false)); boolean main = false;
		if (dist > 160000) { dist = pos.distanceSq(MistWorld.getCenterPos(world, pos, true)); main = true; }
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