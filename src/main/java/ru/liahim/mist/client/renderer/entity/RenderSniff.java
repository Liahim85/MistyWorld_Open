package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelSniff;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntitySniff;

public class RenderSniff extends RenderLiving<EntitySniff> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/sniff/sniff_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/sniff/sniff_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/sniff/sniff_c.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/sniff/sniff_am.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/sniff/sniff_af.png")	//4
	};

	public RenderSniff(RenderManager manager) {
		super(manager, new ModelSniff(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySniff entity) {
		if (entity.isAlbino()) return entity.isChild() || entity.isFemale() ? textureLoc[4] : textureLoc[3];
		return entity.isChild() ? textureLoc[2] : entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}