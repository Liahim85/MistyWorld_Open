package ru.liahim.mist.inventory.gui;

import org.lwjgl.opengl.GL11;

import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.network.PacketOpenMaskInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiMaskButton extends GuiButton {
	
	private boolean isMask;
	private GuiContainer gui;

	public GuiMaskButton(int buttonId, int left, int top, int width, int height, boolean isMask, GuiContainer gui, String buttonText) {
		super(buttonId, left, top, width, height, buttonText);
		this.isMask = isMask;
		this.gui = gui;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		return super.mousePressed(mc, mouseX, mouseY);
	}

	@Override
	public void drawButton(Minecraft mc, int xx, int yy, float partialTicks) {
		if (this.visible) {
			this.x = this.gui.getGuiLeft() + 8;
			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(GuiMask.slotMask);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = xx >= this.x && yy >= this.y && xx < this.x + this.width && yy < this.y + this.height;
			int k = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			if (!this.isMask) {
				if (k == 1) this.drawTexturedModalRect(this.x - 1, this.y - 1, 24, 167, 18, 8);
				else this.drawTexturedModalRect(this.x - 1, this.y - 1, 24, 175, 18, 8);
			} else if (k == 2) this.drawTexturedModalRect(this.x - 1, this.y - 1, 24, 183, 20, 8);
			this.mouseDragged(mc, xx, yy);
		}
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (!this.isMask && this.hovered && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
			ItemStack stack = ((EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity()).inventory.getItemStack();
			if (!stack.isEmpty() && stack.getItem() instanceof IMask) {
				PacketHandler.INSTANCE.sendToServer(new PacketOpenMaskInventory(stack));
				((EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity()).inventory.setItemStack(ItemStack.EMPTY);
			}
		}
    }
}