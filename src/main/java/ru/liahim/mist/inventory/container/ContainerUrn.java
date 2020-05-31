package ru.liahim.mist.inventory.container;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import ru.liahim.mist.inventory.InventoryBasicMist;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnType;

public class ContainerUrn extends Container {

	private final IInventory tileInventory;
	private final InventoryBasicMist itemInventory;
	private int urnSlotNum = -1;
	private final int size = 9;

	public ContainerUrn(@Nullable IInventory inventory, EntityPlayer player) {
		this.tileInventory = inventory;
		if (inventory == null) {
			this.urnSlotNum = player.inventory.currentItem;
			int i = !player.inventory.getStackInSlot(this.urnSlotNum).isEmpty() &&
					UrnType.getType(player.inventory.getStackInSlot(this.urnSlotNum), null).isRare() ? 64 : 16;
			this.itemInventory = new InventoryBasicMist("", true, this.size, i);
		} else this.itemInventory = null;

		if (this.tileInventory != null) {
			inventory.openInventory(player);
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < 3; ++j) {
					this.addSlotToContainer(new SlotUrn(this.tileInventory, j + i * 3, 62 + j * 18, 17 + i * 18));
				}
			}
		} else {
			for (int i = 0; i < 3; ++i) {
				for (int j = 0; j < 3; ++j) {
					this.addSlotToContainer(new SlotUrn(this.itemInventory, j + i * 3, 62 + j * 18, 17 + i * 18));
				}
			}
			this.loadItems(player);
		}

		for (int k = 0; k < 3; ++k) {
			for (int i1 = 0; i1 < 9; ++i1) {
				this.addSlotToContainer(new Slot(player.inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
			}
		}
		for (int l = 0; l < 9; ++l) {
			if (l == this.urnSlotNum) this.addSlotToContainer(new SlotForShowOnly(player.inventory, l, 8 + l * 18, 142));
			else this.addSlotToContainer(new Slot(player.inventory, l, 8 + l * 18, 142));
		}
	}

	private void loadItems(EntityPlayer player) {
		if (!player.world.isRemote && !player.inventory.getStackInSlot(this.urnSlotNum).isEmpty()) {
			ItemStack stack = player.inventory.getStackInSlot(this.urnSlotNum);
			NBTTagCompound tag = stack.getSubCompound("Urn");
			if (tag != null) {
				NonNullList<ItemStack> content = NonNullList.<ItemStack>withSize(this.size, ItemStack.EMPTY);
				ItemStackHelper.loadAllItems(tag, content);
				this.itemInventory.setItems(content);
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack stack1 = slot.getStack();
			stack = stack1.copy();
			if (index < 9) {
				if (!this.mergeItemStack(stack1, 9, 45, true)) return ItemStack.EMPTY;
			} else if (!this.mergeItemStack(stack1, 0, 9, false)) return ItemStack.EMPTY;
			if (stack1.isEmpty()) slot.putStack(ItemStack.EMPTY);
			else slot.onSlotChanged();
			if (stack1.getCount() == stack.getCount()) return ItemStack.EMPTY;
			slot.onTake(player, stack1);
		}
		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.tileInventory == null ? true : this.tileInventory.isUsableByPlayer(player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (this.tileInventory != null) this.tileInventory.closeInventory(player);
		else this.saveContent(player);
	}

	private void saveContent(EntityPlayer player) {
		ItemStack stack = player.inventory.getStackInSlot(this.urnSlotNum);
		if (!stack.isEmpty()) {
			if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
			NBTTagCompound content = stack.getTagCompound().hasKey("Urn") ? (NBTTagCompound) stack.getTagCompound().getTag("Urn") : new NBTTagCompound();
			ItemStackHelper.saveAllItems(content, this.itemInventory.getItems());
			stack.getTagCompound().setTag("Urn", content);
		}
	}

	public IInventory getUrnInventory() {
		return this.tileInventory;
	}
}