package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityBarvog;

@SideOnly(Side.CLIENT)
public class ModelBarvog extends ModelBase {

	public ModelRenderer body_1;
	public ModelRenderer body_2;
	public ModelRenderer head;
	public ModelRenderer childHead;
	public ModelRenderer legFR;
	public ModelRenderer legFL;
	public ModelRenderer legBR;
	public ModelRenderer legBL;
	public ModelRenderer neck;
	protected float childYOffset = 8.0F;
	protected float childZOffset = 4.0F;

	public ModelBarvog(float scale) {
		this.textureWidth = 80;
		this.textureHeight = 64;
		this.body_1 = new ModelRenderer(this, 0, 0);
		this.body_1.setRotationPoint(0, 16, 0);
		this.body_1.addBox(-7, -14, -8, 14, 16, 15, 0);
		this.body_2 = new ModelRenderer(this, 0, 31);
		this.body_2.setRotationPoint(0, 0, 7);
		this.body_2.addBox(-5, -10, 0, 10, 12, 7, 0);
		this.body_1.addChild(this.body_2);
		this.neck = new ModelRenderer(this, 34, 31);
		this.neck.setRotationPoint(0, -10, -8);
		this.neck.addBox(-5, -3, -5, 10, 11, 8, 0);
		this.body_1.addChild(this.neck);
		this.head = new ModelRenderer(this, 0, 50);
		this.head.setRotationPoint(0, 0, -5);
		this.head.addBox(-3, -2, -7, 6, 5, 9, 0);
		this.neck.addChild(this.head);
		this.childHead = new ModelRenderer(this, 0, 50);
		this.childHead.setRotationPoint(0, 0.5F, -5.5F);
		this.childHead.addBox(-3, -2, -7, 6, 5, 9, 0.5F);
		this.neck.addChild(this.childHead);

		this.legFR = new ModelRenderer(this, 61, 0);
		this.legFR.setRotationPoint(-7, 1, -6.5F);
		this.legFR.addBox(-2, -1, -2.5F, 4, 8, 5, 0);
		this.body_1.addChild(this.legFR);
		this.legFL = new ModelRenderer(this, 43, 0);
		this.legFL.setRotationPoint(7, 1, -6.5F);
		this.legFL.addBox(-2, -1, -2.5F, 4, 8, 5, 0);
		this.body_1.addChild(this.legFL);
		this.legBR = new ModelRenderer(this, 62, 13);
		this.legBR.setRotationPoint(-5, 1, 5.5F);
		this.legBR.addBox(-2, 0, -2.5F, 4, 7, 5, 0);
		this.body_2.addChild(this.legBR);
		this.legBL = new ModelRenderer(this, 62, 25);
		this.legBL.setRotationPoint(5, 1, 5.5F);
		this.legBL.addBox(-2, 0, -2.5F, 4, 7, 5, 0);
		this.body_2.addChild(this.legBL);
	}

	public ModelBarvog() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.head.isHidden = true;
			this.childHead.isHidden = false;
			this.body_1.render(scale);
		} else {
			if (((EntityBarvog)entity).isFemale()) {
				GlStateManager.translate(0.0F, 2.4F * scale, 0.0F);
				GlStateManager.scale(0.9F, 0.9F, 0.9F);
			}
			this.head.isHidden = false;
			this.childHead.isHidden = true;
			this.body_1.render(scale);
		}
		GlStateManager.popMatrix();
	}

	private static final float speed = 0.9f;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		this.head.rotateAngleX = headPitch * 0.007F;
		this.head.rotateAngleY = netHeadYaw * 0.007F;
		this.neck.rotateAngleX = headPitch * 0.007F;
		this.neck.rotateAngleY = netHeadYaw * 0.007F;
		this.body_1.rotateAngleY = MathHelper.cos(limbSwing * speed) * 0.1F * limbSwingAmount;
		this.body_2.rotateAngleY = MathHelper.cos(limbSwing * speed + (float) Math.PI) * 0.1F * limbSwingAmount;
		this.legFR.rotateAngleX = MathHelper.cos(limbSwing * speed) * limbSwingAmount;
		this.legFL.rotateAngleX = MathHelper.cos(limbSwing * speed + (float) Math.PI) * limbSwingAmount;
		this.legBR.rotateAngleX = MathHelper.cos(limbSwing * speed + (float) Math.PI) * limbSwingAmount;
		this.legBL.rotateAngleX = MathHelper.cos(limbSwing * speed) * limbSwingAmount;
	}
}