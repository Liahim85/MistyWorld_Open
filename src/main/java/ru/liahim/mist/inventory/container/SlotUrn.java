package ru.liahim.mist.inventory.container;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.liahim.mist.api.block.MistBlocks;

public class SlotUrn extends Slot {

	public SlotUrn(IInventory inventory, int index, int xPosition, int yPosition) {
		super(inventory, index, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
        return Block.getBlockFromItem(stack.getItem()) != MistBlocks.URN && !stack.getItem().hasContainerItem(stack);
    }
}