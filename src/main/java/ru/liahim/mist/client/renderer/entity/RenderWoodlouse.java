package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelWoodlouse;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityWoodlouse;

public class RenderWoodlouse extends RenderLiving<EntityWoodlouse> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
			new ResourceLocation(Mist.MODID, "textures/entity/woodlouse/woodlouse_1.png"),	//0
			new ResourceLocation(Mist.MODID, "textures/entity/woodlouse/woodlouse_2.png"),	//1
			new ResourceLocation(Mist.MODID, "textures/entity/woodlouse/woodlouse_3.png"),	//2
			new ResourceLocation(Mist.MODID, "textures/entity/woodlouse/woodlouse_4.png")	//3
	};

	public RenderWoodlouse(RenderManager manager) {
		super(manager, new ModelWoodlouse(), 0.4375F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWoodlouse entity) {
		return textureLoc[entity.getColorType()];
	}
}