package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelSnowFlea;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntitySnowFlea;

public class RenderSnowFlea extends RenderLiving<EntitySnowFlea> {

	private static final ResourceLocation textureLoc = new ResourceLocation(Mist.MODID, "textures/entity/snow_flea/snow_flea.png");

	public RenderSnowFlea(RenderManager manager) {
		super(manager, new ModelSnowFlea(), 0.4375F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySnowFlea entity) {
		return textureLoc;
	}
}