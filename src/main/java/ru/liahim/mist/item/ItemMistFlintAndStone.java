package ru.liahim.mist.item;

import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemStack;

public class ItemMistFlintAndStone extends ItemFlintAndSteel {

	@Override
	public String getUnlocalizedName() {
		return "item.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName();
	}
}