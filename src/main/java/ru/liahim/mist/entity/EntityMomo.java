package ru.liahim.mist.entity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIFollowSame;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;
import ru.liahim.mist.item.ItemMistFoodOnStick;

public class EntityMomo extends EntityAlbino {

	private static final DataParameter<Boolean> SADDLED = EntityDataManager.<Boolean>createKey(EntityMomo.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.<Integer>createKey(EntityMomo.class, DataSerializers.VARINT);
	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 17));
	private float xRotFactor = isInWater() || isInLava() ? 0 : 1;
	private boolean boosting;
	private int boostTime;
	private int totalBoostTime;
	private static long pregnantTime = MistTime.getDayInMonth() * 18000;
	private AIHurtByAggressor aiHurt;

	private int angerLevel;
	private UUID angerTargetUUID;

	public EntityMomo(World world) {
		super(world);
		this.setSize(1.25F, 1.125F);
	}

	@Override
	public double getMountedYOffset() {
		return this.isFemale() ? 0.8D : 0.9D;
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 1.2D, 1.2D, false, TEMPTATION_STACKS);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityMomo.AILeapAtTarget(0.4F));
		this.tasks.addTask(2, new EntityMomo.AIAttackMelee(this, 1.45f, false));
		this.tasks.addTask(3, new EntityAIPanic(this, 2.0D));
		this.tasks.addTask(4, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(5, this.aiTempt);
		this.tasks.addTask(6, new EntityAIFollowParentGender(this, 1.1D));
		this.tasks.addTask(7, new EntityMomo.AIAvoidEntity(this, EntityPlayer.class, 16, 1.0D, 1.5D));
		this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(10, new EntityAILookIdle(this));
		this.tasks.addTask(11, new EntityAIFollowSame(this, 1.0D, 12.0D, 24.0D));
		this.aiHurt = new EntityMomo.AIHurtByAggressor(this);
		this.targetTasks.addTask(1, this.aiHurt);
		this.targetTasks.addTask(2, new EntityMomo.AITargetAggressor(this));
	}

	@Override
	protected void updateAITasks() {
		if (this.isAngry()) --this.angerLevel;
		if (this.angerLevel > 0 && this.angerTargetUUID != null && this.attackingPlayer == null) {
			EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
			this.attackingPlayer = entityplayer;
			this.recentlyHit = this.getRevengeTimer();
		}
		super.updateAITasks();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
	}

	@Override
	protected long getPregnantTime() {
		return pregnantTime;
	}

	@Override
	public int getChildCount() {
		return this.rand.nextInt(this.rand.nextInt(2) + 2) + 1;
	}

	@Override
	public boolean initFemale() {
		return this.rand.nextInt(3) != 0;
	}

	@Override
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
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

	private void becomeAngryAt(Entity entity) {
		this.angerLevel = 300 + this.rand.nextInt(300);
		this.setRevengeTime(1000 + this.rand.nextInt(1000));
		if (entity != null && entity instanceof EntityLivingBase) this.angerTargetUUID = ((EntityLivingBase)entity).getUniqueID();
	}

	public boolean isAngry() {
		return this.angerLevel > 0;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("Saddle", this.isSaddled());
		compound.setShort("Anger", (short) this.angerLevel);
		if (this.angerTargetUUID != null) compound.setString("HurtBy", this.angerTargetUUID.toString());
		else compound.setString("HurtBy", "");
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setSaddled(compound.getBoolean("Saddle"));
		this.angerLevel = compound.getShort("Anger");
		String s = compound.getString("HurtBy");
		if (!s.isEmpty()) {
			this.angerTargetUUID = UUID.fromString(s);
			EntityPlayer entityplayer = this.world.getPlayerEntityByUUID(this.angerTargetUUID);
			if (entityplayer != null) {
				this.attackingPlayer = entityplayer;
				this.recentlyHit = this.getRevengeTimer();
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_MOMO_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_MOMO_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_MOMO_DEATH;
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
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			this.setRevengeTime(1000 + this.rand.nextInt(1000));
			if (this.getHealth() <= 0.0F) this.aiHurt.startExecuting();
		}
		return flag;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		return entity.attackEntityFrom(DamageSource.causeMobDamage(this), ((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.MOMO_LOOT;
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
		return this.isChild() ? this.height : 0.4375F;
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
		if (this.world.isRemote) {
			if (isInWater() || isInLava()) xRotFactor = Math.max(xRotFactor - 0.05f, 0);
			else xRotFactor = Math.min(xRotFactor + 0.05f, 1);
		} else if (this.rand.nextInt(900) == 0 && this.deathTime == 0) this.heal(1.0F);
		super.onLivingUpdate();
	}

	@Override
	public boolean childCheck() {
		List<EntityMomo> list = this.world.<EntityMomo>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityMomo entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityMomo(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntityMomo.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public float getXRotFactor(float tick) {
		return xRotFactor;
	}

	@Override
	public boolean isDriven() {
		return true;
	}

	@Override
	public int getTameLevel() {
		return 1;
	}

	/** Revenge target to attack target. Help to others. */
	class AIHurtByAggressor extends EntityAIHurtByTarget {

		public AIHurtByAggressor(EntityMomo entity) {
			super(entity, true);
		}

		@Override
		public boolean shouldExecute() {
			if (super.shouldExecute() && !((EntityTameable)this.taskOwner).isTamed()) {
				double d0 = this.getTargetDistance();
				for (EntityCreature entitycreature : this.taskOwner.world.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).grow(d0, 10.0D, d0))) {
					if (this.taskOwner != entitycreature && !this.taskOwner.isChild()) {
						return true;	
					}
				}
			}
			return false;
		}

		@Override
		public void startExecuting() {
			((EntityMomo)this.taskOwner).becomeAngryAt(this.target);
			super.startExecuting();
		}

		@Override
		protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase entityLivingBase) {
			super.setEntityAttackTarget(creature, entityLivingBase);
			if (creature instanceof EntityMomo) {
				((EntityMomo)creature).becomeAngryAt(entityLivingBase);
			}
		}
	}

	/** Time to attack. */
	class AITargetAggressor extends EntityAINearestAttackableTarget<EntityPlayer> {
		public AITargetAggressor(EntityMomo entity) {
			super(entity, EntityPlayer.class, true);
		}

		@Override
		public boolean shouldExecute() {
			return ((EntityMomo)this.taskOwner).isAngry() && super.shouldExecute();
		}

		@Override
		public void resetTask() {
			super.resetTask();
			((EntityMomo)this.taskOwner).setAttackTarget(null);
			((EntityMomo)this.taskOwner).setRevengeTarget(null);
		}
	}

	class AIAttackMelee extends EntityAIAttackMelee {
		public AIAttackMelee(EntityMomo entity, float speed, boolean useLongMemory) {
			super(entity, speed, useLongMemory);
		}

		@Override
		public boolean shouldExecute() {
			return !((EntityMomo)this.attacker).isPregnant() && this.attacker.getHealth() > this.attacker.getMaxHealth() / 2 && super.shouldExecute();
		}

		@Override
		protected double getAttackReachSqr(EntityLivingBase attackTarget) {
			return this.attacker.width * this.attacker.width * 2.0F + attackTarget.width;
		}
	}

	class AIAvoidEntity extends EntityAIAvoidEntity {
		public AIAvoidEntity(EntityMomo entity, Class<EntityPlayer> classToAvoid, float avoidDistance, double farSpeed, double nearSpeed) {
			super(entity, classToAvoid, avoidDistance, farSpeed, nearSpeed);
		}

		@Override
		public boolean shouldExecute() {
			return ((EntityMomo)this.entity).revengeTimer > 0 && super.shouldExecute();
		}
	}

	class AILeapAtTarget extends EntityAILeapAtTarget {
		EntityLiving leaper;
		public AILeapAtTarget(float leapMotionY) {
			super(EntityMomo.this, leapMotionY);
			this.leaper = EntityMomo.this;
		}

		@Override
		public boolean shouldExecute() {
			return this.leaper.getAttackTarget() != null && !this.leaper.getAttackTarget().isDead && super.shouldExecute();
		}
	}
}