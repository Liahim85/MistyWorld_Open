package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelHorb;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityHorb;

public class RenderHorb extends RenderLiving<EntityHorb> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
			new ResourceLocation(Mist.MODID, "textures/entity/horb/horb_m.png"),	//0
			new ResourceLocation(Mist.MODID, "textures/entity/horb/horb_f.png"),	//1
			new ResourceLocation(Mist.MODID, "textures/entity/horb/horb_am.png"),	//2
			new ResourceLocation(Mist.MODID, "textures/entity/horb/horb_af.png")	//3
		};

	public RenderHorb(RenderManager manager) {
		super(manager, new ModelHorb(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHorb entity) {
		if (entity.isChild()) return entity.isAlbino() ? textureLoc[3] : textureLoc[1];
		if (entity.isAlbino()) return entity.isFemale() ? textureLoc[3] : textureLoc[2];
		return entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}