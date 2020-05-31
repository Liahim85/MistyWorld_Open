package ru.liahim.mist.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.AbstractMistChestMount;
import ru.liahim.mist.entity.AbstractMistMount;

public class ContainerMountInventory extends Container {

	private final IInventory horseInventory;
	private final AbstractMistMount horse;

	public ContainerMountInventory(IInventory playerInventory, IInventory horseInventory, final AbstractMistMount horse, EntityPlayer player) {
		this.horseInventory = horseInventory;
		this.horse = horse;
		horseInventory.openInventory(player);
		this.addSlotToContainer(new Slot(horseInventory, 0, 8, 18) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return stack.getItem() == Items.SADDLE && !this.getHasStack() && horse.canBeSaddled();
			}

			@Override
			@SideOnly(Side.CLIENT)
			public boolean isEnabled() {
				return horse.canBeSaddled();
			}
		});

		//Bag
		this.addSlotToContainer(new Slot(horseInventory, 1, 8, 36) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return horse.isBag(stack);
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}

			@Override
			@SideOnly(Side.CLIENT)
			public boolean isEnabled() {
				return horse.wearsBag();
			}
		});

		if (horse instanceof AbstractMistChestMount && ((AbstractMistChestMount) horse).hasChest()) {
			for (int k = 0; k < 3; ++k) {
				for (int l = 0; l < ((AbstractMistChestMount) horse).getInventoryColumns(); ++l) {
					this.addSlotToContainer(new Slot(horseInventory, 2 + l + k * ((AbstractMistChestMount) horse).getInventoryColumns(), 80 + l * 18, 18 + k * 18));
				}
			}
		}

		for (int i1 = 0; i1 < 3; ++i1) {
			for (int k1 = 0; k1 < 9; ++k1) {
				this.addSlotToContainer(new Slot(playerInventory, k1 + i1 * 9 + 9, 8 + k1 * 18, 102 + i1 * 18 + -18));
			}
		}

		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot(playerInventory, j1, 8 + j1 * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.horseInventory.isUsableByPlayer(player) && this.horse.isEntityAlive() && this.horse.getDistance(player) < 8.0F;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < this.horseInventory.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.horseInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			}/* else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack()) {
				if (!this.mergeItemStack(itemstack1, 1, 2, false)) return ItemStack.EMPTY;
			}*/ else if (this.getSlot(0).isItemValid(itemstack1)) {
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) return ItemStack.EMPTY;
			} else if (this.horseInventory.getSizeInventory() <= 1 || !this.mergeItemStack(itemstack1, 1, this.horseInventory.getSizeInventory(), false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
			else slot.onSlotChanged();
		}
		return itemstack;
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.horseInventory.closeInventory(player);
	}
}