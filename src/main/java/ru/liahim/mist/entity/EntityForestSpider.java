package ru.liahim.mist.entity;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
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
import ru.liahim.mist.entity.ai.PathNavigateClimberMistUpper;

public class EntityForestSpider extends EntityMobMist /*implements IRangedAttackMob*/ {

	private static final DataParameter<Byte> CLIMBING = EntityDataManager.<Byte>createKey(EntityForestSpider.class, DataSerializers.BYTE);

	public EntityForestSpider(World world) {
		super(world);
		this.setSize(0.9375F, 0.9375F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(3, new EntityForestSpider.AISpiderAttack(this));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 0.8D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityForestSpider.AISpiderTarget(this, EntityPlayer.class));
	}

	@Override
	protected PathNavigate createNavigator(World world) {
		return new PathNavigateClimberMistUpper(this, world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(CLIMBING, (byte) 0);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (!this.world.isRemote) this.setBesideClimbableBlock(this.collidedHorizontally);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_FOREST_SPIDER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_FOREST_SPIDER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_FOREST_SPIDER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(MistSounds.ENTITY_FOREST_SPIDER_STEP, 0.2F, 1.0F);
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.FOREST_SPIDER_LOOT;
	}

	@Override
	public int getTalkInterval() {
		return 160;
	}

	@Override
	protected float getSoundVolume() {
		return 0.9F;
	}

	@Override
	public boolean isOnLadder() {
		return this.isBesideClimbableBlock();
	}

	@Override
	public void setInWeb() {}

	@Override
	public EnumCreatureAttribute getCreatureAttribute() {
		return EnumCreatureAttribute.ARTHROPOD;
	}

	@Override
	public boolean isPotionApplicable(PotionEffect potioneffect) {
		return potioneffect.getPotion() == MobEffects.POISON ? false : super.isPotionApplicable(potioneffect);
	}

	public boolean isBesideClimbableBlock() {
		return (this.dataManager.get(CLIMBING) & 1) != 0;
	}

	public void setBesideClimbableBlock(boolean climbing) {
		byte b0 = this.dataManager.get(CLIMBING);
		if (climbing) b0 = (byte) (b0 | 1);
		else b0 = (byte) (b0 & -2);
		this.dataManager.set(CLIMBING, b0);
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);

		if (livingdata == null) {
			livingdata = new EntityForestSpider.GroupData();
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntityForestSpider.GroupData) livingdata).setRandomEffect(this.world.rand);
			}
		}

		if (livingdata instanceof EntityForestSpider.GroupData) {
			Potion potion = ((EntityForestSpider.GroupData) livingdata).effect;
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

	static class AISpiderAttack extends EntityAIAttackMelee {
		public AISpiderAttack(EntityForestSpider spider) {
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

	static class AISpiderTarget<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
		public AISpiderTarget(EntityForestSpider spider, Class<T> classTarget) {
			super(spider, classTarget, true);
		}

		@Override
		public boolean shouldExecute() {
			float f = this.taskOwner.getBrightness();
			return f >= 0.5F ? false : super.shouldExecute();
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