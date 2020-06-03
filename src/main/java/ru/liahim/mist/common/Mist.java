package ru.liahim.mist.common;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.loottable.LootTables;
import ru.liahim.mist.block.MistSaltpeterOre;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.init.ModBiomes;
import ru.liahim.mist.init.ModBlocks;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.init.ModEntities;
import ru.liahim.mist.init.ModItems;
import ru.liahim.mist.init.ModRecipes;
import ru.liahim.mist.init.ModSounds;
import ru.liahim.mist.init.ModTiles;
import ru.liahim.mist.item.ItemMistSoap;
import ru.liahim.mist.world.WorldProviderMist;

@Mod(modid = Mist.MODID, name = Mist.NAME, version = Mist.VERSION, updateJSON = "https://gist.githubusercontent.com/Liahim85/cf1ee7ca8b76425bcb1ee228a3ae762a/raw/mw_updates.json" /*, dependencies = "after:jaff;"*/ )
public class Mist {

	public static final String MODID = "mist";
	public static final String NAME = "misty_world";
	public static final String VERSION = "1.2.4.7";
	public static final Logger logger = LogManager.getLogger(NAME);
	public static DimensionType dimensionType;
	public static CreativeTabs mistTab = new MistTab("mist.tab");
	public static final EnumPlantType MIST_DOWN_PLANT = EnumPlantType.getPlantType("mist_down_plant");
	public static final int FLAG = 18;
	public static boolean saltymod;
	@Instance(MODID)
	public static Mist instance;
	@SidedProxy(clientSide = "ru.liahim.mist.common.ClientProxy", serverSide = "ru.liahim.mist.common.CommonProxy")
	public static CommonProxy proxy;

	static { FluidRegistry.enableUniversalBucket(); }

	public static int getID() { return ModConfig.dimension.dimensionID; }

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger.info("Starting " + Mist.NAME + " PreInitialization");
		saltymod = Loader.isModLoaded("saltmod");
		ModConfig.init();
		ModBlocks.registerBlocks();
		ModTiles.registerTileEntity();
		ModItems.registerItems();
		ModEntities.registerEntities();
		ModBiomes.registerBiomes();
		ModRecipes.registerRecipes();
		ModSounds.registerSounds();
		ModAdvancements.load();
		proxy.preInit(event);
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.info("Starting " + Mist.NAME + " Initialization");
		ModRecipes.postRegisterRecipes();
		dimensionType = DimensionType.register(NAME, "_mist", getID(), WorldProviderMist.class, false);
		DimensionManager.registerDimension(getID(), dimensionType);
		initCompatibilityMod();
		LootTables.Init();
		proxy.init(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logger.info("Starting " + Mist.NAME + " PostInitialization");
		proxy.postInit(event);
	}

	public static void initCompatibilityMod() {
		if (Loader.isModLoaded("jaff")) {
			//com.tmtravlr.jaff.api.JustAFewFishAPI.addDimensionToBlacklist(getID());
			com.tmtravlr.jaff.api.JustAFewFishAPI.addFluidBlockToBlacklist(MistBlocks.ACID_BLOCK);
		}
		if (saltymod) {
			MistSaltpeterOre.salt = ForgeRegistries.ITEMS.getValue(new ResourceLocation("saltmod", "salt"));
			Block extractor = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("saltmod", "extractor"));
			if (extractor != null) {
				GameRegistry.addShapedRecipe(new ResourceLocation(MODID, "extractor"), new ResourceLocation("saltmod", "extractor"), new ItemStack(extractor), "RCR", "R R", "RRR", 'R', new ItemStack(MistItems.ROCKS), 'C', new ItemStack(Items.CAULDRON));
			}
			Item mudArmor = ForgeRegistries.ITEMS.getValue(new ResourceLocation("saltmod", "mud_helmet"));
			if (mudArmor != null) ItemMistSoap.mudArmor = mudArmor;
		}
	}
}