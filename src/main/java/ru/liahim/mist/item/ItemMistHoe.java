package ru.liahim.mist.item;

import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

public class ItemMistHoe extends ItemHoe {

	public ItemMistHoe(ToolMaterial material) {
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