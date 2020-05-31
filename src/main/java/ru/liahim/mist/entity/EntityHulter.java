package ru.liahim.mist.entity;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
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
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptMeat;

public class EntityHulter extends EntityAlbino {

	private static final DataParameter<Boolean> IS_STANDING = EntityDataManager.<Boolean>createKey(EntityHulter.class, DataSerializers.BOOLEAN);
	private static long pregnantTime = MistTime.getDayInMonth() * 24000;
	private float clientSideStandAnimation0;
	private float clientSideStandAnimation;
	private int warningSoundTicks;

	public EntityHulter(World world) {
		super(world);
        this.experienceValue = 5;
		this.setSize(1.8F, 2.4F);
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 1.6875F;
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
		this.tasks.addTask(1, new EntityHulter.AIAttackMelee(this, 1.15D, false));
		this.tasks.addTask(2, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(3, new EntityAIFollowParentGender(this, 1.15D));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(5, this.aiTempt);
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(4, new EntityHulter.AIAttackPlayer<EntityPlayer>(EntityPlayer.class, true, false));
		this.targetTasks.addTask(5, new EntityHulter.AIAttackPlayer<EntityMomo>(EntityMomo.class, true, false));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
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
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_HULTER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_HULTER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_HULTER_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15F, 1.0F);
	}

	protected void playWarningSound() {
		if (this.warningSoundTicks <= 0) {
			this.playSound(MistSounds.ENTITY_HULTER_WARNING, 1.0F, getSoundPitch());
			this.warningSoundTicks = 40;
		}
	}

	@Override
	protected float getSoundPitch() {
		return this.isChild() ? (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F + 1.2F : (this.rand.nextFloat() - this.rand.nextFloat()) * 0.4F + 1.0F;
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
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.revengeTimer > 0 && !player.capabilities.isCreativeMode) return true;
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) return super.processInteract(player, hand);
		else if (this.isTamed() || player.capabilities.isCreativeMode) {
			if (this.isBreedingItem(stack)) {
				if (this.getHealth() < this.getMaxHealth()) {
					this.consumeItemFromStack(player, stack);
					this.heal(5);
				} else return super.processInteract(player, hand);
			} else if (stack.getItem() == Items.NAME_TAG) {
				stack.interactWithEntity(player, this, hand);
				return true;
			} else return false;
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
			this.setRevengeTime(this.revengeTimer + this.rand.nextInt((int)(200 * amount) + 1) + (int)(100 * amount) + 100);
		}
		return flag;
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
		return LootTables.HULTER_LOOT;
	}

	@Override
	public boolean childCheck() {
		List<EntityHulter> list = this.world.<EntityHulter>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityHulter entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityHulter(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.ROTTEN_FLESH) return false;
        return item instanceof ItemFood && ((ItemFood)item).isWolfsFavoriteMeat();
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		super.onInitialSpawn(difficulty, livingdata);
		if (livingdata instanceof EntityHulter.GroupData) {
			if (((EntityHulter.GroupData)livingdata).madeParent) {
				this.setGrowingAge(-24000);
			} else {
				this.setFemale(true);
				((EntityHulter.GroupData)livingdata).madeParent = this.rand.nextBoolean();
			}
		} else {
			EntityHulter.GroupData data = new EntityHulter.GroupData();
			data.madeParent = this.isFemale() && this.rand.nextBoolean();
			livingdata = data;
		}
		return livingdata;
	}

	@Override
	public int getTameLevel() {
		return 4;
	}

	class AIAttackMelee extends EntityAIAttackMelee {
		public AIAttackMelee(EntityCreature creature, double speed, boolean useLongMemory) {
			super(creature, speed, useLongMemory);
		}

		@Override
		protected void checkAndPerformAttack(EntityLivingBase entity, double distance) {
			double d0 = this.getAttackReachSqr(entity);
			if (distance <= d0 && this.attackTick <= 0) {
				this.attackTick = 20;
				this.attacker.attackEntityAsMob(entity);
				EntityHulter.this.setStanding(false);
			} else if (distance <= d0 * 2.0D) {
				if (this.attackTick <= 0) {
					EntityHulter.this.setStanding(false);
					this.attackTick = 20;
				}
				if (this.attackTick <= 10) {
					EntityHulter.this.setStanding(true);
					EntityHulter.this.playWarningSound();
				}
			} else {
				this.attackTick = 20;
				EntityHulter.this.setStanding(false);
			}
		}

		@Override
		public void resetTask() {
			EntityHulter.this.setStanding(false);
			super.resetTask();
		}
	}

	class AIAttackPlayer<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
		public AIAttackPlayer(Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
			super(EntityHulter.this, classTarget, checkSight, onlyNearby);
		}

		@Override
		public boolean shouldExecute() {
			boolean check = super.shouldExecute();
			if (EntityHulter.this.isTamed()) return !EntityHulter.this.isOwner(this.targetEntity) && this.targetEntity == EntityHulter.this.getRevengeTarget();
			else {
				if (check && (EntityHulter.this.aiTempt == null || !EntityHulter.this.aiTempt.isRunning())) return EntityHulter.this.getEntitySenses().canSee(this.targetEntity);
				return false;
			}
		}
	}

	static class GroupData implements IEntityLivingData {
		public boolean madeParent;
		private GroupData() {}
	}
}