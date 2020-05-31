package ru.liahim.mist.client.renderer.tileentity;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.ModelUrn;
import ru.liahim.mist.tileentity.TileEntityUrn;

@SideOnly(Side.CLIENT)
public class TileEntityUrnRenderer extends TileEntitySpecialRenderer<TileEntityUrn> {

	private final ModelUrn model = new ModelUrn();

	@Override
	public void render(TileEntityUrn te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
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
		} else this.bindTexture(te.getUrnType().getTexture());

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		if (destroyStage < 0) GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

		GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);
		GlStateManager.scale(1.0F, -1.0F, -1.0F);
		GlStateManager.translate(0.5F, 0.5F, 0.5F);

		float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;

		f = 1.0F - f;
		f = 1.0F - f * f * f;
		this.model.lid.rotateAngleX = 0;
		this.model.lid.rotateAngleZ = 0;
		if (te.openSide == EnumFacing.SOUTH) this.model.lid.rotateAngleX = -(f * ((float) Math.PI / 36));
		else if (te.openSide == EnumFacing.NORTH) this.model.lid.rotateAngleX = (f * ((float) Math.PI / 36));
		else if (te.openSide == EnumFacing.EAST) this.model.lid.rotateAngleZ = -(f * ((float) Math.PI / 36));
		else if (te.openSide == EnumFacing.WEST) this.model.lid.rotateAngleZ = (f * ((float) Math.PI / 36));
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		this.model.renderAll(te.getUrnType().isRare(), te.getTintColor(), te.getPatina());
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