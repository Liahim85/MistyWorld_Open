package ru.liahim.mist.client.renderer.tileentity;

import ru.liahim.mist.tileentity.TileEntityCampStick;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileEntityCampStickRenderer extends TileEntitySpecialRenderer<TileEntityCampStick> {

	@Override
	public void render(TileEntityCampStick te, double x, double y, double z, float partialTicks, int destroyStage, float alphaIn) {
		if (!te.getFood().isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 0.5, y + 0.588826875, z + 0.5);
			GlStateManager.rotate(90, -1, 0, 0);
			GlStateManager.rotate(180 - te.getFacing().getHorizontalAngle(), 0, 0, 1);
			GlStateManager.scale(0.5, 0.5, 0.5);
			GlStateManager.translate(0.09375, 1.06910625, 0);
			GlStateManager.rotate(20, 1, 0, 0);
			Minecraft.getMinecraft().getRenderItem().renderItem(te.getFood(), TransformType.NONE);
			GlStateManager.popMatrix();
		}
	}
}