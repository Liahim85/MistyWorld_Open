package ru.liahim.mist.client.renderer.entity;

import ru.liahim.mist.client.model.entity.ModelBarvog;
import ru.liahim.mist.client.renderer.entity.layers.LayerBarvogSaddle;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityBarvog;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBarvog extends RenderLiving<EntityBarvog> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/barvog/barvog_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/barvog/barvog_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/barvog/barvog_am.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/barvog/barvog_af.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/barvog/barvog_c.png")		//4
	};

	public RenderBarvog(RenderManager manager) {
		super(manager, new ModelBarvog(), 0.9F);
        this.addLayer(new LayerBarvogSaddle(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBarvog entity) {
		if (entity.isChild()) return entity.isAlbino() ? textureLoc[3] : textureLoc[4];
		if (entity.isAlbino()) return entity.isFemale() ? textureLoc[3] : textureLoc[2];
		return entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}