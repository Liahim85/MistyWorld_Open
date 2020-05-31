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
public class ModelSniff extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer nose;
	public ModelRenderer childHead;
	public ModelRenderer childNose;
	public ModelRenderer body;
	public ModelRenderer hip;
	public ModelRenderer back;
	public ModelRenderer tail;
	public ModelRenderer legFR1;
	public ModelRenderer legFL1;
	public ModelRenderer legFR2;
	public ModelRenderer legFL2;
	public ModelRenderer legBR1;
	public ModelRenderer legBL1;
	public ModelRenderer legBR2;
	public ModelRenderer legBL2;
	public ModelRenderer targetR;
	public ModelRenderer targetL;
	private SimpleIK ikR;
	private SimpleIK ikL;
	protected float childYOffset = 8.0F;
	protected float childZOffset = 4.0F;

	public ModelSniff(float scale) {
		this.textureWidth = 96;
		this.textureHeight = 64;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-7, -15, -13.5F, 14, 17, 15, scale);
		this.body.setRotationPoint(0, 14, 8);
		// this.body.rotateAngleX = (float) -Math.PI/7.2F;
		this.hip = new ModelRenderer(this, 45, 19);
		this.hip.addBox(-6, 0, -13, 12, 13, 13, scale);
		this.hip.setRotationPoint(0, -14, -13.5F);
		// this.hip.rotateAngleX = (float) Math.PI/6;
		this.body.addChild(this.hip);
		this.head = new ModelRenderer(this, 43, 0);
		this.head.addBox(-4, -2, -8, 8, 7, 8, scale);
		this.head.setTextureOffset(0, 0);
		this.head.addBox(-5, -3, -3, 2, 2, 2, scale);
		this.head.setTextureOffset(0, 4);
		this.head.addBox(3, -3, -3, 2, 2, 2, scale);
		this.head.setRotationPoint(0, 4, -13);
		this.hip.addChild(this.head);
		this.nose = new ModelRenderer(this, 0, 32);
		this.nose.addBox(-5, -4, -6, 10, 9, 6, scale);
		this.nose.setRotationPoint(0, 1, -7);
		this.head.addChild(this.nose);
		this.back = new ModelRenderer(this, 0, 48);
		this.back.addBox(-5, 0, -1, 10, 10, 6, scale);
		this.back.setRotationPoint(0, -11, 1.5F);
		this.back.rotateAngleX = (float) -Math.PI / 9;
		this.body.addChild(this.back);
		this.tail = new ModelRenderer(this, 26, 32);
		this.tail.addBox(-2, 0, 0, 4, 3, 3, scale);
		this.tail.setRotationPoint(0, 5, 5);
		this.tail.rotateAngleX = (float) -Math.PI / 6;
		this.back.addChild(this.tail);
		
		this.childHead = new ModelRenderer(this, 43, 0);
		this.childHead.addBox(-4, -2, -9, 8, 7, 8, 1);
		this.childHead.setTextureOffset(0, 0);
		this.childHead.addBox(-6, -4, -3.5F, 2, 2, 2, 0.25F);
		this.childHead.setTextureOffset(0, 4);
		this.childHead.addBox(4, -4, -3.5F, 2, 2, 2, 0.25F);
		this.childHead.setRotationPoint(0, 4, -13);
		this.hip.addChild(this.childHead);
		this.childNose = new ModelRenderer(this, 0, 32);
		this.childNose.addBox(-5, -4, -9, 10, 9, 6, 1.25F);
		this.childNose.setRotationPoint(0, 1, -7);
		this.childHead.addChild(this.childNose);

		this.legFR1 = new ModelRenderer(this, 32, 38);
		this.legFR1.addBox(-1, -1, -1.5F, 3, 8, 3, scale);
		this.legFR1.setRotationPoint(-6, 12, -10.5F);
		// this.hip.addChild(this.legFR1);
		this.legFL1 = new ModelRenderer(this, 82, 21);
		this.legFL1.addBox(-2, -1, -1.5F, 3, 8, 3, scale);
		this.legFL1.setRotationPoint(6, 12, -10.5F);
		// this.hip.addChild(this.legFL1);
		this.legFR2 = new ModelRenderer(this, 32, 49);
		this.legFR2.addBox(-1.01F, 0, -1, 4, 10, 5, scale);
		this.legFR2.setRotationPoint(0, 7, -0.5F);
		this.legFR1.addChild(this.legFR2);
		this.legFL2 = new ModelRenderer(this, 75, 0);
		this.legFL2.addBox(-2.99F, 0, -1, 4, 10, 5, scale);
		this.legFL2.setRotationPoint(0, 7, -0.5F);
		this.legFL1.addChild(this.legFL2);
		this.legBR1 = new ModelRenderer(this, 45, 45);
		this.legBR1.addBox(-1, -1, -1.5F, 3, 3, 3, scale);
		this.legBR1.setRotationPoint(-7, 14, 8);
		this.legBL1 = new ModelRenderer(this, 57, 45);
		this.legBL1.addBox(-2, -1, -1.5F, 3, 3, 3, scale);
		this.legBL1.setRotationPoint(7, 14, 8);
		this.legBR2 = new ModelRenderer(this, 50, 51);
		this.legBR2.addBox(-1, 0, -1, 4, 8, 5, scale);
		this.legBR2.setRotationPoint(0, 2, -0.5F);
		this.legBR1.addChild(this.legBR2);
		this.legBL2 = new ModelRenderer(this, 68, 51);
		this.legBL2.addBox(-3, 0, -1, 4, 8, 5, scale);
		this.legBL2.setRotationPoint(0, 2, -0.5F);
		this.legBL1.addChild(this.legBL2);

		this.targetR = new ModelRenderer(this, 28, 0);
		this.targetR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetR.setRotationPoint(0, 10, 1);
		this.targetL = new ModelRenderer(this, 28, 0);
		this.targetL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetL.setRotationPoint(0, 10, 1);

		this.ikR = new SimpleIK(Axis.Z, false, this.body, this.hip, this.legFR1, this.legFR2, this.targetR);
		this.ikL = new SimpleIK(Axis.Z, false, this.body, this.hip, this.legFL1, this.legFL2, this.targetL);

		this.targetR.rotationPointX = -6;
		this.targetL.rotationPointX = 6;
	}

	public ModelSniff() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		this.childHead.isHidden = !this.isChild;
		this.head.isHidden = this.isChild;
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.body.render(scale);
			this.legBL1.render(scale);
			this.legBR1.render(scale);
			this.legFL1.render(scale);
			this.legFR1.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.body.render(scale);
			this.legFL1.render(scale);
			this.legFR1.render(scale);
			this.legBL1.render(scale);
			this.legBR1.render(scale);
		}
	}

	static final float speed = 0.8F;
	static final float bRadius = 10;
	static final float pi = (float) Math.PI;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
		float s = limbSwingAmount * pi / 36;
		this.body.rotateAngleX = -pi / 6 + s * 2;
		this.hip.rotateAngleX = pi / 6 - s;
		this.head.rotateAngleX = headPitch * 0.015F + pi / 6 - s;
		this.head.rotateAngleY = headYaw * 0.008F;
		float f = MathHelper.cos(limbSwing) * 0.8F * limbSwingAmount;
		this.legBR1.rotateAngleX = -f;
		this.legBL1.rotateAngleX = f;
		this.back.rotateAngleY = -f * 0.2F;

		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;

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

		this.targetR.rotationPointY = yL + 24;
		this.targetR.rotationPointZ = zL - 8F;

		this.targetL.rotationPointY = yR + 24;
		this.targetL.rotationPointZ = zR - 8F;

		this.ikR.rotateBones(pi / 15);
		this.ikL.rotateBones(-pi / 15);
	}
}