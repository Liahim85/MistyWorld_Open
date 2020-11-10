package ru.liahim.mist.entity;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import ru.liahim.mist.common.Mist;

public abstract class AbstractMistMount extends EntityAlbino implements IInventoryChangedListener, IJumpingMount {

	private static final DataParameter<Boolean> SADDLED = EntityDataManager.<Boolean>createKey(AbstractMistMount.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> SPRINT_TIME = EntityDataManager.<Integer>createKey(AbstractMistMount.class, DataSerializers.VARINT);
	private boolean sprinting;
	private int sprintTime;
	public ContainerHorseChest horseChest;
    protected boolean jump;
    protected boolean horseJumping;

	public AbstractMistMount(World world) {
		super(world);
        this.stepHeight = 1.0F;
        this.initHorseChest();
	}

    @Override
	protected void entityInit() {
        super.entityInit();
        this.dataManager.register(SADDLED, false);
        this.dataManager.register(SPRINT_TIME, 0);
	}

	public boolean canBeSaddled() {
		return this.isTamed() && this.revengeTimer == 0;
	}

	public boolean isSaddled() {
		return this.dataManager.get(SADDLED);
	}

	public void setSaddled(boolean saddled) {
        this.dataManager.set(SADDLED, saddled);
	}

	public void setHorseJumping(boolean jumping) {
		this.horseJumping = jumping;
	}

	public void setSprintTime(int time) {
        this.dataManager.set(SPRINT_TIME, time);
	}

	public boolean isHorseJumping() {
		return this.horseJumping;
	}

	public void sprint(boolean sprint) {
		this.sprinting = sprint;
		this.setSprintTime(this.sprintTime);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		if (SPRINT_TIME.equals(key) && this.world.isRemote) {
			this.sprintTime = this.dataManager.get(SPRINT_TIME);
		}
		super.notifyDataManagerChange(key);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.sprinting && this.sprintTime > 0) {
			this.sprintTime = Math.max(this.sprintTime - 2, 0);
			if (this.sprintTime == 0) this.setSprintTime(0);
		}
	}

	@Override
	public void travel(float strafe, float vertical, float forward) {
		if (this.isBeingRidden() && this.canBeSteered() && this.isSaddled()) {
			EntityLivingBase passenger = (EntityLivingBase) this.getControllingPassenger();
			forward = passenger.moveForward * getSpeedMultipler();
			this.prevRotationYaw -= passenger.moveStrafing * 3;
			this.rotationYaw = this.prevRotationYaw;
			this.rotationPitch = 0;
			this.setRotation(this.rotationYaw, this.rotationPitch);
			this.renderYawOffset = this.rotationYaw;
			this.rotationYawHead = this.rotationYaw;

			if (forward <= 0.0F) forward *= 0.5F;

			if (this.jump && !this.isHorseJumping() && this.onGround) {
				this.jump = false;
				this.motionY = this.getHorseJumpStrength();
				if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
					this.motionY += (this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
				}
				this.setHorseJumping(true);
				this.isAirBorne = true;

				if (forward > 0.0F) {
					float f = MathHelper.sin(this.rotationYaw * 0.017453292F);
					float f1 = MathHelper.cos(this.rotationYaw * 0.017453292F);
					this.motionX += -0.4F * f;
					this.motionZ += 0.4F * f1;
					this.playSound(SoundEvents.ENTITY_HORSE_JUMP, 0.4F, 1.0F);
				}
			}

			this.jumpMovementFactor = this.getAIMoveSpeed() * 0.1F;

			if (this.sprinting) {
				if (this.sprintTime++ > this.getMaxSprintTime()) {
					this.sprint(false);
					passenger.setSprinting(false);
				}
			}

			if (this.canPassengerSteer()) {
				float f = (float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
				if (passenger.isSprinting()) {
					if (this.sprinting) f *= getSpintMultipler();
					else this.sprint(true);
				}
				this.setAIMoveSpeed(f);
				super.travel(strafe, vertical, forward);
			} else if (passenger instanceof EntityPlayer) {
				this.motionX = 0.0D;
				this.motionY = 0.0D;
				this.motionZ = 0.0D;
			}

			if (this.onGround) this.setHorseJumping(false);

			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d1 = this.posX - this.prevPosX;
			double d0 = this.posZ - this.prevPosZ;
			float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0);

			if (f2 > 1.0F) f2 = 1.0F;

			this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
			this.limbSwing += this.limbSwingAmount;
		} else {
			this.jumpMovementFactor = 0.02F;
			super.travel(strafe, vertical, forward);
		}
	}

	public float getSpeedMultipler() {
		return 0.5F;
	}

	public float getSpintMultipler() {
		return 1.5F;
	}

	public double getHorseJumpStrength() {
		return 0.6;
	}

	public int getMaxSprintTime() {
		return 300;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setJumpPower(int jumpPower) {
		if (this.canJump() && jumpPower > 0) this.jump = true;
	}

	@Override
	public boolean canJump() {
		return this.isSaddled();
	}

	@Override
	public void handleStartJump(int jumpPower) {
		if (this.canJump() && jumpPower > 0) this.jump = true;
	}

	@Override
	public void handleStopJump() {}

	@Override
	public boolean canBeSteered() {
		return this.getControllingPassenger() instanceof EntityLivingBase;
	}

	@Override
	@Nullable
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
	}

	public int getInventorySize() {
		return 2;
	}

	@Override
	protected void playStepSound(BlockPos pos, Block block) {
		if (!block.getDefaultState().getMaterial().isLiquid()) {
			SoundType soundtype = block.getSoundType();
			if (this.world.getBlockState(pos.up()).getBlock() == Blocks.SNOW_LAYER) {
				soundtype = Blocks.SNOW_LAYER.getSoundType();
			}
			this.playSound(SoundEvents.ENTITY_COW_STEP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (distance > 1.0F) this.playSound(SoundEvents.ENTITY_HORSE_LAND, 0.4F, 1.0F);
		int i = MathHelper.ceil((distance * 0.5F - 3.0F) * damageMultiplier);
		if (i > 0) {
			this.attackEntityFrom(DamageSource.FALL, i);
			if (this.isBeingRidden()) {
				for (Entity entity : this.getRecursivePassengers()) {
					entity.attackEntityFrom(DamageSource.FALL, i);
				}
			}

			IBlockState iblockstate = this.world.getBlockState( new BlockPos(this.posX, this.posY - 0.2D - this.prevRotationYaw, this.posZ));
			Block block = iblockstate.getBlock();

			if (iblockstate.getMaterial() != Material.AIR && !this.isSilent()) {
				SoundType soundtype = block.getSoundType();
				this.world.playSound((EntityPlayer) null, this.posX, this.posY, this.posZ, soundtype.getStepSound(), this.getSoundCategory(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
			}
		}
	}

	public void openGUI(EntityPlayer player) {
		if ((!this.isBeingRidden() || this.isPassenger(player)) && this.isTamed()) {
			this.horseChest.setCustomName(this.getName());
			player.openGui(Mist.MODID, 8, world, this.getEntityId(), 0, 0);
		}
	}

	protected void initHorseChest() {
		ContainerHorseChest chest = this.horseChest;
		this.horseChest = new ContainerHorseChest("HorseChest", this.getInventorySize());
		this.horseChest.setCustomName(this.getName());
		if (chest != null) {
			chest.removeInventoryChangeListener(this);
			int i = Math.min(chest.getSizeInventory(), this.horseChest.getSizeInventory());
			for (int j = 0; j < i; ++j) {
				ItemStack stack = chest.getStackInSlot(j);
				if (!stack.isEmpty()) {
					this.horseChest.setInventorySlotContents(j, stack.copy());
				}
			}
		}
		this.horseChest.addInventoryChangeListener(this);
		this.updateHorseSlots();
		this.itemHandler = new InvWrapper(this.horseChest);
	}

	protected void updateHorseSlots() {
		if (!this.world.isRemote) {
			this.setSaddled(!this.horseChest.getStackInSlot(0).isEmpty() && this.canBeSaddled());
		}
	}

	@Override
	public void onInventoryChanged(IInventory invBasic) {
		boolean flag = this.isSaddled();
		this.updateHorseSlots();
		if (this.ticksExisted > 20 && !flag && this.isSaddled()) {
			this.playSound(SoundEvents.ENTITY_HORSE_SADDLE, 0.5F, 1.0F);
		}
	}

	public boolean wearsBag() {
		return false;
	}

	public boolean isBag(ItemStack stack) {
		return Block.getBlockFromItem(stack.getItem()) instanceof BlockChest;
	}

	@Override
	public boolean replaceItemInInventory(int slot, ItemStack stack) {
		int i = slot - 400;
		if (i == 0 && i < 2  && i < this.horseChest.getSizeInventory()) {
			if (i == 0 && stack.getItem() != Items.SADDLE) {
				return false;
			} else if (i != 1 || this.wearsBag() && this.isBag(stack)) {
				this.horseChest.setInventorySlotContents(i, stack);
				this.updateHorseSlots();
				return true;
			} else return false;
		} else {
			int j = slot - 500 + 2;
			if (j >= 2 && j < this.horseChest.getSizeInventory()) {
				this.horseChest.setInventorySlotContents(j, stack);
				return true;
			} else return false;
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		super.onDeath(cause);
		if (!this.world.isRemote) {
			if (!this.world.isRemote && this.isSaddled()) this.dropItem(Items.SADDLE, 1);
			if (this.horseChest != null) {
				for (int i = 0; i < this.horseChest.getSizeInventory(); ++i) {
					ItemStack stack = this.horseChest.getStackInSlot(i);
					if (!stack.isEmpty()) this.entityDropItem(stack, 0);
				}
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
		compound.setInteger("SprintTime", this.sprintTime);
        if (!this.horseChest.getStackInSlot(0).isEmpty()) {
            compound.setTag("SaddleItem", this.horseChest.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        }
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.sprintTime = compound.getInteger("SprintTime");
		if (compound.hasKey("SaddleItem", 10)) {
			ItemStack itemstack = new ItemStack(compound.getCompoundTag("SaddleItem"));
			if (itemstack.getItem() == Items.SADDLE) {
				this.horseChest.setInventorySlotContents(0, itemstack);
			}
		}
		this.updateHorseSlots();
	}

	// FORGE
	private IItemHandler itemHandler = null; // Initialized by initHorseChest above.

	//@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) itemHandler;
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
}