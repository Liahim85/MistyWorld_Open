package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelMonk;
import ru.liahim.mist.client.renderer.entity.layers.LayerMonkSaddle;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityMonk;

public class RenderMonk extends RenderLiving<EntityMonk> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/monk/monk_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/monk/monk_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/monk/monk_am.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/monk/monk_af.png")	//3
	};
	
	public RenderMonk(RenderManager manager) {
		super(manager, new ModelMonk(), 1.0F);
		this.addLayer(new LayerMonkSaddle(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMonk entity) {
		if (entity.isAlbino()) return entity.isChild() || entity.isFemale() ? textureLoc[3] : textureLoc[2];
		return entity.isChild() || entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}