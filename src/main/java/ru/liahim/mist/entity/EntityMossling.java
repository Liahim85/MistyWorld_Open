package ru.liahim.mist.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.entity.IMatWalkable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.api.sound.MistSounds;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.entity.ai.EntityAIEatFloatingMat;
import ru.liahim.mist.entity.ai.EntityAIFollowParentGender;
import ru.liahim.mist.entity.ai.EntityAIMateGender;
import ru.liahim.mist.entity.ai.EntityAITemptTamed;
import ru.liahim.mist.item.ItemMistFoodOnStick;

public class EntityMossling extends EntityAlbino implements IMatWalkable, IShearable {
	
	private static final DataParameter<Boolean> SADDLED = EntityDataManager.<Boolean>createKey(EntityMossling.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> BOOST_TIME = EntityDataManager.<Integer>createKey(EntityMossling.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> SHEARED = EntityDataManager.<Boolean>createKey(EntityMossling.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> WOOL_TIME = EntityDataManager.<Integer>createKey(EntityMossling.class, DataSerializers.VARINT);
	private static final Set<ItemStack> TEMPTATION_STACKS = Sets.newHashSet(new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 9), new ItemStack(MistItems.MUSHROOMS_FOOD, 1, 10));
	private float xRotFactor = isInWater() || isInLava() ? 0 : 1;
	private boolean boosting;
	private int boostTime;
	private int totalBoostTime;
	private int eatTimer;
	private int woolTimer;
	private static long pregnantTime = MistTime.getDayInMonth() * 24000;
	private EntityAIEatFloatingMat aiEatGrass;

	public EntityMossling(World world) {
		super(world);
		this.setSize(0.9F, 0.9F);
	}

	@Override
	public double getMountedYOffset() {
		return 0.7D;
	}

	@Override
	protected void initEntityAI() {
		this.aiTempt = new EntityAITemptTamed(this, 0.6D, 1.2D, true, TEMPTATION_STACKS);
		this.aiEatGrass = new EntityAIEatFloatingMat(this);
		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIPanic(this, 2.0D));
		this.tasks.addTask(2, new EntityAIMateGender(this, 1.0D));
		this.tasks.addTask(3, this.aiTempt);
		this.tasks.addTask(4, new EntityAIFollowParentGender(this, 1.1D));
		this.tasks.addTask(5, new EntityAIAvoidEntity(this, EntityPlayer.class, 16, 0.8D, 2.0D));
		this.tasks.addTask(6, new EntityAIAvoidEntity(this, EntityGalaga.class, 17, 1.5D, 2.0D));
		this.tasks.addTask(7, this.aiEatGrass);
		this.tasks.addTask(8, new EntityAIWanderAvoidWater(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
		this.tasks.addTask(10, new EntityAILookIdle(this));
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
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
			return this.boosting || this.isBreedingItem(ItemMistFoodOnStick.getFood(entityplayer.getHeldItemMainhand()))
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
		if (WOOL_TIME.equals(key) && this.world.isRemote) {
			this.woolTimer = this.dataManager.get(WOOL_TIME);
		}
		super.notifyDataManagerChange(key);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(SADDLED, false);
		this.dataManager.register(BOOST_TIME, 0);
		this.dataManager.register(SHEARED, false);
		this.dataManager.register(WOOL_TIME, 0);
	}

	public void setSheared(boolean sheared) {
		this.dataManager.set(SHEARED, sheared);
	}

	public boolean isSheared() {
		return this.dataManager.get(SHEARED);
	}

	private void updateWoolTimer() {
		this.dataManager.set(WOOL_TIME, this.woolTimer);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("Saddle", this.isSaddled());
		compound.setBoolean("Sheared", this.isSheared());
		compound.setInteger("WoolTime", this.woolTimer);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setSaddled(compound.getBoolean("Saddle"));
		this.setSheared(compound.getBoolean("Sheared"));
		this.woolTimer = compound.getInteger("WoolTime");
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return MistSounds.ENTITY_MOSSLING_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damage) {
		return MistSounds.ENTITY_MOSSLING_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return MistSounds.ENTITY_MOSSLING_DEATH;
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
		if (!this.world.isRemote) {
			if (this.isSaddled()) this.dropItem(Items.SADDLE, 1);
			if (!this.isSheared()) this.dropWool(cause, this.isAlbino() ? 0 : 13);
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		boolean flag = super.attackEntityFrom(source, amount);
		Entity entity = source.getTrueSource();
		if (flag && entity != null && entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode) {
			if (this.getHealth() <= 0.0F) this.<EntityMossling>closePanic(EntityMossling.class, (EntityPlayer)entity, this.rand.nextInt(8000) + 8000);
			else this.<EntityMossling>closePanic(EntityMossling.class, (EntityPlayer)entity, this.rand.nextInt(250) + 250);
			this.setRevengeTime(this.rand.nextInt((int)(500 * amount) + 1) + (int)(250 * amount) + 250);
		}
		return flag;
	}

	@Override
	@Nullable
	protected ResourceLocation getLootTable() {
		return LootTables.MOSSLING_LOOT;
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
		return this.isChild() ? this.height : 0.5625F;
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
	protected void updateAITasks() {
		this.eatTimer = this.aiEatGrass.getEatingGrassTimer();
		super.updateAITasks();
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.world.isRemote) {
			if (isInWater() || isInLava()) xRotFactor = Math.max(xRotFactor - 0.05f, 0);
			else xRotFactor = Math.min(xRotFactor + 0.05f, 1);
			if (this.eatTimer > 0) --this.eatTimer;
		}
		if (this.woolTimer > 0) {
			--this.woolTimer;
			if (this.woolTimer == 0) {
				this.setSheared(false);
				updateWoolTimer();
			}
		}
	}

	@Override
	public boolean childCheck() {
		List<EntityMossling> list = this.world.<EntityMossling>getEntitiesWithinAABB(this.getClass(), this.getEntityBoundingBox().grow(8.0D, 4.0D, 8.0D));
		for (EntityMossling entity : list) {
			if (entity.isChild() && (!entity.canBeTempted() || !entity.isTamed())) return false;
		}
		return true;
	}

	@Override
	protected EntityAlbino getChild() {
		return new EntityMossling(this.world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		for (ItemStack st : EntityMossling.TEMPTATION_STACKS) {
			if (stack.getItem() == st.getItem() && stack.getItemDamage() == st.getItemDamage()) return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleStatusUpdate(byte id) {
		if (id == 10) this.eatTimer = 40;
		else super.handleStatusUpdate(id);
	}

	@SideOnly(Side.CLIENT)
	public float getXRotFactor(float tick) {
		return xRotFactor;
	}

	@SideOnly(Side.CLIENT)
	public float getYEatFactor(float partialTickTime) {
		if (this.eatTimer <= 0) return 1.0F;
		else if (this.eatTimer >= 4 && this.eatTimer <= 36) return 0.0F;
		else return this.eatTimer < 4 ? (4 - this.eatTimer + partialTickTime) / 4.0F : (this.eatTimer - partialTickTime - 36) / 4.0F;
	}

	@SideOnly(Side.CLIENT)
	public float getXEatFactor(float partialTickTime) {
		if (this.eatTimer > 4 && this.eatTimer <= 36) {
			float f = (this.eatTimer - 4 - partialTickTime) / 4.0F;
			return MathHelper.sin((float) Math.PI * f);
		} else return 0;
	}

	@Override
	public void eatGrassBonus() {
		this.woolTimer = Math.max(1, this.woolTimer - 4000);
		if (this.isChild()) {
			this.addGrowth(60);
		}
	}

	@Override
	public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
		return this.isTamed() && this.woolTimer == 0 && !this.isSheared() && !this.isChild();
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
		this.setSheared(true);
		this.woolTimer = this.isAlbino() ? 16000 : 24000;
		int i = 1 + this.rand.nextInt(fortune + 1);
		int meta = this.isAlbino() ? 0 : 13;
		List<ItemStack> ret = new ArrayList<ItemStack>();
		for (int j = 0; j < i; ++j) ret.add(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, meta));
		this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
		return ret;
	}

	@Override
	public boolean isDriven() {
		return true;
	}

	@Override
	public int getTameLevel() {
		return 2;
	}
}