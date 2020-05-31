package ru.liahim.mist.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelUrn extends ModelBase {

	public ModelRenderer base = (new ModelRenderer(this, 0, 1)).setTextureSize(48, 32);
	public ModelRenderer patina = (new ModelRenderer(this, 16, 17)).setTextureSize(48, 32);
	public ModelRenderer lid = (new ModelRenderer(this, 0, 16)).setTextureSize(48, 32);
	public ModelRenderer lid_color = (new ModelRenderer(this, 0, 21)).setTextureSize(48, 32);
	public ModelRenderer inside = (new ModelRenderer(this, 0, 26)).setTextureSize(48, 32);

	public ModelUrn() {
		this.base.addBox(-3.0F, -1.0F, -3.0F, 6, 9, 6, 0.0F);
		this.patina.addBox(-3.0F, -1.0F, -3.0F, 6, 9, 6, 0.0F);
		this.lid.addBox(-2.0F, 22.0F, -2.0F, 4, 1, 4, 0.0F);
		this.lid.setRotationPoint(0, -24, 0);
		this.lid_color.addBox(-2.0F, 22.0F, -2.0F, 4, 1, 4, 0.0F);
		this.lid_color.setRotationPoint(0, -24, 0);
		this.inside.addBox(-2.0F, -1.0F, -2.0F, 4, 2, 4, 0.0F);
	}

	public void renderAll(boolean rare, int tintColor, int patina) {
		if (!rare) GlStateManager.color((float)((tintColor >> 16) & 255) / 255, (float)((tintColor >> 8) & 255) / 255, (float)(tintColor & 255) / 255, 1.0F);
		this.lid.render(0.0625F);
		this.base.render(0.0625F);
		this.inside.render(-0.0625F);
		if (!rare) GlStateManager.color((float)((patina >> 16) & 255) / 255, (float)((patina >> 8) & 255) / 255, (float)(patina & 255) / 255, 1.0F);
		else OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        this.lid_color.rotateAngleX = this.lid.rotateAngleX;
		this.lid_color.rotateAngleZ = this.lid.rotateAngleZ;
		this.lid_color.render(0.0625F);
		this.patina.render(0.0625F);
	}
}