package ru.liahim.mist.inventory.container;

import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMask extends ContainerPlayer {

	public IMistCapaHandler mask;
	private final EntityPlayer player;

	public ContainerMask(InventoryPlayer playerInv, boolean localWorld, EntityPlayer player) {
		super(playerInv, localWorld, player);
		this.player = player;
		mask = IMistCapaHandler.getHandler(player);
		/**Mask slot*/
		this.addSlotToContainer(new SlotMask(player, mask, 0, 8, -16));
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack finalStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			finalStack = slotStack.copy();
			EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(finalStack);

			if (index == 0) {
				if (!this.mergeItemStack(slotStack, 9, 45, true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(slotStack, finalStack);
			} else if (index >= 1 && index < 5) {
				if (!this.mergeItemStack(slotStack, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 5 && index < 9) {
				if (!this.mergeItemStack(slotStack, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			}
			// mask -> inv
			else if (index == 46) {
				if (!this.mergeItemStack(slotStack, 9, 45, false)) {
					return ItemStack.EMPTY;
				}
			}
			// inv -> armor
			else if (entityequipmentslot.getSlotType() == EntityEquipmentSlot.Type.ARMOR
					&& !this.inventorySlots.get(8 - entityequipmentslot.getIndex()).getHasStack()) {
				int i = 8 - entityequipmentslot.getIndex();
				if (!this.mergeItemStack(slotStack, i, i + 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			// inv -> offhand
			else if (entityequipmentslot == EntityEquipmentSlot.OFFHAND
					&& !this.inventorySlots.get(45).getHasStack()) {
				if (!this.mergeItemStack(slotStack, 45, 46, false)) {
					return ItemStack.EMPTY;
				}
			}
			// inv -> mask
			else if (IMask.isMask(slotStack)) {
				if (IMask.canEquip(slotStack, player) && !this.inventorySlots.get(46).getHasStack() &&
						!this.mergeItemStack(slotStack, 46, 47, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 9 && index < 36) {
				if (!this.mergeItemStack(slotStack, 36, 45, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 36 && index < 45) {
				if (!this.mergeItemStack(slotStack, 9, 36, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(slotStack, 9, 45, false)) {
				return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else slot.onSlotChanged();

			if (slotStack.getCount() == finalStack.getCount()) {
				return ItemStack.EMPTY;
			}
			
			ItemStack slotStack2 = slot.onTake(playerIn, slotStack);

			if (index == 0) {
				playerIn.dropItem(slotStack2, false);
			}
		}
		return finalStack;
	}
}