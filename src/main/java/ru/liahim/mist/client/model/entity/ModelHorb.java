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
import ru.liahim.mist.entity.EntityHorb;

@SideOnly(Side.CLIENT)
public class ModelHorb extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer nose1;
	public ModelRenderer nose2;
	public ModelRenderer nose3;
	public ModelRenderer legFR1;
	public ModelRenderer legFR2;
	public ModelRenderer legFR3;
	public ModelRenderer legFL1;
	public ModelRenderer legFL2;
	public ModelRenderer legFL3;
	public ModelRenderer legBR1;
	public ModelRenderer legBR2;
	public ModelRenderer legBR3;
	public ModelRenderer legBL1;
	public ModelRenderer legBL2;
	public ModelRenderer legBL3;
	private SimpleIK ikFR;
	private SimpleIK ikFL;
	private SimpleIK ikBR;
	private SimpleIK ikBL;
	protected float childYOffset = 8.0F;
	protected float childZOffset = 4.0F;

	public ModelHorb(float scale) {
		this.textureWidth = 96;
		this.textureHeight = 64;
		this.back = new ModelRenderer(this, 0, 29);
		this.back.addBox(-5, -11, -5, 10, 13, 9, scale);
		this.back.setTextureOffset(40, 0);
		this.back.addBox(-4, -11, -10, 8, 11, 5, scale);
		this.back.setRotationPoint(0, 2, 10);
		this.back.rotateAngleX = (float) Math.PI * 7 / 36;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-6, -13, -16, 12, 13, 16, scale);
		this.body.setRotationPoint(0, -7, 4);
		this.body.rotateAngleX = (float) -Math.PI/12;
		this.back.addChild(this.body);
		this.head = new ModelRenderer(this, 66, 0);
		this.head.addBox(-4, -1, -5, 8, 9, 7, scale);
		this.head.setRotationPoint(0, -11, -16);
		this.head.rotateAngleX = (float) -Math.PI/12;
		this.body.addChild(this.head);
		this.nose1 = new ModelRenderer(this, 0, 51);
		this.nose1.addBox(-2, 0, -1, 4, 6, 5, scale);
		this.nose1.setRotationPoint(0, 3, -5);
		this.nose1.rotateAngleX = (float) -Math.PI * 7 / 36;
		this.head.addChild(this.nose1);
		this.nose2 = new ModelRenderer(this, 0, 0);
		this.nose2.addBox(-1.5F, 0, 0, 3, 12, 3, scale);
		this.nose2.setRotationPoint(0, 6, -1);
		this.nose2.rotateAngleX = (float) Math.PI / 7.2F;
		this.nose1.addChild(this.nose2);
		this.nose3 = new ModelRenderer(this, 0, 29);
		this.nose3.addBox(-1, 0, 0, 2, 6, 2, scale);
		this.nose3.setRotationPoint(0, 12, 0);
		this.nose3.rotateAngleX = (float) Math.PI / 12F;
		this.nose2.addChild(this.nose3);

		this.legFR1 = new ModelRenderer(this, 38, 29);
		this.legFR1.addBox(-3, -2, -2, 4, 10, 6, scale);
		this.legFR1.setRotationPoint(-4, -1, -15);
		//this.body.addChild(this.legFR1);
		this.legFR2 = new ModelRenderer(this, 60, 30);
		this.legFR2.addBox(-2.01F, -1, 0, 3, 9, 3, scale);
		this.legFR2.setRotationPoint(0, 7, -2);
		this.legFR1.addChild(this.legFR2);
		this.legFR3 = new ModelRenderer(this, 56, 16);
		this.legFR3.addBox(-1.02F, -12, 0, 2, 12, 2, scale);
		this.legFR3.setRotationPoint(0, 20, 0);
		//this.legFR2.addChild(this.legFR3);
		this.legFL1 = new ModelRenderer(this, 32, 45);
		this.legFL1.addBox(-1, -2, -2, 4, 10, 6, scale);
		this.legFL1.setRotationPoint(4, -1, -15);
		//this.body.addChild(this.legFL1);
		this.legFL2 = new ModelRenderer(this, 72, 30);
		this.legFL2.addBox(-0.99F, -1, 0, 3, 9, 3, scale);
		this.legFL2.setRotationPoint(0, 7, -2);
		this.legFL1.addChild(this.legFL2);
		this.legFL3 = new ModelRenderer(this, 64, 16);
		this.legFL3.addBox(-0.98F, -12, 0, 2, 12, 2, scale);
		this.legFL3.setRotationPoint(0, 20, 0);
		//this.legFL2.addChild(this.legFL3);
		this.legBR1 = new ModelRenderer(this, 56, 42);
		this.legBR1.addBox(-3, -2, -2, 4, 8, 6, scale);
		this.legBR1.setRotationPoint(-3, 0, 0);
		//this.back.addChild(this.legBR1);
		this.legBR2 = new ModelRenderer(this, 84, 30);
		this.legBR2.addBox(-2.01F, 0, -1, 3, 9, 3, scale);
		this.legBR2.setRotationPoint(0, 6, 1);
		this.legBR1.addChild(this.legBR2);
		this.legBR3 = new ModelRenderer(this, 72, 16);
		this.legBR3.addBox(-1.02F, -12, -2, 2, 12, 2, scale);
		this.legBR3.setRotationPoint(0, 21, 2);
		//this.legBR2.addChild(this.legBR3);
		this.legBL1 = new ModelRenderer(this, 70, 50);
		this.legBL1.addBox(-1, -2, -2, 4, 8, 6, scale);
		this.legBL1.setRotationPoint(3, 0, 0);
		//this.back.addChild(this.legBL1);
		this.legBL2 = new ModelRenderer(this, 84, 42);
		this.legBL2.addBox(-0.99F, 0, -1, 3, 9, 3, scale);
		this.legBL2.setRotationPoint(0, 6, 1);
		this.legBL1.addChild(this.legBL2);
		this.legBL3 = new ModelRenderer(this, 80, 16);
		this.legBL3.addBox(-0.98F, -12, -2, 2, 12, 2, scale);
		this.legBL3.setRotationPoint(0, 21, 2);
		//this.legBL2.addChild(this.legBL3);

		this.ikFR = new SimpleIK(Axis.Z, true, new float[] {0, -12, 0}, this.back, this.body, this.legFR1, this.legFR2, this.legFR3);
		this.ikFL = new SimpleIK(Axis.Z, true, new float[] {0, -12, 0}, this.back, this.body, this.legFL1, this.legFL2, this.legFL3);
		this.ikBR = new SimpleIK(Axis.Z, false, new float[] {0, -12, 0}, this.back, this.legBR1, this.legBR2, this.legBR3);
		this.ikBL = new SimpleIK(Axis.Z, false, new float[] {0, -12, 0}, this.back, this.legBL1, this.legBL2, this.legBL3);

		this.legFR3.rotationPointX = -4;
		this.legFL3.rotationPointX = 4;
		this.legBR3.rotationPointX = -3;
		this.legBL3.rotationPointX = 3;
	}

	public ModelHorb() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.back.render(scale);
			this.legFL1.render(scale);
			this.legFL3.render(scale);
			this.legFR1.render(scale);
			this.legFR3.render(scale);
			this.legBL1.render(scale);
			this.legBL3.render(scale);
			this.legBR1.render(scale);
			this.legBR3.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.back.render(scale);
			this.legFL1.render(scale);
			this.legFL3.render(scale);
			this.legFR1.render(scale);
			this.legFR3.render(scale);
			this.legBL1.render(scale);
			this.legBL3.render(scale);
			this.legBR1.render(scale);
			this.legBR3.render(scale);
		}
	}

	static final float speed = 0.7F;
	static final float bRadius = 13;
	static final float pi = (float) Math.PI;
	static final float shift = pi/2;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
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

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.legFR3.rotationPointY = yL + 24;
		this.legFR3.rotationPointZ = zL - 9 + 2 * limbSwingAmount;
		this.legFR3.rotateAngleX = MathHelper.cos(limbSwing) * pi/9 * limbSwingAmount;

		this.legFL3.rotationPointY = yR + 24;
		this.legFL3.rotationPointZ = zR - 9 + 2 * limbSwingAmount;
		this.legFL3.rotateAngleX = MathHelper.cos(limbSwing + pi) * pi/9 * limbSwingAmount;
		
		float fr = (MathHelper.cos(limbSwing * 2) * 0.04F + 0.02F) * limbSwingAmount;
		
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

		this.legBR3.rotationPointY = yR + 24;
		this.legBR3.rotationPointZ = zR + 13;
		this.legBR3.rotateAngleX = MathHelper.cos(limbSwing + pi) * pi/9 * limbSwingAmount;

		this.legBL3.rotationPointY = yL + 24;
		this.legBL3.rotationPointZ = zL + 13;
		this.legBL3.rotateAngleX = MathHelper.cos(limbSwing) * pi/9 * limbSwingAmount;

		float fm = (MathHelper.cos(limbSwing * 2) * 0.8F + 0.4F) * limbSwingAmount;

		this.back.rotationPointY = 2 + fm;
		fr -= fm * 0.07F;
		this.back.rotateAngleX = pi * 7 / 36 + fr;

		this.head.rotateAngleX = headPitch * 0.015F + -pi / 12 - fr;
		this.head.rotateAngleY = headYaw * 0.01F;
		this.head.rotateAngleZ = -headYaw * 0.003F;

		ageInTicks += ((EntityHorb)entity).animShift;
		float xr = (MathHelper.cos(ageInTicks * 0.25F) * 0.05F + 0.04F) * MathHelper.abs(MathHelper.cos(ageInTicks * 0.01F) * MathHelper.sin(ageInTicks * 0.001F)) - headPitch * 0.005F;
		float zr = (MathHelper.sin(ageInTicks * 0.25F) * 0.03F) * MathHelper.cos(ageInTicks * 0.007F) * MathHelper.cos(ageInTicks * 0.001F);

		this.nose1.rotateAngleX = -pi * 7 / 36 + xr;
		this.nose1.rotateAngleZ = zr;
		this.nose2.rotateAngleX = pi / 7.2F + xr;
		this.nose2.rotateAngleZ = zr;
		this.nose3.rotateAngleX = pi / 12 + xr;
		this.nose3.rotateAngleZ = zr;

		this.ikFR.rotateBones(0);
		this.ikFL.rotateBones(0);
		this.ikBR.rotateBones(0);
		this.ikBL.rotateBones(0);
	}
}