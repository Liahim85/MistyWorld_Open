package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityForestRunner;

@SideOnly(Side.CLIENT)
public class ModelForestRunner extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer childHead;
	public ModelRenderer neck;
	public ModelRenderer body;
	public ModelRenderer leg1;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg4;

	public ModelRenderer horn1;
	public ModelRenderer horn11;
	public ModelRenderer horn12;
	public ModelRenderer horn2;
	public ModelRenderer horn21;
	public ModelRenderer horn22;

	protected float childYOffset = 8.0F;
	protected float childZOffset = 4.0F;

	public ModelForestRunner(float scale) {
		this.textureHeight = 48;
		this.body = new ModelRenderer(this, 26, 0);
		this.body.addBox(-4, -8, 0, 8, 16, 10, scale);
		this.body.setRotationPoint(0, 13, 0);
		this.body.rotateAngleX = (float) Math.PI / 2F;
		this.neck = new ModelRenderer(this, 0, 34);
		this.neck.addBox(-3, -1, -6, 6, 6, 8, scale);
		this.neck.setRotationPoint(0, 5, -8);
		this.head = new ModelRenderer(this, 0, 0);
		this.head.addBox(-2, -1, -6, 4, 4, 8, scale);
		this.head.setRotationPoint(0, -1, -6);
		this.neck.addChild(this.head);
		this.childHead = new ModelRenderer(this, 0, 0);
		this.childHead.addBox(-2, -0.5F, -6, 4, 4, 8, 0.5F + scale);
		this.childHead.setRotationPoint(0, -1, -6);
		this.neck.addChild(this.childHead);

		this.horn1 = new ModelRenderer(this, 0, 0);
		this.horn1.addBox(-0.5F, -3, -0.5F, 1, 3, 1, scale);
		this.horn1.setRotationPoint(1.655F, -0.43f, -4.45F);
		this.horn1.rotateAngleX = (float) Math.PI / 3F;
		this.horn1.rotateAngleY = (float) -Math.PI / 2.25F;
		this.head.addChild(this.horn1);
		this.horn11 = new ModelRenderer(this, 0, 0);
		this.horn11.addBox(-0.5F, -3, -0.5F, 1, 3, 1, scale);
		this.horn11.setRotationPoint(1.305F, -0.28F, -5.54F);
		this.horn11.rotateAngleX = (float) Math.PI / 3F;
		this.horn11.rotateAngleY = (float) -Math.PI / 6F;
		this.head.addChild(this.horn11);
		this.horn12 = new ModelRenderer(this, 4, 0);
		this.horn12.addBox(-0.5F, -3, 0, 1, 3, 1, scale);
		this.horn12.setRotationPoint(0, -3F, -0.5F);
		this.horn12.rotateAngleX = (float) -Math.PI / 6F;
		this.horn11.addChild(this.horn12);

		this.horn2 = new ModelRenderer(this, 0, 0);
		this.horn2.addBox(-0.5F, -3, -0.5F, 1, 3, 1, scale);
		this.horn2.setRotationPoint(-1.655F, -0.43f, -4.45F);
		this.horn2.rotateAngleX = (float) Math.PI / 3F;
		this.horn2.rotateAngleY = (float) Math.PI / 2.25F;
		this.horn2.mirror = true;
		this.head.addChild(this.horn2);
		this.horn21 = new ModelRenderer(this, 0, 0);
		this.horn21.addBox(-0.5F, -3, -0.5F, 1, 3, 1, scale);
		this.horn21.setRotationPoint(-1.305F, -0.28F, -5.54F);
		this.horn21.rotateAngleX = (float) Math.PI / 3F;
		this.horn21.rotateAngleY = (float) Math.PI / 6F;
		this.horn21.mirror = true;
		this.head.addChild(this.horn21);
		this.horn22 = new ModelRenderer(this, 4, 0);
		this.horn22.addBox(-0.5F, -3, 0, 1, 3, 1, scale);
		this.horn22.setRotationPoint(0, -3F, -0.5F);
		this.horn22.rotateAngleX = (float) -Math.PI / 6F;
		this.horn22.mirror = true;
		this.horn21.addChild(this.horn22);

		this.leg1 = new ModelRenderer(this, 0, 12);
		this.leg1.addBox(-1, 0, -1, 3, 11, 3, scale);
		this.leg1.setRotationPoint(-2.95F, 13, 6);
		this.leg2 = new ModelRenderer(this, 12, 12);
		this.leg2.addBox(-2, 0, -1, 3, 11, 3, scale);
		this.leg2.setRotationPoint(2.95F, 13, 6);
		this.leg3 = new ModelRenderer(this, 0, 12);
		this.leg3.addBox(-1, 0, -2, 3, 11, 3, scale);
		this.leg3.setRotationPoint(-2.95F, 13, -5);
		this.leg4 = new ModelRenderer(this, 12, 12);
		this.leg4.addBox(-2, 0, -2, 3, 11, 3, scale);
		this.leg4.setRotationPoint(2.95F, 13, -5);
	}

	public ModelForestRunner() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		if (((EntityForestRunner)entity).isFemale() || this.isChild) {
			this.horn1.isHidden = true;
			this.horn11.isHidden = true;
			this.horn12.isHidden = true;
			this.horn2.isHidden = true;
			this.horn21.isHidden = true;
			this.horn22.isHidden = true;
		} else {
			this.horn1.isHidden = false;
			this.horn11.isHidden = false;
			this.horn12.isHidden = false;
			this.horn2.isHidden = false;
			this.horn21.isHidden = false;
			this.horn22.isHidden = false;
		}
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.head.isHidden = true;
			this.childHead.isHidden = false;
			this.neck.render(scale);
			this.body.render(scale);
			this.leg1.render(scale);
			this.leg2.render(scale);
			this.leg3.render(scale);
			this.leg4.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.head.isHidden = false;
			this.childHead.isHidden = true;
			this.neck.render(scale);
			this.body.render(scale);
			this.leg1.render(scale);
			this.leg2.render(scale);
			this.leg3.render(scale);
			this.leg4.render(scale);
		}
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		this.head.rotateAngleX = headPitch * 0.0085F;
		this.head.rotateAngleY = netHeadYaw * 0.0085F;
		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.neck.rotateAngleX = headPitch * 0.0085F;
		this.neck.rotateAngleY = netHeadYaw * 0.0085F;
		this.leg1.rotateAngleX = MathHelper.cos(limbSwing * 0.6F) * 1.4F * limbSwingAmount;
		this.leg2.rotateAngleX = MathHelper.cos(limbSwing * 0.6F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.leg3.rotateAngleX = MathHelper.cos(limbSwing * 0.6F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.leg4.rotateAngleX = MathHelper.cos(limbSwing * 0.6F) * 1.4F * limbSwingAmount;
	}
}