package ru.liahim.mist.item;

import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;

public class ItemMistShovel extends ItemSpade {

	public ItemMistShovel(ToolMaterial material) {
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