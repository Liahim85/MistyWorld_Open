package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelMomo;
import ru.liahim.mist.client.renderer.entity.RenderMomo;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityMomo;

@SideOnly(Side.CLIENT)
public class LayerMomoSaddle implements LayerRenderer<EntityMomo> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/momo/momo_saddle.png");
	private final RenderMomo renderer;
	private final ModelMomo model = new ModelMomo(0.2F);

	public LayerMomoSaddle(RenderMomo renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityMomo entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entitylivingbase.isSaddled()) {
			this.renderer.bindTexture(TEXTURE);
			this.model.setModelAttributes(this.renderer.getMainModel());
			this.model.render(entitylivingbase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}