package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelCyclops;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityCyclops;

public class RenderCyclops extends RenderLiving<EntityCyclops> {

	private static final ResourceLocation textureLoc = new ResourceLocation(Mist.MODID, "textures/entity/cyclops/cyclops.png");

	public RenderCyclops(RenderManager manager) {
		super(manager, new ModelCyclops(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCyclops entity) {
		return textureLoc;
	}
}