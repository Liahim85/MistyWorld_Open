package ru.liahim.mist.init.recipe;

import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.item.ItemMistFilter;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesFilterCoalMix extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	
	private ItemStack resultItem = ItemStack.EMPTY;
	private int restDamage;
	private final int maxDamage = new ItemStack(MistItems.FILTER_COAL).getMaxDamage();

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		this.resultItem = ItemStack.EMPTY;
		this.restDamage = 0;
		int damage = 0;
		int count = 0;
		Item item;
		ItemStack stack = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				item = stack.getItem();
				if (item == MistItems.FILTER_COAL) {
					++count;
					damage += stack.getItemDamage();
				} else return false;
			}
		}
		if (count > 1) {
			float i = ((float)(count * this.maxDamage) - damage) / this.maxDamage;
			if (i < 1) {
				this.resultItem = new ItemStack(MistItems.FILTER_COAL, 1, this.maxDamage * (1 - count) + damage);
				this.resultItem = ItemMistFilter.setDamageNBT(this.resultItem, false);
			} else {
				this.resultItem = new ItemStack(MistItems.FILTER_COAL, (int)i);
				this.restDamage = this.maxDamage * ((int)i + 1 - count) + damage;
			}
		}
		return !this.resultItem.isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return this.resultItem.copy();
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> list = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		if (this.restDamage > 0 && this.restDamage < this.maxDamage) {
			for (int i = 0; i < list.size(); ++i) {
				if (!inv.getStackInSlot(i).isEmpty()) {
					list.set(i, ItemMistFilter.setDamageNBT(new ItemStack(MistItems.FILTER_COAL, 1, this.restDamage), false));
					return list;
				}
			}
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