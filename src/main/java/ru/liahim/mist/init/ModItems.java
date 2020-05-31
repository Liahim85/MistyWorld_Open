package ru.liahim.mist.init;

import static ru.liahim.mist.api.item.MistItems.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.item.*;
import ru.liahim.mist.item.food.*;

public class ModItems {

	static CreativeTabs tab = Mist.mistTab;
	public static final ArmorMaterial LEATHER_MASK_MATERIAL = addArmorMaterial("LEATHER_MASK", "mist:leather_mask", 15, new int[] {0, 0, 0, 0}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0).setRepairItem(new ItemStack(Items.LEATHER));
	public static final ArmorMaterial NIOBIUM_ARMOR = addArmorMaterial("NIOBIUM", "mist:niobium", 11, new int[] {2, 6, 7, 3}, 12, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0).setRepairItem(new ItemStack(MistItems.NIOBIUM_INGOT));
	public static final ToolMaterial NIOBIUM_TOOLS = EnumHelper.addToolMaterial("NIOBIUM", 2, 150, 8.0F, 3.0F, 12).setRepairItem(new ItemStack(NIOBIUM_INGOT)).setRepairItem(new ItemStack(MistItems.NIOBIUM_INGOT));
	public static final ArmorMaterial RUBBER_MATERIAL = addArmorMaterial("RUBBER", "mist:rubber", 15, new int[] {0, 0, 0, 0}, 12, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0).setRepairItem(new ItemStack(MistItems.RUBBER));

	public static void registerItems() {
		Mist.logger.info("Start to initialize Items");
		ACHIEV_ITEM = registerItem(new AchievItem(), "achiev_item", null);
		ROCKS = registerItem(new ItemMistRocks(), "rocks", tab);
		CLAY_BALL = registerItem(new ItemMistClayBall(), "clay_ball", tab);
		FILTER_COAL = registerItem(new ItemMistFilter(1000, 85), "filter_coal", tab);
		BIO_SHALE = registerItem(new ItemMist(), "bio_shale", tab);
		SULFUR = registerItem(new ItemMist(), "sulfur", tab);
		SALTPETER = registerItem(new ItemMistFertilizer(), "saltpeter", tab);
		SPONGE_FIBRE = registerItem(new ItemMist(), "sponge_fibre", tab);
		SPONGE_FIBRE_CLEAR = registerItem(new ItemMistFilter(300, 90), "sponge_fibre_clear", tab);
		SPONGE_MEAT = (ItemFood) registerItem(new ItemToxicFood(1, 0.6F, false, 128), "sponge_meat", tab);
		SPONGE_SPORE = registerItem(new ItemMistSeeds(MistBlocks.SPONGE), "sponge_spore", tab);
		SPONGE_SLIME = registerItem(new ItemMist(), "sponge_slime", tab);
		HUMUS = registerItem(new ItemMistFertilizer(), "humus", tab);
		SAPROPEL_BALL = registerItem(new ItemMistFertilizer(), "sapropel_ball", tab);
		MULCH = registerItem(new ItemMistMulch(), "mulch", tab);
		COMPOST = registerItem(new ItemMist(), "compost", tab);
		REMAINS = registerItem(new ItemMist(), "remains", tab);
		ASH = registerItem(new ItemMistFertilizer(), "ash", tab);
		TALLOW = registerItem(new ItemMist(), "tallow", tab);
		SOAP = registerItem(new ItemMistSoap(), "soap", tab);
		LATEX = registerItem(new ItemMist(), "latex", tab);
		RUBBER = registerItem(new ItemMistRubber(), "rubber", tab);
		WING = registerItem(new ItemMist(), "wing", tab);
		SWIM_BLADDER = (ItemFood) registerItem(new ItemToxicFood(2, 0.6F, false, -300), "swim_bladder", tab);
		NIOBIUM_INGOT = registerItem(new ItemMist(), "niobium_ingot", tab);
		NIOBIUM_NUGGET = registerItem(new ItemMist(), "niobium_nugget", tab);
		//Tools
		NIOBIUM_AXE = registerItem(new ItemMistAxe(NIOBIUM_TOOLS, 8.0F, -3.1F), "niobium_axe", tab);
		NIOBIUM_HOE = registerItem(new ItemMistHoe(NIOBIUM_TOOLS), "niobium_hoe", tab);
		NIOBIUM_PICKAXE = registerItem(new ItemMistPickaxe(NIOBIUM_TOOLS), "niobium_pickaxe", tab);
		NIOBIUM_SHOVEL = registerItem(new ItemMistShovel(NIOBIUM_TOOLS), "niobium_shovel", tab);
		NIOBIUM_SWORD = registerItem(new ItemMistSword(NIOBIUM_TOOLS), "niobium_sword", tab);
		//Armor
		NIOBIUM_HELMET = registerItem(new ItemMistArmor(NIOBIUM_ARMOR, 2, EntityEquipmentSlot.HEAD), "niobium_helmet", tab);
		NIOBIUM_CHESTPLATE = registerItem(new ItemMistArmor(NIOBIUM_ARMOR, 2, EntityEquipmentSlot.CHEST), "niobium_chestplate", tab);
		NIOBIUM_LEGGINGS = registerItem(new ItemMistArmor(NIOBIUM_ARMOR, 2, EntityEquipmentSlot.LEGS), "niobium_leggings", tab);
		NIOBIUM_BOOTS = registerItem(new ItemMistArmor(NIOBIUM_ARMOR, 2, EntityEquipmentSlot.FEET), "niobium_boots", tab);
		RUBBER_HELMET = registerItem(new ItemMistSuit(RUBBER_MATERIAL, 0, EntityEquipmentSlot.HEAD, 80), "rubber_helmet", tab);
		RUBBER_CHESTPLATE = registerItem(new ItemMistSuit(RUBBER_MATERIAL, 0, EntityEquipmentSlot.CHEST, 80), "rubber_chestplate", tab);
		RUBBER_LEGGINGS = registerItem(new ItemMistSuit(RUBBER_MATERIAL, 0, EntityEquipmentSlot.LEGS, 80), "rubber_leggings", tab);
		RUBBER_BOOTS = registerItem(new ItemMistSuit(RUBBER_MATERIAL, 0, EntityEquipmentSlot.FEET, 80), "rubber_boots", tab);
		//Gizmos
		RESPIRATOR_SINGLE = registerItem(new ItemMistMask(LEATHER_MASK_MATERIAL, 90, false), "respirator_single", tab);
		RESPIRATOR_SINGLE_OPEN = registerItem(new ItemMistMask(LEATHER_MASK_MATERIAL, 85, true), "respirator_single_open", tab);
		RESPIRATOR_RUBBER = registerItem(new ItemMistMask(RUBBER_MATERIAL, 95, false), "respirator_rubber", tab);
		RESPIRATOR_RUBBER_OPEN = registerItem(new ItemMistMask(RUBBER_MATERIAL, 90, true), "respirator_rubber_open", tab);
		HYGROMETER = registerItem(new ItemMistHygrometer(), "hygrometer", tab);
		GAS_ANALYZER = registerItem(new ItemMistGasAnalyzer(), "gas_analyzer", tab);
		CENTROMETER = registerItem(new ItemMistCentrometer(), "centrometer", tab);
		FLINT_AND_STONE = registerItem(new ItemMistFlintAndStone().setMaxDamage(48), "flint_and_stone", tab);
		GLASS_CONTAINER = (ItemFood) registerItem(new ItemMistSoup(4, true), "glass_container", tab);
		GLASS_CONTAINER.setContainerItem(GLASS_CONTAINER);
		FOOD_ON_STICK = registerItem(new ItemMistFoodOnStick(), "food_on_stick", tab);
		//Plants
		DESERT_COTTON_SEED = registerItem(new ItemMistSeedDesertCotton(), "desert_cotton_seed", tab);
		TREE_SEEDS = registerItem(new ItemMistSeedTree(), "tree_seed", tab);
		//Food
		SOUP = (ItemFood) registerItem(new ItemMistSoup(1).setContainerItem(Items.BOWL), "soup", null);
		NIGHTBERRY = (ItemFood) registerItem(new ItemMistNightberry(), "nightberry", tab);
		TINDER_FUNGUS = (ItemFood) registerItem(new ItemMistTinderFungus(), "tinder_fungus", tab);
		MEAT_FOOD = (ItemFood) registerItem(new ItemMistMeat(false), "meat_food", tab);
		MEAT_COOK = (ItemFood) registerItem(new ItemMistMeat(true), "meat_cook", tab);
		MUSHROOMS_FOOD = (ItemFood) registerItem(new ItemMistMushroom(false), "mushrooms_food", tab);
		MUSHROOMS_COOK = (ItemFood) registerItem(new ItemMistMushroom(true), "mushrooms_cook", tab);
		//Maps
		MAP_UP = registerItem(new ItemMistMapUp(), "map_up", null);
		MAP_DOWN = registerItem(new ItemMistMapDown(), "map_down", null);
		//Other
		PAINTING = registerItem(new ItemMistPainting(), "painting", tab);
		Mist.logger.info("Finished initializing Items");
	}

	private static Item registerItem(Item item, String name, CreativeTabs tab) {
		ResourceLocation RL = new ResourceLocation(Mist.MODID, name);
		item.setRegistryName(RL).setUnlocalizedName(name).setCreativeTab(tab);
		ForgeRegistries.ITEMS.register(item);
		Mist.proxy.registerItemColored(item);
		return ForgeRegistries.ITEMS.getValue(RL);
	}

	private static ItemArmor.ArmorMaterial addArmorMaterial(String enumName, String textureName, int durability, int[] reductionAmounts, int enchantability, SoundEvent soundOnEquip, float toughness) {
		return EnumHelper.addEnum(ItemArmor.ArmorMaterial.class, enumName, new Class<?>[]{String.class, int.class, int[].class, int.class, SoundEvent.class, float.class}, textureName, durability, reductionAmounts, enchantability, soundOnEquip, toughness);
	}
}