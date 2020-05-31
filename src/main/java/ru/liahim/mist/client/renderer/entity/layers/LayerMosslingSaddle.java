package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelMossling;
import ru.liahim.mist.client.renderer.entity.RenderMossling;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityMossling;

@SideOnly(Side.CLIENT)
public class LayerMosslingSaddle implements LayerRenderer<EntityMossling> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_saddle.png");
	private final RenderMossling renderer;
	private final ModelMossling model = new ModelMossling(0.2F);

	public LayerMosslingSaddle(RenderMossling renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityMossling entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
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