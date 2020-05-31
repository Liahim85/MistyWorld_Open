package ru.liahim.mist.inventory.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.inventory.container.ContainerUrn;

@SideOnly(Side.CLIENT)
public class GuiUrn extends GuiItemContainer {

	private static final ResourceLocation TEXTURES = new ResourceLocation(Mist.MODID, "textures/gui/container/urn.png");
	private String name = "";

	public GuiUrn(IInventory urnInv, EntityPlayer player) {
		super(new ContainerUrn(urnInv, player), player.inventory, urnInv == null);
		this.name = urnInv == null ? this.playerInventory.getStackInSlot(this.playerInventory.currentItem).getDisplayName() : urnInv.getDisplayName().getUnformattedText();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(this.name, this.xSize / 2 - this.fontRenderer.getStringWidth(this.name) / 2, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}
}