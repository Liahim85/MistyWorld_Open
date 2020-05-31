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
import ru.liahim.mist.entity.EntityGender;

@SideOnly(Side.CLIENT)
public class ModelBrachiodon extends ModelBase {

	public ModelRenderer body;
	public ModelRenderer bodyFur;
	public ModelRenderer hip;
	public ModelRenderer hipFur;
	public ModelRenderer tail;
	public ModelRenderer tailFur1;
	public ModelRenderer tailFur2;
	public ModelRenderer head;
	public ModelRenderer nouse1;
	public ModelRenderer nouse2;
	public ModelRenderer childHead;
	public ModelRenderer childNouse1;
	public ModelRenderer childNouse2;
	public ModelRenderer tuskL1;
	public ModelRenderer tuskL2;
	public ModelRenderer tuskR1;
	public ModelRenderer tuskR2;
	public ModelRenderer tuskML;
	public ModelRenderer tuskMR;
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

	public ModelBrachiodon(float scale) {
		this.textureWidth = 148;
		this.textureHeight = 148;
		this.body = new ModelRenderer(this, 6, 0);
		this.body.addBox(-10, -16, -11, 20, 26, 22, scale);
		this.body.setRotationPoint(0, -5, -5);
		this.body.rotateAngleX = (float) (Math.PI/9 + Math.PI/2);

		this.bodyFur = new ModelRenderer(this, 0, 91);
		this.bodyFur.addBox(-10, -16, -18, 20, 28, 29, scale);
		this.bodyFur.setRotationPoint(0, 0, 0);
		this.body.addChild(this.bodyFur);

		this.hip = new ModelRenderer(this, 70, 80);
		this.hip.addBox(-8, 0, -18, 16, 18, 18, scale);
		this.hip.setRotationPoint(0, 10, 8);
		this.hip.rotateAngleX = (float) (-Math.PI / 7.2F);
		this.body.addChild(this.hip);

		this.hipFur = new ModelRenderer(this, 66, 23);
		this.hipFur.addBox(-8, 0, -25, 16, 20, 25, scale);
		this.hipFur.setRotationPoint(0, 0, 0);
		this.hip.addChild(this.hipFur);

		this.tail = new ModelRenderer(this, 98, 116);
		this.tail.addBox(-6, 0, -12, 12, 20, 12, scale);
		this.tail.setRotationPoint(0, 18, -3);
		this.tail.rotateAngleX = (float) (-Math.PI/4);
		this.hip.addChild(this.tail);

		this.tailFur1 = new ModelRenderer(this, 3, 48);
		this.tailFur1.addBox(-6, 0, -17, 12, 25, 17, scale);
		this.tailFur1.setRotationPoint(0, 0, 0);
		this.tail.addChild(this.tailFur1);
		
		this.tailFur2 = new ModelRenderer(this, 88, 68);
		this.tailFur2.addBox(-3, -12, -3, 6, 9, 3, scale);
		this.tailFur2.setRotationPoint(0, 20, 0);
		this.tailFur2.rotateAngleX = (float) (Math.PI/2);
		this.tail.addChild(this.tailFur2);

		this.head = new ModelRenderer(this, 96, 0);
		this.head.addBox(-5, -4, -8, 10, 10, 10, scale);
		this.head.setRotationPoint(0, -16, -1);
		this.body.addChild(this.head);
		this.nouse1 = new ModelRenderer(this, 0, 90);
		this.nouse1.addBox(-3, 0, -5, 6, 7, 5, scale);
		this.nouse1.setRotationPoint(0, -1, -8);
		this.nouse1.rotateAngleX = (float) (Math.PI / 7.2F);
		this.head.addChild(this.nouse1);
		this.nouse2 = new ModelRenderer(this, 69, 86);
		this.nouse2.addBox(-2, 0, -4, 4, 5, 4, scale);
		this.nouse2.setRotationPoint(0, 1, -5);
		this.nouse2.rotateAngleX = (float) (Math.PI / 7.2F);
		this.nouse1.addChild(this.nouse2);
		
		this.childHead = new ModelRenderer(this, 96, 0);
		this.childHead.addBox(-5, -4, -8, 10, 10, 10, scale + 1);
		this.childHead.setRotationPoint(0, -16, -1);
		this.body.addChild(this.childHead);
		this.childNouse1 = new ModelRenderer(this, 0, 90);
		this.childNouse1.addBox(-3, 0, -5, 6, 7, 5, scale + 0.6F);
		this.childNouse1.setRotationPoint(0, -2F, -9);
		this.childNouse1.rotateAngleX = (float) (Math.PI / 7.2F);
		this.childHead.addChild(this.childNouse1);
		this.childNouse2 = new ModelRenderer(this, 69, 86);
		this.childNouse2.addBox(-2, 0, -4, 4, 5, 4, scale + 0.4F);
		this.childNouse2.setRotationPoint(0, 1, -5.6F);
		this.childNouse2.rotateAngleX = (float) (Math.PI / 7.2F);
		this.childNouse1.addChild(this.childNouse2);

		this.tuskR1 = new ModelRenderer(this, 140, 0);
		this.tuskR1.addBox(-1, 0, -1, 2, 10, 2, scale);
		this.tuskR1.setRotationPoint(-3, 4, -6);
		this.tuskR1.rotateAngleX = (float) (-Math.PI/3);
		this.tuskR1.rotateAngleY = (float) (Math.PI/15);
		this.tuskR1.rotateAngleZ = (float) (Math.PI/12);
		this.head.addChild(this.tuskR1);
		this.tuskR2 = new ModelRenderer(this, 140, 12);
		this.tuskR2.addBox(-1, 0, -2, 2, 5, 2, scale);
		this.tuskR2.setRotationPoint(0, 10, 1);
		this.tuskR2.rotateAngleX = (float) (-Math.PI/6);
		this.tuskR1.addChild(this.tuskR2);

		this.tuskL1 = new ModelRenderer(this, 140, 109);
		this.tuskL1.addBox(-1, 0, -1, 2, 10, 2, scale);
		this.tuskL1.setRotationPoint(3, 4, -6);
		this.tuskL1.rotateAngleX = (float) (-Math.PI/3);
		this.tuskL1.rotateAngleY = (float) (-Math.PI/15);
		this.tuskL1.rotateAngleZ = (float) (-Math.PI/12);
		this.head.addChild(this.tuskL1);
		this.tuskL2 = new ModelRenderer(this, 140, 121);
		this.tuskL2.addBox(-1, 0, -2, 2, 5, 2, scale);
		this.tuskL2.setRotationPoint(0, 10, 1);
		this.tuskL2.rotateAngleX = (float) (-Math.PI/6);
		this.tuskL1.addChild(this.tuskL2);
		
		this.tuskMR = new ModelRenderer(this, 140, 0);
		this.tuskMR.addBox(-1, 0, -1, 2, 6, 2, scale);
		this.tuskMR.setRotationPoint(-3, 4, -6);
		this.tuskMR.rotateAngleX = (float) (-Math.PI/3);
		this.tuskMR.rotateAngleY = (float) (Math.PI/15);
		this.tuskMR.rotateAngleZ = (float) (Math.PI/12);
		this.head.addChild(this.tuskMR);

		this.tuskML = new ModelRenderer(this, 140, 109);
		this.tuskML.addBox(-1, 0, -1, 2, 6, 2, scale);
		this.tuskML.setRotationPoint(3, 4, -6);
		this.tuskML.rotateAngleX = (float) (-Math.PI/3);
		this.tuskML.rotateAngleY = (float) (-Math.PI/15);
		this.tuskML.rotateAngleZ = (float) (-Math.PI/12);
		this.head.addChild(this.tuskML);
		
		this.legBR1 = new ModelRenderer(this, 123, 20);
		this.legBR1.addBox(-2, 0, -2.5F, 5, 13, 5, scale);
		this.legBR1.setRotationPoint(-8, 11, -16);
		this.legBR2 = new ModelRenderer(this, 0, 0);
		this.legBR2.addBox(-1, 0, -3.5F, 7, 14, 7, scale);
		this.legBR2.setRotationPoint(-2, 13, 0);
		this.legBR1.addChild(this.legBR2);
		this.legBL1 = new ModelRenderer(this, 61, 68);
		this.legBL1.addBox(-3, 0, -2.5F, 5, 13, 5, scale);
		this.legBL1.setRotationPoint(8, 11, -16);
		this.legBL2 = new ModelRenderer(this, 68, 0);
		this.legBL2.addBox(-6, 0, -3.5F, 7, 14, 7, scale);
		this.legBL2.setRotationPoint(2, 13, 0);
		this.legBL1.addChild(this.legBL2);
		this.legFR1 = new ModelRenderer(this, 0, 48);
		this.legFR1.addBox(-2, 0, -2.5F, 5, 10, 5, scale);
		this.legFR1.setRotationPoint(-10, -10, -7);
		this.legFR2 = new ModelRenderer(this, 120, 80);
		this.legFR2.addBox(-1, 0, -3.5F, 7, 11, 7, scale);
		this.legFR2.setRotationPoint(-2, 10, 0);
		this.legFR1.addChild(this.legFR2);
		this.legFL1 = new ModelRenderer(this, 44, 48);
		this.legFL1.addBox(-3, 0, -2.5F, 5, 10, 5, scale);
		this.legFL1.setRotationPoint(10, -10, -7);
		this.legFL2 = new ModelRenderer(this, 0, 102);
		this.legFL2.addBox(-6, 0, -3.5F, 7, 11, 7, scale);
		this.legFL2.setRotationPoint(2, 10, 0);
		this.legFL1.addChild(this.legFL2);

		this.targetBR = new ModelRenderer(this, 28, 0);
		this.targetBR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetBR.setRotationPoint(3, 14, 0);
		this.targetBL = new ModelRenderer(this, 28, 0);
		this.targetBL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetBL.setRotationPoint(-3, 14, 0);
		this.targetFR = new ModelRenderer(this, 28, 0);
		this.targetFR.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetFR.setRotationPoint(2, 11, 0);
		this.targetFL = new ModelRenderer(this, 28, 0);
		this.targetFL.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.targetFL.setRotationPoint(-2, 11, 0);

		this.ikBR = new SimpleIK(Axis.X, false, this.body, this.hip, this.legBR1, this.legBR2, this.targetBR);
		this.ikBL = new SimpleIK(Axis.X, true, this.body, this.hip, this.legBL1, this.legBL2, this.targetBL);
		this.ikFR = new SimpleIK(Axis.X, false, this.body, this.legFR1, this.legFR2, this.targetFR);
		this.ikFL = new SimpleIK(Axis.X, true, this.body, this.legFL1, this.legFL2, this.targetFL);

		this.targetBR.rotationPointX = -11;
		this.targetBL.rotationPointX = 11;
		this.targetFR.rotationPointX = -12;
		this.targetFL.rotationPointX = 12;
	}

	public ModelBrachiodon() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
		this.head.isHidden = this.isChild;
		this.childHead.isHidden = !this.isChild;
		boolean female = ((EntityGender)entity).isFemale();
		this.tuskL1.isHidden = !female;
		this.tuskR1.isHidden = !female;
		this.tuskML.isHidden = female;
		this.tuskMR.isHidden = female;
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

	static final float speed = 0.6F;
	static final float bRadius = 13;
	static final float pi = (float) Math.PI;
	static final float shift = pi / 4;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
		this.head.rotateAngleX = headPitch * 0.01F - pi / 2 - this.swingProgress * 0.8F;
		this.head.rotateAngleY = -headYaw * 0.001F;
		this.head.rotateAngleZ = -headYaw * 0.01F;

		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.childHead.rotateAngleZ = this.head.rotateAngleZ;

		float bodyRot = MathHelper.cos(limbSwing) * 0.2F * limbSwingAmount;
		this.body.rotateAngleY = bodyRot;
		this.body.rotateAngleX = pi/9 + pi/2 - this.swingProgress * 0.4F;
		this.body.rotationPointY = -5 - this.swingProgress * 5;
		bodyRot = MathHelper.cos(limbSwing - shift) * 0.1F * limbSwingAmount;
		this.hip.rotateAngleZ = -bodyRot;
		this.hip.rotateAngleY = bodyRot / 5;
		this.hip.rotateAngleX = -pi/7.2F + this.swingProgress * 0.4F;
		this.tail.rotateAngleZ = -bodyRot;
		this.tail.rotateAngleY = bodyRot / 5;

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

		this.targetFR.rotationPointY = yL + 24;
		this.targetFR.rotationPointZ = zL - 12;

		this.targetFL.rotationPointY = yR + 24;
		this.targetFL.rotationPointZ = zR - 12;

		// Shift
		limbSwing -= shift;
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

		yR *= limbSwingAmount;
		zR *= limbSwingAmount;

		yL *= limbSwingAmount;
		zL *= limbSwingAmount;

		this.targetBR.rotationPointY = yR + 24;
		this.targetBR.rotationPointZ = zR + 14;

		this.targetBL.rotationPointY = yL + 24;
		this.targetBL.rotationPointZ = zL + 14;

		this.ikBR.rotateBones(-pi / 6);
		this.ikBL.rotateBones(-pi / 6);
		this.ikFR.rotateBones(pi / 9);
		this.ikFL.rotateBones(pi / 9);
	}
}