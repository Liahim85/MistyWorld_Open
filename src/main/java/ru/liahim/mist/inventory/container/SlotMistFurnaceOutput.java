package ru.liahim.mist.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import ru.liahim.mist.init.ModAdvancements;

public class SlotMistFurnaceOutput extends SlotFurnaceOutput {

	private final EntityPlayer player;

	public SlotMistFurnaceOutput(EntityPlayer player, IInventory inventory, int slotIndex, int xPosition, int yPosition) {
		super(player, inventory, slotIndex, xPosition, yPosition);
		this.player = player;
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		super.onCrafting(stack);
		if (this.player != null && this.player instanceof EntityPlayerMP) {
			ModAdvancements.ITEM_SMELTED_MIST.trigger((EntityPlayerMP) this.player, stack);
		}
	}
}