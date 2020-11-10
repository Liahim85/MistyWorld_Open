package ru.liahim.mist.init;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistBlockDoor;
import ru.liahim.mist.block.MistTreeSapling;
import ru.liahim.mist.block.upperplant.MistMycelium;
import ru.liahim.mist.client.renderer.entity.*;
import ru.liahim.mist.client.renderer.tileentity.TEISR;
import ru.liahim.mist.client.renderer.tileentity.TileEntityCampStickRenderer;
import ru.liahim.mist.client.renderer.tileentity.TileEntityCampfireRenderer;
import ru.liahim.mist.client.renderer.tileentity.TileEntityLatexPotRenderer;
import ru.liahim.mist.client.renderer.tileentity.TileEntityNiobiumChestRenderer;
import ru.liahim.mist.client.renderer.tileentity.TileEntityUrnRenderer;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.entity.*;
import ru.liahim.mist.entity.item.*;
import ru.liahim.mist.entity.item.EntityMistPainting.EnumArt;
import ru.liahim.mist.item.AchievItem;
import ru.liahim.mist.item.food.ItemMistMeat;
import ru.liahim.mist.item.food.ItemMistMushroom;
import ru.liahim.mist.tileentity.TileEntityCampStick;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.tileentity.TileEntityLatexPot;
import ru.liahim.mist.tileentity.TileEntityMistChest;
import ru.liahim.mist.tileentity.TileEntityUrn;

public class ModClientRegistry {

	public static String modid = Mist.MODID;
	public static TEISR TEISR = new TEISR();
	static final int nameSubstring = 10;

	public static void registerBlockRenderer() {
		registerBlocks(MistBlocks.STONE);
		registerBlocks(MistBlocks.STONE_POROUS);
		registerBlocks(MistBlocks.STONE_BASIC);
		registerMultyBlocks(MistBlocks.STONE_MINED, 0, "stone_mined");
		registerMultyBlocks(MistBlocks.STONE_MINED, 4, "stone_mined_moss");
		registerMultyBlocks(MistBlocks.STONE_MINED, 5, "stone_chiseled");
		registerMultyBlocks(MistBlocks.STONE_MINED, 9, "stone_chiseled_moss");
		registerMultyBlocks(MistBlocks.STONE_BRICK, 0, "stone_brick");
		registerMultyBlocks(MistBlocks.STONE_BRICK, 1, "stone_brick_moss");
		registerMultyBlocks(MistBlocks.MASONRY, 0, "masonry");
		registerMultyBlocks(MistBlocks.MASONRY, 1, "masonry_moss");
		registerMultyBlocks(MistBlocks.COBBLESTONE, 0, "cobblestone");
		registerMultyBlocks(MistBlocks.COBBLESTONE, 1, "cobblestone_moss");
		registerBlocks(MistBlocks.GRAVEL);
		registerMultyBlocks(MistBlocks.SAND, 0, "sand_dry");
		registerMultyBlocks(MistBlocks.SAND, 1, "sand_wet");
		registerMultyBlocks(MistBlocks.SAND, 2, "red_sand_dry");
		registerMultyBlocks(MistBlocks.SAND, 3, "red_sand_wet");
		registerWettable(MistBlocks.ACID_SAND);
		registerMultyBlocks(MistBlocks.CLAY, 0, "clay_gray_block_wet");
		registerMultyBlocks(MistBlocks.CLAY, 1, "clay_gray_block_dry");
		registerMultyBlocks(MistBlocks.CLAY, 2, "clay_gray_nature_wet");
		registerMultyBlocks(MistBlocks.CLAY, 3, "clay_gray_nature_dry");
		registerMultyBlocks(MistBlocks.CLAY, 4, "clay_gray_cracked_wet");
		registerMultyBlocks(MistBlocks.CLAY, 5, "clay_gray_cracked_dry");
		registerMultyBlocks(MistBlocks.CLAY, 6, "clay_gray_nature_wet");
		registerMultyBlocks(MistBlocks.CLAY, 7, "clay_gray_nature_dry");
		registerMultyBlocks(MistBlocks.CLAY, 8, "clay_red_block_wet");
		registerMultyBlocks(MistBlocks.CLAY, 9, "clay_red_block_dry");
		registerMultyBlocks(MistBlocks.CLAY, 10, "clay_red_nature_wet");
		registerMultyBlocks(MistBlocks.CLAY, 11, "clay_red_nature_dry");
		registerMultyBlocks(MistBlocks.CLAY, 12, "clay_red_cracked_wet");
		registerMultyBlocks(MistBlocks.CLAY, 13, "clay_red_cracked_dry");
		registerMultyBlocks(MistBlocks.CLAY, 14, "clay_red_nature_wet");
		registerMultyBlocks(MistBlocks.CLAY, 15, "clay_red_nature_dry");
		registerMultyBlocks(MistBlocks.FLOATING_MAT, 0, "floating_mat_g");
		registerMultyBlocks(MistBlocks.FLOATING_MAT, 1, "floating_mat");
		registerWettable(MistBlocks.PEAT);
		registerMultyBlocks(MistBlocks.SAPROPEL, 0, "sapropel_block_wet");
		registerMultyBlocks(MistBlocks.SAPROPEL, 1, "sapropel_block_dry");
		registerMultyBlocks(MistBlocks.SAPROPEL, 2, "sapropel_nature_wet");
		registerMultyBlocks(MistBlocks.SAPROPEL, 3, "sapropel_nature_dry");
		/**Ore*/
		registerBlocks(MistBlocks.FILTER_COAL_ORE);
		registerBlocks(MistBlocks.BIO_SHALE_ORE);
		registerBlocks(MistBlocks.IRON_ORE);
		registerBlocks(MistBlocks.GOLD_ORE);
		registerBlocks(MistBlocks.NIOBIUM_ORE);
		registerBlocks(MistBlocks.LAPIS_ORE);
		registerBlocks(MistBlocks.SULFUR_ORE);
		registerMultyBlocks(MistBlocks.SALTPETER_ORE, 0, "saltpeter_ore");
		registerMultyBlocks(MistBlocks.SALTPETER_ORE, 1, "salt_ore");
		registerBlocks(MistBlocks.TALLOW_BLOCK);
		registerBlocks(MistBlocks.SOAP_BLOCK);
		registerBlocks(MistBlocks.LATEX_BLOCK);
		registerBlocks(MistBlocks.RUBBER_BLOCK);
		/**Ore Block*/
		registerCoalBlock(MistBlocks.FILTER_COAL_BLOCK);
		registerBlocks(MistBlocks.BIO_SHALE_BLOCK);
		registerBlocks(MistBlocks.NIOBIUM_BLOCK);
		registerBlocks(MistBlocks.SULFUR_BLOCK);
		registerBlocks(MistBlocks.SALTPETER_BLOCK);
		/**Dirt*/
		registerWettable(MistBlocks.HUMUS_DIRT);
		registerSoil(MistBlocks.DIRT_F);
		registerSoil(MistBlocks.DIRT_S);
		registerSoil(MistBlocks.DIRT_C);
		registerSoil(MistBlocks.DIRT_R);
		registerSoil(MistBlocks.DIRT_T);
		registerWettable(MistBlocks.ACID_DIRT_0);
		registerWettable(MistBlocks.ACID_DIRT_1);
		registerWettable(MistBlocks.ACID_DIRT_2);
		/**Grass*/
		registerWettable(MistBlocks.HUMUS_GRASS);
		registerSoil(MistBlocks.GRASS_F);
		registerSoil(MistBlocks.GRASS_S);
		registerSoil(MistBlocks.GRASS_C);
		registerSoil(MistBlocks.GRASS_R);
		registerSoil(MistBlocks.GRASS_T);
		registerWettable(MistBlocks.ACID_GRASS_0);
		registerWettable(MistBlocks.ACID_GRASS_1);
		registerWettable(MistBlocks.ACID_GRASS_2);
		/**Mulch*/
		registerBlocks(MistBlocks.MULCH_BLOCK);
		/**Mycelium*/
		registerMycelium(MistBlocks.MYCELIUM);
		/**Tree trunks*/
		registerBlocks(MistBlocks.ACACIA_TRUNK);
		registerBlocks(MistBlocks.ASPEN_TRUNK);
		registerBlocks(MistBlocks.A_TREE_TRUNK);
		registerBlocks(MistBlocks.BIRCH_TRUNK);
		registerBlocks(MistBlocks.OAK_TRUNK);
		registerBlocks(MistBlocks.PINE_TRUNK);
		registerBlocks(MistBlocks.POPLAR_TRUNK);
		registerBlocks(MistBlocks.SNOW_TRUNK);
		registerBlocks(MistBlocks.SPRUSE_TRUNK);
		registerBlocks(MistBlocks.S_TREE_TRUNK);
		registerBlocks(MistBlocks.T_TREE_TRUNK);
		registerBlocks(MistBlocks.WILLOW_TRUNK);
		registerBlocks(MistBlocks.R_TREE_TRUNK);
		/**Tree leaves*/
		registerBlocks(MistBlocks.ACACIA_LEAVES);
		registerBlocks(MistBlocks.ASPEN_LEAVES);
		registerBlocks(MistBlocks.A_TREE_LEAVES);
		registerBlocks(MistBlocks.BIRCH_LEAVES);
		registerBlocks(MistBlocks.OAK_LEAVES);
		registerBlocks(MistBlocks.PINE_LEAVES);
		registerBlocks(MistBlocks.POPLAR_LEAVES);
		registerBlocks(MistBlocks.SNOW_LEAVES);
		registerBlocks(MistBlocks.SPRUSE_LEAVES);
		registerBlocks(MistBlocks.S_TREE_LEAVES);
		registerBlocks(MistBlocks.T_TREE_LEAVES);
		registerBlocks(MistBlocks.WILLOW_LEAVES);
		registerBlocks(MistBlocks.R_TREE_LEAVES);
		/**Sapling*/
		registerSapling(MistBlocks.TREE_SAPLING);
		/**Wood Block*/
		registerWoodBlocks(MistBlocks.ACACIA_BLOCK);
		registerWoodBlocks(MistBlocks.ASPEN_BLOCK);
		registerWoodBlocks(MistBlocks.A_TREE_BLOCK);
		registerWoodBlocks(MistBlocks.BIRCH_BLOCK);
		registerWoodBlocks(MistBlocks.OAK_BLOCK);
		registerWoodBlocks(MistBlocks.PINE_BLOCK);
		registerWoodBlocks(MistBlocks.POPLAR_BLOCK);
		registerWoodBlocks(MistBlocks.SNOW_BLOCK);
		registerWoodBlocks(MistBlocks.SPRUCE_BLOCK);
		registerWoodBlocks(MistBlocks.S_TREE_BLOCK);
		registerWoodBlocks(MistBlocks.T_TREE_BLOCK);
		registerWoodBlocks(MistBlocks.WILLOW_BLOCK);
		registerWoodBlocks(MistBlocks.R_TREE_BLOCK);
		/**Step*/
		registerBlocks(MistBlocks.COBBLESTONE_STEP);
		registerBlocks(MistBlocks.COBBLESTONE_MOSS_STEP);
		registerBlocks(MistBlocks.STONE_BRICK_STEP);
		registerBlocks(MistBlocks.STONE_BRICK_MOSS_STEP);
		registerBlocks(MistBlocks.ACACIA_STEP);
		registerBlocks(MistBlocks.ASPEN_STEP);
		registerBlocks(MistBlocks.A_TREE_STEP);
		registerBlocks(MistBlocks.BIRCH_STEP);
		registerBlocks(MistBlocks.OAK_STEP);
		registerBlocks(MistBlocks.PINE_STEP);
		registerBlocks(MistBlocks.POPLAR_STEP);
		registerBlocks(MistBlocks.SNOW_STEP);
		registerBlocks(MistBlocks.SPRUCE_STEP);
		registerBlocks(MistBlocks.S_TREE_STEP);
		registerBlocks(MistBlocks.T_TREE_STEP);
		registerBlocks(MistBlocks.WILLOW_STEP);
		registerBlocks(MistBlocks.R_TREE_STEP);
		/**Wall*/
		registerBlocks(MistBlocks.COBBLESTONE_WALL);
		registerBlocks(MistBlocks.COBBLESTONE_MOSS_WALL);
		registerBlocks(MistBlocks.STONE_BRICK_WALL);
		registerBlocks(MistBlocks.STONE_BRICK_MOSS_WALL);
		registerBlocks(MistBlocks.ACACIA_WALL);
		registerBlocks(MistBlocks.ASPEN_WALL);
		registerBlocks(MistBlocks.A_TREE_WALL);
		registerBlocks(MistBlocks.BIRCH_WALL);
		registerBlocks(MistBlocks.OAK_WALL);
		registerBlocks(MistBlocks.PINE_WALL);
		registerBlocks(MistBlocks.POPLAR_WALL);
		registerBlocks(MistBlocks.SNOW_WALL);
		registerBlocks(MistBlocks.SPRUCE_WALL);
		registerBlocks(MistBlocks.S_TREE_WALL);
		registerBlocks(MistBlocks.T_TREE_WALL);
		registerBlocks(MistBlocks.WILLOW_WALL);
		registerBlocks(MistBlocks.R_TREE_WALL);
		/**Slab*/
		registerMultyBlocks(MistBlocks.COBBLESTONE_SLAB, 0, "cobblestone_slab");
		registerMultyBlocks(MistBlocks.COBBLESTONE_SLAB, 1, "cobblestone_moss_slab");
		registerMultyBlocks(MistBlocks.STONE_BRICK_SLAB, 0, "stone_brick_slab");
		registerMultyBlocks(MistBlocks.STONE_BRICK_SLAB, 1, "stone_brick_moss_slab");
		registerBlocks(MistBlocks.ACACIA_SLAB);
		registerBlocks(MistBlocks.ASPEN_SLAB);
		registerBlocks(MistBlocks.A_TREE_SLAB);
		registerBlocks(MistBlocks.BIRCH_SLAB);
		registerBlocks(MistBlocks.OAK_SLAB);
		registerBlocks(MistBlocks.PINE_SLAB);
		registerBlocks(MistBlocks.POPLAR_SLAB);
		registerBlocks(MistBlocks.SNOW_SLAB);
		registerBlocks(MistBlocks.SPRUCE_SLAB);
		registerBlocks(MistBlocks.S_TREE_SLAB);
		registerBlocks(MistBlocks.T_TREE_SLAB);
		registerBlocks(MistBlocks.WILLOW_SLAB);
		registerBlocks(MistBlocks.R_TREE_SLAB);
		/**Stairs*/
		registerBlocks(MistBlocks.COBBLESTONE_STAIRS);
		registerBlocks(MistBlocks.COBBLESTONE_MOSS_STAIRS);
		registerBlocks(MistBlocks.STONE_BRICK_STAIRS);
		registerBlocks(MistBlocks.STONE_BRICK_MOSS_STAIRS);
		registerBlocks(MistBlocks.ACACIA_STAIRS);
		registerBlocks(MistBlocks.ASPEN_STAIRS);
		registerBlocks(MistBlocks.A_TREE_STAIRS);
		registerBlocks(MistBlocks.BIRCH_STAIRS);
		registerBlocks(MistBlocks.OAK_STAIRS);
		registerBlocks(MistBlocks.PINE_STAIRS);
		registerBlocks(MistBlocks.POPLAR_STAIRS);
		registerBlocks(MistBlocks.SNOW_STAIRS);
		registerBlocks(MistBlocks.SPRUCE_STAIRS);
		registerBlocks(MistBlocks.S_TREE_STAIRS);
		registerBlocks(MistBlocks.T_TREE_STAIRS);
		registerBlocks(MistBlocks.WILLOW_STAIRS);
		registerBlocks(MistBlocks.R_TREE_STAIRS);
		/**Branch*/
		registerBranch(MistBlocks.ACACIA_BRANCH);
		registerBranch(MistBlocks.ASPEN_BRANCH);
		registerBranch(MistBlocks.A_TREE_BRANCH);
		registerBranch(MistBlocks.BIRCH_BRANCH);
		registerBranch(MistBlocks.OAK_BRANCH);
		registerBranch(MistBlocks.PINE_BRANCH);
		registerBranch(MistBlocks.POPLAR_BRANCH);
		registerBranch(MistBlocks.SNOW_BRANCH);
		registerBranch(MistBlocks.SPRUCE_BRANCH);
		registerBranch(MistBlocks.S_TREE_BRANCH);
		registerBranch(MistBlocks.T_TREE_BRANCH);
		registerBranch(MistBlocks.WILLOW_BRANCH);
		registerBranch(MistBlocks.R_TREE_BRANCH);
		/**Fence*/
		registerMultyBlocks(MistBlocks.COBBLESTONE_FENCE, 0, "cobblestone_fence");
		registerMultyBlocks(MistBlocks.COBBLESTONE_FENCE, 1, "cobblestone_moss_fence");
		registerMultyBlocks(MistBlocks.STONE_BRICK_FENCE, 0, "stone_brick_fence");
		registerMultyBlocks(MistBlocks.STONE_BRICK_FENCE, 1, "stone_brick_moss_fence");
		registerFence(MistBlocks.ACACIA_FENCE);
		registerFence(MistBlocks.ASPEN_FENCE);
		registerFence(MistBlocks.A_TREE_FENCE);
		registerFence(MistBlocks.BIRCH_FENCE);
		registerFence(MistBlocks.OAK_FENCE);
		registerFence(MistBlocks.PINE_FENCE);
		registerFence(MistBlocks.POPLAR_FENCE);
		registerFence(MistBlocks.SNOW_FENCE);
		registerFence(MistBlocks.SPRUCE_FENCE);
		registerFence(MistBlocks.S_TREE_FENCE);
		registerFence(MistBlocks.T_TREE_FENCE);
		registerFence(MistBlocks.WILLOW_FENCE);
		registerFence(MistBlocks.R_TREE_FENCE);
		/**Fence Gate*/
		registerBlocks(MistBlocks.ACACIA_FENCE_GATE);
		registerBlocks(MistBlocks.ASPEN_FENCE_GATE);
		registerBlocks(MistBlocks.A_TREE_FENCE_GATE);
		registerBlocks(MistBlocks.BIRCH_FENCE_GATE);
		registerBlocks(MistBlocks.OAK_FENCE_GATE);
		registerBlocks(MistBlocks.PINE_FENCE_GATE);
		registerBlocks(MistBlocks.POPLAR_FENCE_GATE);
		registerBlocks(MistBlocks.SNOW_FENCE_GATE);
		registerBlocks(MistBlocks.SPRUCE_FENCE_GATE);
		registerBlocks(MistBlocks.S_TREE_FENCE_GATE);
		registerBlocks(MistBlocks.T_TREE_FENCE_GATE);
		registerBlocks(MistBlocks.WILLOW_FENCE_GATE);
		registerBlocks(MistBlocks.R_TREE_FENCE_GATE);
		/**Door*/
		registerDoors(MistBlocks.ACACIA_DOOR);
		registerDoors(MistBlocks.ASPEN_DOOR);
		registerDoors(MistBlocks.A_TREE_DOOR);
		registerDoors(MistBlocks.BIRCH_DOOR);
		registerDoors(MistBlocks.OAK_DOOR);
		registerDoors(MistBlocks.PINE_DOOR);
		registerDoors(MistBlocks.POPLAR_DOOR);
		registerDoors(MistBlocks.SNOW_DOOR);
		registerDoors(MistBlocks.SPRUCE_DOOR);
		registerDoors(MistBlocks.S_TREE_DOOR);
		registerDoors(MistBlocks.T_TREE_DOOR);
		registerDoors(MistBlocks.WILLOW_DOOR);
		registerDoors(MistBlocks.R_TREE_DOOR);
		registerDoors(MistBlocks.NIOBIUM_DOOR);
		/**Trapdoor*/
		registerBlocks(MistBlocks.ACACIA_TRAPDOOR);
		registerBlocks(MistBlocks.ASPEN_TRAPDOOR);
		registerBlocks(MistBlocks.A_TREE_TRAPDOOR);
		registerBlocks(MistBlocks.BIRCH_TRAPDOOR);
		registerBlocks(MistBlocks.OAK_TRAPDOOR);
		registerBlocks(MistBlocks.PINE_TRAPDOOR);
		registerBlocks(MistBlocks.POPLAR_TRAPDOOR);
		registerBlocks(MistBlocks.SNOW_TRAPDOOR);
		registerBlocks(MistBlocks.SPRUCE_TRAPDOOR);
		registerBlocks(MistBlocks.S_TREE_TRAPDOOR);
		registerBlocks(MistBlocks.T_TREE_TRAPDOOR);
		registerBlocks(MistBlocks.WILLOW_TRAPDOOR);
		registerBlocks(MistBlocks.R_TREE_TRAPDOOR);
		registerBlocks(MistBlocks.NIOBIUM_TRAPDOOR);
		/**Down plants*/
		registerMultyBlocks(MistBlocks.SPONGE, 13, "sponge_clear");
		registerMultyBlocks(MistBlocks.SPONGE, 14, "sponge_wet");
		registerMultyBlocks(MistBlocks.SPONGE, 15, "sponge_spoiled");
		/**Portal*/
		registerMultyBlocks(MistBlocks.PORTAL_BASE, 0, "portal_new_down");
		registerMultyBlocks(MistBlocks.PORTAL_BASE, 1, "portal_new_up");
		registerMultyBlocks(MistBlocks.PORTAL_BASE, 2, "portal_old_down");
		registerMultyBlocks(MistBlocks.PORTAL_BASE, 3, "portal_old_up");
		/**Gizmo*/
		registerBlocks(MistBlocks.FURNACE);
		registerTEBlocks(MistBlocks.NIOBIUM_CHEST);
		registerTEBlocks(MistBlocks.NIOBIUM_TRAPPED_CHEST);
		registerMultyBlocks(MistBlocks.URN, 0, "urn_normal");
		registerMultyBlocks(MistBlocks.URN, 1, "urn_raw");
		registerMultyBlocks(MistBlocks.REMAINS, 7, "remains_block");
		registerBlocks(MistBlocks.LATEX_POT);
		/**TE Renderer*/
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCampfire.class, new TileEntityCampfireRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCampStick.class, new TileEntityCampStickRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMistChest.class, new TileEntityNiobiumChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityUrn.class, new TileEntityUrnRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLatexPot.class, new TileEntityLatexPotRenderer());
	}

	public static void registerItemRenderer() {
		registerItems(MistItems.ROCKS);
		registerItems(MistItems.BRICK);
		registerMultyItems(MistItems.CLAY_BALL, 0, "clay_ball_gray");
		registerMultyItems(MistItems.CLAY_BALL, 1, "clay_ball_red");
		registerItems(MistItems.HUMUS);
		registerItems(MistItems.SAPROPEL_BALL);
		registerItems(MistItems.COMPOST);
		registerItems(MistItems.SPONGE_FIBRE);
		registerItems(MistItems.SPONGE_FIBRE_CLEAR);
		registerItems(MistItems.SPONGE_MEAT);
		registerItems(MistItems.SPONGE_SLIME);
		registerItems(MistItems.SPONGE_SPORE);
		registerItems(MistItems.FILTER_COAL);
		registerItems(MistItems.BIO_SHALE);
		registerItems(MistItems.SULFUR);
		registerItems(MistItems.SALTPETER);
		registerItems(MistItems.NIOBIUM_INGOT);
		registerItems(MistItems.NIOBIUM_NUGGET);
		registerItems(MistItems.MULCH);
		registerItems(MistItems.REMAINS);
		registerItems(MistItems.ASH);
		registerItems(MistItems.TALLOW);
		registerItems(MistItems.SOAP);
		registerItems(MistItems.LATEX);
		registerItems(MistItems.RUBBER);
		registerItems(MistItems.WING);
		registerItems(MistItems.SWIM_BLADDER);
		registerItems(MistItems.PILLS_BITTER);
		//Tools
		registerItems(MistItems.NIOBIUM_AXE);
		registerItems(MistItems.NIOBIUM_HOE);
		registerItems(MistItems.NIOBIUM_PICKAXE);
		registerItems(MistItems.NIOBIUM_SHOVEL);
		registerItems(MistItems.NIOBIUM_SWORD);
		registerItems(MistItems.NIOBIUM_CHISEL);
		//Armor
		registerItems(MistItems.NIOBIUM_HELMET);
		registerItems(MistItems.NIOBIUM_CHESTPLATE);
		registerItems(MistItems.NIOBIUM_LEGGINGS);
		registerItems(MistItems.NIOBIUM_BOOTS);
		registerItems(MistItems.RUBBER_HELMET);
		registerItems(MistItems.RUBBER_CHESTPLATE);
		registerItems(MistItems.RUBBER_LEGGINGS);
		registerItems(MistItems.RUBBER_BOOTS);
		//Gizmos
		registerItems(MistItems.RESPIRATOR_SINGLE);
		registerItems(MistItems.RESPIRATOR_SINGLE_OPEN);
		registerItems(MistItems.RESPIRATOR_RUBBER);
		registerItems(MistItems.RESPIRATOR_RUBBER_OPEN);
		registerItems(MistItems.HYGROMETER);
		registerItems(MistItems.GAS_ANALYZER);
		registerMultyItems(MistItems.CENTROMETER, 0, "centrometer");
		registerMultyItems(MistItems.CENTROMETER, 1, "centrometer");
		registerItems(MistItems.FLINT_AND_STONE);
		registerItems(MistItems.GLASS_CONTAINER);
		registerItems(MistItems.FOOD_ON_STICK);
		//Maps
		registerMaps(MistItems.MAP_UP);
		registerMaps(MistItems.MAP_DOWN);
		//Plant
		registerMultyItems(MistItems.DESERT_COTTON_SEED, 0, "desert_cotton_f");
		registerMultyItems(MistItems.DESERT_COTTON_SEED, 1, "desert_cotton_s");
		registerTreeSeeds(MistItems.TREE_SEEDS);
		//Food
		registerItems(MistItems.SOUP);
		registerItems(MistItems.NIGHTBERRY);
		registerItems(MistItems.TINDER_FUNGUS);
		registerMobsItems(MistItems.MEAT_FOOD, "meat_", "");
		registerMobsItems(MistItems.MEAT_COOK, "meat_", "_c");
		registerMushroom(MistItems.MUSHROOMS_FOOD, "");
		registerMushroom(MistItems.MUSHROOMS_COOK, "_c");
		//Painting
		for (EnumArt art : EntityMistPainting.EnumArt.values()) {
			registerMultyItems(MistItems.PAINTING, art.ordinal(), "painting_" + art.title);
		}
		//Achievement
		ResourceLocation[] res = new ResourceLocation[AchievItem.count + 1];
		String fileName;
		for (int i = 0; i <= AchievItem.count; ++i) {
			fileName = "achiev_item_" + String.valueOf(i);
			registerMultyItems(MistItems.ACHIEV_ITEM, i, fileName);
			res[i] = new ResourceLocation(modid + ":" + fileName);
		}
		ModelBakery.registerItemVariants(MistItems.ACHIEV_ITEM, res);
	}

	public static void registerEntityRenderer() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMossling.class, m -> new RenderMossling(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityForestRunner.class, m -> new RenderForestRunner(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityMomo.class, m -> new RenderMomo(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityBarvog.class, m -> new RenderBarvog(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityPrickler.class, m -> new RenderPrickler(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityCaravan.class, m -> new RenderCaravan(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityWulder.class, m -> new RenderWulder(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityHorb.class, m -> new RenderHorb(m));
		RenderingRegistry.registerEntityRenderingHandler(EntitySniff.class, m -> new RenderSniff(m));
		RenderingRegistry.registerEntityRenderingHandler(EntitySloth.class, m -> new RenderSloth(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityMonk.class, m -> new RenderMonk(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityGalaga.class, m -> new RenderGalaga(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityHulter.class, m -> new RenderHulter(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityBrachiodon.class, m -> new RenderBrachiodon(m));
		//Hostile
		RenderingRegistry.registerEntityRenderingHandler(EntityGraveBug.class, m -> new RenderGraveBug(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityDesertFish.class, m -> new RenderDesertFish(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityCyclops.class, m -> new RenderCyclops(m));
		RenderingRegistry.registerEntityRenderingHandler(EntitySwampCrab.class, m -> new RenderSwampCrab(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityForestSpider.class, m -> new RenderForestSpider(m));
		RenderingRegistry.registerEntityRenderingHandler(EntitySnowFlea.class, m -> new RenderSnowFlea(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityWoodlouse.class, m -> new RenderWoodlouse(m));
		//EntityItems
		RenderingRegistry.registerEntityRenderingHandler(EntityMistPainting.class, m -> new RenderMistPainting(m));
		RenderingRegistry.registerEntityRenderingHandler(EntityRubberBall.class, m -> new RenderRubberBall(m));
	}

	private static void registerWettable(Block block) {
		for (int i = 0; i < 2; ++i) {
			registerMultyBlocks(block, i, block.getUnlocalizedName().substring(nameSubstring) + (i == 0 ? "_wet" : "_dry"));
		}
	}

	private static void registerSoil(Block block) {
		for (int i = 0; i < 8; ++i) {
			registerMultyBlocks(block, i, block.getUnlocalizedName().substring(nameSubstring) + (i < 4 ? "_wet_" : "_dry_") + i % 4);
		}
	}

	private static void registerMycelium(Block block) {
		int length = MistMycelium.SoilType.values().length;
		for (int i = 0; i < length * 2; ++i) {
			registerMultyBlocks(block, i, "mycelium_" + MistMycelium.SoilType.byMetadata(i >> 1).getName() + ((i & 1) == 0 ? "_wet" : "_dry"));
		}
	}

	private static void registerCoalBlock(Block block) {
		for (int i = 0; i < 16; ++i) {
			registerMultyBlocks(block, i, block.getUnlocalizedName().substring(nameSubstring) + "_" + i/4);
		}
	}

	private static void registerWoodBlocks(Block block) {
		String blockName = block.getUnlocalizedName();
		blockName = blockName.substring(nameSubstring, blockName.length() - 5);
		registerMultyBlocks(block, 0, blockName + "log");
		registerMultyBlocks(block, 3, blockName + "log_b");
		registerMultyBlocks(block, 4, blockName + "log_c");
		registerMultyBlocks(block, 7, blockName + "log_n");
		registerMultyBlocks(block, 8, blockName + "log_d");
		registerMultyBlocks(block, 11, blockName + "log_nd");
		registerMultyBlocks(block, 13, blockName + "planks");		
	}

	private static void registerBranch(Block block) {
		String blockName = block.getUnlocalizedName().substring(nameSubstring);
		registerMultyBlocks(block, 0, blockName + "_4");
		registerMultyBlocks(block, 3, blockName + "_4_d");
		registerMultyBlocks(block, 6, blockName + "_8");
		registerMultyBlocks(block, 9, blockName + "_8_d");
	}

	private static void registerFence(Block block) {
		String blockName = block.getUnlocalizedName().substring(nameSubstring);
		registerMultyBlocks(block, 0, blockName + "_4");
		registerMultyBlocks(block, 1, blockName + "_4_d");
		registerMultyBlocks(block, 2, blockName + "_8");
		registerMultyBlocks(block, 3, blockName + "_8_d");
	}

	private static void registerSapling(Block block) {
		if (block instanceof MistTreeSapling) {
			for (MistTreeSapling.EnumType type : MistTreeSapling.EnumType.values()) {
				registerMultyBlocks(block, type.getMeta(), type.getName() + "_sapling");
			}
		}
	}

	private static void registerTreeSeeds(Item item) {
		for (MistTreeSapling.EnumType type : MistTreeSapling.EnumType.values()) {
			registerMultyItems(item, type.getMeta(), type.getName() + "_seed");
		}
	}

	private static void registerMushroom(Item item, String suffix) {
		for (int i = 0; i < ItemMistMushroom.MUSHROOMS.length; ++i) {
			for (int j = 0; j < ItemMistMushroom.MUSHROOMS[i].getTypeProperty().getAllowedValues().size(); ++j) {
				registerMultyItems(item, j + i * 16, "mushroom_" + ItemMistMushroom.MUSHROOMS[i].getTypeName(j) + suffix);
			}
		}
	}

	private static void registerMobsItems(Item item, String prefix, String suffix) {
		for (int i = 0; i < ItemMistMeat.MeatType.values().length; ++i) {
			registerMultyItems(item, i, prefix + ItemMistMeat.MeatType.byMetadata(i).getName() + suffix);
		}
	}

	private static void registerMaps(Item item) {
		ModelLoader.setCustomMeshDefinition(item, stack -> new ModelResourceLocation(modid + ":" + item.getUnlocalizedName().substring(5), "inventory"));
	}

	public static void registerItems(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(modid + ":" + item.getUnlocalizedName().substring(nameSubstring), "inventory"));
	}

	public static void registerMultyItems(Item item, int meta, String file) {
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(modid + ":" + file, "inventory"));
	}

	public static void registerBlocks(Block block) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(modid + ":" + block.getUnlocalizedName().substring(nameSubstring), "inventory"));
	}

	public static void registerDoors(MistBlockDoor block) {
		ModelLoader.setCustomModelResourceLocation(block.getDoor(), 0, new ModelResourceLocation(modid + ":" + block.getUnlocalizedName().substring(nameSubstring), "inventory"));
	}

	public static void registerMultyBlocks(Block block, int meta, String file) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(modid + ":" + file, "inventory"));
	}

	public static void registerTEBlocks(Block block) {
		Item item = Item.getItemFromBlock(block);
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(modid + ":" + block.getUnlocalizedName().substring(nameSubstring), "inventory"));
		item.setTileEntityItemStackRenderer(TEISR);
	}
}