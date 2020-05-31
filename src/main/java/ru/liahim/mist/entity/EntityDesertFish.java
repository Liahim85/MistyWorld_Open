package ru.liahim.mist.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.entity.ai.PathNavigateWaterMistUpper;

public class EntityDesertFish extends EntityWaterMobMist {

	public EntityDesertFish(World world) {
		super(world);
		this.setSize(0.875F, 0.875F);
        this.setPathPriority(PathNodeType.WATER, 8.0F);
        this.enablePersistence();
	}

	@Override
	protected PathNavigate createNavigator(World world) {
		return new PathNavigateWaterMistUpper(this, world);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityDesertFish.AIAttack(this));
		this.tasks.addTask(2, new EntityDesertFish.AISwimRandmly(this));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 2, true, false, (Predicate)null));
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
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_DESERT_FISH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_DESERT_FISH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_DESERT_FISH_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(MistSounds.ENTITY_DESERT_FISH_STEP, 0.3F, 1.0F);
	}

	@Override
	protected float getSoundPitch() {
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F + 1.0F;
	}

	@Override
	protected float getSoundVolume() {
		return 0.7F;
	}

	@Override
	public int getTalkInterval() {
		return 80;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.DESERT_FISH_LOOT;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (super.attackEntityAsMob(entity)) {
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
	protected int getAirVolum() {
		return 3000;
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		this.setAir(this.getAirVolum());
		return super.onInitialSpawn(difficulty, livingdata);
	}

	@Override
	public boolean getCanSpawnHere() {
		return true;
	}

	public static boolean spawnFish(World world, BlockPos pos, Random rand) {
		if (!world.isRemote) {
			EntityDesertFish fish = new EntityDesertFish(world);
			fish.moveToBlockPosAndAngles(pos, MathHelper.wrapDegrees(rand.nextFloat() * 360), 0);
			fish.onInitialSpawn(world.getDifficultyForLocation(pos), (IEntityLivingData)null);
			fish.rotationYawHead = fish.rotationYaw;
			fish.renderYawOffset = fish.rotationYaw;
			world.spawnEntity(fish);
			return true;
		}
		return false;
	}

	static class AIAttack extends EntityAIAttackMelee {
		public AIAttack(EntityDesertFish fish) {
			super(fish, 1.2D, true);
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

	static class AISwimRandmly extends EntityAIWander {
		public AISwimRandmly(EntityDesertFish fish) {
			super(fish, 1.0D);
		}

		@Override
		@Nullable
		protected Vec3d getPosition() {
			Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
			for (int i = 0; vec3d != null && this.entity.world.getBlockState(new BlockPos(vec3d)).getMaterial() != Material.WATER && i++ < 10; vec3d = RandomPositionGenerator.findRandomTarget(this.entity, 10, 7)) {
				;
			}
			return vec3d;
		}
	}
}