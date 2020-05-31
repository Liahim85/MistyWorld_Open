package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelBarvog;
import ru.liahim.mist.client.renderer.entity.RenderBarvog;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityBarvog;

@SideOnly(Side.CLIENT)
public class LayerBarvogSaddle implements LayerRenderer<EntityBarvog> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/barvog/barvog_saddle.png");
	private final RenderBarvog renderer;
	private final ModelBarvog model = new ModelBarvog(0.2F);

	public LayerBarvogSaddle(RenderBarvog renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityBarvog entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
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