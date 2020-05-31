package ru.liahim.mist.client.renderer.entity;

import ru.liahim.mist.client.model.entity.ModelMossling;
import ru.liahim.mist.client.renderer.entity.layers.LayerMosslingSaddle;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityMossling;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMossling extends RenderLiving<EntityMossling> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_c.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_am.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_af.png"),	//4
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_sm.png"),	//5
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_sf.png"),	//6
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_asm.png"),	//7
		new ResourceLocation(Mist.MODID, "textures/entity/mossling/mossling_asf.png")	//8
	};

	public RenderMossling(RenderManager manager) {
		super(manager, new ModelMossling(), 0.7F);
		this.addLayer(new LayerMosslingSaddle(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMossling entity) {
		if (entity.isChild()) return entity.isAlbino() ? textureLoc[4] : textureLoc[2];
		else if (entity.isAlbino()) {
			if (entity.isSheared()) return entity.isFemale() ? textureLoc[8] : textureLoc[7];
			else return entity.isFemale() ? textureLoc[4] : textureLoc[3];
		} else {
			if (entity.isSheared()) return entity.isFemale() ? textureLoc[6] : textureLoc[5];
			else return entity.isFemale() ? textureLoc[1] : textureLoc[0];
		}
	}
}