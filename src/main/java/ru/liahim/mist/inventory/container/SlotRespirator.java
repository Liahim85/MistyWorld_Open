package ru.liahim.mist.inventory.container;

import ru.liahim.mist.api.item.IFilter;
import ru.liahim.mist.init.ModAdvancements;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotRespirator extends Slot {

	private final EntityPlayer player;

	public SlotRespirator(IInventory inventory, int index, int xPosition, int yPosition, EntityPlayer player) {
		super(inventory, index, xPosition, yPosition);
		this.player = player;
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof IFilter;
	}

	@Override
	public void putStack(ItemStack stack) {
		if (!stack.isEmpty() && stack.getItem() instanceof IFilter && player instanceof EntityPlayerMP) {
			ModAdvancements.PUT_FILTER.trigger((EntityPlayerMP) player, stack);
		}
		super.putStack(stack);
	}
}