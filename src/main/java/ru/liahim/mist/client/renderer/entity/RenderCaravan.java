package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelCaravan;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityCaravan;

public class RenderCaravan extends RenderLiving<EntityCaravan> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/caravan/caravan_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/caravan/caravan_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/caravan/caravan_c.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/caravan/caravan_am.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/caravan/caravan_af.png"),	//4
		new ResourceLocation(Mist.MODID, "textures/entity/caravan/caravan_ac.png")	//5
	};

	public RenderCaravan(RenderManager manager) {
		super(manager, new ModelCaravan(), 1.2F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCaravan entity) {
		if (entity.isChild()) return entity.isAlbino() ? textureLoc[5] : textureLoc[2];
		if (entity.isFemale()) return entity.isAlbino() ? textureLoc[4] : textureLoc[1];
		return entity.isAlbino() ? textureLoc[3] : textureLoc[0];
	}
}