package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntitySwampCrab;

@SideOnly(Side.CLIENT)
public class ModelSwampCrab extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer body;
	public ModelRenderer eyeL;
	public ModelRenderer eyeR;
	public ModelRenderer clawL;
	public ModelRenderer clawR;
	public ModelRenderer legL1;
	public ModelRenderer legL2;
	public ModelRenderer legL3;
	public ModelRenderer legR1;
	public ModelRenderer legR2;
	public ModelRenderer legR3;

	private float eatFactor;
	private float eyeFactor;

	public ModelSwampCrab(float scale) {
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-4, -5, -4, 8, 5, 8, scale);
		this.body.setRotationPoint(0, 22, 0);
		this.eyeL = new ModelRenderer(this, 0, 3);
		this.eyeL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.eyeL.setRotationPoint(2, -5, -4);
		this.body.addChild(this.eyeL);
		this.eyeR = new ModelRenderer(this, 0, 3);
		this.eyeR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.eyeR.setRotationPoint(-2, -5, -4);
		this.body.addChild(this.eyeR);

		this.clawL = new ModelRenderer(this, 12, 13);
		this.clawL.addBox(-1, -1.5F, -4, 2, 3, 4, scale);
		this.clawL.rotateAngleX = pi / 12;
		this.clawL.rotateAngleY = pi / 4;
		this.clawL.rotateAngleZ = -pi / 12;
		this.clawL.setRotationPoint(4, -2, -4);
		this.body.addChild(this.clawL);
		this.clawR = new ModelRenderer(this, 0, 13);
		this.clawR.addBox(-1, -1.5F, -4, 2, 3, 4, scale);
		this.clawR.rotateAngleX = pi / 12;
		this.clawR.rotateAngleY = -pi / 4;
		this.clawR.rotateAngleZ = pi / 12;
		this.clawR.setRotationPoint(-4, -2, -4);
		this.body.addChild(this.clawR);

		this.legL1 = new ModelRenderer(this, 24, 0);
		this.legL1.addBox(0, -0.5F, -0.5F, 3, 2, 1, scale);
		this.legL1.rotateAngleY = pi / 12;
		this.legL1.rotateAngleZ = pi / 9;
		this.legL1.setRotationPoint(4, -1, -1.5F);
		this.body.addChild(this.legL1);
		this.legL2 = new ModelRenderer(this, 24, 0);
		this.legL2.addBox(0, -0.5F, -0.5F, 3, 2, 1, scale);
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 9;
		this.legL2.setRotationPoint(4, -1, 0.5F);
		this.body.addChild(this.legL2);
		this.legL3 = new ModelRenderer(this, 24, 0);
		this.legL3.addBox(0, -0.5F, -0.5F, 3, 2, 1, scale);
		this.legL3.rotateAngleY = -pi / 4;
		this.legL3.rotateAngleZ = pi / 9;
		this.legL3.setRotationPoint(4, -1, 2.5F);
		this.body.addChild(this.legL3);

		this.legR1 = new ModelRenderer(this, 0, 0);
		this.legR1.addBox(-3, -0.5F, -0.5F, 3, 2, 1, scale);
		this.legR1.rotateAngleY = -pi / 12;
		this.legR1.rotateAngleZ = -pi / 9;
		this.legR1.setRotationPoint(-4, -1, -1.5F);
		this.body.addChild(this.legR1);
		this.legR2 = new ModelRenderer(this, 0, 0);
		this.legR2.addBox(-3, -0.5F, -0.5F, 3, 2, 1, scale);
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 9;
		this.legR2.setRotationPoint(-4, -1, 0.5F);
		this.body.addChild(this.legR2);
		this.legR3 = new ModelRenderer(this, 0, 0);
		this.legR3.addBox(-3, -0.5F, -0.5F, 3, 2, 1, scale);
		this.legR3.rotateAngleY = pi / 4;
		this.legR3.rotateAngleZ = -pi / 9;
		this.legR3.setRotationPoint(-4, -1, 2.5F);
		this.body.addChild(this.legR3);
	}

	public ModelSwampCrab() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.body.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.body.render(scale);
		}
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
        this.eatFactor = ((EntitySwampCrab)entity).getEatFactor(partialTickTime);
		this.eyeFactor = ((EntitySwampCrab)entity).getEyeFactor(partialTickTime);
	}

	static final float speed = 2;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		this.eyeL.rotateAngleY = headYaw * 0.01F * this.eyeFactor;
		this.eyeL.rotateAngleX = headPitch * 0.01F * this.eyeFactor + 0.5F * (1 - this.eyeFactor);
		this.eyeR.rotateAngleY = this.eyeL.rotateAngleY;
		this.eyeR.rotateAngleX = this.eyeL.rotateAngleX;

		this.clawL.rotateAngleX = pi / 12;
		this.clawL.rotateAngleY = pi / 4 + 0.2F * this.eatFactor;
		this.clawL.rotateAngleZ = -pi / 12 + 0.2F * this.eatFactor;
		this.clawR.rotateAngleX = pi / 12;
		this.clawR.rotateAngleY = -pi / 4 + 0.2F * this.eatFactor;
		this.clawR.rotateAngleZ = pi / 12 + 0.2F * this.eatFactor;

		this.legL1.rotateAngleY = pi / 12;
		this.legL1.rotateAngleZ = pi / 9;
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 9;
		this.legL3.rotateAngleY = -pi / 4;
		this.legL3.rotateAngleZ = pi / 9;
		this.legR1.rotateAngleY = -pi / 12;
		this.legR1.rotateAngleZ = -pi / 9;
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 9;
		this.legR3.rotateAngleY = pi / 4;
		this.legR3.rotateAngleZ = -pi / 9;

		this.clawL.rotateAngleX -= limbSwingAmount * pi / 3;
		this.clawR.rotateAngleX -= limbSwingAmount * pi / 3;

		float y1 = MathHelper.cos(limbSwing) * 0.8F * limbSwingAmount;
		float z1 = MathHelper.sin(limbSwing) * 0.8F * limbSwingAmount;
		float y2 = MathHelper.cos(limbSwing + pi) * 0.5F * limbSwingAmount;
		float z2 = MathHelper.sin(limbSwing + pi) * 0.5F * limbSwingAmount;
		this.legL1.rotateAngleY += y1;
		this.legL1.rotateAngleZ += Math.min(z1, 0);
		this.legR1.rotateAngleY += y1;
		this.legR1.rotateAngleZ += Math.max(z1, 0);
		this.legL2.rotateAngleY += y2;
		this.legL2.rotateAngleZ += Math.min(z2, 0);
		this.legR2.rotateAngleY += y2;
		this.legR2.rotateAngleZ += Math.max(z2, 0);
		this.legL3.rotateAngleY += y1;
		this.legL3.rotateAngleZ += Math.min(z1, 0);
		this.legR3.rotateAngleY += y1;
		this.legR3.rotateAngleZ += Math.max(z1, 0);
	}
}