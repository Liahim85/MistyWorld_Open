package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityWulder;

@SideOnly(Side.CLIENT)
public class ModelWulderWool extends ModelBase {

	public ModelRenderer neck;
	public ModelRenderer neckWool;
	public ModelRenderer body;
	public ModelRenderer bodyWool;
	public ModelRenderer back;
	public ModelRenderer backWool;

	private float xEatFactor;
	private float yEatFactor;

	public ModelWulderWool(float scale) {
		this.textureWidth = 128;
		this.textureHeight = 96;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-7, -17.99F, -10, 14, 20, 20, scale);
		this.body.setRotationPoint(0, 8, 0);
		this.bodyWool = new ModelRenderer(this, 0, 40);
		this.bodyWool.addBox(-7, -9.99F, -5, 14, 20, 5, scale);
		this.bodyWool.setRotationPoint(0, 2, 0);
		this.bodyWool.rotateAngleX = (float) Math.PI/2;
		this.body.addChild(this.bodyWool);
		this.back = new ModelRenderer(this, 38, 45);
		this.back.addBox(-6, 0.01F, 0, 12, 17, 9, scale);
		this.back.setRotationPoint(0, -15, 10);
		this.body.addChild(this.back);
		this.backWool = new ModelRenderer(this, 0, 65);
		this.backWool.addBox(-6, 0.01F, 0, 12, 5, 9, scale);
		this.backWool.setRotationPoint(0, 17, 0);
		this.back.addChild(this.backWool);
		this.neck = new ModelRenderer(this, 68, 0);
		this.neck.addBox(-4, -4, -10, 8, 11, 11, scale);
		this.neck.setRotationPoint(0, -10, -9);
		this.body.addChild(this.neck);
		this.neckWool = new ModelRenderer(this, 68, 22);
		this.neckWool.addBox(-4, 0, -10, 8, 5, 11, scale);
		this.neckWool.setRotationPoint(0, 7, 0);
		this.neck.addChild(this.neckWool);
	}

	public ModelWulderWool() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 20.0F * scale, 0.0F);
			this.body.render(scale);
			GlStateManager.popMatrix();
		} else this.body.render(scale);
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
        this.xEatFactor = ((EntityWulder)entity).getXEatFactor(partialTickTime);
		this.yEatFactor = ((EntityWulder)entity).getYEatFactor(partialTickTime);
	}

	static final float speed = 0.6F;
	static final float pi = (float) Math.PI;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		float invertEatY = 1F - yEatFactor;
		limbSwing *= speed;
		float xNoise = MathHelper.cos(limbSwing) * 0.02F * limbSwingAmount;
		float yNoise = MathHelper.sin(limbSwing * 0.5F) * 0.02F * limbSwingAmount;
		this.neck.rotateAngleX = headPitch * 0.007F + 0.05F * xEatFactor + 0.5F * invertEatY + xNoise;
		this.neck.rotateAngleY = headYaw * 0.004F * yEatFactor + yNoise;
		this.neck.rotationPointY = -10 + 3 * invertEatY;
	}
}