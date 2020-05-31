package ru.liahim.mist.item;

import javax.annotation.Nullable;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemCarrotOnAStick;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.entity.EntityAnimalMist;

public class ItemMistFoodOnStick extends ItemCarrotOnAStick {

	public static final int[] mushroomIndex = new int[] { 9, 10, 12, 17, 27 };

	public ItemMistFoodOnStick() {
		super();
        this.setMaxDamage(28);
		this.addPropertyOverride(new ResourceLocation("mist:food"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
				ItemStack food = getFood(stack);
				if (food.getItem() == MistItems.MUSHROOMS_FOOD) {
					int i = food.getMetadata();
					if (i == 9) return 1;		// Marsh
					else if (i == 10) return 2;	// Pink
					else if (i == 12) return 3;	// Sand
					else if (i == 17) return 4;	// Cup
					else if (i == 27) return 5;	// Tan
					else return 0;
				}
				return 0;
			}
		});
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
		ItemStack food = ItemMistFoodOnStick.getFood(stack);
		String name = food.getItem().getItemStackDisplayName(food);
		return name + " " + super.getItemStackDisplayName(stack);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote) return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		else {
			if (player.isRiding() && EntityAnimalMist.isDriven(player.getRidingEntity())) {
				EntityAnimalMist entity = (EntityAnimalMist) player.getRidingEntity();
				if (stack.getMaxDamage() - stack.getMetadata() >= 7 && entity.isBreedingItem(getFood(stack)) && entity.boost()) {
					stack.damageItem(7, player);
					if (stack.getMaxDamage() - stack.getMetadata() <= 0) {
						ItemStack rod = new ItemStack(Items.FISHING_ROD);
						NBTTagCompound tag = stack.getTagCompound();
						tag.removeTag(MistTags.foodOnStickTag);
						rod.setTagCompound(tag);
						return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, rod);
					}
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}
			}
			player.addStat(StatList.getObjectUseStats(this));
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			for (int i : mushroomIndex) {
				ItemStack stack = new ItemStack(this);
				stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setTag(MistTags.foodOnStickTag, new ItemStack(MistItems.MUSHROOMS_FOOD, 1, i).writeToNBT(new NBTTagCompound()));
				items.add(stack);
			}
		}
	}

	public static ItemStack getFood(ItemStack stack) {
		NBTTagCompound tag = stack.getSubCompound(MistTags.foodOnStickTag);
		return tag != null ? new ItemStack(tag) : ItemStack.EMPTY;
	}
}