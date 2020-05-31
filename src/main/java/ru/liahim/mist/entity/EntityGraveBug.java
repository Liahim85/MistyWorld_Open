package ru.liahim.mist.entity;

import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
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
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import ru.liahim.mist.api.biome.EnumBiomeType;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.world.biome.BiomeMist;

public class EntityGraveBug extends EntityMobMist {

	private static final DataParameter<Byte> COLOR_TYPE = EntityDataManager.<Byte>createKey(EntityGraveBug.class, DataSerializers.BYTE);
	private static final DataParameter<Boolean> IS_CHILD = EntityDataManager.<Boolean>createKey(EntityGraveBug.class, DataSerializers.BOOLEAN);
    
	public EntityGraveBug(World world) {
		super(world);
        this.setSize(0.75F, 0.5F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(3, new EntityGraveBug.AISpiderAttack(this));
		this.tasks.addTask(4, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(6, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, 2, true, false, (Predicate)null));
	}

	@Override
	public float getEyeHeight() {
		return 0.25F;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(COLOR_TYPE, (byte)0);
        this.dataManager.register(IS_CHILD, false);
	}

	public void setColorType(int color) {
		this.dataManager.set(COLOR_TYPE, (byte)color);
	}

	public byte getColorType() {
		return this.dataManager.get(COLOR_TYPE);
	}

	@Override
	public boolean isChild() {
		return this.dataManager.get(IS_CHILD);
	}

	public void setChild(boolean child) {
		this.dataManager.set(IS_CHILD, child);
		if (child) {
			this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 0.5D);
			this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue() * 0.5D);
		}
		this.setChildSize(child);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (IS_CHILD.equals(key)) this.setChildSize(this.isChild());
		super.notifyDataManagerChange(key);
	}

	public void setChildSize(boolean isChild) {
		if (isChild) super.setSize(0.5F, 0.25F);
		else super.setSize(0.75F, 0.5F);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setByte("ColorType", this.getColorType());
		if (this.isChild()) compound.setBoolean("IsBaby", true);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setColorType(compound.getByte("ColorType"));
		if (compound.getBoolean("IsBaby")) this.setChild(true);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23D);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_GRAVE_BUG_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return MistSounds.ENTITY_GRAVE_BUG_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_GRAVE_BUG_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		this.playSound(MistSounds.ENTITY_GRAVE_BUG_STEP, 0.5F, 1.0F);
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.GRAVE_BUG_LOOT;
	}

	@Override
	protected boolean canDropLoot() {
		return !this.isChild();
	}

	public static boolean spawnBug(World world, BlockPos pos, Random rand) {
		if (!world.isRemote && rand.nextInt(6) == 0) {
			if (rand.nextInt(3) == 0) {
				for (int i = 0; i < rand.nextInt(3) + 3; ++i) {
					EntityGraveBug bug = new EntityGraveBug(world);
					bug.setChild(true);
					bug.moveToBlockPosAndAngles(pos.add(rand.nextFloat() * 0.5F + 0.25F, 0, rand.nextFloat() * 0.5F + 0.25F), MathHelper.wrapDegrees(rand.nextFloat() * 360), 0);
					bug.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(pos)), (IEntityLivingData)null);
					bug.rotationYawHead = bug.rotationYaw;
					bug.renderYawOffset = bug.rotationYaw;
					world.spawnEntity(bug);
				}
			} else {
				EntityGraveBug bug = new EntityGraveBug(world);
				bug.moveToBlockPosAndAngles(pos, MathHelper.wrapDegrees(rand.nextFloat() * 360), 0);
				bug.onInitialSpawn(world.getDifficultyForLocation(pos), (IEntityLivingData)null);
				bug.rotationYawHead = bug.rotationYaw;
				bug.renderYawOffset = bug.rotationYaw;
				world.spawnEntity(bug);
			}
			return true;
		}
		return false;
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
	public boolean attackEntityAsMob(Entity entity) {
		if (super.attackEntityAsMob(entity) && this.rand.nextInt(4) == 0) {
			if (entity instanceof EntityLivingBase) {
				int i = 0;
				if (this.world.getDifficulty() == EnumDifficulty.NORMAL) i = 5;
				else if (this.world.getDifficulty() == EnumDifficulty.HARD) i = 10;
				if (i > 0) ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(MobEffects.POISON, i * 20, 0));
			}
			return true;
		} else return false;
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) {
			if (!this.world.isRemote) {
				Class<? extends Entity> oclass = EntityList.getClass(ItemMonsterPlacer.getNamedIdFrom(stack));
				if (oclass != null && this.getClass() == oclass) {
					EntityGraveBug entity = new EntityGraveBug(this.world);
					if (entity != null) {
						entity.setChild(true);
						entity.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
						entity.onInitialSpawn(world.getDifficultyForLocation(this.getPosition()), (IEntityLivingData)null);
						this.world.spawnEntity(entity);
						if (stack.hasDisplayName()) entity.setCustomNameTag(stack.getDisplayName());
						if (!player.capabilities.isCreativeMode) stack.shrink(1);
					}
				}
			}
            return true;
		}
		return super.processInteract(player, hand);
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		livingdata = super.onInitialSpawn(difficulty, livingdata);
		Biome biome = this.world.getBiome(this.getPosition());
		int i = 0;
		if (biome instanceof BiomeMist) {
			i = ((BiomeMist)biome).getBiomeType().ordinal();
			if (i > EnumBiomeType.Swamp.ordinal()) i = 0;
		}
		this.setColorType(i);
		if (livingdata == null) {
			livingdata = new EntityGraveBug.GroupData();
			if (this.world.getDifficulty() == EnumDifficulty.HARD && this.world.rand.nextFloat() < 0.1F * difficulty.getClampedAdditionalDifficulty()) {
				((EntityGraveBug.GroupData) livingdata).setRandomEffect(this.world.rand);
			}
		}

		if (livingdata instanceof EntityGraveBug.GroupData) {
			Potion potion = ((EntityGraveBug.GroupData) livingdata).effect;
			if (potion != null) {
				this.addPotionEffect(new PotionEffect(potion, Integer.MAX_VALUE));
			}
		}

		return livingdata;
	}

	static class AISpiderAttack extends EntityAIAttackMelee {
		public AISpiderAttack(EntityGraveBug spider) {
			super(spider, 1.0D, true);
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
			int i = rand.nextInt(5);
			if (i <= 1) this.effect = MobEffects.SPEED;
			else if (i <= 2) this.effect = MobEffects.STRENGTH;
			else if (i <= 3) this.effect = MobEffects.REGENERATION;
			else if (i <= 4) this.effect = MobEffects.INVISIBILITY;
		}
	}
}