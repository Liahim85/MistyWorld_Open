package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelPrickler;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityPrickler;

public class RenderPrickler extends RenderLiving<EntityPrickler> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/prickler/prickler_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/prickler/prickler_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/prickler/prickler_am.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/prickler/prickler_af.png")	//3
	};

	public RenderPrickler(RenderManager manager) {
		super(manager, new ModelPrickler(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPrickler entity) {
		if (entity.isChild()) return entity.isAlbino() ? textureLoc[3] : textureLoc[1];
		if (entity.isAlbino()) return entity.isFemale() ? textureLoc[3] : textureLoc[2];
		return entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}