package ru.liahim.mist.init.recipe;

import java.util.ArrayList;
import javax.annotation.Nullable;

import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.item.IMistFood;
import ru.liahim.mist.api.item.MistItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesSaltyFood extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private ArrayList<Item> items = createList(MistItems.MEAT_COOK, MistItems.MUSHROOMS_COOK);
	private ItemStack saltyStack = ItemStack.EMPTY;

	@Override
	@Nullable
	public ItemStack getRecipeOutput() {
		return saltyStack;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		return NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		saltyStack = ItemStack.EMPTY;
		ItemStack food = ItemStack.EMPTY;
		ItemStack stack;
		boolean salt = false;
		for (int i = 0; i < inv.getHeight(); ++i) {
			for (int j = 0; j < inv.getWidth(); ++j) {
				stack = inv.getStackInRowAndColumn(j, i);
				if (!stack.isEmpty()) {
					if (salt && !food.isEmpty()) return false;
					if (this.items.contains(stack.getItem()) && !IMistFood.hasSalt(stack)) {
						if (food.isEmpty()) food = stack;
						else return false;
					} else if (OreDictionary.containsMatch(false, OreDictionary.getOres("dustSalt"), stack)) {
						if (!salt) salt = true;
						else return false;
					}
				}
			}
		}
		if (salt && !food.isEmpty()) {
			saltyStack = food.copy();
			saltyStack.setCount(1);
			if (!saltyStack.hasTagCompound()) saltyStack.setTagCompound(new NBTTagCompound());
			saltyStack.getTagCompound().setBoolean(MistTags.saltTag, true);
		}
		return !saltyStack.isEmpty();
	}

	@Override
	@Nullable
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return saltyStack.copy();
	}

	private static ArrayList<Item> createList(Item... items) {
		ArrayList<Item> list = new ArrayList<Item>();
		for (Item item : items) if (item != null) list.add(item);
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