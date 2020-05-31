package ru.liahim.mist.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;

public class EntitySnowFlea extends EntityMobMist {

	public EntitySnowFlea(World world) {
		super(world);
		this.setSize(0.75F, 0.75F);
		this.stepHeight = 2;
		this.jumpMovementFactor = 0.3F;
        this.moveHelper = new EntitySnowFlea.FleaMoveHelper(this);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(3, new EntitySnowFlea.AIAttack(this));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 0.8D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 5, true, false, (Predicate)null));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_SNOW_FLEA_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_SNOW_FLEA_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_SNOW_FLEA_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(MistSounds.ENTITY_SNOW_FLEA_STEP, 0.15F, 1.0F);
	}

	protected SoundEvent getJumpSound() {
		return MistSounds.ENTITY_SNOW_FLEA_FLAY;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.SNOW_FLEA_LOOT;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (super.attackEntityAsMob(entity) && this.rand.nextInt(8) == 0) {
			if (entity instanceof EntityLivingBase) {
				int i = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL) i = 5;
				else if (this.world.getDifficulty() == EnumDifficulty.HARD) i = 10;
				if (i > 0) ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 20, 0));
			}
			return true;
		} else return false;
	}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffect) {
		return potioneffect.getPotion() == MobEffects.POISON ? false : super.isPotionApplicable(potioneffect);
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);

		if (livingdata == null) {
			livingdata = new EntitySnowFlea.GroupData();
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntitySnowFlea.GroupData) livingdata).setRandomEffect(this.world.rand);
			}
		}

		if (livingdata instanceof EntitySnowFlea.GroupData) {
			Potion potion = ((EntitySnowFlea.GroupData) livingdata).effect;
			if (potion != null) {
				this.addPotionEffect(new PotionEffect(potion, Integer.MAX_VALUE));
			}
		}

		return livingdata;
	}

	@Override
	public float getEyeHeight() {
		return 0.65F;
	}

	static class AIAttack extends EntityAIAttackMelee {
		public AIAttack(EntitySnowFlea spider) {
			super(spider, 1.0D, true);
		}

		@Override
		public boolean shouldContinueExecuting() {
			float f = this.attacker.getBrightness();
			if (f >= 0.5F && this.attacker.getRNG().nextInt(100) == 0) {
				this.attacker.setAttackTarget((EntityLivingBase) null);
				return false;
			} else return super.shouldContinueExecuting();
		}

		@Override
		protected double getAttackReachSqr(EntityLivingBase attackTarget) {
			return 4.0F + attackTarget.width;
		}
	}

	static class FleaMoveHelper extends EntityMoveHelper {

		public FleaMoveHelper(EntitySnowFlea slimeIn) {
			super(slimeIn);
		}

		@Override
		public void onUpdateMoveHelper() {
			if (this.action == EntityMoveHelper.Action.MOVE_TO) {
				this.action = EntityMoveHelper.Action.WAIT;
				double d0 = this.posX - this.entity.posX;
				double d1 = this.posZ - this.entity.posZ;
				double d2 = this.posY - this.entity.posY;
				double d3 = d0 * d0 + d2 * d2 + d1 * d1;
				double d4 = 0;

				if (d3 < 2.5D) {
					this.entity.setMoveForward(0.0F);
					return;
				}

				float f9 = (float) (MathHelper.atan2(d1, d0) * (180D / Math.PI)) - 90.0F;
				this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f9, 90.0F);
				this.entity.setAIMoveSpeed((float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));

				if ((d2 > this.entity.stepHeight || d3 > 4) && d0 * d0 + d1 * d1 < Math.max(5.0F, this.entity.width)) {
					this.entity.setAIMoveSpeed((float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue()));
					this.entity.getJumpHelper().setJumping();
					this.entity.playSound(MistSounds.ENTITY_SNOW_FLEA_FLAY, 0.15F, 1.0F);
					this.action = EntityMoveHelper.Action.JUMPING;
				}
			} else super.onUpdateMoveHelper();
		}
	}

	public static class GroupData implements IEntityLivingData {
		public Potion effect;

		public void setRandomEffect(Random rand) {
			int i = rand.nextInt(4);
			if (i <= 1) this.effect = MobEffects.SPEED;
			else if (i <= 2) this.effect = MobEffects.STRENGTH;
			else if (i <= 3) this.effect = MobEffects.REGENERATION;
		}
	}
}