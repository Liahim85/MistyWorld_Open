package ru.liahim.mist.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSuit extends ModelBiped {

	public static final ModelSuit thin = new ModelSuit(0.4F, true);
	public static final ModelSuit thick = new ModelSuit(0.4F, false);
	public static final ModelSuit leggins = new ModelSuit(0.2F, false);

	private final boolean smallArms;

	public ModelSuit(float scale, boolean smallArms) {
		super(scale, 0, 64, 32);
		this.smallArms = smallArms;
        this.bipedHead = new ModelRenderer(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.1F);
        this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		if (smallArms) {
			this.bipedLeftArm = new ModelRenderer(this, 40, 16);
			this.bipedLeftArm.mirror = true;
			this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, scale);
			this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
			this.bipedRightArm = new ModelRenderer(this, 40, 16);
			this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, scale);
			this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
		}
	}
}