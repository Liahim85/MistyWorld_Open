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
import ru.liahim.mist.entity.EntityHulter;

@SideOnly(Side.CLIENT)
public class ModelHulter extends ModelBase {

	public ModelRenderer body;
	public ModelRenderer chest;
	public ModelRenderer neck;
	public ModelRenderer head;
	public ModelRenderer childNeck;
	public ModelRenderer childHead;
	public ModelRenderer legBR1;
	public ModelRenderer legBR2;
	public ModelRenderer legBL1;
	public ModelRenderer legBL2;
	public ModelRenderer legFR1;
	public ModelRenderer legFR2;
	public ModelRenderer legFL1;
	public ModelRenderer legFL2;
	public ModelRenderer targetBR;
	public ModelRenderer targetBL;
	public ModelRenderer targetFR;
	public ModelRenderer targetFL;
	private SimpleIK ikBR;
	private SimpleIK ikBL;
	private SimpleIK ikFR;
	private SimpleIK ikFL;

	public ModelHulter(float scale) {
		this.textureWidth = 96;
		this.textureHeight = 64;
		this.body = new ModelRenderer(this, 0, 30);
		this.body.addBox(-4, -11, -3, 8, 14, 10, scale);
		this.body.setRotationPoint(0, 8, 7);
		this.body.rotateAngleX = (float) (Math.PI / 6);
		this.chest = new ModelRenderer(this, 0, 0);
		this.chest.addBox(-6, -14, -14, 12, 14, 16, scale);
		this.chest.setRotationPoint(0, -10, 7);
		this.chest.rotateAngleX = (float) (Math.PI / 6);
		this.body.addChild(this.chest);
		this.neck = new ModelRenderer(this, 72, 16);
		this.neck.addBox(-2, -11, -5, 4, 12, 5, scale);
		this.neck.setRotationPoint(0, -14, 1);
		this.neck.rotateAngleX = (float) (Math.PI / 6);
		this.chest.addChild(this.neck);
		this.head = new ModelRenderer(this, 27, 48);
		this.head.addBox(-3, -1, -9, 6, 7, 9, scale);
		this.head.setRotationPoint(0, -10, 0);
		this.head.rotateAngleX = -(float) (Math.PI / 3);
		this.neck.addChild(this.head);

		this.childNeck = new ModelRenderer(this, 72, 16);
		this.childNeck.addBox(-2, -11, -5, 4, 12, 5, 1);
		this.childNeck.setRotationPoint(0, -14, 0);
		this.childNeck.rotateAngleX = (float) (Math.PI / 6);
		this.chest.addChild(this.childNeck);
		this.childHead = new ModelRenderer(this, 27, 48);
		this.childHead.addBox(-3, -1, -9, 6, 7, 9, 1);
		this.childHead.setRotationPoint(0, -11, 0);
		this.childHead.rotateAngleX = -(float) (Math.PI / 3);
		this.childNeck.addChild(this.childHead);

		this.legBR1 = new ModelRenderer(this, 42, 4);
		this.legBR1.addBox(-2, -1, -2, 3, 8, 4, scale);
		this.legBR1.setRotationPoint(-3.5F, 2, 1);
		//this.body.addChild(this.legBR1);
		this.legBR2 = new ModelRenderer(this, 76, 0);
		this.legBR2.addBox(-3, 1, -3, 5, 10, 5, scale);
		this.legBR2.setRotationPoint(0, 6, 0);
		this.legBR1.addChild(this.legBR2);
		this.legBL1 = new ModelRenderer(this, 0, 4);
		this.legBL1.addBox(-1, -1, -2, 3, 8, 4, scale);
		this.legBL1.setRotationPoint(3.5F, 2, 1);
		//this.body.addChild(this.legBL1);
		this.legBL2 = new ModelRenderer(this, 56, 0);
		this.legBL2.addBox(-2, 1, -3, 5, 10, 5, scale);
		this.legBL2.setRotationPoint(0, 6, 0);
		this.legBL1.addChild(this.legBL2);
		this.legFR1 = new ModelRenderer(this, 56, 15);
		this.legFR1.addBox(-1, 0, -2, 4, 14, 4, scale);
		this.legFR1.setRotationPoint(-6, -10, -7);
		//this.chest.addChild(this.legFR1);
		this.legFR2 = new ModelRenderer(this, 74, 34);
		this.legFR2.addBox(-3, 0, -3, 5, 16, 6, scale);
		this.legFR2.setRotationPoint(1, 14, 0);
		this.legFR1.addChild(this.legFR2);
		this.legFL1 = new ModelRenderer(this, 36, 30);
		this.legFL1.addBox(-3, 0, -2, 4, 14, 4, scale);
		this.legFL1.setRotationPoint(6, -10, -7);
		//this.chest.addChild(this.legFL1);
		this.legFL2 = new ModelRenderer(this, 52, 34);
		this.legFL2.addBox(-2, 0, -3, 5, 16, 6, scale);
		this.legFL2.setRotationPoint(-1, 14, 0);
		this.legFL1.addChild(this.legFL2);

		this.targetBR = new ModelRenderer(this, 12, 0);
		this.targetBR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetBR.setRotationPoint(0, 11, 0);
		this.targetBL = new ModelRenderer(this, 12, 0);
		this.targetBL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetBL.setRotationPoint(0, 11, 0);
		this.targetFR = new ModelRenderer(this, 12, 0);
		this.targetFR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetFR.setRotationPoint(0, 16.5F, 0);
		this.targetFL = new ModelRenderer(this, 12, 0);
		this.targetFL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetFL.setRotationPoint(0, 16.5F, 0);

		this.ikBR = new SimpleIK(Axis.Z, false, this.body, this.legBR1, this.legBR2, this.targetBR);
		this.ikBL = new SimpleIK(Axis.Z, false, this.body, this.legBL1, this.legBL2, this.targetBL);
		this.ikFR = new SimpleIK(Axis.X, false, this.body, this.chest, this.legFR1, this.legFR2, this.targetFR);
		this.ikFL = new SimpleIK(Axis.X, true, this.body, this.chest, this.legFL1, this.legFL2, this.targetFL);

		this.targetBR.rotationPointX = -4;
		this.targetBL.rotationPointX = 4;
		this.targetFR.rotationPointX = -9;
		this.targetFL.rotationPointX = 9;
    }

    public ModelHulter() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
		this.childNeck.isHidden = !this.isChild;
		this.childHead.isHidden = this.childNeck.isHidden;
		this.neck.isHidden = this.isChild;
		this.head.isHidden = this.neck.isHidden;
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
		} else if (((EntityHulter)entity).isFemale()) {
			GlStateManager.translate(0.0F, 2.4F * scale, 0.0F);
			GlStateManager.scale(0.9F, 0.9F, 0.9F);
		}
		this.body.render(scale);
		this.legBR1.render(scale);
		this.legBL1.render(scale);
		this.legFR1.render(scale);
		this.legFL1.render(scale);
		GlStateManager.popMatrix();
	}

	static final float speed = 0.6F;
	static final float bRadius = 13;
	static final float pi = (float) Math.PI;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
        float f = ((EntityHulter)entity).getStandingAnimationScale(ageInTicks - entity.ticksExisted);
        f *= f;
		this.head.rotateAngleX = headPitch * 0.005F - pi/3;
		this.head.rotateAngleZ = -netHeadYaw * 0.005F;
		this.head.rotateAngleY = netHeadYaw * 0.003F;
		this.neck.rotateAngleX = headPitch * 0.005F + pi/6;
		this.neck.rotateAngleZ = -netHeadYaw * 0.003F;
		
		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleZ = this.head.rotateAngleZ;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.childNeck.rotateAngleX = this.neck.rotateAngleX;
		this.childNeck.rotateAngleZ = this.neck.rotateAngleZ;

		float bodyRot = MathHelper.cos(limbSwing) * 0.2F * limbSwingAmount;
		this.body.rotateAngleX = pi / 6 - f * pi / 12;
		this.body.rotateAngleY = -bodyRot;
		this.chest.rotateAngleY = bodyRot;
		this.chest.rotateAngleZ = -bodyRot;

		float yL = -MathHelper.sin(limbSwing) * bRadius;
		float yR;
		float zL;
		float zR;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / 5;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= 5;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetBR.rotationPointY = yR + 24;
		this.targetBR.rotationPointZ = zR + 9;

		this.targetBL.rotationPointY = yL + 24;
		this.targetBL.rotationPointZ = zL + 9;

		// Shift
		limbSwing -= pi/4;
		yL = -MathHelper.sin(limbSwing) * bRadius;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / 5;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= 5;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}
		
		yR = yR * limbSwingAmount + 24;
		zR = zR * limbSwingAmount - 9;

		yL = yL * limbSwingAmount + 24;
		zL = zL * limbSwingAmount - 9;

		if (f > 0) {
			f *= pi;
			int centreY = 0;
			int centreZ = 0;
			float[] xRot = SimpleIK.rotateX(new float[] { 0, yR - centreY, zR - centreZ }, -f * 0.75F);
			this.targetFR.rotationPointY = xRot[1] + centreY;
			this.targetFR.rotationPointZ = xRot[2] + centreZ;

			xRot = SimpleIK.rotateX(new float[] { 0, yL - centreY, zL - centreZ }, -f * 0.75F);
			this.targetFL.rotationPointY = xRot[1] + centreY;
			this.targetFL.rotationPointZ = xRot[2] + centreZ;
		} else {
			this.targetFR.rotationPointY = yL;
			this.targetFR.rotationPointZ = zL;
	
			this.targetFL.rotationPointY = yR;
			this.targetFL.rotationPointZ = zR;
		}

		this.ikBR.rotateBones(pi/12);
		this.ikBL.rotateBones(-pi/12);
		this.ikFR.rotateBones(pi/12 - bodyRot);
		this.ikFL.rotateBones(pi/12 - bodyRot);
	}
}