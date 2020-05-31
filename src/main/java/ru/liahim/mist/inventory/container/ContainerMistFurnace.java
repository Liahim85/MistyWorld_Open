package ru.liahim.mist.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerMistFurnace extends Container {
	private final IInventory tileFurnace;
	private int[] cookTime = new int[2];
	private int[] totalCookTime = new int[2];
	private int[] furnaceBurnTime = new int[2];
	private int[] currentItemBurnTime = new int[2];
	private int[] ashTimer = new int[2];
	private int[] temperarure = new int[11];
	private boolean close = false;

	public ContainerMistFurnace(InventoryPlayer playerInventory, IInventory furnaceInventory) {
        this.tileFurnace = furnaceInventory;
        this.addSlotToContainer(new SlotFurnaceInput(furnaceInventory, 0, 71, 46));
        this.addSlotToContainer(new SlotFurnaceInput(furnaceInventory, 1, 89, 46));
        this.addSlotToContainer(new SlotMistFurnaceOutput(playerInventory.player, furnaceInventory, 2, 26, 46));
        this.addSlotToContainer(new SlotMistFurnaceOutput(playerInventory.player, furnaceInventory, 3, 134, 46));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendAllWindowProperties(this, this.tileFurnace);
	}

	@Override
	public void detectAndSendChanges() {
		super.detectAndSendChanges();
		for (int i = 0; i < this.listeners.size(); ++i) {
			IContainerListener icontainerlistener = this.listeners.get(i);

			for (int j = 0; j < 2; ++j) {
				if (this.furnaceBurnTime[j] != this.tileFurnace.getField(j)) {
					icontainerlistener.sendWindowProperty(this, j, this.tileFurnace.getField(j));
				}
				if (this.currentItemBurnTime[j] != this.tileFurnace.getField(j + 2)) {
					icontainerlistener.sendWindowProperty(this, j + 2, this.tileFurnace.getField(j + 2));
				}
				if (this.cookTime[j] != this.tileFurnace.getField(j + 4)) {
					icontainerlistener.sendWindowProperty(this, j + 4, this.tileFurnace.getField(j + 4));
				}
				if (this.totalCookTime[j] != this.tileFurnace.getField(j + 6)) {
					icontainerlistener.sendWindowProperty(this, j + 6, this.tileFurnace.getField(j + 6));
				}
				if (this.ashTimer[j] != this.tileFurnace.getField(j + 8)) {
					icontainerlistener.sendWindowProperty(this, j + 8, this.tileFurnace.getField(j + 8));
				}
			}
			for (int j = 0; j < 11; ++j) {
				if (this.temperarure[j] != this.tileFurnace.getField(j + 10)) {
					icontainerlistener.sendWindowProperty(this, j + 10, this.tileFurnace.getField(j + 10));
				}
			}
			if (this.close != (this.tileFurnace.getField(21) == 0)) {
				icontainerlistener.sendWindowProperty(this, 21, this.tileFurnace.getField(21));
			}
		}

		for (int i = 0; i < 2; ++i) {
			this.furnaceBurnTime[i] = this.tileFurnace.getField(i);
			this.currentItemBurnTime[i] = this.tileFurnace.getField(i + 2);
			this.cookTime[i] = this.tileFurnace.getField(i + 4);
			this.totalCookTime[i] = this.tileFurnace.getField(i + 6);
			this.ashTimer[i] = this.tileFurnace.getField(i + 8);
		}
		for (int i = 0; i < 11; ++i) {
			this.temperarure[i] = this.tileFurnace.getField(i + 10);
		}
		this.close = this.tileFurnace.getField(21) == 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		this.tileFurnace.setField(id, data);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.tileFurnace.isUsableByPlayer(player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.tileFurnace.closeInventory(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (index == 2 || index == 3) {
				if (!this.mergeItemStack(itemstack1, 4, 40, true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(itemstack1, itemstack);
			} else if (index > 3) {
				if (TileEntityFurnace.isItemFuel(itemstack1) || !FurnaceRecipes.instance().getSmeltingResult(itemstack1).isEmpty()) {
					if (!this.mergeItemStack(itemstack1, 0, 2, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index > 3 && index <= 30) {
					if (!this.mergeItemStack(itemstack1, 31, 40, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= 31 && index < 40 && !this.mergeItemStack(itemstack1, 4, 31, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 4, 40, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, itemstack1);
		}

		return itemstack;
	}
}