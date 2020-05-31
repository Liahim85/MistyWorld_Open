package ru.liahim.mist.network;

import ru.liahim.mist.common.Mist;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Mist.MODID.toLowerCase());

	public static void init() {
		int id = 0;
		INSTANCE.registerMessage(PacketSeedSync.Handler.class, PacketSeedSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketOpenMaskInventory.Handler.class, PacketOpenMaskInventory.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketOpenNormalInventory.Handler.class, PacketOpenNormalInventory.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketMaskSync.Handler.class, PacketMaskSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketTranzitStack.Handler.class, PacketTranzitStack.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketTimeSync.Handler.class, PacketTimeSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketFirePitUpdate.Handler.class, PacketFirePitUpdate.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketFirePitFillPot.Handler.class, PacketFirePitFillPot.class, id++, Side.SERVER);
		INSTANCE.registerMessage(PacketMushroomSync.Handler.class, PacketMushroomSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketToxicFoodSync.Handler.class, PacketToxicFoodSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketToxicSync.Handler.class, PacketToxicSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketSkillSync.Handler.class, PacketSkillSync.class, id++, Side.CLIENT);
		INSTANCE.registerMessage(PacketFurnaceClose.Handler.class, PacketFurnaceClose.class, id++, Side.SERVER);
	}
}