package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.animation.SimpleIK;

@SideOnly(Side.CLIENT)
public class ModelForestSpider extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer heliceraL;
	public ModelRenderer heliceraR;
	public ModelRenderer legL1;
	public ModelRenderer legL1_;
	public ModelRenderer legL2;
	public ModelRenderer legL2_;
	public ModelRenderer legL3;
	public ModelRenderer legL3_;
	public ModelRenderer legL4;
	public ModelRenderer legL4_;
	public ModelRenderer legR1;
	public ModelRenderer legR1_;
	public ModelRenderer legR2;
	public ModelRenderer legR2_;
	public ModelRenderer legR3;
	public ModelRenderer legR3_;
	public ModelRenderer legR4;
	public ModelRenderer legR4_;

	public ModelRenderer targetL1;
	public ModelRenderer targetL2;
	public ModelRenderer targetL3;
	public ModelRenderer targetL4;
	public ModelRenderer targetR1;
	public ModelRenderer targetR2;
	public ModelRenderer targetR3;
	public ModelRenderer targetR4;

	private SimpleIK ikL1;
	private SimpleIK ikL2;
	private SimpleIK ikL3;
	private SimpleIK ikL4;
	private SimpleIK ikR1;
	private SimpleIK ikR2;
	private SimpleIK ikR3;
	private SimpleIK ikR4;

	public ModelForestSpider(float scale) {
		int lengthIn1 = 8;
		int lengthIn2 = 10;
		int lengthOut1 = 9;
		int lengthOut2 = 10;
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-3, -4, -2, 6, 4, 6, scale);
		this.body.setTextureOffset(0, 20);
		this.body.addBox(-2, -4, -4, 4, 3, 2, scale);
		this.body.setRotationPoint(0, 12, 0);
		this.back = new ModelRenderer(this, 0, 10);
		this.back.addBox(-2, 0, -2, 4, 4, 6, scale);
		this.back.rotateAngleX = -pi / 9;
		this.back.setRotationPoint(0, -3, 4);
		this.body.addChild(this.back);
		this.heliceraL = new ModelRenderer(this, 0, 0);
		this.heliceraL.addBox(-0.5F, 0, -1, 1, 2, 1, 0.2F);
		this.heliceraL.rotateAngleX = -pi / 6;
		this.heliceraL.rotateAngleZ = -pi / 12;
		this.heliceraL.setRotationPoint(1, -1.5F, -4);
		this.body.addChild(this.heliceraL);
		this.heliceraR = new ModelRenderer(this, 0, 0);
		this.heliceraR.addBox(-0.5F, 0, -1, 1, 2, 1, 0.2F);
		this.heliceraR.rotateAngleX = -pi / 6;
		this.heliceraR.rotateAngleZ = pi / 12;
		this.heliceraR.setRotationPoint(-1, -1.5F, -4);
		this.body.addChild(this.heliceraR);

		this.legL1 = new ModelRenderer(this, 24, 0);
		this.legL1.addBox(-0.5F, 0, -0.5F, 1, lengthOut1, 1, 0.2F);
		this.legL1.setRotationPoint(1, 0, -2.25F);
		//this.body.addChild(this.legL1);
		this.legL2 = new ModelRenderer(this, 24, 0);
		this.legL2.addBox(-0.5F, 0, -0.5F, 1, lengthIn1, 1, 0.25F);
		this.legL2.setRotationPoint(2, 0, -0.75F);
		//this.body.addChild(this.legL2);
		this.legL3 = new ModelRenderer(this, 24, 0);
		this.legL3.addBox(-0.5F, 0, -0.5F, 1, lengthIn1, 1, 0.25F);
		this.legL3.setRotationPoint(2, 0, 0.75F);
		//this.body.addChild(this.legL3);
		this.legL4 = new ModelRenderer(this, 24, 0);
		this.legL4.addBox(-0.5F, 0, -0.5F, 1, lengthOut1, 1, 0.2F);
		this.legL4.setRotationPoint(1, 0, 2.25F);
		//this.body.addChild(this.legL4);

		this.legL1_ = new ModelRenderer(this, 28, 0);
		this.legL1_.addBox(-0.5F, 0, -0.5F, 1, lengthOut2, 1, scale);
		this.legL1_.setRotationPoint(0, lengthOut1 + 0.2F, 0);
		this.legL1.addChild(this.legL1_);
		this.legL2_ = new ModelRenderer(this, 28, 0);
		this.legL2_.addBox(-0.5F, 0, -0.5F, 1, lengthIn2, 1, scale);
		this.legL2_.setRotationPoint(0, lengthIn1 + 0.25F, 0);
		this.legL2.addChild(this.legL2_);
		this.legL3_ = new ModelRenderer(this, 28, 0);
		this.legL3_.addBox(-0.5F, 0, -0.5F, 1, lengthIn2, 1, scale);
		this.legL3_.setRotationPoint(0, lengthIn1 + 0.25F, 0);
		this.legL3.addChild(this.legL3_);
		this.legL4_ = new ModelRenderer(this, 28, 0);
		this.legL4_.addBox(-0.5F, 0, -0.5F, 1, lengthOut2, 1, scale);
		this.legL4_.setRotationPoint(0, lengthOut1 + 0.2F, 0);
		this.legL4.addChild(this.legL4_);

		this.targetL1 = new ModelRenderer(this, 0, 0);
		this.targetL1.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetL1.setRotationPoint(0, lengthOut2, 0);
		this.targetL2 = new ModelRenderer(this, 0, 0);
		this.targetL2.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetL2.setRotationPoint(0, lengthIn2, 0);
		this.targetL3 = new ModelRenderer(this, 0, 0);
		this.targetL3.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetL3.setRotationPoint(0, lengthIn2, 0);
		this.targetL4 = new ModelRenderer(this, 0, 0);
		this.targetL4.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetL4.setRotationPoint(0, lengthOut2, 0);

		this.legR1 = new ModelRenderer(this, 24, 0);
		this.legR1.addBox(-0.5F, 0, -0.5F, 1, lengthOut1, 1, 0.2F);
		this.legR1.setRotationPoint(-1, 0, -2.25F);
		//this.body.addChild(this.legR1);
		this.legR2 = new ModelRenderer(this, 24, 0);
		this.legR2.addBox(-0.5F, 0, -0.5F, 1, lengthIn1, 1, 0.25F);
		this.legR2.setRotationPoint(-2, 0, -0.75F);
		//this.body.addChild(this.legR2);
		this.legR3 = new ModelRenderer(this, 24, 0);
		this.legR3.addBox(-0.5F, 0, -0.5F, 1, lengthIn1, 1, 0.25F);
		this.legR3.setRotationPoint(-2, 0, 0.75F);
		//this.body.addChild(this.legR3);
		this.legR4 = new ModelRenderer(this, 24, 0);
		this.legR4.addBox(-0.5F, 0, -0.5F, 1, lengthOut1, 1, 0.2F);
		this.legR4.setRotationPoint(-1, 0, 2.25F);
		//this.body.addChild(this.legR4);

		this.legR1_ = new ModelRenderer(this, 28, 0);
		this.legR1_.addBox(-0.5F, 0, -0.5F, 1, lengthOut2, 1, scale);
		this.legR1_.setRotationPoint(0, lengthOut1 + 0.2F, 0);
		this.legR1.addChild(this.legR1_);
		this.legR2_ = new ModelRenderer(this, 28, 0);
		this.legR2_.addBox(-0.5F, 0, -0.5F, 1, lengthIn2, 1, scale);
		this.legR2_.setRotationPoint(0, lengthIn1 + 0.25F, 0);
		this.legR2.addChild(this.legR2_);
		this.legR3_ = new ModelRenderer(this, 28, 0);
		this.legR3_.addBox(-0.5F, 0, -0.5F, 1, lengthIn2, 1, scale);
		this.legR3_.setRotationPoint(0, lengthIn1 + 0.25F, 0);
		this.legR3.addChild(this.legR3_);
		this.legR4_ = new ModelRenderer(this, 28, 0);
		this.legR4_.addBox(-0.5F, 0, -0.5F, 1, lengthOut2, 1, scale);
		this.legR4_.setRotationPoint(0, lengthOut1 + 0.2F, 0);
		this.legR4.addChild(this.legR4_);

		this.targetR1 = new ModelRenderer(this, 0, 0);
		this.targetR1.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetR1.setRotationPoint(0, lengthOut2, 0);
		this.targetR2 = new ModelRenderer(this, 0, 0);
		this.targetR2.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetR2.setRotationPoint(0, lengthIn2, 0);
		this.targetR3 = new ModelRenderer(this, 0, 0);
		this.targetR3.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetR3.setRotationPoint(0, lengthIn2, 0);
		this.targetR4 = new ModelRenderer(this, 0, 0);
		this.targetR4.addBox(-0.5F, -0.5F, -0.5F, 1, 1, 1, scale);
		this.targetR4.setRotationPoint(0, lengthOut2, 0);

		this.ikL1 = new SimpleIK(Axis.Y, false, this.body, this.legL1, this.legL1_, this.targetL1);
		this.ikL2 = new SimpleIK(Axis.Y, false, this.body, this.legL2, this.legL2_, this.targetL2);
		this.ikL3 = new SimpleIK(Axis.Y, false, this.body, this.legL3, this.legL3_, this.targetL3);
		this.ikL4 = new SimpleIK(Axis.Y, false, this.body, this.legL4, this.legL4_, this.targetL4);
		this.ikR1 = new SimpleIK(Axis.Y, false, this.body, this.legR1, this.legR1_, this.targetR1);
		this.ikR2 = new SimpleIK(Axis.Y, false, this.body, this.legR2, this.legR2_, this.targetR2);
		this.ikR3 = new SimpleIK(Axis.Y, false, this.body, this.legR3, this.legR3_, this.targetR3);
		this.ikR4 = new SimpleIK(Axis.Y, false, this.body, this.legR4, this.legR4_, this.targetR4);

		this.targetL1.rotationPointX = 8;
		this.targetL1.rotationPointY = 24;
		this.targetL1.rotationPointZ = -11;
		this.targetL2.rotationPointX = 12;
		this.targetL2.rotationPointY = 24;
		this.targetL2.rotationPointZ = -4;
		this.targetL3.rotationPointX = 12;
		this.targetL3.rotationPointY = 24;
		this.targetL3.rotationPointZ = 4;
		this.targetL4.rotationPointX = 8;
		this.targetL4.rotationPointY = 24;
		this.targetL4.rotationPointZ = 11;

		this.targetR1.rotationPointX = -8;
		this.targetR1.rotationPointY = 24;
		this.targetR1.rotationPointZ = -11;
		this.targetR2.rotationPointX = -12;
		this.targetR2.rotationPointY = 24;
		this.targetR2.rotationPointZ = -4;
		this.targetR3.rotationPointX = -12;
		this.targetR3.rotationPointY = 24;
		this.targetR3.rotationPointZ = 4;
		this.targetR4.rotationPointX = -8;
		this.targetR4.rotationPointY = 24;
		this.targetR4.rotationPointZ = 11;
	}

	public ModelForestSpider() {
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
			this.legL1.render(scale);
			this.legL2.render(scale);
			this.legL3.render(scale);
			this.legL4.render(scale);
			this.legR1.render(scale);
			this.legR2.render(scale);
			this.legR3.render(scale);
			this.legR4.render(scale);
		}
	}

	static final float speed = 1.5F;
	static final float bRadius = 8;
	static final float shift = pi / 4;
	static final float upFactor = 4;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {

		this.body.rotationPointY = 12 + 3 * limbSwingAmount;
		this.body.rotateAngleY = headYaw * 0.005F;
		this.body.rotateAngleX = headPitch * 0.01F;

		limbSwing *= speed;
		float yL = -MathHelper.sin(limbSwing) * bRadius;
		float yR;
		float zL;
		float zR;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / upFactor;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= upFactor;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetL1.rotationPointY = yR + 24;
		this.targetL1.rotationPointZ = zR - 11;

		this.targetR1.rotationPointY = yL + 24;
		this.targetR1.rotationPointZ = zL - 11;

		// Shift
		limbSwing -= shift * 2;
		yL = -MathHelper.sin(limbSwing) * bRadius;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / upFactor;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= upFactor;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetL2.rotationPointY = yR + 24;
		this.targetL2.rotationPointZ = zR - 5;

		this.targetR2.rotationPointY = yL + 24;
		this.targetR2.rotationPointZ = zL - 5;

		limbSwing += shift;
		yL = -MathHelper.sin(limbSwing) * bRadius;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / upFactor;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= upFactor;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetR3.rotationPointY = yR + 24;
		this.targetR3.rotationPointZ = zR + 5;

		this.targetL3.rotationPointY = yL + 24;
		this.targetL3.rotationPointZ = zL + 5;

		limbSwing -= shift * 2;
		yL = -MathHelper.sin(limbSwing) * bRadius;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / upFactor;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= upFactor;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetL4.rotationPointY = yR + 24;
		this.targetL4.rotationPointZ = zR + 11;

		this.targetR4.rotationPointY = yL + 24;
		this.targetR4.rotationPointZ = zL + 11;

		this.ikL1.rotateBones(0);
		this.ikL2.rotateBones(0);
		this.ikL3.rotateBones(0);
		this.ikL4.rotateBones(0);
		this.ikR1.rotateBones(0);
		this.ikR2.rotateBones(0);
		this.ikR3.rotateBones(0);
		this.ikR4.rotateBones(0);
	}
}