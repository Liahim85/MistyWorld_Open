package ru.liahim.mist.inventory.gui;

import java.io.IOException;

import ru.liahim.mist.inventory.container.ContainerFirePitPot;
import ru.liahim.mist.network.PacketFirePitFillPot;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiCampfirePotInfo extends GuiContainer {

	private final TileEntityCampfire te;
	private static final ResourceLocation BACKGROUND = new ResourceLocation("mist:textures/gui/container/pot_info.png");
	private int uni;

	public GuiCampfirePotInfo(TileEntityCampfire te) {
		super(new ContainerFirePitPot(te));
		this.te = te;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.xSize = 126;
		this.ySize = 102;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		this.buttonList.clear();
		this.buttonList.add(new GuiMistButton(1, this.guiLeft + 92, this.guiTop + 68, 26, 26, 0, 115, "", BACKGROUND));
        this.uni = this.fontRenderer.getUnicodeFlag() ? 0 : 1;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		for (int i = 0; i < this.buttonList.size(); ++i) {
			this.buttonList.get(i).drawButton(this.mc, mouseX, mouseY, partialTicks);
		}
		String name = I18n.format("gui.mist.pot_info");
		this.fontRenderer.drawString(name, this.width / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 5 + uni, 4210752);
		int i = 0;
		float persent;
		float other = 100.0F;
		for (ItemStack stack : te.getMainFood()) {
			if (!stack.isEmpty()) {
				mc.getRenderItem().renderItemAndEffectIntoGUI(stack, this.guiLeft + 13 + (28 * i), this.guiTop + 23);
				persent = te.getFoodPercent()[i] / te.getFinalAmount() * 100;
				other -= persent;
				name = persent >= 100 ? "100%" : String.format("%(.1f", persent) + "%";
				this.fontRenderer.drawString(name, this.guiLeft + 21 + (28 * i) - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 46 + uni, 4210752);
			} else break;
			++i;
		}
		other = Math.max(other, 0);
		if (te.getFinalAmount() > 0) {
			name = I18n.format("gui.mist.pot_impurity");
			this.fontRenderer.drawString(name, this.guiLeft + 49 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 57 + uni, 4210752);
			name = String.format("%(.1f", other) + "%";
			this.fontRenderer.drawString(name, this.guiLeft + 105 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 57 + uni, 4210752);
		}
		name = I18n.format("gui.mist.pot_saturation");
		this.fontRenderer.drawString(name, this.guiLeft + 49 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 68 + uni, 4210752);
		// TODO
		i = 0;
		for (ItemStack stack : te.getMainFood()) {
			if (!stack.isEmpty()) {
				if (this.isPointInRegion(13 + (28 * i), 23, 16, 16, mouseX, mouseY)) {
					this.renderToolTip(stack, mouseX, mouseY);
				}
			} else break;
			++i;
		}
	}

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 90, 0, this.ySize, this.getProgressScaled(82), 4);
		float total = Math.min(20, this.te.getFinalAmount() / this.te.getVolum());
		if (total >= 1) total = (int)total;
		for (int i = 0; i < 10; ++i) {
			if (total > 1) {
				this.drawTexturedModalRect(this.guiLeft + 80 - 8 * i, this.guiTop + 79, 18, this.ySize + 4, 9, 9);
			} else if (total == 1) {
				this.drawTexturedModalRect(this.guiLeft + 80 - 8 * i, this.guiTop + 79, 9, this.ySize + 4, 9, 9);
				break;
			} else if (total > 0 && i == 0) {
				this.drawTexturedModalRect(this.guiLeft + 80 - 8 * i, this.guiTop + 79, 0, this.ySize + 4, 9, 9);
				break;
			}
			total -= 2;
		}
	}

	private int getProgressScaled(int pixels) {
		return this.te.getField(0) * pixels / 10000;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 1:
			this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
			PacketHandler.INSTANCE.sendToServer(new PacketFirePitFillPot(this.te.getPos(), 0, 0));
			break;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {}
}