package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelSwampCrab;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntitySwampCrab;

public class RenderSwampCrab extends RenderLiving<EntitySwampCrab> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
			new ResourceLocation(Mist.MODID, "textures/entity/swamp_crab/swamp_crab_1.png"),	//0
			new ResourceLocation(Mist.MODID, "textures/entity/swamp_crab/swamp_crab_2.png"),	//1
			new ResourceLocation(Mist.MODID, "textures/entity/swamp_crab/swamp_crab_3.png"),	//2
			new ResourceLocation(Mist.MODID, "textures/entity/swamp_crab/swamp_crab_4.png"),	//3
			new ResourceLocation(Mist.MODID, "textures/entity/swamp_crab/swamp_crab_5.png")		//4
	};

	public RenderSwampCrab(RenderManager manager) {
		super(manager, new ModelSwampCrab(), 0.4375F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySwampCrab entity) {
		return textureLoc[entity.getColorType()];
	}
}