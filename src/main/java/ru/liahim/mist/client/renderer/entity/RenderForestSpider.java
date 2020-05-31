package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelForestSpider;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityForestSpider;

public class RenderForestSpider extends RenderLiving<EntityForestSpider> {

	private static final ResourceLocation textureLoc = new ResourceLocation(Mist.MODID, "textures/entity/forest_spider/forest_spider.png");

	public RenderForestSpider(RenderManager manager) {
		super(manager, new ModelForestSpider(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityForestSpider entity) {
		return textureLoc;
	}
}