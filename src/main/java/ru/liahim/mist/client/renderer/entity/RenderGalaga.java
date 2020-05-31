package ru.liahim.mist.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import ru.liahim.mist.client.model.entity.ModelGalaga;
import ru.liahim.mist.client.renderer.entity.layers.LayerGalagaSaddle;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.EntityGalaga;

public class RenderGalaga extends RenderLiving<EntityGalaga> {

	private static final ResourceLocation textureLoc[] = new ResourceLocation[] {
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_f.png"),		//0
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_1.png"),	//1
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_2.png"),	//2
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_3.png"),	//3
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_4.png"),	//4
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_5.png"),	//5
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_6.png"),	//6
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_m_7.png"),	//7
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_am.png"),		//8
			new ResourceLocation(Mist.MODID, "textures/entity/galaga/galaga_af.png")		//9
	};

	public RenderGalaga(RenderManager manager) {
		super(manager, new ModelGalaga(), 1.0F);
		this.addLayer(new LayerGalagaSaddle(this));
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGalaga entity) {
		if (entity.isAlbino()) return entity.isFemale() ? textureLoc[9] : textureLoc[8];
		return entity.isFemale() ? textureLoc[0] : textureLoc[entity.getColorType()];
	}
}