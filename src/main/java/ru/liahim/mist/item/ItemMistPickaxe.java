package ru.liahim.mist.item;

import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;

public class ItemMistPickaxe extends ItemPickaxe {

	public ItemMistPickaxe(ToolMaterial material) {
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