package ru.liahim.mist.init;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.tileentity.*;

public class ModTiles {

	public static void registerTileEntity() {
		GameRegistry.registerTileEntity(TileEntityCampfire.class, new ResourceLocation(Mist.MODID, "campfire"));
		GameRegistry.registerTileEntity(TileEntityCampStick.class, new ResourceLocation(Mist.MODID, "camp_stick"));
		GameRegistry.registerTileEntity(TileEntityMycelium.class, new ResourceLocation(Mist.MODID, "mycelium"));
		GameRegistry.registerTileEntity(TileEntityMistFurnace.class, new ResourceLocation(Mist.MODID, "furnace"));
		GameRegistry.registerTileEntity(TileEntityMistChest.class, new ResourceLocation(Mist.MODID, "niobium_chest"));
		GameRegistry.registerTileEntity(TileEntityRemains.class, new ResourceLocation(Mist.MODID, "remains"));
		GameRegistry.registerTileEntity(TileEntityUrn.class, new ResourceLocation(Mist.MODID, "urn"));
		GameRegistry.registerTileEntity(TileEntityLatexPot.class, new ResourceLocation(Mist.MODID, "latex_pot"));
	}
}