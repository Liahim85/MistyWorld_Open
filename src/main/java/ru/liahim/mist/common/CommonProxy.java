package ru.liahim.mist.common;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import ru.liahim.mist.capability.MistCapability;
import ru.liahim.mist.capability.SkillCapability;
import ru.liahim.mist.capability.FoodCapability;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.capability.handler.ISkillCapaHandler;
import ru.liahim.mist.capability.handler.IFoodHandler;
import ru.liahim.mist.capability.handler.MistCapaHandler;
import ru.liahim.mist.capability.handler.SkillCapaHandler;
import ru.liahim.mist.capability.handler.FoodHandler;
import ru.liahim.mist.handlers.GuiHandler;
import ru.liahim.mist.handlers.ServerEventHandler;
import ru.liahim.mist.handlers.TerrainEventHandler;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.network.PacketHandler;
import ru.liahim.mist.world.generators.ClayLakesGenerator;
import ru.liahim.mist.world.generators.DecorateLakesGenerator;

public class CommonProxy {

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		CapabilityManager.INSTANCE.register(IMistCapaHandler.class, new MistCapability.Storage<IMistCapaHandler>(), MistCapaHandler.class);
		CapabilityManager.INSTANCE.register(ISkillCapaHandler.class, new SkillCapability.Storage<ISkillCapaHandler>(), SkillCapaHandler.class);
		CapabilityManager.INSTANCE.register(IFoodHandler.class, new FoodCapability.Storage<IFoodHandler>(), FoodHandler.class);
		MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
		MinecraftForge.TERRAIN_GEN_BUS.register(new TerrainEventHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(Mist.instance, new GuiHandler());
		PacketHandler.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		GameRegistry.registerWorldGenerator(new DecorateLakesGenerator(), 0);
		GameRegistry.registerWorldGenerator(new ClayLakesGenerator(), 1);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		ModConfig.onConfigChange();
	}

	public void registerBlockColored(Block block) {}
	public void registerItemColored(Item item) {}
	public void registerFluidBlockRendering(Block block, String name) {}

	public void setClientSeed(long seed) {}
	public long getClientSeed() { return 0; }
	public boolean hasOptifine() { return false; }
	public void onConfigChange() {}
}