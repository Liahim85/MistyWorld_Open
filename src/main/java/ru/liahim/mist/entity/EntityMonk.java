package ru.liahim.mist.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.entity.IOffetPassangerMount;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.client.model.animation.SimpleIK;
import ru.liahim.mist.client.model.entity.ModelMonk;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptMeat;

public class EntityMonk extends AbstractMistChestMount implements IOffetPassangerMount {

	private static final DataParameter<Boolean> IS_STANDING = EntityDataManager.<Boolean>createKey(EntityMonk.class, DataSerializers.BOOLEAN);
	private static long pregnantTime = MistTime.getDayInMonth() * 32000;
	private float clientSideStandAnimation0;
	private float clientSideStandAnimation;
	private int warningSoundTicks;

	public EntityMonk(World world) {
		super(world);
		this.experienceValue = 10;
		this.setSize(1.8F, 1.9375F);
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 1.6875F;
	}

	@Override
	public double getMountedYOffset() {
		return 1.8125D;
	}

	private float[] vec = new float[3];

	@Override
	public void updatePassenger(Entity passenger) {
		if (this.isPassenger(passenger)) {
			float offset = 0.9375F;
			float f3 = getStandingAnimationScale(0);
			f3 *= f3;
			vec[0] = 0;
			vec[1] = offset;
			vec[2] = 0.0625F + offset;
			vec = SimpleIK.rotateX(vec, -f3 * (float) Math.PI / 6);
			vec[1] -= offset;
			vec[2] -= offset;
			vec = SimpleIK.rotateY(vec, -(float) Math.toRadians(this.renderYawOffset));
			passenger.setPosition(this.posX + vec[0], this.posY + this.getMountedYOffset() + passenger.getYOffset() + vec[1], this.posZ + vec[2]);
		}
	}

	@Override
	public void onLivingUpdate() {
		this.updateArmSwingProgress();
		super.onLivingUpdate();
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptMeat(this, 0.6D, 1.0D, true, 16.0D);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityMonk.AIAttackMelee(this, 1.2D, false));
		this.tasks.addTask(1, new EntityMonk.AIPanic());
		this.tasks.addTask(2, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(3, new EntityAIFollowParentGender(this, 1.15D));
		this.tasks.addTask(4, new EntityMonk.AIAvoidEntity(EntityPlayer.class, 16, 1.0D, 1.15D));
		this.tasks.addTask(5, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(6, this.aiTempt);
		this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityMonk.AIHurtByTarget());
		this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(4, new EntityMonk.AIAttackEntity<EntityPlayer>(EntityPlayer.class, true, false));
		this.targetTasks.addTask(5, new EntityMonk.AIAttackEntity<EntityForestRunner>(EntityForestRunner.class, true, false));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(60.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.9D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player) {
		return this.isTamed() && super.canBeLeashedTo(player);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(IS_STANDING, false);
	}

	public boolean isStanding() {
		return this.dataManager.get(IS_STANDING);
	}

	public void setStanding(boolean standing) {
		this.dataManager.set(IS_STANDING, standing);
	}

	@Override
	protected long getPregnantTime() {
		return pregnantTime;
	}

	@Override
	public int getChildCount() {
		return this.rand.nextInt(this.rand.nextInt(2) + 1) + 1;
	}

	@Override
	public boolean initFemale() {
		return this.rand.nextBoolean();
	}

	@Override
	public int getTalkInterval() {
		return 400;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_MONK_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_MONK_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_MONK_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		if (!block.getDefaultState().getMaterial().isLiquid()) {
			SoundType soundtype = block.getSoundType();
			if (this.world.getBlockState(pos.up()).getBlock() == Blocks.SNOW_LAYER) {
				soundtype = Blocks.SNOW_LAYER.getSoundType();
			}
			this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
		}
	}

	protected void playWarningSound() {
		if (this.warningSoundTicks <= 0) {
			this.playSound(MistSounds.ENTITY_MONK_WARNING, 1.0F, getSoundPitch());
			this.warningSoundTicks = 40;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.world.isRemote) {
			this.clientSideStandAnimation0 = this.clientSideStandAnimation;
			if (this.isStanding()) {
				this.clientSideStandAnimation = MathHelper.clamp(this.clientSideStandAnimation + 1.0F, 0.0F, 6.0F);
			} else this.clientSideStandAnimation = MathHelper.clamp(this.clientSideStandAnimation - 1.0F, 0.0F, 6.0F);
		}
		if (this.warningSoundTicks > 0) --this.warningSoundTicks;
	}

	@SideOnly(Side.CLIENT)
	public float getStandingAnimationScale(float partialTicks) {
		return (this.clientSideStandAnimation0 + (this.clientSideStandAnimation - this.clientSideStandAnimation0) * partialTicks) / 6.0F;
	}

	@Override
	public float getSpeedMultipler() {
		return 0.3F;
	}

	@Override
	public float getSpintMultipler() {
		return 1.2F;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.revengeTimer > 0 && !player.capabilities.isCreativeMode) return true;
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) return super.processInteract(player, hand);
		else if (this.isTamed() || player.capabilities.isCreativeMode) {
			if (player.isSneaking()) {
				if (!this.isChild()) {
					this.openGUI(player);
					return true;
				} else return false;
			} else {
				if (this.isBreedingItem(stack)) {
					if (this.getHealth() < this.getMaxHealth()) {
						this.consumeItemFromStack(player, stack);
						this.heal(5);
					} else return super.processInteract(player, hand);
				} else if (stack.getItem() == Items.NAME_TAG) {
					stack.interactWithEntity(player, this, hand);
					return true;
				} else if (!this.hasChest() && Block.getBlockFromItem(stack.getItem()) instanceof BlockChest) {
					this.setChested(true);
					this.playChestEquipSound();
					this.initHorseChest();
					this.horseChest.setInventorySlotContents(1, stack.splitStack(1));
					if (stack.isEmpty()) player.setHeldItem(hand, ItemStack.EMPTY);
					return true;
				} else if (this.isSaddled() && !this.isBeingRidden() /*&& this.isOwner(player)*/ && isRiddingItem(player.getHeldItemMainhand()) && isRiddingItem(player.getHeldItemOffhand())) {
					this.startRiding(player);
					return true;
				} else if (stack.getItem() == Items.SADDLE && this.isTamed() && !this.isSaddled() && !this.isChild()) {
					this.horseChest.setInventorySlotContents(0, stack.splitStack(1));
					if (stack.isEmpty()) player.setHeldItem(hand, ItemStack.EMPTY);
					this.updateHorseSlots();
					return true;
				} else return false;
			}
		}
		return tamedProcess(player, stack);
	}

	@Override
	public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {
		if (this.revengeTimer == 0 && this.getPosition().distanceSq(owner.getPosition()) <= 1024) {
			if (!(target instanceof EntityCreeper) && !(target instanceof EntityGhast)) {
				if (target instanceof EntityAnimalMist) return !this.isTamed();
				if (target instanceof EntityTameable) {
					EntityTameable teamable = (EntityTameable)target;
					if (teamable.isTamed() && teamable.getOwner() == owner) return false;
				}
				if (target instanceof EntityPlayer && owner instanceof EntityPlayer &&
						!((EntityPlayer)owner).canAttackPlayer((EntityPlayer)target)) return false;
				else return (!(target instanceof AbstractHorse) || !((AbstractHorse)target).isTame()) &&
						(!(target instanceof AbstractMistMount) || !((AbstractMistMount)target).isTamed());
			}
		}
		return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			if (this.getHealth() <= 0.0F) this.<EntityMonk>closePanic(EntityMonk.class, (EntityPlayer)entity, this.rand.nextInt(1000) + 1000);
			this.setRevengeTime(this.revengeTimer + this.rand.nextInt((int)(200 * amount) + 1) + (int)(100 * amount) + 100);
		}
		return flag;
	}

	@Override
	protected <T extends EntityAnimalMist> void closePanic(Class <T> clazz, EntityPlayer sourse, long time) {
		List<T> list = this.world.<T>getEntitiesWithinAABB(clazz, this.getEntityBoundingBox().grow(16.0D, 3.0D, 16.0D));
		for (T entity : list) {
			if (entity.isChild() && entity.getEntitySenses().canSee(sourse)) entity.setRevengeTime(time);
		}
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		float damage = (float) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), this.isChild() ? damage / 2 : damage);
		if (flag) this.applyEnchantments(this, entity);
		return flag;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.MONK_LOOT;
	}

	@Override
	protected int getSkillPoint() {
		return 2;
	}

	@Override
	public boolean childCheck() {
		List<EntityMonk> list = this.world.<EntityMonk>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityMonk entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	public boolean isOnSameTeam(Entity entity) {
		if (this.revengeTimer == 0) return isOnTeam(entity);
		return false;
	}

	public boolean isOnTeam(Entity entity) {
		if (this.isTamed()) {
			EntityLivingBase owner = this.getOwner();
			if (entity == owner) return true;
			if (owner != null) return owner.isOnSameTeam(entity);
		}
		return this.isOnScoreboardTeam(entity.getTeam());
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityMonk(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.ROTTEN_FLESH) return false;
        return item instanceof ItemFood && ((ItemFood)item).isWolfsFavoriteMeat();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] getPassangerOffset(float[] vec, float limbSwing, float limbSwingAmount) {
		return ModelMonk.getPassangerOffset(vec, limbSwing, limbSwingAmount);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		super.onInitialSpawn(difficulty, livingdata);
		if (livingdata instanceof EntityMonk.GroupData) {
			if (((EntityMonk.GroupData)livingdata).madeParent) {
				this.setGrowingAge(-24000);
			} else {
				this.setFemale(true);
				((EntityMonk.GroupData)livingdata).madeParent = this.rand.nextBoolean();
			}
		} else {
			EntityMonk.GroupData data = new EntityMonk.GroupData();
			data.madeParent = this.isFemale() && this.rand.nextBoolean();
			livingdata = data;
		}
		return livingdata;
	}

	@Override
	public int getTameLevel() {
		return 6;
	}

	class AIAttackMelee extends EntityAIAttackMelee {
		public AIAttackMelee(EntityCreature creature, double speed, boolean useLongMemory) {
			super(creature, speed, useLongMemory);
		}

		@Override
		public boolean shouldExecute() {
			if (EntityMonk.this.isChild()) return false;
			else return super.shouldExecute();
		}

		@Override
		protected void checkAndPerformAttack(EntityLivingBase entity, double distance) {
			double d0 = this.getAttackReachSqr(entity);
			if (distance <= d0 && this.attackTick <= 0) {
				this.attackTick = 20;
				this.attacker.attackEntityAsMob(entity);
				EntityMonk.this.setStanding(false);
			} else if (distance <= d0 * 2.0D) {
				if (this.attackTick <= 0) {
					EntityMonk.this.setStanding(false);
					this.attackTick = 20;
				}
				if (this.attackTick <= 10) {
					EntityMonk.this.setStanding(true);
					EntityMonk.this.playWarningSound();
				}
			} else {
				this.attackTick = 20;
				EntityMonk.this.setStanding(false);
			}
		}

		@Override
		public void resetTask() {
			EntityMonk.this.setStanding(false);
			super.resetTask();
		}

		@Override
		protected double getAttackReachSqr(EntityLivingBase attackTarget) {
			return 9.0F + attackTarget.width;
		}
	}

	class AIPanic extends EntityAIPanic {
		public AIPanic() {
			super(EntityMonk.this, 1.5D);
		}

		@Override
		public boolean shouldExecute() {
			return !EntityMonk.this.isChild() && !EntityMonk.this.isBurning() ? false : super.shouldExecute();
		}
	}

	class AIAvoidEntity<T extends Entity> extends EntityAIAvoidEntity {
		public AIAvoidEntity(Class<T> classToAvoid, float avoidDistance, double farSpeed, double nearSpeed) {
			super(EntityMonk.this, classToAvoid, avoidDistance, farSpeed, nearSpeed);
		}

		@Override
		public boolean shouldExecute() {
			return EntityMonk.this.isChild() && EntityMonk.this.revengeTimer > 0 && super.shouldExecute();
		}
	}

	class AIHurtByTarget extends EntityAIHurtByTarget {

		public AIHurtByTarget() {
			super(EntityMonk.this, true);
		}

		@Override
		public void startExecuting() {
			super.startExecuting();
			if (EntityMonk.this.isChild()) {
				//this.alertOthers();
				this.resetTask();
			}
		}

		@Override
		protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase entityLivingBase) {
			if (creature instanceof EntityMonk && !creature.isChild()) {
				creature.setRevengeTarget(entityLivingBase);
			}
		}
	}

	class AIAttackEntity<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
		public AIAttackEntity(Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
			super(EntityMonk.this, classTarget, checkSight, onlyNearby);
		}

		@Override
		public boolean shouldExecute() {
			if (EntityMonk.this.isChild()) return false;
			boolean check = super.shouldExecute();
			if (this.targetEntity != null && this.targetEntity.getRidingEntity() instanceof EntityMonk && EntityMonk.this.getRevengeTarget() != this.targetEntity) return false;
			if (EntityMonk.this.isTamed()) return !EntityMonk.this.isOwner(this.targetEntity) && this.targetEntity == EntityMonk.this.getRevengeTarget();
			else {
				if (check && (EntityMonk.this.aiTempt == null || !EntityMonk.this.aiTempt.isRunning())) return EntityMonk.this.getEntitySenses().canSee(this.targetEntity);
				return false;
			}
		}
	}

	static class GroupData implements IEntityLivingData {
		public boolean madeParent;
		private GroupData() {}
	}
}