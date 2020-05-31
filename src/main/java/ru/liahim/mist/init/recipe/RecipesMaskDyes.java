package ru.liahim.mist.init.recipe;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.IColoredItem;
import ru.liahim.mist.item.ItemMistArmor;

import com.google.common.collect.Lists;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesMaskDyes extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		ItemStack itemstack = ItemStack.EMPTY;
		List<ItemStack> list = Lists.<ItemStack> newArrayList();
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack itemstack1 = inv.getStackInSlot(i);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() instanceof ItemMistArmor) {
					ItemMistArmor itemarmor = (ItemMistArmor)itemstack1.getItem();
					if (!(itemarmor instanceof IColoredItem) || !itemstack.isEmpty()) {
						return false;
					}
					itemstack = itemstack1;
				} else {
					if (itemstack1.getItem() != Items.DYE) {
						return false;
					}
					list.add(itemstack1);
				}
			}
		}
		return !itemstack.isEmpty() && !list.isEmpty();
	}

	@Override
	@Nullable
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack itemstack = ItemStack.EMPTY;
		int[] aint = new int[3];
		int i = 0;
		int j = 0;
		ItemMistArmor itemarmor = null;
		for (int k = 0; k < inv.getSizeInventory(); ++k) {
			ItemStack itemstack1 = inv.getStackInSlot(k);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() instanceof ItemMistArmor) {
					itemarmor = (ItemMistArmor) itemstack1.getItem();
					if (!(itemarmor instanceof IColoredItem) || !itemstack.isEmpty()) {
						return ItemStack.EMPTY;
					}
					itemstack = itemstack1.copy();
					itemstack.setCount(1);
					if (itemarmor.hasColor(itemstack1)) {
						int l = itemarmor.getColor(itemstack);
						float f = (l >> 16 & 255) / 255.0F;
						float f1 = (l >> 8 & 255) / 255.0F;
						float f2 = (l & 255) / 255.0F;
						i = (int) (i + Math.max(f, Math.max(f1, f2)) * 255.0F);
						aint[0] = (int)(aint[0] + f * 255.0F);
						aint[1] = (int)(aint[1] + f1 * 255.0F);
						aint[2] = (int)(aint[2] + f2 * 255.0F);
						++j;
					}
				} else {
					if (itemstack1.getItem() != Items.DYE) {
						return ItemStack.EMPTY;
					}
					float[] afloat = EntitySheep.getDyeRgb(EnumDyeColor.byDyeDamage(itemstack1.getMetadata()));
					int l1 = (int)(afloat[0] * 255.0F);
					int i2 = (int)(afloat[1] * 255.0F);
					int j2 = (int)(afloat[2] * 255.0F);
					i += Math.max(l1, Math.max(i2, j2));
					aint[0] += l1;
					aint[1] += i2;
					aint[2] += j2;
					++j;
				}
			}
		}
		if (itemarmor == null) {
			return ItemStack.EMPTY;
		} else {
			int i1 = aint[0] / j;
			int j1 = aint[1] / j;
			int k1 = aint[2] / j;
			float f3 = (float) i / (float) j;
			float f4 = Math.max(i1, Math.max(j1, k1));
			i1 = (int)(i1 * f3 / f4);
			j1 = (int)(j1 * f3 / f4);
			k1 = (int)(k1 * f3 / f4);
			int lvt_12_3_ = (i1 << 8) + j1;
			lvt_12_3_ = (lvt_12_3_ << 8) + k1;
			itemarmor.setColor(itemstack, lvt_12_3_);
			return itemstack;
		}
	}

	@Override
	@Nullable
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> list = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < list.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			list.set(i, ForgeHooks.getContainerItem(itemstack));
		}
		return list;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}