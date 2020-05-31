package ru.liahim.mist.client.model.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.entity.AbstractMistChestMount;
import ru.liahim.mist.entity.EntityWulder;

@SideOnly(Side.CLIENT)
public class ModelWulder extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer headWool;
	public ModelRenderer childHead;
	public ModelRenderer childHeadWool;
	public ModelRenderer neck;
	public ModelRenderer body;
	public ModelRenderer back;
	public ModelRenderer legFR;
	public ModelRenderer legFL;
	public ModelRenderer legBR;
	public ModelRenderer legBL;
	public ModelRenderer chestL;
	public ModelRenderer chestR;
	public ModelRenderer strap;
	public ModelRenderer strapWool;
	public ModelRenderer hornL_L;
	public ModelRenderer hornL_S;
	public ModelRenderer hornL_LL;
	public ModelRenderer hornL_LS;
	public ModelRenderer hornL_SS;
	public ModelRenderer hornR_L;
	public ModelRenderer hornR_S;
	public ModelRenderer hornR_LL;
	public ModelRenderer hornR_LS;
	public ModelRenderer hornR_SS;

	private float xEatFactor;
	private float yEatFactor;

	public ModelWulder(float scale) {
		this.textureWidth = 128;
		this.textureHeight = 96;
		this.body = new ModelRenderer(this, 0, 0);
		this.body.addBox(-6, -17, -9, 12, 19, 18, scale);
		this.body.setRotationPoint(0, 8, 0);
		this.back = new ModelRenderer(this, 38, 45);
		this.back.addBox(-5, 1, -1, 10, 16, 9, scale);
		this.back.setRotationPoint(0, -15, 10);
		this.body.addChild(this.back);
		this.neck = new ModelRenderer(this, 60, 0);
		this.neck.addBox(-4, -4, -10, 8, 11, 12, scale - 0.01F);
		this.neck.setRotationPoint(0, -10, -9);
		this.body.addChild(this.neck);
		this.head = new ModelRenderer(this, 38, 73);
		this.head.addBox(-5, -5, -8, 10, 14, 9, scale);
		this.head.setRotationPoint(0, 0, -6);
		this.neck.addChild(this.head);
		this.childHead = new ModelRenderer(this, 38, 73);
		this.childHead.addBox(-5, -5, -8, 10, 14, 9, scale + 1);
		this.childHead.setRotationPoint(0, 0, -6);
		this.neck.addChild(this.childHead);
		this.headWool = new ModelRenderer(this, 0, 82);
		this.headWool.addBox(-5, 0, -8, 10, 5, 9, scale);
		this.headWool.setRotationPoint(0, 9, 0);
		this.head.addChild(this.headWool);
		this.childHeadWool = new ModelRenderer(this, 0, 82);
		this.childHeadWool.addBox(-5, 2, -8, 10, 5, 9, scale + 1);
		this.childHeadWool.setRotationPoint(0, 9, 0);
		this.childHead.addChild(this.childHeadWool);

		this.legFR = new ModelRenderer(this, 100, 48);
		this.legFR.addBox(-2, 0, -3, 4, 18, 6, scale);
		this.legFR.setRotationPoint(-4.5F, 6, -5);
		this.legFL = new ModelRenderer(this, 80, 48);
		this.legFL.addBox(-2, 0, -3, 4, 18, 6, scale);
		this.legFL.setRotationPoint(4.5F, 6, -5);
		this.legBR = new ModelRenderer(this, 100, 72);
		this.legBR.addBox(-2, 0, -3, 4, 18, 6, scale);
		this.legBR.setRotationPoint(-3.5F, 6, 14);
		this.legBL = new ModelRenderer(this, 80, 72);
		this.legBL.addBox(-2, 0, -3, 4, 18, 6, scale);
		this.legBL.setRotationPoint(3.5F, 6, 14);
		
        this.chestL = new ModelRenderer(this, 100, 12);
        this.chestL.addBox(-4, 0, 0, 8, 8, 3);
        this.chestL.setRotationPoint(5, 2, 4);
        this.chestL.rotateAngleY = ((float)Math.PI / 2F);
		this.back.addChild(this.chestL);
        this.chestR = new ModelRenderer(this, 100, 0);
        this.chestR.addBox(-4, 0, -3, 8, 8, 3);
        this.chestR.setRotationPoint(-5, 2, 4);
        this.chestR.rotateAngleY = ((float)Math.PI / 2F);
		this.back.addChild(this.chestR);

        this.strap = new ModelRenderer(this, 100, 31);
        this.strap.addBox(-5, -3, 0, 10, 6, 2, 0.01F);
        this.strap.setRotationPoint(0, 1, 4);
        this.strap.rotateAngleX = -((float)Math.PI / 2F);
		this.back.addChild(this.strap);
        this.strapWool = new ModelRenderer(this, 100, 23);
        this.strapWool.addBox(-6, -3, 0, 12, 6, 2, 0.01F);
        this.strapWool.setRotationPoint(0, 0, 4);
        this.strapWool.rotateAngleX = -((float)Math.PI / 2F);
		this.back.addChild(this.strapWool);

        this.hornL_L = new ModelRenderer(this, 0, 37);
        this.hornL_L.addBox(-2, 0, -2, 4, 6, 4);
        this.hornL_L.setRotationPoint(5, -2, -4);
        this.hornL_L.rotateAngleY = -((float)Math.PI / 2F);
        this.hornL_L.rotateAngleZ = -((float)Math.PI / 2F);
		this.head.addChild(this.hornL_L);
        this.hornL_LL = new ModelRenderer(this, 0, 47);
        this.hornL_LL.addBox(-1.5F, -1, -1.5F, 3, 7, 3);
        this.hornL_LL.setRotationPoint(0, 6, 0);
        //this.hornLL2L.rotateAngleX = ((float)Math.PI / 12F);
		this.hornL_L.addChild(this.hornL_LL);
        this.hornL_LS = new ModelRenderer(this, 0, 47);
        this.hornL_LS.addBox(-1.5F, -1, -1.5F, 3, 4, 3);
        this.hornL_LS.setRotationPoint(0, 6, 0);
        //this.hornLL2S.rotateAngleX = ((float)Math.PI / 12F);
		this.hornL_L.addChild(this.hornL_LS);
        this.hornR_L = new ModelRenderer(this, 0, 37);
        this.hornR_L.addBox(-2, 0, -2, 4, 6, 4);
        this.hornR_L.setRotationPoint(-5, -2, -4);
        this.hornR_L.rotateAngleY = ((float)Math.PI / 2F);
        this.hornR_L.rotateAngleZ = ((float)Math.PI / 2F);
		this.head.addChild(this.hornR_L);
        this.hornR_LL = new ModelRenderer(this, 0, 47);
        this.hornR_LL.addBox(-1.5F, -1, -1.5F, 3, 7, 3);
        this.hornR_LL.setRotationPoint(0, 6, 0);
        //this.hornRL2L.rotateAngleX = ((float)Math.PI / 12F);
		this.hornR_L.addChild(this.hornR_LL);
        this.hornR_LS = new ModelRenderer(this, 0, 47);
        this.hornR_LS.addBox(-1.5F, -1, -1.5F, 3, 4, 3);
        this.hornR_LS.setRotationPoint(0, 6, 0);
        //this.hornRL2S.rotateAngleX = ((float)Math.PI / 12F);
		this.hornR_L.addChild(this.hornR_LS);

        this.hornL_S = new ModelRenderer(this, 0, 37);
        this.hornL_S.addBox(-2, 0, -2, 4, 3, 4);
        this.hornL_S.setRotationPoint(5, -2, -4);
        this.hornL_S.rotateAngleY = -((float)Math.PI / 2F);
        this.hornL_S.rotateAngleZ = -((float)Math.PI / 2F);
		this.head.addChild(this.hornL_S);
        this.hornL_SS = new ModelRenderer(this, 0, 47);
        this.hornL_SS.addBox(-1.5F, -1, -1.5F, 3, 4, 3);
        this.hornL_SS.setRotationPoint(0, 3, 0);
        //this.hornLS2S.rotateAngleX = ((float)Math.PI / 12F);
		this.hornL_S.addChild(this.hornL_SS);
        this.hornR_S = new ModelRenderer(this, 0, 37);
        this.hornR_S.addBox(-2, 0, -2, 4, 3, 4);
        this.hornR_S.setRotationPoint(-5, -2, -4);
        this.hornR_S.rotateAngleY = ((float)Math.PI / 2F);
        this.hornR_S.rotateAngleZ = ((float)Math.PI / 2F);
		this.head.addChild(this.hornR_S);
        this.hornR_SS = new ModelRenderer(this, 0, 47);
        this.hornR_SS.addBox(-1.5F, -1, -1.5F, 3, 4, 3);
        this.hornR_SS.setRotationPoint(0, 3, 0);
        //this.hornRS2S.rotateAngleX = ((float)Math.PI / 12F);
		this.hornR_S.addChild(this.hornR_SS);
	}

	public ModelWulder() {
		this(0);
	}

	static final float ha = (float)Math.PI / 12F;

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch, float scale) {
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch, scale, entity);
		boolean chest = !this.isChild && entity instanceof AbstractMistChestMount && ((AbstractMistChestMount)entity).hasChest();
		boolean wool = !((EntityWulder)entity).isSheared();
		int chestX = wool ? 6 : 5;
		int chestY = wool ? 2 : 3;
		this.chestL.isHidden = !chest;
		this.chestR.isHidden = !chest;
		this.chestL.rotationPointX = chestX;
		this.chestL.rotationPointY = chestY;
		this.chestR.rotationPointX = -chestX;
		this.chestR.rotationPointY = chestY;
		this.strap.isHidden = !chest || wool;
		this.strapWool.isHidden = !chest || !wool;
		if (this.isChild || ((EntityWulder)entity).isFemale()) {
			this.hornL_L.isHidden = true;
			this.hornL_S.isHidden = true;
			this.hornR_L.isHidden = true;
			this.hornR_S.isHidden = true;
		} else {
			int [] hornArray = ((EntityWulder)entity).getHornArray();
			int typeL = hornArray[0];
			int typeR = hornArray[3];
			this.hornL_L.isHidden = typeL > 1;
			this.hornR_L.isHidden = typeR > 1;
			this.hornL_S.isHidden = typeL <= 1;
			this.hornR_S.isHidden = typeR <= 1;
			this.hornL_LL.isHidden = typeL != 0;
			this.hornR_LL.isHidden = typeR != 0;
			this.hornL_LS.isHidden = typeL != 1;
			this.hornR_LS.isHidden = typeR != 1;
			this.hornL_SS.isHidden = typeL != 2;
			this.hornR_SS.isHidden = typeR != 2;
			this.hornL_LL.rotateAngleX = ha * (hornArray[1] - 1);
			this.hornL_LL.rotateAngleZ = -ha * (hornArray[2] - 1);
			this.hornL_LS.rotateAngleX = this.hornL_LL.rotateAngleX;
			this.hornL_LS.rotateAngleZ = this.hornL_LL.rotateAngleZ;
			this.hornL_SS.rotateAngleX = this.hornL_LL.rotateAngleX;
			this.hornL_SS.rotateAngleZ = this.hornL_LL.rotateAngleZ;
			this.hornR_LL.rotateAngleX = ha * (hornArray[4] - 1);
			this.hornR_LL.rotateAngleZ = ha * (hornArray[5] - 1);
			this.hornR_LS.rotateAngleX = this.hornR_LL.rotateAngleX;
			this.hornR_LS.rotateAngleZ = this.hornR_LL.rotateAngleZ;
			this.hornR_SS.rotateAngleX = this.hornR_LL.rotateAngleX;
			this.hornR_SS.rotateAngleZ = this.hornR_LL.rotateAngleZ;
		}
		if (this.isChild) {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 20.0F * scale, 0.0F);
			this.head.isHidden = true;
			this.childHead.isHidden = false;
			this.body.render(scale);
			this.legBL.render(scale);
			this.legBR.render(scale);
			this.legFL.render(scale);
			this.legFR.render(scale);
			GlStateManager.popMatrix();
		} else {
			this.head.isHidden = false;
			this.childHead.isHidden = true;
			this.body.render(scale);
			this.legFL.render(scale);
			this.legFR.render(scale);
			this.legBL.render(scale);
			this.legBR.render(scale);
		}
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
		this.head.rotateAngleX = headPitch * 0.007F + 0.05F * xEatFactor - 0.5F * invertEatY + xNoise;
		this.head.rotateAngleY = headYaw * 0.004F * yEatFactor + yNoise;
		this.childHead.rotateAngleX = this.head.rotateAngleX;
		this.childHead.rotateAngleY = this.head.rotateAngleY;
		this.neck.rotateAngleX = headPitch * 0.007F + 0.05F * xEatFactor + 0.5F * invertEatY + xNoise;
		this.neck.rotateAngleY = headYaw * 0.004F * yEatFactor + yNoise;
		this.neck.rotationPointY = -10 + 3 * invertEatY;
		this.legFR.rotateAngleX = MathHelper.cos(limbSwing + pi) * 0.5F * limbSwingAmount;
		this.legFL.rotateAngleX = MathHelper.cos(limbSwing) * 0.5F * limbSwingAmount;
		limbSwing -= pi/5;
		this.legBR.rotateAngleX = MathHelper.cos(limbSwing) * 0.5F * limbSwingAmount;
		this.legBL.rotateAngleX = MathHelper.cos(limbSwing + pi) * 0.5F * limbSwingAmount;
		if (this.isChild) {
			this.legFR.rotationPointY = 10;
			this.legFL.rotationPointY = 10;
			this.legBR.rotationPointY = 10;
			this.legBR.rotationPointY = 10;
		} else {
			this.legFR.rotationPointY = 6;
			this.legFL.rotationPointY = 6;
			this.legBR.rotationPointY = 6;
			this.legBR.rotationPointY = 6;
		}
	}
}