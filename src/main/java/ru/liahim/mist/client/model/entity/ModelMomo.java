package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityMomo;

@SideOnly(Side.CLIENT)
public class ModelMomo extends ModelBase {

	public ModelRenderer neck;
	public ModelRenderer head;
	public ModelRenderer nose;
	public ModelRenderer childNeck;
	public ModelRenderer childHead;
	public ModelRenderer childNose;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer tail;
	public ModelRenderer legFL;
	public ModelRenderer legFR;
	public ModelRenderer legBL;
	public ModelRenderer legBR;

	private float xRotFactor;

	public ModelMomo(float scale) {
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-7, -7, 0, 14, 15, 13, scale);
		this.body.setRotationPoint(0, 19, 0);
		this.back = new ModelRenderer(this, 0, 28);
		this.back.addBox(-5, -9, -5, 10, 9, 6, scale);
		this.back.setRotationPoint(0, 19, 8);
		this.tail = new ModelRenderer(this, 32, 36);
		this.tail.addBox(-2, -3, -1, 4, 3, 4, scale);
		this.tail.setRotationPoint(0, 0, 1);
		this.back.addChild(this.tail);
		this.neck = new ModelRenderer(this, 0, 43);
		this.neck.addBox(-5, -5, -5, 10, 9, 8, scale);
		this.neck.setRotationPoint(0, 14, -7);
		this.head = new ModelRenderer(this, 41, 0);
		this.head.addBox(-3, -3, -3, 6, 5, 4, scale);
		this.head.setRotationPoint(0, 2, -5);
		this.neck.addChild(this.head);
		this.nose = new ModelRenderer(this, 32, 28);
		this.nose.addBox(-2, 0, -5, 4, 3, 5, scale);
		this.nose.setRotationPoint(0, -1, -3);
		this.head.addChild(this.nose);
		
		this.childNeck = new ModelRenderer(this, 0, 43);
		this.childNeck.addBox(-5, -5, -5, 10, 9, 8, scale);
		this.childNeck.setRotationPoint(0, 14, -7);
		this.childHead = new ModelRenderer(this, 41, 0);
		this.childHead.addBox(-3, -3, -3, 6, 5, 4, 0.5F);
		this.childHead.setRotationPoint(0, 2, -5);
		this.childNeck.addChild(this.childHead);
		this.childNose = new ModelRenderer(this, 32, 28);
		this.childNose.addBox(-2, 0, -5, 4, 3, 5, 0.5F);
		this.childNose.setRotationPoint(0, -0.5F, -3.5F);
		this.childHead.addChild(this.childNose);

		this.legBR = new ModelRenderer(this, 48, 34);
		this.legBR.addBox(-2, 0, -2, 4, 5, 4, scale);
		this.legBR.setRotationPoint(4.99F, 19, 6);
		this.legBL = new ModelRenderer(this, 48, 43);
		this.legBL.addBox(-2, 0, -2, 4, 5, 4, scale);
		this.legBL.setRotationPoint(-4.99F, 19, 6);
		this.legFR = new ModelRenderer(this, 48, 34);
		this.legFR.addBox(-2, 0, -2, 4, 5, 4, scale);
		this.legFR.setRotationPoint(4.99F, 19, -5);
		this.legFL = new ModelRenderer(this, 48, 43);
		this.legFL.addBox(-2, 0, -2, 4, 5, 4, scale);
		this.legFL.setRotationPoint(-4.99F, 19, -5);
	}

	public ModelMomo() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.childNeck.render(scale);
		} else {
			if (((EntityMomo)entity).isFemale()) {
				GlStateManager.translate(0.0F, 2.4F * scale, 0.0F);
				GlStateManager.scale(0.9F, 0.9F, 0.9F);
			}
			this.neck.render(scale);
		}
		this.body.render(scale);
		this.back.render(scale);
		this.legFL.render(scale);
		this.legFR.render(scale);
		this.legBL.render(scale);
		this.legBR.render(scale);
		GlStateManager.popMatrix();
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
		this.xRotFactor = ((EntityMomo)entity).getXRotFactor(partialTickTime);
	}

	private static final float speed = 0.9F;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		limbSwing *= speed;
		float f = MathHelper.sin(this.swingProgress * (float)Math.PI);
		this.nose.rotateAngleX = (float)Math.PI/9F;
		this.head.rotateAngleX = Math.min(headPitch * 0.008F + (float)Math.PI/18F * xRotFactor, 0.22f);
		this.head.rotateAngleX -= f * 0.8F;
		this.head.rotateAngleY = netHeadYaw * 0.007F;
		this.neck.rotateAngleX = Math.min(headPitch * 0.008F + (float)Math.PI/12F * xRotFactor, 0.33f);
		this.neck.rotateAngleX -= f * 0.8F;
		this.neck.rotateAngleY = netHeadYaw * 0.007F;

		this.childNose.rotateAngleX = this.nose.rotateAngleX;
		this.childHead.rotateAngleX = this.head.rotateAngleX - (float) Math.PI/36F;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.childNeck.rotateAngleX = this.neck.rotateAngleX - (float) Math.PI/36F;
		this.childNeck.rotateAngleY = this.neck.rotateAngleY;

		this.body.rotateAngleX = (float)Math.PI/2F;
		this.back.rotateAngleX = -(float)Math.PI/9F;
		this.back.rotateAngleY = MathHelper.cos(limbSwing + (float)Math.PI) * 0.3F * limbSwingAmount;
		this.tail.rotateAngleX = -(float)Math.PI/18F;
		this.tail.rotateAngleY = MathHelper.cos(limbSwing + (float)Math.PI) * 0.4F * limbSwingAmount;
		this.legFR.rotateAngleX = MathHelper.cos(limbSwing + (float)Math.PI * 1.5F) * 1.4F * limbSwingAmount;
		this.legFL.rotateAngleX = MathHelper.cos(limbSwing + (float)Math.PI * 0.5F) * 1.4F * limbSwingAmount;
		this.legBR.rotateAngleX = MathHelper.cos(limbSwing) * 1.4F * limbSwingAmount;
		this.legBL.rotateAngleX = MathHelper.cos(limbSwing + (float)Math.PI) * 1.4F * limbSwingAmount;
	}
}