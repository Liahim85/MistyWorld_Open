package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityMossling;

@SideOnly(Side.CLIENT)
public class ModelMossling extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer neck;
	public ModelRenderer childHead;
	public ModelRenderer childNeck;
	public ModelRenderer body;
	public ModelRenderer bodyS;
	public ModelRenderer leg1;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg4;
	protected float childYOffset = 18.4F;
	protected float childZOffset = 1.0F;

	private float xRotFactor;
	private float xEatFactor;
	private float yEatFactor;

	public ModelMossling(float scale) {
		this.textureHeight = 48;
		this.body = new ModelRenderer(this, 28, 2);
		this.body.addBox(-5, -8, 0, 10, 18, 8, scale);
		this.body.setTextureOffset(42, 28);
		this.body.addBox(-5, -8, -2, 10, 18, 2, scale);
		this.body.setRotationPoint(0, 17, 0);
		this.body.rotateAngleX = ((float) Math.PI / 2F);

		this.bodyS = new ModelRenderer(this, 28, 2);
		this.bodyS.addBox(-5, -7, 0, 10, 16, 8, scale);
		this.bodyS.setRotationPoint(0, 17, 0);
		this.bodyS.rotateAngleX = ((float) Math.PI / 2F);

		this.neck = new ModelRenderer(this, 0, 18);
		this.neck.addBox(-3, -2, -6, 6, 6, 8, scale);
		this.neck.setRotationPoint(0, 13, -8);
		this.head = new ModelRenderer(this, 0, 7);
		this.head.addBox(-2, -2, -6, 4, 4, 7, scale);
		this.head.setTextureOffset(0, 0);
		this.head.addBox(-2, 2, -6, 4, 2, 4, scale);
		this.head.setRotationPoint(0, 2, -6);
		this.neck.addChild(this.head);

		this.childNeck = new ModelRenderer(this, 2, 20);
		this.childNeck.addBox(-3, -2, -4, 6, 5, 6, scale);
		this.childNeck.setRotationPoint(0, 13, -8);
		this.childHead = new ModelRenderer(this, 0, 7);
		this.childHead.addBox(-2, -3, -6, 4, 4, 7, scale);
		this.childHead.setTextureOffset(0, 0);
		this.childHead.addBox(-2, 1, -4, 4, 2, 4, scale);
		this.childHead.setRotationPoint(0, 2, -4);
		this.childNeck.addChild(this.childHead);

		this.leg1 = new ModelRenderer(this, 0, 37);
		this.leg1.addBox(-2, 0, -2, 4, 7, 4, scale);
		this.leg1.setRotationPoint(-2.95F, 17, 7);
		this.leg2 = new ModelRenderer(this, 16, 37);
		this.leg2.addBox(-2, 0, -2, 4, 7, 4, scale);
		this.leg2.setRotationPoint(2.95F, 17, 7);
		this.leg3 = new ModelRenderer(this, 0, 37);
		this.leg3.addBox(-2, 0, -2, 4, 7, 4, scale);
		this.leg3.setRotationPoint(-2.95F, 17, -5);
		this.leg4 = new ModelRenderer(this, 16, 37);
		this.leg4.addBox(-2, 0, -2, 4, 7, 4, scale);
		this.leg4.setRotationPoint(2.95F, 17, -5);
	}

	public ModelMossling() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.6F, 0.6F, 0.6F);
			GlStateManager.translate(0.0F, this.childYOffset * scale, this.childZOffset * scale);
			this.childNeck.render(scale);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.body.render(scale);
			this.leg1.render(scale);
			this.leg2.render(scale);
			this.leg3.render(scale);
			this.leg4.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.neck.render(scale);
			if (((EntityMossling)entity).isSheared()) this.bodyS.render(scale);
			else this.body.render(scale);
			this.leg1.render(scale);
			this.leg2.render(scale);
			this.leg3.render(scale);
			this.leg4.render(scale);
		}
	}

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTickTime) {
		super.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTickTime);
		this.xRotFactor = ((EntityMossling)entity).getXRotFactor(partialTickTime);
		this.xEatFactor = ((EntityMossling)entity).getXEatFactor(partialTickTime);
		this.yEatFactor = ((EntityMossling)entity).getYEatFactor(partialTickTime);
	}

	static final float speed = 0.8F;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		if (this.isChild) {
			this.childHead.rotateAngleX = headPitch * 0.0085F - 0.3F * yEatFactor + 0.15F * xEatFactor;
			this.childHead.rotateAngleY = netHeadYaw * 0.0085F * yEatFactor;
			this.childNeck.rotateAngleX = headPitch * 0.0085F + 0.3F * xRotFactor + 0.15F * (1 - yEatFactor);
			this.childNeck.rotateAngleY = netHeadYaw * 0.0085F * yEatFactor;
		} else {
			this.head.rotateAngleX = headPitch * 0.0085F - 0.2F * yEatFactor + 0.1F * xEatFactor;
			this.head.rotateAngleY = netHeadYaw * 0.0085F * yEatFactor;
			this.neck.rotateAngleX = headPitch * 0.0085F + 0.2F * xRotFactor + 0.1F * (1 - yEatFactor);
			this.neck.rotateAngleY = netHeadYaw * 0.0085F * yEatFactor;
		}
		
		this.leg1.rotateAngleX = MathHelper.cos(limbSwing) * 1.4F * limbSwingAmount;
		this.leg2.rotateAngleX = MathHelper.cos(limbSwing + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.leg3.rotateAngleX = MathHelper.cos(limbSwing + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.leg4.rotateAngleX = MathHelper.cos(limbSwing) * 1.4F * limbSwingAmount;
	}
}