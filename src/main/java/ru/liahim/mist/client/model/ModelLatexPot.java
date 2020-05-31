package ru.liahim.mist.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelLatexPot extends ModelBase {

	public ModelRenderer base;
	public ModelRenderer inside;
	public ModelRenderer belt;
	public ModelRenderer tube;
	public ModelRenderer tubeL;
	public ModelRenderer latex0;
	public ModelRenderer latex1;
	public ModelRenderer latex2;

	public ModelLatexPot() {
		this.textureWidth = 64;
		this.textureHeight = 48;
		this.base = new ModelRenderer(this, 0, 0);
		this.base.addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6, 0.0F);
		this.base.setRotationPoint(0, 8, 5);
		this.inside = new ModelRenderer(this, 0, 12);
		this.inside.addBox(-2.0F, 2.0F, -2.0F, 4, 5, 4, 0.0F);
		this.inside.setRotationPoint(0, -9, -5);
		this.belt = new ModelRenderer(this, 0, 16);
		this.belt.addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16, 0.01F);
		this.belt.setRotationPoint(0, 0, 16);
		this.tube = new ModelRenderer(this, 18, 0);
		this.tube.addBox(-1.0F, 0.0F, -2.0F, 2, 1, 2, 0.0F);
		this.tube.setRotationPoint(0, -1, 8);
		this.tube.rotateAngleX = (float) (Math.PI / 18);
		this.tubeL = new ModelRenderer(this, 18, 3);
		this.tubeL.addBox(-1.0F, 0.0F, -2.0F, 2, 1, 2, 0.01F);
		this.tubeL.setRotationPoint(0, -1, 8);
		this.tubeL.rotateAngleX = (float) (Math.PI / 18);
		this.latex0 = new ModelRenderer(this, 0, 21);
		this.latex0.addBox(-3.0F, -7.0F, 0.0F, 6, 7, 1, 0.01F);
		this.latex0.setRotationPoint(-2, -1, 7.98F);
		this.latex1 = new ModelRenderer(this, 40, 0);
		this.latex1.addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6, 0.0F);
		this.latex1.setRotationPoint(0, 8, 5);
		this.latex2 = new ModelRenderer(this, 40, 12);
		this.latex2.addBox(-3.0F, -6.0F, -3.0F, 6, 6, 6, 0.01F);
		this.latex2.setRotationPoint(0, 8, 5);
	}

	public void renderAll(int stage) {
		this.latex0.isHidden = stage == 0;
		this.latex1.isHidden = stage < 2 || stage == 6;
		this.latex2.isHidden = stage < 6;
		this.tubeL.isHidden = this.latex0.isHidden;
		this.latex1.rotationPointY = 14 - stage;
		this.base.render(0.0625F);
		this.inside.render(-0.0625F);
		this.belt.render(0.0625F);
		this.tube.render(0.0625F);
		this.tubeL.render(0.0625F);
		this.latex0.render(0.0625F);
		this.latex1.render(0.0625F);
		this.latex2.render(0.0625F);
	}
}