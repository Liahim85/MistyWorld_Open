package ru.liahim.mist.init.recipe;

import java.util.List;

import javax.annotation.Nullable;

import ru.liahim.mist.api.item.MistItems;
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

public class RecipesAcidBuket extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private final ItemStack slime = new ItemStack(MistItems.SPONGE_SLIME);
	private final ItemStack buket = new ItemStack(Items.WATER_BUCKET);
	private final List<ItemStack> list = Lists.newArrayList(buket, slime);

	@Override
	@Nullable
	public ItemStack getRecipeOutput() {
		return ModRecipes.ACID_BUCKET;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		return NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		List<ItemStack> list = Lists.newArrayList(this.list);
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
		return ModRecipes.ACID_BUCKET.copy();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.from(Ingredient.EMPTY, Ingredient.fromStacks(buket), Ingredient.fromStacks(slime));
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
}