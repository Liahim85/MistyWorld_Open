package ru.liahim.mist.inventory.gui;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.common.ClientProxy;
import ru.liahim.mist.util.RomanNumber;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSkills extends GuiScreen {

	public static final ResourceLocation guiTextures = new ResourceLocation("mist:textures/gui/skills.png");
	private final EntityPlayer player;
	protected int xSize = 176;
	protected int ySize = 80;
	protected int barSize = 160;
	protected int guiLeft;
	protected int guiTop;
    private static Page currentPage = Page.PAGES_ARRAY[0];
	private static final boolean renderPageTooltip = false;
    private static final int barsShift = 20;

	public GuiSkills(EntityPlayer player) {
		this.player = player;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize - 28) / 2;
		GuiSkills.currentPage.initPage(player);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawGuiContainerForegroundLayer(mouseX, mouseY);
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.mc.getTextureManager().bindTexture(guiTextures);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		for (Page page : Page.PAGES_ARRAY) {
			boolean current = page.index == GuiSkills.currentPage.index;
			int i = page.index == 0 ? 0 : 1;
			drawTexturedModalRect(this.guiLeft + page.index * 29, this.guiTop + this.ySize - 4, i * 28, this.ySize + (current ? 50 : 18), 28, 32);
		}
		int shift = barsShift + 4;
		for (int i = 0; i < GuiSkills.currentPage.hotbarsCount; ++i) {
			boolean icon = GuiSkills.currentPage.hasIcon(i);
			if (icon) drawTexturedModalRect(this.guiLeft + 7, this.guiTop + shift + 6, this.xSize + i * 9, 0, 9, 10);
			drawTexturedModalRect(this.guiLeft, this.guiTop + shift + 10, 0, ySize + (icon ? 9 : 1), this.xSize, 4);
			GuiSkills.currentPage.setBarColor(i);
			drawTexturedModalRect(this.guiLeft + (icon ? 21 : 8), this.guiTop + shift + 11, (icon ? 21 : 8), ySize + (icon ? 14 : 6), GuiSkills.currentPage.getScaledBar(i, this.barSize - (icon ? 13 : 0)), 2);
			shift += barsShift;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String str = I18n.format("gui.mist." + GuiSkills.currentPage.label);
		this.fontRenderer.drawString(str, this.guiLeft + this.xSize / 2 - this.fontRenderer.getStringWidth(str) / 2, this.guiTop + 8, 4210752);
		int shift = barsShift + 4;
		for (int i = 0; i < GuiSkills.currentPage.hotbarsCount; ++i) {
			boolean icon = GuiSkills.currentPage.hasIcon(i);
			this.fontRenderer.drawString(GuiSkills.currentPage.getBarName(i), this.guiLeft + (icon ? 20 : 7), this.guiTop + shift, 4210752);
			shift += barsShift;
		}
		if (GuiSkills.renderPageTooltip) {
			int x = mouseX - this.guiLeft;
			int y = mouseY - this.guiTop;
			for (Page page : Page.PAGES_ARRAY) {
				if (this.isMouseOverPage(page, x, y)) {
					str = I18n.format("gui.mist." + page.label);
					this.drawHoveringText(Lists.<String>newArrayList(str), x + this.guiLeft, y + this.guiTop, this.fontRenderer);
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			int x = mouseX - this.guiLeft;
			int y = mouseY - this.guiTop;
			for (Page page : Page.PAGES_ARRAY) {
				if (this.isMouseOverPage(page, x, y)) {
					return;
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		if (state == 0) {
			int x = mouseX - this.guiLeft;
			int y = mouseY - this.guiTop;
			for (Page page : Page.PAGES_ARRAY) {
				if (page != null && this.isMouseOverPage(page, x, y)) {
					this.setCurrentCreativeTab(page);
					return;
				}
			}
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	private boolean isMouseOverPage(Page page, int x, int y) {
		int i = 29 * page.index;
		return y > this.ySize && y < this.ySize + 28 && x > 0 + i && x < 28 + i;
	}

	private void setCurrentCreativeTab(Page page) {
		GuiSkills.currentPage = page;
		GuiSkills.currentPage.initPage(player);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == ClientProxy.skillKey.getKeyCode()) this.mc.displayGuiScreen(null);
		else super.keyTyped(typedChar, keyCode);
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	public static abstract class Page {

		public static Page[] PAGES_ARRAY = new Page[2];
		public static final Page SKILLS = new Page(0, "skills", Skill.values().length) {
			private ISkillCapaHandler capa;
			@Override
			public void initPage(EntityPlayer player) {
				this.capa = ISkillCapaHandler.getHandler(player);
			}
			@Override
			public int getScaledBar(int barIndex, int barSize) {
				Skill skill = Skill.values()[barIndex];
				return (int) (skill.getPosition(this.capa.getSkill(skill)) * barSize);
			}
			@Override
			public String getBarName(int barIndex) {
				Skill skill = Skill.values()[barIndex];
				return I18n.format("gui.mist.skills." + skill.getName()) + ": " + RomanNumber.toRoman(skill.getLevel(capa.getSkill(skill)));
			}
		};
		public static final Page EFFECTS = new Page(1, "effects", 2) {
			private IMistCapaHandler capa;
			@Override
			public void initPage(EntityPlayer player) {
				this.capa = IMistCapaHandler.getHandler(player);
			}
			@Override
			public int getScaledBar(int barIndex, int barSize) {
				if (barIndex == 0) return MathHelper.ceil(this.capa.getToxic() * barSize / 10000F);
				else if (barIndex == 1) return MathHelper.ceil(this.capa.getPollution() * barSize / 10000F);
				else return 0;
			}
			@Override
			public String getBarName(int barIndex) {
				if (barIndex == 0) return I18n.format("gui.mist.effects.toxic") + ": " + String.format("%.2f", this.capa.getToxic() / 100F) + "%";
				else return I18n.format("gui.mist.effects.pollution") + ": " + String.format("%.2f", this.capa.getPollution() / 100F) + "%";
			}
			@Override
			public boolean hasIcon(int barIndex) { return true; }
			@Override
			public void setBarColor(int barIndex) {
				if (barIndex == 1) GL11.glColor4f(40/255F, 213/255F, 92/255F, 1.0F);
			}
		};

		private final int index;
		private final String label;
		private final int hotbarsCount;

		public Page(int index, String label, int hotbarsCount) {
			if (index >= PAGES_ARRAY.length) {
				Page[] tmp = new Page[index + 1];
				for (int x = 0; x < PAGES_ARRAY.length; x++) {
					tmp[x] = PAGES_ARRAY[x];
				}
				PAGES_ARRAY = tmp;
			}
			this.index = index;
			this.label = label;
			this.hotbarsCount = hotbarsCount;
			PAGES_ARRAY[index] = this;
		}

		@SideOnly(Side.CLIENT)
		public int getIndex() {
			return this.index;
		}

		@SideOnly(Side.CLIENT)
		public String getLabel() {
			return this.label;
		}

		public void initPage(EntityPlayer player) {}
		public int getScaledBar(int barIndex, int barSize) { return 0; }
		public String getBarName(int barIndex) { return ""; }
		public boolean hasIcon(int barIndex) { return false; }
		public void setBarColor(int barIndex) {}
	}
}