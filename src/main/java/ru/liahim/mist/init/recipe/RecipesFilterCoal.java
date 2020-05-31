package ru.liahim.mist.init.recipe;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.item.ItemMistFilter;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesFilterCoal extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {
	
	private ItemStack resultItem = ItemStack.EMPTY;
	private int restDamage;
	private final int maxDamage = new ItemStack(MistItems.FILTER_COAL).getMaxDamage();

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		this.resultItem = ItemStack.EMPTY;
		this.restDamage = 0;
		Item item;
		ItemStack stack = ItemStack.EMPTY;
		ItemStack block = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				item = stack.getItem();
				if (item instanceof ItemBlock && Block.getBlockFromItem(item) == MistBlocks.FILTER_COAL_BLOCK) {
					if (block.isEmpty()) block = stack;
					else return false;
				} else return false;
			}
		}
		if (!block.isEmpty()) {
			float i = 9.0F * (16.0F - block.getItemDamage()) / 16.0F;
			if (i < 1) {
				this.resultItem = new ItemStack(MistItems.FILTER_COAL, 1, (int)Math.ceil(this.maxDamage * (1 - i)));
				this.resultItem = ItemMistFilter.setDamageNBT(this.resultItem, false);
			} else {
				this.resultItem = new ItemStack(MistItems.FILTER_COAL, (int)i);
				this.restDamage = (int)Math.ceil(this.maxDamage * (1 - i + (int)i));
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
		return new ItemStack(MistItems.FILTER_COAL);
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
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(new ItemStack(MistBlocks.FILTER_COAL_BLOCK)));
	}
}