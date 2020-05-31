package ru.liahim.mist.block.upperplant;

import java.util.Random;

import ru.liahim.mist.api.block.IWettable;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.block.MistSoil;
import ru.liahim.mist.tileentity.TileEntityMycelium;
import ru.liahim.mist.util.SoilHelper;
import ru.liahim.mist.world.MistWorld;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.EnumPlantType;

public abstract class MistMushroom extends BlockMushroom {

	public static final PropertyEnum<MushroomType_0> TYPE_0 = PropertyEnum.<MushroomType_0>create("type", MushroomType_0.class);
	public static final PropertyEnum<MushroomType_1> TYPE_1 = PropertyEnum.<MushroomType_1>create("type", MushroomType_1.class);
	public static final NoiseGeneratorPerlin NOISE_0 = new NoiseGeneratorPerlin(new Random(1234L), 1);
	public static final NoiseGeneratorPerlin NOISE_1 = new NoiseGeneratorPerlin(new Random(3456L), 1);

	public MistMushroom() {
		super();
		this.setSoundType(SoundType.PLANT);
	}

	@Override
	public String getUnlocalizedName() {
		return "tile.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		// TODO РїРµСЂРµРїРёСЃР°С‚СЊ РїРѕРґ РІР»Р°Р¶РЅРѕСЃС‚СЊ? Р�Р»Рё СЃРґРµР»Р°С‚СЊ РїР»Р°РІРЅРѕРµ СѓРІРµР»РёС‡РµРЅРёРµ С€Р°РЅСЃР°.
		IBlockState downState = world.getBlockState(pos.down());
		if (MistWorld.isPosInFog(world, pos.down()) || (downState.getBlock() instanceof IWettable && ((IWettable)downState.getBlock()).isAcid())) {
			world.setBlockToAir(pos);
		} else {
			if (rand.nextInt(4) == 0 && !isPair(state, world.getTileEntity(pos.down()))) {
				int time = (int) (world.getWorldTime() % 24000);
				if (time >= 6000 && time < 12000) { //&& !world.isRaining()) {
					world.setBlockToAir(pos);
					if (downState.getBlock() instanceof MistSoil) {
						int hum = SoilHelper.getHumus(downState);
						if (hum < 2) SoilHelper.setSoil(world, pos.down(), downState, hum + 1, 2);
					}
				}
			}
		}
	}

	public static boolean isPair(IBlockState mushroom, TileEntity te) {
		if (te instanceof TileEntityMycelium) return mushroom == ((TileEntityMycelium)te).getMushroomState();
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			world.setBlockToAir(pos);
			spawnAsEntity(world, pos, new ItemStack(getItemDropped(state, world.rand, 0), 1, damageDropped(state)));
		}
		return false;		
	}

	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
		return world.isSideSolid(pos.down(), EnumFacing.UP) &&
				world.getBlockState(pos.down()).getBlock() instanceof IWettable &&
				!((IWettable)world.getBlockState(pos.down()).getBlock()).isAcid();
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Cave;
	}

	public abstract PropertyEnum getTypeProperty();
	public abstract String getTypeName(int meta);
	public abstract IFoodProperty getFoodProperty(int meta);

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return MistItems.MUSHROOMS_FOOD;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(MistItems.MUSHROOMS_FOOD, 1, this.damageDropped(state));
	}

	@Override
	public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) { return false; }

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) { return false; }

	@Override
	public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {}

	public enum MushroomType_0 implements IStringSerializable, IFoodProperty {

		BROWN(0, "brown", 2, 0.5F, 5, 0.6F),
		BLACK(1, "black", 2, 0.3F, 4, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.SLOWNESS, 300, 2, false, false)}, 0.2F),
		GRAY(2, "gray", 2, 0.4F, 4, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.SLOWNESS, 100, 1, false, false)}, 0.1F),
		RED(3, "red", 2, 0.4F, 4, 0.6F),
		CORAL(4, "coral", 1, 0.1F, 3, 0.6F),
		ORANGE(5, "orange", 1, 0.4F, 3, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 300, 3, false, false)}, 0.2F),
		YELLOW(6, "yellow", 2, 0.4F, 4, 0.6F),
		BLUE(7, "blue", 1, 0.5F, 3, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.SLOWNESS, 600, 2, false, false), new PotionEffect(MobEffects.WEAKNESS, 600, 2, false, false)}, 0.2F),
		PURPLE(8, "purple", 2, 0.2F, 5, 0.6F),
		MARSH(9, "marsh", 1, 0.4F, 3, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.SLOWNESS, 400, 1, false, false)}, 0.2F),
		PINK(10, "pink", 1, 0.3F, 2, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.SLOWNESS, 400, 1, false, false)}, 0.3F),
		PUFF(11, "puff", 1, 0.1F, 2, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.MINING_FATIGUE, 1200, 1, false, false)}, 0.3F),
		SAND(12, "sand", 1, 0.2F, 3, 0.6F, new PotionEffect[] {new PotionEffect(MobEffects.SLOWNESS, 200, 3, false, false), new PotionEffect(MobEffects.HUNGER, 200, 1, false, false)}, 0.1F);

		private static final MushroomType_0[] META_LOOKUP = new MushroomType_0[values().length];
		private final int meta;
		private final String name;
		private final int heal;
		private final float saturation;
		private final boolean edable;
		private final PotionEffect[] potion;
		private final float probability;
		private final int cookHeal;
		private final float cookSaturation;
		private final boolean cookEdable;
		private final PotionEffect[] cookPotion;
		private final float cookProbability;

		private MushroomType_0(int meta, String name, int heal, float saturation, boolean edable, int cookHeal, float cookSaturation, boolean cookEdable, PotionEffect[] potion, float probability, PotionEffect[] cookPotion, float cookProbability) {
			this.meta = meta;
			this.name = name;
			this.heal = heal;
			this.saturation = saturation;
			this.edable = edable;
			this.potion = potion;
			this.probability = probability;
			this.cookHeal = cookHeal;
			this.cookSaturation = cookSaturation;
			this.cookEdable = cookEdable;
			this.cookPotion = cookPotion;
			this.cookProbability = cookProbability;
		}

		private MushroomType_0(int meta, String name, int heal, float saturation, int cookHeal, float cookSaturation, PotionEffect[] potion, float probability) {
			this(meta, name, heal, saturation, true, cookHeal, cookSaturation, true, potion, probability, (PotionEffect[])null, 0.0F);
		}

		private MushroomType_0(int meta, String name, int heal, float saturation, int cookHeal, float cookSaturation) {
			this(meta, name, heal, saturation, true, cookHeal, cookSaturation, true, (PotionEffect[])null, 0.0F, (PotionEffect[])null, 0.0F);
		}

		public int getMetadata() {
			return this.meta;
		}

		public static MushroomType_0 byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (MushroomType_0 type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}

		@Override
		public int getHealAmount(boolean isCook) {
			return isCook ? cookHeal : heal;
		}

		@Override
		public float getSaturationModifier(boolean isCook) {
			return isCook ? cookSaturation : saturation;
		}

		@Override
		public PotionEffect[] getPotionEffect(boolean isCook) {
			return isCook ? cookPotion : potion;
		}

		@Override
		public float getProbability(boolean isCook) {
			return isCook ? cookProbability : probability;
		}

		@Override
		public boolean isEdable(boolean isCook) {
			return isCook ? cookEdable : edable;
		}
	}

	//SLOWNESS			Р—Р°РјРµРґР»РµРЅРёРµ
	//MINING_FATIGUE	РЈСЃС‚Р°Р»РѕСЃС‚СЊ
	//INSTANT_DAMAGE	РњРѕРјРµРЅС‚Р°Р»СЊРЅС‹Р№ СѓСЂРѕРЅ
	//NAUSEA			РўРѕС€РЅРѕС‚Р°
	//BLINDNESS			РЎР»РµРїРѕС‚Р°
	//HUNGER			Р“РѕР»РѕРґ
	//WEAKNESS			РЎР»Р°Р±РѕСЃС‚СЊ
	//POISON			РћС‚СЂР°РІР»РµРЅРёРµ
	//WITHER			Р�СЃСЃСѓС€РµРЅРёРµ

	public enum MushroomType_1 implements IStringSerializable, IFoodProperty {

		SPOT(0, "spot", 2, 0.1F, false, 3, 0.6F, true,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 600, 1, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.POISON, 50, 0, false, false)}, 0.3F),
		CUP(1, "cup", 1, 0.3F, false, 2, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 400, 3, false, false), new PotionEffect(MobEffects.POISON, 200, 0, false, false), new PotionEffect(MobEffects.WEAKNESS, 1200, 2, false, false), new PotionEffect(MobEffects.MINING_FATIGUE, 1200, 2, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.POISON, 75, 0, false, false), new PotionEffect(MobEffects.WEAKNESS, 600, 2, false, false), new PotionEffect(MobEffects.MINING_FATIGUE, 600, 2, false, false)}),
		AZURE(2, "azure", 1, 0.3F, false, 2, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 600, 4, false, false), new PotionEffect(MobEffects.POISON, 300, 0, false, false), new PotionEffect(MobEffects.BLINDNESS, 600, 0, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 100, 2, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false), new PotionEffect(MobEffects.BLINDNESS, 300, 0, false, false)}),
		GREEN(3, "green", 1, 0.3F, false, 2, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 1200, 8, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false), new PotionEffect(MobEffects.NAUSEA, 600, 0, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 600, 4, false, false), new PotionEffect(MobEffects.POISON, 25, 0, false, false)}),
		COPPER(4, "copper", 1, 0.2F, false, 2, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 600, 3, false, false), new PotionEffect(MobEffects.POISON, 200, 0, false, false), new PotionEffect(MobEffects.SLOWNESS, 600, 4, false, false), new PotionEffect(MobEffects.MINING_FATIGUE, 600, 4, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 100, 1, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false)}),
		SILVER(5, "silver", 1, 0.1F, false, 2, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.BLINDNESS, 30, 0, false, false), new PotionEffect(MobEffects.INSTANT_DAMAGE, 10, 0, false, false), new PotionEffect(MobEffects.WITHER, 300, 1, false, false), new PotionEffect(MobEffects.HUNGER, 600, 2, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.WITHER, 150, 1, false, false), new PotionEffect(MobEffects.HUNGER, 100, 1, false, false)}),
		BEIGE(6, "beige", 2, 0.2F, false, 3, 0.6F, true,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 400, 2, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false), new PotionEffect(MobEffects.MINING_FATIGUE, 800, 3, false, false)},
				(PotionEffect[])null),
		GOLD(7, "gold", 1, 0.1F, false, 3, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.INSTANT_DAMAGE, 5, 0, false, false), new PotionEffect(MobEffects.POISON, 200, 2, false, false), new PotionEffect(MobEffects.NAUSEA, 200, 0, false, false), new PotionEffect(MobEffects.HUNGER, 400, 3, false, false), new PotionEffect(MobEffects.MINING_FATIGUE, 1200, 4, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.INSTANT_DAMAGE, 5, 0, false, false), new PotionEffect(MobEffects.POISON, 100, 2, false, false), new PotionEffect(MobEffects.NAUSEA, 200, 0, false, false), new PotionEffect(MobEffects.HUNGER, 200, 2, false, false), new PotionEffect(MobEffects.MINING_FATIGUE, 600, 2, false, false)}),
		WHITE(8, "white", 1, 0.1F, false, 2, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 600, 2, false, false), new PotionEffect(MobEffects.POISON, 200, 0, false, false), new PotionEffect(MobEffects.BLINDNESS, 200, 0, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 100, 0, false, false), new PotionEffect(MobEffects.POISON, 25, 0, false, false), new PotionEffect(MobEffects.BLINDNESS, 200, 0, false, false)}),
		VIOLET(9, "violet", 1, 0.1F, false, 3, 0.6F, false,
				new PotionEffect[] {new PotionEffect(MobEffects.INSTANT_DAMAGE, 5, 0, false, false), new PotionEffect(MobEffects.WITHER, 600, 0, false, false), new PotionEffect(MobEffects.HUNGER, 600, 2, false, false), new PotionEffect(MobEffects.WEAKNESS, 1200, 3, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.WITHER, 150, 0, false, false), new PotionEffect(MobEffects.HUNGER, 200, 2, false, false), new PotionEffect(MobEffects.WEAKNESS, 600, 2, false, false)}),
		LILAC(10, "lilac", 1, 0.1F, false, 2, 0.6F, true,
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 600, 1, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false), new PotionEffect(MobEffects.NAUSEA, 40, 0, false, false)},
				(PotionEffect[])null),
		TAN(11, "tan", 1, 0.1F, false, 2, 0.6F, true,
				new PotionEffect[] {new PotionEffect(MobEffects.INSTANT_DAMAGE, 5, 0, false, false), new PotionEffect(MobEffects.POISON, 100, 0, false, false), new PotionEffect(MobEffects.HUNGER, 400, 3, false, false)},
				new PotionEffect[] {new PotionEffect(MobEffects.HUNGER, 400, 3, false, false)}, 0.2F);

		private static final MushroomType_1[] META_LOOKUP = new MushroomType_1[values().length];
		private final int meta;
		private final String name;
		private final int heal;
		private final float saturation;
		private final boolean edable;
		private final PotionEffect[] potion;
		private final float probability;
		private final int cookHeal;
		private final float cookSaturation;
		private final boolean cookEdable;
		private final PotionEffect[] cookPotion;
		private final float cookProbability;

		private MushroomType_1(int meta, String name, int heal, float saturation, boolean edable, int cookHeal, float cookSaturation, boolean cookEdable, PotionEffect[] potion, float probability, PotionEffect[] cookPotion, float cookProbability) {
			this.meta = meta;
			this.name = name;
			this.heal = heal;
			this.saturation = saturation;
			this.edable = edable;
			this.potion = potion;
			this.probability = probability;
			this.cookHeal = cookHeal;
			this.cookSaturation = cookSaturation;
			this.cookEdable = cookEdable;
			this.cookPotion = cookPotion;
			this.cookProbability = cookProbability;
		}

		private MushroomType_1(int meta, String name, int heal, float saturation, boolean edable, int cookHeal, float cookSaturation, boolean cookEdable, PotionEffect[] potion, PotionEffect[] cookPotion, float cookProbability) {
			this(meta, name, heal, saturation, edable, cookHeal, cookSaturation, cookEdable, potion, potion == null ? 0.0F : 1.0F, cookPotion, cookProbability);
		}

		private MushroomType_1(int meta, String name, int heal, float saturation, boolean edable, int cookHeal, float cookSaturation, boolean cookEdable, PotionEffect[] potion, PotionEffect[] cookPotion) {
			this(meta, name, heal, saturation, edable, cookHeal, cookSaturation, cookEdable, potion, potion == null ? 0.0F : 1.0F, cookPotion, cookPotion == null ? 0.0F : 1.0F);
		}

		private MushroomType_1(int meta, String name, int heal, float saturation, boolean edable, int cookHeal, float cookSaturation, boolean cookEdable) {
			this(meta, name, heal, saturation, edable, cookHeal, cookSaturation, cookEdable, (PotionEffect[])null, 0.0F, (PotionEffect[])null, 0.0F);
		}

		public int getMetadata() {
			return this.meta;
		}

		public static MushroomType_1 byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		static {
			for (MushroomType_1 type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}

		@Override
		public int getHealAmount(boolean isCook) {
			return isCook ? cookHeal : heal;
		}

		@Override
		public float getSaturationModifier(boolean isCook) {
			return isCook ? cookSaturation : saturation;
		}

		@Override
		public PotionEffect[] getPotionEffect(boolean isCook) {
			return isCook ? cookPotion : potion;
		}

		@Override
		public float getProbability(boolean isCook) {
			return isCook ? cookProbability : probability;
		}

		@Override
		public boolean isEdable(boolean isCook) {
			return isCook ? cookEdable : edable;
		}
	}

	public interface IFoodProperty {
		public int getHealAmount(boolean isCook);
		public float getSaturationModifier(boolean isCook);
		public PotionEffect[] getPotionEffect(boolean isCook);
		public float getProbability(boolean isCook);
		public boolean isEdable(boolean isCook);
	}
}