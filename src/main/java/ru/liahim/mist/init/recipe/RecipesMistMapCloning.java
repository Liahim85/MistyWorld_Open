package ru.liahim.mist.init.recipe;

import ru.liahim.mist.api.item.MistItems;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class RecipesMistMapCloning extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		int i = 0;
		ItemStack itemstack = ItemStack.EMPTY;
		for (int j = 0; j < inv.getSizeInventory(); ++j) {
			ItemStack itemstack1 = inv.getStackInSlot(j);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() == MistItems.MAP_UP || itemstack1.getItem() == MistItems.MAP_DOWN) {
					if (!itemstack.isEmpty()) return false;
					itemstack = itemstack1;
				} else {
					if (itemstack1.getItem() != Items.MAP) return false;
					++i;
				}
			}
		}
		return !itemstack.isEmpty() && i > 0;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		int i = 0;
		ItemStack itemstack = ItemStack.EMPTY;
		for (int j = 0; j < inv.getSizeInventory(); ++j) {
			ItemStack itemstack1 = inv.getStackInSlot(j);
			if (!itemstack1.isEmpty()) {
				if (itemstack1.getItem() == MistItems.MAP_UP || itemstack1.getItem() == MistItems.MAP_DOWN) {
					if (!itemstack.isEmpty()) return ItemStack.EMPTY;
					itemstack = itemstack1;
				} else {
					if (itemstack1.getItem() != Items.MAP) return ItemStack.EMPTY;
					++i;
				}
			}
		}

		if (!itemstack.isEmpty() && i >= 1) {
			ItemStack itemstack2;
			if (itemstack.getItem() == MistItems.MAP_UP)
				itemstack2 = new ItemStack(MistItems.MAP_UP, i + 1, itemstack.getMetadata());
			else itemstack2 = new ItemStack(MistItems.MAP_DOWN, i + 1, itemstack.getMetadata());

			if (itemstack.hasDisplayName()) {
				itemstack2.setStackDisplayName(itemstack.getDisplayName());
			}

			if (itemstack.hasTagCompound()) {
				itemstack2.setTagCompound(itemstack.getTagCompound());
			}

			return itemstack2;
		} else {
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack> withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			nonnulllist.set(i, ForgeHooks.getContainerItem(itemstack));
		}

		return nonnulllist;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width >= 3 && height >= 3;
	}
}