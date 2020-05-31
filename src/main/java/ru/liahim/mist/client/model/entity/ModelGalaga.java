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
import ru.liahim.mist.entity.EntityGalaga;

@SideOnly(Side.CLIENT)
public class ModelGalaga extends ModelBase {

	public ModelRenderer body;
	public ModelRenderer tail1;
	public ModelRenderer tail2;
	public ModelRenderer neck;
	public ModelRenderer head;
	public ModelRenderer jaw;
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

	public ModelGalaga(float scale) {
		this.textureWidth = 96;
		this.textureHeight = 96;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-8, -13, -9, 16, 15, 18, scale);
		this.body.setRotationPoint(0, 8, 0);
		this.body.rotateAngleX = (float) Math.PI/36;

		this.neck = new ModelRenderer(this, 0, 33);
		this.neck.addBox(-6, 0, -10, 12, 12, 11, scale);
		this.neck.setRotationPoint(0, -11, -9);
		this.neck.rotateAngleX = (float) Math.PI/9;
		this.body.addChild(this.neck);

		this.head = new ModelRenderer(this, 46, 33);
		this.head.addBox(-4, 0, -6, 8, 9, 9, scale);
		this.head.setTextureOffset(0, 82);
		this.head.addBox(-4, 0, -12, 8, 7, 6, scale);
		this.head.setRotationPoint(0, 2, -10);
		this.head.rotateAngleX = (float) Math.PI/36;
		this.neck.addChild(this.head);

		this.jaw = new ModelRenderer(this, 28, 82);
		this.jaw.addBox(-4, 0, -6, 8, 2, 6, scale);
		this.jaw.setRotationPoint(0, 7, -6);
		this.head.addChild(this.jaw);

		this.tail1 = new ModelRenderer(this, 0, 56);
		this.tail1.addBox(-5, 0, 0, 10, 12, 13, scale);
		this.tail1.setRotationPoint(0, -11, 9);
		this.tail1.rotateAngleX = (float) -Math.PI/9;
		this.body.addChild(this.tail1);

		this.tail2 = new ModelRenderer(this, 50, 0);
		this.tail2.addBox(-3, 0, 0, 6, 9, 8, scale);
		this.tail2.setRotationPoint(0, 3, 13);
		this.tail2.rotateAngleX = (float) -Math.PI/12;
		this.tail1.addChild(this.tail2);

		this.legBR1 = new ModelRenderer(this, 33, 56);
		this.legBR1.addBox(-1, 0, -1.5F, 3, 8, 3, scale);
		this.legBR1.setRotationPoint(-8, 0, 7.5F);
		// this.hip.addChild(this.legBR1);
		this.legBR2 = new ModelRenderer(this, 76, 12);
		this.legBR2.addBox(-1.5F, 0, -2.5F, 5, 10, 5, scale);
		this.legBR2.setRotationPoint(0, 8, 0);
		this.legBR1.addChild(this.legBR2);
		this.legBL1 = new ModelRenderer(this, 45, 56);
		this.legBL1.addBox(-2, 0, -1.5F, 3, 8, 3, scale);
		this.legBL1.setRotationPoint(8, 0, 7.5F);
		// this.hip.addChild(this.legBL1);
		this.legBL2 = new ModelRenderer(this, 76, 27);
		this.legBL2.addBox(-3.5F, 0, -2.5F, 5, 10, 5, scale);
		this.legBL2.setRotationPoint(0, 8, 0);
		this.legBL1.addChild(this.legBL2);
		this.legFR1 = new ModelRenderer(this, 57, 56);
		this.legFR1.addBox(-1, 0, -1.5F, 3, 7, 3, scale);
		this.legFR1.setRotationPoint(-6, 9, -7);
		// this.body.addChild(this.legFR1);
		this.legFR2 = new ModelRenderer(this, 46, 67);
		this.legFR2.addBox(-1.5F, 0, -2.5F, 5, 10, 5, scale);
		this.legFR2.setRotationPoint(0, 7, 0);
		this.legFR1.addChild(this.legFR2);
		this.legFL1 = new ModelRenderer(this, 69, 56);
		this.legFL1.addBox(-2, 0, -1.5F, 3, 7, 3, scale);
		this.legFL1.setRotationPoint(6, 9, -7);
		// this.body.addChild(this.legFL1);
		this.legFL2 = new ModelRenderer(this, 66, 67);
		this.legFL2.addBox(-3.5F, 0, -2.5F, 5, 10, 5, scale);
		this.legFL2.setRotationPoint(0, 7, 0);
		this.legFL1.addChild(this.legFL2);

		this.targetBR = new ModelRenderer(this, 18, 0);
		this.targetBR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetBR.setRotationPoint(0, 11, 0);
		this.targetBL = new ModelRenderer(this, 18, 0);
		this.targetBL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetBL.setRotationPoint(0, 11, 0);
		this.targetFR = new ModelRenderer(this, 18, 0);
		this.targetFR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetFR.setRotationPoint(0, 10, 0);
		this.targetFL = new ModelRenderer(this, 18, 0);
		this.targetFL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetFL.setRotationPoint(0, 10, 0);

		this.ikBR = new SimpleIK(Axis.X, false, this.body, this.legBR1, this.legBR2, this.targetBR);
		this.ikBL = new SimpleIK(Axis.X, true, this.body, this.legBL1, this.legBL2, this.targetBL);
		this.ikFR = new SimpleIK(Axis.X, false, this.body, this.neck, this.legFR1, this.legFR2, this.targetFR);
		this.ikFL = new SimpleIK(Axis.X, true, this.body, this.neck, this.legFL1, this.legFL2, this.targetFL);

		this.targetBR.rotationPointX = -12;
		this.targetBL.rotationPointX = 12;
		this.targetFR.rotationPointX = -10;
		this.targetFL.rotationPointX = 10;
	}

	public ModelGalaga() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.body.render(scale);
			this.legBR1.render(scale);
			this.legBL1.render(scale);
			this.legFR1.render(scale);
			this.legFL1.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.body.render(scale);
			this.legBR1.render(scale);
			this.legBL1.render(scale);
			this.legFR1.render(scale);
			this.legFL1.render(scale);
		}
	}

	static final float speed = 0.65F;
	static final float bRadius = 12;
	static final float pi = (float) Math.PI;
	static final float shift = pi / 5;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
        float f = ((EntityGalaga)entity).getOpenMouthAnimationScale(ageInTicks - entity.ticksExisted);
        f *= f;
        f *= pi / 7.2F;
		this.head.rotateAngleX = headPitch * 0.01F + pi/36 - f;
		this.head.rotateAngleY = headYaw * 0.005F;
		this.jaw.rotateAngleX = f * 2;

		float bodyRot = MathHelper.cos(limbSwing) * 0.3F * limbSwingAmount;
		this.body.rotateAngleY = bodyRot / 2;
		this.neck.rotateAngleX = pi/9 + headPitch * 0.002F;
		this.neck.rotateAngleY = bodyRot / 2 + headYaw * 0.002F;
		bodyRot = MathHelper.cos(limbSwing - shift) * 0.3F * limbSwingAmount;
		this.tail1.rotateAngleY = -bodyRot / 2;
		bodyRot = MathHelper.cos(limbSwing - shift / 2) * 0.3F * limbSwingAmount;
		this.tail2.rotateAngleY = -bodyRot;

		float yL = -MathHelper.sin(limbSwing) * bRadius;
		float yR;
		float zL;
		float zR;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / 3;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= 3;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetFR.rotationPointX = yL - 10;
		this.targetFR.rotationPointY = yL + 24;
		this.targetFR.rotationPointZ = zL - 13;

		this.targetFL.rotationPointX = -yR + 10;
		this.targetFL.rotationPointY = yR + 24;
		this.targetFL.rotationPointZ = zR - 13;

		this.ikFR.rotateBones(pi/9 + zR * 0.1F);
		this.ikFL.rotateBones(pi/9 + zL * 0.1F);

		// Shift
		limbSwing -= shift;
		yL = -MathHelper.sin(limbSwing) * bRadius;
		if (yL > 0) {
			yL = 0;
			zL = ((((limbSwing) % pi) / pi) * 2 - 1) * bRadius;
			yR = -MathHelper.sin(limbSwing + pi) * bRadius / 3;
			zR = MathHelper.cos(limbSwing + pi) * bRadius;
		} else {
			yL /= 3;
			zL = MathHelper.cos(limbSwing) * bRadius;
			yR = 0;
			zR = ((((limbSwing + pi) % pi) / pi) * 2 - 1) * bRadius;
		}

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetBR.rotationPointX = yR - 12;
		this.targetBR.rotationPointY = yR + 24;
		this.targetBR.rotationPointZ = zR + 9;

		this.targetBL.rotationPointX = -yL + 12;
		this.targetBL.rotationPointY = yL + 24;
		this.targetBL.rotationPointZ = zL + 9;

		this.ikBR.rotateBones(-pi/9 - zR * 0.05F);
		this.ikBL.rotateBones(-pi/9 - zL * 0.05F);
	}
}