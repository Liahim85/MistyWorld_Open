package ru.liahim.mist.init.recipe;

import javax.annotation.Nullable;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;
import ru.liahim.mist.api.MistTags;
import ru.liahim.mist.api.item.IMask;
import ru.liahim.mist.item.ItemMistSuit;

public class RecipesInnerSuit extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private ItemStack armor = ItemStack.EMPTY;
	private ItemStack suit = ItemStack.EMPTY;
	private ItemStack inner = ItemStack.EMPTY;

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		ItemStack stack = ItemStack.EMPTY;
		armor = ItemStack.EMPTY;
		suit = ItemStack.EMPTY;
		inner = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ItemMistSuit) {
					if (!suit.isEmpty()) return false;
					else suit = stack;
				} else if (stack.getItem() instanceof ItemArmor && !IMask.isMask(stack)) {
					if (!armor.isEmpty()) return false;
					else {
						NBTTagCompound tag = stack.getSubCompound(MistTags.nbtInnerSuitTag);
						if (tag != null) inner = new ItemStack(tag);
						armor = stack;
					}
				} else return false;
			}
		}
		boolean out = !armor.isEmpty() && !inner.isEmpty() && suit.isEmpty();
		boolean in = !armor.isEmpty() && !suit.isEmpty() && inner.isEmpty() && ((ItemArmor)armor.getItem()).armorType == ((ItemMistSuit)suit.getItem()).armorType;
		return in ^ out;
	}

	@Override
	@Nullable
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		if (!armor.isEmpty()) {
			if (!inner.isEmpty()) {
				return inner.copy();
			} else if (!suit.isEmpty()) {
				ItemStack stack = armor.copy();
				if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
				stack.getTagCompound().setTag(MistTags.nbtInnerSuitTag, suit.serializeNBT());
				return stack;
			}
		}
		return ItemStack.EMPTY;
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
			ItemStack stack = inv.getStackInSlot(i);
			list.set(i, ForgeHooks.getContainerItem(stack));
			if (stack.getItem() instanceof ItemArmor) {
				NBTTagCompound tag = stack.getSubCompound(MistTags.nbtInnerSuitTag);
				if (tag != null) {
					stack.getTagCompound().removeTag(MistTags.nbtInnerSuitTag);
					list.set(i, stack.copy());
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