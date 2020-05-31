package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelCyclops extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer body;
	public ModelRenderer tail_1;
	public ModelRenderer tail_2;
	public ModelRenderer eye;
	public ModelRenderer clawL;
	public ModelRenderer clawR;
	public ModelRenderer legL1;
	public ModelRenderer legL2;
	public ModelRenderer legL3;
	public ModelRenderer legR1;
	public ModelRenderer legR2;
	public ModelRenderer legR3;

	public ModelCyclops(float scale) {
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-3, -4, -6, 6, 4, 8, scale);
		this.body.setRotationPoint(0, 22, 1);
		this.eye = new ModelRenderer(this, 0, 3);
		this.eye.addBox(-1, -1, -1, 2, 2, 2, scale);
		this.eye.setRotationPoint(0, -2, -6);
		this.body.addChild(this.eye);
		this.tail_1 = new ModelRenderer(this, 0, 12);
		this.tail_1.addBox(-2, -2, -1, 4, 3, 4, scale);
		this.tail_1.setRotationPoint(0, -1, 2);
		this.body.addChild(this.tail_1);
		this.tail_2 = new ModelRenderer(this, 16, 12);
		this.tail_2.addBox(-1, -1, -1, 2, 2, 5, scale);
		this.tail_2.setTextureOffset(20, 3);
		this.tail_2.addBox(1, 0, 3, 1, 1, 3, scale);
		this.tail_2.setTextureOffset(20, 3);
		this.tail_2.addBox(-2, 0, 3, 1, 1, 3, scale);
		this.tail_2.setRotationPoint(0, 0, 3);
		this.tail_1.addChild(this.tail_2);

		this.clawL = new ModelRenderer(this, 10, 19);
		this.clawL.addBox(0, -1, -4, 1, 2, 4, scale);
		this.clawL.rotateAngleX = pi / 12;
		this.clawL.rotateAngleY = pi / 4;
		this.clawL.rotateAngleZ = -pi / 12;
		this.clawL.setRotationPoint(3, -1, -6);
		this.body.addChild(this.clawL);
		this.clawR = new ModelRenderer(this, 0, 19);
		this.clawR.addBox(-1, -1, -4, 1, 2, 4, scale);
		this.clawR.rotateAngleX = pi / 12;
		this.clawR.rotateAngleY = -pi / 4;
		this.clawR.rotateAngleZ = pi / 12;
		this.clawR.setRotationPoint(-3, -1, -6);
		this.body.addChild(this.clawR);

		this.legL1 = new ModelRenderer(this, 20, 0);
		this.legL1.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL1.setTextureOffset(27, 0);
		this.legL1.addBox(2, 0.5F, -0.5F, 1, 1, 1, scale);
		this.legL1.rotateAngleY = pi / 12;
		this.legL1.rotateAngleZ = pi / 9;
		this.legL1.setRotationPoint(3, -1, -4);
		this.body.addChild(this.legL1);
		this.legL2 = new ModelRenderer(this, 20, 0);
		this.legL2.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL2.setTextureOffset(27, 0);
		this.legL2.addBox(2, 0.5F, -0.5F, 1, 1, 1, scale);
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 9;
		this.legL2.setRotationPoint(3, -1, -1.5F);
		this.body.addChild(this.legL2);
		this.legL3 = new ModelRenderer(this, 20, 0);
		this.legL3.addBox(0, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legL3.setTextureOffset(27, 0);
		this.legL3.addBox(2, 0.5F, -0.5F, 1, 1, 1, scale);
		this.legL3.rotateAngleY = -pi / 4;
		this.legL3.rotateAngleZ = pi / 9;
		this.legL3.setRotationPoint(3, -1, 1);
		this.body.addChild(this.legL3);

		this.legR1 = new ModelRenderer(this, 0, 0);
		this.legR1.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR1.setTextureOffset(27, 0);
		this.legR1.addBox(-3, 0.5F, -0.5F, 1, 1, 1, scale);
		this.legR1.rotateAngleY = -pi / 12;
		this.legR1.rotateAngleZ = -pi / 9;
		this.legR1.setRotationPoint(-3, -1, -4);
		this.body.addChild(this.legR1);
		this.legR2 = new ModelRenderer(this, 0, 0);
		this.legR2.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR2.setTextureOffset(27, 0);
		this.legR2.addBox(-3, 0.5F, -0.5F, 1, 1, 1, scale);
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 9;
		this.legR2.setRotationPoint(-3, -1, -1.5F);
		this.body.addChild(this.legR2);
		this.legR3 = new ModelRenderer(this, 0, 0);
		this.legR3.addBox(-3, -0.5F, -0.5F, 3, 1, 1, scale);
		this.legR3.setTextureOffset(27, 0);
		this.legR3.addBox(-3, 0.5F, -0.5F, 1, 1, 1, scale);
		this.legR3.rotateAngleY = pi / 4;
		this.legR3.rotateAngleZ = -pi / 9;
		this.legR3.setRotationPoint(-3, -1, 1);
		this.body.addChild(this.legR3);
	}

	public ModelCyclops() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		this.body.render(scale);
	}

	static final float speed = 2;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		this.eye.rotateAngleY = headYaw * 0.01F;
		this.eye.rotateAngleX = headPitch * 0.01F;

		this.clawL.rotateAngleX = pi / 12;
		this.clawL.rotateAngleY = pi / 4;
		this.clawL.rotateAngleZ = -pi / 12;
		this.clawR.rotateAngleX = pi / 12;
		this.clawR.rotateAngleY = -pi / 4;
		this.clawR.rotateAngleZ = pi / 12;

		this.legL1.rotateAngleY = pi / 12;
		this.legL1.rotateAngleZ = pi / 9;
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 9;
		this.legL3.rotateAngleY = -pi / 4;
		this.legL3.rotateAngleZ = pi / 9;
		this.legR1.rotateAngleY = -pi / 12;
		this.legR1.rotateAngleZ = -pi / 9;
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 9;
		this.legR3.rotateAngleY = pi / 4;
		this.legR3.rotateAngleZ = -pi / 9;

		this.clawL.rotateAngleX -= limbSwingAmount * pi / 2.5F;
		this.clawL.rotateAngleY -= limbSwingAmount * pi / 3;
		this.clawR.rotateAngleX -= limbSwingAmount * pi / 2.5F;
		this.clawR.rotateAngleY += limbSwingAmount * pi / 3;

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

		this.tail_1.rotateAngleX = -pi/25;
		this.tail_1.rotateAngleY = -y1/5;
		this.tail_2.rotateAngleX = -pi/25;
		this.tail_2.rotateAngleY = -y1/5;
	}
}