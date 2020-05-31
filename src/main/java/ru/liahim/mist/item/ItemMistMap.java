package ru.liahim.mist.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;

public class ItemMistMap extends ItemMap {

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return Items.FILLED_MAP.getUnlocalizedName();
	}
}