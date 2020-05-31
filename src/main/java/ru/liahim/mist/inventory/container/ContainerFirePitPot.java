package ru.liahim.mist.inventory.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerFirePitPot extends Container {
	private final IInventory te;
	private int progress;

	public ContainerFirePitPot(IInventory potInventory) {
		this.te = potInventory;
	}

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		listener.sendAllWindowProperties(this, this.te);
	}

	@Override
	public void detectAndSendChanges() {
		for (int i = 0; i < this.listeners.size(); ++i) {
			IContainerListener icontainerlistener = this.listeners.get(i);
			if (this.progress != this.te.getField(0)) {
				icontainerlistener.sendWindowProperty(this, 0, this.te.getField(0));
			}
		}
		this.progress = this.te.getField(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int id, int data) {
		this.te.setField(id, data);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return this.te.isUsableByPlayer(player);
	}
}