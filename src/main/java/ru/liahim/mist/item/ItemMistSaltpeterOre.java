package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemMistSaltpeterOre extends ItemMistSingleNameBlock {

	public ItemMistSaltpeterOre(Block block) {
		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return stack.getItemDamage() == 0 ? "tile.mist.saltpeter_ore" : "tile.mist.salt_ore";
	}
}