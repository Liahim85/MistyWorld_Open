package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.entity.EntityRubberBall;

public class RenderRubberBall extends RenderSnowball<EntityRubberBall> {

	public RenderRubberBall(RenderManager renderManager) {
		super(renderManager, MistItems.RUBBER, Minecraft.getMinecraft().getRenderItem());
	}
}