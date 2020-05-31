package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelGraveBug extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer body;
	public ModelRenderer childBody;
	public ModelRenderer back;
	public ModelRenderer head;
	public ModelRenderer legL1;
	public ModelRenderer legL2;
	public ModelRenderer legL3;
	public ModelRenderer legR1;
	public ModelRenderer legR2;
	public ModelRenderer legR3;

	public ModelGraveBug(float scale) {
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 0, 11);
		this.body.addBox(-2, -3, -3, 4, 3, 6, scale);
		this.body.setRotationPoint(0, 22, 0);
		this.back = new ModelRenderer(this, 0, 0);
		this.back.addBox(-2.5F, -3, 0, 5, 3, 8, scale);
		this.back.rotateAngleX = pi / 12;
		this.back.setRotationPoint(0, -0.5F, 0);
		this.body.addChild(this.back);
		this.head = new ModelRenderer(this, 0, 0);
		this.head.addBox(-1, -1, -2, 2, 1, 2, scale);
		this.head.rotateAngleX = pi / 6;
		this.head.setRotationPoint(0, -1, -3);
		this.body.addChild(this.head);

		this.legL1 = new ModelRenderer(this, 18, 0);
		this.legL1.addBox(0, -0.5F, -0.5F, 5, 1, 1, scale);
		this.legL1.rotateAngleY = pi / 5;
		this.legL1.rotateAngleZ = pi / 9;
		this.legL1.setRotationPoint(1, 0, -1.5F);
		this.body.addChild(this.legL1);
		this.legL2 = new ModelRenderer(this, 18, 0);
		this.legL2.addBox(0, -0.5F, -0.5F, 5, 1, 1, scale);
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 9;
		this.legL2.setRotationPoint(1, 0, 0.5F);
		this.body.addChild(this.legL2);
		this.legL3 = new ModelRenderer(this, 18, 0);
		this.legL3.addBox(0, -0.5F, -0.5F, 5, 1, 1, scale);
		this.legL3.rotateAngleY = -pi / 4;
		this.legL3.rotateAngleZ = pi / 9;
		this.legL3.setRotationPoint(1, 0, 2.5F);
		this.body.addChild(this.legL3);

		this.legR1 = new ModelRenderer(this, 18, 2);
		this.legR1.addBox(-5, -0.5F, -0.5F, 5, 1, 1, scale);
		this.legR1.rotateAngleY = -pi / 5;
		this.legR1.rotateAngleZ = -pi / 9;
		this.legR1.setRotationPoint(-1, 0, -1.5F);
		this.body.addChild(this.legR1);
		this.legR2 = new ModelRenderer(this, 18, 2);
		this.legR2.addBox(-5, -0.5F, -0.5F, 5, 1, 1, scale);
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 9;
		this.legR2.setRotationPoint(-1, 0, 0.5F);
		this.body.addChild(this.legR2);
		this.legR3 = new ModelRenderer(this, 18, 2);
		this.legR3.addBox(-5, -0.5F, -0.5F, 5, 1, 1, scale);
		this.legR3.rotateAngleY = pi / 4;
		this.legR3.rotateAngleZ = -pi / 9;
		this.legR3.setRotationPoint(-1, 0, 2.5F);
		this.body.addChild(this.legR3);
		
		this.childBody = new ModelRenderer(this, 0, 20);
		this.childBody.addBox(-1.5F, -2, -2, 3, 2, 4, scale);
		this.childBody.setTextureOffset(0, 26);
		this.childBody.addBox(-1, -1, -3, 2, 1, 1, scale);
		this.childBody.setRotationPoint(0, 24, 0);
	}

	public ModelGraveBug() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		if (this.isChild) this.childBody.render(scale);
		else this.body.render(scale);
	}

	static final float speed = 2;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		this.back.rotateAngleX = pi * limbSwingAmount / 6;
		this.head.rotateAngleY = headYaw * 0.01F;
		this.head.rotateAngleX = headPitch * 0.015F + pi / 6;
		this.legL1.rotateAngleY = pi / 5;
		this.legL1.rotateAngleZ = pi / 9;
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 9;
		this.legL3.rotateAngleY = -pi / 4;
		this.legL3.rotateAngleZ = pi / 9;
		this.legR1.rotateAngleY = -pi / 5;
		this.legR1.rotateAngleZ = -pi / 9;
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 9;
		this.legR3.rotateAngleY = pi / 4;
		this.legR3.rotateAngleZ = -pi / 9;
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