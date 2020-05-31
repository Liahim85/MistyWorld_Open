package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelWulder;
import ru.liahim.mist.client.renderer.entity.layers.LayerWulderSaddle;
import ru.liahim.mist.client.renderer.entity.layers.LayerWulderWool;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityWulder;

public class RenderWulder extends RenderLiving<EntityWulder> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/wulder/wulder.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/wulder/wulder_a.png")	//1
	};

	public RenderWulder(RenderManager manager) {
		super(manager, new ModelWulder(), 1.0F);
        this.addLayer(new LayerWulderWool(this));
        this.addLayer(new LayerWulderSaddle(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWulder entity) {
		return entity.isAlbino() ? textureLoc[1] : textureLoc[0];
	}
}