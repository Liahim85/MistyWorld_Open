package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelPrickler extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer childHead;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer tail;
	public ModelRenderer legFL;
	public ModelRenderer legFR;
	public ModelRenderer legBL;
	public ModelRenderer legBR;

	public ModelPrickler(float scale) {
		this.textureWidth = 64;
		this.textureHeight = 64;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-6.5F, -11, -6, 13, 13, 12, scale);
		this.body.setTextureOffset(0, 25);
		this.body.addBox(-6.5F, -11, 6, 13, 13, 2, scale);
		this.body.setRotationPoint(0, 17.5F, 0);
		this.body.rotateAngleX = (float) Math.PI / 36;
		this.back = new ModelRenderer(this, 30, 25);
		this.back.addBox(-5.5F, 0, 0, 11, 10, 6, scale);
		this.back.setTextureOffset(38, 0);
		this.back.addBox(-5.5F, 0, 6, 11, 10, 2, scale);
		this.back.setRotationPoint(0, -8, 6);
		this.back.rotateAngleX = (float) -Math.PI / 12;
		this.body.addChild(this.back);
		this.tail = new ModelRenderer(this, 48, 49);
		this.tail.addBox(-2.5F, 0, 0, 5, 5, 3, scale);
		this.tail.setTextureOffset(50, 12);
		this.tail.addBox(-2.5F, 0, 3, 5, 5, 2, scale);
		this.tail.setRotationPoint(0, 5, 6);
		this.tail.rotateAngleX = (float) -Math.PI / 12;
		this.back.addChild(this.tail);
		this.head = new ModelRenderer(this, 0, 40);
		this.head.addBox(-3.5F, -3, -10, 7, 7, 11, scale);
		this.head.setTextureOffset(7, 58);
		this.head.addBox(-3.5F, 4, -10, 7, 1, 4, scale);
		this.head.setRotationPoint(0, -3, -6);
		this.head.rotateAngleX = (float) -Math.PI / 18;
		this.body.addChild(this.head);
		
		this.childHead = new ModelRenderer(this, 0, 40);
		this.childHead.addBox(-3.5F, -3, -10, 7, 7, 11, 0.5F);
		this.childHead.setRotationPoint(0, -4, -6);
		this.body.addChild(this.childHead);

		this.legBR = new ModelRenderer(this, 25, 41);
		this.legBR.addBox(-0.99F, -1, -1.5F, 3, 5, 3, scale);
		this.legBR.setRotationPoint(-4.5F, 10, 4.5F);
		this.back.addChild(this.legBR);
		this.legBL = new ModelRenderer(this, 37, 41);
		this.legBL.addBox(-2.01F, -1, -1.5F, 3, 5, 3, scale);
		this.legBL.setRotationPoint(4.5F, 10, 4.5F);
		this.back.addChild(this.legBL);
		this.legFR = new ModelRenderer(this, 49, 41);
		this.legFR.addBox(-0.99F, -1, -1.5F, 3, 5, 3, scale);
		this.legFR.setRotationPoint(-5.5F, 2, -4.5F);
		this.body.addChild(this.legFR);
		this.legFL = new ModelRenderer(this, 36, 49);
		this.legFL.addBox(-2.01F, -1, -1.5F, 3, 5, 3, scale);
		this.legFL.setRotationPoint(5.5F, 2, -4.5F);
		this.body.addChild(this.legFL);
	}

	public ModelPrickler() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			this.head.isHidden = true;
			this.childHead.isHidden = false;
		} else {
			this.head.isHidden = false;
			this.childHead.isHidden = true;
		}
		this.body.render(scale);
		GlStateManager.popMatrix();
	}

	private static final float speed = 0.8F;
	static final float pi = (float) Math.PI;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		limbSwing *= speed;
		this.head.rotateAngleX = headPitch * 0.005F - pi/18;
		this.head.rotateAngleY = netHeadYaw * 0.01F;

		this.childHead.rotateAngleX = this.head.rotateAngleX - pi/36;
		this.childHead.rotateAngleY = this.head.rotateAngleY;

		this.back.rotateAngleY = MathHelper.cos(limbSwing) * 0.1F * limbSwingAmount;
		this.tail.rotateAngleY = MathHelper.cos(limbSwing) * 0.2F * limbSwingAmount;

		this.legFR.rotateAngleX = MathHelper.cos(limbSwing + pi) * 1.4F * limbSwingAmount - pi/36;
		this.legFL.rotateAngleX = MathHelper.cos(limbSwing) * 1.4F * limbSwingAmount - pi/36;
		this.legBR.rotateAngleX = MathHelper.cos(limbSwing) * 1.4F * limbSwingAmount + pi/18;
		this.legBL.rotateAngleX = MathHelper.cos(limbSwing + pi) * 1.4F * limbSwingAmount + pi/18;
	}
}