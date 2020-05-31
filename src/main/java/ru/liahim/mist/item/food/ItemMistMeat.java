package ru.liahim.mist.item.food;

import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.common.Mist;
import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistMeat extends ItemFood implements IMistFood {

	private final boolean isCook;

	public ItemMistMeat(boolean isCook) {
		super(0, 0.0F, true);
		this.isCook = isCook;
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.addPropertyOverride(new ResourceLocation("mist:salt"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
				return IMistFood.hasSalt(stack) ? 1 : 0;
			}
		});
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote && world.rand.nextFloat() < this.getProbability(stack)) {
			PotionEffect[] potions = getPotions(stack);
			if (potions != null) {
				for (PotionEffect po : potions) player.addPotionEffect(po);
			}
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String name = MeatType.byMetadata(stack.getItemDamage()).getName();
		return "item.mist.meat_" + name + (this.isCook ? "_c" : "");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String salt = IMistFood.hasSalt(stack) ? I18n.translateToLocal("item.mist.food_salt.tooltip") : "";
		return super.getItemStackDisplayName(stack) + " " + salt;
	}

	@Override
	public int getHealAmount(ItemStack stack) {
		int salt = IMistFood.hasSalt(stack) ? 1 : 0;
		return Math.min(MeatType.byMetadata(stack.getItemDamage()).getAmount(this.isCook) + salt, 20);
	}

	@Override
	public float getSaturationModifier(ItemStack stack) {
		float salt = IMistFood.hasSalt(stack) ? 0.1F : 0;
		return MeatType.byMetadata(stack.getItemDamage()).getSaturation(this.isCook) + salt;
	}

	@SuppressWarnings("unused")
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this == MistItems.MEAT_COOK && this.isInCreativeTab(tab)) {
			for (int i = 0; i < MeatType.values().length; ++i) {
				items.add(new ItemStack(MistItems.MEAT_FOOD, 1, i));
				ItemStack stack = new ItemStack(this, 1, i);
				items.add(stack);
				if (showSaltyFood && Mist.saltymod) {
					stack = stack.copy();
					stack.setTagCompound(new NBTTagCompound());
					stack.getTagCompound().setBoolean(MistTags.saltTag, true);
					items.add(stack);
				}
			}
		}
	}

	@Override
	public PotionEffect[] getPotions(ItemStack stack) {
		if (IMistFood.hasSalt(stack)) {
			PotionEffect[] list = MeatType.byMetadata(stack.getItemDamage()).getEffects(this.isCook);
			PotionEffect[] temp = new PotionEffect[list.length];
			int i = 0;
			for (PotionEffect pe : list) {
				if (pe.getPotion().isBadEffect()) {
					temp[i] = new PotionEffect(pe.getPotion(), pe.getDuration()/2, Math.max(pe.getAmplifier() - 1, 0), false, false);
				} else temp[i] = pe;
				++i;
			}
			return temp;
		}
		return MeatType.byMetadata(stack.getItemDamage()).getEffects(this.isCook);
	}

	@Override
	public float getProbability(ItemStack stack) {
		return MeatType.byMetadata(stack.getItemDamage()).getProbability(this.isCook);
	}

	@Override
	public float getToxic(ItemStack stack) {
		return 0;
	}

	public static enum MeatType implements IStringSerializable {

		MOSSLING		(0, "mossling",			2, 0.3F, 5, 0.7F,	0.3F, new PotionEffect(MobEffects.HUNGER, 600, 0, false, false), new PotionEffect(MobEffects.NAUSEA, 300, 0, false, false)),
		FOREST_RUNNER	(1, "forest_runner",	3, 0.2F, 6, 0.8F),
		MOMO			(2, "momo",				3, 0.3F, 6, 0.8F),
		BARVOG			(3, "barvog",			3, 0.3F, 6, 0.8F,	0.3F, new PotionEffect(MobEffects.HUNGER, 600, 0, false, false), new PotionEffect(MobEffects.SLOWNESS, 400, 0, false, false)),
		PRICKLER		(4, "prickler",			2, 0.3F, 5, 0.7F,	0.1F, new PotionEffect(MobEffects.HUNGER, 400, 0, false, false)),
		CARAVAN			(5, "caravan",			3, 0.3F, 7, 0.8F,	0.5F, new PotionEffect(MobEffects.SLOWNESS, 600, 1, false, false)),
		WULDER			(6, "wulder",			3, 0.3F, 8, 0.8F),
		HORB			(7, "horb",				2, 0.3F, 6, 0.9F,	0.2F, new PotionEffect(MobEffects.HUNGER, 400, 0, false, false), new PotionEffect(MobEffects.SLOWNESS, 400, 0, false, false)),
		SNIFF			(8, "sniff",			2, 0.5F, 7, 0.8F),
		SLOTH			(9, "sloth",			2, 0.2F, 5, 0.8F,	0.4F, new PotionEffect(MobEffects.HUNGER, 600, 0, false, false), new PotionEffect(MobEffects.POISON, 80, 0, false, false), new PotionEffect(MobEffects.NAUSEA, 200, 0, false, false)),
		MONK			(10, "monk",			3, 0.4F, 8, 0.8F),
		GALAGA			(11, "galaga",			2, 0.3F, 6, 0.8F,	0.2F, new PotionEffect(MobEffects.HUNGER, 400, 0, false, false), new PotionEffect(MobEffects.NAUSEA, 300, 0, false, false)),
		HULTER			(12, "hulter",			2, 0.2F, 5, 0.7F,	0.3F, new PotionEffect(MobEffects.HUNGER, 600, 0, false, false)),
		BRACHIODON		(13, "brachiodon",		4, 0.3F, 9, 0.8F,	0.1F, new PotionEffect(MobEffects.SLOWNESS, 400, 0, false, false));

		private final int meta;
		private final String name;
		private final int amount;
		private final float saturation;
		private final int cookAmount;
		private final float cookSaturation;
		private final float probability;
		private final PotionEffect[] effects;
		private static final MeatType[] META_LOOKUP = new MeatType[values().length];

		MeatType(int meta, String name, int amount, float saturation, int cookAmount, float cookSaturation, float probability, PotionEffect... effects) {
			this.meta = meta;
			this.name = name;
			this.amount = amount;
			this.saturation = saturation;
			this.cookAmount = cookAmount;
			this.cookSaturation = cookSaturation;
			this.probability = probability;
			this.effects = effects;
		}

		MeatType(int meta, String name, int amount, float saturation, int cookAmount, float cookSaturation) {
			this(meta, name, amount, saturation, cookAmount, cookSaturation, 0, (PotionEffect[])null);
		}

		public int getMetadata() {
			return this.meta;
		}

		public static MeatType byMetadata(int meta) {
			if (meta < 0 || meta >= META_LOOKUP.length) meta = 0;
			return META_LOOKUP[meta];
		}

		@Override
		public String getName() {
			return this.name;
		}

		public int getAmount(boolean isCook) {
			return isCook ? this.cookAmount : this.amount;
		}

		public float getSaturation(boolean isCook) {
			return isCook ? this.cookSaturation : this.saturation;
		}

		public float getProbability(boolean isCook) {
			return isCook ? 0 : this.probability;
		}

		public PotionEffect[] getEffects(boolean isCook) {
			return isCook ? null : this.effects;
		}

		static {
			for (MeatType type : values()) {
				META_LOOKUP[type.getMetadata()] = type;
			}
		}
	}
}