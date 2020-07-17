package ru.liahim.mist.init;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.vecmath.Vector2f;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.api.registry.MistRegistry;
import ru.liahim.mist.capability.handler.ISkillCapaHandler.Skill;
import ru.liahim.mist.common.Mist;
import ru.liahim.mist.common.MistTime;
import ru.liahim.mist.tileentity.TileEntityCampfire;
import ru.liahim.mist.world.generators.TombGen;

@Config(modid = Mist.MODID, name = "mist", category = "")
public class ModConfig {

	@Ignore private static final Map<ItemStack,Vector2f> stoneColors = new HashMap<ItemStack,Vector2f>();

	@LangKey("config.mist.dimension")
    public static Dimension dimension = new Dimension();
	@LangKey("config.mist.player")
    public static Player player = new Player();
	@LangKey("config.mist.graphic")
    public static Graphic graphic = new Graphic();
	@LangKey("config.mist.campfire")
    public static Campfire campfire = new Campfire();
	@LangKey("config.mist.time")
    public static Time time = new Time();
	@LangKey("config.mist.generation")
    public static Generation generation = new Generation();

	public static void init() {}

	public static class Dimension {

		@RequiresMcRestart
		@LangKey("config.mist.dimension.id")
		@Comment("What ID number to assign to the Misty World dimension. Change if you are having conflicts with another mod")
		public int dimensionID = 69;

		@LangKey("config.mist.dimension.stones")
		@Comment("Disable portal stone drop")
		public boolean disableStoneDrop = false;

		@LangKey("config.mist.dimension.trees")
		@Comment("Disable vanilla tree growth")
		public boolean disableVanillaTreeGrowth = true;

		@LangKey("config.mist.dimension.mycelium")
		@Comment("Can mycelium be harvested without a silk touch")
		public boolean myceliumHarvesting = false;

		@LangKey("config.mist.dimension.blacklist")
		@Comment("Black list of dimensions in which it is impossible to build a portal into a Misty World")
		public int[] dimBlackList = { 1 };

		@LangKey("config.mist.dimension.breakers")
		@Comment("Assigns the mining speed multiplier of the foggy stone to the item (ModID:Item:Porous:Upper:Basic). Please do not change this parameter! This may affect the game balance")
		public String[] stoneBreakers = { "mist:niobium_pickaxe:1:8:8" };

		@LangKey("config.mist.dimension.mod_blacklist")
		@Comment("Blacklist of mobs that can't spawn in a Misty World (modId:mobName or modId:* for all mobs in the mod). For example: minecraft:pig")
		public String[] mobsBlackList = {};

		@LangKey("config.mist.dimension.cascad_lag")
		@Comment("Disable the message about cascading worldgen lag. Temporary measure until I find a solution to the problem")
		public boolean disableCascadingLog = true;

		@Ignore public static final ArrayList<Integer> loadedDimBlackList = new ArrayList<Integer>();
	}

	public static class Player {

		@LangKey("config.mist.player.search")
		@Comment("Enable search bar on creative tab")
		public boolean enableSearchBar = true;

		@LangKey("config.mist.player.cut")
		@Comment("To what percent will the values of intoxication and chemical pollution be cut after the player's death")
		@RangeInt(min = 0, max = 100)
		public int effectsReduction = 50;

		@LangKey("config.mist.player.bars")
		@Comment("Show effects progress bar on the main screen")
		public boolean showEffectsBar = true;

		@LangKey("config.mist.player.effects")
		@Comment("Show effects percentages on the main screen")
		public boolean showEffectsPercent = false;

		@LangKey("config.mist.player.soap")
		@Comment("Enable washing armor with soap")
		public boolean soapWashingArmor = true;

		@LangKey("config.mist.player.skill_factor")
		@Comment("Skill factor in order: Taming, Cutting. The lower the value, the faster the skills upgrade")
		@RangeDouble(min = 0.02, max = 10)
		public double[] skillFactor = new double[] { 1, 1 };

		@LangKey("config.mist.player.mobs_for_skill")
		@Comment("A list of creatures (not monsters) whose killing will increase the cutting skill (modId:mobName:points or modId:*:points for all mobs in the mod). For example: mist:mossling:1, mist:monk:2, mist:brachiodon:3")
		public String[] mobsForSkill = {};
	}

	public static class Graphic {

		@Ignore public static boolean smoothFogTexture = Mist.proxy.hasOptifine();
		@Ignore public static boolean mipMapOptimization = smoothFogTexture;

		@LangKey("config.mist.graphic.fog")
		@Comment("Advanced fog renderer. Adds falling shadows to the fog")
		public boolean advancedFogRenderer = true;
	}

	public static class Campfire {

		@LangKey("config.mist.campfire.stones")
		@Comment("The stones are available for the creation of the campfire base (ModID:Item:Required quantity:Metadata:Color)")
		public String[] stoneAndColors = {	"mist:rocks:4:0:9bb6af",
											"minecraft:cobblestone:1:0:bcbcbc",
											"minecraft:brick:4:0:c46c58",
											"minecraft:netherbrick:4:0:522932",
											"minecraft:flint:4:0:262626",
											"minecraft:dye:4:4:3052c1",
											"minecraft:prismarine_shard:4:0:92f0de" };

		@LangKey("config.mist.campfire.effects")
		@Comment("Enable pottage effects display")
		public boolean showSoupEffects = false;
	}

	public static class Time {

		@LangKey("config.mist.time.day")
		@Comment("The number of days in a month")
		@RangeInt(min = 2, max = 16)
		public int dayInMonth = 4;

		@LangKey("config.mist.time.cotton")
		@Comment("How many times a year will bloom desert cotton")
		@RangeInt(min = 1, max = 4)
		public int desertCottonBloomCount = 2;
	}

	public static class Generation {

		@LangKey("config.mist.generation.basement")
		@Comment("Old basement generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double basementGenerationChance = 0.2;

		@LangKey("config.mist.generation.well")
		@Comment("Wells generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double wellsGenerationChance = 0.002;

		@LangKey("config.mist.generation.tomb_forest")
		@Comment("Mixed Forest Tomb generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double forestTombGenerationChance = 0.02;

		@LangKey("config.mist.generation.tomb_swamp")
		@Comment("Swampy Tomb generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double swampTombGenerationChance = 0.007;

		@LangKey("config.mist.generation.tomb_desert")
		@Comment("Desert and Savanna Tomb generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double desertTombGenerationChance = 0.005;

		@LangKey("config.mist.generation.tomb_snow")
		@Comment("Taiga Tomb generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double snowTombGenerationChance = 0.015;

		@LangKey("config.mist.generation.tomb_jungle")
		@Comment("Jungle Tomb generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double jungleTombGenerationChance = 0.008;

		@LangKey("config.mist.generation.tomb_cliff")
		@Comment("Cliff Tomb generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double cliffTombGenerationChance = 0.15;

		@LangKey("config.mist.generation.altar")
		@Comment("Altar generation chance (0 - never, 1 - each chunk)")
		@RangeDouble(min = 0, max = 1)
		public double altarGenerationChance = 0.005;

		@LangKey("config.mist.generation.rare_urn")
		@Comment("Rare Urn generation chance (0 - never, 1 - each urn)")
		@RangeDouble(min = 0, max = 1)
		public double rareUrnGenerationChance = 0.1;

		@LangKey("config.mist.generation.desert_fish")
		@Comment("Desert Fish generation chance (0 - never, 1 - each block)")
		@RangeDouble(min = 0, max = 1)
		public double desertFishGenRarity = 0.015;
	}

	public static void onConfigChange() {
		ConfigManager.sync(Mist.MODID, Type.INSTANCE);
		for (int i : ModConfig.dimension.dimBlackList) Dimension.loadedDimBlackList.add(i);
		MistTime.setMonthLength(ModConfig.time.dayInMonth);
		for (Skill skill : Skill.values()) skill.updateSizes();
		ModConfig.applyFirePitColors(false);
		ModConfig.applyStoneBreakers();
		ModConfig.applyMobsForSkill();
		ModConfig.applyMobsBlackList();
		TombGen.updateChance();
	}

	public static long getCustomSeed(long seed) {
        /*String s = config.get("dimension", "customSeed", "").getString();
        if (!StringUtils.isNullOrEmpty(s)) {
            try {
                long j = Long.parseLong(s);
                if (j != 0L) seed = j;
            }
            catch (NumberFormatException num) {
            	long j = s.hashCode();
            	if (j != 0L) seed = j;
            }
        }*/
        //MistWorld.setCustomSeed(seed);
		return seed;
	}

	public static void applyFirePitColors(boolean repeat) {
		if (!repeat) {
			stoneColors.clear();
			Pattern splitpattern = Pattern.compile(":");
			for (int i = 0; i < ModConfig.campfire.stoneAndColors.length; i++) {
				String s = ModConfig.campfire.stoneAndColors[i];
				String[] pettern = splitpattern.split(s);
				if (pettern.length != 5) {
					Mist.logger.warn("Invalid set of parameters at stoneAndColors line " + (i + 1));
					continue;
				}
				ResourceLocation res = new ResourceLocation(pettern[0], pettern[1]);
				Item item;
				int count;
				int meta;
				int color;
				if (ForgeRegistries.ITEMS.containsKey(res)) {
					item = ForgeRegistries.ITEMS.getValue(res);
				} else if (ForgeRegistries.BLOCKS.containsKey(res)) {
					item = Item.getItemFromBlock(ForgeRegistries.BLOCKS.getValue(res));
				} else {
					Mist.logger.warn("Cannot found item/block \"" + pettern[0] + ":" + pettern[1] + "\" from stoneAndColors line " + (i + 1));
					continue;
				}
				try {
					count = Integer.parseInt(pettern[2]);
					if (count < 1 || count > 4) {
						MathHelper.clamp(count, 1, 4);
						Mist.logger.warn("Count \"" + pettern[2] + "\" out of valid range point at stoneAndColors line " + (i + 1) + ". Count will be changed to " + count);
					}
				} catch (NumberFormatException e) {
					Mist.logger.warn("Cannot parse Count \"" + pettern[2] + "\" to integer point at stoneAndColors line " + (i + 1));
					continue;
				}
				try {
					meta = Integer.parseInt(pettern[3]);
					if (meta < 0 || (meta > 0 && item.getMaxDamage() != 0) || (meta > 15 && item instanceof ItemBlock)) {
						meta = 0;
						Mist.logger.warn("Metadata \"" + pettern[3] + "\" out of valid range point at stoneAndColors line " + (i + 1) + ". Metadata will be changed to " + meta);
					}
				} catch (NumberFormatException e) {
					Mist.logger.warn("Cannot parse Metadata \"" + pettern[3] + "\" to integer point at stoneAndColors line " + (i + 1));
					continue;
				}
				try {
					color = Integer.parseInt(pettern[4], 16);
				} catch (NumberFormatException e) {
					Mist.logger.warn("Cannot parse Color \"" + pettern[4] + "\" to integer point at stoneAndColors line " + (i + 1));
					continue;
				}
				boolean check = false;
				lab:
				{
					for (ItemStack stones : stoneColors.keySet()) {
						if (stones.getItem() == item && stones.getItemDamage() == meta) {
							check = true;
							Mist.logger.warn("Item \"" + pettern[0] + ":" + pettern[1] + "\" with Metadata \"" + pettern[3] + "\" is already exist");
							break lab;
						}
					}
					if (!check) stoneColors.put(new ItemStack(item, 1, meta), new Vector2f(count, color));
				}
			}
			if (stoneColors.isEmpty()) stoneColors.put(new ItemStack(MistItems.ROCKS), new Vector2f(4, 0x9bb6af));
		}
		TileEntityCampfire.putStoneAndColorList(stoneColors);
	}

	public static void applyStoneBreakers() {
		MistRegistry.mistStoneBreakers.clear();
		Pattern splitpattern = Pattern.compile(":");
		for (int i = 0; i < ModConfig.dimension.stoneBreakers.length; i++) {
			String s = ModConfig.dimension.stoneBreakers[i];
			String[] pettern = splitpattern.split(s);
			if (pettern.length != 5) {
				Mist.logger.warn("Invalid set of parameters at stoneBreakers line " + (i + 1));
				continue;
			}
			ResourceLocation res = new ResourceLocation(pettern[0], pettern[1]);
			Item item;
			int porous;
			int upper;
			int basic;
			if (ForgeRegistries.ITEMS.containsKey(res)) {
				item = ForgeRegistries.ITEMS.getValue(res);
			} else {
				Mist.logger.warn("Cannot found item \"" + pettern[0] + ":" + pettern[1] + "\" from stoneBreakers line " + (i + 1));
				continue;
			}
			try {
				porous = Integer.parseInt(pettern[2]);
				if (porous < 0) {
					porous = 1;
					Mist.logger.warn("Porous stone multiplier \"" + pettern[2] + "\" less than zero point at stoneBreakers line " + (i + 1) + ". Multiplier will be changed to " + porous);
				}
			} catch (NumberFormatException e) {
				Mist.logger.warn("Cannot parse porous stone multiplier \"" + pettern[2] + "\" to integer point at stoneBreakers line " + (i + 1));
				continue;
			}
			try {
				upper = Integer.parseInt(pettern[3]);
				if (upper < 0) {
					upper = 1;
					Mist.logger.warn("Upper stone multiplier \"" + pettern[3] + "\" less than zero point at stoneBreakers line " + (i + 1) + ". Multiplier will be changed to " + upper);
				}
			} catch (NumberFormatException e) {
				Mist.logger.warn("Cannot parse upper stone multiplier \"" + pettern[3] + "\" to integer point at stoneBreakers line " + (i + 1));
				continue;
			}
			try {
				basic = Integer.parseInt(pettern[4]);
				if (basic < 0) {
					basic = 1;
					Mist.logger.warn("Basic stone multiplier \"" + pettern[4] + "\" less than zero point at stoneBreakers line " + (i + 1) + ". Multiplier will be changed to " + basic);
				}
			} catch (NumberFormatException e) {
				Mist.logger.warn("Cannot parse basic stone multiplier \"" + pettern[4] + "\" to integer point at stoneBreakers line " + (i + 1));
				continue;
			}
			boolean check = false;
			lab:
			{
				for (Item tool : MistRegistry.mistStoneBreakers.keySet()) {
					if (tool == item) {
						check = true;
						Mist.logger.warn("Item \"" + pettern[0] + ":" + pettern[1] + "\" is already exist (stoneBreakers, line " + (i + 1) + ")");
						break lab;
					}
				}
				if (!check) MistRegistry.mistStoneBreakers.put(item, new int[] {porous, upper, basic});
			}
		}
		if (MistRegistry.mistStoneBreakers.isEmpty()) MistRegistry.mistStoneBreakers.put(MistItems.NIOBIUM_PICKAXE, new int[] {1, 8, 8});
	}

	public static void applyMobsForSkill() {
		MistRegistry.dimsForSkill.clear();
		MistRegistry.mobsForSkill.clear();
		Pattern splitpattern = Pattern.compile(":");
		for (int i = 0; i < ModConfig.player.mobsForSkill.length; i++) {
			String[] pettern = splitpattern.split(ModConfig.player.mobsForSkill[i]);
			if (pettern.length != 3) {
				Mist.logger.warn("Invalid set of parameters at mobsForSkill line " + (i + 1));
				continue;
			}
			if (!Loader.isModLoaded(pettern[0])) {
				Mist.logger.warn("Cannot found the modId \"" + pettern[0] + "\" from mobsForSkill line " + (i + 1));
				continue;
			} else if (pettern[0].equals(Mist.MODID)) {
				Mist.logger.warn("Misty mobs are already participating in skill calculation (mobsForSkill, line " + (i + 1) + ")");
				continue;
			} else {
				int point = 0;
				try {
					point = Integer.parseInt(pettern[2]);
					if (point < 0) {
						point = 1;
						Mist.logger.warn("Mob skill points \"" + pettern[2] + "\" less than zero point at mobsForSkill line " + (i + 1) + ". Points will be changed to " + point);
					}
				} catch (NumberFormatException e) {
					Mist.logger.warn("Cannot parse the mob skill points \"" + pettern[2] + "\" to integer point at mobsForSkill line " + (i + 1));
					continue;
				}
				if (!pettern[1].equals("*")) {
					ResourceLocation res = new ResourceLocation(pettern[0], pettern[1]);
					if (!ForgeRegistries.ENTITIES.containsKey(res)) {
						Mist.logger.warn("Cannot found the mob \"" + pettern[0] + ":" + pettern[1] + "\" from mobsForSkill line " + (i + 1));
						continue;
					} else if (MistRegistry.mobsForSkill.containsKey(res)) {
						Mist.logger.warn("Mob \"" + pettern[0] + ":" + pettern[1] + "\" is already exist (mobsForSkill, line " + (i + 1) + ")");
						continue;
					} else {
						MistRegistry.mobsForSkill.put(res, Integer.valueOf(point));
					}
				} else MistRegistry.dimsForSkill.put(pettern[0], Integer.valueOf(point));
			}
		}
	}

	public static void applyMobsBlackList() {
		MistRegistry.mobsDimsBlackList.clear();
		MistRegistry.mobsBlackList.clear();
		Pattern splitpattern = Pattern.compile(":");
		for (int i = 0; i < ModConfig.dimension.mobsBlackList.length; i++) {
			String[] pettern = splitpattern.split(ModConfig.dimension.mobsBlackList[i]);
			if (pettern.length != 2) {
				Mist.logger.warn("Invalid set of parameters at mobsBlackList line " + (i + 1));
				continue;
			}
			if (!Loader.isModLoaded(pettern[0])) {
				Mist.logger.warn("Cannot found the modId \"" + pettern[0] + "\" from mobsBlackList line " + (i + 1));
				continue;
			} else if (pettern[0].equals(Mist.MODID)) {
				Mist.logger.warn("Misty mobs cannot add to the blacklist (mobsBlackList, line " + (i + 1) + ")");
				continue;
			} else {
				if (!pettern[1].equals("*")) {
					ResourceLocation res = new ResourceLocation(pettern[0], pettern[1]);
					if (!ForgeRegistries.ENTITIES.containsKey(res)) {
						Mist.logger.warn("Cannot found the mob \"" + pettern[0] + ":" + pettern[1] + "\" from mobsBlackList line " + (i + 1));
						continue;
					} else if (MistRegistry.mobsBlackList.contains(res)) {
						Mist.logger.warn("Mob \"" + pettern[0] + ":" + pettern[1] + "\" is already exist (mobsBlackList, line " + (i + 1) + ")");
						continue;
					} else {
						MistRegistry.mobsBlackList.add(res);
					}
				} else MistRegistry.mobsDimsBlackList.add(pettern[0]);
			}
		}
	}
}