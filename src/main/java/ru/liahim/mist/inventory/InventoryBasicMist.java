package ru.liahim.mist.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryBasicMist extends InventoryBasic {

	private final int slotLimit;

	public InventoryBasicMist(String title, boolean customName, int slotCount, int slotLimit) {
		super(title, customName, slotCount);
		this.slotLimit = slotLimit;
	}

	@Override
	public int getInventoryStackLimit() {
		return this.slotLimit;
	}

	public NonNullList<ItemStack> getItems() {
		return this.inventoryContents;
	}

	public void setItems(NonNullList<ItemStack> content) {
		ItemStack stack;
		for (int i = 0; i < content.size() && i < this.inventoryContents.size(); ++i) {
			stack = content.get(i);
			this.inventoryContents.set(i, stack);
	        if (!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
	            stack.setCount(this.getInventoryStackLimit());
	        }
		}
		this.markDirty();
	}
}