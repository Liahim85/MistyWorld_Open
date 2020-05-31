package ru.liahim.mist.inventory.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.inventory.container.ContainerMistFurnace;
import ru.liahim.mist.network.PacketFurnaceClose;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.tileentity.TileEntityMistFurnace;

@SideOnly(Side.CLIENT)
public class GuiMistFurnace extends GuiContainer {
	private static final ResourceLocation FURNACE_GUI_TEXTURES = new ResourceLocation(Mist.MODID, "textures/gui/container/furnace.png");
	private final InventoryPlayer playerInventory;
	private final TileEntityMistFurnace tileFurnace;
	private boolean close = false;
	private static final int[] xTempCoord = new int[] { 56, 58, 62, 67, 74, 81, 96, 102, 106, 110, 111 };
	private static final int[] yTempCoord = new int[] { 38, 31, 25, 21, 18, 16, 18, 21, 25, 31, 38 };
	private static final int[] xTexCoord = new int[] { 1, 1, 1, 1, 1, 4, 15, 14, 13, 13, 12 };
	private static final int[] yTexCoord = new int[] { 203, 196, 188, 181, 174, 167, 174, 181, 188, 196, 203 };
	private static final int[] xTempSize = new int[] { 9, 8, 8, 7, 6, 14, 6, 7, 8, 8, 9 };
	private static final int[] yTempSize = new int[] { 5, 6, 7, 6, 6, 6, 6, 6, 7, 6, 5 };

	public GuiMistFurnace(InventoryPlayer playerInv, TileEntityMistFurnace furnaceInv) {
		super(new ContainerMistFurnace(playerInv, furnaceInv));
		this.playerInventory = playerInv;
		this.tileFurnace = furnaceInv;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		this.buttonList.add(new GuiMistButton(0, this.guiLeft + 70, this.guiTop + 64, 8, 7, 176, 15, "", FURNACE_GUI_TEXTURES));
		this.buttonList.add(new GuiMistButton(1, this.guiLeft + 70, this.guiTop + 64, 36, 7, 176, 36, "", FURNACE_GUI_TEXTURES));
		this.close = TileEntityMistFurnace.isClose(this.tileFurnace);
		updateButtons();
	}

	private void updateButtons() {
		this.buttonList.get(0).visible = !this.close;
		this.buttonList.get(1).visible = this.close;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = this.tileFurnace.getDisplayName().getUnformattedText();
		this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(FURNACE_GUI_TEXTURES);
		int i = this.guiLeft;
		int j = this.guiTop;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		if (this.close != TileEntityMistFurnace.isClose(this.tileFurnace)) {
			this.close = !this.close;
			updateButtons();
        }
		if (this.close)	this.drawTexturedModalRect(i + 70, j + 64, 176, 36, 36, 7);
		if (TileEntityMistFurnace.isBurning(this.tileFurnace, 0)) {
			int k = this.getBurnScaled(13, 0);
			this.drawTexturedModalRect(i + 72, j + 29 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		if (TileEntityMistFurnace.isBurning(this.tileFurnace, 1)) {
			int k = this.getBurnScaled(13, 1);
			this.drawTexturedModalRect(i + 89, j + 29 + 12 - k, 176, 12 - k, 14, k + 1);
		}
		int l = this.getCookProgressScaled(22, 0);
		this.drawTexturedModalRect(i + 67 - l, j + 47, 212 - l, 1, l, 12);
		l = this.getCookProgressScaled(22, 1);
		this.drawTexturedModalRect(i + 109, j + 47, 212, 1, l, 12);
		for (int k = 0; k < 11; ++k) {
			int t = TileEntityMistFurnace.getTemp(this.tileFurnace, k) / 300;
			if (t > 0) {
				if (t > 7) t = 7;
				this.drawTexturedModalRect(i + xTempCoord[k], j + yTempCoord[k], xTexCoord[k] + t * 22, yTexCoord[k], xTempSize[k], yTempSize[k]);
			}
		}
	}

	private int getCookProgressScaled(int pixels, int index) {
		int cook = this.tileFurnace.getField(index + 4);
		int total = this.tileFurnace.getField(index + 6);
		int ash = this.tileFurnace.getField(index + 8);
		return total != 0 && cook != 0 ? cook * pixels / total : ash != 0 ? ash * pixels / TileEntityMistFurnace.ashTime : 0;
	}

	private int getBurnScaled(int pixels, int index) {
		int i = this.tileFurnace.getField(index + 2);
		if (i == 0) i = 200;
		return this.tileFurnace.getField(index) * pixels / i;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		int i = TileEntityMistFurnace.getStatus(this.tileFurnace);
		switch (button.id) {
			case 0: {
				PacketHandler.INSTANCE.sendToServer(new PacketFurnaceClose(this.tileFurnace.getPos(), i));
				break;
			}
			case 1: {
				int signal = this.tileFurnace.getWorld().isBlockIndirectlyGettingPowered(this.tileFurnace.getPos());
				if (signal == 0 || signal > this.tileFurnace.getClientComparatorOutput(this.tileFurnace)) {
					PacketHandler.INSTANCE.sendToServer(new PacketFurnaceClose(this.tileFurnace.getPos(), i));
				}
				break;
			}
		}
	}
}