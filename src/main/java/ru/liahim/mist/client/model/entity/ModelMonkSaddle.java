package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityMonk;

@SideOnly(Side.CLIENT)
public class ModelMonkSaddle extends ModelBase {

	static final float pi = (float) Math.PI;
	public ModelRenderer back;
	public ModelRenderer body;
	public ModelRenderer strap;
	public ModelRenderer chestL;
	public ModelRenderer chestR;

	public ModelMonkSaddle(float scale) {
		this.textureWidth = 128;
		this.textureHeight = 128;
		this.back = new ModelRenderer(this, 0, 39);
		this.back.addBox(-8, -18, -4, 16, 20, 16, scale);
		this.back.setRotationPoint(0, 9, 15);
		this.back.rotateAngleX = pi / 2.4F;
		this.body = new ModelRenderer(this, 48, 20);
		this.body.addBox(-10, -11, -1, 20, 19, 16, 0.02F);
		this.body.setRotationPoint(0, -18, -3);
		this.body.rotateAngleX = pi / 9;
		this.back.addChild(this.body);

        this.chestL = new ModelRenderer(this, 106, 12);
        this.chestL.addBox(-4, 0, 0, 8, 8, 3);
        this.chestL.setRotationPoint(8, -6, 11);
        this.chestL.rotateAngleZ = -pi / 2;
        this.chestL.rotateAngleX = -pi / 2;
		this.back.addChild(this.chestL);
        this.chestR = new ModelRenderer(this, 106, 0);
        this.chestR.addBox(-4, 0, -3, 8, 8, 3);
        this.chestR.setRotationPoint(-8, -6, 11);
        this.chestR.rotateAngleZ = -pi / 2;
        this.chestR.rotateAngleX = -pi / 2F;
		this.back.addChild(this.chestR);
		this.strap = new ModelRenderer(this, 0, 0);
		this.strap.addBox(-8, -18, -4, 16, 20, 16, 0.01F);
		this.strap.setRotationPoint(0, 0, 0);
		this.back.addChild(this.strap);
	}

	public ModelMonkSaddle() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!this.isChild) {
			this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
			boolean chest = ((EntityMonk)entity).hasChest();
			this.chestL.isHidden = !chest;
			this.chestR.isHidden = !chest;
			this.strap.isHidden = !chest;
			this.body.isHidden = !((EntityMonk)entity).isSaddled();
			this.back.render(scale);
		}
	}

	static final float speed = 0.65F;
	static final float bRadius = 12;
	static final float shift = pi/2;


	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
		float f = MathHelper.sin(limbSwing) * 0.15F * limbSwingAmount;
		float f1 = MathHelper.cos(limbSwing * 2) * 0.01F * limbSwingAmount;
		float f2 = ageInTicks - entity.ticksExisted;
        float f3 = ((EntityMonk)entity).getStandingAnimationScale(f2);
        f3 *= f3;
		this.back.rotateAngleX = pi / 2.4F + f1 - pi * f3 / 6;
		this.back.rotateAngleZ = f;
	}
}