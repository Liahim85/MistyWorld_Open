package ru.liahim.mist.inventory.gui;

import org.lwjgl.opengl.GL11;

import ru.liahim.mist.inventory.container.ContainerRespirator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiRespirator extends GuiItemContainer {

	public static final ResourceLocation guiTextures = new ResourceLocation("mist:textures/gui/container/respirator.png");
	private String invName;

	public GuiRespirator(InventoryPlayer inventory) {
		super(new ContainerRespirator(inventory), inventory, true);
		this.invName = inventory.getStackInSlot(inventory.currentItem).getDisplayName();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.mc.getTextureManager().bindTexture(guiTextures);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(this.invName, this.xSize / 2 - this.fontRenderer.getStringWidth(this.invName) / 2, 8, 4210752);
        this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }
}