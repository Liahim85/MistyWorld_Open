package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntitySnowFlea;

@SideOnly(Side.CLIENT)
public class ModelSnowFlea extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer head;
	public ModelRenderer legL1;
	public ModelRenderer legL2;
	public ModelRenderer legL3;
	public ModelRenderer legR1;
	public ModelRenderer legR2;
	public ModelRenderer legR3;

	public ModelSnowFlea(float scale) {
		this.textureWidth = 32;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-2.5F, -6, -3, 5, 6, 6, scale);
		this.body.setRotationPoint(0, 20.5F, 0);
		this.back = new ModelRenderer(this, 0, 12);
		this.back.addBox(-1.5F, 0, -1, 3, 6, 7, scale);
		this.back.rotateAngleX = -pi / 7.2F;
		this.back.setRotationPoint(0, -5, 3);
		this.body.addChild(this.back);
		this.head = new ModelRenderer(this, 0, 25);
		this.head.addBox(-1.5F, 0, -3, 3, 4, 3, scale);
		this.head.rotateAngleX = pi / 9;
		this.head.setRotationPoint(0, -4, -3);
		this.body.addChild(this.head);

		this.legL1 = new ModelRenderer(this, 16, 0);
		this.legL1.addBox(-1, -0.5F, -0.5F, 4, 1, 1, scale);
		this.legL1.rotateAngleY = pi / 12;
		this.legL1.rotateAngleZ = pi / 3;
		this.legL1.setRotationPoint(2, 0.5F, -2.5F);
		this.body.addChild(this.legL1);
		this.legL2 = new ModelRenderer(this, 16, 0);
		this.legL2.addBox(-1, -0.5F, -0.5F, 4, 1, 1, scale);
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 3;
		this.legL2.setRotationPoint(2, 0.5F, -0.5F);
		this.body.addChild(this.legL2);
		this.legL3 = new ModelRenderer(this, 22, 0);
		this.legL3.addBox(0, -2, 0, 1, 2, 4, scale);
		this.legL3.rotateAngleY = pi / 12;
		this.legL3.rotateAngleZ = pi / 6;
		this.legL3.setRotationPoint(2, 0, 2);
		this.body.addChild(this.legL3);

		this.legR1 = new ModelRenderer(this, 16, 0);
		this.legR1.addBox(-3, -0.5F, -0.5F, 4, 1, 1, scale);
		this.legR1.rotateAngleY = -pi / 12;
		this.legR1.rotateAngleZ = -pi / 3;
		this.legR1.setRotationPoint(-2, 0.5F, -2.5F);
		this.body.addChild(this.legR1);
		this.legR2 = new ModelRenderer(this, 16, 0);
		this.legR2.addBox(-3, -0.5F, -0.5F, 4, 1, 1, scale);
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 3;
		this.legR2.setRotationPoint(-2, 0.5F, -0.5F);
		this.body.addChild(this.legR2);
		this.legR3 = new ModelRenderer(this, 22, 6);
		this.legR3.addBox(-1, -2, 0, 1, 2, 4, scale);
		this.legR3.rotateAngleY = -pi / 12;
		this.legR3.rotateAngleZ = -pi / 6;
		this.legR3.setRotationPoint(-2, 0, 2);
		this.body.addChild(this.legR3);
	}

	public ModelSnowFlea() {
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
	private float jump = 1;

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
		this.jump = ((EntitySnowFlea)entity).onGround ? 1 : 0;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		this.back.rotateAngleX = pi * (limbSwingAmount - 1) / 7.2F;
        this.head.rotateAngleY = headYaw * 0.01F;
        this.head.rotateAngleX = headPitch * 0.015F + pi / 9;
		this.legL1.rotateAngleY = pi / 12;
		this.legL1.rotateAngleZ = pi / 3;
		this.legL2.rotateAngleY = -pi / 12;
		this.legL2.rotateAngleZ = pi / 3;
		this.legL3.rotateAngleX = -pi / 4.5F;
		this.legL3.rotateAngleY = pi / 12;
		this.legL3.rotateAngleZ = pi / 12;
		this.legR1.rotateAngleY = -pi / 12;
		this.legR1.rotateAngleZ = -pi / 3;
		this.legR2.rotateAngleY = pi / 12;
		this.legR2.rotateAngleZ = -pi / 3;
		this.legR3.rotateAngleX = -pi / 4.5F;
		this.legR3.rotateAngleY = -pi / 12;
		this.legR3.rotateAngleZ = -pi / 12;
		limbSwingAmount *= this.jump;
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