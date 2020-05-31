package ru.liahim.mist.client.renderer.entity;

import ru.liahim.mist.client.model.entity.ModelMomo;
import ru.liahim.mist.client.renderer.entity.layers.LayerMomoSaddle;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityMomo;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderMomo extends RenderLiving<EntityMomo> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/momo/momo_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/momo/momo_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/momo/momo_am.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/momo/momo_af.png")	//3
	};

	public RenderMomo(RenderManager manager) {
		super(manager, new ModelMomo(), 0.75F);
        this.addLayer(new LayerMomoSaddle(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMomo entity) {
		if (entity.isAlbino()) return entity.isChild() || entity.isFemale() ? textureLoc[3] : textureLoc[2];
		return entity.isChild() || entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}