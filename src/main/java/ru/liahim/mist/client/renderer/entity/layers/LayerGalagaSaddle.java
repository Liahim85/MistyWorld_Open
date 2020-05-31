package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelSalamSaddle;
import ru.liahim.mist.client.renderer.entity.RenderGalaga;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityGalaga;

@SideOnly(Side.CLIENT)
public class LayerGalagaSaddle implements LayerRenderer<EntityGalaga> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_saddle.png");
	private final RenderGalaga renderer;
	private final ModelSalamSaddle model = new ModelSalamSaddle();

	public LayerGalagaSaddle(RenderGalaga renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityGalaga entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
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