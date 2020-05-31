package ru.liahim.mist.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelRespirator extends ModelBiped {

	public static final ModelRespirator respirator = new ModelRespirator();
	private ModelRenderer mask;
	private ModelRenderer capsule1;

	public ModelRespirator() {
		super(0, 0, 32, 16);
		mask = new ModelRenderer(this, 0, 0);
		mask.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.125F);
		mask.setRotationPoint(0.0F, 0.0F, 0.0F);
		mask.setTextureSize(32, 16);
		capsule1 = new ModelRenderer(this, 0, 0);
		capsule1.addBox(-1.0F, -1.0F, -1.0F, 2, 2, 2, 0.125F);
		capsule1.setRotationPoint(0.0F, -0.8F, -4.0F);
		capsule1.rotateAngleX = 0.3F;
		capsule1.setTextureSize(32, 16);
		mask.addChild(capsule1);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entityIn instanceof EntityLivingBase) {
			if (((EntityLivingBase)entityIn).getActivePotionEffect(MobEffects.INVISIBILITY) != null) return;
			this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
			copyModelAngles(this.bipedHead, this.mask);
			GlStateManager.pushMatrix();
			if (this.isChild) {
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
			} else if (entityIn.isSneaking())
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			mask.render(scale);
			GlStateManager.popMatrix();
		}
	}
}