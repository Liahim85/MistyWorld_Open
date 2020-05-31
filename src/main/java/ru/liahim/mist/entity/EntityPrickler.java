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
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIFollowSame;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;

public class EntityPrickler extends EntityAlbino {

	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 26));
	private static long pregnantTime = MistTime.getDayInMonth() * 24000;
	private AIHurtByAggressor aiHurt;

	public EntityPrickler(World world) {
		super(world);
		this.setSize(0.9F, 0.95F);
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 0.8D, 1.2D, false, TEMPTATION_STACKS);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAILeapAtTarget(this, 0.2F));
		this.tasks.addTask(2, new EntityPrickler.AIAttackMelee(this, 1.2D, true));
		this.tasks.addTask(3, new EntityAIPanic(this, 1.2D));
		this.tasks.addTask(4, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(5, this.aiTempt);
		this.tasks.addTask(6, new EntityAIFollowParentGender(this, 1.3D));
		this.tasks.addTask(7, new EntityPrickler.AIAvoidEntity(this, EntityPlayer.class, 8, 1.0D, 1.2D));
		this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(10, new EntityAILookIdle(this));
		this.tasks.addTask(11, new EntityAIFollowSame(this, 1.0D, 12.0D, 24.0D));
		this.aiHurt = new EntityPrickler.AIHurtByAggressor(this);
		this.targetTasks.addTask(1, this.aiHurt);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
	}

	@Override
	protected long getPregnantTime() {
		return pregnantTime;
	}

	@Override
	public int getChildCount() {
		return this.rand.nextInt(3) + 1;
	}

	@Override
	public boolean initFemale() {
		return this.rand.nextBoolean();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_PRICKLER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_PRICKLER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_PRICKLER_DEATH;
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
				stack.interactWithEntity(player, this, hand); return true;
			} else return false;
		}
		return tamedProcess(player, stack);
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		if (entity.attackEntityFrom(DamageSource.causeMobDamage(this), ((int) this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()))) {
			return true;
		} else return false;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			if (!source.isProjectile()) attackEntityAsMob(entity);
			if (this.getHealth() <= 0.0F) this.<EntityWulder>closePanic(EntityWulder.class, (EntityPlayer)entity, this.rand.nextInt(2000) + 2000);
			else this.<EntityWulder>closePanic(EntityWulder.class, (EntityPlayer)entity, this.rand.nextInt(250) + 250);
			this.setRevengeTime(this.rand.nextInt((int)(500 * amount) + 1) + (int)(250 * amount) + 250);
		}
		return flag;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.PRICKLER_LOOT;
	}

	@Override
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
		if (this.rand.nextInt(temptationStackDropChance) == 0) {
			this.entityDropItem((ItemStack)TEMPTATION_STACKS.toArray()[this.rand.nextInt(TEMPTATION_STACKS.size())], 0);
		}
		super.dropLoot(wasRecentlyHit, lootingModifier, source);
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 0.8F;
	}

	@Override
	public void onLivingUpdate() {
		this.updateArmSwingProgress();
		if (!this.world.isRemote && this.rand.nextInt(900) == 0 && this.deathTime == 0)
			this.heal(1.0F);
		super.onLivingUpdate();
	}

	@Override
	public boolean childCheck() {
		List<EntityPrickler> list = this.world.<EntityPrickler>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityPrickler entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityPrickler(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntityPrickler.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@Override
	public int getTameLevel() {
		return 2;
	}

	/** Revenge target to attack target. Help to others. */
	static class AIHurtByAggressor extends EntityAIHurtByTarget {

		public AIHurtByAggressor(EntityPrickler entity) {
			super(entity, false);
		}

		@Override
		public boolean shouldExecute() {
			return !((EntityTameable) this.taskOwner).isTamed() && super.shouldExecute();
		}
	}

	static class AIAttackMelee extends EntityAIAttackMelee {
		public AIAttackMelee(EntityPrickler entity, double speed, boolean useLongMemory) {
			super(entity, speed, useLongMemory);
		}

		@Override
		public boolean shouldExecute() {
			return !((EntityPrickler) this.attacker).isPregnant() && this.attacker.getHealth() > this.attacker.getMaxHealth() / 3 && super.shouldExecute();
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
		public AIAvoidEntity(EntityPrickler entity, Class<EntityPlayer> classToAvoid, float avoidDistance, double farSpeed, double nearSpeed) {
			super(entity, classToAvoid, avoidDistance, farSpeed, nearSpeed);
		}

		@Override
		public boolean shouldExecute() {
			return (!((EntityPrickler) this.entity).isTamed() || this.entity.getHealth() <= this.entity.getMaxHealth() / 3) && super.shouldExecute();
		}
	}
}