package ru.liahim.mist.inventory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiMistButton extends GuiButton {

	private final int textureX;
	private final int textureY;
	protected final ResourceLocation BUTTON_TEXTURES;

	public GuiMistButton(int buttonId, int x, int y, int width, int height, int textureX, int textureY, String buttonText, ResourceLocation res) {
		super(buttonId, x, y, width, height, buttonText);
		this.BUTTON_TEXTURES = res;
		this.textureX = textureX;
		this.textureY = textureY;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, this.textureX, this.textureY + i * this.height, this.width, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
		}
	}

	@Override
	protected int getHoverState(boolean mouseOver) {
		if (!this.enabled) return 2;
		else if (mouseOver) return 1;
		return 0;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		if (this.visible) this.enabled = true;
	}
}