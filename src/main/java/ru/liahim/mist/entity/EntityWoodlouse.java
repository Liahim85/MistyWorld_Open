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
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
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

public class EntityWoodlouse extends EntityMobMist {

	private static final DataParameter<Byte> COLOR_TYPE = EntityDataManager.<Byte>createKey(EntityWoodlouse.class, DataSerializers.BYTE);

	public EntityWoodlouse(World world) {
		super(world);
		this.setSize(0.75F, 0.5F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityWoodlouse.AISpiderAttack(this));
		this.tasks.addTask(3, new EntityAIWanderAvoidWater(this, 1.0D));
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
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(COLOR_TYPE, (byte) 0);
	}

	public void setColorType(int color) {
		this.dataManager.set(COLOR_TYPE, (byte) color);
	}

	public byte getColorType() {
		return this.dataManager.get(COLOR_TYPE);
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
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(12.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.3D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_WOODLOUSE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_WOODLOUSE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_WOODLOUSE_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(SoundEvents.ENTITY_SPIDER_STEP, 0.15F, 1.0F);
	}

	@Override
	public int getTalkInterval() {
		return 160;
	}

	@Override
	protected float getSoundVolume() {
		return 0.8F;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.WOODLOUSE_LOOT;
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
		this.setColorType(this.rand.nextInt(4));
		if (livingdata == null) {
			livingdata = new EntityWoodlouse.GroupData();
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntityWoodlouse.GroupData) livingdata).setRandomEffect(this.world.rand);
			}
		}

		if (livingdata instanceof EntityWoodlouse.GroupData) {
			Potion potion = ((EntityWoodlouse.GroupData) livingdata).effect;
			if (potion != null) {
				this.addPotionEffect(new PotionEffect(potion, Integer.MAX_VALUE));
			}
		}

		return livingdata;
	}

	static class AISpiderAttack extends EntityAIAttackMelee {
		public AISpiderAttack(EntityWoodlouse spider) {
			super(spider, 1.3D, true);
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