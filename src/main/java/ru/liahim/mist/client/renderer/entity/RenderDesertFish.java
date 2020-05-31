package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelDesertFish;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityDesertFish;

public class RenderDesertFish extends RenderLiving<EntityDesertFish> {

	private static final ResourceLocation textureLoc = new ResourceLocation(Mist.MODID, "textures/entity/desert_fish/desert_fish.png");

	public RenderDesertFish(RenderManager manager) {
		super(manager, new ModelDesertFish(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDesertFish entity) {
		return textureLoc;
	}
}