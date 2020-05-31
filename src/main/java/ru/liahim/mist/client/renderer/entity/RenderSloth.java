package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelSloth;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntitySloth;

public class RenderSloth extends RenderLiving<EntitySloth> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/sloth/sloth_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/sloth/sloth_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/sloth/sloth_c.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/sloth/sloth_am.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/sloth/sloth_af.png")	//4
	};

	public RenderSloth(RenderManager manager) {
		super(manager, new ModelSloth(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySloth entity) {
		if (entity.isAlbino()) return entity.isChild() || entity.isFemale() ? textureLoc[4] : textureLoc[3];
		if (entity.isChild()) return textureLoc[2];
		return entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}