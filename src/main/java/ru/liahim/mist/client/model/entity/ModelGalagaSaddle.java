package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.EntityGalaga;

@SideOnly(Side.CLIENT)
public class ModelGalagaSaddle extends ModelBase {

	public ModelRenderer body;
	public ModelRenderer tail1;
	public ModelRenderer saddle;
	public ModelRenderer chestL;
	public ModelRenderer chestR;

	public ModelGalagaSaddle(float scale) {
		this.textureWidth = 96;
		this.textureHeight = 96;
		this.body = new ModelRenderer(this, 0, 33);
		this.body.addBox(-8, -13, -9, 16, 15, 18);
		this.body.setRotationPoint(0, 8, 0);
		this.body.rotateAngleX = (float) Math.PI/36;

		this.saddle = new ModelRenderer(this, 0, 0);
		this.saddle.addBox(-8, -13, -9, 16, 15, 18, 0.02F);
		this.saddle.setRotationPoint(0, 0, 0);
		this.body.addChild(this.saddle);

		this.tail1 = new ModelRenderer(this, 0, 66);
		this.tail1.addBox(-5, 0, 0, 10, 12, 13, 0.01F);
		this.tail1.setRotationPoint(0, -11, 9);
		this.tail1.rotateAngleX = (float) -Math.PI/9;
		this.body.addChild(this.tail1);
		
        this.chestL = new ModelRenderer(this, 74, 12);
        this.chestL.addBox(-4, 0, 0, 8, 8, 3);
        this.chestL.setRotationPoint(5, 1, 8);
        this.chestL.rotateAngleY = ((float)Math.PI / 2F);
		this.tail1.addChild(this.chestL);
        this.chestR = new ModelRenderer(this, 74, 0);
        this.chestR.addBox(-4, 0, -3, 8, 8, 3);
        this.chestR.setRotationPoint(-5, 1, 8);
        this.chestR.rotateAngleY = ((float)Math.PI / 2F);
		this.tail1.addChild(this.chestR);
	}

	public ModelGalagaSaddle() {
		this(0);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		if (!this.isChild) {
			this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
			this.tail1.isHidden = !((EntityGalaga)entity).hasChest();
			this.saddle.isHidden = !((EntityGalaga)entity).isSaddled();
			this.body.render(scale);
		}
	}

	static final float speed = 0.65F;
	static final float shift = (float) (Math.PI / 5);

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		limbSwingAmount = Math.min(limbSwingAmount, 1);
		float bodyRot = MathHelper.cos(limbSwing) * 0.3F * limbSwingAmount;
		this.body.rotateAngleY = bodyRot / 2;
		bodyRot = MathHelper.cos(limbSwing - shift) * 0.3F * limbSwingAmount;
		this.tail1.rotateAngleY = -bodyRot / 2;
	}
}