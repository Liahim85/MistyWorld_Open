package ru.liahim.mist.entity;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

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
import net.minecraft.entity.ai.EntityAIPanic;
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
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;

public class EntitySloth extends EntityAlbino {

	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistBlocks.TREE_SAPLING, 1, 6), new ItemStack(MistBlocks.TREE_SAPLING, 1, 11));
	private static long pregnantTime = MistTime.getDayInMonth() * 24000;

	public EntitySloth(World world) {
		super(world);
		this.setSize(1.6F, 2.0F);
        this.experienceValue = 3;
		this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 0.6D, 1.2D, true, TEMPTATION_STACKS);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIAttackMelee(this, 1.25f, true));
		this.tasks.addTask(1, new EntitySloth.AIPanic());
		this.tasks.addTask(2, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(3, new EntityAIFollowParentGender(this, 1.25D));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(5, this.aiTempt);
		this.tasks.addTask(6, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(7, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntitySloth.AIHurtByTarget());
		this.targetTasks.addTask(2, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(3, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(4, new EntitySloth.AIAttackPlayer());
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 1.6875F;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player) {
		return this.isTamed() && super.canBeLeashedTo(player);
	}

	@Override
	public void onLivingUpdate() {
		this.updateArmSwingProgress();
		if (!this.world.isRemote && this.rand.nextInt(900) == 0 && this.deathTime == 0)
			this.heal(1.0F);
		super.onLivingUpdate();
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isChild() ? MistSounds.ENTITY_SLOTH_AMBIENT_CHILD : MistSounds.ENTITY_SLOTH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_SLOTH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_SLOTH_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15F, 1.0F);
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.SLOTH_LOOT;
	}

	@Override
	protected int getSkillPoint() {
		return 2;
	}

	@Override
	protected void dropLoot(boolean wasRecentlyHit, int lootingModifier, DamageSource source) {
		if (this.rand.nextInt(temptationStackDropChance) == 0) {
			this.entityDropItem((ItemStack)TEMPTATION_STACKS.toArray()[this.rand.nextInt(TEMPTATION_STACKS.size())], 0);
		}
		super.dropLoot(wasRecentlyHit, lootingModifier, source);
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (this.revengeTimer > 0 && !player.capabilities.isCreativeMode) return false;
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) return super.processInteract(player, hand);
		if (this.isTamed() || player.capabilities.isCreativeMode) {
			boolean heal = false;
			if (this.isBreedingItem(stack) && this.getHealth() < this.getMaxHealth()) {
				this.heal(5.0f);
				heal = true;
			}
			boolean mate = super.processInteract(player, hand);
			if (heal && !mate && !player.capabilities.isCreativeMode) stack.shrink(1);
			return heal || mate;
		}
		return tamedProcess(player, stack);
	}

	@Override
	public boolean childCheck() {
		List<EntitySloth> list = this.world.<EntitySloth>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntitySloth entity : list) {
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
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntitySloth.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@Override
	public boolean attackEntityAsMob(Entity entity) {
		boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), ((int)this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
		if (flag) {
			int i = this.getRNG().nextInt(6);
			if (i < 3) entity.motionY += i == 0 ? 0.4D : 0.2D;
			this.applyEnchantments(this, entity);
		}
		return flag;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode && this.getHealth() <= 0.0F) {
			this.<EntitySloth>closePanic(EntitySloth.class, (EntityPlayer)entity, this.rand.nextInt(4000) + 4000);
		}
		return flag;
	}

	@Override
	public EntityAlbino getChild() {
		return new EntitySloth(this.world);
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
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata) {
		super.onInitialSpawn(difficulty, livingdata);
		if (livingdata instanceof EntitySloth.GroupData) {
			if (((EntitySloth.GroupData)livingdata).madeParent) {
				this.setGrowingAge(-24000);
			} else {
				this.setFemale(true);
				((EntitySloth.GroupData)livingdata).madeParent = this.rand.nextBoolean();
			}
		} else {
			EntitySloth.GroupData data = new EntitySloth.GroupData();
			data.madeParent = this.isFemale() && this.rand.nextBoolean();
			livingdata = data;
		}
		return livingdata;
	}

	class AIAttackPlayer extends EntityAINearestAttackableTarget<EntityPlayer> {

		public AIAttackPlayer() {
			super(EntitySloth.this, EntityPlayer.class, 20, true, true, (Predicate)null);
		}

		@Override
		public boolean shouldExecute() {
			if (EntitySloth.this.isChild() || (EntitySloth.this.isTamed() && EntitySloth.this.revengeTimer == 0)) return false;
			else {
				if (super.shouldExecute() && (EntitySloth.this.aiTempt == null || !EntitySloth.this.aiTempt.isRunning())) return EntitySloth.this.getEntitySenses().canSee(this.targetEntity);
				//EntitySloth.this.setAttackTarget((EntityLivingBase)null);
				return false;
			}
		}

		@Override
		protected double getTargetDistance() {
			for (EntitySloth entitypolarbear : EntitySloth.this.world.getEntitiesWithinAABB(EntitySloth.class, EntitySloth.this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D))) {
				if (entitypolarbear.isChild()) return super.getTargetDistance() * 0.5f;
			}
			return 5.0D;
		}
	}

	class AIHurtByTarget extends EntityAIHurtByTarget {

		public AIHurtByTarget() {
			super(EntitySloth.this, false);
		}

		@Override
		public void startExecuting() {
			super.startExecuting();
			if (EntitySloth.this.isChild()) {
				this.alertOthers();
				this.resetTask();
			}
		}

		@Override
		protected void setEntityAttackTarget(EntityCreature creature, EntityLivingBase entityLivingBase) {
			if (creature instanceof EntitySloth && !creature.isChild()) {
				creature.setRevengeTarget(entityLivingBase);
			}
		}
	}

	class AIPanic extends EntityAIPanic {
		public AIPanic() {
			super(EntitySloth.this, 1.5D);
		}

		@Override
		public boolean shouldExecute() {
			return !EntitySloth.this.isChild() && !EntitySloth.this.isBurning() ? false : super.shouldExecute();
		}
	}

	@Override
	public int getTameLevel() {
		return 3;
	}

	static class GroupData implements IEntityLivingData {
		public boolean madeParent;
		private GroupData() {}
	}
}