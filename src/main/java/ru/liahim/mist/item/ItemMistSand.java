package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemMistSand extends ItemMistGenderNameBlock {

	public ItemMistSand(Block block) {
		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return stack.getItemDamage() > 1 ? "tile.mist.red_sand" : "tile.mist.sand";
	}
}