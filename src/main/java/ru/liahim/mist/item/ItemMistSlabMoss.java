package ru.liahim.mist.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemMistSlabMoss extends ItemMistSlab {

	public ItemMistSlabMoss(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		String name = this.fullBlock.getUnlocalizedName();
		return stack.getItemDamage() > 0 ? name + "_moss_slab" : name + "_slab";
	}
}