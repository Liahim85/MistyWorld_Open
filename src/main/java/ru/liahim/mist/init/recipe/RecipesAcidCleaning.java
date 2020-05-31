package ru.liahim.mist.init.recipe;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.block.MistBlocks;
import ru.liahim.mist.init.ModRecipes;

import com.google.common.collect.Lists;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesAcidCleaning extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private final ItemStack recipeOutput = new ItemStack(Items.WATER_BUCKET);
	private final ItemStack sponge = new ItemStack(MistBlocks.SPONGE, 1, 13);

	@Override
	@Nullable
	public ItemStack getRecipeOutput() {
		return this.recipeOutput;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> list = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < list.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			if (!itemstack.isEmpty() && itemstack.isItemEqual(sponge)) {
				list.set(i, new ItemStack(MistBlocks.SPONGE, 1, 15));
			}
		}
		return list;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		List<ItemStack> list = Lists.newArrayList(sponge, ModRecipes.ACID_BUCKET);
		for (int i = 0; i < inv.getHeight(); ++i) {
			for (int j = 0; j < inv.getWidth(); ++j) {
				ItemStack itemstack = inv.getStackInRowAndColumn(j, i);
				if (!itemstack.isEmpty()) {
					boolean flag = false;
					for (ItemStack itemstack1 : list) {
						if (itemstack.getItem() == itemstack1.getItem()
								&& (itemstack1.getMetadata() == 32767 || itemstack.getMetadata() == itemstack1.getMetadata())) {
							flag = true;
							list.remove(itemstack1);
							break;
						}
					}
					if (!flag) return false;
				}
			}
		}
		return list.isEmpty();
	}

	@Override
	@Nullable
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return this.recipeOutput.copy();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(ModRecipes.ACID_BUCKET), Ingredient.fromStacks(sponge));
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
}