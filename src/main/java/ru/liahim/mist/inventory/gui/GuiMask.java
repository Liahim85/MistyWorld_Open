package ru.liahim.mist.inventory.gui;

import java.io.IOException;

import ru.liahim.mist.inventory.container.ContainerMask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiMask extends InventoryEffectRenderer implements IRecipeShownListener {

	public static final ResourceLocation slotMask = new ResourceLocation("mist:textures/gui/container/mask.png");

	/** The old x position of the mouse pointer */
    private float oldMouseX;
    /** The old y position of the mouse pointer */
    private float oldMouseY;
    private GuiButtonImage recipeButton;
    private final GuiRecipeBook recipeBookGui = new GuiRecipeBook();
    private boolean widthTooNarrow;
    private boolean buttonClicked;

	public GuiMask(EntityPlayer player) {
		super(new ContainerMask(player.inventory, !player.getEntityWorld().isRemote, player));
		this.allowUserInput = true;
	}

	/** Called from the main game loop to update the screen.*/
	@Override
	public void updateScreen() {
		((ContainerMask)inventorySlots).mask.setMaskBlock(false);
		this.recipeBookGui.tick();
	}

	/** Adds the buttons (and other controls) to the screen in question.*/
	@Override
	public void initGui() {
		this.buttonList.clear();
		super.initGui();

		this.widthTooNarrow = this.width < 379;
        this.recipeBookGui.func_194303_a(this.width, this.height, this.mc, this.widthTooNarrow, ((ContainerMask)this.inventorySlots).craftMatrix);
        this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
        this.recipeButton = new GuiButtonImage(10, this.guiLeft + 104, this.height / 2 - 22, 20, 18, 178, 0, 19, INVENTORY_BACKGROUND);
        this.buttonList.add(this.recipeButton);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("container.crafting"), 97, 8, 4210752);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.hasActivePotionEffects = !this.recipeBookGui.isVisible();

        if (this.recipeBookGui.isVisible() && this.widthTooNarrow) {
            this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
        } else {
            this.recipeBookGui.render(mouseX, mouseY, partialTicks);
            super.drawScreen(mouseX, mouseY, partialTicks);
            this.recipeBookGui.renderGhostRecipe(this.guiLeft, this.guiTop, false, partialTicks);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
        this.recipeBookGui.renderTooltip(this.guiLeft, this.guiTop, mouseX, mouseY);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(INVENTORY_BACKGROUND);
		int k = this.guiLeft;
		int l = this.guiTop;
		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
		this.mc.getTextureManager().bindTexture(slotMask);
		this.drawTexturedModalRect(k + 4, l - 24, 0, 166, 24, 32);
		drawPlayerModel(k + 51, l + 75, 30, k + 51 - this.oldMouseX, l + 75 - 50 - this.oldMouseY, this.mc.player);
	}

	public static void drawPlayerModel(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(posX, posY, 50.0F);
        GlStateManager.scale((-scale), scale, scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan(mouseY / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = (float)Math.atan(mouseX / 40.0F) * 20.0F;
        ent.rotationYaw = (float)Math.atan(mouseX / 40.0F) * 40.0F;
        ent.rotationPitch = -((float)Math.atan(mouseY / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntity(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, false);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

	/** Test if the 2D point is in a rectangle (relative to the GUI).
	 * Args : rectX, rectY, rectWidth, rectHeight, pointX, pointY
	 */
	@Override
	protected boolean isPointInRegion(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY) {
		return (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) && super.isPointInRegion(rectX, rectY, rectWidth, rectHeight, pointX, pointY);
	}

	/** Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton */
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (!this.recipeBookGui.mouseClicked(mouseX, mouseY, mouseButton)) {
			if (!this.widthTooNarrow || !this.recipeBookGui.isVisible()) {
				super.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	/** Called when a mouse button is released. */
	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.buttonClicked) {
			this.buttonClicked = false;
		} else {
			super.mouseReleased(mouseX, mouseY, state);
		}
	}

	@Override
	protected boolean hasClickedOutside(int par_1, int par_2, int par_3, int par_4) {
		boolean flag = par_1 < par_3 || par_2 < par_4 || par_1 >= par_3 + this.xSize || par_2 >= par_4 + this.ySize;
		return this.recipeBookGui.hasClickedOutside(par_1, par_2, this.guiLeft, this.guiTop, this.xSize, this.ySize) && flag;
	}

	/** Called by the controls from the buttonList when activated. (Mouse pressed for buttons) */
	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 10) {
			this.recipeBookGui.initVisuals(this.widthTooNarrow, ((ContainerMask)this.inventorySlots).craftMatrix);
			this.recipeBookGui.toggleVisibility();
			this.guiLeft = this.recipeBookGui.updateScreenPosition(this.widthTooNarrow, this.width, this.xSize);
			this.recipeButton.setPosition(this.guiLeft + 104, this.height / 2 - 22);
			this.buttonClicked = true;
		}
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (!this.recipeBookGui.keyPressed(typedChar, keyCode)) {
			super.keyTyped(typedChar, keyCode);
		}
	}

	/** Called when the mouse is clicked over a slot or outside the gui. */
	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		this.recipeBookGui.slotClicked(slotIn);
	}

	@Override
	public void recipesUpdated() {
		this.recipeBookGui.recipesUpdated();
	}

    /** Called when the screen is unloaded. Used to disable keyboard repeat events */
	@Override
	public void onGuiClosed() {
		this.recipeBookGui.removed();
		super.onGuiClosed();
	}

	@Override
	public GuiRecipeBook func_194310_f() {
		return this.recipeBookGui;
	}
}