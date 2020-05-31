package ru.liahim.mist.entity;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;

public class EntityCyclops extends EntityMobMist {

	public EntityCyclops(World world) {
		super(world);
		this.setSize(0.875F, 0.25F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityCyclops.AIAttack(this));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(5, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
	}

	@Override
	public float getEyeHeight() {
		return 0.25F;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(12.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_CYCLOPS_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_CYCLOPS_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_CYCLOPS_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(MistSounds.ENTITY_CYCLOPS_STEP, 0.3F, 1.0F);
	}

	@Override
	protected float getSoundPitch() {
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F;
	}

	@Override
	public int getTalkInterval() {
		return 160;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.CYCLOPS_LOOT;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (super.attackEntityAsMob(entity) && this.rand.nextInt(4) == 0) {
			if (entity instanceof EntityLivingBase) {
				int i = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL) i = 10;
				else if (this.world.getDifficulty() == EnumDifficulty.HARD) i = 20;
				if (i > 0) ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.HUNGER, i * 20, 0));
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
			livingdata = new EntityCyclops.GroupData();
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntityCyclops.GroupData) livingdata).setRandomEffect(this.world.rand);
			}
		}

		if (livingdata instanceof EntityCyclops.GroupData) {
			Potion potion = ((EntityCyclops.GroupData) livingdata).effect;
			if (potion != null) {
				this.addPotionEffect(new PotionEffect(potion, Integer.MAX_VALUE));
			}
		}

		return livingdata;
	}

	static class AIAttack extends EntityAIAttackMelee {
		public AIAttack(EntityCyclops spider) {
			super(spider, 1.2D, true);
		}

		@Override
		public boolean shouldContinueExecuting() {
			if (this.attacker.getRNG().nextInt(100) == 0) {
				this.attacker.setAttackTarget((EntityLivingBase) null);
				return false;
			} else return super.shouldContinueExecuting();
		}

		@Override
		protected double getAttackReachSqr(EntityLivingBase attackTarget) {
			return 2 + attackTarget.width;
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