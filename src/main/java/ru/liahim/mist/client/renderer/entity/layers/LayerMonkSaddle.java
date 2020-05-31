package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelMonkSaddle;
import ru.liahim.mist.client.renderer.entity.RenderMonk;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityMonk;

@SideOnly(Side.CLIENT)
public class LayerMonkSaddle implements LayerRenderer<EntityMonk> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/monk/monk_saddle.png");
	private final RenderMonk renderer;
	private final ModelMonkSaddle model = new ModelMonkSaddle();

	public LayerMonkSaddle(RenderMonk renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityMonk entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entitylivingbase.isSaddled() || entitylivingbase.hasChest()) {
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