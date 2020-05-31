package ru.liahim.mist.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class ItemMistSword extends ItemSword {

	public ItemMistSword(ToolMaterial material) {
		super(material);
	}

	@Override
	public String getUnlocalizedName() {
		return "item.mist." + super.getUnlocalizedName().substring(5);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName();
	}
}