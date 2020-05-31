package ru.liahim.mist.entity;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIFollowSame;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;
import ru.liahim.mist.item.ItemMistFoodOnStick;

public class EntityBarvog extends EntityAlbino {

	private static final DataParameter<Boolean> SADDLED = EntityDataManager.<Boolean>createKey(EntityBarvog.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.<Integer>createKey(EntityBarvog.class, DataSerializers.VARINT);
	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 12), new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 27));
	private boolean boosting;
	private int boostTime;
	private int totalBoostTime;
	private static long pregnantTime = MistTime.getDayInMonth() * 24000;
	private AIHurtByAggressor aiHurt;

	public EntityBarvog(World world) {
		super(world);
		this.setSize(1.6F, 1.4F);
	}

	@Override
	public double getMountedYOffset() {
		return this.isFemale() ? 1.0D : 1.15D;
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 1.2D, 1.2D, false, TEMPTATION_STACKS);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAILeapAtTarget(this, 0.2F));
		this.tasks.addTask(2, new EntityBarvog.AIAttackMelee(this, 1.5D, true));
		this.tasks.addTask(3, new EntityBarvog.AIAvoidEntity(this, EntityPlayer.class, 16, 1.0D, 1.5D));
		this.tasks.addTask(4, new EntityAIPanic(this, 1.5D));
		this.tasks.addTask(5, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(6, this.aiTempt);
		this.tasks.addTask(7, new EntityAIFollowParentGender(this, 1.1D));
		this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(10, new EntityAILookIdle(this));
		this.tasks.addTask(11, new EntityAIFollowSame(this, 1.0D, 12.0D, 24.0D));
		this.aiHurt = new EntityBarvog.AIHurtByAggressor(this);
		this.targetTasks.addTask(1, this.aiHurt);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(25.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}

	@Override
	public int getTalkInterval() {
		return 300;
	}

	@Override
	protected long getPregnantTime() {
		return pregnantTime;
	}

	@Override
	public int getChildCount() {
		return this.rand.nextInt(4) + 2;
	}

	@Override
	public boolean initFemale() {
		return this.rand.nextBoolean();
	}

	@Override
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
	}

	@Override
	public boolean canBeSteered() {
		Entity entity = this.getControllingPassenger();
		if (!(entity instanceof EntityPlayer)) return false;
		else {
			EntityPlayer entityplayer = (EntityPlayer)entity;
			return  this.boosting || this.isBreedingItem(ItemMistFoodOnStick.getFood(entityplayer.getHeldItemMainhand()))
					|| this.isBreedingItem(ItemMistFoodOnStick.getFood(entityplayer.getHeldItemOffhand()));
		}
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (BOOST_TIME.equals(key) && this.world.isRemote) {
			this.boosting = true;
			this.boostTime = 0;
			this.totalBoostTime = this.dataManager.get(BOOST_TIME);
		}
		super.notifyDataManagerChange(key);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(SADDLED, false);
		this.dataManager.register(BOOST_TIME, 0);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("Saddle", this.isSaddled());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setSaddled(compound.getBoolean("Saddle"));
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_BARVOG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_BARVOG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_BARVOG_DEATH;
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15F, 1.0F);
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.revengeTimer > 0 && !player.capabilities.isCreativeMode) return true;
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) return super.processInteract(player, hand);
		if (this.isTamed() || player.capabilities.isCreativeMode) {
			if (this.isBreedingItem(stack)) return super.processInteract(player, hand);
			if (stack.getItem() == Items.NAME_TAG) {
				stack.interactWithEntity(player, this, hand);
				return true;
			} else if (this.isSaddled() && !this.isBeingRidden()) {
				this.startRiding(player);
				return true;
			} else if (stack.getItem() == Items.SADDLE && !this.isSaddled() && !this.isChild()) {
				this.setSaddled(true);
				this.world.playSound(player, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PIG_SADDLE, SoundCategory.NEUTRAL, 0.5F, 1.0F);
				stack.shrink(1);
				return true;
			} else return false;
		}
		return tamedProcess(player, stack);
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (!this.world.isRemote && this.isSaddled()) this.dropItem(Items.SADDLE, 1);
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (entity.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()))) {
			if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getRNG().nextInt(4) == 0) {
				int i = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL) i = 1;
				else if (this.world.getDifficulty() == EnumDifficulty.HARD) i = 2;
				if (i > 0) {
					((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 50, 0));
					if (((EntityLivingBase) entity).getRNG().nextBoolean()) {
						((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.NAUSEA, i * 200, 1));
					}
				}
			}
			return true;
		} else return false;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.LAGUH_LOOT;
	}

	@Override
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
		if (this.rand.nextInt(temptationStackDropChance) == 0) {
			this.entityDropItem((ItemStack)TEMPTATION_STACKS.toArray()[this.rand.nextInt(TEMPTATION_STACKS.size())], 0);
		}
		super.dropLoot(wasRecentlyHit, lootingModifier, source);
	}

	public boolean isSaddled() {
		return this.dataManager.get(SADDLED);
	}

	public void setSaddled(boolean saddled) {
		this.dataManager.set(SADDLED, saddled);
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 1.2F;
	}

	@Override
	public void travel(float strafe, float vertical, float forward) {
		Entity entity = this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
		if (this.isBeingRidden() && this.canBeSteered()) {
			this.rotationYaw = entity.rotationYaw;
			this.prevRotationYaw = this.rotationYaw;
			this.rotationPitch = entity.rotationPitch * 0.5F;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.renderYawOffset = this.rotationYaw;
			this.rotationYawHead = this.rotationYaw;
			this.stepHeight = 1.0F;
			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;

			if (this.boosting && this.boostTime++ > this.totalBoostTime) {
				this.boosting = false;
			}

			if (this.canPassengerSteer()) {
				float f = (float) this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.225F;
				if (this.boosting) f += f * 1.15F * MathHelper.sin((float) this.boostTime / (float) this.totalBoostTime * (float) Math.PI);
				this.setAIMoveSpeed(f);
				super.travel(0.0F, 0.0F, 1.0F);
			} else {
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
			}

			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d1 = this.posX - this.prevPosX;
			double d0 = this.posZ - this.prevPosZ;
			float f1 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;

			if (f1 > 1.0F) f1 = 1.0F;

			this.limbSwingAmount += (f1 - this.limbSwingAmount) * 0.4F;
			this.limbSwing += this.limbSwingAmount;
		} else {
			this.stepHeight = 0.5F;
			this.jumpMovementFactor = 0.02F;
			super.travel(strafe, vertical, forward);
		}
	}

	@Override
	public boolean boost() {
		if (this.boosting) return false;
		else {
			this.boosting = true;
			this.boostTime = 0;
			this.totalBoostTime = this.getRNG().nextInt(841) + 140;
			this.dataManager.set(BOOST_TIME, this.totalBoostTime);
			return true;
		}
	}

	@Override
	public void onLivingUpdate() {
		this.updateArmSwingProgress();
		if (!this.world.isRemote && this.rand.nextInt(900) == 0 && this.deathTime == 0) this.heal(1.0F);
		super.onLivingUpdate();
	}

	@Override
	public boolean childCheck() {
		List<EntityBarvog> list = this.world.<EntityBarvog>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityBarvog entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityBarvog(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntityBarvog.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@Override
	public boolean isDriven() {
		return true;
	}

	@Override
	public int getTameLevel() {
		return 3;
	}

	/** Revenge target to attack target. Help to others. */
	static class AIHurtByAggressor extends EntityAIHurtByTarget {

		public AIHurtByAggressor(EntityBarvog entity) {
			super(entity, false);
		}

		@Override
		public boolean shouldExecute() {
			return !((EntityTameable) this.taskOwner).isTamed() && super.shouldExecute();
		}
	}

	static class AIAttackMelee extends EntityAIAttackMelee {
		public AIAttackMelee(EntityBarvog entity, double speed, boolean useLongMemory) {
			super(entity, speed, useLongMemory);
		}

		@Override
		public boolean shouldExecute() {
			return !((EntityBarvog) this.attacker).isPregnant() && this.attacker.getHealth() > this.attacker.getMaxHealth() / 3 && super.shouldExecute();
		}

		@Override
		public boolean shouldContinueExecuting() {
			return this.attacker.getHealth() > this.attacker.getMaxHealth() / 3 && super.shouldContinueExecuting();
		}

		@Override
		protected double getAttackReachSqr(EntityLivingBase attackTarget) {
			return 4.0F + attackTarget.width;
		}
	}

	static class AIAvoidEntity extends EntityAIAvoidEntity {
		public AIAvoidEntity(EntityBarvog entity, Class<EntityPlayer> classToAvoid, float avoidDistance, double farSpeed, double nearSpeed) {
			super(entity, classToAvoid, avoidDistance, farSpeed, nearSpeed);
		}

		@Override
		public boolean shouldExecute() {
			return !((EntityBarvog) this.entity).isTamed() && this.entity.getHealth() <= this.entity.getMaxHealth() / 3 && super.shouldExecute();
		}
	}
}