package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemMistGenderNameBlock extends ItemMistSingleNameBlock {

	public ItemMistGenderNameBlock(Block block) {
		super(block);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String s = super.getItemStackDisplayName(stack);
		if (s.length() > 2 && s.substring(s.length() - 2, s.length() - 1).equals("_")) {
			return s.substring(0, s.length() - 2);
		}
		return s;
    }
}