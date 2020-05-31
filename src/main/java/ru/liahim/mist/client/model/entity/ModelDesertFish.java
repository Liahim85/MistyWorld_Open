package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelDesertFish extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer tail;
	public ModelRenderer fin_L;
	public ModelRenderer fin_R;

	public ModelDesertFish(float scale) {
		this.textureWidth = 48;
		this.textureHeight = 32;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-3, -10, -4, 6, 10, 8, scale);
		this.body.setRotationPoint(0, 24, 0);
		this.head = new ModelRenderer(this, 28, 4);
		this.head.addBox(-2, -4, -5, 4, 8, 6, scale);
		this.head.setRotationPoint(0, -4, -4);
		this.body.addChild(this.head);
		this.back = new ModelRenderer(this, 0, 18);
		this.back.addBox(-2, -4, -1, 4, 8, 6, scale);
		this.back.setRotationPoint(0, -4, 4);
		this.body.addChild(this.back);
		this.tail = new ModelRenderer(this, 20, 21);
		this.tail.addBox(-1, -3, -1, 2, 6, 5, scale);
		this.tail.setRotationPoint(0, 1, 5);
		this.back.addChild(this.tail);
		this.fin_L = new ModelRenderer(this, 34, 25);
		this.fin_L.addBox(0, -3, 0, 1, 3, 4, scale);
		this.fin_L.setRotationPoint(3, -1, -3);
		this.body.addChild(this.fin_L);
		this.fin_R = new ModelRenderer(this, 34, 25);
		this.fin_R.addBox(-1, -3, 0, 1, 3, 4, scale);
		this.fin_R.setRotationPoint(-3, -1, -3);
		this.body.addChild(this.fin_R);
	}

	public ModelDesertFish() {
		this(0);
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entityIn);
		this.body.render(scale);
	}

	static final float speed = 1;
	static final float pi = (float) Math.PI;

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scaleFactor, Entity entity) {
		limbSwing *= speed;
		float y = headYaw * 0.01F;
		float f = MathHelper.cos(limbSwing) * 0.8F;
		float i = Math.abs(headYaw / 90);
		f = f * (1 - i) + y * i;
		this.body.rotateAngleZ = f/5;
		this.head.rotateAngleY = f;
		this.back.rotateAngleY = -f;
		this.tail.rotateAngleY = -f;
		f = Math.abs(f/5);
		this.head.rotateAngleX = f;
		this.back.rotateAngleX = -f;
		this.tail.rotateAngleX = -f;
		this.fin_L.rotateAngleY = limbSwingAmount * pi / 3;
		this.fin_R.rotateAngleY = -this.fin_L.rotateAngleY;
	}
}