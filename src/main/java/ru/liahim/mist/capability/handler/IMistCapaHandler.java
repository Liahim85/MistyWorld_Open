package ru.liahim.mist.capability.handler;

import ru.liahim.mist.capability.MistCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IMistCapaHandler extends IItemHandlerModifiable {

	public boolean isItemValidForSlot(int slot, ItemStack stack, EntityPlayer player);

	/**Used internally to prevent equip/unequip events from triggering when they shouldn't.*/
	public boolean isMaskBlocked();
	public void setMaskBlock(boolean blockEvents);

	/**Used internally for syncing. Indicates if the inventory has changed since last sync.*/
	boolean isMaskChanged();
	boolean isGlobalChanged();
	void setMaskChanged(boolean changed, boolean global);

	public default ItemStack getMask() {
		return getStackInSlot(0);
	}

	public void setPlayer(EntityPlayer player);

	/** Pollution */
	public int getPollution();
	public void setPollution(int pollution);
	public void addPollution(int pollution);

	/** Intoxication */
	public int getToxic();
	public void setToxic(int toxic);
	public void addToxic(int toxic);

	public static IMistCapaHandler getHandler(EntityPlayer player) {
		IMistCapaHandler handler = player.getCapability(MistCapability.CAPABILITY_MIST, null);
		handler.setPlayer(player);
		return handler;
	}
}