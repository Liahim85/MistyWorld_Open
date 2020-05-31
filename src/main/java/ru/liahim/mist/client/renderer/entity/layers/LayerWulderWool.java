package ru.liahim.mist.client.renderer.entity.layers;

import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.client.model.entity.ModelWulderWool;
import ru.liahim.mist.client.renderer.entity.RenderWulder;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityWulder;

@SideOnly(Side.CLIENT)
public class LayerWulderWool implements LayerRenderer<EntityWulder> {
	
	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/wulder/wulder_wool.png"),		//0
		new ResourceLocation(Mist.MODID, "textures/entity/wulder/wulder_wool_a.png")	//1
	};
	private final RenderWulder renderer;
	private final ModelWulderWool model = new ModelWulderWool(0.0F);

	public LayerWulderWool(RenderWulder renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(EntityWulder entitylivingbase, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!entitylivingbase.isSheared()) {
			this.renderer.bindTexture(entitylivingbase.isAlbino() ? textureLoc[1] : textureLoc[0]);
			this.model.setModelAttributes(this.renderer.getMainModel());
			this.model.setLivingAnimations(entitylivingbase, limbSwing, limbSwingAmount, partialTicks);
			this.model.render(entitylivingbase, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}

	@Override
	public boolean shouldCombineTextures() {
		return true;
	}
}