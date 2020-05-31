package ru.liahim.mist.inventory.container;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;

public class SlotFurnaceInput extends Slot {

	public SlotFurnaceInput(IInventory inventoryIn, int index, int xPosition, int yPosition) {
		super(inventoryIn, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return !FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty() || TileEntityFurnace.isItemFuel(stack);
	}
}