package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelForestRunner;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityForestRunner;

public class RenderForestRunner extends RenderLiving<EntityForestRunner> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/forest_runner/forest_runner_m.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/forest_runner/forest_runner_f.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/forest_runner/forest_runner_a.png")	//2
	};

	public RenderForestRunner(RenderManager manager) {
		super(manager, new ModelForestRunner(), 0.7F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityForestRunner entity) {
		return entity.isAlbino() ? textureLoc[2] : entity.isChild() || entity.isFemale() ? textureLoc[1] : textureLoc[0];
	}
}