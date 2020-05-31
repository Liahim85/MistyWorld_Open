package ru.liahim.mist.inventory.container;

import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.SlotItemHandler;

public class SlotMask extends SlotItemHandler {

	int maskSlot;
	EntityPlayer player;

	public SlotMask(EntityPlayer player, IMistCapaHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
		this.maskSlot = index;
		this.player = player;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return ((IMistCapaHandler)this.getItemHandler()).isItemValidForSlot(maskSlot, stack, player);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return this.getHasStack() && IMask.canUnequip(this.getStack(), player);
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack) {
		if (!this.getHasStack() && !((IMistCapaHandler)getItemHandler()).isMaskBlocked()) {
			IMask.onUnequipped(stack, player);
		}
		return super.onTake(player, stack);
	}

	@Override
	public void putStack(ItemStack stack) {
		if (this.getHasStack() && !((IMistCapaHandler)getItemHandler()).isMaskBlocked()) {
			IMask.onUnequipped(getStack(), player);
		}
		super.putStack(stack);
		if (this.getHasStack() && !((IMistCapaHandler)getItemHandler()).isMaskBlocked()) {
			IMask.onEquipped(getStack(), player);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getSlotTexture() {
		return "mist:items/empty_mask";
	}

	@Override
	public int getSlotStackLimit() {
		return 1;
	}
}