package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelWoodlouse extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer neck;
	public ModelRenderer head;
	public ModelRenderer tail;
	public ModelRenderer legL1;
	public ModelRenderer legL2;
	public ModelRenderer legL3;
	public ModelRenderer legL4;
	public ModelRenderer legR1;
	public ModelRenderer legR2;
	public ModelRenderer legR3;
	public ModelRenderer legR4;

	public ModelWoodlouse(float scale) {
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 1, 1);
		this.body.addBox(-3.5F, -7, -3.5F, 7, 7, 7, scale);
		this.body.setRotationPoint(0, 22, 0);
		this.back = new ModelRenderer(this, 0, 15);
		this.back.addBox(-3, -5, -3, 6, 5, 3, scale);
		this.back.setRotationPoint(0, 0, 3.5F);
		this.back.rotateAngleX = -pi / 6;
		this.body.addChild(this.back);
		this.neck = new ModelRenderer(this, 0, 23);
		this.neck.addBox(-3, -3, -1, 6, 5, 3, scale);
		this.neck.setRotationPoint(0, -2, -3.5F);
		this.neck.rotateAngleX = pi / 6;
		this.body.addChild(this.neck);
		this.head = new ModelRenderer(this, 18, 21);
		this.head.addBox(-1.5F, -2, -0.5F, 3, 3, 3, scale);
		this.head.setTextureOffset(22, 4);
		this.head.addBox(-2.5F, -3, -1.5F, 2, 2, 2, scale);
		this.head.setTextureOffset(22, 0);
		this.head.addBox(0.5F, -3, -1.5F, 2, 2, 2, scale);
		this.head.setTextureOffset(18, 27);
		this.head.addBox(-1, -2, -2.5F, 2, 1, 2, scale);
		this.head.setRotationPoint(0, 1, -1);
		this.head.rotateAngleX = pi / 6;
		this.neck.addChild(this.head);
		this.tail = new ModelRenderer(this, 18, 15);
		this.tail.addBox(-2, -3, -3, 4, 3, 3, scale);
		this.tail.setRotationPoint(0, 0, 0);
		this.tail.rotateAngleX = -pi / 6;
		this.back.addChild(this.tail);

		this.legL1 = new ModelRenderer(this, 0, 2);
		this.legL1.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL1.rotateAngleY = pi / 4;
		this.legL1.rotateAngleZ = pi / 6;
		this.legL1.setRotationPoint(2.5F, 0, -3);
		this.body.addChild(this.legL1);
		this.legL2 = new ModelRenderer(this, 0, 2);
		this.legL2.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL2.rotateAngleY = pi / 12;
		this.legL2.rotateAngleZ = pi / 6;
		this.legL2.setRotationPoint(3, 0, -1.5F);
		this.body.addChild(this.legL2);
		this.legL3 = new ModelRenderer(this, 0, 2);
		this.legL3.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL3.rotateAngleY = -pi / 12;
		this.legL3.rotateAngleZ = pi / 6;
		this.legL3.setRotationPoint(3, 0, 1.5F);
		this.body.addChild(this.legL3);
		this.legL4 = new ModelRenderer(this, 0, 2);
		this.legL4.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL4.rotateAngleY = -pi / 4;
		this.legL4.rotateAngleZ = pi / 6;
		this.legL4.setRotationPoint(2.5F, 0, 3);
		this.body.addChild(this.legL4);

		this.legR1 = new ModelRenderer(this, 0, 0);
		this.legR1.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR1.rotateAngleY = -pi / 4;
		this.legR1.rotateAngleZ = -pi / 6;
		this.legR1.setRotationPoint(-2.5F, 0, -3);
		this.body.addChild(this.legR1);
		this.legR2 = new ModelRenderer(this, 0, 0);
		this.legR2.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR2.rotateAngleY = -pi / 12;
		this.legR2.rotateAngleZ = -pi / 6;
		this.legR2.setRotationPoint(-3, 0, -1.5F);
		this.body.addChild(this.legR2);
		this.legR3 = new ModelRenderer(this, 0, 0);
		this.legR3.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR3.rotateAngleY = pi / 12;
		this.legR3.rotateAngleZ = -pi / 6;
		this.legR3.setRotationPoint(-3, 0, 1.5F);
		this.body.addChild(this.legR3);
		this.legR4 = new ModelRenderer(this, 0, 0);
		this.legR4.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR4.rotateAngleY = pi / 4;
		this.legR4.rotateAngleZ = -pi / 6;
		this.legR4.setRotationPoint(-2.5F, 0, 3);
		this.body.addChild(this.legR4);
	}

	public ModelWoodlouse() {
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

	static final float speed = 2;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		this.head.rotateAngleY = headYaw * 0.007F;
		this.neck.rotateAngleY = headYaw * 0.003F;
		this.head.rotateAngleX = headPitch * 0.007F + pi / 6;
		this.neck.rotateAngleX = headPitch * 0.003F + pi / 6;

		this.legL1.rotateAngleY = pi / 4;
		this.legL1.rotateAngleZ = pi / 6;
		this.legL2.rotateAngleY = pi / 12;
		this.legL2.rotateAngleZ = pi / 6;
		this.legL3.rotateAngleY = -pi / 12;
		this.legL3.rotateAngleZ = pi / 6;
		this.legL4.rotateAngleY = -pi / 4;
		this.legL4.rotateAngleZ = pi / 6;
		this.legR1.rotateAngleY = -pi / 4;
		this.legR1.rotateAngleZ = -pi / 6;
		this.legR2.rotateAngleY = -pi / 12;
		this.legR2.rotateAngleZ = -pi / 6;
		this.legR3.rotateAngleY = pi / 12;
		this.legR3.rotateAngleZ = -pi / 6;
		this.legR4.rotateAngleY = pi / 4;
		this.legR4.rotateAngleZ = -pi / 6;


		float y1 = MathHelper.cos(limbSwing) * 0.8F * limbSwingAmount;
		float z1 = MathHelper.sin(limbSwing) * 0.8F * limbSwingAmount;
		float y2 = MathHelper.cos(limbSwing + pi) * 0.5F * limbSwingAmount;
		float z2 = MathHelper.sin(limbSwing + pi) * 0.5F * limbSwingAmount;
		float y3 = MathHelper.cos(limbSwing - pi/2) * 0.5F * limbSwingAmount;
		float z3 = MathHelper.sin(limbSwing - pi/2) * 0.5F * limbSwingAmount;
		float y4 = MathHelper.cos(limbSwing + pi/2) * 0.8F * limbSwingAmount;
		float z4 = MathHelper.sin(limbSwing + pi/2) * 0.8F * limbSwingAmount;
		this.legL1.rotateAngleY += y1;
		this.legL1.rotateAngleZ += Math.min(z1, 0);
		this.legR1.rotateAngleY += y1;
		this.legR1.rotateAngleZ += Math.max(z1, 0);
		this.legL2.rotateAngleY += y2;
		this.legL2.rotateAngleZ += Math.min(z2, 0);
		this.legR2.rotateAngleY += y2;
		this.legR2.rotateAngleZ += Math.max(z2, 0);
		this.legL3.rotateAngleY += y3;
		this.legL3.rotateAngleZ += Math.min(z3, 0);
		this.legR3.rotateAngleY += y3;
		this.legR3.rotateAngleZ += Math.max(z3, 0);
		this.legL4.rotateAngleY += y4;
		this.legL4.rotateAngleZ += Math.min(z4, 0);
		this.legR4.rotateAngleY += y4;
		this.legR4.rotateAngleZ += Math.max(z4, 0);
	}
}