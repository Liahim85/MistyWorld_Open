package ru.liahim.mist.client.renderer.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.ModelLatexPot;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.tileentity.TileEntityLatexPot;

@SideOnly(Side.CLIENT)
public class TileEntityLatexPotRenderer extends TileEntitySpecialRenderer<TileEntityLatexPot> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/latex_pot/latex_pot.png");
	private final ModelLatexPot model = new ModelLatexPot();

	@Override
	public void render(TileEntityLatexPot te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		if (destroyStage >= 0) {
			this.bindTexture(DESTROY_STAGES[destroyStage]);
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scale(3.0F, 2.0F, 1.0F);
			GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		} else this.bindTexture(TEXTURE);

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		if (destroyStage < 0) GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

		GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);
		GlStateManager.scale(1.0F, -1.0F, -1.0F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);
		int i = te.getBlockMetadata();
		int j;
		if (i == 1) j = 90;
		else if (i == 2) j = 180;
		else if (i == 3) j = -90;
		else j = 0;
		GlStateManager.rotate(j, 0.0F, 1.0F, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		this.model.renderAll(te.getStage());
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (destroyStage >= 0) {
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}
}