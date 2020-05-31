package ru.liahim.mist.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

public abstract class AbstractMistChestMount extends AbstractMistMount {

	private static final DataParameter<Boolean> DATA_ID_CHEST = EntityDataManager.<Boolean>createKey(AbstractMistChestMount.class, DataSerializers.BOOLEAN);

	public AbstractMistChestMount(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(DATA_ID_CHEST, Boolean.valueOf(false));
	}

	public boolean hasChest() {
		return this.dataManager.get(DATA_ID_CHEST).booleanValue();
	}

	public void setChested(boolean chested) {
		this.dataManager.set(DATA_ID_CHEST, Boolean.valueOf(chested));
	}

	@Override
	public int getInventorySize() {
		return this.hasChest() ? 17 : super.getInventorySize();
	}

	public int getInventoryColumns() {
		return 5;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setBoolean("ChestedHorse", this.hasChest());
		if (this.hasChest()) {
			NBTTagList list = new NBTTagList();
			for (int i = 1; i < this.horseChest.getSizeInventory(); ++i) {
				ItemStack stack = this.horseChest.getStackInSlot(i);
				if (!stack.isEmpty()) {
					NBTTagCompound nbt = new NBTTagCompound();
					nbt.setByte("Slot", (byte) i);
					stack.writeToNBT(nbt);
					list.appendTag(nbt);
				}
			}
			compound.setTag("Items", list);
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		super.readEntityFromNBT(compound);
		this.setChested(compound.getBoolean("ChestedHorse"));
		if (this.hasChest()) {
			NBTTagList list = compound.getTagList("Items", 10);
			this.initHorseChest();
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound nbt = list.getCompoundTagAt(i);
				int j = nbt.getByte("Slot") & 255;
				if (j >= 1 && j < this.horseChest.getSizeInventory()) {
					this.horseChest.setInventorySlotContents(j, new ItemStack(nbt));
				}
			}
		}
		this.updateHorseSlots();
	}

	@Override
	public boolean replaceItemInInventory(int slot, ItemStack stack) {
		if (slot == 499) {
			if (this.hasChest() && stack.isEmpty()) {
				this.setChested(false);
				this.initHorseChest();
				return true;
			}

			if (!this.hasChest() && Block.getBlockFromItem(stack.getItem()) instanceof BlockChest) {
				this.setChested(true);
				this.initHorseChest();
				return true;
			}
		}

		return super.replaceItemInInventory(slot, stack);
	}

	protected boolean isRiddingItem(ItemStack stack) {
    	Item item = stack.getItem();
    	return !this.isBreedingItem(stack) && item != Items.SPAWN_EGG && item != Items.NAME_TAG && (!this.isFemale() || this.isChild() || item != Items.BUCKET) && !(item instanceof ItemShears);
	}

	protected void playChestEquipSound() {
		this.playSound(SoundEvents.ENTITY_DONKEY_CHEST, 1.0F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
	}
}