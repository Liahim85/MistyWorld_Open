package ru.liahim.mist.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public abstract class EntityGender extends EntityAnimalMist {
	
	protected static final DataParameter<Boolean> FEMALE = EntityDataManager.<Boolean>createKey(EntityGender.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Boolean> PREGNANT = EntityDataManager.<Boolean>createKey(EntityGender.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Byte> PREGNANT_STAGE = EntityDataManager.<Byte>createKey(EntityGender.class, DataSerializers.BYTE);
	private NBTTagList childTagList = new NBTTagList();
	private long conceptionTime;
	private int pregnantStage;

	public EntityGender(World world) {
		super(world);
	}

	/*@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public String getName() {
		String name;
		if (this.hasCustomName()) {
			name = this.getCustomNameTag();
		} else {
			String s = EntityList.getEntityString(this);
			if (s == null) s = "generic";
			name = I18n.format("entity." + s + ".name");
		}
		String gender = this.isFemale() ? TextFormatting.LIGHT_PURPLE + "female" : TextFormatting.AQUA + "male";
		return (!name.isEmpty() ? name + " - " : "") + gender + (this.isPregnant() ? " \u25cf" : "");
	}*/

	public String getGenderTag() {
		if (this.isPregnant()) {
			if (this.pregnantStage == 0) return "\u25cb";
			else if (this.pregnantStage == 1) return "\u25d0";
			else return "\u25cf";
		}
		return "";
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(FEMALE, false);
		this.dataManager.register(PREGNANT, false);
		this.dataManager.register(PREGNANT_STAGE, (byte)0);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (PREGNANT_STAGE.equals(key) && this.world.isRemote) {
			this.pregnantStage = this.dataManager.get(PREGNANT_STAGE);
		}
		super.notifyDataManagerChange(key);
	}

	private void setPregnantStage(int stage) {
		this.dataManager.set(PREGNANT_STAGE, (byte)stage);
	}

	private int getPregnantStage() {
		return this.dataManager.get(PREGNANT_STAGE);
	}

	private void setConceptionTime(long time) {
		this.conceptionTime = time;
		this.pregnantStage = calculatePregnantStage();
		this.setPregnantStage(this.pregnantStage);
	}

	private int calculatePregnantStage() {
		return MathHelper.clamp(MathHelper.floor((this.world.getTotalWorldTime() - this.conceptionTime) / (this.getPregnantTime() / 3)), 0, 2);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote && this.isPregnant()) {
			int i = calculatePregnantStage();
			if (this.pregnantStage != i) {
				this.pregnantStage = i;
				this.setPregnantStage(this.pregnantStage);
			}
			if (this.world.getTotalWorldTime() - this.conceptionTime > this.getPregnantTime()) {
				this.spawnBaby();
			}
		}
	}

	@Override
	public boolean canMateWith(EntityAnimal otherAnimal) {
		if (otherAnimal == this) return false;
		else if (otherAnimal.getClass() != this.getClass()) return false;
		else if (((EntityGender)otherAnimal).isFemale() == this.isFemale()) return false;
		else if (((EntityGender)otherAnimal).isPregnant() || this.isPregnant()) return false;
		else return this.isInLove() && otherAnimal.isInLove();
	}

	public boolean isFemale() {
		return this.dataManager.get(FEMALE);
	}

	public void setFemale(boolean female) {
		this.dataManager.set(FEMALE, female);
	}

	public boolean isPregnant() {
		return this.isFemale() && this.dataManager.get(PREGNANT);
	}

	public void setPregnant(boolean pregnant) {
		this.dataManager.set(PREGNANT, pregnant);
	}

	public void setChild(NBTTagList childTagList) {
		if (this.isFemale()) {
			this.childTagList = childTagList;
			this.setConceptionTime(this.world.getTotalWorldTime());
			this.setPregnant(true);
		}
	}

	private void spawnBaby() {
		if (this.canBirth()) {
			if (this.isPregnant()) {
				for (int i = 0; i < this.childTagList.tagCount(); ++i) {
					EntityAgeable child = (EntityAgeable)EntityList.createEntityFromNBT(this.childTagList.getCompoundTagAt(i), this.world);
					child.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
					child.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(child)), (IEntityLivingData)null);
					this.world.spawnEntity(child);
				}
				this.childTagList = new NBTTagList();
				this.setGrowingAge(6000);
				this.getNavigator().clearPath();
			}
			this.setPregnant(false);
		} else this.setConceptionTime(this.world.getTotalWorldTime() - this.getPregnantTime() + 100);
	}

	protected boolean canBirth() {
		return this.onGround && !this.inPortal && !this.isInWater() && !this.isInLava() &&
				!this.isSwingInProgress && !this.isJumping && !this.isInWeb && (!this.isBurning() || this.isImmuneToFire);
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getItem() == Items.SPAWN_EGG) {
			if (!this.world.isRemote) {
				Class<? extends Entity> oclass = EntityList.getClass(ItemMonsterPlacer.getNamedIdFrom(stack));
				if (oclass != null && this.getClass() == oclass) {
					EntityAgeable child = this.createChild(this);
					if (child != null) {
						child.setGrowingAge(-24000);
						child.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
						child.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(child)), (IEntityLivingData)null);
						this.world.spawnEntity(child);
						if (stack.hasDisplayName()) child.setCustomNameTag(stack.getDisplayName());
						if (!player.capabilities.isCreativeMode) stack.shrink(1);
					}
				}
			}
			return true;
		} else return super.processInteract(player, hand);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("Female", this.isFemale());
		compound.setBoolean("Pregnant", this.isPregnant());
		compound.setLong("ConceptionTime", this.conceptionTime);
		compound.setTag("Child", this.childTagList);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setFemale(compound.getBoolean("Female"));
		this.setPregnant(compound.getBoolean("Pregnant"));
		this.setConceptionTime(compound.getLong("ConceptionTime"));
		this.childTagList = compound.getTagList("Child", 10);
	}

	@Override
	@Nullable
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
		this.setFemale(initFemale());
		return super.onInitialSpawn(difficulty, livingdata);
	}

	protected abstract long getPregnantTime();
	public abstract int getChildCount();
	public abstract boolean initFemale();
}