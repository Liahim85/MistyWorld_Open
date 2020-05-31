package ru.liahim.mist.item;

import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;

public class ItemMistAxe extends ItemAxe {

	public ItemMistAxe(ToolMaterial material, float damage, float speed) {
		super(material, damage, speed);
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