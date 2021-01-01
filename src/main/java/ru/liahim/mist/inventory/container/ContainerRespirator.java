package ru.liahim.mist.inventory.container;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.liahim.mist.api.MistTags;

public class ContainerRespirator extends Container {

	private InventoryBasic respiratorInv = new InventoryBasic("", true, 1);
	private int respSlotNum;
	private final EntityPlayer player;

	public ContainerRespirator(InventoryPlayer inventory) {
		this.player = inventory.player;
		this.respSlotNum = player.inventory.currentItem;
		layoutContainer(inventory);
		loadFilter();
	}

	private void layoutContainer(InventoryPlayer inventory) {
		this.addSlotToContainer(new SlotRespirator(this.respiratorInv, 0, 80, 35, player));
		int row;
		int col;
		for (row = 0; row < 9; ++row) {
        	if (row == this.respSlotNum) this.addSlotToContainer(new SlotForShowOnly(inventory, row, 8 + row * 18, 142));
        	else this.addSlotToContainer(new Slot(inventory, row, 8 + row * 18, 142));
        }
        for (row = 0; row < 3; ++row) {
            for (col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
	}

	private void loadFilter() {
		if (!player.world.isRemote) {
			if (!player.inventory.getStackInSlot(this.respSlotNum).isEmpty()) {
				ItemStack stack = player.inventory.getStackInSlot(this.respSlotNum);
				if (stack.hasTagCompound() && stack.getTagCompound().hasKey("MistFilter")) {
					NBTTagCompound tag = stack.getTagCompound().getCompoundTag("MistFilter");
					ItemStack filter = new ItemStack(tag);
					if (filter.getCount() == 1) this.respiratorInv.setInventorySlotContents(0, filter);
					else this.respiratorInv.setInventorySlotContents(0, ItemStack.EMPTY);
				}
			}
		}
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		saveFilter(this.respiratorInv.getStackInSlot(0));
	}

	private void saveFilter(ItemStack filter) {
		ItemStack stack = player.inventory.getStackInSlot(this.respSlotNum);
		if (!stack.isEmpty()) {
			if (!filter.isEmpty()) {
				NBTTagCompound tag = filter.serializeNBT();
				if(!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setTag(MistTags.nbtFilterTag, tag);
				stack.getTagCompound().setInteger(MistTags.nbtFilterDurabilityTag, filter.getItemDamage());
			} else if (stack.hasTagCompound() && stack.getTagCompound().hasKey(MistTags.nbtFilterTag)) {
				stack.getTagCompound().removeTag(MistTags.nbtFilterTag);
				stack.getTagCompound().setInteger(MistTags.nbtFilterDurabilityTag, 0);
			}
		}
	}

	@Override
	@Nullable
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack finalStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			finalStack = slotStack.copy();
			if (index == 0) {
				if (!this.mergeItemStack(slotStack, 1, inventorySlots.size(), false)) return ItemStack.EMPTY;
			} else {
				Slot filterSlot = this.inventorySlots.get(0);
				if (filterSlot.getStack().isEmpty() && filterSlot.isItemValid(slotStack)) {
					filterSlot.putStack(slotStack.splitStack(1));
				}
			}
			if (slotStack.getCount() <= 0) slot.putStack(ItemStack.EMPTY);
			else slot.onSlotChanged();

			if (slotStack.getCount() == finalStack.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, slotStack);
		}
		return finalStack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
}