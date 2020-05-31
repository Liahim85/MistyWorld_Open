package ru.liahim.mist.client.model;

import com.google.common.collect.ImmutableList;
import ru.liahim.mist.common.Mist;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class MistModelLoader implements ICustomModelLoader {

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if(!modelLocation.getResourceDomain().equals(Mist.MODID)) return false;
		String st = modelLocation.getResourcePath();
        return st.substring(0, st.length() - 2).equals("models/block/loose_rock");
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		String st = modelLocation.getResourcePath();
		st = st.substring(st.length() - 1);
		return new RockOnGroundModel(ImmutableList.of(new ResourceLocation(Mist.MODID, "blocks/rock_" + st)));
	}
}