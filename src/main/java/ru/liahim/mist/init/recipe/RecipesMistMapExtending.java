package ru.liahim.mist.init.recipe;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

public class RecipesMistMapExtending extends ShapedRecipes {
	
	private final Item map;

	public RecipesMistMapExtending(Item map) {
		super("", 3, 3, NonNullList.from(Ingredient.EMPTY, Ingredient.fromItems(Items.PAPER),
				Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(Items.PAPER),
				Ingredient.fromItems(Items.PAPER), Ingredient.fromItem(map),
				Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(Items.PAPER),
				Ingredient.fromItems(Items.PAPER), Ingredient.fromItems(Items.PAPER)), new ItemStack(Items.MAP));
		this.map = map;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		if (!super.matches(inv, world)) {
			return false;
		} else {
			ItemStack itemstack = ItemStack.EMPTY;
			for (int i = 0; i < inv.getSizeInventory() && itemstack.isEmpty(); ++i) {
				ItemStack itemstack1 = inv.getStackInSlot(i);
				if (itemstack1.getItem() == this.map) {
					itemstack = itemstack1;
				}
			}

			if (itemstack.isEmpty()) {
				return false;
			} else {
				MapData mapdata = ((ItemMap)this.map).getMapData(itemstack, world);

				if (mapdata == null) {
					return false;
				} else if (this.isExplorationMap(mapdata)) {
					return false;
				} else {
					return mapdata.scale < 4;
				}
			}
		}
	}

	private boolean isExplorationMap(MapData mapdata) {
		if (mapdata.mapDecorations != null) {
			for (MapDecoration mapdecoration : mapdata.mapDecorations.values()) {
				if (mapdecoration.getType() == MapDecoration.Type.MANSION
						|| mapdecoration.getType() == MapDecoration.Type.MONUMENT) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		ItemStack itemstack = ItemStack.EMPTY;

		for (int i = 0; i < inv.getSizeInventory() && itemstack.isEmpty(); ++i) {
			ItemStack itemstack1 = inv.getStackInSlot(i);

			if (itemstack1.getItem() == this.map) {
				itemstack = itemstack1;
			}
		}

		itemstack = itemstack.copy();
		itemstack.setCount(1);

		if (itemstack.getTagCompound() == null) {
			itemstack.setTagCompound(new NBTTagCompound());
		}

		itemstack.getTagCompound().setInteger("map_scale_direction", 1);
		return itemstack;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}
}