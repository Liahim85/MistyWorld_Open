package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelGraveBug;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityGraveBug;

public class RenderGraveBug extends RenderLiving<EntityGraveBug> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
		new ResourceLocation(Mist.MODID, "textures/entity/grave_bug/grave_bug_forest.png"),	//0
		new ResourceLocation(Mist.MODID, "textures/entity/grave_bug/grave_bug_desert.png"),	//1
		new ResourceLocation(Mist.MODID, "textures/entity/grave_bug/grave_bug_cold.png"),	//2
		new ResourceLocation(Mist.MODID, "textures/entity/grave_bug/grave_bug_jungle.png"),	//3
		new ResourceLocation(Mist.MODID, "textures/entity/grave_bug/grave_bug_swamp.png")	//4
	};

	public RenderGraveBug(RenderManager manager) {
		super(manager, new ModelGraveBug(), 0.4375F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGraveBug entity) {
		return textureLoc[entity.getColorType()];
	}
}