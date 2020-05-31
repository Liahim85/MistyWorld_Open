package ru.liahim.mist.entity;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.entity.IMatWalkable;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptMeat;

public class EntityGalaga extends AbstractMistChestMount implements IMatWalkable {
	
	private static final DataParameter<Boolean> OPEN_MOUTH = EntityDataManager.<Boolean>createKey(EntityGalaga.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Byte> COLOR_TYPE = EntityDataManager.<Byte>createKey(EntityGalaga.class, DataSerializers.BYTE);
	private static long pregnantTime = MistTime.getDayInMonth() * 24000;
	private float clientOpenMouthAnimation0;
	private float clientOpenMouthAnimation;

	public EntityGalaga(World world) {
		super(world);
		this.experienceValue = 5;
		this.setSize(1.8F, 1.75F);
	}

	@Override
	public float getEyeHeight() {
		return this.isChild() ? this.height : 1.6875F;
	}

	@Override
	public double getMountedYOffset() {
		return 1.625D;
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
		this.tasks.addTask(1, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(2, this.aiTempt);
		this.tasks.addTask(3, new EntityGalaga.AIAttackMelee(this, 1.15D, false));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityGalaga.AIAttackPlayer<EntityPlayer>(EntityPlayer.class, true, false));
		this.targetTasks.addTask(2, new EntityGalaga.AIAttackPlayer<EntityMossling>(EntityMossling.class, true, false));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(50.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
	}

	@Override
	public boolean canBeLeashedTo(EntityPlayer player) {
		return this.isTamed() && super.canBeLeashedTo(player);
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
	public int getTalkInterval() {
		return 400;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(COLOR_TYPE, (byte)0);
		this.dataManager.register(OPEN_MOUTH, false);
	}

	public void setColorType(int color) {
		this.dataManager.set(COLOR_TYPE, (byte)color);
	}

	public byte getColorType() {
		return this.dataManager.get(COLOR_TYPE);
	}

	public boolean isMouthOpened() {
		return this.dataManager.get(OPEN_MOUTH);
	}

	public void setMouthOpened(boolean open) {
		this.dataManager.set(OPEN_MOUTH, open);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("ColorType", this.getColorType());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setColorType(compound.getByte("ColorType"));
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		IEntityLivingData eld = super.onInitialSpawn(difficulty, livingdata);
		if (!this.isFemale()) this.setColorType(this.rand.nextInt(this.rand.nextInt(7) + 1) + 1);
		return eld;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_GALAGA_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_GALAGA_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_GALAGA_DEATH;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.world.isRemote) {
			this.clientOpenMouthAnimation0 = this.clientOpenMouthAnimation;
			if (this.isMouthOpened()) {
				this.clientOpenMouthAnimation = MathHelper.clamp(this.clientOpenMouthAnimation + 1.0F, 0.0F, 6.0F);
			} else this.clientOpenMouthAnimation = MathHelper.clamp(this.clientOpenMouthAnimation - 1.0F, 0.0F, 6.0F);
		}
	}

	@SideOnly(Side.CLIENT)
	public float getOpenMouthAnimationScale(float partialTicks) {
		return (this.clientOpenMouthAnimation0 + (this.clientOpenMouthAnimation - this.clientOpenMouthAnimation0) * partialTicks) / 6.0F;
	}

	@Override
	public float getSpeedMultipler() {
		return 0.3F;
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
				} else if (this.isSaddled() && !this.isBeingRidden() && isRiddingItem(player.getHeldItemMainhand()) && isRiddingItem(player.getHeldItemOffhand())) {
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
		if (entity.attackEntityFrom(DamageSource.causeMobDamage(this), this.isChild() ? damage / 2 : damage)) {
			if (entity instanceof EntityLivingBase && ((EntityLivingBase) entity).getRNG().nextInt(4) == 0) {
				int i = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL) i = 1;
				else if (this.world.getDifficulty() == EnumDifficulty.HARD) i = 2;
				if (i > 0) {
					((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 50, 0));
					if (((EntityLivingBase) entity).getRNG().nextBoolean()) {
						((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, i * 200, 2));
					}
				}
			}
			this.applyEnchantments(this, entity);
			return true;
		} else return false;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.SALAM_LOOT;
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.95F;
	}

	@Override
	public boolean childCheck() {
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityGalaga(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.ROTTEN_FLESH) return false;
        return item instanceof ItemFood && ((ItemFood)item).isWolfsFavoriteMeat();
	}

	@Override
	public int getTameLevel() {
		return 5;
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
				EntityGalaga.this.setMouthOpened(false);
			} else if (distance <= d0 * 2.0D) {
				if (this.attackTick <= 0) {
					EntityGalaga.this.setMouthOpened(false);
					this.attackTick = 20;
				}
				if (this.attackTick <= 10) {
					EntityGalaga.this.setMouthOpened(true);
				}
			} else {
				this.attackTick = 20;
				EntityGalaga.this.setMouthOpened(false);
			}
		}

		@Override
		public void resetTask() {
			EntityGalaga.this.setMouthOpened(false);
			super.resetTask();
		}

		@Override
		protected double getAttackReachSqr(EntityLivingBase attackTarget) {
			return this.attacker.width * this.attacker.width * 3 + attackTarget.width;
		}
	}

	class AIAttackPlayer<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
		public AIAttackPlayer(Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
			super(EntityGalaga.this, classTarget, checkSight, onlyNearby);
		}

		@Override
		public boolean shouldExecute() {
			boolean check = super.shouldExecute();
			if (this.targetEntity != null && this.targetEntity.getRidingEntity() instanceof EntityGalaga && EntityGalaga.this.getRevengeTarget() != this.targetEntity) return false;
			if (EntityGalaga.this.isTamed()) return !EntityGalaga.this.isOwner(this.targetEntity) && this.targetEntity == EntityGalaga.this.getRevengeTarget();
			else {
				if (check && (EntityGalaga.this.aiTempt == null || !EntityGalaga.this.aiTempt.isRunning())) return EntityGalaga.this.getEntitySenses().canSee(this.targetEntity);
				return false;
			}
		}
	}
}