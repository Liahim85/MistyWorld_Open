package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityCaravan;

@SideOnly(Side.CLIENT)
public class ModelCaravan extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer neck;
	public ModelRenderer childHead;
	public ModelRenderer childNeck;
	public ModelRenderer body1;
	public ModelRenderer body2;
	public ModelRenderer body3;
	public ModelRenderer hump;
	public ModelRenderer childHump;
	public ModelRenderer legFR1;
	public ModelRenderer legFR2;
	public ModelRenderer legFL1;
	public ModelRenderer legFL2;
	public ModelRenderer childLegFR1;
	public ModelRenderer childLegFR2;
	public ModelRenderer childLegFL1;
	public ModelRenderer childLegFL2;
	public ModelRenderer legBR1;
	public ModelRenderer legBR2;
	public ModelRenderer legBR3;
	public ModelRenderer legBL1;
	public ModelRenderer legBL2;
	public ModelRenderer legBL3;
	public ModelRenderer childLegBR3;
	public ModelRenderer childLegBL3;
	protected float childYOffset = 8.0F;
	protected float childZOffset = 4.0F;
	
	public ModelCaravan(float scale) {
		this.textureWidth = 100;
		this.textureHeight = 108;
		this.body1 = new ModelRenderer(this, 0, 0);
		this.body1.addBox(-6, -15, -4, 12, 15, 10, scale);
		this.body1.setRotationPoint(0, 4, 4);
		this.body2 = new ModelRenderer(this, 32, 24);
		this.body2.addBox(-7, -16, -20, 14, 17, 20, scale);
		this.body2.setRotationPoint(0, 4, 4);
		this.body3 = new ModelRenderer(this, 56, 64);
		this.body3.addBox(-5, 0, 0, 10, 10, 12, scale);
		this.body3.setRotationPoint(0, -11, 6);
		this.body1.addChild(this.body3);
		this.hump = new ModelRenderer(this, 0, 66);
		this.hump.addBox(-9, -13, -9, 18, 16, 20, scale);
		this.hump.setRotationPoint(0, -8, -8);
		this.body2.addChild(this.hump);
		this.childHump = new ModelRenderer(this, 0, 66);
		this.childHump.addBox(-8, -11, -9, 16, 14, 20, scale);
		this.childHump.setRotationPoint(0, -8, -8);
		this.body2.addChild(this.childHump);
		this.neck = new ModelRenderer(this, 48, 6);
		this.neck.addBox(-5, -6, -3, 10, 12, 6, scale);
		this.neck.setRotationPoint(0, -6, -20);
		this.body2.addChild(this.neck);
		this.head = new ModelRenderer(this, 0, 44);
		this.head.addBox(-4, -4, -7, 8, 9, 7, scale);
		this.head.setTextureOffset(82, 6);
		this.head.addBox(-2, 0, -9, 4, 6, 5, scale);
		this.head.setRotationPoint(0, 0, -3);
		this.neck.addChild(this.head);

		this.childNeck = new ModelRenderer(this, 48, 6);
		this.childNeck.addBox(-5, -6, -3, 10, 12, 6, scale + 1.0F);
		this.childNeck.setRotationPoint(0, -7, -20);
		this.body2.addChild(this.childNeck);
		this.childHead = new ModelRenderer(this, 0, 44);
		this.childHead.addBox(-4, -4, -7, 8, 9, 7, scale + 0.8F);
		this.childHead.setTextureOffset(82, 6);
		this.childHead.addBox(-2, 0.32F, -9, 4, 6, 5, scale + 0.4F);
		this.childHead.setRotationPoint(0, -1.0F, -4.8F);
		this.childNeck.addChild(this.childHead);

		this.legFR1 = new ModelRenderer(this, 0, 72);
		this.legFR1.addBox(-3, -3, -2, 4, 7, 5, scale);
		this.legFR1.setRotationPoint(7, 0, -13);
		this.body2.addChild(this.legFR1);
		this.legFR2 = new ModelRenderer(this, 88, 86);
		this.legFR2.addBox(-3, -1, 0, 3, 13, 3, scale);
		this.legFR2.setRotationPoint(0, 4, -2);
		this.legFR1.addChild(this.legFR2);
		this.legFL1 = new ModelRenderer(this, 0, 60);
		this.legFL1.addBox(-1, -3, -2, 4, 7, 5, scale);
		this.legFL1.setRotationPoint(-7, 0, -13);
		this.legFL2 = new ModelRenderer(this, 76, 86);
		this.legFL2.addBox(0, -1, 0, 3, 13, 3, scale);
		this.legFL2.setRotationPoint(0, 4, -2);
		this.legFL1.addChild(this.legFL2);
		this.body2.addChild(this.legFL1);

		this.childLegFR1 = new ModelRenderer(this, 0, 72);
		this.childLegFR1.addBox(-3, -3, -2, 4, 7, 5, scale + 0.5F);
		this.childLegFR1.setRotationPoint(6.75F, 0.5F, -12.5F);
		this.body2.addChild(this.childLegFR1);
		this.childLegFR2 = new ModelRenderer(this, 88, 86);
		this.childLegFR2.addBox(-3, -1, 0, 3, 17, 3, scale + 0.5F);
		this.childLegFR2.setRotationPoint(0, 5, -2);
		this.childLegFR1.addChild(this.childLegFR2);
		this.childLegFL1 = new ModelRenderer(this, 0, 60);
		this.childLegFL1.addBox(-1, -3, -2, 4, 7, 5, scale + 0.5F);
		this.childLegFL1.setRotationPoint(-6.75F, 0.5F, -12.5F);
		this.childLegFL2 = new ModelRenderer(this, 76, 86);
		this.childLegFL2.addBox(0, -1, 0, 3, 17, 3, scale + 0.5F);
		this.childLegFL2.setRotationPoint(0, 5, -2);
		this.childLegFL1.addChild(this.childLegFL2);
		this.body2.addChild(this.childLegFL1);

		this.legBR1 = new ModelRenderer(this, 26, 25);
		this.legBR1.addBox(-4, -3, -3, 5, 11, 8, scale);
		this.legBR1.setRotationPoint(5, 6, 9);
		this.body3.addChild(this.legBR1);
		this.legBR2 = new ModelRenderer(this, 82, 31);
		this.legBR2.addBox(-2, 0, 0, 4, 8, 5, scale);
		this.legBR2.setRotationPoint(-2, 8, -3);
		this.legBR1.addChild(this.legBR2);
		this.legBR3 = new ModelRenderer(this, 88, 61);
		this.legBR3.addBox(-2, 0, -3, 3, 10, 3, scale);
		this.legBR3.setRotationPoint(0, 8, 5);
		this.legBR2.addChild(this.legBR3);
		this.childLegBR3 = new ModelRenderer(this, 88, 61);
		this.childLegBR3.addBox(-2, 0, -3, 3, 12, 3, 0.5F + scale);
		this.childLegBR3.setRotationPoint(0.5F, 8.25F, 4.35F);
		this.legBR2.addChild(this.childLegBR3);
		this.legBL1 = new ModelRenderer(this, 0, 25);
		this.legBL1.addBox(-1, -3, -3, 5, 11, 8, scale);
		this.legBL1.setRotationPoint(-5, 6, 9);
		this.body3.addChild(this.legBL1);
		this.legBL2 = new ModelRenderer(this, 82, 18);
		this.legBL2.addBox(-2, 0, 0, 4, 8, 5, scale);
		this.legBL2.setRotationPoint(2, 8, -3);
		this.legBL1.addChild(this.legBL2);
		this.legBL3 = new ModelRenderer(this, 56, 61);
		this.legBL3.addBox(-1, 0, -3, 3, 10, 3, scale);
		this.legBL3.setRotationPoint(0, 8, 5);
		this.legBL2.addChild(this.legBL3);
		this.childLegBL3 = new ModelRenderer(this, 56, 61);
		this.childLegBL3.addBox(-1, 0, -3, 3, 12, 3, 0.5F + scale);
		this.childLegBL3.setRotationPoint(-0.5F, 8.25F, 4.35F);
		this.legBL2.addChild(this.childLegBL3);
	}

	public ModelCaravan() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 20.0F * scale, 0.0F);
			this.childLegFL1.isHidden = false;
			this.childLegFR1.isHidden = false;
			this.legFL1.isHidden = true;
			this.legFR1.isHidden = true;
			this.childLegBL3.isHidden = false;
			this.childLegBR3.isHidden = false;
			this.legBL3.isHidden = true;
			this.legBR3.isHidden = true;
			this.legFR1.isHidden = true;
			this.childNeck.isHidden = false;
			this.childHead.isHidden = false;
			this.neck.isHidden = true;
			this.head.isHidden = true;
			this.childHump.isHidden = false;
			this.hump.isHidden = true;
			this.body1.render(scale);
			this.body2.render(scale);
		} else {
			if (((EntityCaravan)entity).isFemale()) {
				GlStateManager.translate(0.0F, 2.4F * scale, 0.0F);
				GlStateManager.scale(0.9F, 0.9F, 0.9F);
			}
			this.childLegFL1.isHidden = true;
			this.childLegFR1.isHidden = true;
			this.legFL1.isHidden = false;
			this.legFR1.isHidden = false;
			this.childLegBL3.isHidden = true;
			this.childLegBR3.isHidden = true;
			this.legBL3.isHidden = false;
			this.legBR3.isHidden = false;
			this.childNeck.isHidden = true;
			this.childHead.isHidden = true;
			this.neck.isHidden = false;
			this.head.isHidden = false;
			this.childHump.isHidden = true;
			this.hump.isHidden = false;
			this.body1.render(scale);
			this.body2.render(scale);
		}
		GlStateManager.popMatrix();
	}

	private static final float speed = 0.6f;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		this.head.rotateAngleX = headPitch * 0.008F;
		this.head.rotateAngleY = netHeadYaw * 0.008F;
		this.neck.rotateAngleX = headPitch * 0.007F;
		this.neck.rotateAngleY = netHeadYaw * 0.005F;
		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.childNeck.rotateAngleX = this.neck.rotateAngleX;
		this.childNeck.rotateAngleY = this.neck.rotateAngleY;
		if (this.isChild) {
			this.body2.rotateAngleX = (float) Math.PI / 36F;
			this.body3.rotateAngleX = -(float) Math.PI / 36F;
		} else {
			this.body2.rotateAngleX = (float) Math.PI / 12F;
			this.body3.rotateAngleX = -(float) Math.PI / 18F;
		}
		this.body1.rotateAngleY = MathHelper.cos(limbSwing * speed) * 0.1F * limbSwingAmount;
		this.body3.rotateAngleY = MathHelper.cos(limbSwing * speed) * 0.1F * limbSwingAmount;
		this.hump.rotateAngleX = MathHelper.cos(limbSwing * speed + 1.0F + (float) Math.PI * 1.5F) * 0.05F * limbSwingAmount;
		this.hump.rotateAngleZ = MathHelper.cos(limbSwing * speed + 1.0F) * 0.1F * limbSwingAmount;
		this.legFR1.rotateAngleX = MathHelper.cos(limbSwing * speed + 1.0F) * limbSwingAmount;
		this.legFL1.rotateAngleX = MathHelper.cos(limbSwing * speed + 1.0F + (float) Math.PI) * limbSwingAmount;
		if (this.isChild) {
			this.legFR2.rotateAngleX = -(float) Math.PI / 36F;
			this.legFL2.rotateAngleX = -(float) Math.PI / 36F;
		} else {
			this.legFR2.rotateAngleX = -(float) Math.PI / 12F;
			this.legFL2.rotateAngleX = -(float) Math.PI / 12F;
		}
		this.childLegFR1.rotateAngleX = this.legFR1.rotateAngleX;
		this.childLegFL1.rotateAngleX = this.legFL1.rotateAngleX;
		this.childLegFR2.rotateAngleX = this.legFR2.rotateAngleX;
		this.childLegFL2.rotateAngleX = this.legFL2.rotateAngleX;
		this.legBR1.rotateAngleX = MathHelper.cos(limbSwing * speed + (float) Math.PI) * 0.9F * limbSwingAmount;
		this.legBL1.rotateAngleX = MathHelper.cos(limbSwing * speed) * 0.9F * limbSwingAmount;
		if (this.isChild) {
			this.legBR2.rotateAngleX = (float) Math.PI / 18F;
			this.legBL2.rotateAngleX = (float) Math.PI / 18F;
		} else {
			this.legBR2.rotateAngleX = (float) Math.PI / 6F;
			this.legBL2.rotateAngleX = (float) Math.PI / 6F;
		}
		this.legBR3.rotateAngleX = -(float) Math.PI / 9F;
		this.legBL3.rotateAngleX = -(float) Math.PI / 9F;
		this.childLegBR3.rotateAngleX = -(float) Math.PI / 36F;
		this.childLegBL3.rotateAngleX = -(float) Math.PI / 36F;
	}
}