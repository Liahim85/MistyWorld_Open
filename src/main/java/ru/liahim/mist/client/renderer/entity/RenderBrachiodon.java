package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelBrachiodon;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityBrachiodon;

public class RenderBrachiodon extends RenderLiving<EntityBrachiodon> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
			new ResourceLocation(Mist.MODID, "textures/entity/brachiodon/brachiodon_1.png"),	//0
			new ResourceLocation(Mist.MODID, "textures/entity/brachiodon/brachiodon_2.png"),	//1
			new ResourceLocation(Mist.MODID, "textures/entity/brachiodon/brachiodon_3.png"),	//2
			new ResourceLocation(Mist.MODID, "textures/entity/brachiodon/brachiodon_4.png"),	//3
			new ResourceLocation(Mist.MODID, "textures/entity/brachiodon/brachiodon_a.png")		//4
	};

	public RenderBrachiodon(RenderManager manager) {
		super(manager, new ModelBrachiodon(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBrachiodon entity) {
		if (entity.isAlbino()) return textureLoc[4];
		return textureLoc[entity.getColorType()];
	}
}