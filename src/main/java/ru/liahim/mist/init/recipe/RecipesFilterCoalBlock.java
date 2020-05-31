package ru.liahim.mist.init.recipe;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.api.item.MistItems;
import ru.liahim.mist.item.ItemMistFilter;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesFilterCoalBlock extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private ItemStack resultItem = ItemStack.EMPTY;
	private int restDamage;
	private final int maxDamage = new ItemStack(MistItems.FILTER_COAL).getMaxDamage() * 9;

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		this.resultItem = ItemStack.EMPTY;
		this.restDamage = 0;
		if (inv.getHeight() != 3 && inv.getWidth() != 3) return false;
		ItemStack stack = ItemStack.EMPTY;
		int damage = 0;
		for (int x = 0; x < 3; ++x) {
			for (int y = 0; y < 3; ++ y) {
				stack = inv.getStackInRowAndColumn(x, y);
				if (!stack.isEmpty() && stack.getItem() == MistItems.FILTER_COAL) {
					damage += stack.getItemDamage();
				} else return false;
			}
		}
		if (damage >= this.maxDamage) return false;
		int i = (int)Math.ceil((float)damage * 16 / this.maxDamage);
		this.resultItem = new ItemStack(MistBlocks.FILTER_COAL_BLOCK, 1, i);
		this.restDamage = this.maxDamage / 9 - (int)(this.maxDamage * (float)i / 16 - damage);
		return true;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return this.resultItem.copy();
	}

	@Override
	public ItemStack getRecipeOutput() {
		return new ItemStack(MistBlocks.FILTER_COAL_BLOCK);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> list = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		if (this.restDamage > 0 && this.restDamage < this.maxDamage / 9) {
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
		return width * height >= 9;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		Ingredient i = Ingredient.fromStacks(new ItemStack(MistItems.FILTER_COAL));
		return NonNullList.from(Ingredient.EMPTY, i, i, i, i, i, i, i, i, i);
	}
}