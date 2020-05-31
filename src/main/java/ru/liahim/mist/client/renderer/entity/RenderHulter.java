package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelHulter;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityHulter;

public class RenderHulter extends RenderLiving<EntityHulter> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/hulter/hulter_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/hulter/hulter_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/hulter/hulter_c.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/hulter/hulter_am.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/hulter/hulter_af.png")	//4
	};

	public RenderHulter(RenderManager manager) {
		super(manager, new ModelHulter(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHulter entity) {
		if (entity.isAlbino()) return entity.isChild() || entity.isFemale() ? textureLoc[4] : textureLoc[3];
		if (entity.isChild()) return textureLoc[2];
		return entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}