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
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.entity.IMatWalkable;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.entity.ai.EntityAIEatFloatingMat;
import ru.liahim.mist.entity.ai.EntityAIEatMistGrass;

public class EntitySwampCrab extends EntityMobMist implements IMatWalkable {

	private static final DataParameter<Byte> COLOR_TYPE = EntityDataManager.<Byte>createKey(EntitySwampCrab.class, DataSerializers.BYTE);
	private EntityAIEatMistGrass aiEatGrass;
	private EntityAIEatFloatingMat aiEatMat;
	private int eatTimer;

	public EntitySwampCrab(World world) {
		super(world);
		this.setSize(0.75F, 0.25F);
	}

	@Override
	protected void initEntityAI() {
		this.aiEatGrass = new EntityAIEatMistGrass(this, false, false);
		this.aiEatMat = new EntityAIEatFloatingMat(this, false);
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntitySwampCrab.AISpiderAttack(this));
		this.tasks.addTask(3, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(5, this.aiEatGrass);
		this.tasks.addTask(5, this.aiEatMat);
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, true));
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
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_SWAMP_CRAB_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_SWAMP_CRAB_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_SWAMP_CRAB_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(MistSounds.ENTITY_SWAMP_CRAB_STEP, 0.3F, 1.0F);
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
		return 160;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.SWAMP_CRAB_LOOT;
	}

	@Override
	protected void updateAITasks() {
		this.eatTimer = this.aiEatGrass.getEatingGrassTimer();
		if (this.eatTimer == 0) this.eatTimer = this.aiEatMat.getEatingGrassTimer();
		super.updateAITasks();
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.eatTimer > 0) {
			if (!this.world.isRemote && this.eatTimer % 10 == 0) this.playSound(MistSounds.ENTITY_SWAMP_CRAB_STEP, 0.2F, 1.0F);
			--this.eatTimer;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 10) this.eatTimer = 40;
		else super.handleStatusUpdate(id);
	}

	@SideOnly(Side.CLIENT)
	public float getEatFactor(float partialTickTime) {
		if (this.eatTimer > 4 && this.eatTimer <= 36) {
			float f = (this.eatTimer - 4 - partialTickTime) / 1.7F;
			return MathHelper.sin((float) Math.PI * f);
		} else return 0;
	}

	@SideOnly(Side.CLIENT)
	public float getEyeFactor(float partialTickTime) {
		if (this.eatTimer <= 0) return 1.0F;
		else if (this.eatTimer >= 4 && this.eatTimer <= 36) return 0.0F;
		else return this.eatTimer < 4 ? (4 - this.eatTimer + partialTickTime) / 4.0F : (this.eatTimer - partialTickTime - 36) / 4.0F;
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
		this.setColorType(this.rand.nextInt(5));
		if (livingdata == null) {
			livingdata = new EntitySwampCrab.GroupData();
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntitySwampCrab.GroupData) livingdata).setRandomEffect(this.world.rand);
			}
		}

		if (livingdata instanceof EntitySwampCrab.GroupData) {
			Potion potion = ((EntitySwampCrab.GroupData) livingdata).effect;
			if (potion != null) {
				this.addPotionEffect(new PotionEffect(potion, Integer.MAX_VALUE));
			}
		}

		return livingdata;
	}

	static class AISpiderAttack extends EntityAIAttackMelee {
		public AISpiderAttack(EntitySwampCrab spider) {
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