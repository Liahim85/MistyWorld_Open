package ru.liahim.mist.inventory.gui;

import java.io.IOException;

import ru.liahim.mist.network.PacketFirePitFillPot;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class GuiCampfirePot extends GuiScreen {

	private final EntityPlayer player;
	private final TileEntityCampfire te;
	private static final ResourceLocation BACKGROUND = new ResourceLocation("mist:textures/gui/container/pot.png");
	protected int xSize = 88;
	protected int ySize = 70;
	protected int guiLeft;
	protected int guiTop;
	private final int minVolume;
	private int volume;
	private FluidStack fluid;
	private final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(FluidRegistry.WATER.getStill().toString());
	private int uni;

	public GuiCampfirePot(EntityPlayer player, TileEntityCampfire te) {
		this.player = player;
		this.te = te;
		this.minVolume = this.te.getVolum();
		this.volume = this.minVolume == 0 ? 4 : this.minVolume;
		IFluidHandlerItem fluidHandler = FluidUtil.getFluidHandler(this.player.getHeldItemMainhand());
		if (fluidHandler != null) this.fluid = fluidHandler.drain(Fluid.BUCKET_VOLUME, false);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		this.buttonList.clear();
        this.buttonList.add(new GuiMistButton(1, this.guiLeft + 31, this.guiTop + 17, 26, 11, 48, 70, "", BACKGROUND));
        this.buttonList.add(new GuiMistButton(2, this.guiLeft + 31, this.guiTop + 52, 26, 11, 74, 70, "", BACKGROUND));
        this.buttonList.add(new GuiMistButton(3, this.guiLeft + 56, this.guiTop + 28, 24, 24, 24, 70, "", BACKGROUND));
        this.buttonList.add(new GuiMistButton(4, this.guiLeft +  8, this.guiTop + 28, 24, 24,  0, 70, "", BACKGROUND));
        if (this.minVolume == 0) {
        	if (this.volume == 0) this.buttonList.get(1).visible = false;
        	else if (this.volume == 4) this.buttonList.get(0).visible = false;
        } else {
        	this.buttonList.get(1).visible = false;
        	if (this.minVolume == 4) this.buttonList.get(0).visible = false;
        }
        this.uni = this.fontRenderer.getUnicodeFlag() ? 0 : 1;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawSoup();
		this.mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft + 32, this.guiTop + 28, 88, 0, 4, 24);
		String name = I18n.format("gui.mist.pot");
		this.fontRenderer.drawString(name, this.width / 2 - this.fontRenderer.getStringWidth(name) / 2, this.guiTop + 5 + uni, 4210752);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(BACKGROUND);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		int i = Math.min(4 - this.minVolume, this.fluid.amount / 250);
		switch (button.id) {
		case 1:
			if (this.volume < this.minVolume + i) ++this.volume;
			this.buttonList.get(1).visible = true;
			this.buttonList.get(1).enabled = true;
			button.enabled = false;
			if (this.volume == this.minVolume + i) button.visible = false;
			break;
		case 2:
			if (this.volume > 0 && this.volume > this.minVolume) --this.volume;
			this.buttonList.get(0).visible = true;
			this.buttonList.get(0).enabled = true;
			button.enabled = false;
			if (this.volume == this.minVolume) button.visible = false;
			break;
		case 3:
			this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
            if (this.volume != this.minVolume) {
            	boolean milk = this.fluid.getFluid().getName().equals("milk");
            	PacketHandler.INSTANCE.sendToServer(new PacketFirePitFillPot(this.te.getPos(), this.volume, milk ? this.volume - this.minVolume : 0));
            }
			break;
		case 4:
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
			if (this.minVolume > 0) PacketHandler.INSTANCE.sendToServer(new PacketFirePitFillPot(this.te.getPos(), 0, 0));
			break;
		}
	}

	private void drawSoup() {
		this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		Gui.drawScaledCustomSizeModalRect(this.guiLeft + 32, this.guiTop + 52 - this.volume * 6, this.sprite.getOriginX(), this.sprite.getOriginY() + 8 - this.volume * 2, 8, this.volume * 2, 24, this.volume * 6, this.sprite.getOriginX() / this.sprite.getMinU(), this.sprite.getOriginY() / this.sprite.getMinV());
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}