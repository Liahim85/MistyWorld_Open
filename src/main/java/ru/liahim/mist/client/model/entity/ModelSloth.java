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
public class ModelSloth extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer nose;
	public ModelRenderer childHead;
	public ModelRenderer childNose;
	public ModelRenderer neck;
	public ModelRenderer body;
	public ModelRenderer legBR;
	public ModelRenderer legBL;
	public ModelRenderer legFR1;
	public ModelRenderer legFL1;
	public ModelRenderer legFR2;
	public ModelRenderer legFL2;
	public ModelRenderer legFR3;
	public ModelRenderer legFL3;
	public ModelRenderer targetR;
	public ModelRenderer targetL;
	private SimpleIK ikR;
	private SimpleIK ikL;
	protected float childYOffset = 8.0F;
	protected float childZOffset = 4.0F;

	public ModelSloth(float scale) {
		this.textureWidth = 96;
		this.textureHeight = 64;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-5, -20, -4, 10, 28, 12, scale);
		this.body.setRotationPoint(0, 9, 8);
		this.neck = new ModelRenderer(this, 44, 0);
		this.neck.addBox(-6, -9, -6, 12, 12, 12, scale);
		this.neck.setRotationPoint(0, -17, 0);
		this.body.addChild(this.neck);
		this.head = new ModelRenderer(this, 0, 42);
		this.head.addBox(-3, -14, -3, 6, 16, 6, scale);
		this.head.setRotationPoint(0, -1, -4);
		this.neck.addChild(this.head);
		this.nose = new ModelRenderer(this, 0, 0);
		this.nose.addBox(-1.5F, -3, -1, 3, 4, 3, scale);
		this.nose.setRotationPoint(0, -13, -2);
		this.nose.rotateAngleX = (float) Math.PI / 8;
		this.head.addChild(this.nose);
		
		this.childHead = new ModelRenderer(this, 0, 42);
		this.childHead.addBox(-3, -13, -3, 6, 15, 6, 0.5f); // !!!
		this.childHead.setRotationPoint(0, -2, -4);
		this.neck.addChild(this.childHead);
		this.childNose = new ModelRenderer(this, 0, 0);
		this.childNose.addBox(-1.5F, -3, -1, 3, 4, 3, 0.5f); // !!!
		this.childNose.setRotationPoint(0, -12.5f, -2.5f);
		this.childNose.rotateAngleX = (float) Math.PI / 8;
		this.childHead.addChild(this.childNose);

		this.legBR = new ModelRenderer(this, 24, 42);
		this.legBR.addBox(-2, -3, -5, 3, 14, 8, scale);
		this.legBR.setTextureOffset(32, 0);
		this.legBR.addBox(-1, 11, -5, 2, 3, 3, scale);
		this.legBR.setRotationPoint(-4, 9, 8);
		this.legBL = new ModelRenderer(this, 46, 42);
		this.legBL.addBox(-1, -3, -5, 3, 14, 8, scale);
		this.legBL.setTextureOffset(46, 0);
		this.legBL.addBox(-1, 11, -5, 2, 3, 3, scale);
		this.legBL.setRotationPoint(4, 9, 8);
		this.legFR1 = new ModelRenderer(this, 44, 24);
		this.legFR1.addBox(-3, -2, -1, 5, 13, 3, scale);
		this.legFR1.setRotationPoint(-5, -14, -2);
		this.legFL1 = new ModelRenderer(this, 60, 24);
		this.legFL1.addBox(-2, -2, -1, 5, 13, 3, scale);
		this.legFL1.setRotationPoint(5, -14, -2);
		this.legFR2 = new ModelRenderer(this, 68, 48);
		this.legFR2.addBox(-2, -1, -1, 3, 14, 2, scale);
		this.legFR2.setRotationPoint(0, 10, 0.5F);
		this.legFL2 = new ModelRenderer(this, 78, 48);
		this.legFL2.addBox(-1, -1, -1, 3, 14, 2, scale);
		this.legFL2.setRotationPoint(0, 10, 0.5F);
		this.legFR1.addChild(this.legFR2);
		this.legFL1.addChild(this.legFL2);
		this.legFR3 = new ModelRenderer(this, 80, 0);
		this.legFR3.addBox(-1, -1, -0.5F, 2, 5, 1, scale);
		this.legFR3.setRotationPoint(0, 13, 0);
		this.legFL3 = new ModelRenderer(this, 86, 0);
		this.legFL3.addBox(-1, -1, -0.5F, 2, 5, 1, scale);
		this.legFL3.setRotationPoint(0, 13, 0);
		this.legFR2.addChild(this.legFR3);
		this.legFL2.addChild(this.legFL3);

		this.targetR = new ModelRenderer(this, 12, 0);
		this.targetR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetR.setRotationPoint(3, 17, -0.5F);
		this.targetL = new ModelRenderer(this, 12, 0);
		this.targetL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetL.setRotationPoint(-3, 17, -0.5F);

		this.ikR = new SimpleIK(Axis.X, false, this.body, this.legFR1, this.legFR2, this.targetR);
		this.ikL = new SimpleIK(Axis.X, true, this.body, this.legFL1, this.legFL2, this.targetL);

		this.targetR.rotationPointX = -5;
		this.targetL.rotationPointX = 5;
	}

	public ModelSloth() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.head.isHidden = true;
			this.childHead.isHidden = false;
			this.body.render(scale);
			this.legFR1.render(scale);
			this.legFL1.render(scale);
			this.legBR.render(scale);
			this.legBL.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.head.isHidden = false;
			this.childHead.isHidden = true;
			this.body.render(scale);
			this.legFR1.render(scale);
			this.legFL1.render(scale);
			this.legBR.render(scale);
			this.legBL.render(scale);
		}
	}

	final float speed = 0.5F;
	final float radius = 15;
	static final float pi = (float) Math.PI;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		float f = MathHelper.sin(this.swingProgress * pi);
		float f1 = MathHelper.cos(limbSwing * 2) * 0.01F * limbSwingAmount;
		this.head.rotateAngleX = headPitch * pi * 0.005F + pi * 9 / 16 - f1;
		this.head.rotateAngleY = headYaw * pi * 0.003F;
		this.head.rotateAngleZ = -headYaw * 0.002F;
		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.childHead.rotateAngleZ = this.head.rotateAngleZ;
		this.neck.rotateAngleX = headPitch * pi * 0.0005F - pi / 4;
		this.neck.rotateAngleY = headYaw * pi * 0.00025F;
		this.body.rotateAngleX = pi / 4 + f1;
		this.body.rotateAngleX -= f * 0.2f;
		this.legBR.rotateAngleX = MathHelper.cos(limbSwing + 2.0F) * limbSwingAmount + pi / 9;
		this.legBL.rotateAngleX = -MathHelper.cos(limbSwing + 2.0F) * limbSwingAmount + pi / 9;

		float yL = -MathHelper.sin(limbSwing) * radius;
		float yR;
		float zL;
		float zR;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * radius;
			yR = -MathHelper.sin(limbSwing + pi) * radius / 4;
			zR = MathHelper.cos(limbSwing + pi) * radius;
		} else {
			yL /= 4;
			zL = MathHelper.cos(limbSwing) * radius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * radius;
		}

		yR = yR * limbSwingAmount + 24;
		zR = zR * Math.min(limbSwingAmount, 1) - 7;

		yL = yL * limbSwingAmount + 24;
		zL = zL * Math.min(limbSwingAmount, 1) - 7;

		this.legFR3.rotateAngleZ = (-1F + f)*pi/4;
		this.legFL3.rotateAngleZ = (1F - f)*pi/4;

		if (f > 0) {
			float[] base = ikR.calculateBasePoint();
			float[] xRot = SimpleIK.rotateX(new float[] { 0, yR - base[1], zR - base[2] }, -f * 1.5F);
			this.targetR.rotationPointY = xRot[1] + base[1];
			this.targetR.rotationPointZ = xRot[2] + base[2];
			this.ikR.rotateBones(pi*4/9, base);

			base = ikL.calculateBasePoint();
			xRot = SimpleIK.rotateX(new float[] { 0, yL - base[1], zL - base[2] }, -f * 1.5F);
			this.targetL.rotationPointY = xRot[1] + base[1];
			this.targetL.rotationPointZ = xRot[2] + base[2];
			this.ikL.rotateBones(pi*4/9, base);
		} else {
			this.targetR.rotationPointY = yR;
			this.targetR.rotationPointZ = zR;
	
			this.targetL.rotationPointY = yL;
			this.targetL.rotationPointZ = zL;
			
			this.ikR.rotateBones(pi*4/9);
			this.ikL.rotateBones(pi*4/9);
		}
	}
}
