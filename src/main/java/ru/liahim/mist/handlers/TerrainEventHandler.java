package ru.liahim.mist.handlers;

import net.minecraftforge.event.terraingen.SaplingGrowTreeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.world.MistWorld;

public class TerrainEventHandler {

	@SubscribeEvent
	public void disableTreeGrow(SaplingGrowTreeEvent event) {
		if (!event.getWorld().isRemote && event.getWorld().provider.getDimension() == Mist.getID()) {
			if (MistWorld.isPosInFog(event.getWorld(), event.getPos())) {
				event.getWorld().destroyBlock(event.getPos(), false);
				event.setResult(Result.DENY);
			} else if (ModConfig.dimension.disableVanillaTreeGrowth) event.setResult(Result.DENY);
		}
	}
}