package ru.liahim.mist.inventory.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public abstract class GuiItemContainer extends GuiContainer {

	protected final InventoryPlayer playerInventory;
	protected int mouseX, mouseY;
	protected boolean isItem;

	public GuiItemContainer(Container inventorySlots, InventoryPlayer playerInventory, boolean isItem) {
		super(inventorySlots);
		this.isItem = isItem;
		this.playerInventory = playerInventory;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawSlot(Slot slot) {
		super.drawSlot(slot);
		if (this.isItem && slot.inventory == this.playerInventory && slot.getSlotIndex() == this.playerInventory.currentItem &&
				!this.isPointInRegion(slot.xPos, slot.yPos, 16, 16, this.mouseX, this.mouseY)) {
	        GlStateManager.disableLighting();
	        GlStateManager.disableDepth();
	        int j1 = slot.xPos;
	        int k1 = slot.yPos;
	        GlStateManager.colorMask(true, true, true, false);
	        this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
	        GlStateManager.colorMask(true, true, true, true);
	        GlStateManager.enableLighting();
	        GlStateManager.enableDepth();
		}
	}

	@Override
	protected void renderHoveredToolTip(int mouseX, int mouseY) {
		if (this.hoveredSlot != null && (!this.isItem || this.hoveredSlot.inventory != this.playerInventory || this.hoveredSlot.getSlotIndex() != this.playerInventory.currentItem)) {
			super.renderHoveredToolTip(mouseX, mouseY);
		}
	}
}