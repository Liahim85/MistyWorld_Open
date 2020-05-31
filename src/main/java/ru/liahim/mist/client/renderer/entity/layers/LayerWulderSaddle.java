package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelWulder;
import ru.liahim.mist.client.model.entity.ModelWulderWool;
import ru.liahim.mist.client.renderer.entity.RenderWulder;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityWulder;

@SideOnly(Side.CLIENT)
public class LayerWulderSaddle implements LayerRenderer<EntityWulder> {
	
	private static final ResourceLocation TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/wulder/wulder_saddle.png");
	private static final ResourceLocation WOOL_TEXTURE = new ResourceLocation(Mist.MODID, "textures/entity/wulder/wulder_saddle_wool.png");
	private final RenderWulder renderer;
	private final ModelWulder model = new ModelWulder(0.2F);
	private final ModelWulderWool wool = new ModelWulderWool(0.2F);

	public LayerWulderSaddle(RenderWulder renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityWulder entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (entitylivingbase.isSaddled()) {
			if (entitylivingbase.isSheared()) {
				this.renderer.bindTexture(TEXTURE);
				this.model.setModelAttributes(this.renderer.getMainModel());
				this.model.render(entitylivingbase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			} else {
				this.renderer.bindTexture(WOOL_TEXTURE);
				this.wool.setModelAttributes(this.renderer.getMainModel());
				this.wool.render(entitylivingbase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			}
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return false;
	}
}