package ru.liahim.mist.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistTreeSapling;
import ru.liahim.mist.block.MistTreeSapling.EnumType;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.init.recipe.*;
import ru.liahim.mist.item.ItemMistFoodOnStick;
import ru.liahim.mist.item.food.ItemMistMeat;
import ru.liahim.mist.item.food.ItemMistMushroom;

public class ModRecipes {

	public static ItemStack ACID_BUCKET = ItemStack.EMPTY;

	public static void registerRecipes() {
		registerRecipe("acid_cleaning", new RecipesAcidCleaning());
		registerRecipe("acid_bucket", new RecipesAcidBuket());
		registerRecipe("filter_coal", new RecipesFilterCoal());
		registerRecipe("filter_coal_block", new RecipesFilterCoalBlock());
		registerRecipe("filter_coal_mix", new RecipesFilterCoalMix());
		registerRecipe("mist_map_cloning", new RecipesMistMapCloning());
		registerRecipe("mist_map_up_extending", new RecipesMistMapExtending(MistItems.MAP_UP));
		registerRecipe("mist_map_down_extending", new RecipesMistMapExtending(MistItems.MAP_DOWN));
		registerRecipe("mask_dye", new RecipesMaskDyes());
		registerRecipe("urn_dye", new RecipesUrnDyes());
		registerRecipe("mist_dirt", new RecipesMistDirt());
		registerRecipe("inner_suit", new RecipesInnerSuit());
		registerRecipe("salty_food", new RecipesSaltyFood());
		(new RecipesWood()).addRecipes();
		for (int i : ItemMistFoodOnStick.mushroomIndex) {
			ItemStack food = new ItemStack(MistItems.MUSHROOMS_FOOD, 1, i);
			ItemStack stick = new ItemStack(MistItems.FOOD_ON_STICK);
			stick.setTagCompound(new NBTTagCompound());
			stick.getTagCompound().setTag(MistTags.foodOnStickTag, food.writeToNBT(new NBTTagCompound()));
			GameRegistry.addShapedRecipe(new ResourceLocation(Mist.MODID, "food_on_stick_" + ItemMistMushroom.MUSHROOMS[i / 16].getTypeName(i % 16)), new ResourceLocation(Mist.MODID, "food_on_stick"), stick, "R ", " M", 'R', new ItemStack(Items.FISHING_ROD), 'M', food);
		}

		//Smelting
		GameRegistry.addSmelting(new ItemStack(MistItems.CLAY_BALL, 1, 0), new ItemStack(Items.BRICK), 0.3F);
		GameRegistry.addSmelting(new ItemStack(MistItems.CLAY_BALL, 1, 1), new ItemStack(Items.BRICK), 0.3F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.CLAY, 1, 0), new ItemStack(MistBlocks.CLAY, 1, 1), 0.0F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.CLAY, 1, 8), new ItemStack(MistBlocks.CLAY, 1, 9), 0.0F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.CLAY, 1, 1), new ItemStack(Blocks.HARDENED_CLAY), 0.35F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.CLAY, 1, 9), new ItemStack(Blocks.HARDENED_CLAY), 0.35F);
		GameRegistry.addSmelting(MistBlocks.FILTER_COAL_ORE, new ItemStack(MistItems.FILTER_COAL), 1.0F);
		GameRegistry.addSmelting(MistBlocks.BIO_SHALE_ORE, new ItemStack(MistItems.BIO_SHALE), 0.1F);
		GameRegistry.addSmelting(MistBlocks.SULFUR_ORE, new ItemStack(MistItems.SULFUR), 0.1F);
		GameRegistry.addSmelting(MistBlocks.SALTPETER_ORE, new ItemStack(MistItems.SALTPETER), 0.1F);
		GameRegistry.addSmelting(MistBlocks.IRON_ORE, new ItemStack(Items.IRON_INGOT), 0.7F);
		GameRegistry.addSmelting(MistBlocks.LAPIS_ORE, new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), 0.2F);
		GameRegistry.addSmelting(MistBlocks.GOLD_ORE, new ItemStack(Items.GOLD_INGOT), 1.0F);
		GameRegistry.addSmelting(MistBlocks.NIOBIUM_ORE, new ItemStack(MistItems.NIOBIUM_INGOT), 0.7F);
		GameRegistry.addSmelting(MistBlocks.COBBLESTONE, new ItemStack(MistBlocks.STONE_POROUS), 0.1F);
		GameRegistry.addSmelting(MistBlocks.SAND, new ItemStack(Blocks.GLASS), 0.1F);
		GameRegistry.addSmelting(MistBlocks.ACID_SAND, new ItemStack(Blocks.GLASS), 0.1F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.PEAT, 1, 0), new ItemStack(MistBlocks.PEAT, 1, 1), 0.0F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.SAPROPEL, 1, 0), new ItemStack(MistBlocks.SAPROPEL, 1, 1), 0.0F);
		for (int i = 0; i < ItemMistMeat.MeatType.values().length; ++i) {
			GameRegistry.addSmelting(new ItemStack(MistItems.MEAT_FOOD, 1, i), new ItemStack(MistItems.MEAT_COOK, 1, i), 0.0F);
		}
		for (int i = 0; i < ItemMistMushroom.MUSHROOMS.length; ++i) {
			for (int j = 0; j < ItemMistMushroom.MUSHROOMS[i].getTypeProperty().getAllowedValues().size(); ++j) {
				GameRegistry.addSmelting(new ItemStack(MistItems.MUSHROOMS_FOOD, 1, j + i * 16), new ItemStack(MistItems.MUSHROOMS_COOK, 1, j + i * 16), 0.0F);
			}
		}
		GameRegistry.addSmelting(MistItems.NIOBIUM_PICKAXE, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_SHOVEL, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_AXE, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_HOE, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_SWORD, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_HELMET, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_CHESTPLATE, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_LEGGINGS, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(MistItems.NIOBIUM_BOOTS, new ItemStack(MistItems.NIOBIUM_NUGGET), 0.1F);
		GameRegistry.addSmelting(new ItemStack(MistBlocks.URN, 1, 1), new ItemStack(MistBlocks.URN, 1, 0), 0.1F);

		/** OreDictionary */
		OreDictionary.registerOre("oreGold", MistBlocks.GOLD_ORE);
		OreDictionary.registerOre("oreIron", MistBlocks.IRON_ORE);
		OreDictionary.registerOre("oreLapis", MistBlocks.LAPIS_ORE);
		OreDictionary.registerOre("oreSulfur", MistBlocks.SULFUR_ORE);
		OreDictionary.registerOre("blockSulfur", MistBlocks.SULFUR_BLOCK);
		OreDictionary.registerOre("dustSulfur", MistItems.SULFUR);
		OreDictionary.registerOre("oreSaltpeter", new ItemStack(MistBlocks.SALTPETER_ORE, 1, 0));
		OreDictionary.registerOre("oreSalt", new ItemStack(MistBlocks.SALTPETER_ORE, 1, 1));
		OreDictionary.registerOre("blockSaltpeter", MistBlocks.SALTPETER_BLOCK);
		OreDictionary.registerOre("dustSaltpeter", MistItems.SALTPETER);
		OreDictionary.registerOre("clayball", Items.CLAY_BALL);
		OreDictionary.registerOre("clayball", new ItemStack(MistItems.CLAY_BALL, 1, 0));
		OreDictionary.registerOre("clayball", new ItemStack(MistItems.CLAY_BALL, 1, 1));
		OreDictionary.registerOre("mulch", MistItems.MULCH);
		OreDictionary.registerOre("tallow", MistItems.TALLOW);
		OreDictionary.registerOre("latex", MistItems.LATEX);
		OreDictionary.registerOre("rubber", MistItems.RUBBER);
		OreDictionary.registerOre("feather", MistItems.WING);
		// niobium
		OreDictionary.registerOre("oreNiobium", MistBlocks.NIOBIUM_ORE);
		OreDictionary.registerOre("blockNiobium", MistBlocks.NIOBIUM_BLOCK);
		OreDictionary.registerOre("ingotNiobium", MistItems.NIOBIUM_INGOT);
		OreDictionary.registerOre("nuggetNiobium", MistItems.NIOBIUM_NUGGET);
		// block
		OreDictionary.registerOre("sand", MistBlocks.SAND);
		OreDictionary.registerOre("sand", MistBlocks.ACID_SAND);
		OreDictionary.registerOre("gravel", MistBlocks.GRAVEL);
		OreDictionary.registerOre("stone", MistBlocks.STONE_POROUS);
		OreDictionary.registerOre("peat", MistBlocks.PEAT);
		OreDictionary.registerOre("sapropel", MistBlocks.SAPROPEL);
		// Plants
		OreDictionary.registerOre("dyeYellow", new ItemStack(MistItems.DESERT_COTTON_SEED, 1, 0));
		// sapling
		OreDictionary.registerOre("treeSapling", new ItemStack(MistBlocks.TREE_SAPLING, 1, OreDictionary.WILDCARD_VALUE));
		// leaves
		for (EnumType type : MistTreeSapling.TYPE.getAllowedValues()) {
			OreDictionary.registerOre("treeLeaves", new ItemStack(type.getTree().getLeaves(), 1, OreDictionary.WILDCARD_VALUE));
		}
	}

	public static void postRegisterRecipes() {
		ACID_BUCKET = FluidUtil.getFilledBucket(new FluidStack(MistBlocks.ACID, 0));
	}

	private static void registerRecipe(String name, IRecipe recipe) {
		ForgeRegistries.RECIPES.register(recipe.setRegistryName(new ResourceLocation(Mist.MODID, name)));
	}
}