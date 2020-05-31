package ru.liahim.mist.init.recipe;

import javax.annotation.Nullable;

import ru.liahim.mist.block.gizmos.MistUrn;
import ru.liahim.mist.tileentity.TileEntityUrn.UrnType;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipesUrnDyes extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	private ItemStack resultItem = ItemStack.EMPTY;

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		this.resultItem = ItemStack.EMPTY;
		ItemStack urn = ItemStack.EMPTY;
		ItemStack tool = ItemStack.EMPTY;
		int r = 0, g = 0, b = 0, j = 0;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				if (Block.getBlockFromItem(stack.getItem()) instanceof MistUrn) {
					if (!urn.isEmpty()) return false;
					if (stack.getItemDamage() != 0) return false;
					NBTTagCompound tag = UrnType.getTag(stack);
					if (UrnType.getType(stack, tag).isRare()) return false;
					urn = stack;
				} else {
					if (stack.getItem() == Items.DYE) {
						int color = EnumDyeColor.byDyeDamage(stack.getMetadata()).colorValue;
						r += color >> 16 & 255;
						g += color >> 8 & 255;
						b += color & 255;
						++j;
					} else {
						if (!UrnType.isTool(stack)) return false;
						else if (!tool.isEmpty()) return false;
						tool = stack;
					}
				}
			}
		}
		if (j > 0 && !urn.isEmpty() && (urn.getItemDamage() == 0 || tool.isEmpty())) {
			this.resultItem = urn.copy();
			NBTTagCompound tag;
			r /= j;
			g /= j;
			b /= j;
			int finalColor = r << 16 | g << 8 | b;
			tag = UrnType.getTag(this.resultItem);
			if (tag == null) tag = new NBTTagCompound();
			if (tool.isEmpty()) {
				tag.setInteger("UrnType", UrnType.NORMAL.getId());
				tag.setInteger("TintColor", finalColor);
				tag.setInteger("PatinaColor", -1);
			} else if (UrnType.getPatinaColor(this.resultItem, tag) < 0) {
				tag.setInteger("UrnType", UrnType.byTool(tool).getId());
				tag.setInteger("PatinaColor", finalColor);
			} else return false;
			if (!this.resultItem.hasTagCompound()) this.resultItem.setTagCompound(new NBTTagCompound());
			this.resultItem.getTagCompound().setTag("Urn", tag);
		}
		return !this.resultItem.isEmpty();
	}

	@Override
	@Nullable
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		return this.resultItem.copy();
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