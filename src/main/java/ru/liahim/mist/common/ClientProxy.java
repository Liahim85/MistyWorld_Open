package ru.liahim.mist.common;

import java.util.List;

import org.lwjgl.input.Keyboard;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import ru.liahim.mist.api.block.IColoredBlock;
import ru.liahim.mist.api.item.IColoredItem;
import ru.liahim.mist.client.renderer.CloudRendererMist;
import ru.liahim.mist.client.renderer.SkyRendererMist;
import ru.liahim.mist.client.renderer.WeatherRendererMist;
import ru.liahim.mist.client.renderer.RainParticleRenderer;
import ru.liahim.mist.client.renderer.layers.LayerRespirator;
import ru.liahim.mist.client.renderer.layers.LayerSuit;
import ru.liahim.mist.handlers.ClientEventHandler;
import ru.liahim.mist.handlers.FogRenderer;
import ru.liahim.mist.init.ItemColoring;
import ru.liahim.mist.init.ModClientRegistry;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.init.ModParticle;
import ru.liahim.mist.shader.ShaderProgram;
import ru.liahim.mist.util.FogTexture;
import ru.liahim.mist.world.MistWorld;
import com.google.common.collect.Lists;

public class ClientProxy extends CommonProxy {

	public static final IRenderHandler CloudRendererMist = new CloudRendererMist();
	public static final IRenderHandler SkyRendererMist = new SkyRendererMist();
	public static final IRenderHandler WeatherRendererMist = new WeatherRendererMist();
	public static final IRenderHandler RainParticleRenderer = new RainParticleRenderer();
	//public static ModelResourceLocation acidBucket = new ModelResourceLocation("mist:acid_bucket", "inventory");
	public static KeyBinding maskKey = new KeyBinding(I18n.format("keybind.mist.mask_inventory"), Keyboard.KEY_M, "key.categories.inventory");
	public static KeyBinding skillKey = new KeyBinding(I18n.format("keybind.mist.skill_inventory"), Keyboard.KEY_I, "key.categories.inventory");
	private static List<Block> blocksToColour = Lists.newArrayList();
	private static List<Item> itemsToColor = Lists.newArrayList();
	@Override
	public void setClientSeed(long seed) { MistWorld.setClientSeed(seed); }
	@Override
	public long getClientSeed() { return MistWorld.getClientSeed(); }

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		if (ModConfig.graphic.advancedFogRenderer) {
			ShaderProgram.initShaders();
			FogTexture.initFogTexture();
		}
		MinecraftForge.EVENT_BUS.register(new FogRenderer());
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		ModClientRegistry.registerBlockRenderer();
		ModClientRegistry.registerItemRenderer();
		ModClientRegistry.registerEntityRenderer();
	}

	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		ClientRegistry.registerKeyBinding(maskKey);
		ClientRegistry.registerKeyBinding(skillKey);
		registerLayerRenderer();
		registerColouring();
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		ModParticle.registerParticles();
		ItemColoring.createFoodColorList();
	}

	@Override
	public void registerBlockColored(Block block) {
		if (block instanceof IColoredBlock) {
			IColoredBlock coloredBlock = (IColoredBlock)block;
			if (coloredBlock.getBlockColor() != null || coloredBlock.getItemColor() != null)
				blocksToColour.add(block);
		}
	}

	@Override
	public void registerItemColored(Item item) {
		if (item instanceof IColoredItem && ((IColoredItem)item).getItemColor() != null)
			itemsToColor.add(item);
	}

	public void registerColouring() {
		for (Block block : blocksToColour) {
			IColoredBlock colorBlock = (IColoredBlock)block;
			if (colorBlock.getBlockColor() != null)
				Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(colorBlock.getBlockColor(), block);
			if (colorBlock.getItemColor() != null)
				Minecraft.getMinecraft().getItemColors().registerItemColorHandler(colorBlock.getItemColor(), block);
		}
		for (Item item : itemsToColor) {
			IColoredItem coloredItem = (IColoredItem)item;
			Minecraft.getMinecraft().getItemColors().registerItemColorHandler(coloredItem.getItemColor(), item);
		}
	}

	public void registerLayerRenderer() {
		Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default").addLayer(new LayerRespirator(Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default")));
		Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim").addLayer(new LayerRespirator(Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim")));
		Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default").addLayer(new LayerSuit(Minecraft.getMinecraft().getRenderManager().getSkinMap().get("default")));
		Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim").addLayer(new LayerSuit(Minecraft.getMinecraft().getRenderManager().getSkinMap().get("slim")));
	}

	@Override
	public void registerFluidBlockRendering(Block block, String name) {
		final ModelResourceLocation fluidLocation = new ModelResourceLocation("mist:fluids", name);
		ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return fluidLocation;
			}
		});
	}

	@Override
	public boolean hasOptifine() {
		return !FMLClientHandler.instance().hasOptifine();
	}

	@Override
	public void onConfigChange() {
		FogRenderer.updateFogQuality();
	}

	/*@Override
	public void replaceBucketTexture() {
		Map<Item, ItemMeshDefinition> meshMapping = ReflectionHelper.getPrivateValue(ItemModelMesher.class, Minecraft.getMinecraft().getRenderItem().getItemModelMesher(), "field_178092_c", "shapers");
		final ItemMeshDefinition meshDefinition = meshMapping.get(ForgeModContainer.getInstance().universalBucket);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(ForgeModContainer.getInstance().universalBucket, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				FluidStack fluidStack = FluidUtil.getFluidContained(stack);
				if (fluidStack != null) {
					if (fluidStack.getFluid() == MistAcid.instance) {
						return acidBucket;
					}
				}
				return meshDefinition == null ? ModelDynBucket.LOCATION : meshDefinition.getModelLocation(stack);
			}
		});
	}*/
}