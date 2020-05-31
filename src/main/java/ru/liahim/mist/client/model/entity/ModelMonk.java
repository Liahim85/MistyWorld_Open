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
import ru.liahim.mist.entity.EntityMonk;

@SideOnly(Side.CLIENT)
public class ModelMonk extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer childHead;
	public ModelRenderer back;
	public ModelRenderer body;
	public ModelRenderer legFR1;
	public ModelRenderer legFL1;
	public ModelRenderer legFR2;
	public ModelRenderer legFL2;
	public ModelRenderer legFR3;
	public ModelRenderer legFL3;
	public ModelRenderer legFR4;
	public ModelRenderer legFL4;
	public ModelRenderer legBR1;
	public ModelRenderer legBL1;
	public ModelRenderer legBR2;
	public ModelRenderer legBL2;
	public ModelRenderer legBR3;
	public ModelRenderer legBL3;

	private SimpleIK ikFR;
	private SimpleIK ikFL;
	private SimpleIK ikBR;
	private SimpleIK ikBL;

	public ModelMonk(float scale) {
		this.textureWidth = 128;
		this.textureHeight = 128;
		this.back = new ModelRenderer(this, 0, 0);
		this.back.addBox(-8, -18, -4, 16, 20, 16, scale);
		this.back.setTextureOffset(0, 36);
		this.back.addBox(-6, -18, -7, 12, 17, 3, scale);
		this.back.setTextureOffset(0, 100);
		this.back.addBox(-8, -18, -5, 16, 20, 1, scale);
		this.back.setRotationPoint(0, 9, 15);
		this.back.rotateAngleX = (float) Math.PI / 2.4F;
		this.body = new ModelRenderer(this, 48, 20);
		this.body.addBox(-10, -11, -1, 20, 19, 16, scale);
		this.body.setTextureOffset(64, 9);
		this.body.addBox(-6, -8, -4, 12, 8, 3, scale);
		this.body.setTextureOffset(40, 92);
		this.body.addBox(-10, 3, -2, 20, 5, 1, scale);
		this.body.setRotationPoint(0, -18, -3);
		this.body.rotateAngleX = (float) Math.PI / 9;
		this.back.addChild(this.body);
		this.head = new ModelRenderer(this, 94, 0);
		this.head.addBox(-4, -4.01F, -8, 8, 10, 9, scale);
		this.head.setTextureOffset(0, 56);
		this.head.addBox(-5, -4.02F, -8.01F, 10, 8, 6, scale);
		this.head.setTextureOffset(30, 36);
		this.head.addBox(-3, -4.02F, -11.01F, 6, 8, 3, scale);
		this.head.setRotationPoint(0, -11, 5);
		this.head.rotateAngleX = -(float) Math.PI / 2;
		this.body.addChild(this.head);

		this.childHead = new ModelRenderer(this, 94, 0);
		this.childHead.addBox(-4, -4.21F, -8.6F, 8, 10, 9, 0.8F);
		this.childHead.setTextureOffset(0, 56);
		this.childHead.addBox(-5, -4.02F, -8.41F, 10, 8, 6, 1);
		this.childHead.setTextureOffset(30, 36);
		this.childHead.addBox(-3, -4.42F, -13.01F, 6, 8, 3, 0.6F);
		this.childHead.setRotationPoint(0, -11, 6);
		this.childHead.rotateAngleX = -(float) Math.PI / 2;
		this.body.addChild(this.childHead);

		this.legFR1 = new ModelRenderer(this, 32, 55);
		this.legFR1.addBox(-3, -3, -4, 9, 15, 6, scale);
		this.legFR1.setRotationPoint(-10, -4, 6);
		//this.body.addChild(this.legFR1);
		this.legFR2 = new ModelRenderer(this, 104, 19);
		this.legFR2.addBox(0, -1, -3.99F, 7, 9, 5, scale);
		this.legFR2.setRotationPoint(-3, 7, 0);
		this.legFR1.addChild(this.legFR2);
		this.legFR3 = new ModelRenderer(this, 0, 70);
		this.legFR3.addBox(-0.02F, -9, -1, 4, 9, 6, scale);
		this.legFR3.setRotationPoint(0, 17, 1);
		//this.legFR2.addChild(this.legFR3);
		this.legFR4 = new ModelRenderer(this, 20, 76);
		this.legFR4.addBox(1, -4, 0, 4, 5, 6, scale);
		this.legFR4.setRotationPoint(0, 0, 0);
		this.legFR3.addChild(this.legFR4);
		this.legFL1 = new ModelRenderer(this, 62, 55);
		this.legFL1.addBox(-6, -3, -4, 9, 15, 6, scale);
		this.legFL1.setRotationPoint(10, -4, 6);
		//this.body.addChild(this.legFL1);
		this.legFL2 = new ModelRenderer(this, 92, 55);
		this.legFL2.addBox(-7, -1, -3.99F, 7, 9, 5, scale);
		this.legFL2.setRotationPoint(3, 7, 0);
		this.legFL1.addChild(this.legFL2);
		this.legFL3 = new ModelRenderer(this, 0, 85);
		this.legFL3.addBox(-3.98F, -9, -1, 4, 9, 6, scale);
		this.legFL3.setRotationPoint(0, 17, 1);
		//this.legFL2.addChild(this.legFL3);
		this.legFL4 = new ModelRenderer(this, 20, 87);
		this.legFL4.addBox(-5, -4, 0, 4, 5, 6, scale);
		this.legFL4.setRotationPoint(0, 0, 0);
		this.legFL3.addChild(this.legFL4);
		this.legBR1 = new ModelRenderer(this, 40, 76);
		this.legBR1.addBox(-1, -2, -2, 6, 10, 6, scale);
		this.legBR1.setRotationPoint(-8, 0, 0);
		//this.back.addChild(this.legBR1);
		this.legBR2 = new ModelRenderer(this, 106, 69);
		this.legBR2.addBox(-0.01F, 0, 0, 5, 8, 6, scale);
		this.legBR2.setRotationPoint(0, 8, -2);
		this.legBR1.addChild(this.legBR2);
		this.legBR3 = new ModelRenderer(this, 86, 70);
		this.legBR3.addBox(0.98F, -4, 0, 4, 5, 6, scale);
		this.legBR3.setRotationPoint(0, 8, 1);
		//this.legBR2.addChild(this.legBR3);
		this.legBL1 = new ModelRenderer(this, 64, 76);
		this.legBL1.addBox(-5, -2, -2, 6, 10, 6, scale);
		this.legBL1.setRotationPoint(8, 0, 0);
		//this.back.addChild(this.legBL1);
		this.legBL2 = new ModelRenderer(this, 106, 83);
		this.legBL2.addBox(-4.99F, 0, 0, 5, 8, 6, scale);
		this.legBL2.setRotationPoint(0, 8, -2);
		this.legBL1.addChild(this.legBL2);
		this.legBL3 = new ModelRenderer(this, 86, 86);
		this.legBL3.addBox(-4.98F, -4, 0, 4, 5, 6, scale);
		this.legBL3.setRotationPoint(0, 8, 1);
		//this.legBL2.addChild(this.legBL3);

		this.ikFR = new SimpleIK(Axis.X, true, new float[] {0, -9, -1}, this.back, this.body, this.legFR1, this.legFR2, this.legFR3);
		this.ikFL = new SimpleIK(Axis.X, false, new float[] {0, -9, -1}, this.back, this.body, this.legFL1, this.legFL2, this.legFL3);
		this.ikBR = new SimpleIK(Axis.Z, false, this.back, this.legBR1, this.legBR2, this.legBR3);
		this.ikBL = new SimpleIK(Axis.Z, false, this.back, this.legBL1, this.legBL2, this.legBL3);

		this.legFR3.rotationPointX = -10;
		this.legFL3.rotationPointX = 10;
		this.legBR3.rotationPointX = -8;
		this.legBL3.rotationPointX = 8;
	}

	public ModelMonk() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		this.head.isHidden = this.isChild;
		this.childHead.isHidden = !this.isChild;
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.back.render(scale);
			this.legFL1.render(scale);
			this.legFR1.render(scale);
			this.legFL3.render(scale);
			this.legFR3.render(scale);
			this.legBL1.render(scale);
			this.legBR1.render(scale);
			this.legBL3.render(scale);
			this.legBR3.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.back.render(scale);
			this.legFL1.render(scale);
			this.legFR1.render(scale);
			this.legFL3.render(scale);
			this.legFR3.render(scale);
			this.legBL1.render(scale);
			this.legBR1.render(scale);
			this.legBL3.render(scale);
			this.legBR3.render(scale);
		}
	}

	static final float speed = 0.65F;
	static final float bRadius = 12;
	static final float pi = (float) Math.PI;
	static final float shift = pi/2;
	
	public static float[] getPassangerOffset(float[] vec, float limbSwing, float limbSwingAmount) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
		vec[0] = MathHelper.sin(limbSwing) * 0.15F * limbSwingAmount;
		vec[1] = 0;
		vec[2] = 0;
		return vec;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
		float f = MathHelper.sin(limbSwing) * 0.15F * limbSwingAmount;
		float f1 = MathHelper.cos(limbSwing * 2) * 0.01F * limbSwingAmount;
        float f2 = ((EntityMonk)entity).getStandingAnimationScale(ageInTicks - entity.ticksExisted);
        f2 *= f2;
		this.back.rotateAngleX = pi / 2.4F + f1 - pi * f2 / 6;
		this.back.rotateAngleZ = f;
		this.head.rotateAngleX = headPitch * 0.0175F * 0.9F - pi / 2 - f1 + pi * f2 / 6;
		this.head.rotateAngleY = -f;
		this.head.rotateAngleZ = -netHeadYaw * 0.0175F * 0.5F;

		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.childHead.rotateAngleZ = this.head.rotateAngleZ;

		float[] baseR = ikFR.calculateBasePoint();
		float[] baseL = ikFL.calculateBasePoint();

		float yL = -MathHelper.sin(limbSwing) * bRadius;
		float yR;
		float zL;
		float zR;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / 4;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= 4;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR = yR * limbSwingAmount + 23;
		zR = zR * limbSwingAmount - 13.35F + 3 * limbSwingAmount;

		yL = yL * limbSwingAmount + 23;
		zL = zL * limbSwingAmount - 13.35F + 3 * limbSwingAmount;

		if (f2 > 0) {
			f2 *= pi;
			int centreY = -2;
			int centreZ = -1;			
			float[] xRot = SimpleIK.rotateX(new float[] { 0, yR - centreY, zR - centreZ }, -f2 * 0.75F);
			this.legFR3.rotationPointY = xRot[1] + centreY;
			this.legFR3.rotationPointZ = xRot[2] + centreZ;
			this.legFR4.rotateAngleX = -MathHelper.cos(limbSwing) * pi/6 * limbSwingAmount - pi/18;
			this.legFR3.rotateAngleX = -this.legFR4.rotateAngleX - f2;

			xRot = SimpleIK.rotateX(new float[] { 0, yL - centreY, zL - centreZ }, -f2 * 0.75F);
			this.legFL3.rotationPointY = xRot[1] + centreY;
			this.legFL3.rotationPointZ = xRot[2] + centreZ;
			this.legFL4.rotateAngleX = -MathHelper.cos(limbSwing + pi) * pi/6 * limbSwingAmount - pi/18;
			this.legFL3.rotateAngleX = -this.legFL4.rotateAngleX - f2;
		} else {
			this.legFR3.rotationPointY = yL;
			this.legFR3.rotationPointZ = zL;
			this.legFR3.rotateAngleX = MathHelper.cos(limbSwing) * pi/6 * limbSwingAmount + pi/18;
			this.legFR4.rotateAngleX = -this.legFR3.rotateAngleX;
	
			float x = this.legFR3.rotationPointX - baseR[0];
			float y = this.legFR3.rotationPointY - baseR[1];
			float z = this.legFR3.rotationPointZ - baseR[2];
			float l = (float) Math.sqrt(x * x + y * y + z * z);
			this.legFR3.rotateAngleZ = (float) -Math.atan(x/l);
	
			this.legFL3.rotationPointY = yR;
			this.legFL3.rotationPointZ = zR;
			this.legFL3.rotateAngleX = MathHelper.cos(limbSwing + pi) * pi/6 * limbSwingAmount + pi/18;
			this.legFL4.rotateAngleX = -this.legFL3.rotateAngleX;
	
			x = this.legFL3.rotationPointX - baseL[0];
			y = this.legFL3.rotationPointY - baseL[1];
			z = this.legFL3.rotationPointZ - baseL[2];
			l = (float) Math.sqrt(x * x + y * y + z * z);
			this.legFL3.rotateAngleZ = (float) -Math.atan(x/l);
		}
		this.ikFR.rotateBones(-pi/2, baseR);
		this.ikFL.rotateBones(-pi/2, baseL);

		// Shift
		limbSwing -= shift;
		yL = -MathHelper.sin(limbSwing) * bRadius;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / 4;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= 4;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.legBR3.rotationPointY = yR + 23;
		this.legBR3.rotationPointZ = zR + 15;

		this.legBL3.rotationPointY = yL + 23;
		this.legBL3.rotationPointZ = zL + 15;

		this.ikBR.rotateBones(0);
		this.ikBL.rotateBones(0);
	}
}