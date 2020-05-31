package ru.liahim.mist.item.food;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.IColoredItem;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.capability.handler.IMistCapaHandler;
import ru.liahim.mist.init.ItemColoring;
import ru.liahim.mist.init.ModAdvancements;
import ru.liahim.mist.init.ModConfig;
import ru.liahim.mist.util.RomanNumber;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMistSoup extends ItemSoup implements IColoredItem, IMistFood {

	@Override
	public IItemColor getItemColor() {
		return ItemColoring.ITEM_SOUP_COLORING;
	}

	private final int portion;
	private final boolean drink;

	public ItemMistSoup(int portion, boolean drink) {
		super(0);
		this.portion = portion;
		this.drink = drink;
		this.addPropertyOverride(new ResourceLocation("portion"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
				return getCurrentPortion(stack);
			}
		});
	}

	public ItemMistSoup(int portion) {
		this(portion, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		if (GuiScreen.isShiftKeyDown()) {
			ItemStack[] ingredients = this.getMainFood(stack);
			if (ingredients != null) {
				for (ItemStack food : ingredients) {
					if (!food.isEmpty()) tooltip.add("-" + food.getDisplayName());
				}
				if (ModConfig.campfire.showSoupEffects) {
					float toxic = this.getToxic(stack)/100;
					if (toxic != 0) {
						StringBuilder sb = new StringBuilder();
						sb.append(I18n.format("item.mist.soup_toxic.name"));
						sb.append(": ");
						sb.append(toxic > 0 ? TextFormatting.RED : TextFormatting.GREEN);
						sb.append(String.format("%.2f", toxic));
						sb.append("%");
						tooltip.add(sb.toString());
					}
					PotionEffect[] potions = this.getPotions(stack);
					if (potions != null) {
						for (PotionEffect potion : potions) {
							String mess = "";
							mess += (potion.getPotion().isBadEffect() ? TextFormatting.RED : TextFormatting.GRAY);
							mess += I18n.format(potion.getEffectName()).trim();
							mess += RomanNumber.toRoman(potion.getAmplifier() + 1);
			
							if (potion.getDuration() > 20) {
								mess += " (" + Potion.getPotionDurationString(potion, 1) + ")";
							}

							mess += TextFormatting.RESET;
							tooltip.add(mess);
						}
					}
				}
			}
		} else if (this.getCurrentPortion(stack) > 0) tooltip.add(I18n.format("item.mist.more_info.tooltip"));
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;
			int toxic = (int) this.getToxic(stack);
			if (toxic != 0) {
				IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
				if (capa != null) capa.addToxic(toxic);
				if (player instanceof EntityPlayerMP) ModAdvancements.CONSUME_TOXIC.trigger((EntityPlayerMP) player, stack, Float.valueOf(toxic));
			}
			player.getFoodStats().addStats(this, stack);
			if (player instanceof EntityPlayerMP) {
				ItemStack mask = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
				if (!IMask.isMask(mask)) {
					IMistCapaHandler capa = IMistCapaHandler.getHandler(player);
					if (capa != null) mask = capa.getMask();
				}
				if (!IMask.canEat(mask)) ModAdvancements.GLASS_CONTAINER.trigger((EntityPlayerMP) player, stack, this.getToxic(stack));
			}
			world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5F, world.rand.nextFloat() * 0.1F + 0.9F);
			this.onFoodEaten(stack, world, player);
			player.addStat(StatList.getObjectUseStats(this));

			if (player instanceof EntityPlayerMP) {
				CriteriaTriggers.CONSUME_ITEM.trigger((EntityPlayerMP) player, stack);
			}
		}
		int i = this.getCurrentPortion(stack) - 1;
		if (i <= 0) return this.getContainerItem(stack);
		else this.setPortion(stack, i);
		return stack;
	}

	@Override
	public String getUnlocalizedName() {
		return "item.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName();
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String heal = "";
		int h = this.getHealAmount(stack);
		if (h > 0) {
			if (this != MistItems.SOUP) return MistItems.SOUP.getItemStackDisplayName(stack);
			for (int i = 0; i < 10; ++i) {
				if (h > 1) { heal = "\u25cf" + heal; h -= 2; } // Full
				else if (h == 1) { heal = "\u25d1" + heal; h = 0; } // Medium
				else heal = "\u25cb" + heal; // Empty
			}
		}
		return super.getItemStackDisplayName(stack) + " " + heal;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if (!world.isRemote) {
			PotionEffect[] potions = getPotions(stack);
			if (potions != null) {
				for (PotionEffect po : potions) player.addPotionEffect(po);
			}
		}
	}

	@Override
	public int getHealAmount(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SoupHealAmount")) {
			return (int) stack.getTagCompound().getFloat("SoupHealAmount");
		}
		return super.getHealAmount(stack);
	}

	@Override
	public float getSaturationModifier(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("SoupSaturation")) {
			return stack.getTagCompound().getFloat("SoupSaturation");
		}
		return super.getSaturationModifier(stack);
	}

	public ItemStack[] getMainFood(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("MainFood")) {
			NBTTagList tagList = stack.getTagCompound().getTagList("MainFood", 10);
			ItemStack[] mainFood = new ItemStack[4];
			for (int i = 0; i < 4; ++i) {
				if (i < tagList.tagCount()) {
					mainFood[i] = new ItemStack(tagList.getCompoundTagAt(i));
				} else mainFood[i] = ItemStack.EMPTY;
			}
			return mainFood;
		}
		return null;
	}

	public ItemStack[] getFoodColors(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("FoodColors")) {
			NBTTagList tagList = stack.getTagCompound().getTagList("FoodColors", 10);
			ItemStack[] mainFood = new ItemStack[4];
			for (int i = 0; i < 4; ++i) {
				if (i < tagList.tagCount()) {
					mainFood[i] = new ItemStack(tagList.getCompoundTagAt(i));
				} else mainFood[i] = ItemStack.EMPTY;
			}
			return mainFood;
		}
		return null;
	}

	public float[] getFoodPercent(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("FoodPercent")) {
			NBTTagList tagList = stack.getTagCompound().getTagList("FoodPercent", 10);
			float[] mainFood = new float[4];
			for (int i = 0; i < 4; ++i) {
				if (i < tagList.tagCount()) {
					mainFood[i] = tagList.getCompoundTagAt(i).getFloat("Percent");
				} else mainFood[i] = 0;
			}
			return mainFood;
		}
		return null;
	}

	@Override
	public PotionEffect[] getPotions(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Potions")) {
			NBTTagList tagList = stack.getTagCompound().getTagList("Potions", 10);
			if (tagList.tagCount() > 0) {
				PotionEffect[] potions = new PotionEffect[tagList.tagCount()];
				for (int i = 0; i < tagList.tagCount(); ++i) {
					potions[i] = PotionEffect.readCustomPotionEffectFromNBT(tagList.getCompoundTagAt(i));
				}
				return potions;
			}
		}
		return null;
	}

	@Override
	public float getToxic(ItemStack stack) {
		return stack.hasTagCompound() ? (int) stack.getTagCompound().getFloat("SoupToxic") : 0;
	}

	@Override
	public float getProbability(ItemStack stack) {
		return 1;
	}

	public int getCurrentPortion(ItemStack stack) {
		return stack.hasTagCompound() ? stack.getTagCompound().getInteger("SoupPortion") : 0;
	}

	public void setPortion(ItemStack stack, int portion) {
		if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("SoupPortion", portion);
	}

	public int getMaxPortion() {
		return this.portion;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return this.drink ? EnumAction.DRINK : EnumAction.EAT;
	}

	public static ItemStack getSoupStack(ItemStack stack) {
		if (stack.getItem() == Items.BOWL) return new ItemStack(MistItems.SOUP);
		else if (stack.getItem() == MistItems.GLASS_CONTAINER) return stack.copy();
		else return ItemStack.EMPTY;
	}
}